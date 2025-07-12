import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table, Spinner } from 'reactstrap';
import { JhiItemCount, JhiPagination, getPaginationState } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';

import TaskManagerApiService from 'app/shared/services/TaskManagerApiService';

interface Project {
  id?: number;
  title: string;
  description?: string;
  workGroup?: { id: number; name: string };
}

const Project = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(false);
  const [totalItems, setTotalItems] = useState(0);

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(location, ITEMS_PER_PAGE, 'id'), location.search),
  );

  const fetchProjects = () => {
    setLoading(true);
    TaskManagerApiService.getProjects({
      page: paginationState.activePage - 1,
      size: paginationState.itemsPerPage,
      sort: `${paginationState.sort},${paginationState.order}`,
    })
      .then(response => {
        setProjects(
          response.data.map((p: any) => ({
            ...p,
            workGroup: p.workGroup
              ? {
                  id: p.workGroup.id ?? 0,
                  name: p.workGroup.name ?? '',
                }
              : undefined,
          })),
        );
        const total = response.headers['x-total-count'];
        setTotalItems(total ? parseInt(total, 10) : 0);
      })
      .catch(err => {
        console.error('Error loading projects:', err);
      })
      .finally(() => setLoading(false));
  };

  const sortEntities = () => {
    fetchProjects();
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
      setPaginationState({
        ...paginationState,
        activePage: +page,
        sort: sortField,
        order: sortOrder,
      });
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
      <h2 id="project-heading" data-cy="ProjectHeading">
        Projects
        <div className="d-flex justify-content-end">
          <Button color="info" className="me-2" onClick={fetchProjects} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Refresh list
          </Button>
          <Link to="/project/new" className="btn btn-primary jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Create a new Project
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {loading ? (
          <Spinner />
        ) : projects.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th onClick={toggleSort('id')} className="hand">
                  ID <FontAwesomeIcon icon={getSortIcon('id')} />
                </th>
                <th onClick={toggleSort('title')} className="hand">
                  Title <FontAwesomeIcon icon={getSortIcon('title')} />
                </th>
                <th onClick={toggleSort('description')} className="hand">
                  Description <FontAwesomeIcon icon={getSortIcon('description')} />
                </th>
                <th>
                  Work Group <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {projects.map((project, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/project/${project.id}`} color="link" size="sm">
                      {project.id}
                    </Button>
                  </td>
                  <td>{project.title}</td>
                  <td>{project.description}</td>
                  <td>{project.workGroup ? <Link to={`/work-group/${project.workGroup.id}`}>{project.workGroup.name}</Link> : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/project/${project.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">View</span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/project/${project.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        color="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
                      </Button>
                      <Button
                        onClick={() =>
                          (window.location.href = `/project/${project.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
                        }
                        color="danger"
                        size="sm"
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
          !loading && <div className="alert alert-warning">No Projects found</div>
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

export default Project;
