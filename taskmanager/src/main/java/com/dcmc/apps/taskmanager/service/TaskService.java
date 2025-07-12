package com.dcmc.apps.taskmanager.service;

import com.dcmc.apps.taskmanager.domain.Task;
import com.dcmc.apps.taskmanager.domain.TaskStatus;
import com.dcmc.apps.taskmanager.domain.enumeration.WorkGroupRole;
import com.dcmc.apps.taskmanager.repository.TaskRepository;
import com.dcmc.apps.taskmanager.repository.WorkGroupMembershipRepository;
import com.dcmc.apps.taskmanager.security.SecurityUtils;
import com.dcmc.apps.taskmanager.service.dto.TaskDTO;
import com.dcmc.apps.taskmanager.service.dto.TaskStatusDTO;
import com.dcmc.apps.taskmanager.service.mapper.TaskMapper;
import com.dcmc.apps.taskmanager.repository.TaskStatusRepository;

import java.time.Instant;
import java.util.Optional;
import jakarta.persistence.EntityNotFoundException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TaskService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final WorkGroupMembershipRepository workGroupMembershipRepository;
    private final TaskStatusRepository taskStatusRepository;

    public TaskService(
        TaskRepository taskRepository,
        TaskMapper taskMapper,
        WorkGroupMembershipRepository workGroupMembershipRepository
        , TaskStatusRepository taskStatusRepository
    ) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.workGroupMembershipRepository = workGroupMembershipRepository;
        this.taskStatusRepository = taskStatusRepository;
    }

/**
     * Save a task, assigning defaults if necessary.
     */
    public TaskDTO save(TaskDTO taskDTO) {
        LOG.debug("Request to save Task : {}", taskDTO);
        Instant now = Instant.now();

        // Default status if missing
        if (taskDTO.getStatus() == null || taskDTO.getStatus().getId() == null) {
            TaskStatus defaultStatus = taskStatusRepository
                .findByName("NOT_STARTED")
                .orElseThrow(() -> new IllegalStateException("Default status NOT_STARTED not found"));
            taskDTO.setStatus(new TaskStatusDTO(defaultStatus.getId(), defaultStatus.getName(), defaultStatus.isVisible()));
        }

        // Default timestamps if missing
        if (taskDTO.getCreateTime() == null) {
            taskDTO.setCreateTime(now);
        }
        taskDTO.setUpdateTime(now);

        Task task = taskMapper.toEntity(taskDTO);
        task = taskRepository.save(task);
        return taskMapper.toDto(task);
    }

    /**
     * Update a task (full replacement), preserving createTime and assigning new updateTime.
     */
    public TaskDTO update(TaskDTO taskDTO) {
        LOG.debug("Request to update Task : {}", taskDTO);

        Task existing = taskRepository.findById(taskDTO.getId())
            .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        if (Boolean.TRUE.equals(existing.isArchived())) {
            throw new IllegalStateException("Archived tasks cannot be edited");
        }

        Instant now = Instant.now();
        // Preserve original createTime
        taskDTO.setCreateTime(existing.getCreateTime());
        // Always refresh updateTime
        taskDTO.setUpdateTime(now);

        Task task = taskMapper.toEntity(taskDTO);
        task = taskRepository.save(task);
        return taskMapper.toDto(task);
    }

    /**
     * Partially update a task, setting updateTime and forbidding edits on archived.
     */
    public Optional<TaskDTO> partialUpdate(TaskDTO taskDTO) {
        LOG.debug("Request to partially update Task : {}", taskDTO);

        return taskRepository.findById(taskDTO.getId()).map(existingTask -> {
            if (Boolean.TRUE.equals(existingTask.isArchived())) {
                throw new IllegalStateException("Archived tasks cannot be edited");
            }

            // Apply only non-null fields
            taskMapper.partialUpdate(existingTask, taskDTO);
            // Update the timestamp
            existingTask.setUpdateTime(Instant.now());

            return taskRepository.save(existingTask);
        }).map(taskMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TaskDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Tasks");
        return taskRepository.findAll(pageable).map(taskMapper::toDto);
    }

    public Page<TaskDTO> findAllWithEagerRelationships(Pageable pageable) {
        return taskRepository.findAllWithEagerRelationships(pageable).map(taskMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<TaskDTO> findOne(Long id) {
        LOG.debug("Request to get Task : {}", id);
        return taskRepository.findOneWithEagerRelationships(id).map(taskMapper::toDto);
    }

    public void delete(Long id) {
        LOG.debug("Request to delete Task : {}", id);
        taskRepository.deleteById(id);
    }

    public TaskDTO archiveTask(Long taskId) {
        LOG.debug("Request to archive Task : {}", taskId);

        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        if (!task.getStatus().getName().equals("DONE")) {
            throw new IllegalStateException("Only DONE tasks can be archived");
        }

        String currentUserId = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new RuntimeException("User not authenticated"));

        WorkGroupRole role = workGroupMembershipRepository
            .findByUserIdAndWorkGroupId(currentUserId, task.getWorkGroup().getId())
            .map(m -> m.getRole())
            .orElseThrow(() -> new RuntimeException("User not part of the work group"));

        if (!(role == WorkGroupRole.OWNER || role == WorkGroupRole.MODERADOR)) {
            throw new AccessDeniedException("Only OWNER or MODERADOR can archive tasks");
        }

        task.setArchived(true);
        task = taskRepository.save(task);
        return taskMapper.toDto(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskDTO> findArchivedTasks(Pageable pageable) {
        LOG.debug("Request to get archived Tasks");
        return taskRepository.findByArchivedTrue(pageable).map(taskMapper::toDto);
    }

    public void deleteArchivedTask(Long id) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        if (!Boolean.TRUE.equals(task.isArchived())) {
            throw new IllegalStateException("Task is not archived");
        }

        String currentUserId = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new RuntimeException("User not authenticated"));

        WorkGroupRole role = workGroupMembershipRepository
            .findByUserIdAndWorkGroupId(currentUserId, task.getWorkGroup().getId())
            .map(m -> m.getRole())
            .orElseThrow(() -> new RuntimeException("User not part of the work group"));

        if (!(role == WorkGroupRole.OWNER || role == WorkGroupRole.MODERADOR)) {
            throw new AccessDeniedException("Only OWNER or MODERADOR can delete archived tasks");
        }

        taskRepository.deleteById(id);
    }
}
