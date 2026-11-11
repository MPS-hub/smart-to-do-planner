import java.time.*;

// One to-do item.
public class Task {
    int id;
    String title, date, time;   // date "2026-11-10", time "09:30" (or "")
    int priority;               // 1 = High, 2 = Medium, 3 = Low
    boolean done;

    Task(int id, String title, String date, String time, int priority, boolean done) {
        this.id = id; this.title = title; this.date = date;
        this.time = time; this.priority = priority; this.done = done;
    }

    // Overdue = not done, and its time (or the end of its day) has passed.
    boolean overdue() {
        LocalTime t = time.isEmpty() ? LocalTime.MAX : LocalTime.parse(time);
        return !done && LocalDate.parse(date).atTime(t).isBefore(LocalDateTime.now());
    }

    String toJson() {
        return "{\"id\":" + id + ",\"title\":\"" + title.replace("\\", "\\\\").replace("\"", "\\\"")
             + "\",\"date\":\"" + date + "\",\"time\":\"" + time + "\",\"priority\":" + priority
             + ",\"done\":" + done + ",\"overdue\":" + overdue() + "}";
    }
}
