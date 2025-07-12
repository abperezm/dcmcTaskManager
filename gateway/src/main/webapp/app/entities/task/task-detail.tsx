import React, { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row, Spinner } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import TaskManagerApiService, { Task } from 'app/shared/services/TaskManagerApiService';

export const TaskDetail = () => {
  const { id } = useParams<'id'>();
  const [task, setTask] = useState<Task | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (id) {
      setLoading(true);
      TaskManagerApiService.getTask(Number(id))
        .then(resp => setTask(resp.data))
        .catch(err => console.error('Error fetching Task:', err))
        .finally(() => setLoading(false));
    }
  }, [id]);

  if (loading) {
    return (
      <Row>
        <Col md="8" className="text-center">
          <Spinner color="primary" />
        </Col>
      </Row>
    );
  }

  if (!task) {
    return (
      <Row>
        <Col md="8">
          <div className="alert alert-warning">Task no encontrada</div>
          <Button tag={Link} to="/work-group" replace color="info">
            <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Volver</span>
          </Button>
        </Col>
      </Row>
    );
  }

  return (
    <Row>
      <Col md="8">
        <h2 data-cy="taskDetailsHeading">Tarea</h2>
        <dl className="jh-entity-details">
          <dt>Título</dt>
          <dd>{task.title}</dd>

          <dt>Descripción</dt>
          <dd>{task.description}</dd>

          <dt>Fecha de creación</dt>
          <dd>{task.createTime ? <TextFormat value={task.createTime} type="date" format={APP_DATE_FORMAT} /> : '—'}</dd>

          <dt>Fecha de actualización</dt>
          <dd>{task.updateTime ? <TextFormat value={task.updateTime} type="date" format={APP_DATE_FORMAT} /> : '—'}</dd>

          <dt>Work Group</dt>
          <dd>{task.workGroup ? task.workGroup.name : '—'}</dd>

          <dt>Prioridad</dt>
          <dd>{task.priority ? task.priority.name : '—'}</dd>

          <dt>Estado</dt>
          <dd>{task.status ? task.status.name : '—'}</dd>

          <dt>Proyecto</dt>
          <dd>{task.project ? task.project.title : '—'}</dd>
        </dl>
        <Button tag={Link} to={`/project/${task.project.id}`} replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Volver al Proyecto</span>
        </Button>{' '}
      </Col>
    </Row>
  );
};

export default TaskDetail;
