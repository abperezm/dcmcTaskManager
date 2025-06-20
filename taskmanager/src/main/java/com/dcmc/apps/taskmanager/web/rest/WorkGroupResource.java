package com.dcmc.apps.taskmanager.web.rest;

import com.dcmc.apps.taskmanager.repository.WorkGroupRepository;
import com.dcmc.apps.taskmanager.security.SecurityUtils;
import com.dcmc.apps.taskmanager.service.WorkGroupService;
import com.dcmc.apps.taskmanager.service.WorkGroupPermissionService;
import com.dcmc.apps.taskmanager.service.dto.WorkGroupDTO;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.dcmc.apps.taskmanager.domain.WorkGroup}.
 */
@RestController
@RequestMapping("/api/work-groups")
public class WorkGroupResource {

    private static final Logger LOG = LoggerFactory.getLogger(WorkGroupResource.class);

    private static final String ENTITY_NAME = "taskmanagerWorkGroup";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final WorkGroupService workGroupService;
    private final WorkGroupRepository workGroupRepository;
    private final WorkGroupPermissionService workGroupPermissionService;

    public WorkGroupResource(
        WorkGroupService workGroupService,
        WorkGroupRepository workGroupRepository,
        WorkGroupPermissionService workGroupPermissionService
    ) {
        this.workGroupService = workGroupService;
        this.workGroupRepository = workGroupRepository;
        this.workGroupPermissionService = workGroupPermissionService;
    }

    @PostMapping("")
    public ResponseEntity<WorkGroupDTO> createWorkGroup(@Valid @RequestBody WorkGroupDTO workGroupDTO) throws URISyntaxException {
        LOG.debug("REST request to save WorkGroup : {}", workGroupDTO);
        if (workGroupDTO.getId() != null) {
            throw new BadRequestAlertException("A new workGroup cannot already have an ID", ENTITY_NAME, "idexists");
        }
        workGroupDTO = workGroupService.save(workGroupDTO);
        return ResponseEntity.created(new URI("/api/work-groups/" + workGroupDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, workGroupDTO.getId().toString()))
            .body(workGroupDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkGroupDTO> updateWorkGroup(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody WorkGroupDTO workGroupDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update WorkGroup : {}, {}", id, workGroupDTO);
        if (workGroupDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, workGroupDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!workGroupRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        if (!workGroupPermissionService.isOwner(id)) {
            throw new AccessDeniedException("Only owners can update the work group");
        }

        workGroupDTO = workGroupService.update(workGroupDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, workGroupDTO.getId().toString()))
            .body(workGroupDTO);
    }

    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<WorkGroupDTO> partialUpdateWorkGroup(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody WorkGroupDTO workGroupDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update WorkGroup partially : {}, {}", id, workGroupDTO);
        if (workGroupDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, workGroupDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!workGroupRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        if (!workGroupPermissionService.isOwner(id)) {
            throw new AccessDeniedException("Only owners can partially update the work group");
        }

        Optional<WorkGroupDTO> result = workGroupService.partialUpdate(workGroupDTO);
        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, workGroupDTO.getId().toString())
        );
    }

    @GetMapping("")
    public ResponseEntity<List<WorkGroupDTO>> getAllWorkGroups(@org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of WorkGroups");
        Page<WorkGroupDTO> page = workGroupService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkGroupDTO> getWorkGroup(@PathVariable("id") Long id) {
        LOG.debug("REST request to get WorkGroup : {}", id);
        Optional<WorkGroupDTO> workGroupDTO = workGroupService.findOne(id);
        return ResponseUtil.wrapOrNotFound(workGroupDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkGroup(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete WorkGroup : {}", id);

        if (!workGroupPermissionService.isOwner(id)) {
            throw new AccessDeniedException("Only owners can delete the work group");
        }

        workGroupService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }

    @PutMapping("/{workGroupId}/transfer-ownership/{newOwnerUserId}")
    public ResponseEntity<Void> transferOwnership(
        @PathVariable Long workGroupId,
        @PathVariable String newOwnerUserId
    ) {
        LOG.debug("REST request to transfer ownership of WorkGroup {} to user {}", workGroupId, newOwnerUserId);
        workGroupService.transferOwnership(workGroupId, newOwnerUserId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{workGroupId}/promote-to-moderator/{userId}")
    public ResponseEntity<Void> promoteToModerator(
        @PathVariable Long workGroupId,
        @PathVariable String userId
    ) {
        LOG.debug("REST request to promote user {} to MODERATOR in WorkGroup {}", userId, workGroupId);
        workGroupService.promoteToModerator(workGroupId, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{workGroupId}/demote-moderator/{userId}")
    public ResponseEntity<Void> demoteModerator(
        @PathVariable Long workGroupId,
        @PathVariable String userId
    ) {
        LOG.debug("REST request to demote user {} to MEMBER in WorkGroup {}", userId, workGroupId);
        workGroupService.demoteModerator(workGroupId, userId);
        return ResponseEntity.noContent().build();
    }    

    @DeleteMapping("/{groupId}/members/{targetUserId}")
    public ResponseEntity<Void> removeMember(
        @PathVariable Long groupId,
        @PathVariable String targetUserId
    ) {
        LOG.debug("REST request to remove member {} from group {}", targetUserId, groupId);
        workGroupService.removeMember(groupId, targetUserId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/leave")
    public ResponseEntity<Void> leaveGroup(@PathVariable("id") Long groupId) {
        String currentUserId = SecurityUtils.getCurrentUserLogin().orElseThrow();
        workGroupService.leaveGroup(groupId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
