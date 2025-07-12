import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import {} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './task-status.reducer';

export const TaskStatusDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const taskStatusEntity = useAppSelector(state => state.gateway.taskStatus.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="taskStatusDetailsHeading">Task Status</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{taskStatusEntity.id}</dd>
          <dt>
            <span id="name">Name</span>
          </dt>
          <dd>{taskStatusEntity.name}</dd>
          <dt>
            <span id="visible">Visible</span>
          </dt>
          <dd>{taskStatusEntity.visible ? 'true' : 'false'}</dd>
        </dl>
        <Button tag={Link} to="/task-status" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/task-status/${taskStatusEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default TaskStatusDetail;
