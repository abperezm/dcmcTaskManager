package com.dcmc.apps.taskmanager.web.rest;

import com.dcmc.apps.taskmanager.repository.TaskPriorityRepository;
import com.dcmc.apps.taskmanager.service.TaskPriorityService;
import com.dcmc.apps.taskmanager.service.dto.TaskPriorityDTO;
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
 * REST controller for managing {@link com.dcmc.apps.taskmanager.domain.TaskPriority}.
 */
@RestController
@RequestMapping("/api/task-priorities")
public class TaskPriorityResource {

    private static final Logger LOG = LoggerFactory.getLogger(TaskPriorityResource.class);

    private static final String ENTITY_NAME = "taskmanagerTaskPriority";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TaskPriorityService taskPriorityService;

    private final TaskPriorityRepository taskPriorityRepository;

    public TaskPriorityResource(TaskPriorityService taskPriorityService, TaskPriorityRepository taskPriorityRepository) {
        this.taskPriorityService = taskPriorityService;
        this.taskPriorityRepository = taskPriorityRepository;
    }

    /**
     * {@code POST  /task-priorities} : Create a new taskPriority.
     *
     * @param taskPriorityDTO the taskPriorityDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new taskPriorityDTO, or with status {@code 400 (Bad Request)} if the taskPriority has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<TaskPriorityDTO> createTaskPriority(@Valid @RequestBody TaskPriorityDTO taskPriorityDTO)
        throws URISyntaxException {
        LOG.debug("REST request to save TaskPriority : {}", taskPriorityDTO);
        if (taskPriorityDTO.getId() != null) {
            throw new BadRequestAlertException("A new taskPriority cannot already have an ID", ENTITY_NAME, "idexists");
        }
        taskPriorityDTO = taskPriorityService.save(taskPriorityDTO);
        return ResponseEntity.created(new URI("/api/task-priorities/" + taskPriorityDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, taskPriorityDTO.getId().toString()))
            .body(taskPriorityDTO);
    }

    /**
     * {@code PUT  /task-priorities/:id} : Updates an existing taskPriority.
     *
     * @param id the id of the taskPriorityDTO to save.
     * @param taskPriorityDTO the taskPriorityDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated taskPriorityDTO,
     * or with status {@code 400 (Bad Request)} if the taskPriorityDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the taskPriorityDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskPriorityDTO> updateTaskPriority(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody TaskPriorityDTO taskPriorityDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update TaskPriority : {}, {}", id, taskPriorityDTO);
        if (taskPriorityDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, taskPriorityDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!taskPriorityRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        taskPriorityDTO = taskPriorityService.update(taskPriorityDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, taskPriorityDTO.getId().toString()))
            .body(taskPriorityDTO);
    }

    /**
     * {@code PATCH  /task-priorities/:id} : Partial updates given fields of an existing taskPriority, field will ignore if it is null
     *
     * @param id the id of the taskPriorityDTO to save.
     * @param taskPriorityDTO the taskPriorityDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated taskPriorityDTO,
     * or with status {@code 400 (Bad Request)} if the taskPriorityDTO is not valid,
     * or with status {@code 404 (Not Found)} if the taskPriorityDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the taskPriorityDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<TaskPriorityDTO> partialUpdateTaskPriority(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody TaskPriorityDTO taskPriorityDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update TaskPriority partially : {}, {}", id, taskPriorityDTO);
        if (taskPriorityDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, taskPriorityDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!taskPriorityRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<TaskPriorityDTO> result = taskPriorityService.partialUpdate(taskPriorityDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, taskPriorityDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /task-priorities} : get all the taskPriorities.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of taskPriorities in body.
     */
    @GetMapping("")
    public ResponseEntity<List<TaskPriorityDTO>> getAllTaskPriorities(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of TaskPriorities");
        Page<TaskPriorityDTO> page = taskPriorityService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /task-priorities/:id} : get the "id" taskPriority.
     *
     * @param id the id of the taskPriorityDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the taskPriorityDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskPriorityDTO> getTaskPriority(@PathVariable("id") Long id) {
        LOG.debug("REST request to get TaskPriority : {}", id);
        Optional<TaskPriorityDTO> taskPriorityDTO = taskPriorityService.findOne(id);
        return ResponseUtil.wrapOrNotFound(taskPriorityDTO);
    }

    /**
     * {@code DELETE  /task-priorities/:id} : delete the "id" taskPriority.
     *
     * @param id the id of the taskPriorityDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTaskPriority(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete TaskPriority : {}", id);
        taskPriorityService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    /**
     * {@code GET /task-priorities/visible} : get all visible task priorities.
     */
    @GetMapping("/visible")
    public ResponseEntity<List<TaskPriorityDTO>> getAllVisible(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get all VISIBLE TaskPriorities");
        Page<TaskPriorityDTO> page = taskPriorityService.findAllVisible(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code POST /task-priorities/{id}/hide} : hide a task priority by id.
     */
    @PostMapping("/{id}/hide")
    public ResponseEntity<TaskPriorityDTO> hideTaskPriority(@PathVariable Long id) {
        LOG.debug("REST request to hide TaskPriority : {}", id);
        Optional<TaskPriorityDTO> result = taskPriorityService.hide(id);
        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, id.toString())
        );
    }

    /**
     * {@code POST /task-priorities/{id}/unhide} : unhide a task priority by id.
     */
    @PostMapping("/{id}/unhide")
    public ResponseEntity<TaskPriorityDTO> unhideTaskPriority(@PathVariable Long id) {
        LOG.debug("REST request to unhide TaskPriority : {}", id);
        Optional<TaskPriorityDTO> result = taskPriorityService.unhide(id);
        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, id.toString())
        );
    }

}
