package com.mbclab.lablink.features.project;

import com.mbclab.lablink.features.member.MemberRepository;
import com.mbclab.lablink.features.member.ResearchAssistant;
import com.mbclab.lablink.features.project.dto.CreateProjectRequest;
import com.mbclab.lablink.features.project.dto.ProjectResponse;
import com.mbclab.lablink.features.project.dto.UpdateProjectRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Test untuk Project Service
 * 
 * Test langsung ke Service layer (tidak perlu MockMvc)
 * Ini cara paling reliable untuk test di Spring Boot 4.x
 * 
 * Cara jalankan:
 * - Via terminal: .\mvnw.cmd test -Dtest=ProjectControllerTest
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProjectControllerTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private MemberRepository memberRepository;

    private static String createdProjectId;
    private static String testMemberId;

    @BeforeEach
    void setup() {
        if (testMemberId == null) {
            ResearchAssistant member = new ResearchAssistant();
            member.setUsername("PROJECTTEST001");
            member.setPassword("password");
            member.setFullName("Test Member for Project");
            member.setRole("ASSISTANT");
            member.setExpertDivision("CYBER_SECURITY");
            member.setDepartment("Internal");
            member = memberRepository.save(member);
            testMemberId = member.getId();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Create Project")
    void createProject_shouldSucceed() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Test Project Integration");
        request.setDescription("Test description");
        request.setDivision("CYBER_SECURITY");
        request.setActivityType("RISET");
        request.setLeaderId(testMemberId);
        request.setTeamMemberIds(new HashSet<>());
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now().plusMonths(6));

        ProjectResponse response = projectService.createProject(request);

        assertNotNull(response);
        assertEquals("RST-0001", response.getProjectCode());
        assertEquals("NOT_STARTED", response.getStatus());
        assertEquals("Test Project Integration", response.getName());

        createdProjectId = response.getId();
        System.out.println("✅ Created project ID: " + createdProjectId);
    }

    @Test
    @Order(2)
    @DisplayName("Get All Projects")
    void getAllProjects_shouldReturnList() {
        List<ProjectResponse> projects = projectService.getAllProjects();

        assertNotNull(projects);
        assertTrue(projects.size() >= 1);
        System.out.println("✅ Found " + projects.size() + " projects");
    }

    @Test
    @Order(3)
    @DisplayName("Get Project By ID")
    void getProjectById_shouldReturnProject() {
        if (createdProjectId == null) return;

        ProjectResponse response = projectService.getProjectById(createdProjectId);

        assertNotNull(response);
        assertEquals(createdProjectId, response.getId());
        assertEquals("Test Project Integration", response.getName());
        System.out.println("✅ Get project by ID successful");
    }

    @Test
    @Order(4)
    @DisplayName("Update Project")
    void updateProject_shouldSucceed() {
        if (createdProjectId == null) return;

        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("Updated Project Name");
        request.setStatus("IN_PROGRESS");
        request.setProgressPercent(50);

        ProjectResponse response = projectService.updateProject(createdProjectId, request);

        assertEquals("Updated Project Name", response.getName());
        assertEquals("IN_PROGRESS", response.getStatus());
        assertEquals(50, response.getProgressPercent());
        System.out.println("✅ Update project successful");
    }

    @Test
    @Order(5)
    @DisplayName("Delete Project")
    void deleteProject_shouldSucceed() {
        if (createdProjectId == null) return;

        projectService.deleteProject(createdProjectId);

        // Verify deleted
        assertThrows(RuntimeException.class, () -> 
            projectService.getProjectById(createdProjectId)
        );
        System.out.println("✅ Delete project successful");
    }

    @AfterAll
    static void cleanup(@Autowired MemberRepository memberRepository) {
        if (testMemberId != null) {
            memberRepository.deleteById(testMemberId);
        }
    }
}
