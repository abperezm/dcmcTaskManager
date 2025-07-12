import React, { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row, Spinner, Table, Modal, ModalHeader, ModalBody, ModalFooter, Form, FormGroup, Label, Input } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import TaskManagerApiService, {
  Project,
  WorkGroup,
  TaskPriority,
  TaskStatus,
  TaskSummary,
  Task,
} from 'app/shared/services/TaskManagerApiService';

export const ProjectDetail = () => {
  const { id } = useParams<'id'>();
  const [project, setProject] = useState<Project | null>(null);
  const [loadingProject, setLoadingProject] = useState(false);

  const [tasks, setTasks] = useState<TaskSummary[]>([]);
  const [loadingTasks, setLoadingTasks] = useState(false);
  const [deletingTaskId, setDeletingTaskId] = useState<number | null>(null);

  // Modal state and form
  const [showModal, setShowModal] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [priorities, setPriorities] = useState<TaskPriority[]>([]);
  const [priorityId, setPriorityId] = useState<number>();
  const [statuses, setStatuses] = useState<TaskStatus[]>([]);
  const [statusId, setStatusId] = useState<number>();
  const [saving, setSaving] = useState(false);
  const [busyTaskId, setBusyTaskId] = useState<number | null>(null);

  const toggleModal = () => setShowModal(!showModal);

  // Load project
  useEffect(() => {
    if (!id) return;
    setLoadingProject(true);
    TaskManagerApiService.getProject(Number(id))
      .then(res => setProject(res.data))
      .catch(console.error)
      .finally(() => setLoadingProject(false));
  }, [id]);

  // Load tasks
  const loadTasks = () => {
    if (!id) return;
    setLoadingTasks(true);
    TaskManagerApiService.getTaskSummariesByProject(Number(id))
      .then(res => setTasks(res.data))
      .catch(console.error)
      .finally(() => setLoadingTasks(false));
  };
  useEffect(loadTasks, [id]);

  // Load priorities & statuses for modal
  useEffect(() => {
    TaskManagerApiService.getPriorities().then(res => setPriorities(res.data));
    TaskManagerApiService.getStatuses().then(res => setStatuses(res.data));
  }, []);

  // Open modal for new task
  const openForCreate = () => {
    setEditingTask(null);
    setTitle('');
    setDescription('');
    setPriorityId(priorities[0]?.id);
    setStatusId(undefined); // new tasks default on backend
    toggleModal();
  };

  // Open modal for editing existing task
  const openForEdit = (taskId: number) => {
    TaskManagerApiService.getTask(taskId)
      .then(res => {
        const t = res.data;
        setEditingTask(t);
        setTitle(t.title);
        setDescription(t.description || '');
        setPriorityId(t.priority.id);
        setStatusId(t.status.id);
        toggleModal();
      })
      .catch(console.error);
  };

  // Save handler: create or update
  const handleSave = () => {
    if (!project || priorityId == null || !title.trim()) return;
    setSaving(true);

    const payload: Partial<Task> = {
      title: title.trim(),
      description: description.trim() || undefined,
      priority: { id: priorityId },
      project: { id: project.id },
      workGroup: { id: project.workGroup.id },
      archived: editingTask?.archived ?? false,
      // include status: for edit use selected, for create omit to use default
      ...(editingTask ? { status: { id: statusId } } : undefined),
    };

    const request = editingTask
      ? TaskManagerApiService.updateTask({ ...payload, id: editingTask.id } as Task)
      : TaskManagerApiService.createTask(payload);

    request
      .then(() => {
        toggleModal();
        loadTasks();
      })
      .catch(err => {
        console.error(err);
        alert(`Error al ${editingTask ? 'actualizar' : 'crear'} la tarea`);
      })
      .finally(() => setSaving(false));
  };

  const handleDeleteTask = (taskId: number) => {
    if (!window.confirm('¿Seguro que deseas eliminar esta tarea definitivamente?')) return;
    setDeletingTaskId(taskId);
    TaskManagerApiService.deleteArchivedTask(taskId)
      .then(() => loadTasks())
      .catch(err => {
        console.error(err);
        alert('Error al eliminar la tarea');
      })
      .finally(() => setDeletingTaskId(null));
  };

  if (loadingProject) {
    return (
      <Row>
        <Col md="8" className="text-center">
          <Spinner color="primary" />
        </Col>
      </Row>
    );
  }
  if (!project) {
    return (
      <Row>
        <Col md="8">
          <div className="alert alert-warning">Proyecto No Encontrado</div>
          <Button tag={Link} to="/work-group" replace color="info">
            <FontAwesomeIcon icon="arrow-left" /> Volver
          </Button>
        </Col>
      </Row>
    );
  }

  // Separar tareas activas y archivadas
  const activeTasks = tasks.filter(t => !t.archived);
  const archivedTasks = tasks.filter(t => t.archived);

  return (
    <Row>
      <Col md="8">
        <h2>Detalles del Proyecto</h2>
        <dl className="jh-entity-details mb-4">
          <dt>Nombre</dt>
          <dd>{project.title}</dd>
          <dt>Descripción</dt>
          <dd>{project.description || '—'}</dd>
        </dl>

        <div className="d-flex justify-content-between align-items-center mb-2">
          <h4>Tareas Activas</h4>
          <Button color="primary" onClick={openForCreate}>
            <FontAwesomeIcon icon="plus" /> Añadir tarea
          </Button>
        </div>

        {loadingTasks ? (
          <Spinner size="sm" />
        ) : activeTasks.length > 0 ? (
          <Table bordered hover size="sm">
            <thead>
              <tr>
                <th>Título</th>
                <th>Prioridad</th>
                <th>Estado</th>
                <th className="text-end">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {activeTasks.map(task => (
                <tr key={task.id}>
                  <td>{task.title}</td>
                  <td>{task.priority}</td>
                  <td>{task.status}</td>
                  <td className="text-end">
                    <Button tag={Link} to={`/task/${task.id}`} color="info" size="sm" className="me-1">
                      Detalles
                    </Button>
                    <Button color="secondary" size="sm" onClick={() => openForEdit(task.id)}>
                      Editar
                    </Button>
                    <Button
                      color="warning"
                      size="sm"
                      onClick={() => {
                        if (!window.confirm('¿Archivar esta tarea?')) return;
                        if (task.status !== 'DONE') {
                          alert('Solo se pueden archivar tareas completadas.');
                          return;
                        }
                        setBusyTaskId(task.id);
                        TaskManagerApiService.archiveTask(task.id)
                          .then(() => loadTasks())
                          .catch(err => {
                            console.error(err);
                            alert('Error al archivar la tarea');
                          })
                          .finally(() => setBusyTaskId(null));
                      }}
                      disabled={busyTaskId === task.id}
                      className="me-1"
                    >
                      Archivar
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          <p>
            <em>No hay tareas activas.</em>
          </p>
        )}

        {/* Tareas archivadas */}
        <h4 className="mt-4">Tareas Archivadas</h4>
        {loadingTasks ? (
          <Spinner size="sm" />
        ) : archivedTasks.length > 0 ? (
          <Table bordered hover size="sm">
            <thead>
              <tr>
                <th>Título</th>
                <th>Prioridad</th>
                <th>Estado</th>
                <th className="text-end">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {archivedTasks.map(task => (
                <tr key={task.id}>
                  <td>{task.title}</td>
                  <td>{task.priority}</td>
                  <td>{task.status}</td>
                  <td className="text-end">
                    <Button tag={Link} to={`/task/${task.id}`} color="info" size="sm" className="me-1">
                      Detalles
                    </Button>
                    <Button size="sm" color="danger" onClick={() => handleDeleteTask(task.id)} disabled={deletingTaskId === task.id}>
                      Eliminar
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          <p>
            <em>No hay tareas archivadas.</em>
          </p>
        )}

        <div className="mt-4">
          <Button tag={Link} to={`/work-group/${project.workGroup?.id}`} replace color="info">
            <FontAwesomeIcon icon="arrow-left" /> Volver al Grupo de Trabajo
          </Button>
        </div>

        {/* === Modal de creación/edición de tarea === */}
        <Modal isOpen={showModal} toggle={toggleModal}>
          <ModalHeader toggle={toggleModal}>{editingTask ? 'Editar tarea' : 'Nueva tarea'}</ModalHeader>
          <ModalBody>
            <Form>
              <FormGroup>
                <Label for="taskTitle">Título</Label>
                <Input id="taskTitle" type="text" value={title} onChange={e => setTitle(e.target.value)} placeholder="Título de la tarea" />
              </FormGroup>
              <FormGroup>
                <Label for="taskDesc">Descripción</Label>
                <Input
                  id="taskDesc"
                  type="textarea"
                  value={description}
                  onChange={e => setDescription(e.target.value)}
                  placeholder="Opcional"
                />
              </FormGroup>
              <FormGroup>
                <Label for="taskPriority">Prioridad</Label>
                <Input id="taskPriority" type="select" value={priorityId} onChange={e => setPriorityId(Number(e.target.value))}>
                  {priorities.map(p => (
                    <option key={p.id} value={p.id}>
                      {p.name}
                    </option>
                  ))}
                </Input>
              </FormGroup>
              {editingTask && (
                <FormGroup>
                  <Label for="taskStatus">Estado</Label>
                  <Input id="taskStatus" type="select" value={statusId} onChange={e => setStatusId(Number(e.target.value))}>
                    {statuses.map(s => (
                      <option key={s.id} value={s.id}>
                        {s.name}
                      </option>
                    ))}
                  </Input>
                </FormGroup>
              )}
            </Form>
          </ModalBody>
          <ModalFooter>
            <Button color="secondary" onClick={toggleModal} disabled={saving}>
              Cancelar
            </Button>
            <Button color="primary" onClick={handleSave} disabled={!title.trim() || saving}>
              {saving ? <Spinner size="sm" /> : <FontAwesomeIcon icon="save" />} Guardar
            </Button>
          </ModalFooter>
        </Modal>
      </Col>
    </Row>
  );
};

export default ProjectDetail;
