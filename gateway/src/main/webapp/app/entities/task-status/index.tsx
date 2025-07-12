import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import TaskStatus from './task-status';
import TaskStatusDetail from './task-status-detail';
import TaskStatusUpdate from './task-status-update';
import TaskStatusDeleteDialog from './task-status-delete-dialog';

const TaskStatusRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<TaskStatus />} />
    <Route path="new" element={<TaskStatusUpdate />} />
    <Route path=":id">
      <Route index element={<TaskStatusDetail />} />
      <Route path="edit" element={<TaskStatusUpdate />} />
      <Route path="delete" element={<TaskStatusDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default TaskStatusRoutes;
