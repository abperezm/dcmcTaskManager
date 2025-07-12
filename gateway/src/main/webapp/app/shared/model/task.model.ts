import dayjs from 'dayjs';
import { IWorkGroup } from 'app/shared/model/work-group.model';
import { ITaskPriority } from 'app/shared/model/task-priority.model';
import { ITaskStatus } from 'app/shared/model/task-status.model';
import { IProject } from 'app/shared/model/project.model';

export interface ITask {
  id?: number;
  title?: string;
  description?: string;
  createTime?: dayjs.Dayjs;
  updateTime?: dayjs.Dayjs;
  workGroup?: IWorkGroup | null;
  priority?: ITaskPriority | null;
  status?: ITaskStatus | null;
  project?: IProject | null;
}

export const defaultValue: Readonly<ITask> = {};
