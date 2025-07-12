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

    // Eager loading methods, ahora incluye también priority y status
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
        value = """
            select task
            from Task task
            left join fetch task.workGroup
            left join fetch task.project
            left join fetch task.priority
            left join fetch task.status
            """,
        countQuery = "select count(task) from Task task"
    )
    Page<Task> findAllWithToOneRelationships(Pageable pageable);

    @Query("""
        select task
        from Task task
        left join fetch task.workGroup
        left join fetch task.project
        left join fetch task.priority
        left join fetch task.status
        """)
    List<Task> findAllWithToOneRelationships();

    @Query("""
        select task
        from Task task
        left join fetch task.workGroup
        left join fetch task.project
        left join fetch task.priority
        left join fetch task.status
        where task.id =:id
        """)
    Optional<Task> findOneWithToOneRelationships(@Param("id") Long id);

    // Archivado
    Page<Task> findAllByArchivedTrue(Pageable pageable);

    Page<Task> findAllByArchivedFalse(Pageable pageable);

    List<Task> findAllByArchivedTrue();

    List<Task> findAllByArchivedFalse();

    Page<Task> findByArchivedTrue(Pageable pageable);

    Page<Task> findByArchivedFalse(Pageable pageable);
}
