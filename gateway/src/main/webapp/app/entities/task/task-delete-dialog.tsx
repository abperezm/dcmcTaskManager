import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { Button, Modal, ModalBody, ModalFooter, ModalHeader, Spinner } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import TaskManagerApiService, { Task } from 'app/shared/services/TaskManagerApiService';

export const TaskDeleteDialog = () => {
  const { id } = useParams<'id'>();
  const navigate = useNavigate();
  const location = useLocation();

  const [task, setTask] = useState<Task | null>(null);
  const [loadModal, setLoadModal] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteSuccess, setDeleteSuccess] = useState(false);

  // Carga la entidad al montar el componente
  useEffect(() => {
    if (id) {
      TaskManagerApiService.getTask(Number(id))
        .then(resp => {
          setTask(resp.data);
          setLoadModal(true);
        })
        .catch(err => {
          console.error('Error al cargar la tarea:', err);
        });
    }
  }, [id]);

  const handleClose = () => {
    navigate(`/task${location.search}`);
  };

  // Cuando la eliminación se completa, cerramos el modal y regresamos al listado
  useEffect(() => {
    if (deleteSuccess && loadModal) {
      handleClose();
      setLoadModal(false);
    }
  }, [deleteSuccess, loadModal]);

  const confirmDelete = () => {
    if (!id) return;
    setDeleting(true);
    TaskManagerApiService.deleteTask(Number(id))
      .then(() => setDeleteSuccess(true))
      .catch(err => {
        console.error('Error al eliminar la tarea:', err);
        setDeleting(false);
      });
  };

  return (
    <Modal isOpen={loadModal} toggle={handleClose}>
      <ModalHeader toggle={handleClose} data-cy="taskDeleteDialogHeading">
        Confirmar eliminación
      </ModalHeader>
      <ModalBody id="gatewayApp.task.delete.question">
        {task ? (
          <>
            ¿Estás seguro de que deseas eliminar la tarea <strong>{task.title}</strong> (ID: {task.id})?
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
        <Button id="jhi-confirm-delete-task" data-cy="entityConfirmDeleteButton" color="danger" onClick={confirmDelete} disabled={deleting}>
          {deleting ? <Spinner size="sm" /> : <FontAwesomeIcon icon="trash" />}
          &nbsp; Eliminar
        </Button>
      </ModalFooter>
    </Modal>
  );
};

export default TaskDeleteDialog;
