package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = Task.builder()
                .id(1L)
                .title("Write unit tests")
                .description("Cover all service methods")
                .priority(Task.Priority.HIGH)
                .status(Task.Status.PENDING)
                .build();
    }

    @Test
    void addTask_shouldSaveAndReturnTask() {
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        Task result = taskService.addTask("Write unit tests", "Cover all service methods", Task.Priority.HIGH);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Write unit tests");
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void completeTask_shouldSetStatusDone() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Task> result = taskService.completeTask(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(Task.Status.DONE);
        assertThat(result.get().getCompletedAt()).isNotNull();
    }

    @Test
    void deleteTask_shouldReturnTrueIfExists() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        doNothing().when(taskRepository).deleteById(1L);

        boolean deleted = taskService.deleteTask(1L);

        assertThat(deleted).isTrue();
        verify(taskRepository).deleteById(1L);
    }

    @Test
    void deleteTask_shouldReturnFalseIfNotFound() {
        when(taskRepository.existsById(99L)).thenReturn(false);

        boolean deleted = taskService.deleteTask(99L);

        assertThat(deleted).isFalse();
        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void markInProgress_shouldUpdateStatus() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Task> result = taskService.markInProgress(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getStatus()).isEqualTo(Task.Status.IN_PROGRESS);
    }
}
