package com.dcmc.apps.taskmanager.repository;

import com.dcmc.apps.taskmanager.domain.TaskStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the TaskStatus entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TaskStatusRepository extends JpaRepository<TaskStatus, Long> {}
