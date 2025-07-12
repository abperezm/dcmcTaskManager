import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { Button, Modal, ModalBody, ModalFooter, ModalHeader, Spinner } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import TaskManagerApiService, { Comment as IComment } from 'app/shared/services/TaskManagerApiService';

export const CommentDeleteDialog = () => {
  const { id } = useParams<'id'>();
  const navigate = useNavigate();
  const location = useLocation();

  const [comment, setComment] = useState<IComment | null>(null);
  const [loadModal, setLoadModal] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteSuccess, setDeleteSuccess] = useState(false);

  // Carga el comentario y abre el modal
  useEffect(() => {
    if (id) {
      TaskManagerApiService.getComment(Number(id))
        .then(resp => {
          setComment(resp.data);
          setLoadModal(true);
        })
        .catch(console.error);
    }
  }, [id]);

  const handleClose = () => {
    navigate(`/comment${location.search}`);
  };

  // Cuando la eliminación se completa, cierra el modal
  useEffect(() => {
    if (deleteSuccess && loadModal) {
      handleClose();
      setLoadModal(false);
    }
  }, [deleteSuccess, loadModal]);

  const confirmDelete = () => {
    if (!comment?.id) return;
    setDeleting(true);
    TaskManagerApiService.deleteComment(comment.id)
      .then(() => setDeleteSuccess(true))
      .catch(err => {
        console.error('Error al eliminar Comment:', err);
        setDeleting(false);
      });
  };

  return (
    <Modal isOpen={loadModal} toggle={handleClose}>
      <ModalHeader toggle={handleClose} data-cy="commentDeleteDialogHeading">
        Confirmar eliminación
      </ModalHeader>
      <ModalBody id="gatewayApp.comment.delete.question">
        {comment ? (
          <>
            ¿Estás seguro de que deseas eliminar el comentario <strong>#{comment.id}</strong>?
          </>
        ) : (
          <Spinner size="sm" />
        )}
      </ModalBody>
      <ModalFooter>
        <Button color="secondary" onClick={handleClose} disabled={deleting}>
          <FontAwesomeIcon icon="ban" />
          &nbsp; Cancelar
        </Button>
        <Button
          id="jhi-confirm-delete-comment"
          data-cy="entityConfirmDeleteButton"
          color="danger"
          onClick={confirmDelete}
          disabled={deleting}
        >
          {deleting ? <Spinner size="sm" /> : <FontAwesomeIcon icon="trash" />}
          &nbsp; Eliminar
        </Button>
      </ModalFooter>
    </Modal>
  );
};

export default CommentDeleteDialog;
