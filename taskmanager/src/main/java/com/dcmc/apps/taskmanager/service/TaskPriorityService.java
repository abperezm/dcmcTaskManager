package com.dcmc.apps.taskmanager.service;

import com.dcmc.apps.taskmanager.domain.TaskPriority;
import com.dcmc.apps.taskmanager.repository.TaskPriorityRepository;
import com.dcmc.apps.taskmanager.service.dto.TaskPriorityDTO;
import com.dcmc.apps.taskmanager.service.mapper.TaskPriorityMapper;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.dcmc.apps.taskmanager.domain.TaskPriority}.
 */
@Service
@Transactional
public class TaskPriorityService {

    private static final Logger LOG = LoggerFactory.getLogger(TaskPriorityService.class);

    private final TaskPriorityRepository taskPriorityRepository;
    private final TaskPriorityMapper taskPriorityMapper;

    public TaskPriorityService(TaskPriorityRepository taskPriorityRepository, TaskPriorityMapper taskPriorityMapper) {
        this.taskPriorityRepository = taskPriorityRepository;
        this.taskPriorityMapper = taskPriorityMapper;
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public TaskPriorityDTO save(TaskPriorityDTO taskPriorityDTO) {
        LOG.debug("Request to save TaskPriority : {}", taskPriorityDTO);
        TaskPriority taskPriority = taskPriorityMapper.toEntity(taskPriorityDTO);
        taskPriority = taskPriorityRepository.save(taskPriority);
        return taskPriorityMapper.toDto(taskPriority);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public TaskPriorityDTO update(TaskPriorityDTO taskPriorityDTO) {
        LOG.debug("Request to update TaskPriority : {}", taskPriorityDTO);
        TaskPriority taskPriority = taskPriorityMapper.toEntity(taskPriorityDTO);
        taskPriority = taskPriorityRepository.save(taskPriority);
        return taskPriorityMapper.toDto(taskPriority);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Optional<TaskPriorityDTO> partialUpdate(TaskPriorityDTO taskPriorityDTO) {
        LOG.debug("Request to partially update TaskPriority : {}", taskPriorityDTO);

        return taskPriorityRepository
            .findById(taskPriorityDTO.getId())
            .map(existingTaskPriority -> {
                taskPriorityMapper.partialUpdate(existingTaskPriority, taskPriorityDTO);
                return existingTaskPriority;
            })
            .map(taskPriorityRepository::save)
            .map(taskPriorityMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TaskPriorityDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all TaskPriorities");
        return taskPriorityRepository.findAll(pageable).map(taskPriorityMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TaskPriorityDTO> findAllVisible(Pageable pageable) {
        LOG.debug("Request to get all VISIBLE TaskPriorities");
        return taskPriorityRepository.findByVisibleTrue(pageable).map(taskPriorityMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<TaskPriorityDTO> findOne(Long id) {
        LOG.debug("Request to get TaskPriority : {}", id);
        return taskPriorityRepository.findById(id).map(taskPriorityMapper::toDto);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void delete(Long id) {
        LOG.debug("Request to delete TaskPriority : {}", id);
        taskPriorityRepository.deleteById(id);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Optional<TaskPriorityDTO> hide(Long id) {
        LOG.debug("Request to hide TaskPriority : {}", id);
        return taskPriorityRepository
            .findById(id)
            .map(priority -> {
                priority.setVisible(false);
                return taskPriorityRepository.save(priority);
            })
            .map(taskPriorityMapper::toDto);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public Optional<TaskPriorityDTO> unhide(Long id) {
        LOG.debug("Request to unhide TaskPriority : {}", id);
        return taskPriorityRepository
            .findById(id)
            .map(priority -> {
                priority.setVisible(true);
                return taskPriorityRepository.save(priority);
            })
            .map(taskPriorityMapper::toDto);
    }
}
