package com.dcmc.apps.taskmanager.repository;

import com.dcmc.apps.taskmanager.domain.TaskPriority;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the TaskPriority entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TaskPriorityRepository extends JpaRepository<TaskPriority, Long> {
    Page<TaskPriority> findByVisibleTrue(Pageable pageable);
}
