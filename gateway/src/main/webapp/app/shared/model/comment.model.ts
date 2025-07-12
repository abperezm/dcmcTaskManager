import dayjs from 'dayjs';
import { ITask } from 'app/shared/model/task.model';

export interface IComment {
  id?: number;
  content?: string;
  createdAt?: dayjs.Dayjs;
  task?: ITask | null;
}

export const defaultValue: Readonly<IComment> = {};
