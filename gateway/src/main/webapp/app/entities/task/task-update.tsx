import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams, useLocation } from 'react-router-dom';
import { Button, Col, Row, Spinner } from 'reactstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import TaskManagerApiService, {
  Task as ITask,
  WorkGroup,
  TaskPriority,
  TaskStatus,
  Project,
} from 'app/shared/services/TaskManagerApiService';

export const TaskUpdate = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const [workGroups, setWorkGroups] = useState<WorkGroup[]>([]);
  const [priorities, setPriorities] = useState<TaskPriority[]>([]);
  const [statuses, setStatuses] = useState<TaskStatus[]>([]);
  const [projects, setProjects] = useState<Project[]>([]);
  const [task, setTask] = useState<ITask>({
    title: '',
    description: '',
    createTime: displayDefaultDateTime(),
    updateTime: displayDefaultDateTime(),
    archived: false,
  } as ITask);

  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  // Carga datos iniciales
  useEffect(() => {
    // cargar catálogos
    TaskManagerApiService.getWorkGroups().then(r => setWorkGroups(r.data));
    TaskManagerApiService.getPriorities().then(r => setPriorities(r.data));
    TaskManagerApiService.getStatuses().then(r => setStatuses(r.data));
    TaskManagerApiService.getProjects().then(r => setProjects(r.data));

    if (!isNew && id) {
      setLoading(true);
      TaskManagerApiService.getTask(Number(id))
        .then(resp => {
          const data = resp.data;
          setTask({
            ...data,
            createTime: convertDateTimeFromServer(data.createTime),
            updateTime: convertDateTimeFromServer(data.updateTime),
          });
        })
        .catch(err => console.error('Error loading Task:', err))
        .finally(() => setLoading(false));
    }
  }, [id, isNew]);

  const handleClose = () => {
    navigate(`/task${location.search}`);
  };

  const saveEntity = values => {
    setSaving(true);

    const entity: ITask = {
      ...task,
      ...values,
      createTime: convertDateTimeToServer(values.createTime),
      updateTime: convertDateTimeToServer(values.updateTime),
      workGroup: workGroups.find(wg => wg.id === values.workGroup) || undefined,
      priority: priorities.find(p => p.id === values.priority),
      status: statuses.find(s => s.id === values.status),
      project: projects.find(pj => pj.id === values.project) || undefined,
      archived: values.archived,
    };

    const request = isNew ? TaskManagerApiService.createTask(entity) : TaskManagerApiService.updateTask(entity);

    request
      .then(() => handleClose())
      .catch(err => console.error('Error saving Task:', err))
      .finally(() => setSaving(false));
  };

  const defaultValues = () =>
    isNew
      ? task
      : {
          ...task,
          workGroup: task.workGroup?.id,
          priority: task.priority?.id,
          status: task.status?.id,
          project: task.project?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 data-cy="TaskCreateUpdateHeading">{isNew ? 'Crear nueva Tarea' : 'Editar Tarea'}</h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <Spinner color="primary" />
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" readOnly hidden label="ID" id="task-id" validate={{ required: true }} />}
              <ValidatedField
                label="Title"
                id="task-title"
                name="title"
                type="text"
                validate={{ required: { value: true, message: 'Este campo es obligatorio.' } }}
              />
              <ValidatedField
                label="Description"
                id="task-description"
                name="description"
                type="text"
                validate={{ required: { value: true, message: 'Este campo es obligatorio.' } }}
              />
              <ValidatedField
                label="Create Time"
                id="task-createTime"
                name="createTime"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{ required: { value: true, message: 'Este campo es obligatorio.' } }}
              />
              <ValidatedField
                label="Update Time"
                id="task-updateTime"
                name="updateTime"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{ required: { value: true, message: 'Este campo es obligatorio.' } }}
              />
              <ValidatedField id="task-workGroup" name="workGroup" label="Work Group" type="select">
                <option value="" key="0" />
                {workGroups.map(wg => (
                  <option value={wg.id} key={wg.id}>
                    {wg.name}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                id="task-priority"
                name="priority"
                label="Priority"
                type="select"
                validate={{ required: { value: true, message: 'Este campo es obligatorio.' } }}
              >
                <option value="" key="0" />
                {priorities.map(p => (
                  <option value={p.id} key={p.id}>
                    {p.name}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                id="task-status"
                name="status"
                label="Status"
                type="select"
                validate={{ required: { value: true, message: 'Este campo es obligatorio.' } }}
              >
                <option value="" key="0" />
                {statuses.map(s => (
                  <option value={s.id} key={s.id}>
                    {s.name}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField id="task-project" name="project" label="Project" type="select">
                <option value="" key="0" />
                {projects.map(pj => (
                  <option value={pj.id} key={pj.id}>
                    {pj.title}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField label="Archived" id="task-archived" name="archived" type="checkbox" />
              <Button tag={Link} to="/task" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp; Volver
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

export default TaskUpdate;
