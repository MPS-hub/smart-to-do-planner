# Smart Daily Planner

A tiny to-do planner in plain Java (no libraries) with a plain HTML/CSS/JS frontend.

## Run
Needs Java 11 or newer.

    cd smart-planner
    javac -d out src/*.java
    java -cp out Main

Then open http://localhost:8080

## Files
    src/Task.java       one task (+ overdue check)
    src/Main.java       web server, sorting, "do this next", save/load to tasks.txt
    web/index.html      page structure
    web/style.css       looks
    web/app.js          page behaviour (talks to the Java server)

## Smart features
- Tasks sort by: not done first, then time, then priority.
- Overdue tasks (time passed today, or an earlier day) are marked.
- "Do this next" picks the highest-priority task that is not done or overdue.
- Progress bar shows how much of the day is done.
