package com.dcmc.apps.taskmanager.service.mapper;

import com.dcmc.apps.taskmanager.domain.TaskPriority;
import com.dcmc.apps.taskmanager.service.dto.TaskPriorityDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TaskPriority} and its DTO {@link TaskPriorityDTO}.
 */
@Mapper(componentModel = "spring")
public interface TaskPriorityMapper extends EntityMapper<TaskPriorityDTO, TaskPriority> {}
