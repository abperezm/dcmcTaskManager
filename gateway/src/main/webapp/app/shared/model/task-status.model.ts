export interface ITaskStatus {
  id?: number;
  name?: string;
  visible?: boolean;
}

export const defaultValue: Readonly<ITaskStatus> = {
  visible: false,
};
