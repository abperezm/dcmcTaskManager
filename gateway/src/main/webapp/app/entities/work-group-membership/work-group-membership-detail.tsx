import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import {} from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './work-group-membership.reducer';

export const WorkGroupMembershipDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const workGroupMembershipEntity = useAppSelector(state => state.gateway.workGroupMembership.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="workGroupMembershipDetailsHeading">Work Group Membership</h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">ID</span>
          </dt>
          <dd>{workGroupMembershipEntity.id}</dd>
          <dt>
            <span id="role">Role</span>
          </dt>
          <dd>{workGroupMembershipEntity.role}</dd>
          <dt>Work Group</dt>
          <dd>{workGroupMembershipEntity.workGroup ? workGroupMembershipEntity.workGroup.name : ''}</dd>
        </dl>
        <Button tag={Link} to="/work-group-membership" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Back</span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/work-group-membership/${workGroupMembershipEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Edit</span>
        </Button>
      </Col>
    </Row>
  );
};

export default WorkGroupMembershipDetail;
