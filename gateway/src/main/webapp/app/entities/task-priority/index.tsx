import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import TaskPriority from './task-priority';
import TaskPriorityDetail from './task-priority-detail';
import TaskPriorityUpdate from './task-priority-update';
import TaskPriorityDeleteDialog from './task-priority-delete-dialog';

const TaskPriorityRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<TaskPriority />} />
    <Route path="new" element={<TaskPriorityUpdate />} />
    <Route path=":id">
      <Route index element={<TaskPriorityDetail />} />
      <Route path="edit" element={<TaskPriorityUpdate />} />
      <Route path="delete" element={<TaskPriorityDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default TaskPriorityRoutes;
