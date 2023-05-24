package com.ecore.roles.service;

import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.repository.RoleRepository;
import com.ecore.roles.service.impl.RolesServiceImpl;
import com.ecore.roles.service.model.Membership;
import com.ecore.roles.service.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.ecore.roles.utils.TestData.*;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolesServiceTest {

    @InjectMocks
    private RolesServiceImpl rolesService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private MembershipsService membershipsService;

    @Mock
    private TeamsService teamsService;

    @Test
    void shouldCreateRole() {
        Role developerRole = DEVELOPER_ROLE();
        when(roleRepository.save(developerRole)).thenReturn(developerRole);

        Role role = rolesService.createRole(developerRole);

        assertNotNull(role);
        assertEquals(developerRole, role);
    }

    @Test
    void shouldFailToCreateRoleWhenRoleIsNull() {
        assertThrows(NullPointerException.class,
                () -> rolesService.createRole(null));
    }

    @Test
    void shouldReturnRoleWhenRoleIdExists() {
        Role developerRole = DEVELOPER_ROLE();
        when(roleRepository.findById(developerRole.getId())).thenReturn(Optional.of(developerRole));

        Role role = rolesService.getRole(developerRole.getId());

        assertNotNull(role);
        assertEquals(developerRole, role);
    }

    @Test
    void shouldFailToGetRoleWhenRoleIdDoesNotExist() {
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> rolesService.getRole(UUID_1));

        assertEquals(format("Role %s not found", UUID_1), exception.getMessage());
    }

    @Test
    void shouldReturnRoleByTeamIdAndUserId() {
        Role expectedRole = DEVELOPER_ROLE();
        Membership defaultMembership = DEFAULT_MEMBERSHIP();

        when(teamsService.getTeam(ORDINARY_CORAL_LYNX_TEAM_UUID))
                .thenReturn(ORDINARY_CORAL_LYNX_TEAM(true));
        when(membershipsService.getMembership(ORDINARY_CORAL_LYNX_TEAM_UUID, GIANNI_USER_UUID))
                .thenReturn(defaultMembership);
        when(roleRepository.findById(defaultMembership.getRole().getId()))
                .thenReturn(Optional.of(expectedRole));

        Role role = rolesService.getRole(ORDINARY_CORAL_LYNX_TEAM_UUID, GIANNI_USER_UUID);

        assertNotNull(role);
        assertEquals(expectedRole, role);
    }

    @Test
    void shouldFailToGetRoleByTeamIdAndUserIdIfTheTeamDoesNotExists() {
        when(teamsService.getTeam(ORDINARY_CORAL_LYNX_TEAM_UUID))
                .thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> rolesService.getRole(ORDINARY_CORAL_LYNX_TEAM_UUID, GIANNI_USER_UUID));

        assertEquals(format("Team %s not found", ORDINARY_CORAL_LYNX_TEAM_UUID),
                exception.getMessage());

        verify(teamsService, times(1)).getTeam(any());
        verify(membershipsService, times(0)).getMembership(any(), any());
        verify(roleRepository, times(0)).findAllById(any());
    }

    @Test
    void shouldFailToGetRoleByTeamIdAndUserIdIfTheMembershipDoesNotExists() {
        when(teamsService.getTeam(ORDINARY_CORAL_LYNX_TEAM_UUID))
                .thenReturn(ORDINARY_CORAL_LYNX_TEAM(true));
        when(membershipsService.getMembership(ORDINARY_CORAL_LYNX_TEAM_UUID, GIANNI_USER_UUID))
                .thenThrow(new ResourceNotFoundException(Membership.class, "Test error message"));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> rolesService.getRole(ORDINARY_CORAL_LYNX_TEAM_UUID, GIANNI_USER_UUID));

        assertEquals("Resource Membership not found. Test error message", exception.getMessage());

        verify(teamsService, times(1)).getTeam(any());
        verify(membershipsService, times(1)).getMembership(any(), any());
        verify(roleRepository, times(0)).findAllById(any());

    }
}
