import React from 'react';
// eslint-disable-line

import { useAppSelector } from 'app/config/store';
import MenuItem from 'app/shared/layout/menus/menu-item'; // eslint-disable-line

const EntitiesMenu = () => {
  const authorities = useAppSelector(state => state.authentication.account.authorities);
  const isAdmin = authorities?.includes('ROLE_ADMIN');
  return (
    <>
      {/* prettier-ignore */}
      <MenuItem icon="asterisk" to="/work-group">
        Grupos de Trabajo
      </MenuItem>

      {isAdmin && (
        <MenuItem icon="asterisk" to="/task-priority">
          Prioridad de Tarea
        </MenuItem>
      )}

      {/* jhipster-needle-add-entity-to-menu - JHipster will add entities to the menu here */}
    </>
  );
};

export default EntitiesMenu;
