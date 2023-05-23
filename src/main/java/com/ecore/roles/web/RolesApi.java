package com.ecore.roles.web;

import com.ecore.roles.web.dto.RoleDto;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface RolesApi {

    ResponseEntity<RoleDto> createRole(
            RoleDto role);

    ResponseEntity<RoleDto> getRole(
            UUID roleId);

    ResponseEntity<RoleDto> getRole(
            UUID teamId,
            UUID userId);

    ResponseEntity<List<RoleDto>> getRoles();
}
