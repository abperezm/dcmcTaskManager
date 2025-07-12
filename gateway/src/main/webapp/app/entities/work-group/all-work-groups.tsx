// src/main/webapp/app/entities/work-group/all-work-groups.tsx

import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { JhiItemCount, JhiPagination, getPaginationState } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';

import TaskManagerApiService, { WorkGroup } from 'app/shared/services/TaskManagerApiService';

export const AllWorkGroups = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const [workGroups, setWorkGroups] = useState<WorkGroup[]>([]);
  const [loading, setLoading] = useState(false);
  const [totalItems, setTotalItems] = useState(0);

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(location, ITEMS_PER_PAGE, 'id'), location.search),
  );

  const fetchAll = () => {
    setLoading(true);
    const { activePage, itemsPerPage, sort, order } = paginationState;
    TaskManagerApiService.getWorkGroups(activePage - 1, itemsPerPage, `${sort},${order}`)
      .then(response => {
        setWorkGroups(response.data);
        const total = response.headers['x-total-count'];
        setTotalItems(total ? parseInt(total, 10) : 0);
      })
      .catch(err => console.error('Error loading all work groups:', err))
      .finally(() => setLoading(false));
  };

  const sortAndNavigate = () => {
    fetchAll();
    const params = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (location.search !== params) {
      navigate(`${location.pathname}${params}`);
    }
  };

  useEffect(() => {
    sortAndNavigate();
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

  const changeSort = (field: string) => () =>
    setPaginationState({
      ...paginationState,
      sort: field,
      order: paginationState.order === ASC ? DESC : ASC,
    });

  const getSortIcon = (field: string) =>
    paginationState.sort !== field ? 'sort' : paginationState.order === ASC ? 'sort-up' : 'sort-down';

  const handlePagination = (page: number) => setPaginationState({ ...paginationState, activePage: page });

  return (
    <div>
      <h2 id="all-work-group-heading" data-cy="AllWorkGroupsHeading">
        Todos los Grupos de Trabajo
        <div className="d-flex justify-content-end">
          <Button color="info" className="me-2" onClick={fetchAll} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} /> Recargar
          </Button>
          <Link to="/work-group/new" className="btn btn-primary" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp; Crear Grupo
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {workGroups.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={changeSort('name')}>
                  Nombre <FontAwesomeIcon icon={getSortIcon('name')} />
                </th>
                <th className="hand" onClick={changeSort('description')}>
                  Descripción <FontAwesomeIcon icon={getSortIcon('description')} />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {workGroups.map((wg, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>{wg.name}</td>
                  <td>{wg.description}</td>
                  <td className="text-end">
                    <Button tag={Link} to={`/work-group/${wg.id}`} color="info" size="sm" className="me-1">
                      <FontAwesomeIcon icon="eye" /> Ver
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && <div className="alert alert-warning">No se encontraron grupos</div>
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

export default AllWorkGroups;
