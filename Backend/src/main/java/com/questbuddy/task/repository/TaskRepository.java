package com.questbuddy.task.repository;

import com.questbuddy.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Spring Data JPA repository for {@link Task} providing CRUD access to the tasks table.
 * Includes the derived query method {@code findByUser_Id(Long)} to load all tasks owned by a given user (via property traversal of {@code user.id}).
 * Typically injected into services/controllers; perform write operations within a transactional boundary.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUser_Id(Long userId);
}