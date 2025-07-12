import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import WorkGroup from './work-group';
import WorkGroupDetail from './work-group-detail';
import WorkGroupUpdate from './work-group-update';
import WorkGroupDeleteDialog from './work-group-delete-dialog';
import AllWorkGroups from './all-work-groups';

const WorkGroupRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<WorkGroup />} />
    <Route path="new" element={<WorkGroupUpdate />} />
    <Route path="all" element={<AllWorkGroups />} />
    {/* This route is for admins to see all work groups */}
    <Route path=":id">
      <Route index element={<WorkGroupDetail />} />
      <Route path="edit" element={<WorkGroupUpdate />} />
      <Route path="delete" element={<WorkGroupDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default WorkGroupRoutes;
