package com.dcmc.apps.taskmanager.web.rest.internal;

import com.dcmc.apps.taskmanager.domain.User;
import com.dcmc.apps.taskmanager.security.SecurityUtils;
import com.dcmc.apps.taskmanager.service.UserSyncService;
import com.dcmc.apps.taskmanager.service.dto.UserSyncDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal")
public class UserSyncResource {

    private final UserSyncService userSyncService;

    public UserSyncResource(UserSyncService userSyncService) {
        this.userSyncService = userSyncService;
    }

    @PostMapping("/sync-user")
    public ResponseEntity<Void> syncUser(@Valid @RequestBody UserSyncDTO userSyncDTO) {
        userSyncService.syncUser(userSyncDTO);
        return ResponseEntity.ok().build();
    }
}
