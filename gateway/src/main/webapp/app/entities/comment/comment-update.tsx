import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams, useLocation } from 'react-router-dom';
import { Button, Col, Row, Spinner } from 'reactstrap';
import { ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import TaskManagerApiService, { Comment as IComment, Task as ITask } from 'app/shared/services/TaskManagerApiService';

export const CommentUpdate = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const [comment, setComment] = useState<Partial<IComment>>({
    content: '',
    createdAt: displayDefaultDateTime(),
  });
  const [tasks, setTasks] = useState<ITask[]>([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  // Carga la lista de tareas y, si es edición, el comentario existente
  useEffect(() => {
    TaskManagerApiService.getTasks()
      .then(resp => setTasks(resp.data))
      .catch(console.error);

    if (!isNew && id) {
      setLoading(true);
      TaskManagerApiService.getComment(Number(id))
        .then(resp => {
          const data = resp.data;
          setComment({
            ...data,
            createdAt: convertDateTimeFromServer(data.createdAt),
          });
        })
        .catch(console.error)
        .finally(() => setLoading(false));
    }
  }, [id, isNew]);

  const handleClose = () => {
    navigate(`/comment${location.search}`);
  };

  const saveEntity = (values: IComment) => {
    setSaving(true);
    const payload: Partial<IComment> = {
      ...comment,
      ...values,
      createdAt: convertDateTimeToServer(values.createdAt).toString(),
      task: tasks.find(t => t.id === Number(values.task)),
    };

    const request = isNew ? TaskManagerApiService.createComment(payload) : TaskManagerApiService.updateComment(payload as IComment);

    request
      .then(() => handleClose())
      .catch(console.error)
      .finally(() => setSaving(false));
  };

  const defaultValues = (): Partial<IComment> =>
    isNew
      ? { createdAt: displayDefaultDateTime() }
      : {
          ...comment,
          task: comment.task,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 data-cy="CommentCreateUpdateHeading">{isNew ? 'Crear un nuevo Comentario' : 'Editar Comentario'}</h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <Spinner color="primary" />
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew && <ValidatedField name="id" readOnly hidden label="ID" id="comment-id" validate={{ required: true }} />}
              <ValidatedField
                label="Contenido"
                id="comment-content"
                name="content"
                data-cy="content"
                type="text"
                validate={{
                  required: { value: true, message: 'Este campo es obligatorio' },
                }}
              />
              <ValidatedField
                label="Creado En"
                id="comment-createdAt"
                name="createdAt"
                data-cy="createdAt"
                type="datetime-local"
                placeholder="YYYY-MM-DD HH:mm"
                validate={{
                  required: { value: true, message: 'Este campo es obligatorio' },
                }}
              />
              <ValidatedField id="comment-task" name="task" data-cy="task" label="Tarea" type="select">
                <option value="" key="0" />
                {tasks.map(task => (
                  <option value={task.id} key={task.id}>
                    {task.title}
                  </option>
                ))}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" to="/comment" replace color="info" disabled={saving}>
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp; Atrás
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={saving}>
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

export default CommentUpdate;
