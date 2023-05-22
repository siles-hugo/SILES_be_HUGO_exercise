package com.ecore.roles.web;

import com.ecore.roles.web.dto.MembershipDto;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public interface MembershipsApi {

    ResponseEntity<MembershipDto> createMembershipWithAssignedRole(
            MembershipDto membership);

    ResponseEntity<List<MembershipDto>> getMemberships(
            UUID roleId);

}
