package com.dcmc.apps.taskmanager.service;

import com.dcmc.apps.taskmanager.domain.Authority;
import com.dcmc.apps.taskmanager.domain.User;
import com.dcmc.apps.taskmanager.repository.AuthorityRepository;
import com.dcmc.apps.taskmanager.repository.UserRepository;
import com.dcmc.apps.taskmanager.service.dto.UserSyncDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserSyncService {

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;

    public UserSyncService(UserRepository userRepository, AuthorityRepository authorityRepository) {
        this.userRepository = userRepository;
        this.authorityRepository = authorityRepository;
    }

    @Transactional
    public void syncUser(UserSyncDTO dto) {
        if (dto.getLogin() == null) return;

        userRepository.findOneByLogin(dto.getLogin()).orElseGet(() -> {
            User newUser = new User();
            newUser.setLogin(dto.getLogin());
            newUser.setEmail(dto.getEmail());
            newUser.setFirstName(dto.getFirstName());
            newUser.setLastName(dto.getLastName());
            newUser.setActivated(true);
            newUser.setLangKey("en");

            Set<Authority> authorities = dto.getRoles().stream()
                .map(role -> authorityRepository.findById(role).orElseGet(() -> {
                    Authority newAuthority = new Authority();
                    newAuthority.setName(role);
                    return authorityRepository.save(newAuthority);
                }))
                .collect(Collectors.toSet());

            newUser.setAuthorities(authorities);
            return userRepository.save(newUser);
        });
    }
}
