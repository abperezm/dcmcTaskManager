// src/shared/services/TaskManagerApiService.ts

import axios, { AxiosInstance, AxiosResponse } from 'axios';

// ----------------------
// DTO Interfaces
// ----------------------
export interface WorkGroup {
  id?: number;
  name?: string;
  description?: string;
}

export interface Project {
  id?: number;
  title?: string;
  description?: string;
  workGroup?: WorkGroup;
}

export interface TaskPriority {
  id?: number;
  name?: string;
  level?: number;
  visible?: boolean;
}

export interface TaskStatus {
  id?: number;
  name?: string;
  visible?: boolean;
}

export interface Task {
  id?: number;
  title: string;
  description: string;
  priority: TaskPriority;
  status: TaskStatus;
  createTime: string;
  updateTime: string;
  archived: boolean;
  workGroup?: WorkGroup;
  project?: Project;
}

// Nueva interfaz Comment
export interface Comment {
  id?: number;
  content: string;
  createdAt: string;
  task?: Task;
}

export interface UserWorkGroup {
  id?: number;
  name: string;
  description?: string;
  role: string;
}

export interface ProjectSummary {
  id?: number;
  title: string;
  description?: string;
}

export interface MemberSummary {
  userId: string;
  login: string;
  role: string;
}

export interface WorkGroupDetail {
  id?: number;
  name: string;
  description?: string;
  projects: ProjectSummary[];
  members: MemberSummary[];
}

export interface Account {
  login: string;
  authorities: string[];
  details: Record<string, any>;
}

export interface UserSummary {
  id: string;
  login: string;
}

export interface TaskSummary {
  id: number;
  title: string;
  priority: string;
  status: string;
  archived: boolean;
}

// ----------------------
// Service Class
// ----------------------
class TaskManagerApiService {
  private api: AxiosInstance;

  constructor() {
    this.api = axios.create({
      baseURL: 'http://localhost:8080/services/taskmanager',
      headers: {
        'Content-Type': 'application/json',
      },
    });
  }

  public getPotentialMembers(workGroupId: number): Promise<AxiosResponse<UserSummary[]>> {
    return this.api.get<UserSummary[]>(`/api/work-groups/${workGroupId}/potential-members`);
  }

  public getAccount(): Promise<AxiosResponse<Account>> {
    // Como tu baseURL ya apunta al Gateway,
    // esto invoca GET http://localhost:8080/services/gateway/api/account
    return this.api.get<Account>('/api/account');
  }

  // ===== WorkGroup “mis grupos” =====
  /**
   * Devuelve solo los grupos a los que pertenece el usuario actual,
   * junto con su rol en cada uno.
   */
  public getMyWorkGroups(page?: number, size?: number, sort?: string): Promise<AxiosResponse<UserWorkGroup[]>> {
    return this.api.get<UserWorkGroup[]>('/api/work-groups/my');
  }

  public getWorkGroupDetail(id: number): Promise<AxiosResponse<WorkGroupDetail>> {
    return this.api.get<WorkGroupDetail>(`/api/work-groups/${id}/detail`);
  }

  // ===== WorkGroup Endpoints =====

  public getWorkGroups(
    page?: number,
    size?: number,
    sort?: string,
    params?: { page?: number; size?: number; sort?: string },
  ): Promise<AxiosResponse<WorkGroup[]>> {
    return this.api.get<WorkGroup[]>('/api/work-groups', { params });
  }
  public getWorkGroup(id: number): Promise<AxiosResponse<WorkGroup>> {
    return this.api.get<WorkGroup>(`/api/work-groups/${id}`);
  }
  public createWorkGroup(data: Partial<WorkGroup>): Promise<AxiosResponse<WorkGroup>> {
    return this.api.post<WorkGroup>('/api/work-groups', data);
  }
  public updateWorkGroup(data: WorkGroup): Promise<AxiosResponse<WorkGroup>> {
    return this.api.put<WorkGroup>(`/api/work-groups/${data.id}`, data);
  }
  public deleteWorkGroup(id: number): Promise<AxiosResponse<void>> {
    return this.api.delete<void>(`/api/work-groups/${id}`);
  }

  // ===== WorkGroup Membership Endpoints =====

  /**
   * Transferir la propiedad de un WorkGroup a otro usuario
   * @param workGroupId Id del grupo
   * @param newOwnerUserId Id del nuevo owner
   */
  public transferOwnership(workGroupId: number, newOwnerUserId: string): Promise<AxiosResponse<void>> {
    return this.api.put<void>(`/api/work-groups/${workGroupId}/transfer-ownership/${newOwnerUserId}`);
  }

  /**
   * Promover un miembro a moderador
   * @param workGroupId Id del grupo
   * @param userId Id del usuario a promover
   */
  public promoteToModerator(workGroupId: number, userId: string): Promise<AxiosResponse<void>> {
    return this.api.put<void>(`/api/work-groups/${workGroupId}/promote-to-moderator/${userId}`);
  }

  /**
   * Degradar un moderador a miembro
   * @param workGroupId Id del grupo
   * @param userId Id del usuario a degradar
   */
  public demoteModerator(workGroupId: number, userId: string): Promise<AxiosResponse<void>> {
    return this.api.put<void>(`/api/work-groups/${workGroupId}/demote-moderator/${userId}`);
  }

  /**
   * Añadir un miembro al grupo
   * @param workGroupId Id del grupo
   * @param userId Id del usuario a añadir
   */
  public addMember(workGroupId: number, userId: string): Promise<AxiosResponse<void>> {
    return this.api.post<void>(`/api/work-groups/${workGroupId}/members/${userId}`);
  }

  /**
   * Eliminar un miembro del grupo
   * @param workGroupId Id del grupo
   * @param targetUserId Id del usuario a eliminar
   */
  public removeMember(workGroupId: number, targetUserId: string): Promise<AxiosResponse<void>> {
    return this.api.delete<void>(`/api/work-groups/${workGroupId}/members/${targetUserId}`);
  }

  /**
   * El usuario actual abandona el grupo
   * @param workGroupId Id del grupo
   */
  public leaveGroup(workGroupId: number): Promise<AxiosResponse<void>> {
    return this.api.delete<void>(`/api/work-groups/${workGroupId}/leave`);
  }

  // ===== Project Endpoints =====

  public getProjects(params?: { page?: number; size?: number; sort?: string }): Promise<AxiosResponse<Project[]>> {
    return this.api.get<Project[]>('/api/projects', { params });
  }
  public getProject(id: number): Promise<AxiosResponse<Project>> {
    return this.api.get<Project>(`/api/projects/${id}`);
  }
  public createProject(data: Partial<Project>): Promise<AxiosResponse<Project>> {
    return this.api.post<Project>('/api/projects', data);
  }
  public updateProject(data: Project): Promise<AxiosResponse<Project>> {
    return this.api.put<Project>(`/api/projects/${data.id}`, data);
  }
  public deleteProject(id: number): Promise<AxiosResponse<void>> {
    return this.api.delete<void>(`/api/projects/${id}`);
  }

  // ===== Project‐<u>members & tasks</u> Endpoints =====

  /**
   * Obtiene el listado de TaskSummaryDTO para un proyecto
   */
  public getTaskSummariesByProject(projectId: number): Promise<AxiosResponse<TaskSummary[]>> {
    return this.api.get<TaskSummary[]>(`/api/projects/${projectId}/tasks/summary`);
  }

  /**
   * Actualiza el conjunto de miembros de un proyecto.
   */
  public updateProjectMembers(projectId: number, memberDTOs: UserWorkGroup[]): Promise<AxiosResponse<Project>> {
    return this.api.put<Project>(`/api/projects/${projectId}/members`, memberDTOs);
  }

  /**
   * Obtiene la lista de tareas asignadas a un proyecto.
   */
  public getTasksByProject(projectId: number): Promise<AxiosResponse<Task[]>> {
    return this.api.get<Task[]>(`/api/projects/${projectId}/tasks`);
  }

  /**
   * Asigna un listado de tareas a un proyecto.
   */
  public assignTasksToProject(projectId: number, taskIds: number[]): Promise<AxiosResponse<Project>> {
    return this.api.put<Project>(`/api/projects/${projectId}/tasks`, taskIds);
  }

  /**
   * Elimina la asignación de una tarea de un proyecto.
   */
  public removeTaskFromProject(projectId: number, taskId: number): Promise<AxiosResponse<void>> {
    return this.api.delete<void>(`/api/projects/${projectId}/tasks/${taskId}`);
  }

  // ===== Task Endpoints =====

  public getTasks(
    page?: number,
    size?: number,
    sort?: string,
    params?: { page?: number; size?: number; sort?: string },
  ): Promise<AxiosResponse<Task[]>> {
    return this.api.get<Task[]>('/api/tasks', { params });
  }
  public getTask(id: number): Promise<AxiosResponse<Task>> {
    return this.api.get<Task>(`/api/tasks/${id}`);
  }
  public createTask(data: Partial<Task>): Promise<AxiosResponse<Task>> {
    return this.api.post<Task>('/api/tasks', data);
  }
  public updateTask(data: Task): Promise<AxiosResponse<Task>> {
    return this.api.put<Task>(`/api/tasks/${data.id}`, data);
  }
  public deleteTask(id: number): Promise<AxiosResponse<void>> {
    return this.api.delete<void>(`/api/tasks/${id}`);
  }
  public archiveTask(id: number): Promise<AxiosResponse<Task>> {
    return this.api.post<Task>(`/api/tasks/${id}/archive`);
  }
  public getArchivedTasks(params?: { page?: number; size?: number; sort?: string }): Promise<AxiosResponse<Task[]>> {
    return this.api.get<Task[]>('/api/tasks/archived', { params });
  }
  public deleteArchivedTask(id: number): Promise<AxiosResponse<void>> {
    return this.api.delete<void>(`/api/tasks/archived/${id}`);
  }

  // ===== TaskPriority Endpoints =====

  public getPriorities(params?: { page?: number; size?: number; sort?: string }): Promise<AxiosResponse<TaskPriority[]>> {
    return this.api.get<TaskPriority[]>('/api/task-priorities', { params });
  }
  public getPriority(id: number): Promise<AxiosResponse<TaskPriority>> {
    return this.api.get<TaskPriority>(`/api/task-priorities/${id}`);
  }
  public createPriority(data: Partial<TaskPriority>): Promise<AxiosResponse<TaskPriority>> {
    return this.api.post<TaskPriority>('/api/task-priorities', data);
  }
  public updatePriority(data: TaskPriority): Promise<AxiosResponse<TaskPriority>> {
    return this.api.put<TaskPriority>(`/api/task-priorities/${data.id}`, data);
  }
  public deletePriority(id: number): Promise<AxiosResponse<void>> {
    return this.api.delete<void>(`/api/task-priorities/${id}`);
  }
  public hidePriority(id: number): Promise<AxiosResponse<TaskPriority>> {
    return this.api.patch<TaskPriority>(`/api/task-priorities/${id}`, { visible: false });
  }
  public showPriority(id: number): Promise<AxiosResponse<TaskPriority>> {
    return this.api.patch<TaskPriority>(`/api/task-priorities/${id}`, { visible: true });
  }

  // ===== TaskStatus Endpoints =====

  public getStatuses(params?: { page?: number; size?: number; sort?: string }): Promise<AxiosResponse<TaskStatus[]>> {
    return this.api.get<TaskStatus[]>('/api/task-statuses', { params });
  }
  public getStatus(id: number): Promise<AxiosResponse<TaskStatus>> {
    return this.api.get<TaskStatus>(`/api/task-statuses/${id}`);
  }
  public createStatus(data: Partial<TaskStatus>): Promise<AxiosResponse<TaskStatus>> {
    return this.api.post<TaskStatus>('/api/task-statuses', data);
  }
  public updateStatus(data: TaskStatus): Promise<AxiosResponse<TaskStatus>> {
    return this.api.put<TaskStatus>(`/api/task-statuses/${data.id}`, data);
  }
  public deleteStatus(id: number): Promise<AxiosResponse<void>> {
    return this.api.delete<void>(`/api/task-statuses/${id}`);
  }
  public hideStatus(id: number): Promise<AxiosResponse<TaskStatus>> {
    return this.api.patch<TaskStatus>(`/api/task-statuses/${id}`, { visible: false });
  }
  public showStatus(id: number): Promise<AxiosResponse<TaskStatus>> {
    return this.api.patch<TaskStatus>(`/api/task-statuses/${id}`, { visible: true });
  }

  // ===== Comment Endpoints =====

  public getComments(params?: { page?: number; size?: number; sort?: string }): Promise<AxiosResponse<Comment[]>> {
    return this.api.get<Comment[]>('/api/comments', { params });
  }
  public getComment(id: number): Promise<AxiosResponse<Comment>> {
    return this.api.get<Comment>(`/api/comments/${id}`);
  }
  public createComment(data: Partial<Comment>): Promise<AxiosResponse<Comment>> {
    return this.api.post<Comment>('/api/comments', data);
  }
  public updateComment(data: Comment): Promise<AxiosResponse<Comment>> {
    return this.api.put<Comment>(`/api/comments/${data.id}`, data);
  }
  public deleteComment(id: number): Promise<AxiosResponse<void>> {
    return this.api.delete<void>(`/api/comments/${id}`);
  }
}

// Export a singleton instance
export default new TaskManagerApiService();
