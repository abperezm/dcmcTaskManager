package com.dcmc.apps.taskmanager.repository;

import com.dcmc.apps.taskmanager.domain.Task;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Task entity.
 *
 * When extending this class, extend TaskRepositoryWithBagRelationships too.
 * For more information refer to https://github.com/jhipster/generator-jhipster/issues/17990.
 */
@Repository
public interface TaskRepository extends TaskRepositoryWithBagRelationships, JpaRepository<Task, Long> {
    default Optional<Task> findOneWithEagerRelationships(Long id) {
        return this.fetchBagRelationships(this.findOneWithToOneRelationships(id));
    }

    default List<Task> findAllWithEagerRelationships() {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships());
    }

    default Page<Task> findAllWithEagerRelationships(Pageable pageable) {
        return this.fetchBagRelationships(this.findAllWithToOneRelationships(pageable));
    }

    @Query(
        value = "select task from Task task left join fetch task.workGroup left join fetch task.project",
        countQuery = "select count(task) from Task task"
    )
    Page<Task> findAllWithToOneRelationships(Pageable pageable);

    @Query("select task from Task task left join fetch task.workGroup left join fetch task.project")
    List<Task> findAllWithToOneRelationships();

    @Query("select task from Task task left join fetch task.workGroup left join fetch task.project where task.id =:id")
    Optional<Task> findOneWithToOneRelationships(@Param("id") Long id);
}
