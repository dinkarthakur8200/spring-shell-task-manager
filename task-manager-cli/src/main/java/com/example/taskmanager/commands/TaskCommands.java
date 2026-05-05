package com.example.taskmanager.commands;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.time.format.DateTimeFormatter;
import java.util.List;

@ShellComponent
@RequiredArgsConstructor
public class TaskCommands {

    private final TaskService taskService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    // ─────────────────────────────────────────────────────────
    //  ADD
    // ─────────────────────────────────────────────────────────

    @ShellMethod(key = "add", value = "Add a new task")
    public String addTask(
            @ShellOption(help = "Task title") String title,
            @ShellOption(defaultValue = "", help = "Task description") String description,
            @ShellOption(defaultValue = "MEDIUM", help = "Priority: LOW | MEDIUM | HIGH") String priority
    ) {
        try {
            Task.Priority p = Task.Priority.valueOf(priority.toUpperCase());
            Task task = taskService.addTask(title, description, p);
            return success("✅ Task added! ID: " + task.getId() + " — " + task.getTitle());
        } catch (IllegalArgumentException e) {
            return error("Invalid priority. Use LOW, MEDIUM, or HIGH.");
        }
    }

    // ─────────────────────────────────────────────────────────
    //  LIST
    // ─────────────────────────────────────────────────────────

    @ShellMethod(key = "list", value = "List all tasks (optionally filter by status)")
    public String listTasks(
            @ShellOption(defaultValue = "ALL", help = "Filter: ALL | PENDING | IN_PROGRESS | DONE") String status
    ) {
        List<Task> tasks;

        if (status.equalsIgnoreCase("ALL")) {
            tasks = taskService.getAllTasks();
        } else {
            try {
                Task.Status s = Task.Status.valueOf(status.toUpperCase());
                tasks = taskService.getTasksByStatus(s);
            } catch (IllegalArgumentException e) {
                return error("Invalid status. Use ALL, PENDING, IN_PROGRESS, or DONE.");
            }
        }

        if (tasks.isEmpty()) return info("No tasks found.");
        return renderTable(tasks);
    }

    // ─────────────────────────────────────────────────────────
    //  VIEW
    // ─────────────────────────────────────────────────────────

    @ShellMethod(key = "view", value = "View details of a specific task")
    public String viewTask(
            @ShellOption(help = "Task ID") Long id
    ) {
        return taskService.findById(id)
                .map(this::renderDetail)
                .orElse(error("Task not found with ID: " + id));
    }

    // ─────────────────────────────────────────────────────────
    //  START
    // ─────────────────────────────────────────────────────────

    @ShellMethod(key = "start", value = "Mark a task as IN_PROGRESS")
    public String startTask(
            @ShellOption(help = "Task ID") Long id
    ) {
        return taskService.markInProgress(id)
                .map(t -> success("🚀 Task #" + id + " is now IN PROGRESS."))
                .orElse(error("Task not found with ID: " + id));
    }

    // ─────────────────────────────────────────────────────────
    //  COMPLETE
    // ─────────────────────────────────────────────────────────

    @ShellMethod(key = "complete", value = "Mark a task as DONE")
    public String completeTask(
            @ShellOption(help = "Task ID") Long id
    ) {
        return taskService.completeTask(id)
                .map(t -> success("🎉 Task #" + id + " marked as DONE!"))
                .orElse(error("Task not found with ID: " + id));
    }

    // ─────────────────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────────────────

    @ShellMethod(key = "update", value = "Update a task's title, description, or priority")
    public String updateTask(
            @ShellOption(help = "Task ID") Long id,
            @ShellOption(defaultValue = "", help = "New title") String title,
            @ShellOption(defaultValue = "", help = "New description") String description,
            @ShellOption(defaultValue = "", help = "New priority: LOW | MEDIUM | HIGH") String priority
    ) {
        Task.Priority p = null;

        if (!priority.isBlank()) {
            try {
                p = Task.Priority.valueOf(priority.toUpperCase());
            } catch (IllegalArgumentException e) {
                return error("Invalid priority. Use LOW, MEDIUM, or HIGH.");
            }
        }

        final Task.Priority finalPriority = p;

        return taskService.updateTask(id, title, description, finalPriority)
                .map(t -> success("✏️  Task #" + id + " updated successfully."))
                .orElse(error("Task not found with ID: " + id));
    }

    // ─────────────────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────────────────

    @ShellMethod(key = "delete", value = "Delete a task by ID")
    public String deleteTask(
            @ShellOption(help = "Task ID") Long id
    ) {
        if (taskService.deleteTask(id)) {
            return success("🗑️  Task #" + id + " deleted.");
        }
        return error("Task not found with ID: " + id);
    }

    // ─────────────────────────────────────────────────────────
    //  CLEAR DONE
    // ─────────────────────────────────────────────────────────

    @ShellMethod(key = "clear-done", value = "Remove all completed (DONE) tasks")
    public String clearDone() {
        taskService.clearCompleted();
        return success("🧹 All completed tasks have been cleared.");
    }

    // ─────────────────────────────────────────────────────────
    //  STATS
    // ─────────────────────────────────────────────────────────

    @ShellMethod(key = "stats", value = "Show task statistics")
    public String stats() {
        long pending    = taskService.countByStatus(Task.Status.PENDING);
        long inProgress = taskService.countByStatus(Task.Status.IN_PROGRESS);
        long done       = taskService.countByStatus(Task.Status.DONE);
        long total      = pending + inProgress + done;

        return String.format("""
                ╔══════════════════════════════╗
                ║        TASK STATISTICS       ║
                ╠══════════════════════════════╣
                ║  📋 Total       : %-10d║
                ║  ⏳ Pending     : %-10d║
                ║  🚀 In Progress : %-10d║
                ║  ✅ Done        : %-10d║
                ╚══════════════════════════════╝
                """, total, pending, inProgress, done);
    }

    // ─────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────

    private String renderTable(List<Task> tasks) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("%-5s %-30s %-12s %-12s %-18s%n",
                "ID", "TITLE", "PRIORITY", "STATUS", "CREATED AT"));
        sb.append("─".repeat(80)).append("\n");

        for (Task t : tasks) {
            String title = t.getTitle().length() > 28
                    ? t.getTitle().substring(0, 25) + "..."
                    : t.getTitle();

            sb.append(String.format("%-5d %-30s %-12s %-12s %-18s%n",
                    t.getId(),
                    title,
                    priorityIcon(t.getPriority()) + " " + t.getPriority(),
                    statusIcon(t.getStatus())   + " " + t.getStatus(),
                    t.getCreatedAt().format(FMT)
            ));
        }

        return sb.toString();
    }

    private String renderDetail(Task t) {
        return String.format("""
                ╔══════════════════════════════════════╗
                ║           TASK DETAIL                ║
                ╠══════════════════════════════════════╣
                ║  ID          : %-22d║
                ║  Title       : %-22s║
                ║  Description : %-22s║
                ║  Priority    : %-22s║
                ║  Status      : %-22s║
                ║  Created At  : %-22s║
                ║  Completed   : %-22s║
                ╚══════════════════════════════════════╝
                """,
                t.getId(),
                truncate(t.getTitle(), 22),
                truncate(t.getDescription() == null ? "-" : t.getDescription(), 22),
                priorityIcon(t.getPriority()) + " " + t.getPriority(),
                statusIcon(t.getStatus())     + " " + t.getStatus(),
                t.getCreatedAt().format(FMT),
                t.getCompletedAt() != null ? t.getCompletedAt().format(FMT) : "-"
        );
    }

    private String priorityIcon(Task.Priority p) {
        return switch (p) {
            case HIGH   -> "🔴";
            case MEDIUM -> "🟡";
            case LOW    -> "🟢";
        };
    }

    private String statusIcon(Task.Status s) {
        return switch (s) {
            case PENDING     -> "⏳";
            case IN_PROGRESS -> "🚀";
            case DONE        -> "✅";
        };
    }

    private String truncate(String s, int max) {
        if (s == null) return "-";
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
    }

    private String success(String msg) { return "\u001B[32m" + msg + "\u001B[0m"; }
    private String error(String msg)   { return "\u001B[31m❌ " + msg + "\u001B[0m"; }
    private String info(String msg)    { return "\u001B[33mℹ️  " + msg + "\u001B[0m"; }
}
