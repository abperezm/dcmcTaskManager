package com.dcmc.apps.taskmanager.service;

import com.dcmc.apps.taskmanager.domain.WorkGroup;
import com.dcmc.apps.taskmanager.domain.WorkGroupMembership;
import com.dcmc.apps.taskmanager.domain.User;
import com.dcmc.apps.taskmanager.domain.enumeration.WorkGroupRole;
import com.dcmc.apps.taskmanager.repository.WorkGroupRepository;
import com.dcmc.apps.taskmanager.security.SecurityUtils;
import com.dcmc.apps.taskmanager.service.dto.MemberSummaryDTO;
import com.dcmc.apps.taskmanager.service.dto.ProjectSummaryDTO;
import com.dcmc.apps.taskmanager.service.dto.UserSummaryDTO;
import com.dcmc.apps.taskmanager.service.dto.UserWorkGroupDTO;
import com.dcmc.apps.taskmanager.service.dto.WorkGroupDTO;
import com.dcmc.apps.taskmanager.service.dto.WorkGroupDetailDTO;
import com.dcmc.apps.taskmanager.service.mapper.WorkGroupMapper;
import com.dcmc.apps.taskmanager.web.rest.errors.BadRequestAlertException;
import com.dcmc.apps.taskmanager.repository.ProjectRepository;
import com.dcmc.apps.taskmanager.repository.WorkGroupMembershipRepository;
import com.dcmc.apps.taskmanager.service.dto.ProjectSummaryDTO;
import com.dcmc.apps.taskmanager.service.dto.MemberSummaryDTO;
import com.dcmc.apps.taskmanager.service.dto.WorkGroupDetailDTO;
import com.dcmc.apps.taskmanager.service.dto.UserSummaryDTO;
import com.dcmc.apps.taskmanager.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
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

    private final ProjectRepository projectRepository;

    private final UserRepository userRepository;
    
    private final WorkGroupMapper workGroupMapper;

    private final WorkGroupMembershipRepository workGroupMembershipRepository;

    private static final String ENTITY_NAME = "taskmanagerWorkGroup";

    public WorkGroupService(
        WorkGroupRepository workGroupRepository,
        ProjectRepository projectRepository,
        UserRepository userRepository,
        WorkGroupMapper workGroupMapper,
        WorkGroupMembershipRepository workGroupMembershipRepository
    ) {
        this.workGroupRepository = workGroupRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.workGroupMapper = workGroupMapper;
        this.workGroupMembershipRepository = workGroupMembershipRepository;
    }

    /**
     * Save a workGroup and assign the creator as OWNER.
     *
     * @param workGroupDTO the entity to save.
     * @return the persisted entity.
     */
    public WorkGroupDTO save(WorkGroupDTO workGroupDTO) {
        LOG.debug("Request to save WorkGroup : {}", workGroupDTO);
        // 1) Persistir el WorkGroup
        WorkGroup workGroup = workGroupMapper.toEntity(workGroupDTO);
        workGroup = workGroupRepository.save(workGroup);

        // 2) Crear la membresía OWNER para el usuario actual
        String currentUserId = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new IllegalStateException("No authenticated user found"));
        User user = new User();
        user.setId(currentUserId);

        WorkGroupMembership membership = new WorkGroupMembership()
            .user(user)
            .workGroup(workGroup)
            .role(WorkGroupRole.OWNER);
        workGroupMembershipRepository.save(membership);

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
  
    /**
     * Transfiere la propiedad de un grupo a otro usuario.
     * Puede invocar el OWNER del grupo o un administrador (ROLE_ADMIN).
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    @Transactional
    public void transferOwnership(Long workGroupId, String newOwnerUserId) {
        String currentLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new IllegalStateException("No authenticated user found"));
        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority("ROLE_ADMIN");

        WorkGroupMembership currentMembership = null;
        if (!isAdmin) {
            currentMembership = workGroupMembershipRepository
                .findByUserIdAndWorkGroupId(currentLogin, workGroupId)
                .orElseThrow(() -> new IllegalStateException("Current user is not a member of the group"));
            if (currentMembership.getRole() != WorkGroupRole.OWNER) {
                throw new IllegalStateException("Only the current OWNER can transfer ownership");
            }
        }

        WorkGroupMembership newOwnerMembership = workGroupMembershipRepository
            .findByUserIdAndWorkGroupId(newOwnerUserId, workGroupId)
            .orElseThrow(() -> new IllegalStateException("New owner must be a member of the group"));

        // Si viene de un OWNER normal, degradar su rol a MODERADOR
        if (!isAdmin) {
            currentMembership.setRole(WorkGroupRole.MODERADOR);
            workGroupMembershipRepository.save(currentMembership);
        }
        // Asignar OWNER al usuario destino
        newOwnerMembership.setRole(WorkGroupRole.OWNER);
        workGroupMembershipRepository.save(newOwnerMembership);
    }

    /**
     * Promueve un miembro a moderador.
     * Puede ejecutarlo OWNER, MODERADOR, o administrador (ROLE_ADMIN).
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    @Transactional
    public void promoteToModerator(Long workGroupId, String targetUserId) {
        String currentLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new IllegalStateException("No authenticated user found"));
        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority("ROLE_ADMIN");

        WorkGroupMembership currentMembership = null;
        if (!isAdmin) {
            currentMembership = workGroupMembershipRepository
                .findByUserIdAndWorkGroupId(currentLogin, workGroupId)
                .orElseThrow(() -> new IllegalStateException("Current user is not a member of the group"));
            if (currentMembership.getRole() == WorkGroupRole.MIEMBRO) {
                throw new IllegalStateException("Only OWNER or MODERATOR can promote members");
            }
        }

        WorkGroupMembership targetMembership = workGroupMembershipRepository
            .findByUserIdAndWorkGroupId(targetUserId, workGroupId)
            .orElseThrow(() -> new IllegalStateException("Target user is not a member of the group"));
        if (targetMembership.getRole() != WorkGroupRole.MIEMBRO) {
            throw new IllegalStateException("Only MEMBERs can be promoted to MODERATOR");
        }

        // Si un MODERADOR promueve, solo puede hacerlo sobre MEMBER
        if (!isAdmin && currentMembership.getRole() == WorkGroupRole.MODERADOR) {
            // ya hemos verificado que targetMembership es MEMBER
        }
        targetMembership.setRole(WorkGroupRole.MODERADOR);
        workGroupMembershipRepository.save(targetMembership);
    }

    /**
     * Degrada un moderador a miembro.
     * Puede ejecutarlo OWNER o administrador (ROLE_ADMIN).
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    @Transactional
    public void demoteModerator(Long workGroupId, String targetUserId) {
        String currentLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new IllegalStateException("No authenticated user found"));
        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority("ROLE_ADMIN");

        if (!isAdmin) {
            WorkGroupMembership currentMembership = workGroupMembershipRepository
                .findByUserIdAndWorkGroupId(currentLogin, workGroupId)
                .orElseThrow(() -> new IllegalStateException("Current user is not a member of the group"));
            if (currentMembership.getRole() != WorkGroupRole.OWNER) {
                throw new IllegalStateException("Only OWNER can remove moderators");
            }
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

    /**
     * Añade un miembro al grupo.
     * Puede hacerlo OWNER, MODERADOR o administrador (ROLE_ADMIN).
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    @Transactional
    public void addMember(Long workGroupId, String newUserId) {
        String currentLogin = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new IllegalStateException("No authenticated user found"));
        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority("ROLE_ADMIN");

        if (!isAdmin) {
            WorkGroupMembership currentMembership = workGroupMembershipRepository
                .findByUserIdAndWorkGroupId(currentLogin, workGroupId)
                .orElseThrow(() -> new IllegalStateException("Current user is not a member of the group"));
            if (currentMembership.getRole() == WorkGroupRole.MIEMBRO) {
                throw new IllegalStateException("Only OWNER or MODERATOR can add members");
            }
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

    /**
     * Elimina un miembro del grupo.
     * Puede hacerlo OWNER, MODERADOR sobre MIEMBROs, o administrador (ROLE_ADMIN).
     */
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_USER')")
    @Transactional
    public void removeMember(Long groupId, String targetUserId) {
        String currentLogin = SecurityUtils.getCurrentUserLogin().orElseThrow();
        boolean isAdmin = SecurityUtils.hasCurrentUserThisAuthority("ROLE_ADMIN");

        if (!isAdmin) {
            WorkGroupMembership currentMembership = getMembershipOrThrow(currentLogin, groupId);
            if (currentLogin.equals(targetUserId)) {
                throw new BadRequestAlertException("You cannot remove yourself using this action", ENTITY_NAME, "selfremoval");
            }
            // MODERADOR sólo puede eliminar MIEMBRO
            if (currentMembership.getRole() == WorkGroupRole.MODERADOR) {
                WorkGroupMembership targetMembership = getMembershipOrThrow(targetUserId, groupId);
                if (targetMembership.getRole() != WorkGroupRole.MIEMBRO) {
                    throw new BadRequestAlertException("MODERATOR can only remove MEMBERs", ENTITY_NAME, "forbidden");
                }
            }
        }

        WorkGroupMembership targetMembership = workGroupMembershipRepository
            .findByUserIdAndWorkGroupId(targetUserId, groupId)
            .orElseThrow(() -> new BadRequestAlertException(
                "User is not a member of this group", ENTITY_NAME, "membershipnotfound"
            ));
        workGroupMembershipRepository.delete(targetMembership);
    }


    private WorkGroupMembership getMembershipOrThrow(String userId, Long groupId) {
    return workGroupMembershipRepository
        .findByUserIdAndWorkGroupId(userId, groupId)
        .orElseThrow(() -> new BadRequestAlertException(
            "User is not a member of this group", ENTITY_NAME, "membershipnotfound"
        ));
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

    /**
     * Busca todos los grupos a los que pertenece el usuario logueado.
     * @return lista de DTOs con name, description y rol.
     */
    @Transactional(readOnly = true)
    public List<UserWorkGroupDTO> findMyWorkGroups() {
        String currentUser = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new IllegalStateException("No authenticated user found"));

        return workGroupMembershipRepository.findByUserId(currentUser).stream()
            .map(m -> new UserWorkGroupDTO(
                    m.getWorkGroup().getId(),
                    m.getWorkGroup().getName(),
                    m.getWorkGroup().getDescription(),
                    m.getRole()
                ))
            .collect(Collectors.toList());
    }  
    
/**
     * Devuelve el detalle completo de un WorkGroup: datos básicos + proyectos + miembros con rol.
     *
     * @param id el id del grupo de trabajo
     * @return WorkGroupDetailDTO con toda la info
     */
    @Transactional(readOnly = true)
    public WorkGroupDetailDTO findDetail(Long id) {
        LOG.debug("Request to get detailed WorkGroup : {}", id);
        WorkGroup wg = workGroupRepository.findById(id)
            .orElseThrow(() -> new BadRequestAlertException("WorkGroup not found", "workGroup", "idnotfound"));

        // 1) Resumir proyectos
        List<ProjectSummaryDTO> projects = projectRepository
            .findByWorkGroupId(id)                                           // necesita método en ProjectRepository
            .stream()
            .map(p -> new ProjectSummaryDTO(p.getId(), p.getTitle()))
            .collect(Collectors.toList());

        // 2) Resumir miembros
        List<MemberSummaryDTO> members = workGroupMembershipRepository
            .findByWorkGroupId(id)                                          // necesita método en WorkGroupMembershipRepository
            .stream()
            .map(m -> new MemberSummaryDTO(
                m.getUser().getId(),
                m.getUser().getLogin(),
                m.getRole()
            ))
            .collect(Collectors.toList());

        // 3) Construir DTO detalle
        return new WorkGroupDetailDTO(
            wg.getId(),
            wg.getName(),
            wg.getDescription(),
            projects,
            members
        );
    }

    @Transactional(readOnly = true)
    public List<UserSummaryDTO> findPotentialMembers(Long workGroupId) {
        // 1) traer todos los usuarios (vía userRepository o mediante UserSyncClient)
        List<User> all = userRepository.findAll();
        // 2) traer los IDs ya miembros
        Set<String> miembros = workGroupMembershipRepository
            .findByWorkGroupId(workGroupId)
            .stream()
            .map(m -> m.getUser().getId())
            .collect(Collectors.toSet());
        // 3) filtrar y mapear a DTO
        return all.stream()
            .filter(u -> !miembros.contains(u.getId()))
            .map(u -> new UserSummaryDTO(u.getId(), u.getLogin()))
            .collect(Collectors.toList());
    }
}
