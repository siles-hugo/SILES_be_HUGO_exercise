package com.ecore.roles.service;

import com.ecore.roles.service.model.Role;

import java.util.List;
import java.util.UUID;

public interface RolesService {

    Role createRole(Role role);

    Role getRole(UUID id);

    Role getRole(UUID teamId, UUID userId);

    List<Role> getRoles();

}
