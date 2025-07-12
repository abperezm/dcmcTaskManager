import React from 'react';
import { Route } from 'react-router'; // eslint-disable-line

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import WorkGroupRoutes from './work-group';
import ProjectRoutes from './project';
import TaskPriorityRoutes from './task-priority';
import TaskStatusRoutes from './task-status';
import TaskRoutes from './task';
import CommentRoutes from './comment';
import WorkGroupMembershipRoutes from './work-group-membership';
/* jhipster-needle-add-route-import - JHipster will add routes here */

export default () => {
  return (
    <div>
      <ErrorBoundaryRoutes>
        {/* prettier-ignore */}
        <Route path="work-group/*" element={<WorkGroupRoutes />} />
        <Route path="project/*" element={<ProjectRoutes />} />
        <Route path="task-priority/*" element={<TaskPriorityRoutes />} />
        <Route path="task-status/*" element={<TaskStatusRoutes />} />
        <Route path="task/*" element={<TaskRoutes />} />
        <Route path="comment/*" element={<CommentRoutes />} />
        <Route path="work-group-membership/*" element={<WorkGroupMembershipRoutes />} />
        {/* jhipster-needle-add-route-path - JHipster will add routes here */}
      </ErrorBoundaryRoutes>
    </div>
  );
};
