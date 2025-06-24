package com.dcmc.apps.taskmanager.web.rest;

import static com.dcmc.apps.taskmanager.domain.TaskStatusAsserts.*;
import static com.dcmc.apps.taskmanager.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dcmc.apps.taskmanager.IntegrationTest;
import com.dcmc.apps.taskmanager.domain.TaskStatus;
import com.dcmc.apps.taskmanager.repository.TaskStatusRepository;
import com.dcmc.apps.taskmanager.service.dto.TaskStatusDTO;
import com.dcmc.apps.taskmanager.service.mapper.TaskStatusMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link TaskStatusResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TaskStatusResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Boolean DEFAULT_VISIBLE = false;
    private static final Boolean UPDATED_VISIBLE = true;

    private static final String ENTITY_API_URL = "/api/task-statuses";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTaskStatusMockMvc;

    private TaskStatus taskStatus;

    private TaskStatus insertedTaskStatus;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TaskStatus createEntity() {
        return new TaskStatus().name(DEFAULT_NAME).visible(DEFAULT_VISIBLE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TaskStatus createUpdatedEntity() {
        return new TaskStatus().name(UPDATED_NAME).visible(UPDATED_VISIBLE);
    }

    @BeforeEach
    void initTest() {
        taskStatus = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedTaskStatus != null) {
            taskStatusRepository.delete(insertedTaskStatus);
            insertedTaskStatus = null;
        }
    }

    @Test
    @Transactional
    void createTaskStatus() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the TaskStatus
        TaskStatusDTO taskStatusDTO = taskStatusMapper.toDto(taskStatus);
        var returnedTaskStatusDTO = om.readValue(
            restTaskStatusMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taskStatusDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TaskStatusDTO.class
        );

        // Validate the TaskStatus in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTaskStatus = taskStatusMapper.toEntity(returnedTaskStatusDTO);
        assertTaskStatusUpdatableFieldsEquals(returnedTaskStatus, getPersistedTaskStatus(returnedTaskStatus));

        insertedTaskStatus = returnedTaskStatus;
    }

    @Test
    @Transactional
    void createTaskStatusWithExistingId() throws Exception {
        // Create the TaskStatus with an existing ID
        taskStatus.setId(1L);
        TaskStatusDTO taskStatusDTO = taskStatusMapper.toDto(taskStatus);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTaskStatusMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taskStatusDTO)))
            .andExpect(status().isBadRequest());

        // Validate the TaskStatus in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        taskStatus.setName(null);

        // Create the TaskStatus, which fails.
        TaskStatusDTO taskStatusDTO = taskStatusMapper.toDto(taskStatus);

        restTaskStatusMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taskStatusDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkVisibleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        taskStatus.setVisible(null);

        // Create the TaskStatus, which fails.
        TaskStatusDTO taskStatusDTO = taskStatusMapper.toDto(taskStatus);

        restTaskStatusMockMvc
            .perform(post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taskStatusDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTaskStatuses() throws Exception {
        // Initialize the database
        insertedTaskStatus = taskStatusRepository.saveAndFlush(taskStatus);

        // Get all the taskStatusList
        restTaskStatusMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(taskStatus.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].visible").value(hasItem(DEFAULT_VISIBLE)));
    }

    @Test
    @Transactional
    void getTaskStatus() throws Exception {
        // Initialize the database
        insertedTaskStatus = taskStatusRepository.saveAndFlush(taskStatus);

        // Get the taskStatus
        restTaskStatusMockMvc
            .perform(get(ENTITY_API_URL_ID, taskStatus.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(taskStatus.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.visible").value(DEFAULT_VISIBLE));
    }

    @Test
    @Transactional
    void getNonExistingTaskStatus() throws Exception {
        // Get the taskStatus
        restTaskStatusMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTaskStatus() throws Exception {
        // Initialize the database
        insertedTaskStatus = taskStatusRepository.saveAndFlush(taskStatus);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the taskStatus
        TaskStatus updatedTaskStatus = taskStatusRepository.findById(taskStatus.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTaskStatus are not directly saved in db
        em.detach(updatedTaskStatus);
        updatedTaskStatus.name(UPDATED_NAME).visible(UPDATED_VISIBLE);
        TaskStatusDTO taskStatusDTO = taskStatusMapper.toDto(updatedTaskStatus);

        restTaskStatusMockMvc
            .perform(
                put(ENTITY_API_URL_ID, taskStatusDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(taskStatusDTO))
            )
            .andExpect(status().isOk());

        // Validate the TaskStatus in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTaskStatusToMatchAllProperties(updatedTaskStatus);
    }

    @Test
    @Transactional
    void putNonExistingTaskStatus() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taskStatus.setId(longCount.incrementAndGet());

        // Create the TaskStatus
        TaskStatusDTO taskStatusDTO = taskStatusMapper.toDto(taskStatus);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTaskStatusMockMvc
            .perform(
                put(ENTITY_API_URL_ID, taskStatusDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(taskStatusDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TaskStatus in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTaskStatus() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taskStatus.setId(longCount.incrementAndGet());

        // Create the TaskStatus
        TaskStatusDTO taskStatusDTO = taskStatusMapper.toDto(taskStatus);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaskStatusMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(taskStatusDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TaskStatus in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTaskStatus() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taskStatus.setId(longCount.incrementAndGet());

        // Create the TaskStatus
        TaskStatusDTO taskStatusDTO = taskStatusMapper.toDto(taskStatus);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaskStatusMockMvc
            .perform(put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taskStatusDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the TaskStatus in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTaskStatusWithPatch() throws Exception {
        // Initialize the database
        insertedTaskStatus = taskStatusRepository.saveAndFlush(taskStatus);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the taskStatus using partial update
        TaskStatus partialUpdatedTaskStatus = new TaskStatus();
        partialUpdatedTaskStatus.setId(taskStatus.getId());

        partialUpdatedTaskStatus.visible(UPDATED_VISIBLE);

        restTaskStatusMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTaskStatus.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTaskStatus))
            )
            .andExpect(status().isOk());

        // Validate the TaskStatus in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTaskStatusUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedTaskStatus, taskStatus),
            getPersistedTaskStatus(taskStatus)
        );
    }

    @Test
    @Transactional
    void fullUpdateTaskStatusWithPatch() throws Exception {
        // Initialize the database
        insertedTaskStatus = taskStatusRepository.saveAndFlush(taskStatus);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the taskStatus using partial update
        TaskStatus partialUpdatedTaskStatus = new TaskStatus();
        partialUpdatedTaskStatus.setId(taskStatus.getId());

        partialUpdatedTaskStatus.name(UPDATED_NAME).visible(UPDATED_VISIBLE);

        restTaskStatusMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTaskStatus.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTaskStatus))
            )
            .andExpect(status().isOk());

        // Validate the TaskStatus in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTaskStatusUpdatableFieldsEquals(partialUpdatedTaskStatus, getPersistedTaskStatus(partialUpdatedTaskStatus));
    }

    @Test
    @Transactional
    void patchNonExistingTaskStatus() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taskStatus.setId(longCount.incrementAndGet());

        // Create the TaskStatus
        TaskStatusDTO taskStatusDTO = taskStatusMapper.toDto(taskStatus);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTaskStatusMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, taskStatusDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(taskStatusDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TaskStatus in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTaskStatus() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taskStatus.setId(longCount.incrementAndGet());

        // Create the TaskStatus
        TaskStatusDTO taskStatusDTO = taskStatusMapper.toDto(taskStatus);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaskStatusMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(taskStatusDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TaskStatus in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTaskStatus() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taskStatus.setId(longCount.incrementAndGet());

        // Create the TaskStatus
        TaskStatusDTO taskStatusDTO = taskStatusMapper.toDto(taskStatus);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaskStatusMockMvc
            .perform(
                patch(ENTITY_API_URL).with(csrf()).contentType("application/merge-patch+json").content(om.writeValueAsBytes(taskStatusDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the TaskStatus in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTaskStatus() throws Exception {
        // Initialize the database
        insertedTaskStatus = taskStatusRepository.saveAndFlush(taskStatus);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the taskStatus
        restTaskStatusMockMvc
            .perform(delete(ENTITY_API_URL_ID, taskStatus.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return taskStatusRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected TaskStatus getPersistedTaskStatus(TaskStatus taskStatus) {
        return taskStatusRepository.findById(taskStatus.getId()).orElseThrow();
    }

    protected void assertPersistedTaskStatusToMatchAllProperties(TaskStatus expectedTaskStatus) {
        assertTaskStatusAllPropertiesEquals(expectedTaskStatus, getPersistedTaskStatus(expectedTaskStatus));
    }

    protected void assertPersistedTaskStatusToMatchUpdatableProperties(TaskStatus expectedTaskStatus) {
        assertTaskStatusAllUpdatablePropertiesEquals(expectedTaskStatus, getPersistedTaskStatus(expectedTaskStatus));
    }
}
