package com.dcmc.apps.taskmanager.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.dcmc.apps.taskmanager.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TaskPriorityDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(TaskPriorityDTO.class);
        TaskPriorityDTO taskPriorityDTO1 = new TaskPriorityDTO();
        taskPriorityDTO1.setId(1L);
        TaskPriorityDTO taskPriorityDTO2 = new TaskPriorityDTO();
        assertThat(taskPriorityDTO1).isNotEqualTo(taskPriorityDTO2);
        taskPriorityDTO2.setId(taskPriorityDTO1.getId());
        assertThat(taskPriorityDTO1).isEqualTo(taskPriorityDTO2);
        taskPriorityDTO2.setId(2L);
        assertThat(taskPriorityDTO1).isNotEqualTo(taskPriorityDTO2);
        taskPriorityDTO1.setId(null);
        assertThat(taskPriorityDTO1).isNotEqualTo(taskPriorityDTO2);
    }
}
