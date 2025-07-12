import React, { useEffect, useState } from 'react';
import { Link, useParams, useNavigate } from 'react-router-dom';
import { Button, Col, Row, Spinner } from 'reactstrap';
import { TextFormat } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import TaskManagerApiService, { Comment as IComment } from 'app/shared/services/TaskManagerApiService';

export const CommentDetail = () => {
  const { id } = useParams<'id'>();
  const navigate = useNavigate();

  const [comment, setComment] = useState<IComment | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      TaskManagerApiService.getComment(Number(id))
        .then(resp => setComment(resp.data))
        .catch(console.error)
        .finally(() => setLoading(false));
    }
  }, [id]);

  const handleBack = () => {
    navigate('/comment');
  };

  if (loading) {
    return (
      <Row>
        <Col md="8" className="text-center">
          <Spinner color="primary" />
        </Col>
      </Row>
    );
  }

  if (!comment) {
    return (
      <Row>
        <Col md="8">
          <div className="alert alert-warning">Comentario no encontrado</div>
          <Button color="info" onClick={handleBack}>
            <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Volver</span>
          </Button>
        </Col>
      </Row>
    );
  }

  return (
    <Row>
      <Col md="8">
        <h2 data-cy="commentDetailsHeading">Comentario</h2>
        <dl className="jh-entity-details">
          <dt>ID</dt>
          <dd>{comment.id}</dd>
          <dt>Contenido</dt>
          <dd>{comment.content}</dd>
          <dt>Creado En</dt>
          <dd>{comment.createdAt ? <TextFormat value={comment.createdAt} type="date" format={APP_DATE_FORMAT} /> : '—'}</dd>
          <dt>Tarea</dt>
          <dd>{comment.task ? comment.task.title : '—'}</dd>
        </dl>
        <Button color="info" onClick={handleBack} data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" /> <span className="d-none d-md-inline">Volver</span>
        </Button>
        &nbsp;
        <Button color="primary" onClick={() => navigate(`/comment/${comment.id}/edit`)}>
          <FontAwesomeIcon icon="pencil-alt" /> <span className="d-none d-md-inline">Editar</span>
        </Button>
      </Col>
    </Row>
  );
};

export default CommentDetail;
