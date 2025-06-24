package com.dcmc.apps.taskmanager.domain;

import static com.dcmc.apps.taskmanager.domain.TaskPriorityTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.dcmc.apps.taskmanager.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TaskPriorityTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TaskPriority.class);
        TaskPriority taskPriority1 = getTaskPrioritySample1();
        TaskPriority taskPriority2 = new TaskPriority();
        assertThat(taskPriority1).isNotEqualTo(taskPriority2);

        taskPriority2.setId(taskPriority1.getId());
        assertThat(taskPriority1).isEqualTo(taskPriority2);

        taskPriority2 = getTaskPrioritySample2();
        assertThat(taskPriority1).isNotEqualTo(taskPriority2);
    }
}
