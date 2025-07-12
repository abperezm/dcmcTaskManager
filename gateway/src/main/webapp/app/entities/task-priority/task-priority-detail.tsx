import React, { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row, Spinner } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import TaskManagerApiService, { TaskPriority } from 'app/shared/services/TaskManagerApiService';

export const TaskPriorityDetail = () => {
  const { id } = useParams<'id'>();
  const [taskPriority, setTaskPriority] = useState<TaskPriority | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (id) {
      setLoading(true);
      TaskManagerApiService.getPriority(Number(id))
        .then(response => setTaskPriority(response.data))
        .catch(err => console.error('Error al cargar TaskPriority:', err))
        .finally(() => setLoading(false));
    }
  }, [id]);

  if (loading) {
    return (
      <Row>
        <Col md="8" className="text-center">
          <Spinner />
        </Col>
      </Row>
    );
  }

  if (!taskPriority) {
    return (
      <Row>
        <Col md="8">
          <div className="alert alert-warning">Task Priority no encontrada</div>
          <Button tag={Link} to="/task-priority" replace color="info">
            <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Volver</span>
          </Button>
        </Col>
      </Row>
    );
  }

  return (
    <Row>
      <Col md="8">
        <h2 data-cy="taskPriorityDetailsHeading">Task Priority</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{taskPriority.id}</dd>
          <dt>
            <span id="name">Name</span>
          </dt>
          <dd>{taskPriority.name}</dd>
          <dt>
            <span id="level">Level</span>
          </dt>
          <dd>{taskPriority.level}</dd>
          <dt>
            <span id="visible">Visible</span>
          </dt>
          <dd>{taskPriority.visible ? 'true' : 'false'}</dd>
        </dl>
        <Button tag={Link} to="/task-priority" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Volver</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/task-priority/${taskPriority.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Editar</span>
        </Button>
      </Col>
    </Row>
  );
};

export default TaskPriorityDetail;
