import React, { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row, Spinner, Table, Modal, ModalHeader, ModalBody, ModalFooter, FormGroup, Input, Label, Form } from 'reactstrap';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { useAppSelector } from 'app/config/store';

import TaskManagerApiService, { UserSummary } from 'app/shared/services/TaskManagerApiService';
import Task from '../task/task';

interface WorkGroupDetail {
  id?: number;
  name: string;
  description?: string;
  projects: { id?: number; title: string; description?: string }[];
  members: { userId: string; login: string; role: string }[];
}

export const WorkGroupDetail = () => {
  const { id } = useParams<'id'>();
  const [detail, setDetail] = useState<WorkGroupDetail | null>(null);
  const [currentRole, setCurrentRole] = useState<string>();
  const [currentLogin, setCurrentLogin] = useState<string>();
  const [loading, setLoading] = useState(false);
  const [busyUser, setBusyUser] = useState<string | null>(null);
  const authorities = useAppSelector(state => state.authentication.account.authorities);
  const isAdmin = authorities?.includes('ROLE_ADMIN');

  // Para añadir miembros
  const [showAddModal, setShowAddModal] = useState(false);
  const [potentialMembers, setPotentialMembers] = useState<UserSummary[]>([]);
  const [newMemberId, setNewMemberId] = useState<string>('');

  // Nuevo estado para el modal de transferencia
  const [showTransferModal, setShowTransferModal] = useState(false);
  const [newOwnerId, setNewOwnerId] = useState<string>('');

  // --- Nuevo estado para el modal de proyectos ---
  const [showProjectModal, setShowProjectModal] = useState(false);
  const [projTitle, setProjTitle] = useState('');
  const [projDesc, setProjDesc] = useState('');
  const [savingProject, setSavingProject] = useState(false);

  // 1) Trae info del grupo
  const fetchDetail = () => {
    if (!id) return;
    setLoading(true);
    TaskManagerApiService.getWorkGroupDetail(Number(id))
      .then(res => setDetail(res.data))
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  // 2) Trae tu rol en este grupo
  const fetchMyRole = () => {
    if (!id) return;
    TaskManagerApiService.getMyWorkGroups()
      .then(res => {
        const me = res.data.find(g => g.id === Number(id));
        setCurrentRole(me?.role);
      })
      .catch(console.error);
  };

  // 3) Trae tu login/id desde /api/account
  const fetchLogin = () => {
    TaskManagerApiService.getAccount()
      .then(res => setCurrentLogin(res.data.login))
      .catch(console.error);
  };

  // 4) Trae usuarios potenciales para añadir
  const fetchPotentialMembers = () => {
    if (!id) return;
    TaskManagerApiService.getPotentialMembers(Number(id))
      .then(res => setPotentialMembers(res.data))
      .catch(console.error);
  };

  useEffect(() => {
    fetchDetail();
    fetchMyRole();
    fetchLogin();
  }, [id]);

  // Cuando abrimos el modal, recargamos la lista de candidatos
  useEffect(() => {
    if (showAddModal) {
      fetchPotentialMembers();
      setNewMemberId('');
    }
  }, [showAddModal, id]);

  // Helper para llamadas que modifican miembros
  const doAction = (fn: () => Promise<any>, onDone?: () => void) => {
    setBusyUser(fn === handleLeave ? 'leave' : fn.name);
    fn()
      .then(() => {
        if (onDone) onDone();
        else fetchDetail();
      })
      .catch(console.error)
      .finally(() => setBusyUser(null));
  };

  const handlePromote = (userId: string) => {
    if (!window.confirm('¿Seguro que deseas promover a moderador?')) return;
    doAction(() => TaskManagerApiService.promoteToModerator(Number(id), userId));
  };

  const handleDemote = (userId: string) => {
    if (!window.confirm('¿Seguro que deseas degradar a miembro?')) return;
    doAction(() => TaskManagerApiService.demoteModerator(Number(id), userId));
  };

  const handleRemove = (userId: string) => {
    if (!window.confirm('¿Seguro que deseas eliminar este miembro?')) return;
    doAction(() => TaskManagerApiService.removeMember(Number(id), userId));
  };

  const handleLeave = () => {
    if (currentRole === 'OWNER') {
      alert('Primero debes transferir la propiedad del grupo antes de abandonarlo.');
      return;
    }
    if (!window.confirm('¿Seguro que deseas abandonar el grupo?')) return;
    doAction(
      () => TaskManagerApiService.leaveGroup(Number(id)),
      () => (window.location.href = '/work-group'),
    );
  };

  const handleAddMember = () => {
    if (!newMemberId) return;
    TaskManagerApiService.addMember(Number(id), newMemberId)
      .then(() => {
        setShowAddModal(false);
        fetchDetail();
      })
      .catch(console.error);
  };

  const handleCreateProject = () => {
    setSavingProject(true);
    TaskManagerApiService.createProject({
      title: projTitle,
      description: projDesc,
      workGroup: {
        id: detail.id,
        name: detail.name,
      },
    })
      .then(() => {
        setShowProjectModal(false);
        setProjTitle('');
        setProjDesc('');
        fetchDetail();
      })
      .catch(console.error)
      .finally(() => setSavingProject(false));
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
  if (!detail) {
    return (
      <Row>
        <Col md="8">
          <div className="alert alert-warning">No se encontró el Grupo de Trabajo</div>
          <Button tag={Link} to="/work-group" replace color="info">
            <FontAwesomeIcon icon="arrow-left" /> Volver
          </Button>
        </Col>
      </Row>
    );
  }

  return (
    <>
      <Row>
        <Col md="8">
          <h2>Detalles del Grupo</h2>

          <dl className="jh-entity-details mb-4">
            <dt>Nombre</dt>
            <dd>{detail.name}</dd>
            <dt>Descripción</dt>
            <dd>{detail.description || '—'}</dd>
          </dl>

          {/* Botón para abrir modal de añadir miembro */}
          {(isAdmin || currentRole === 'OWNER' || currentRole === 'MODERADOR') && (
            <div className="mb-3">
              <Button color="primary" onClick={() => setShowAddModal(true)}>
                <FontAwesomeIcon icon="user-plus" /> Añadir miembro
              </Button>
            </div>
          )}
          {currentRole === 'OWNER' && (
            <Button color="primary" onClick={() => setShowTransferModal(true)}>
              <FontAwesomeIcon icon="exchange-alt" /> Transferir propiedad
            </Button>
          )}

          <h4>Miembros</h4>
          {detail.members.length ? (
            <Table bordered hover size="sm">
              <thead>
                <tr>
                  <th>Usuario</th>
                  <th>Rol</th>
                  <th className="text-end">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {detail.members.map(m => (
                  <tr key={m.userId}>
                    <td>{m.login}</td>
                    <td>{m.role}</td>
                    <td className="text-end">
                      {m.userId !== currentLogin && (
                        <>
                          {(isAdmin || currentRole === 'OWNER' || currentRole === 'MODERADOR') && m.role === 'MIEMBRO' && (
                            <Button
                              size="sm"
                              color="success"
                              className="me-1"
                              onClick={() => handlePromote(m.userId)}
                              disabled={busyUser === m.userId}
                            >
                              <FontAwesomeIcon icon="arrow-up" /> Promover
                            </Button>
                          )}
                          {isAdmin ||
                            (isAdmin || currentRole === 'OWNER' && m.role === 'MODERADOR' && (
                              <Button
                                size="sm"
                                color="warning"
                                className="me-1"
                                onClick={() => handleDemote(m.userId)}
                                disabled={busyUser === m.userId}
                              >
                                <FontAwesomeIcon icon="arrow-down" /> Degradar
                              </Button>
                            ))}
                          {(isAdmin || currentRole === 'OWNER' || (currentRole === 'MODERADOR' && m.role === 'MIEMBRO')) && (
                            <Button size="sm" color="danger" onClick={() => handleRemove(m.userId)} disabled={busyUser === m.userId}>
                              <FontAwesomeIcon icon="trash" /> Eliminar
                            </Button>
                          )}
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          ) : (
            <p>
              <em>No hay miembros.</em>
            </p>
          )}

          {/** ── Botón que abre el modal de nuevo proyecto ── **/}
          <div className="mb-3">
            <Button color="primary" onClick={() => setShowProjectModal(true)}>
              <FontAwesomeIcon icon="plus" /> Añadir proyecto
            </Button>
          </div>

          <h4>Proyectos</h4>
          {detail.projects.length ? (
            <Table bordered hover size="sm">
              <thead>
                <tr>
                  <th>Título</th>
                  <th>Acción</th>
                </tr>
              </thead>
              <tbody>
                {detail.projects.map(p => (
                  <tr key={p.id}>
                    <td>
                      <strong>{p.title}</strong>
                    </td>
                    <td>
                      <Link to={`/project/${p.id}`}>
                        <strong>Detalles</strong>
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          ) : (
            <p>
              <em>No hay proyectos.</em>
            </p>
          )}

          <div className="mt-4">
            <Button tag={Link} to="/work-group" replace color="info">
              <FontAwesomeIcon icon="arrow-left" /> Volver
            </Button>
            <Button color="danger" onClick={handleLeave} disabled={busyUser === 'leave'}>
              <FontAwesomeIcon icon="sign-out-alt" /> Abandonar grupo
            </Button>
          </div>
        </Col>
      </Row>

      {/* Modal para añadir miembro */}
      <Modal isOpen={showAddModal} toggle={() => setShowAddModal(false)}>
        <ModalHeader toggle={() => setShowAddModal(false)}>Añadir miembro</ModalHeader>
        <ModalBody>
          <select className="form-select" value={newMemberId} onChange={e => setNewMemberId(e.target.value)}>
            <option value="">– Selecciona usuario –</option>
            {potentialMembers.map(u => (
              <option key={u.id} value={u.id}>
                {u.login}
              </option>
            ))}
          </select>
        </ModalBody>
        <ModalFooter>
          <Button color="secondary" onClick={() => setShowAddModal(false)}>
            Cancelar
          </Button>
          <Button color="primary" disabled={!newMemberId} onClick={handleAddMember}>
            Añadir
          </Button>
        </ModalFooter>
      </Modal>

      {/* Modal de transferencia */}
      <Modal isOpen={showTransferModal} toggle={() => setShowTransferModal(false)}>
        <ModalHeader toggle={() => setShowTransferModal(false)}>Transferir propiedad</ModalHeader>
        <ModalBody>
          <FormGroup>
            <Label for="newOwner">Selecciona el nuevo propietario</Label>
            <Input type="select" id="newOwner" value={newOwnerId} onChange={e => setNewOwnerId(e.target.value)}>
              <option value="" disabled>
                -- elige un miembro --
              </option>
              {detail.members
                .filter(m => m.role !== 'OWNER')
                .map(m => (
                  <option key={m.userId} value={m.userId}>
                    {m.login} ({m.role})
                  </option>
                ))}
            </Input>
          </FormGroup>
        </ModalBody>
        <ModalFooter>
          <Button color="secondary" onClick={() => setShowTransferModal(false)}>
            Cancelar
          </Button>
          <Button
            color="primary"
            onClick={() => {
              if (!newOwnerId) return alert('Selecciona un miembro');
              doAction(
                () => TaskManagerApiService.transferOwnership(Number(id), newOwnerId),
                () => {
                  setShowTransferModal(false);
                  fetchMyRole();
                  fetchDetail();
                },
              );
            }}
            disabled={!newOwnerId}
          >
            Confirmar
          </Button>
        </ModalFooter>
      </Modal>

      {/** ── Modal de creación de proyecto ── **/}
      <Modal isOpen={showProjectModal} toggle={() => setShowProjectModal(false)}>
        <ModalHeader toggle={() => setShowProjectModal(false)}>Nuevo Proyecto en “{detail.name}”</ModalHeader>
        <ModalBody>
          <Form>
            <FormGroup>
              <Label for="projTitle">Título</Label>
              <Input
                id="projTitle"
                type="text"
                value={projTitle}
                onChange={e => setProjTitle(e.target.value)}
                placeholder="Nombre del proyecto"
              />
            </FormGroup>
            <FormGroup>
              <Label for="projDesc">Descripción</Label>
              <Input id="projDesc" type="textarea" value={projDesc} onChange={e => setProjDesc(e.target.value)} placeholder="Opcional" />
            </FormGroup>
          </Form>
        </ModalBody>
        <ModalFooter>
          <Button color="secondary" onClick={() => setShowProjectModal(false)} disabled={savingProject}>
            Cancelar
          </Button>
          <Button color="primary" onClick={handleCreateProject} disabled={savingProject || !projTitle.trim()}>
            {savingProject ? <Spinner size="sm" /> : <FontAwesomeIcon icon="save" />} Guardar
          </Button>
        </ModalFooter>
      </Modal>
    </>
  );
};

export default WorkGroupDetail;
