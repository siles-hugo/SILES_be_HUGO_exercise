package com.ecore.roles.api;

import com.ecore.roles.service.model.Membership;
import com.ecore.roles.service.model.Role;
import com.ecore.roles.repository.RoleRepository;
import com.ecore.roles.utils.RestAssuredHelper;
import com.ecore.roles.web.dto.RoleDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static com.ecore.roles.utils.MockUtils.mockGetTeamById;
import static com.ecore.roles.utils.RestAssuredHelper.*;
import static com.ecore.roles.utils.TestData.*;
import static io.restassured.RestAssured.when;
import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RolesApiTest {

    private final RestTemplate restTemplate;
    private final RoleRepository roleRepository;

    private MockRestServiceServer mockServer;

    @LocalServerPort
    private int port;

    @Autowired
    public RolesApiTest(RestTemplate restTemplate, RoleRepository roleRepository) {
        this.restTemplate = restTemplate;
        this.roleRepository = roleRepository;
    }

    @BeforeEach
    void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
        RestAssuredHelper.setUp(port);
        Optional<Role> devOpsRole = roleRepository.findByName(DEVOPS_ROLE().getName());
        devOpsRole.ifPresent(roleRepository::delete);
    }

    @Test
    void shouldFailWhenPathDoesNotExist() {
        sendRequest(when()
                .get("/v1/role")
                .then())
                    .validate(HttpStatus.NOT_FOUND.value(), "Not Found");
    }

    @Test
    void shouldCreateNewRole() {
        Role expectedRole = DEVOPS_ROLE();

        RoleDto actualRole = createRole(expectedRole)
                .statusCode(HttpStatus.CREATED.value())
                .extract().as(RoleDto.class);

        assertThat(actualRole.getName()).isEqualTo(expectedRole.getName());
    }

    @Test
    void shouldFailToCreateNewRoleWhenNull() {
        createRole(null)
                .validate(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase());
    }

    @Test
    void shouldFailToCreateNewRoleWhenMissingName() {
        createRole(Role.builder().build())
                .validate(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase());
    }

    @Test
    void shouldFailToCreateNewRoleWhenBlankName() {
        createRole(Role.builder().name("").build())
                .validate(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase());
    }

    @Test
    void shouldFailToCreateNewRoleWhenNameAlreadyExists() {
        createRole(DEVELOPER_ROLE())
                .validate(HttpStatus.BAD_REQUEST.value(), "Role already exists");
    }

    @Test
    void shouldGetAllRoles() {
        RoleDto[] roles = getRoles()
                .extract().as(RoleDto[].class);

        assertThat(roles).hasSizeGreaterThanOrEqualTo(3);
        assertThat(roles).contains(RoleDto.fromModel(DEVELOPER_ROLE()));
        assertThat(roles).contains(RoleDto.fromModel(PRODUCT_OWNER_ROLE()));
        assertThat(roles).contains(RoleDto.fromModel(TESTER_ROLE()));
    }

    @Test
    void shouldGetRoleById() {
        Role expectedRole = DEVELOPER_ROLE();

        getRole(expectedRole.getId())
                .statusCode(HttpStatus.OK.value())
                .body("name", equalTo(expectedRole.getName()));
    }

    @Test
    void shouldFailToGetRoleById() {
        getRole(UUID_1)
                .validate(HttpStatus.NOT_FOUND.value(), format("Role %s not found", UUID_1));
    }

    @Test
    void shouldGetRoleByUserIdAndTeamId() {
        Membership expectedMembership = DEFAULT_MEMBERSHIP();
        mockGetTeamById(mockServer, ORDINARY_CORAL_LYNX_TEAM_UUID, ORDINARY_CORAL_LYNX_TEAM());
        createMembership(expectedMembership)
                .statusCode(HttpStatus.CREATED.value());

        getRole(expectedMembership.getUserId(), expectedMembership.getTeamId())
                .statusCode(HttpStatus.OK.value())
                .body("name", equalTo(expectedMembership.getRole().getName()));
    }

    @Test
    void shouldFailToGetRoleByUserIdAndTeamIdWhenMissingUserId() {
        getRole(null, ORDINARY_CORAL_LYNX_TEAM_UUID)
                .validate(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase());
    }

    @Test
    void shouldFailToGetRoleByUserIdAndTeamIdWhenMissingTeamId() {
        getRole(GIANNI_USER_UUID, null)
                .validate(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase());
    }

    @Test
    void shouldFailToGetRoleByUserIdAndTeamIdWhenItDoesNotExist() {
        mockGetTeamById(mockServer, UUID_1, null);
        getRole(GIANNI_USER_UUID, UUID_1)
                .validate(HttpStatus.NOT_FOUND.value(), format("Team %s not found", UUID_1));
    }
}
