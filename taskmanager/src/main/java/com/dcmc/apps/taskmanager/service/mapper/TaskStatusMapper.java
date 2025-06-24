package com.dcmc.apps.taskmanager.service.mapper;

import com.dcmc.apps.taskmanager.domain.TaskStatus;
import com.dcmc.apps.taskmanager.service.dto.TaskStatusDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link TaskStatus} and its DTO {@link TaskStatusDTO}.
 */
@Mapper(componentModel = "spring")
public interface TaskStatusMapper extends EntityMapper<TaskStatusDTO, TaskStatus> {}
