package com.dcmc.apps.taskmanager.service.mapper;

import com.dcmc.apps.taskmanager.domain.Project;
import com.dcmc.apps.taskmanager.domain.User;
import com.dcmc.apps.taskmanager.domain.WorkGroup;
import com.dcmc.apps.taskmanager.service.dto.ProjectDTO;
import com.dcmc.apps.taskmanager.service.dto.UserDTO;
import com.dcmc.apps.taskmanager.service.dto.WorkGroupDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Project} and its DTO {@link ProjectDTO}.
 */
@Mapper(componentModel = "spring")
public interface ProjectMapper extends EntityMapper<ProjectDTO, Project> {
    @Mapping(target = "workGroup", source = "workGroup", qualifiedByName = "workGroupName")
    @Mapping(target = "members", source = "members", qualifiedByName = "userLoginSet")
    ProjectDTO toDto(Project s);

    @Mapping(target = "removeMembers", ignore = true)
    Project toEntity(ProjectDTO projectDTO);

    @Named("workGroupName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    WorkGroupDTO toDtoWorkGroupName(WorkGroup workGroup);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);

    @Named("userLoginSet")
    default Set<UserDTO> toDtoUserLoginSet(Set<User> user) {
        return user.stream().map(this::toDtoUserLogin).collect(Collectors.toSet());
    }
}
