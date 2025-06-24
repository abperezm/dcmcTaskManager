package com.dcmc.apps.taskmanager.service.mapper;

import com.dcmc.apps.taskmanager.domain.*;
import com.dcmc.apps.taskmanager.service.dto.*;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Task} and its DTO {@link TaskDTO}.
 */
@Mapper(componentModel = "spring", uses = { TaskPriorityMapper.class, TaskStatusMapper.class })
public interface TaskMapper extends EntityMapper<TaskDTO, Task> {

    @Mapping(target = "workGroup", source = "workGroup", qualifiedByName = "workGroupName")
    @Mapping(target = "assignedMembers", source = "assignedMembers", qualifiedByName = "userLoginSet")
    @Mapping(target = "project", source = "project", qualifiedByName = "projectTitle")
    @Mapping(target = "priority", source = "priority", qualifiedByName = "taskPriorityName")
    @Mapping(target = "status", source = "status", qualifiedByName = "taskStatusName")
    TaskDTO toDto(Task task);

    @Mapping(target = "removeAssignedMembers", ignore = true)
    Task toEntity(TaskDTO taskDTO);

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
    default Set<UserDTO> toDtoUserLoginSet(Set<User> users) {
        return users.stream().map(this::toDtoUserLogin).collect(Collectors.toSet());
    }

    @Named("projectTitle")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "title", source = "title")
    ProjectDTO toDtoProjectTitle(Project project);

    @Named("taskPriorityName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    TaskPriorityDTO toDtoTaskPriority(TaskPriority priority);

    @Named("taskStatusName")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    TaskStatusDTO toDtoTaskStatus(TaskStatus status);
}
