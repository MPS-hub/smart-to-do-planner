import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

// Run this file, then open http://localhost:8080
public class Main {
    static List<Task> tasks = new ArrayList<>();
    static int nextId = 1;
    static Path file = Paths.get("tasks.txt");

    public static void main(String[] args) throws IOException {
        load();
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // 1) Serve the web page files
        server.createContext("/", ex -> {
            String path = ex.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            if (!Set.of("/index.html", "/style.css", "/app.js").contains(path)) {
                ex.sendResponseHeaders(404, -1);
                ex.close();
                return;
            }
            String type = path.endsWith(".css") ? "text/css" : path.endsWith(".js") ? "text/javascript" : "text/html";
            reply(ex, type, new String(Files.readAllBytes(Paths.get("web" + path))));
        });

        // 2) The API the page talks to
        server.createContext("/api", ex -> {
            String path = ex.getRequestURI().getPath();
            boolean isGet = ex.getRequestMethod().equals("GET");
            Map<String, String> in = parse(isGet ? ex.getRequestURI().getRawQuery()
                                                 : new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            String out = "ok";

            if (path.equals("/api/add")) {
                tasks.add(new Task(nextId++, in.get("title").trim(), in.get("date"),
                                   in.getOrDefault("time", ""), Integer.parseInt(in.getOrDefault("priority", "2")), false));
                save();
            } else if (path.equals("/api/toggle")) {
                for (Task t : tasks) if (t.id == Integer.parseInt(in.get("id"))) t.done = !t.done;
                save();
            } else if (path.equals("/api/delete")) {
                tasks.removeIf(t -> t.id == Integer.parseInt(in.get("id")));
                save();
            } else {
                out = dayAsJson(in.get("date"));
            }
            reply(ex, "application/json", out);
        });

        server.start();
        System.out.println("Planner running at http://localhost:8080");
    }

    // The tasks of one day, sorted: not done first, then by time, then by priority.
    static String dayAsJson(String date) {
        List<Task> day = new ArrayList<>();
        for (Task t : tasks) if (t.date.equals(date)) day.add(t);

        day.sort(Comparator.comparing((Task t) -> t.done)
                .thenComparing(t -> t.time.isEmpty() ? "99:99" : t.time)
                .thenComparingInt(t -> t.priority));

        // "Do this next" = most important task that is not done and not overdue
        Task next = null;
        for (Task t : day)
            if (!t.done && !t.overdue() && (next == null || t.priority < next.priority)) next = t;

        List<String> items = new ArrayList<>();
        for (Task t : day) items.add(t.toJson());
        return "{\"next\":" + (next == null ? 0 : next.id) + ",\"tasks\":[" + String.join(",", items) + "]}";
    }

    // Save and load: one task per line, parts separated by a tab.
    static void save() throws IOException {
        List<String> lines = new ArrayList<>();
        for (Task t : tasks)
            lines.add(String.join("\t", "" + t.id, "" + t.done, "" + t.priority, t.date, t.time, t.title));
        Files.write(file, lines);
    }

    static void load() throws IOException {
        if (!Files.exists(file)) return;
        for (String line : Files.readAllLines(file)) {
            String[] p = line.split("\t", -1);
            int id = Integer.parseInt(p[0]);
            tasks.add(new Task(id, p[5], p[3], p[4], Integer.parseInt(p[2]), p[1].equals("true")));
            nextId = Math.max(nextId, id + 1);
        }
    }

    // "a=1&b=hello%20there"  ->  {a=1, b="hello there"}
    static Map<String, String> parse(String text) {
        Map<String, String> map = new HashMap<>();
        if (text == null) return map;
        for (String pair : text.split("&")) {
            String[] kv = pair.split("=", 2);
            map.put(kv[0], kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "");
        }
        return map;
    }

    static void reply(HttpExchange ex, String type, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type + "; charset=utf-8");
        ex.sendResponseHeaders(200, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }
}
