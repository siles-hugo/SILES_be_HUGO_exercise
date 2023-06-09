package com.ecore.roles.web.rest;

import com.ecore.roles.service.model.Membership;
import com.ecore.roles.service.MembershipsService;
import com.ecore.roles.web.MembershipsApi;
import com.ecore.roles.web.dto.MembershipDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.ecore.roles.web.dto.MembershipDto.fromModel;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/memberships")
public class MembershipsRestController implements MembershipsApi {

    private final MembershipsService membershipsService;

    @Override
    @PostMapping(
            consumes = {"application/json"},
            produces = {"application/json"})
    public ResponseEntity<MembershipDto> createMembership(
            @NotNull @Valid @RequestBody MembershipDto membershipDto) {
        Membership membership = membershipsService.createMembership(membershipDto.toModel());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(fromModel(membership));
    }

    @Override
    @GetMapping(
            path = "/role/{roleId}",
            produces = {"application/json"})
    public ResponseEntity<List<MembershipDto>> getMemberships(
            @PathVariable UUID roleId) {

        List<Membership> memberships = membershipsService.getMemberships(roleId);

        List<MembershipDto> newMembershipDto = new ArrayList<>();

        for (Membership membership : memberships) {
            MembershipDto membershipDto = fromModel(membership);
            newMembershipDto.add(membershipDto);
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(newMembershipDto);
    }

}
