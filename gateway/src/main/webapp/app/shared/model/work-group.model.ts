export interface IWorkGroup {
  id?: number;
  name?: string;
  description?: string | null;
}

export const defaultValue: Readonly<IWorkGroup> = {};
