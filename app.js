const $ = id => document.getElementById(id);
const post = (url, data) => fetch(url, { method: "POST", body: new URLSearchParams(data) }).then(load);

$("date").value = new Date().toLocaleDateString("en-CA");   // today, like 2026-11-10

// Ask the Java server for the day's tasks and draw them.
async function load() {
  const data = await (await fetch("/api/tasks?date=" + $("date").value)).json();
  const done = data.tasks.filter(t => t.done).length;
  const next = data.tasks.find(t => t.id === data.next);

  $("count").textContent = done + " of " + data.tasks.length + " done";
  $("bar").max = data.tasks.length || 1;
  $("bar").value = done;
  $("next").textContent = next ? "Do this next: " + next.title : "";

  $("list").innerHTML = "";
  for (const t of data.tasks) {
    const li = document.createElement("li");
    li.className = "p" + t.priority + (t.done ? " done" : "") + (t.overdue ? " late" : "");
    li.innerHTML = `<input type="checkbox" ${t.done ? "checked" : ""}> <span></span>
                    <small>${t.time || "any time"}${t.overdue ? " · overdue" : ""}</small> <button>✕</button>`;
    li.querySelector("span").textContent = t.title;
    li.querySelector("input").onchange = () => post("/api/toggle", { id: t.id });
    li.querySelector("button").onclick = () => post("/api/delete", { id: t.id });
    $("list").append(li);
  }
}

// Add a task (the browser makes sure the title is not empty).
$("form").onsubmit = e => {
  e.preventDefault();
  post("/api/add", { ...Object.fromEntries(new FormData(e.target)), date: $("date").value });
  e.target.reset();
};

$("date").onchange = load;
load();
