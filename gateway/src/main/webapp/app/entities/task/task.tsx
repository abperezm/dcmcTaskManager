import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table, Spinner } from 'reactstrap';
import { JhiItemCount, JhiPagination, TextFormat, getPaginationState } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';

import TaskManagerApiService, { Task as ITask } from 'app/shared/services/TaskManagerApiService';
import { APP_DATE_FORMAT } from 'app/config/constants';

export const Task = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const [tasks, setTasks] = useState<ITask[]>([]);
  const [loading, setLoading] = useState(false);
  const [totalItems, setTotalItems] = useState(0);

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(location, ITEMS_PER_PAGE, 'id'), location.search),
  );

  const getAllEntities = () => {
    setLoading(true);
    const page = paginationState.activePage - 1;
    const size = paginationState.itemsPerPage;
    const sort = `${paginationState.sort},${paginationState.order}`;
    TaskManagerApiService.getTasks(page, size, sort)
      .then(response => {
        setTasks(response.data);
        const total = response.headers['x-total-count'];
        setTotalItems(total ? parseInt(total, 10) : 0);
      })
      .catch(err => console.error('Error loading tasks:', err))
      .finally(() => setLoading(false));
  };

  const sortEntities = () => {
    getAllEntities();
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
      const [sortField, sortOrder] = sort.split(',');
      setPaginationState(prev => ({
        ...prev,
        activePage: +page,
        sort: sortField,
        order: sortOrder,
      }));
    }
  }, [location.search]);

  const handlePagination = (currentPage: number) =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  const toggleSort = (field: string) => () =>
    setPaginationState({
      ...paginationState,
      sort: field,
      order: paginationState.order === ASC ? DESC : ASC,
    });

  const getSortIcon = (field: string) => {
    if (paginationState.sort !== field) {
      return faSort;
    }
    return paginationState.order === ASC ? faSortUp : faSortDown;
  };

  return (
    <div>
      <h2 id="task-heading" data-cy="TaskHeading">
        Tasks
        <div className="d-flex justify-content-end">
          <Button color="info" className="me-2" onClick={sortEntities} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/task/new" className="btn btn-primary jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Task
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {loading ? (
          <Spinner />
        ) : tasks.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={toggleSort('id')}>
                  ID <FontAwesomeIcon icon={getSortIcon('id')} />
                </th>
                <th className="hand" onClick={toggleSort('title')}>
                  Title <FontAwesomeIcon icon={getSortIcon('title')} />
                </th>
                <th className="hand" onClick={toggleSort('description')}>
                  Description <FontAwesomeIcon icon={getSortIcon('description')} />
                </th>
                <th className="hand" onClick={toggleSort('createTime')}>
                  Create Time <FontAwesomeIcon icon={getSortIcon('createTime')} />
                </th>
                <th className="hand" onClick={toggleSort('updateTime')}>
                  Update Time <FontAwesomeIcon icon={getSortIcon('updateTime')} />
                </th>
                <th>
                  Work Group <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  Priority <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  Status <FontAwesomeIcon icon="sort" />
                </th>
                <th>
                  Project <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {tasks.map((task, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Link to={`/task/${task.id}`} className="btn btn-link btn-sm">
                      {task.id}
                    </Link>
                  </td>
                  <td>{task.title}</td>
                  <td>{task.description}</td>
                  <td>{task.createTime ? <TextFormat type="date" value={task.createTime} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{task.updateTime ? <TextFormat type="date" value={task.updateTime} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{task.workGroup ? <Link to={`/work-group/${task.workGroup.id}`}>{task.workGroup.name}</Link> : ''}</td>
                  <td>{task.priority ? <Link to={`/task-priority/${task.priority.id}`}>{task.priority.name}</Link> : ''}</td>
                  <td>{task.status ? <Link to={`/task-status/${task.status.id}`}>{task.status.name}</Link> : ''}</td>
                  <td>{task.project ? <Link to={`/project/${task.project.id}`}>{task.project.title}</Link> : ''}</td>
                  <td className="text-end">
                    <div className="btn-group">
                      <Link to={`/task/${task.id}`} className="btn btn-info btn-sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Link>
                      <Link
                        to={`/task/${task.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        className="btn btn-primary btn-sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Link>
                      <Button
                        color="danger"
                        size="sm"
                        onClick={() =>
                          (window.location.href = `/task/${task.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
                        }
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" /> <span className="d-none d-md-inline">Delete</span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          <div className="alert alert-warning">No Tasks found</div>
        )}
      </div>
      {totalItems > 0 && (
        <div className="d-flex justify-content-center">
          <JhiItemCount page={paginationState.activePage} total={totalItems} itemsPerPage={paginationState.itemsPerPage} />
          <JhiPagination
            activePage={paginationState.activePage}
            onSelect={handlePagination}
            maxButtons={5}
            itemsPerPage={paginationState.itemsPerPage}
            totalItems={totalItems}
          />
        </div>
      )}
    </div>
  );
};

export default Task;
