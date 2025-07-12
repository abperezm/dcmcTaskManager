import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams, useLocation } from 'react-router-dom';
import { Button, Col, Row, Spinner } from 'reactstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import TaskManagerApiService, { WorkGroup, Project } from 'app/shared/services/TaskManagerApiService';

export const ProjectUpdate = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const [project, setProject] = useState<Project>({ title: '', description: '' });
  const [workGroups, setWorkGroups] = useState<WorkGroup[]>([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  // Carga lista de WorkGroups y, en edición, el proyecto
  useEffect(() => {
    TaskManagerApiService.getWorkGroups()
      .then(resp => setWorkGroups(resp.data))
      .catch(err => console.error('Error al cargar WorkGroups:', err));

    if (!isNew && id) {
      setLoading(true);
      TaskManagerApiService.getProject(Number(id))
        .then(resp => setProject(resp.data))
        .catch(err => console.error('Error al cargar Project:', err))
        .finally(() => setLoading(false));
    }
  }, [id, isNew]);

  const handleClose = () => {
    navigate(`/project${location.search}`);
  };

  const saveEntity = (values: { title: string; description?: string; workGroup?: string }) => {
    setSaving(true);
    // `values.workGroup` viene como string con el id seleccionado.
    const selectedWg = workGroups.find(wg => wg.id === Number(values.workGroup));
    const payload: Partial<Project> = {
      ...project,
      ...values,
      workGroup: selectedWg,
    };

    const request = isNew ? TaskManagerApiService.createProject(payload) : TaskManagerApiService.updateProject(payload as Project);

    request
      .then(() => handleClose())
      .catch(err => console.error('Error al guardar Project:', err))
      .finally(() => setSaving(false));
  };

  // Valores iniciales del formulario
  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...project,
          // Para el select, solo pasamos el id como string
          workGroup: project.workGroup?.id?.toString(),
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 data-cy="ProjectCreateUpdateHeading">{isNew ? 'Crear Proyecto' : 'Editar Proyecto'}</h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <Spinner color="primary" />
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" readOnly hidden label="ID" id="project-id" validate={{ required: true }} />}
              <ValidatedField
                label="Title"
                id="project-title"
                name="title"
                type="text"
                validate={{ required: { value: true, message: 'Este campo es obligatorio' } }}
              />
              <ValidatedField label="Description" id="project-description" name="description" type="text" />
              <ValidatedField id="project-workGroup" name="workGroup" label="Work Group" type="select">
                <option value="" key="0" />
                {workGroups.map(wg => (
                  <option value={wg.id} key={wg.id}>
                    {wg.name}
                  </option>
                ))}
              </ValidatedField>
              <Button tag={Link} to="/project" replace color="info" disabled={saving}>
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

export default ProjectUpdate;
