package com.dcmc.apps.taskmanager.service;

import com.dcmc.apps.taskmanager.domain.WorkGroup;
import com.dcmc.apps.taskmanager.domain.WorkGroupMembership;
import com.dcmc.apps.taskmanager.domain.User;
import com.dcmc.apps.taskmanager.domain.enumeration.WorkGroupRole;
import com.dcmc.apps.taskmanager.repository.WorkGroupRepository;
import com.dcmc.apps.taskmanager.security.SecurityUtils;
import com.dcmc.apps.taskmanager.service.dto.WorkGroupDTO;
import com.dcmc.apps.taskmanager.service.mapper.WorkGroupMapper;
import com.dcmc.apps.taskmanager.web.rest.errors.BadRequestAlertException;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.dcmc.apps.taskmanager.domain.WorkGroup}.
 */
@Service
@Transactional
public class WorkGroupService {

    private static final Logger LOG = LoggerFactory.getLogger(WorkGroupService.class);

    private final WorkGroupRepository workGroupRepository;

    private final WorkGroupMapper workGroupMapper;

    private final com.dcmc.apps.taskmanager.repository.WorkGroupMembershipRepository workGroupMembershipRepository;

    private static final String ENTITY_NAME = "taskmanagerWorkGroup";

    public WorkGroupService(
        WorkGroupRepository workGroupRepository,
        WorkGroupMapper workGroupMapper,
        com.dcmc.apps.taskmanager.repository.WorkGroupMembershipRepository workGroupMembershipRepository
    ) {
        this.workGroupRepository = workGroupRepository;
        this.workGroupMapper = workGroupMapper;
        this.workGroupMembershipRepository = workGroupMembershipRepository;
    }

    /**
     * Save a workGroup.
     *
     * @param workGroupDTO the entity to save.
     * @return the persisted entity.
     */
    public WorkGroupDTO save(WorkGroupDTO workGroupDTO) {
        LOG.debug("Request to save WorkGroup : {}", workGroupDTO);
        WorkGroup workGroup = workGroupMapper.toEntity(workGroupDTO);
        workGroup = workGroupRepository.save(workGroup);
        return workGroupMapper.toDto(workGroup);
    }

    /**
     * Update a workGroup.
     *
     * @param workGroupDTO the entity to save.
     * @return the persisted entity.
     */
    public WorkGroupDTO update(WorkGroupDTO workGroupDTO) {
        LOG.debug("Request to update WorkGroup : {}", workGroupDTO);
        WorkGroup workGroup = workGroupMapper.toEntity(workGroupDTO);
        workGroup = workGroupRepository.save(workGroup);
        return workGroupMapper.toDto(workGroup);
    }

    /**
     * Partially update a workGroup.
     *
     * @param workGroupDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<WorkGroupDTO> partialUpdate(WorkGroupDTO workGroupDTO) {
        LOG.debug("Request to partially update WorkGroup : {}", workGroupDTO);

        return workGroupRepository
            .findById(workGroupDTO.getId())
            .map(existingWorkGroup -> {
                workGroupMapper.partialUpdate(existingWorkGroup, workGroupDTO);

                return existingWorkGroup;
            })
            .map(workGroupRepository::save)
            .map(workGroupMapper::toDto);
    }

    /**
     * Get all the workGroups.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<WorkGroupDTO> findAll(Pageable pageable) {
        LOG.debug("Request to get all WorkGroups");
        return workGroupRepository.findAll(pageable).map(workGroupMapper::toDto);
    }

    /**
     * Get one workGroup by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<WorkGroupDTO> findOne(Long id) {
        LOG.debug("Request to get WorkGroup : {}", id);
        return workGroupRepository.findById(id).map(workGroupMapper::toDto);
    }

    /**
     * Delete the workGroup by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete WorkGroup : {}", id);
        workGroupRepository.deleteById(id);
    }

    @Transactional
    public void transferOwnership(Long workGroupId, String newOwnerUserId) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new IllegalStateException("No authenticated user found"));

        WorkGroupMembership currentOwnerMembership = workGroupMembershipRepository
            .findByUserIdAndWorkGroupId(currentUserLogin, workGroupId)
            .orElseThrow(() -> new IllegalStateException("Current user is not a member of the group"));

        if (currentOwnerMembership.getRole() != WorkGroupRole.OWNER) {
            throw new IllegalStateException("Only the current OWNER can transfer ownership");
        }

        WorkGroupMembership newOwnerMembership = workGroupMembershipRepository
            .findByUserIdAndWorkGroupId(newOwnerUserId, workGroupId)
            .orElseThrow(() -> new IllegalStateException("New owner must be a member of the group"));

        // Cambiar roles
        currentOwnerMembership.setRole(WorkGroupRole.MODERADOR);
        newOwnerMembership.setRole(WorkGroupRole.OWNER);

        workGroupMembershipRepository.save(currentOwnerMembership);
        workGroupMembershipRepository.save(newOwnerMembership);
    }

    @Transactional
    public void promoteToModerator(Long workGroupId, String targetUserId) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new IllegalStateException("No authenticated user found"));

        WorkGroupMembership currentMembership = workGroupMembershipRepository
            .findByUserIdAndWorkGroupId(currentUserLogin, workGroupId)
            .orElseThrow(() -> new IllegalStateException("Current user is not a member of the group"));

        if (currentMembership.getRole() == WorkGroupRole.MIEMBRO) {
            throw new IllegalStateException("Only OWNER or MODERATOR can promote members");
        }

        WorkGroupMembership targetMembership = workGroupMembershipRepository
            .findByUserIdAndWorkGroupId(targetUserId, workGroupId)
            .orElseThrow(() -> new IllegalStateException("Target user is not a member of the group"));

        if (targetMembership.getRole() != WorkGroupRole.MIEMBRO) {
            throw new IllegalStateException("Only MEMBERs can be promoted to MODERATOR");
        }

        // Solo OWNER puede promover libremente
        if (currentMembership.getRole() == WorkGroupRole.MODERADOR && targetMembership.getRole() != WorkGroupRole.MIEMBRO) {
            throw new IllegalStateException("MODERATORs can only promote MEMBERs");
        }

        targetMembership.setRole(WorkGroupRole.MODERADOR);
        workGroupMembershipRepository.save(targetMembership);
    }

    @Transactional
    public void demoteModerator(Long workGroupId, String targetUserId) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new IllegalStateException("No authenticated user found"));

        WorkGroupMembership currentMembership = workGroupMembershipRepository
            .findByUserIdAndWorkGroupId(currentUserLogin, workGroupId)
            .orElseThrow(() -> new IllegalStateException("Current user is not a member of the group"));

        if (currentMembership.getRole() != WorkGroupRole.OWNER) {
            throw new IllegalStateException("Only OWNER can remove moderators");
        }

        WorkGroupMembership targetMembership = workGroupMembershipRepository
            .findByUserIdAndWorkGroupId(targetUserId, workGroupId)
            .orElseThrow(() -> new IllegalStateException("Target user is not a member of the group"));

        if (targetMembership.getRole() != WorkGroupRole.MODERADOR) {
            throw new IllegalStateException("Target user is not a moderator");
        }

        targetMembership.setRole(WorkGroupRole.MIEMBRO);
        workGroupMembershipRepository.save(targetMembership);
    }

    @Transactional
    public void addMember(Long workGroupId, String newUserId) {
        String currentUserLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new IllegalStateException("No authenticated user found"));

        WorkGroupMembership currentMembership = workGroupMembershipRepository
            .findByUserIdAndWorkGroupId(currentUserLogin, workGroupId)
            .orElseThrow(() -> new IllegalStateException("Current user is not a member of the group"));

        if (currentMembership.getRole() == WorkGroupRole.MIEMBRO) {
            throw new IllegalStateException("Only OWNER or MODERATOR can add members");
        }

        if (workGroupMembershipRepository.findByUserIdAndWorkGroupId(newUserId, workGroupId).isPresent()) {
            throw new IllegalStateException("User is already a member of the group");
        }

        WorkGroup workGroup = workGroupRepository.findById(workGroupId)
            .orElseThrow(() -> new IllegalStateException("WorkGroup not found"));

        User user = new User();
        user.setId(newUserId);

        WorkGroupMembership newMembership = new WorkGroupMembership()
            .user(user)
            .workGroup(workGroup)
            .role(WorkGroupRole.MIEMBRO);

        workGroupMembershipRepository.save(newMembership);
    }

    private WorkGroupMembership getMembershipOrThrow(String userId, Long groupId) {
    return workGroupMembershipRepository
        .findByUserIdAndWorkGroupId(userId, groupId)
        .orElseThrow(() -> new BadRequestAlertException(
            "User is not a member of this group", ENTITY_NAME, "membershipnotfound"
        ));
    }

    @Transactional
    public void removeMember(Long groupId, String targetUserId) {
        String currentUserId = SecurityUtils.getCurrentUserLogin().orElseThrow();
        WorkGroupMembership currentMembership = getMembershipOrThrow(currentUserId, groupId);
        WorkGroupMembership targetMembership = getMembershipOrThrow(targetUserId, groupId);

        if (currentUserId.equals(targetUserId)) {
            throw new BadRequestAlertException("You cannot remove yourself using this action", ENTITY_NAME, "selfremoval");
        }

        switch (currentMembership.getRole()) {
            case OWNER -> {
                // OWNER puede eliminar a cualquiera, excepto a sí mismo
            }
            case MODERADOR -> {
                if (targetMembership.getRole() != WorkGroupRole.MIEMBRO) {
                    throw new BadRequestAlertException("MODERATOR can only remove MEMBERs", ENTITY_NAME, "forbidden");
                }
            }
            default -> throw new BadRequestAlertException("You are not allowed to remove members", ENTITY_NAME, "forbidden");
        }

        workGroupMembershipRepository.delete(targetMembership);
    }

    @Transactional
    public void leaveGroup(Long groupId, String currentUserId) {
        WorkGroupMembership membership = getMembershipOrThrow(currentUserId, groupId);

        if (membership.getRole() == WorkGroupRole.OWNER) {
            throw new BadRequestAlertException(
                "Owner cannot leave the group. Transfer ownership first.",
                ENTITY_NAME,
                "ownercannotleave"
            );
        }

        workGroupMembershipRepository.delete(membership);
    }
}
