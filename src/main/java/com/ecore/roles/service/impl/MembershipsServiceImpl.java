package com.ecore.roles.service.impl;

import com.ecore.roles.client.model.Team;
import com.ecore.roles.client.model.User;
import com.ecore.roles.exception.InvalidArgumentException;
import com.ecore.roles.exception.ResourceExistsException;
import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.service.model.Membership;
import com.ecore.roles.service.model.Role;
import com.ecore.roles.repository.MembershipRepository;
import com.ecore.roles.service.MembershipsService;
import com.ecore.roles.service.RolesService;
import com.ecore.roles.service.TeamsService;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Log4j2
@Service
public class MembershipsServiceImpl implements MembershipsService {

    private final MembershipRepository membershipRepository;
    private final RolesService rolesService;

    private final TeamsService teamsService;

    @Autowired
    public MembershipsServiceImpl(
            MembershipRepository membershipRepository,
            RolesService rolesService,
            TeamsService teamsService) {
        this.membershipRepository = membershipRepository;
        this.rolesService = rolesService;
        this.teamsService = teamsService;
    }

    @Override
    public Membership createMembership(@NonNull Membership m) {

        UUID roleId = ofNullable(m.getRole()).map(Role::getId)
                .orElseThrow(() -> new InvalidArgumentException(Role.class));

        rolesService.getRole(roleId);

        UUID userId = ofNullable(m.getUserId())
                .orElseThrow(() -> new InvalidArgumentException(User.class));

        UUID teamId = ofNullable(m.getTeamId())
                .orElseThrow(() -> new InvalidArgumentException(Team.class));

        Team team = ofNullable(teamsService.getTeam(teamId))
                .orElseThrow(() -> new ResourceNotFoundException(Team.class, teamId));

        if (!userBelongsToTeam(userId, team)) {
            throw new InvalidArgumentException(Membership.class,
                    "The provided user doesn't belong to the provided team.");
        }

        if (membershipRepository.findByUserIdAndTeamId(m.getUserId(), m.getTeamId())
                .isPresent()) {
            throw new ResourceExistsException(Membership.class);
        }

        return membershipRepository.save(m);
    }

    private boolean userBelongsToTeam(UUID userId, Team team) {

        boolean userIsTeamLead = userId.equals(team.getTeamLeadId());

        boolean userBelongsToMembers = team.getTeamMemberIds()
                .stream()
                .anyMatch(userId::equals);

        return userIsTeamLead || userBelongsToMembers;
    }

    @Override
    public List<Membership> getMemberships(@NonNull UUID rid) {
        return membershipRepository.findByRoleId(rid);
    }
}
