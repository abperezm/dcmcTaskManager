import { IWorkGroup } from 'app/shared/model/work-group.model';

export interface IProject {
  id?: number;
  title?: string;
  description?: string | null;
  workGroup?: IWorkGroup | null;
}

export const defaultValue: Readonly<IProject> = {};
