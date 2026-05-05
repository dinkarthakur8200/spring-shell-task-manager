package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    // ─── CREATE ──────────────────────────────────────────────
    public Task addTask(String title, String description, Task.Priority priority) {
        Task task = Task.builder()
                .title(title)
                .description(description)
                .priority(priority)
                .status(Task.Status.PENDING)
                .build();
        return taskRepository.save(task);
    }

    // ─── READ ─────────────────────────────────────────────────
    public List<Task> getAllTasks() {
        return taskRepository.findAllByOrderByPriorityDescCreatedAtAsc();
    }

    public List<Task> getTasksByStatus(Task.Status status) {
        return taskRepository.findByStatusOrderByPriorityDesc(status);
    }

    public List<Task> getTasksByPriority(Task.Priority priority) {
        return taskRepository.findByPriority(priority);
    }

    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    // ─── UPDATE ───────────────────────────────────────────────
    public Optional<Task> markInProgress(Long id) {
        return taskRepository.findById(id).map(task -> {
            task.setStatus(Task.Status.IN_PROGRESS);
            return taskRepository.save(task);
        });
    }

    public Optional<Task> completeTask(Long id) {
        return taskRepository.findById(id).map(task -> {
            task.setStatus(Task.Status.DONE);
            task.setCompletedAt(LocalDateTime.now());
            return taskRepository.save(task);
        });
    }

    public Optional<Task> updateTask(Long id, String title, String description, Task.Priority priority) {
        return taskRepository.findById(id).map(task -> {
            if (title != null && !title.isBlank()) task.setTitle(title);
            if (description != null && !description.isBlank()) task.setDescription(description);
            if (priority != null) task.setPriority(priority);
            return taskRepository.save(task);
        });
    }

    // ─── DELETE ───────────────────────────────────────────────
    public boolean deleteTask(Long id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public void clearCompleted() {
        List<Task> done = taskRepository.findByStatus(Task.Status.DONE);
        taskRepository.deleteAll(done);
    }

    // ─── STATS ────────────────────────────────────────────────
    public long countByStatus(Task.Status status) {
        return taskRepository.findByStatus(status).size();
    }
}
