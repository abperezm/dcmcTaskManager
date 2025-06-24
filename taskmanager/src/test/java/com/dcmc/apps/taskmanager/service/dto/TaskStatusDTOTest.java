package com.dcmc.apps.taskmanager.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.dcmc.apps.taskmanager.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TaskStatusDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(TaskStatusDTO.class);
        TaskStatusDTO taskStatusDTO1 = new TaskStatusDTO();
        taskStatusDTO1.setId(1L);
        TaskStatusDTO taskStatusDTO2 = new TaskStatusDTO();
        assertThat(taskStatusDTO1).isNotEqualTo(taskStatusDTO2);
        taskStatusDTO2.setId(taskStatusDTO1.getId());
        assertThat(taskStatusDTO1).isEqualTo(taskStatusDTO2);
        taskStatusDTO2.setId(2L);
        assertThat(taskStatusDTO1).isNotEqualTo(taskStatusDTO2);
        taskStatusDTO1.setId(null);
        assertThat(taskStatusDTO1).isNotEqualTo(taskStatusDTO2);
    }
}
