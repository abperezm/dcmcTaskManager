import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams, useLocation } from 'react-router-dom';
import { Button, Col, Row, Spinner } from 'reactstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import TaskManagerApiService, { TaskPriority } from 'app/shared/services/TaskManagerApiService';

export const TaskPriorityUpdate = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const [taskPriority, setTaskPriority] = useState<Partial<TaskPriority>>({
    name: '',
    level: undefined,
    visible: true,
  });
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  // Cerrar y volver al listado, conservando query params
  const handleClose = () => {
    navigate(`/task-priority${location.search}`);
  };

  // Cargar entidad si no es nueva
  useEffect(() => {
    if (!isNew && id) {
      setLoading(true);
      TaskManagerApiService.getPriority(Number(id))
        .then(resp => setTaskPriority(resp.data))
        .catch(err => console.error('Error al cargar TaskPriority:', err))
        .finally(() => setLoading(false));
    }
  }, [id, isNew]);

  // Guardar (create o update)
  const saveEntity = (values: TaskPriority) => {
    setSaving(true);
    const payload: TaskPriority = {
      ...taskPriority,
      ...values,
      level: values.level ? Number(values.level) : undefined,
    } as TaskPriority;

    const request = isNew ? TaskManagerApiService.createPriority(payload) : TaskManagerApiService.updatePriority(payload);

    request
      .then(() => handleClose())
      .catch(err => console.error('Error al guardar TaskPriority:', err))
      .finally(() => setSaving(false));
  };

  // Valores iniciales para el formulario
  const defaultValues = () =>
    isNew
      ? { visible: true }
      : {
          name: taskPriority.name,
          level: taskPriority.level,
          visible: taskPriority.visible,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 data-cy="TaskPriorityCreateUpdateHeading">{isNew ? 'Crear Priority' : 'Editar Priority'}</h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <Spinner />
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" readOnly hidden label="ID" id="task-priority-id" validate={{ required: true }} />}
              <ValidatedField
                label="Name"
                id="task-priority-name"
                name="name"
                type="text"
                validate={{
                  required: { value: true, message: 'Este campo es obligatorio' },
                }}
              />
              <ValidatedField label="Level" id="task-priority-level" name="level" type="number" />
              <ValidatedField label="Visible" id="task-priority-visible" name="visible" check type="checkbox" />
              <Button tag={Link} to="/task-priority" replace color="info" disabled={saving}>
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp; Atrás
              </Button>
              &nbsp;
              <Button color="primary" type="submit" disabled={saving}>
                <FontAwesomeIcon icon="save" />
                &nbsp; Guardar
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default TaskPriorityUpdate;
