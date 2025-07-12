import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { Button, Modal, ModalBody, ModalFooter, ModalHeader, Spinner } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import TaskManagerApiService, { TaskPriority } from 'app/shared/services/TaskManagerApiService';

export const TaskPriorityDeleteDialog = () => {
  const { id } = useParams<'id'>();
  const navigate = useNavigate();
  const location = useLocation();

  const [taskPriority, setTaskPriority] = useState<TaskPriority | null>(null);
  const [loadModal, setLoadModal] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteSuccess, setDeleteSuccess] = useState(false);

  // Cargar la prioridad al montar el componente
  useEffect(() => {
    if (id) {
      TaskManagerApiService.getPriority(Number(id))
        .then(response => {
          setTaskPriority(response.data);
          setLoadModal(true);
        })
        .catch(err => console.error('Error al cargar TaskPriority:', err));
    }
  }, [id]);

  const handleClose = () => {
    navigate(`/task-priority${location.search}`);
  };

  // Cuando la eliminación se complete, cerrar modal y volver al listado
  useEffect(() => {
    if (deleteSuccess && loadModal) {
      handleClose();
      setLoadModal(false);
    }
  }, [deleteSuccess, loadModal]);

  const confirmDelete = () => {
    if (!id) return;
    setDeleting(true);
    TaskManagerApiService.deletePriority(Number(id))
      .then(() => setDeleteSuccess(true))
      .catch(err => {
        console.error('Error al eliminar TaskPriority:', err);
        setDeleting(false);
      });
  };

  return (
    <Modal isOpen={loadModal} toggle={handleClose}>
      <ModalHeader toggle={handleClose} data-cy="taskPriorityDeleteDialogHeading">
        Confirmar eliminación
      </ModalHeader>
      <ModalBody id="gatewayApp.taskPriority.delete.question">
        {taskPriority ? (
          <>
            ¿Estás seguro de que deseas eliminar la prioridad <strong>{taskPriority.name}</strong> (ID: {taskPriority.id})?
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
          id="jhi-confirm-delete-taskPriority"
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

export default TaskPriorityDeleteDialog;
