package com.dcmc.apps.taskmanager.web.rest;

import static com.dcmc.apps.taskmanager.domain.TaskPriorityAsserts.*;
import static com.dcmc.apps.taskmanager.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.dcmc.apps.taskmanager.IntegrationTest;
import com.dcmc.apps.taskmanager.domain.TaskPriority;
import com.dcmc.apps.taskmanager.repository.TaskPriorityRepository;
import com.dcmc.apps.taskmanager.service.dto.TaskPriorityDTO;
import com.dcmc.apps.taskmanager.service.mapper.TaskPriorityMapper;
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
 * Integration tests for the {@link TaskPriorityResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class TaskPriorityResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Integer DEFAULT_LEVEL = 1;
    private static final Integer UPDATED_LEVEL = 2;

    private static final Boolean DEFAULT_VISIBLE = false;
    private static final Boolean UPDATED_VISIBLE = true;

    private static final String ENTITY_API_URL = "/api/task-priorities";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskPriorityRepository taskPriorityRepository;

    @Autowired
    private TaskPriorityMapper taskPriorityMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restTaskPriorityMockMvc;

    private TaskPriority taskPriority;

    private TaskPriority insertedTaskPriority;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TaskPriority createEntity() {
        return new TaskPriority().name(DEFAULT_NAME).level(DEFAULT_LEVEL).visible(DEFAULT_VISIBLE);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TaskPriority createUpdatedEntity() {
        return new TaskPriority().name(UPDATED_NAME).level(UPDATED_LEVEL).visible(UPDATED_VISIBLE);
    }

    @BeforeEach
    void initTest() {
        taskPriority = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedTaskPriority != null) {
            taskPriorityRepository.delete(insertedTaskPriority);
            insertedTaskPriority = null;
        }
    }

    @Test
    @Transactional
    void createTaskPriority() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the TaskPriority
        TaskPriorityDTO taskPriorityDTO = taskPriorityMapper.toDto(taskPriority);
        var returnedTaskPriorityDTO = om.readValue(
            restTaskPriorityMockMvc
                .perform(
                    post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taskPriorityDTO))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            TaskPriorityDTO.class
        );

        // Validate the TaskPriority in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedTaskPriority = taskPriorityMapper.toEntity(returnedTaskPriorityDTO);
        assertTaskPriorityUpdatableFieldsEquals(returnedTaskPriority, getPersistedTaskPriority(returnedTaskPriority));

        insertedTaskPriority = returnedTaskPriority;
    }

    @Test
    @Transactional
    void createTaskPriorityWithExistingId() throws Exception {
        // Create the TaskPriority with an existing ID
        taskPriority.setId(1L);
        TaskPriorityDTO taskPriorityDTO = taskPriorityMapper.toDto(taskPriority);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTaskPriorityMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taskPriorityDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TaskPriority in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNameIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        taskPriority.setName(null);

        // Create the TaskPriority, which fails.
        TaskPriorityDTO taskPriorityDTO = taskPriorityMapper.toDto(taskPriority);

        restTaskPriorityMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taskPriorityDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkVisibleIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        taskPriority.setVisible(null);

        // Create the TaskPriority, which fails.
        TaskPriorityDTO taskPriorityDTO = taskPriorityMapper.toDto(taskPriority);

        restTaskPriorityMockMvc
            .perform(
                post(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taskPriorityDTO))
            )
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllTaskPriorities() throws Exception {
        // Initialize the database
        insertedTaskPriority = taskPriorityRepository.saveAndFlush(taskPriority);

        // Get all the taskPriorityList
        restTaskPriorityMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(taskPriority.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].level").value(hasItem(DEFAULT_LEVEL)))
            .andExpect(jsonPath("$.[*].visible").value(hasItem(DEFAULT_VISIBLE)));
    }

    @Test
    @Transactional
    void getTaskPriority() throws Exception {
        // Initialize the database
        insertedTaskPriority = taskPriorityRepository.saveAndFlush(taskPriority);

        // Get the taskPriority
        restTaskPriorityMockMvc
            .perform(get(ENTITY_API_URL_ID, taskPriority.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(taskPriority.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.level").value(DEFAULT_LEVEL))
            .andExpect(jsonPath("$.visible").value(DEFAULT_VISIBLE));
    }

    @Test
    @Transactional
    void getNonExistingTaskPriority() throws Exception {
        // Get the taskPriority
        restTaskPriorityMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingTaskPriority() throws Exception {
        // Initialize the database
        insertedTaskPriority = taskPriorityRepository.saveAndFlush(taskPriority);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the taskPriority
        TaskPriority updatedTaskPriority = taskPriorityRepository.findById(taskPriority.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedTaskPriority are not directly saved in db
        em.detach(updatedTaskPriority);
        updatedTaskPriority.name(UPDATED_NAME).level(UPDATED_LEVEL).visible(UPDATED_VISIBLE);
        TaskPriorityDTO taskPriorityDTO = taskPriorityMapper.toDto(updatedTaskPriority);

        restTaskPriorityMockMvc
            .perform(
                put(ENTITY_API_URL_ID, taskPriorityDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(taskPriorityDTO))
            )
            .andExpect(status().isOk());

        // Validate the TaskPriority in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedTaskPriorityToMatchAllProperties(updatedTaskPriority);
    }

    @Test
    @Transactional
    void putNonExistingTaskPriority() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taskPriority.setId(longCount.incrementAndGet());

        // Create the TaskPriority
        TaskPriorityDTO taskPriorityDTO = taskPriorityMapper.toDto(taskPriority);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTaskPriorityMockMvc
            .perform(
                put(ENTITY_API_URL_ID, taskPriorityDTO.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(taskPriorityDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TaskPriority in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchTaskPriority() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taskPriority.setId(longCount.incrementAndGet());

        // Create the TaskPriority
        TaskPriorityDTO taskPriorityDTO = taskPriorityMapper.toDto(taskPriority);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaskPriorityMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(taskPriorityDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TaskPriority in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamTaskPriority() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taskPriority.setId(longCount.incrementAndGet());

        // Create the TaskPriority
        TaskPriorityDTO taskPriorityDTO = taskPriorityMapper.toDto(taskPriority);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaskPriorityMockMvc
            .perform(
                put(ENTITY_API_URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(taskPriorityDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the TaskPriority in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateTaskPriorityWithPatch() throws Exception {
        // Initialize the database
        insertedTaskPriority = taskPriorityRepository.saveAndFlush(taskPriority);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the taskPriority using partial update
        TaskPriority partialUpdatedTaskPriority = new TaskPriority();
        partialUpdatedTaskPriority.setId(taskPriority.getId());

        partialUpdatedTaskPriority.visible(UPDATED_VISIBLE);

        restTaskPriorityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTaskPriority.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTaskPriority))
            )
            .andExpect(status().isOk());

        // Validate the TaskPriority in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTaskPriorityUpdatableFieldsEquals(
            createUpdateProxyForBean(partialUpdatedTaskPriority, taskPriority),
            getPersistedTaskPriority(taskPriority)
        );
    }

    @Test
    @Transactional
    void fullUpdateTaskPriorityWithPatch() throws Exception {
        // Initialize the database
        insertedTaskPriority = taskPriorityRepository.saveAndFlush(taskPriority);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the taskPriority using partial update
        TaskPriority partialUpdatedTaskPriority = new TaskPriority();
        partialUpdatedTaskPriority.setId(taskPriority.getId());

        partialUpdatedTaskPriority.name(UPDATED_NAME).level(UPDATED_LEVEL).visible(UPDATED_VISIBLE);

        restTaskPriorityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTaskPriority.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedTaskPriority))
            )
            .andExpect(status().isOk());

        // Validate the TaskPriority in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertTaskPriorityUpdatableFieldsEquals(partialUpdatedTaskPriority, getPersistedTaskPriority(partialUpdatedTaskPriority));
    }

    @Test
    @Transactional
    void patchNonExistingTaskPriority() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taskPriority.setId(longCount.incrementAndGet());

        // Create the TaskPriority
        TaskPriorityDTO taskPriorityDTO = taskPriorityMapper.toDto(taskPriority);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTaskPriorityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, taskPriorityDTO.getId())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(taskPriorityDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TaskPriority in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchTaskPriority() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taskPriority.setId(longCount.incrementAndGet());

        // Create the TaskPriority
        TaskPriorityDTO taskPriorityDTO = taskPriorityMapper.toDto(taskPriority);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaskPriorityMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(taskPriorityDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the TaskPriority in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamTaskPriority() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        taskPriority.setId(longCount.incrementAndGet());

        // Create the TaskPriority
        TaskPriorityDTO taskPriorityDTO = taskPriorityMapper.toDto(taskPriority);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTaskPriorityMockMvc
            .perform(
                patch(ENTITY_API_URL)
                    .with(csrf())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(taskPriorityDTO))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the TaskPriority in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteTaskPriority() throws Exception {
        // Initialize the database
        insertedTaskPriority = taskPriorityRepository.saveAndFlush(taskPriority);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the taskPriority
        restTaskPriorityMockMvc
            .perform(delete(ENTITY_API_URL_ID, taskPriority.getId()).with(csrf()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return taskPriorityRepository.count();
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

    protected TaskPriority getPersistedTaskPriority(TaskPriority taskPriority) {
        return taskPriorityRepository.findById(taskPriority.getId()).orElseThrow();
    }

    protected void assertPersistedTaskPriorityToMatchAllProperties(TaskPriority expectedTaskPriority) {
        assertTaskPriorityAllPropertiesEquals(expectedTaskPriority, getPersistedTaskPriority(expectedTaskPriority));
    }

    protected void assertPersistedTaskPriorityToMatchUpdatableProperties(TaskPriority expectedTaskPriority) {
        assertTaskPriorityAllUpdatablePropertiesEquals(expectedTaskPriority, getPersistedTaskPriority(expectedTaskPriority));
    }
}
