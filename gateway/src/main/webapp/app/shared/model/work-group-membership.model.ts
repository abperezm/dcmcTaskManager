import { IWorkGroup } from 'app/shared/model/work-group.model';
import { WorkGroupRole } from 'app/shared/model/enumerations/work-group-role.model';

export interface IWorkGroupMembership {
  id?: number;
  role?: keyof typeof WorkGroupRole;
  workGroup?: IWorkGroup | null;
}

export const defaultValue: Readonly<IWorkGroupMembership> = {};
