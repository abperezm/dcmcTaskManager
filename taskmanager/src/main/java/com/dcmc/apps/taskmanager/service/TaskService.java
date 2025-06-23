package com.dcmc.apps.taskmanager.service;

import com.dcmc.apps.taskmanager.domain.Task;
import com.dcmc.apps.taskmanager.domain.enumeration.WorkGroupRole;
import com.dcmc.apps.taskmanager.repository.TaskRepository;
import com.dcmc.apps.taskmanager.repository.WorkGroupMembershipRepository;
import com.dcmc.apps.taskmanager.security.SecurityUtils;
import com.dcmc.apps.taskmanager.service.dto.TaskDTO;
import com.dcmc.apps.taskmanager.service.mapper.TaskMapper;
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

    public TaskService(
        TaskRepository taskRepository,
        TaskMapper taskMapper,
        WorkGroupMembershipRepository workGroupMembershipRepository
    ) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.workGroupMembershipRepository = workGroupMembershipRepository;
    }

    public TaskDTO save(TaskDTO taskDTO) {
        LOG.debug("Request to save Task : {}", taskDTO);
        Task task = taskMapper.toEntity(taskDTO);
        task = taskRepository.save(task);
        return taskMapper.toDto(task);
    }

    public TaskDTO update(TaskDTO taskDTO) {
        LOG.debug("Request to update Task : {}", taskDTO);

        Task existing = taskRepository.findById(taskDTO.getId())
            .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        if (Boolean.TRUE.equals(existing.isArchived())) {
            throw new IllegalStateException("Archived tasks cannot be edited");
        }

        Task task = taskMapper.toEntity(taskDTO);
        task = taskRepository.save(task);
        return taskMapper.toDto(task);
    }

    public Optional<TaskDTO> partialUpdate(TaskDTO taskDTO) {
        LOG.debug("Request to partially update Task : {}", taskDTO);

        return taskRepository.findById(taskDTO.getId()).map(existingTask -> {
            if (Boolean.TRUE.equals(existingTask.isArchived())) {
                throw new IllegalStateException("Archived tasks cannot be edited");
            }

            taskMapper.partialUpdate(existingTask, taskDTO);
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

        if (!task.getStatus().name().equals("DONE")) {
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
