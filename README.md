# 📋 Task Manager CLI

A command-line Task Manager built with **Spring Boot**, **Spring Shell**, and **H2 Database**.
Supports adding, listing, updating, completing, and deleting tasks — all from your terminal.

---

## 🗂 Project Structure

```
task-manager-cli/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/example/taskmanager/
│   │   │   ├── TaskManagerApplication.java     ← Entry point
│   │   │   ├── model/
│   │   │   │   └── Task.java                   ← JPA Entity
│   │   │   ├── repository/
│   │   │   │   └── TaskRepository.java         ← Spring Data JPA
│   │   │   ├── service/
│   │   │   │   └── TaskService.java            ← Business logic
│   │   │   └── commands/
│   │   │       └── TaskCommands.java           ← CLI commands (Spring Shell)
│   │   └── resources/
│   │       └── application.properties          ← App config & H2 setup
│   └── test/
│       └── java/com/example/taskmanager/
│           └── service/
│               └── TaskServiceTest.java        ← Unit tests
└── data/
    └── taskdb.mv.db                            ← H2 file DB (auto-created)
```

---

## ⚙️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| CLI | Spring Shell 3.2 |
| Persistence | Spring Data JPA + H2 (file-based) |
| Build Tool | Maven |
| Boilerplate | Lombok |
| Testing | JUnit 5 + Mockito |

---

## 🚀 Step-by-Step Setup Guide

### Step 1 — Prerequisites

Make sure the following are installed:

```bash
java -version    # Java 17+
mvn -version     # Maven 3.8+
```

If not installed:
- **Java 17**: https://adoptium.net
- **Maven**: https://maven.apache.org/download.cgi

---

### Step 2 — Clone or Create the Project

**Option A — Clone (if hosted on GitHub):**
```bash
git clone https://github.com/your-username/task-manager-cli.git
cd task-manager-cli
```

**Option B — Create manually:**

Create the directory structure and copy all files as shown in the project tree above.

---

### Step 3 — Understand the Dependencies (`pom.xml`)

Key dependencies used:

```xml
<!-- Spring Shell: Provides the interactive CLI framework -->
<dependency>
    <groupId>org.springframework.shell</groupId>
    <artifactId>spring-shell-starter</artifactId>
    <version>3.2.0</version>
</dependency>

<!-- Spring Data JPA: Simplifies database operations -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- H2: Embedded file-based database (no external DB needed) -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

---

### Step 4 — Configure the Database (`application.properties`)

The app uses H2 in **file mode** so your tasks persist between sessions:

```properties
spring.datasource.url=jdbc:h2:file:./data/taskdb;AUTO_SERVER=TRUE
spring.jpa.hibernate.ddl-auto=update
```

- `file:./data/taskdb` → saves the DB to a `data/` folder in your project root.
- `ddl-auto=update` → auto-creates/updates the `tasks` table on startup.
- No manual database setup required.

---

### Step 5 — Understand the Data Model (`Task.java`)

The `Task` entity has these fields:

| Field | Type | Description |
|---|---|---|
| `id` | Long | Auto-generated primary key |
| `title` | String | Task name (required) |
| `description` | String | Optional details |
| `priority` | Enum | `LOW`, `MEDIUM`, `HIGH` |
| `status` | Enum | `PENDING`, `IN_PROGRESS`, `DONE` |
| `createdAt` | LocalDateTime | Set automatically on save |
| `completedAt` | LocalDateTime | Set when marked DONE |

---

### Step 6 — Build the Project

```bash
cd task-manager-cli
mvn clean install -DskipTests
```

This compiles the code and packages it into a single fat JAR:
```
target/task-manager-cli-1.0.0.jar
```

---

### Step 7 — Run the Application

```bash
mvn spring-boot:run
```

Or run the fat JAR directly:

```bash
java -jar target/task-manager-cli-1.0.0.jar
```

You should see the Spring Shell prompt:

```
shell:>
```

---

### Step 8 — Use the CLI Commands

#### ➕ Add a Task

```bash
shell:> add --title "Build login page" --description "Use Thymeleaf" --priority HIGH
✅ Task added! ID: 1 — Build login page

shell:> add --title "Write unit tests" --priority MEDIUM
✅ Task added! ID: 2 — Write unit tests

shell:> add --title "Fix navbar bug"
✅ Task added! ID: 3 — Fix navbar bug
```

#### 📋 List All Tasks

```bash
shell:> list
ID    TITLE                          PRIORITY     STATUS       CREATED AT
────────────────────────────────────────────────────────────────────────────────
1     Build login page               🔴 HIGH      ⏳ PENDING   01-01-2025 10:00
2     Write unit tests               🟡 MEDIUM    ⏳ PENDING   01-01-2025 10:01
3     Fix navbar bug                 🟡 MEDIUM    ⏳ PENDING   01-01-2025 10:02
```

#### 🔍 Filter by Status

```bash
shell:> list --status PENDING
shell:> list --status IN_PROGRESS
shell:> list --status DONE
```

#### 🔎 View a Single Task

```bash
shell:> view --id 1
╔══════════════════════════════════════╗
║           TASK DETAIL                ║
╠══════════════════════════════════════╣
║  ID          : 1                     ║
║  Title       : Build login page      ║
║  Description : Use Thymeleaf         ║
║  Priority    : 🔴 HIGH               ║
║  Status      : ⏳ PENDING            ║
║  Created At  : 01-01-2025 10:00      ║
║  Completed   : -                     ║
╚══════════════════════════════════════╝
```

#### 🚀 Start a Task

```bash
shell:> start --id 1
🚀 Task #1 is now IN PROGRESS.
```

#### ✅ Complete a Task

```bash
shell:> complete --id 1
🎉 Task #1 marked as DONE!
```

#### ✏️ Update a Task

```bash
# Update title only
shell:> update --id 2 --title "Write integration tests"

# Update priority only
shell:> update --id 3 --priority HIGH

# Update multiple fields
shell:> update --id 2 --title "Write tests" --description "JUnit + Mockito" --priority HIGH
✏️  Task #2 updated successfully.
```

#### 🗑️ Delete a Task

```bash
shell:> delete --id 3
🗑️  Task #3 deleted.
```

#### 🧹 Clear All Completed Tasks

```bash
shell:> clear-done
🧹 All completed tasks have been cleared.
```

#### 📊 View Statistics

```bash
shell:> stats
╔══════════════════════════════╗
║        TASK STATISTICS       ║
╠══════════════════════════════╣
║  📋 Total       : 3          ║
║  ⏳ Pending     : 1          ║
║  🚀 In Progress : 1          ║
║  ✅ Done        : 1          ║
╚══════════════════════════════╝
```

#### ❓ Help

```bash
shell:> help           # List all commands
shell:> help add       # Help for a specific command
```

#### 🚪 Exit

```bash
shell:> exit
```

---

### Step 9 — Run the Tests

```bash
mvn test
```

The unit tests cover:
- Adding a task
- Completing a task (status → DONE, completedAt set)
- Deleting a task (found and not found)
- Marking a task IN_PROGRESS

---

### Step 10 — (Optional) View the Database via Browser

H2 provides a web console. With the app running, open:

```
http://localhost:8080/h2-console
```

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/taskdb` |
| Username | `sa` |
| Password | *(leave blank)* |

Then run SQL directly:
```sql
SELECT * FROM TASKS;
SELECT * FROM TASKS WHERE STATUS = 'PENDING';
```

---

## 📌 Command Reference

| Command | Description | Key Options |
|---|---|---|
| `add` | Add a new task | `--title`, `--description`, `--priority` |
| `list` | List tasks | `--status ALL/PENDING/IN_PROGRESS/DONE` |
| `view` | View task details | `--id` |
| `start` | Mark task as IN_PROGRESS | `--id` |
| `complete` | Mark task as DONE | `--id` |
| `update` | Update task fields | `--id`, `--title`, `--description`, `--priority` |
| `delete` | Delete a task | `--id` |
| `clear-done` | Remove all DONE tasks | — |
| `stats` | Show task statistics | — |
| `help` | Show available commands | — |
| `exit` | Quit the application | — |

---

## 🔧 Common Issues

| Problem | Cause | Fix |
|---|---|---|
| Port 8080 already in use | Another app is running | Add `server.port=8081` to `application.properties` |
| `data/taskdb` not created | No write permission | Run from a directory you own |
| H2 console not loading | Web server disabled | Ensure `spring.h2.console.enabled=true` |
| Command not found in shell | Typo or wrong syntax | Run `help` to see all commands |

---

## 🌱 Possible Enhancements

- **Due dates** — Add a `dueDate` field with overdue detection
- **Tags/Labels** — Categorize tasks with custom labels
- **Export** — Export tasks to CSV or JSON
- **Notifications** — Alert on overdue tasks via email
- **REST API** — Add a web layer alongside the CLI
- **PostgreSQL** — Swap H2 for a production-grade database

---

*Built with ❤️ by Dinkar Thakur using Spring Boot + Spring Shell*
