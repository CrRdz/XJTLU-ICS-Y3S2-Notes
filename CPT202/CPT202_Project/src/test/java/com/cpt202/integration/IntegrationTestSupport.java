package com.cpt202.integration;

import com.cpt202.model.entity.Category;
import com.cpt202.model.entity.Project;
import com.cpt202.model.entity.ProjectRequest;
import com.cpt202.model.entity.StudentProfile;
import com.cpt202.model.entity.Tag;
import com.cpt202.model.entity.TeacherProfile;
import com.cpt202.model.entity.User;
import com.cpt202.repository.CategoryRepository;
import com.cpt202.repository.ProjectRepository;
import com.cpt202.repository.ProjectRequestRepository;
import com.cpt202.repository.StudentProfileRepository;
import com.cpt202.repository.TagRepository;
import com.cpt202.repository.TeacherProfileRepository;
import com.cpt202.repository.UserRepository;
import com.cpt202.service.JwtTokenService;
import com.cpt202.service.ProjectRequestService;
import com.cpt202.service.ProjectTagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;

/**
 * Shared fixtures for controller integration tests.
 *
 * The helper methods intentionally create fully-linked domain objects instead of
 * mocking services so every test still exercises the real controller, service,
 * repository and transaction stack end to end.
 */
public abstract class IntegrationTestSupport {

    protected static final String DEFAULT_PASSWORD = "Password123";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected JwtTokenService jwtTokenService;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected TeacherProfileRepository teacherProfileRepository;

    @Autowired
    protected StudentProfileRepository studentProfileRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected TagRepository tagRepository;

    @Autowired
    protected ProjectRepository projectRepository;

    @Autowired
    protected ProjectRequestRepository projectRequestRepository;

    @Autowired
    protected ProjectRequestService projectRequestService;

    @Autowired
    protected ProjectTagService projectTagService;

    @Autowired(required = false)
    private InMemoryRedisCacheService redisCacheService;

    @BeforeEach
    void clearCachedAuthState() {
        if (redisCacheService != null) {
            redisCacheService.clear();
        }
    }

    /** Creates a user with the default test password. */
    protected User createUser(String username, String email, String fullName, User.UserRole role) {
        return createUser(username, DEFAULT_PASSWORD, email, fullName, role);
    }

    /** Creates a user with an explicit raw password. */
    protected User createUser(String username, String rawPassword, String email, String fullName, User.UserRole role) {
        LocalDateTime now = LocalDateTime.now();
        return userRepository.save(User.builder()
                .username(username)
                .passwordHash(hashPassword(rawPassword))
                .email(email)
                .fullName(fullName)
                .role(role)
                .accountStatus("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    /** Creates an administrator account for test setup. */
    protected User createAdminUser(String username, String email, String fullName) {
        return createUser(username, email, fullName, User.UserRole.ADMIN);
    }

    /** Creates a teacher profile linked to the supplied user. */
    protected TeacherProfile createTeacherProfile(User user, String staffNo) {
        return teacherProfileRepository.save(TeacherProfile.builder()
                .staffNo(staffNo)
                .department("Computer Science")
                .title("Professor")
                .researchArea("Distributed Systems")
                .office("Room 101")
                .updatedAt(LocalDateTime.now())
                .user(user)
                .build());
    }

    /** Creates a student profile linked to the supplied user. */
    protected StudentProfile createStudentProfile(User user, String studentNo) {
        return studentProfileRepository.save(StudentProfile.builder()
                .studentNo(studentNo)
                .programme("Software Engineering")
                .enrollmentDate(LocalDate.of(2024, 9, 1))
                .phone("123456789")
                .interests("AI")
                .updatedAt(LocalDateTime.now())
                .user(user)
                .build());
    }

    /** Creates a category with deterministic default metadata. */
    protected Category createCategory(String categoryName) {
        return categoryRepository.save(Category.builder()
                .categoryName(categoryName)
                .description(categoryName + " description")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    /** Creates a tag with deterministic default metadata. */
    protected Tag createTag(String tagName) {
        return tagRepository.save(Tag.builder()
                .tagName(tagName)
                .description(tagName + " description")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
    }

    /** Creates a published project for controller-level workflow tests. */
    protected Project createProject(TeacherProfile teacherProfile,
                                    Category category,
                                    String title,
                                    int maxStudents) {
        LocalDateTime now = LocalDateTime.now();
        return projectRepository.save(Project.builder()
                .teacher(teacherProfile)
                .category(category)
                .title(title)
                .description(title + " description")
                .requiredSkills("Java, Spring")
                .topicArea("Backend Systems")
                .maxStudents(maxStudents)
                .currentAgreedCount(0)
                .projectStatus(Project.ProjectStatus.AVAILABLE)
                .publishDate(now)
                .closeDate(now.plusDays(7))
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    /** Creates a pending request owned by the supplied student. */
    protected ProjectRequest createPendingRequest(Project project,
                                                  StudentProfile studentProfile,
                                                  int preferenceRank,
                                                  String notes) {
        LocalDateTime now = LocalDateTime.now();
        return projectRequestRepository.save(ProjectRequest.builder()
                .project(project)
                .student(studentProfile)
                .preferenceRank(preferenceRank)
                .notes(notes)
                .requestStatus(ProjectRequest.RequestStatus.PENDING)
                .submittedAt(now)
                .updatedAt(now)
                .build());
    }

    /** Builds a bearer token for the supplied user. */
    protected String authorizationFor(User user) {
        return "Bearer " + jwtTokenService.generateToken(user);
    }

    /** Reads a Long field from the wrapped data payload. */
    protected Long readDataLong(MvcResult result, String fieldName) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path(fieldName)
                .asLong();
    }

    /** Generates a unique suffix for test-only identifiers. */
    protected String uniqueSuffix() {
        return Long.toString(System.nanoTime());
    }

    /**
     * The integration tests create real users, so hashing raw passwords the same
     * way as production keeps future password-change tests honest.
     */
    protected String hashPassword(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to hash password for tests", ex);
        }
    }
}
