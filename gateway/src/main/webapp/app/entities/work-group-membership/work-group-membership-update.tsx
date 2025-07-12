/*import React, { useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities as getWorkGroups } from 'app/entities/work-group/work-group.reducer';
import { WorkGroupRole } from 'app/shared/model/enumerations/work-group-role.model';
import { createEntity, getEntity, reset, updateEntity } from './work-group-membership.reducer';

export const WorkGroupMembershipUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const workGroups = useAppSelector(state => state.gateway.workGroup.entities);
  const workGroupMembershipEntity = useAppSelector(state => state.gateway.workGroupMembership.entity);
  const loading = useAppSelector(state => state.gateway.workGroupMembership.loading);
  const updating = useAppSelector(state => state.gateway.workGroupMembership.updating);
  const updateSuccess = useAppSelector(state => state.gateway.workGroupMembership.updateSuccess);
  const workGroupRoleValues = Object.keys(WorkGroupRole);

  const handleClose = () => {
    navigate('/work-group-membership');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getWorkGroups({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    if (values.id !== undefined && typeof values.id !== 'number') {
      values.id = Number(values.id);
    }

    const entity = {
      ...workGroupMembershipEntity,
      ...values,
      workGroup: workGroups.find(it => it.id.toString() === values.workGroup?.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          role: 'OWNER',
          ...workGroupMembershipEntity,
          workGroup: workGroupMembershipEntity?.workGroup?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="gatewayApp.workGroupMembership.home.createOrEditLabel" data-cy="WorkGroupMembershipCreateUpdateHeading">
            Create or edit a Work Group Membership
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField name="id" required readOnly id="work-group-membership-id" label="ID" validate={{ required: true }} />
              ) : null}
              <ValidatedField label="Role" id="work-group-membership-role" name="role" data-cy="role" type="select">
                {workGroupRoleValues.map(workGroupRole => (
                  <option value={workGroupRole} key={workGroupRole}>
                    {workGroupRole}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField id="work-group-membership-workGroup" name="workGroup" data-cy="workGroup" label="Work Group" type="select">
                <option value="" key="0" />
                {workGroups
                  ? workGroups.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.name}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/work-group-membership" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">Back</span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp; Save
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default WorkGroupMembershipUpdate;
*/
