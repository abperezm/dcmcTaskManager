package com.dcmc.apps.taskmanager.domain;

import static com.dcmc.apps.taskmanager.domain.TaskStatusTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.dcmc.apps.taskmanager.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TaskStatusTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TaskStatus.class);
        TaskStatus taskStatus1 = getTaskStatusSample1();
        TaskStatus taskStatus2 = new TaskStatus();
        assertThat(taskStatus1).isNotEqualTo(taskStatus2);

        taskStatus2.setId(taskStatus1.getId());
        assertThat(taskStatus1).isEqualTo(taskStatus2);

        taskStatus2 = getTaskStatusSample2();
        assertThat(taskStatus1).isNotEqualTo(taskStatus2);
    }
}
