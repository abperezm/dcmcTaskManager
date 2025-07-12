import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { Button, Modal, ModalBody, ModalFooter, ModalHeader, Spinner } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import TaskManagerApiService, { WorkGroup } from 'app/shared/services/TaskManagerApiService';

export const WorkGroupDeleteDialog = () => {
  const { id } = useParams<'id'>();
  const navigate = useNavigate();
  const location = useLocation();

  const [workGroup, setWorkGroup] = useState<WorkGroup | null>(null);
  const [loadModal, setLoadModal] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteSuccess, setDeleteSuccess] = useState(false);

  // Carga la entidad al montar el componente
  useEffect(() => {
    if (id) {
      TaskManagerApiService.getWorkGroup(Number(id))
        .then(resp => {
          setWorkGroup(resp.data);
          setLoadModal(true);
        })
        .catch(err => console.error('Error al cargar WorkGroup:', err));
    }
  }, [id]);

  const handleClose = () => {
    navigate(`/work-group${location.search}`);
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
    TaskManagerApiService.deleteWorkGroup(Number(id))
      .then(() => {
        setDeleteSuccess(true);
      })
      .catch(err => {
        console.error('Error al eliminar WorkGroup:', err);
        setDeleting(false);
      });
  };

  return (
    <Modal isOpen={loadModal} toggle={handleClose}>
      <ModalHeader toggle={handleClose} data-cy="workGroupDeleteDialogHeading">
        Confirmar eliminación
      </ModalHeader>
      <ModalBody id="gatewayApp.workGroup.delete.question">
        {workGroup ? (
          <>
            ¿Estás seguro de que deseas eliminar el grupo de trabajo <strong>{workGroup.name}</strong> (ID: {workGroup.id})?
          </>
        ) : (
          <div className="text-center">
            <Spinner size="sm" />
          </div>
        )}
      </ModalBody>
      <ModalFooter>
        <Button color="secondary" onClick={handleClose} disabled={deleting}>
          <FontAwesomeIcon icon="ban" />
          &nbsp; Cancelar
        </Button>
        <Button
          id="jhi-confirm-delete-workGroup"
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

export default WorkGroupDeleteDialog;
export const getWorkGroupDeleteDialogRoute = (id: number | string) => `/work-group/${id}/delete`;
