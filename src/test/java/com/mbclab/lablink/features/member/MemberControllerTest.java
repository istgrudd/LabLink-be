package com.mbclab.lablink.features.member;

import com.mbclab.lablink.features.member.dto.CreateMemberRequest;
import com.mbclab.lablink.features.member.dto.MemberResponse;
import com.mbclab.lablink.features.member.dto.UpdateMemberRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Test untuk Member Service
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MemberControllerTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    private static String createdMemberId;

    @Test
    @Order(1)
    @DisplayName("Create Member")
    void createMember_shouldSucceed() {
        CreateMemberRequest request = new CreateMemberRequest();
        request.setNim("MEMBERTEST001");
        request.setFullName("Integration Test Member");
        request.setExpertDivision("BIG_DATA");
        request.setDepartment("Internal");

        MemberResponse response = memberService.createResearchAssistant(request);

        assertNotNull(response);
        assertEquals("MEMBERTEST001", response.getUsername());
        assertEquals("Integration Test Member", response.getFullName());
        assertFalse(response.isPasswordChanged()); // Default should be false

        createdMemberId = response.getId();
        System.out.println("✅ Created member ID: " + createdMemberId);
    }

    @Test
    @Order(2)
    @DisplayName("Get All Members")
    void getAllMembers_shouldReturnList() {
        List<MemberResponse> members = memberService.getAllMembersUnpaginated();

        assertNotNull(members);
        assertTrue(members.size() >= 1);
        System.out.println("✅ Found " + members.size() + " members");
    }

    @Test
    @Order(3)
    @DisplayName("Update Member")
    void updateMember_shouldSucceed() {
        if (createdMemberId == null) return;

        UpdateMemberRequest request = new UpdateMemberRequest();
        request.setFullName("Updated Test Member");
        request.setEmail("test@example.com");

        MemberResponse response = memberService.updateMember(createdMemberId, request);

        assertEquals("Updated Test Member", response.getFullName());
        assertEquals("test@example.com", response.getEmail());
        System.out.println("✅ Update member successful");
    }

    @Test
    @Order(4)
    @DisplayName("Delete Member")
    void deleteMember_shouldSucceed() {
        if (createdMemberId == null) return;

        memberService.deleteMember(createdMemberId);

        assertFalse(memberRepository.existsById(createdMemberId));
        System.out.println("✅ Delete member successful");
        createdMemberId = null;
    }

    @AfterAll
    static void cleanup(@Autowired MemberRepository memberRepository) {
        if (createdMemberId != null) {
            memberRepository.deleteById(createdMemberId);
        }
    }
}
