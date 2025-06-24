package com.dcmc.apps.taskmanager.service.mapper;

import static com.dcmc.apps.taskmanager.domain.TaskStatusAsserts.*;
import static com.dcmc.apps.taskmanager.domain.TaskStatusTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskStatusMapperTest {

    private TaskStatusMapper taskStatusMapper;

    @BeforeEach
    void setUp() {
        taskStatusMapper = new TaskStatusMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getTaskStatusSample1();
        var actual = taskStatusMapper.toEntity(taskStatusMapper.toDto(expected));
        assertTaskStatusAllPropertiesEquals(expected, actual);
    }
}
