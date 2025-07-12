import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table, Spinner } from 'reactstrap';
import { JhiItemCount, JhiPagination, getPaginationState } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';

import TaskManagerApiService, { TaskPriority } from 'app/shared/services/TaskManagerApiService';

export const TaskPriorityList = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const [taskPriorities, setTaskPriorities] = useState<TaskPriority[]>([]);
  const [loading, setLoading] = useState(false);
  const [totalItems, setTotalItems] = useState(0);

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(location, ITEMS_PER_PAGE, 'id'), location.search),
  );

  const loadList = () => {
    setLoading(true);
    TaskManagerApiService.getPriorities({
      page: paginationState.activePage - 1,
      size: paginationState.itemsPerPage,
      sort: `${paginationState.sort},${paginationState.order}`,
    })
      .then(response => {
        setTaskPriorities(response.data);
        const total = response.headers['x-total-count'];
        setTotalItems(total ? parseInt(total, 10) : 0);
      })
      .catch(err => console.error('Error fetching task priorities:', err))
      .finally(() => setLoading(false));
  };

  const sortEntities = () => {
    loadList();
    const endURL = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (location.search !== endURL) {
      navigate(`${location.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [paginationState.activePage, paginationState.sort, paginationState.order]);

  useEffect(() => {
    const params = new URLSearchParams(location.search);
    const page = params.get('page');
    const sort = params.get(SORT);
    if (page && sort) {
      const [field, order] = sort.split(',');
      setPaginationState({
        ...paginationState,
        activePage: +page,
        sort: field,
        order,
      });
    }
  }, [location.search]);

  const handlePagination = (page: number) => setPaginationState({ ...paginationState, activePage: page });

  const toggleSort = (field: string) => () =>
    setPaginationState({
      ...paginationState,
      sort: field,
      order: paginationState.order === ASC ? DESC : ASC,
    });

  const getSortIcon = (field: string) => {
    if (paginationState.sort !== field) return faSort;
    return paginationState.order === ASC ? faSortUp : faSortDown;
  };

  return (
    <div>
      <h2 id="task-priority-heading" data-cy="TaskPriorityHeading">
        Prioridades de Tarea
        <div className="d-flex justify-content-end">
          <Button color="info" className="me-2" onClick={loadList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Recargar
          </Button>
          <Link to="/task-priority/new" className="btn btn-primary jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Crear una nueva Prioridad de Tarea
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {loading ? (
          <Spinner />
        ) : taskPriorities.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={toggleSort('id')}>
                  ID <FontAwesomeIcon icon={getSortIcon('id')} />
                </th>
                <th className="hand" onClick={toggleSort('name')}>
                  Name <FontAwesomeIcon icon={getSortIcon('name')} />
                </th>
                <th className="hand" onClick={toggleSort('level')}>
                  Level <FontAwesomeIcon icon={getSortIcon('level')} />
                </th>
                <th className="hand" onClick={toggleSort('visible')}>
                  Visible <FontAwesomeIcon icon={getSortIcon('visible')} />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {taskPriorities.map((tp, i) => (
                <tr key={i} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/task-priority/${tp.id}`} color="link" size="sm">
                      {tp.id}
                    </Button>
                  </td>
                  <td>{tp.name}</td>
                  <td>{tp.level}</td>
                  <td>{tp.visible ? 'Yes' : 'No'}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button
                        tag={Link}
                        to={`/task-priority/${tp.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        color="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Editar</span>
                      </Button>
                      <Button
                        color="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                        onClick={() =>
                          (window.location.href = `/task-priority/${tp.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
                        }
                      >
                        <FontAwesomeIcon icon="trash" /> <span className="d-none d-md-inline">Eliminar</span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-warning">No Task Priorities found</div>
        )}
      </div>
      {totalItems > 0 && (
        <div>
          <div className="d-flex justify-content-center">
            <JhiItemCount page={paginationState.activePage} total={totalItems} itemsPerPage={paginationState.itemsPerPage} />
          </div>
          <div className="d-flex justify-content-center">
            <JhiPagination
              activePage={paginationState.activePage}
              onSelect={handlePagination}
              maxButtons={5}
              itemsPerPage={paginationState.itemsPerPage}
              totalItems={totalItems}
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default TaskPriorityList;
