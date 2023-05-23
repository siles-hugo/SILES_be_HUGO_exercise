package com.ecore.roles.service;

import com.ecore.roles.exception.InvalidArgumentException;
import com.ecore.roles.exception.ResourceExistsException;
import com.ecore.roles.model.Membership;
import com.ecore.roles.repository.MembershipRepository;
import com.ecore.roles.service.impl.MembershipsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.ecore.roles.utils.TestData.DEFAULT_MEMBERSHIP;
import static com.ecore.roles.utils.TestData.DEVELOPER_ROLE;
import static com.ecore.roles.utils.TestData.INVALID_MEMBERSHIP;
import static com.ecore.roles.utils.TestData.ORDINARY_CORAL_LYNX_TEAM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MembershipsServiceTest {

    @InjectMocks
    private MembershipsServiceImpl membershipsService;
    @Mock
    private MembershipRepository membershipRepository;
    @Mock
    private RolesService rolesService;
    @Mock
    private UsersService usersService;
    @Mock
    private TeamsService teamsService;

    @Test
    void shouldCreateMembership() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        when(rolesService.getRole(expectedMembership.getRole().getId()))
                .thenReturn(DEVELOPER_ROLE());
        when(teamsService.getTeam(expectedMembership.getTeamId()))
                .thenReturn(ORDINARY_CORAL_LYNX_TEAM(true));
        when(membershipRepository.findByUserIdAndTeamId(expectedMembership.getUserId(),
                expectedMembership.getTeamId()))
                        .thenReturn(Optional.empty());
        when(membershipRepository
                .save(expectedMembership))
                        .thenReturn(expectedMembership);

        Membership actualMembership = membershipsService.createMembership(expectedMembership);

        assertNotNull(actualMembership);
        assertEquals(actualMembership, expectedMembership);
        verify(rolesService).getRole(expectedMembership.getRole().getId());
    }

    @Test
    void shouldFailToCreateMembershipWhenMembershipsIsNull() {
        assertThrows(NullPointerException.class,
                () -> membershipsService.createMembership(null));
    }

    @Test
    void shouldFailToCreateMembershipWhenItExists() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        when(rolesService.getRole(expectedMembership.getRole().getId()))
                .thenReturn(DEVELOPER_ROLE());
        when(teamsService.getTeam(expectedMembership.getTeamId()))
                .thenReturn(ORDINARY_CORAL_LYNX_TEAM(true));
        when(membershipRepository.findByUserIdAndTeamId(expectedMembership.getUserId(),
                expectedMembership.getTeamId()))
                        .thenReturn(Optional.of(expectedMembership));

        ResourceExistsException exception = assertThrows(ResourceExistsException.class,
                () -> membershipsService.createMembership(expectedMembership));

        assertEquals("Membership already exists", exception.getMessage());
        verify(teamsService, times(1)).getTeam(any());
        verify(rolesService, times(1)).getRole(any());
        verify(usersService, times(0)).getUser(any());
    }

    @Test
    void shouldFailToCreateMembershipWhenItHasInvalidRole() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        expectedMembership.setRole(null);

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> membershipsService.createMembership(expectedMembership));

        assertEquals("Invalid 'Role' object", exception.getMessage());
        verify(membershipRepository, times(0)).findByUserIdAndTeamId(any(), any());
        verify(rolesService, times(0)).getRole(any());
        verify(usersService, times(0)).getUser(any());
        verify(teamsService, times(0)).getTeam(any());
    }

    @Test
    void shouldFailToCreateMembershipWhenItHasInvalidTeamId() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        expectedMembership.setTeamId(null);

        when(rolesService.getRole(expectedMembership.getRole().getId()))
                .thenReturn(DEVELOPER_ROLE());

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> membershipsService.createMembership(expectedMembership));

        assertEquals("Invalid 'Team' object", exception.getMessage());
        verify(rolesService, times(1)).getRole(any());
        verify(membershipRepository, times(0)).findByUserIdAndTeamId(any(), any());
        verify(usersService, times(0)).getUser(any());
        verify(teamsService, times(0)).getTeam(any());
    }

    @Test
    void shouldFailToCreateMembershipWhenItHasInvalidUserId() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        expectedMembership.setUserId(null);

        when(rolesService.getRole(expectedMembership.getRole().getId()))
                .thenReturn(DEVELOPER_ROLE());

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> membershipsService.createMembership(expectedMembership));

        assertEquals("Invalid 'User' object", exception.getMessage());
        verify(rolesService, times(1)).getRole(any());
        verify(membershipRepository, times(0)).findByUserIdAndTeamId(any(), any());
        verify(usersService, times(0)).getUser(any());
        verify(teamsService, times(0)).getTeam(any());
    }

    @Test
    void shouldFailToCreateMembershipWhenUserDoesNotBelongToTeam() {
        Membership expectedMembership = INVALID_MEMBERSHIP();

        when(rolesService.getRole(expectedMembership.getRole().getId()))
                .thenReturn(DEVELOPER_ROLE());

        when(teamsService.getTeam(expectedMembership.getTeamId()))
                .thenReturn(ORDINARY_CORAL_LYNX_TEAM(true));

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class,
                () -> membershipsService.createMembership(expectedMembership));

        assertEquals("Invalid 'Membership' object. The provided user doesn't belong to the provided team.",
                exception.getMessage());
        verify(rolesService, times(1)).getRole(any());
        verify(membershipRepository, times(0)).findByUserIdAndTeamId(any(), any());
        verify(usersService, times(0)).getUser(any());
    }

    @Test
    void shouldFailToGetMembershipsWhenRoleIdIsNull() {
        assertThrows(NullPointerException.class,
                () -> membershipsService.getMemberships(null));
    }

}
