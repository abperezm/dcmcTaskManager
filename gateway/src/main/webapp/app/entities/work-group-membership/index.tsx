import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import WorkGroupMembership from './work-group-membership';
import WorkGroupMembershipDetail from './work-group-membership-detail';
//import WorkGroupMembershipUpdate from './work-group-membership-update';
import WorkGroupMembershipDeleteDialog from './work-group-membership-delete-dialog';

const WorkGroupMembershipRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<WorkGroupMembership />} />
    {/*<Route path="new" element={<WorkGroupMembershipUpdate />} />*/}
    <Route path=":id">
      <Route index element={<WorkGroupMembershipDetail />} />
      {/*<Route path="edit" element={<WorkGroupMembershipUpdate />} />*/}
      <Route path="delete" element={<WorkGroupMembershipDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default WorkGroupMembershipRoutes;
