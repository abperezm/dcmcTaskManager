import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { JhiItemCount, JhiPagination, getPaginationState } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSort, faSortDown, faSortUp } from '@fortawesome/free-solid-svg-icons';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppSelector } from 'app/config/store';

import TaskManagerApiService, { UserWorkGroup } from 'app/shared/services/TaskManagerApiService';

export const WorkGroup = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const authorities = useAppSelector(state => state.authentication.account.authorities);
  const isAdmin = authorities?.includes('ROLE_ADMIN');

  // Ahora UserWorkGroup incluye `role`
  const [workGroups, setWorkGroups] = useState<UserWorkGroup[]>([]);
  const [loading, setLoading] = useState(false);
  const [totalItems, setTotalItems] = useState(0);

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(location, ITEMS_PER_PAGE, 'name'), location.search),
  );

  const getAllEntities = () => {
    setLoading(true);
    const page = paginationState.activePage - 1;
    const size = paginationState.itemsPerPage;
    const sort = `${paginationState.sort},${paginationState.order}`;
    // Llamamos al endpoint “mis grupos”
    TaskManagerApiService.getMyWorkGroups(page, size, sort)
      .then(response => {
        setWorkGroups(response.data);
        const total = response.headers['x-total-count'];
        setTotalItems(total ? parseInt(total, 10) : response.data.length);
      })
      .catch(err => {
        console.error('Error loading work groups:', err);
      })
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
    // eslint-disable-next-line
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
    // eslint-disable-next-line
  }, [location.search]);

  const handlePagination = (currentPage: number) => setPaginationState({ ...paginationState, activePage: currentPage });

  const toggleSort = (field: string) => () =>
    setPaginationState({
      ...paginationState,
      sort: field,
      order: paginationState.order === ASC ? DESC : ASC,
    });

  const getSortIcon = (field: string) => (paginationState.sort !== field ? faSort : paginationState.order === ASC ? faSortUp : faSortDown);

  return (
    <div>
      <h2 id="work-group-heading" data-cy="WorkGroupHeading">
        Mis grupos de trabajo
        <div className="d-flex justify-content-end">
          <Button color="info" className="me-2" onClick={getAllEntities} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Recargar
          </Button>
          <Link to="/work-group/new" className="btn btn-primary jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Crear Grupo
          </Link>
          {isAdmin && (
            <Link to="/work-group/all" className="btn btn-primary jh-create-entity" data-cy="entityCreateButton">
              Ver Todos los Grupos
            </Link>
          )}
        </div>
      </h2>
      <div className="table-responsive">
        {workGroups.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th onClick={toggleSort('name')} className="hand">
                  Nombre <FontAwesomeIcon icon={getSortIcon('name')} />
                </th>
                <th onClick={toggleSort('description')} className="hand">
                  Descripción <FontAwesomeIcon icon={getSortIcon('description')} />
                </th>
                <th onClick={toggleSort('role')} className="hand">
                  Rol <FontAwesomeIcon icon={getSortIcon('role')} />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {workGroups.map((wg, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>{wg.name}</td>
                  <td>{wg.description}</td>
                  <td>{wg.role}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/work-group/${wg.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" /> <span className="d-none d-md-inline">Detalles</span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-warning">No estás en ningún grupo aún</div>
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

export default WorkGroup;
