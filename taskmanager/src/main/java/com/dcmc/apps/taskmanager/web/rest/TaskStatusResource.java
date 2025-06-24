package com.dcmc.apps.taskmanager.web.rest;

import com.dcmc.apps.taskmanager.repository.TaskStatusRepository;
import com.dcmc.apps.taskmanager.service.TaskStatusService;
import com.dcmc.apps.taskmanager.service.WorkGroupPermissionService;
import com.dcmc.apps.taskmanager.service.dto.TaskStatusDTO;
import com.dcmc.apps.taskmanager.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.dcmc.apps.taskmanager.domain.TaskStatus}.
 */
@RestController
@RequestMapping("/api/task-statuses")
public class TaskStatusResource {

    private static final Logger LOG = LoggerFactory.getLogger(TaskStatusResource.class);

    private static final String ENTITY_NAME = "taskmanagerTaskStatus";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TaskStatusService taskStatusService;
    private final TaskStatusRepository taskStatusRepository;
    private final WorkGroupPermissionService workGroupPermissionService;

    public TaskStatusResource(
        TaskStatusService taskStatusService,
        TaskStatusRepository taskStatusRepository,
        WorkGroupPermissionService workGroupPermissionService
    ) {
        this.taskStatusService = taskStatusService;
        this.taskStatusRepository = taskStatusRepository;
        this.workGroupPermissionService = workGroupPermissionService;
    }

    @PostMapping("")
    public ResponseEntity<TaskStatusDTO> createTaskStatus(
        @Valid @RequestBody TaskStatusDTO taskStatusDTO,
        @RequestParam Long workGroupId
    ) throws URISyntaxException {
        LOG.debug("REST request to save TaskStatus : {}", taskStatusDTO);
        workGroupPermissionService.checkCanManageMembers(workGroupId);

        if (taskStatusDTO.getId() != null) {
            throw new BadRequestAlertException("A new taskStatus cannot already have an ID", ENTITY_NAME, "idexists");
        }
        taskStatusDTO = taskStatusService.save(taskStatusDTO);
        return ResponseEntity.created(new URI("/api/task-statuses/" + taskStatusDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, taskStatusDTO.getId().toString()))
            .body(taskStatusDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskStatusDTO> updateTaskStatus(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TaskStatusDTO taskStatusDTO,
        @RequestParam Long workGroupId
    ) throws URISyntaxException {
        LOG.debug("REST request to update TaskStatus : {}, {}", id, taskStatusDTO);
        workGroupPermissionService.checkCanManageMembers(workGroupId);

        if (taskStatusDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, taskStatusDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!taskStatusRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        taskStatusDTO = taskStatusService.update(taskStatusDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, taskStatusDTO.getId().toString()))
            .body(taskStatusDTO);
    }

    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<TaskStatusDTO> partialUpdateTaskStatus(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody TaskStatusDTO taskStatusDTO,
        @RequestParam Long workGroupId
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update TaskStatus partially : {}, {}", id, taskStatusDTO);
        workGroupPermissionService.checkCanManageMembers(workGroupId);

        if (taskStatusDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, taskStatusDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }
        if (!taskStatusRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TaskStatusDTO> result = taskStatusService.partialUpdate(taskStatusDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, taskStatusDTO.getId().toString())
        );
    }

    @GetMapping("")
    public ResponseEntity<List<TaskStatusDTO>> getAllTaskStatuses(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of TaskStatuses");
        Page<TaskStatusDTO> page = taskStatusService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskStatusDTO> getTaskStatus(@PathVariable("id") Long id) {
        LOG.debug("REST request to get TaskStatus : {}", id);
        Optional<TaskStatusDTO> taskStatusDTO = taskStatusService.findOne(id);
        return ResponseUtil.wrapOrNotFound(taskStatusDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaskStatus(@PathVariable("id") Long id, @RequestParam Long workGroupId) {
        LOG.debug("REST request to delete TaskStatus : {}", id);
        workGroupPermissionService.checkCanManageMembers(workGroupId);
        taskStatusService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
