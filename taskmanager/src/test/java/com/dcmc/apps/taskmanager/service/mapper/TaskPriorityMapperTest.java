package com.dcmc.apps.taskmanager.service.mapper;

import static com.dcmc.apps.taskmanager.domain.TaskPriorityAsserts.*;
import static com.dcmc.apps.taskmanager.domain.TaskPriorityTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskPriorityMapperTest {

    private TaskPriorityMapper taskPriorityMapper;

    @BeforeEach
    void setUp() {
        taskPriorityMapper = new TaskPriorityMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getTaskPrioritySample1();
        var actual = taskPriorityMapper.toEntity(taskPriorityMapper.toDto(expected));
        assertTaskPriorityAllPropertiesEquals(expected, actual);
    }
}
