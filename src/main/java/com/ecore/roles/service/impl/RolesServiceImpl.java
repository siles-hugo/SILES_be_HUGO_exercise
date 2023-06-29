package com.ecore.roles.service.impl;

import com.ecore.roles.client.model.Team;
import com.ecore.roles.exception.ResourceExistsException;
import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.repository.RoleRepository;
import com.ecore.roles.service.MembershipsService;
import com.ecore.roles.service.RolesService;
import com.ecore.roles.service.TeamsService;
import com.ecore.roles.service.model.Membership;
import com.ecore.roles.service.model.Role;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Log4j2
@Service
public class RolesServiceImpl implements RolesService {

    private final RoleRepository roleRepository;

    private final MembershipsService membershipsService;

    private final TeamsService teamsService;

    @Autowired
    public RolesServiceImpl(
            RoleRepository roleRepository,
            TeamsService teamsService,
            @Lazy MembershipsService membershipsService) {
        this.roleRepository = roleRepository;
        this.membershipsService = membershipsService;
        this.teamsService = teamsService;
    }

    @Override
    public Role createRole(@NonNull Role r) {
        if (roleRepository.findByName(r.getName()).isPresent()) {
            throw new ResourceExistsException(Role.class);
        }
        return roleRepository.save(r);
    }

    @Override
    public Role getRole(@NonNull UUID rid) {
        return roleRepository.findById(rid)
                .orElseThrow(() -> new ResourceNotFoundException(Role.class, rid));
    }

    @Override
    public Role getRole(@NonNull UUID teamId, @NonNull UUID userId) {

        Team team = ofNullable(teamsService.getTeam(teamId))
                .orElseThrow(() -> new ResourceNotFoundException(Team.class, teamId));

        Membership membership = membershipsService.getMembership(team.getId(), userId);

        return getRole(membership.getRole().getId());
    }

    @Override
    public List<Role> getRoles() {
        return roleRepository.findAll();
    }

}
