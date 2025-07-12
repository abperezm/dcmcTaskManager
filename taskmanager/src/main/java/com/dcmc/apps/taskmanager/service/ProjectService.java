package com.dcmc.apps.taskmanager.service;

import com.dcmc.apps.taskmanager.domain.Project;
import com.dcmc.apps.taskmanager.domain.Task;
import com.dcmc.apps.taskmanager.domain.enumeration.WorkGroupRole;
import com.dcmc.apps.taskmanager.repository.ProjectRepository;
import com.dcmc.apps.taskmanager.security.SecurityUtils;
import com.dcmc.apps.taskmanager.service.dto.ProjectDTO;
import com.dcmc.apps.taskmanager.service.dto.TaskDTO;
import com.dcmc.apps.taskmanager.service.dto.TaskSummaryDTO;
import com.dcmc.apps.taskmanager.service.dto.UserDTO;
import com.dcmc.apps.taskmanager.service.mapper.ProjectMapper;
import com.dcmc.apps.taskmanager.domain.User;
import com.dcmc.apps.taskmanager.repository.WorkGroupMembershipRepository;
import com.dcmc.apps.taskmanager.repository.TaskRepository;
import com.dcmc.apps.taskmanager.service.mapper.TaskMapper;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.dcmc.apps.taskmanager.domain.Project}.
 */
@Service
@Transactional
public class ProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;

    private final ProjectMapper projectMapper;

    private final WorkGroupMembershipRepository workGroupMembershipRepository;

    private final TaskRepository taskRepository;

    private final TaskMapper taskMapper;

    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper, 
                          WorkGroupMembershipRepository workGroupMembershipRepository, TaskRepository taskRepository, TaskMapper taskMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.workGroupMembershipRepository = workGroupMembershipRepository;
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }

    /**
     * Save a project.
     *
     * @param projectDTO the entity to save.
     * @return the persisted entity.
     */
    public ProjectDTO save(ProjectDTO projectDTO) {
        LOG.debug("Request to save Project : {}", projectDTO);
        Project project = projectMapper.toEntity(projectDTO);
        project = projectRepository.save(project);
        return projectMapper.toDto(project);
    }

    /**
     * Update a project.
     *
     * @param projectDTO the entity to save.
     * @return the persisted entity.
     */
    public ProjectDTO update(ProjectDTO projectDTO) {
        LOG.debug("Request to update Project : {}", projectDTO);
        Project project = projectMapper.toEntity(projectDTO);
        project = projectRepository.save(project);
        return projectMapper.toDto(project);
    }

    /**
     * Partially update a project.
     *
     * @param projectDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ProjectDTO> partialUpdate(ProjectDTO projectDTO) {
        LOG.debug("Request to partially update Project : {}", projectDTO);

        return projectRepository
            .findById(projectDTO.getId())
            .map(existingProject -> {
                projectMapper.partialUpdate(existingProject, projectDTO);

                return existingProject;
            })
            .map(projectRepository::save)
            .map(projectMapper::toDto);
    }

    /**
     * Get all the projects.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<ProjectDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all Projects");
        return projectRepository.findAll(pageable).map(projectMapper::toDto);
    }

    /**
     * Get all the projects with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<ProjectDTO> findAllWithEagerRelationships(Pageable pageable) {
        return projectRepository.findAllWithEagerRelationships(pageable).map(projectMapper::toDto);
    }

    /**
     * Get one project by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ProjectDTO> findOne(Long id) {
        LOG.debug("Request to get Project : {}", id);
        return projectRepository.findOneWithEagerRelationships(id).map(projectMapper::toDto);
    }

    /**
     * Delete the project by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Project : {}", id);
        projectRepository.deleteById(id);
    }

    public ProjectDTO updateProjectMembers(Long projectId, Set<UserDTO> memberDTOs) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        Long groupId = project.getWorkGroup().getId();
        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new RuntimeException("User not authenticated"));

        WorkGroupRole role = workGroupMembershipRepository
            .findByUserIdAndWorkGroupId(currentUserLogin, groupId)
            .map(m -> m.getRole())
            .orElseThrow(() -> new RuntimeException("User is not part of the work group"));

        if (role != WorkGroupRole.OWNER && role != WorkGroupRole.MODERADOR) {
            throw new AccessDeniedException("Only OWNER or MODERADOR can update project members");
        }

        // Validar que todos los usuarios están en el mismo grupo
        Set<User> validMembers = memberDTOs.stream()
            .map(dto -> {
                String userId = dto.getId() != null ? dto.getId().toString() : dto.getLogin();
                boolean isInGroup = workGroupMembershipRepository
                    .findByUserIdAndWorkGroupId(userId, groupId)
                    .isPresent();
                if (!isInGroup) {
                    throw new IllegalArgumentException("User " + userId + " is not part of the work group");
                }
                User user = new User();
                if (dto.getId() != null) user.setId(dto.getId());
                else user.setLogin(dto.getLogin());
                return user;
            })
            .collect(Collectors.toSet());

        project.setMembers(validMembers);
        project = projectRepository.save(project);

        return projectMapper.toDto(project);
    }

    public ProjectDTO assignTasksToProject(Long projectId, List<Long> taskIds) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        Long groupId = project.getWorkGroup().getId();
        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new RuntimeException("User not authenticated"));

        WorkGroupRole role = workGroupMembershipRepository
            .findByUserIdAndWorkGroupId(currentUserLogin, groupId)
            .map(m -> m.getRole())
            .orElseThrow(() -> new RuntimeException("User is not part of the work group"));

        if (role != WorkGroupRole.OWNER && role != WorkGroupRole.MODERADOR) {
            throw new AccessDeniedException("Only OWNER or MODERADOR can assign tasks to project");
        }

        Set<Task> validTasks = taskIds.stream()
            .map(taskId -> {
                Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> new EntityNotFoundException("Task with ID " + taskId + " not found"));
                if (!task.getWorkGroup().getId().equals(groupId)) {
                    throw new IllegalArgumentException("Task " + taskId + " does not belong to the same group");
                }
                if (Boolean.TRUE.equals(task.isArchived())) {
                    throw new IllegalArgumentException("Task " + taskId + " is archived and cannot be assigned");
                }
                return task;
            })
            .collect(Collectors.toSet());

        validTasks.forEach(task -> task.setProject(project));
        taskRepository.saveAll(validTasks); // actualizar la relación inversa
        return projectMapper.toDto(project);
    }
    
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksForProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        Long groupId = project.getWorkGroup().getId();
        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new RuntimeException("User not authenticated"));

        boolean isMember = workGroupMembershipRepository
            .findByUserIdAndWorkGroupId(currentUserLogin, groupId)
            .isPresent();

        if (!isMember) {
            throw new AccessDeniedException("User is not part of the work group");
        }

        return project.getTasks().stream()
            .map(taskMapper::toDto)
            .collect(Collectors.toList());
    }

    public void removeTaskFromProject(Long projectId, Long taskId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        // Validar que pertenecen al mismo WorkGroup
        if (!Objects.equals(task.getWorkGroup().getId(), project.getWorkGroup().getId())) {
            throw new IllegalStateException("Task and Project do not belong to the same WorkGroup");
        }

        // Verificar que la tarea pertenece al proyecto
        if (task.getProject() == null || !Objects.equals(task.getProject().getId(), projectId)) {
            throw new IllegalStateException("Task is not part of this project");
        }

        // Validar permisos
        String userId = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new RuntimeException("User not authenticated"));

        WorkGroupRole role = workGroupMembershipRepository
            .findByUserIdAndWorkGroupId(userId, project.getWorkGroup().getId())
            .map(m -> m.getRole())
            .orElseThrow(() -> new AccessDeniedException("User is not part of the WorkGroup"));

        if (!(role == WorkGroupRole.OWNER || role == WorkGroupRole.MODERADOR)) {
            throw new AccessDeniedException("Only OWNER or MODERADOR can remove tasks from a project");
        }

        task.setProject(null);
        taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public List<TaskSummaryDTO> getTaskSummariesForProject(Long projectId) {
    // 1) Recuperar el proyecto o lanzar excepción si no existe
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() -> new EntityNotFoundException("Project not found"));

    // 2) Verificar que el usuario forma parte del work group
    Long groupId = project.getWorkGroup().getId();
    String currentUserLogin = SecurityUtils.getCurrentUserLogin()
        .orElseThrow(() -> new RuntimeException("User not authenticated"));
    boolean isMember = workGroupMembershipRepository
        .findByUserIdAndWorkGroupId(currentUserLogin, groupId)
        .isPresent();
    if (!isMember) {
        throw new AccessDeniedException("User is not part of the work group");
    }

    // 3) Mapear las tareas del proyecto a TaskSummaryDTO
    return project.getTasks().stream()
        .map(t -> new TaskSummaryDTO(
            t.getId(),
            t.getTitle(),
            t.getPriority() != null ? t.getPriority().getName() : null,
            t.getStatus()   != null ? t.getStatus().getName()   : null,
            t.isArchived()
        ))
        .collect(Collectors.toList());
}

}
