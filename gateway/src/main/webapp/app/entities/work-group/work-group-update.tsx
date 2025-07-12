import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams, useLocation } from 'react-router-dom';
import { Button, Col, Row, Spinner } from 'reactstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import TaskManagerApiService, { WorkGroup } from 'app/shared/services/TaskManagerApiService';

export const WorkGroupUpdate = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const [workGroup, setWorkGroup] = useState<WorkGroup>({ name: '', description: '' });
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  // Carga la entidad si estamos editando
  useEffect(() => {
    if (!isNew && id) {
      setLoading(true);
      TaskManagerApiService.getWorkGroup(Number(id))
        .then(resp => setWorkGroup(resp.data))
        .catch(err => console.error('Error al cargar WorkGroup:', err))
        .finally(() => setLoading(false));
    }
  }, [id, isNew]);

  // Después de guardar, vuelve al listado manteniendo paginación/orden
  const handleClose = () => {
    navigate(`/work-group${location.search}`);
  };

  const saveEntity = (values: WorkGroup) => {
    setSaving(true);
    const call = isNew ? TaskManagerApiService.createWorkGroup(values) : TaskManagerApiService.updateWorkGroup({ ...workGroup, ...values });
    call
      .then(() => handleClose())
      .catch(err => console.error('Error al guardar WorkGroup:', err))
      .finally(() => setSaving(false));
  };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 data-cy="WorkGroupCreateUpdateHeading">{isNew ? 'Crear un nuevo Work Group' : 'Editar Work Group'}</h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <div className="text-center">
              <Spinner color="primary" />
            </div>
          ) : (
            <ValidatedForm defaultValues={workGroup} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" readOnly hidden label="ID" id="work-group-id" validate={{ required: true }} />}
              <ValidatedField
                label="Name"
                id="work-group-name"
                name="name"
                type="text"
                validate={{
                  required: { value: true, message: 'Este campo es obligatorio.' },
                }}
              />
              <ValidatedField label="Description" id="work-group-description" name="description" type="text" />
              <Button tag={Link} to={`/work-group${location.search}`} replace color="secondary">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;Cancelar
              </Button>{' '}
              <Button color="primary" type="submit" disabled={saving}>
                <FontAwesomeIcon icon="save" />
                &nbsp;Guardar
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default WorkGroupUpdate;
