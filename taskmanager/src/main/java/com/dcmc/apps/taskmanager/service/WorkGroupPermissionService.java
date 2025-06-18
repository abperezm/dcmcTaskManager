package com.dcmc.apps.taskmanager.service;

import com.dcmc.apps.taskmanager.domain.User;
import com.dcmc.apps.taskmanager.domain.WorkGroupMembership;
import com.dcmc.apps.taskmanager.domain.enumeration.WorkGroupRole;
import com.dcmc.apps.taskmanager.repository.WorkGroupMembershipRepository;
import com.dcmc.apps.taskmanager.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class WorkGroupPermissionService {

    private final WorkGroupMembershipRepository membershipRepository;
    private final UserService userService;

    public WorkGroupPermissionService(WorkGroupMembershipRepository membershipRepository, UserService userService) {
        this.membershipRepository = membershipRepository;
        this.userService = userService;
    }

    public boolean isOwner(Long workGroupId) {
        return hasRole(workGroupId, WorkGroupRole.OWNER);
    }

    public boolean isModerator(Long workGroupId) {
        return hasRole(workGroupId, WorkGroupRole.MODERADOR);
    }

    public boolean canManageMembers(Long workGroupId) {
        return isOwner(workGroupId) || isModerator(workGroupId);
    }

    public boolean isMember(Long workGroupId) {
        return getUserRole(workGroupId)
            .map(role -> role == WorkGroupRole.MIEMBRO || role == WorkGroupRole.MODERADOR || role == WorkGroupRole.OWNER)
            .orElse(false);
    }

    public boolean hasRole(Long workGroupId, WorkGroupRole role) {
        return getUserRole(workGroupId)
            .map(currentRole -> currentRole == role)
            .orElse(false);
    }

    public Optional<WorkGroupRole> getUserRole(Long workGroupId) {
        Optional<String> loginOpt = SecurityUtils.getCurrentUserLogin();
        if (loginOpt.isEmpty()) return Optional.empty();

        return userService.getUserWithAuthoritiesByLogin(loginOpt.get())
            .flatMap(user -> membershipRepository.findByUserIdAndWorkGroupId(Long.valueOf(user.getId()), workGroupId))
            .map(WorkGroupMembership::getRole);
    }
}
