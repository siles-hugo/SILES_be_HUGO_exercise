package com.ecore.roles.service;

import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.service.model.Membership;

import java.util.List;
import java.util.UUID;

public interface MembershipsService {

    Membership createMembership(Membership membership) throws ResourceNotFoundException;

    Membership getMembership(UUID teamId, UUID userId);

    List<Membership> getMemberships(UUID roleId);
}
