export interface ITaskPriority {
  id?: number;
  name?: string;
  level?: number | null;
  visible?: boolean;
}

export const defaultValue: Readonly<ITaskPriority> = {
  visible: false,
};
