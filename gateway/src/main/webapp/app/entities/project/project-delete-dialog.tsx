import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { Button, Modal, ModalBody, ModalFooter, ModalHeader, Spinner } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import TaskManagerApiService, { Project } from 'app/shared/services/TaskManagerApiService';

export const ProjectDeleteDialog = () => {
  const { id } = useParams<'id'>();
  const navigate = useNavigate();
  const location = useLocation();

  const [project, setProject] = useState<Project | null>(null);
  const [loadModal, setLoadModal] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [deleteSuccess, setDeleteSuccess] = useState(false);

  // Carga la entidad al montar el componente
  useEffect(() => {
    if (id) {
      TaskManagerApiService.getProject(Number(id))
        .then(resp => {
          setProject(resp.data);
          setLoadModal(true);
        })
        .catch(err => console.error('Error al cargar Project:', err));
    }
  }, [id]);

  const handleClose = () => {
    navigate(`/project${location.search}`);
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
    TaskManagerApiService.deleteProject(Number(id))
      .then(() => {
        setDeleteSuccess(true);
      })
      .catch(err => {
        console.error('Error al eliminar Project:', err);
        setDeleting(false);
      });
  };

  return (
    <Modal isOpen={loadModal} toggle={handleClose}>
      <ModalHeader toggle={handleClose} data-cy="projectDeleteDialogHeading">
        Confirmar eliminación
      </ModalHeader>
      <ModalBody id="gatewayApp.project.delete.question">
        {project ? (
          <>
            ¿Estás seguro de que deseas eliminar el proyecto <strong>{project.title}</strong> (ID: {project.id})?
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
          id="jhi-confirm-delete-project"
          data-cy="entityConfirmDeleteButton"
          color="danger"
          onClick={confirmDelete}
          disabled={deleting}
        >
          {deleting ? <Spinner size="sm" /> : <FontAwesomeIcon icon="trash" />} &nbsp; Eliminar
        </Button>
      </ModalFooter>
    </Modal>
  );
};

export default ProjectDeleteDialog;
