```
.
├── API_DOCUMENTATION.md
├── Dockerfile
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com
│   │   │       └── cpt202
│   │   │           ├── controller
│   │   │           │   ├── admin
│   │   │           │   │   ├── AdminCategoryController.java
│   │   │           │   │   ├── AdminRecordController.java
│   │   │           │   │   ├── AdminTagController.java
│   │   │           │   │   └── AdminUserController.java
│   │   │           │   ├── common
│   │   │           │   │   └── CommonAuthController.java
│   │   │           │   ├── student
│   │   │           │   │   ├── StudentProfileController.java
│   │   │           │   │   ├── StudentProjectController.java
│   │   │           │   │   ├── StudentProjectRequestController.java
│   │   │           │   │   └── StudentRequestHistoryController.java
│   │   │           │   └── teacher
│   │   │           │       ├── TeacherProfileController.java
│   │   │           │       ├── TeacherProjectController.java
│   │   │           │       ├── TeacherProjectRequestController.java
│   │   │           │       └── TeacherProjectTagController.java
│   │   │           ├── CPT202Application.java
│   │   │           ├── dto
│   │   │           │   ├── AdminRequestRecordQueryDTO.java
│   │   │           │   ├── AdminUserQueryDTO.java
│   │   │           │   ├── CategoryDTO.java
│   │   │           │   ├── LoginDTO.java
│   │   │           │   ├── ProjectDTO.java
│   │   │           │   ├── ProjectRequestCreateDTO.java
│   │   │           │   ├── ProjectRequestReviewDTO.java
│   │   │           │   ├── ProjectStatusUpdateDTO.java
│   │   │           │   ├── ProjectTagBindDTO.java
│   │   │           │   ├── RegisterUserDTO.java
│   │   │           │   ├── StudentProfileUpdateDTO.java
│   │   │           │   ├── StudentProjectQueryDTO.java
│   │   │           │   ├── StudentProjectRequestQueryDTO.java
│   │   │           │   ├── TagDTO.java
│   │   │           │   ├── TeacherProfileUpdateDTO.java
│   │   │           │   ├── TeacherProjectQueryDTO.java
│   │   │           │   └── TeacherProjectRequestQueryDTO.java
│   │   │           ├── exception
│   │   │           │   ├── BusinessException.java
│   │   │           │   └── RuleViolationException.java
│   │   │           ├── handler
│   │   │           │   └── GlobalExceptionHandler.java
│   │   │           ├── model
│   │   │           │   └── entity
│   │   │           │       ├── Category.java
│   │   │           │       ├── Project.java
│   │   │           │       ├── ProjectRequest.java
│   │   │           │       ├── ProjectStatusHistory.java
│   │   │           │       ├── ProjectTag.java
│   │   │           │       ├── ProjectTagId.java
│   │   │           │       ├── RequestStatusHistory.java
│   │   │           │       ├── StudentProfile.java
│   │   │           │       ├── Tag.java
│   │   │           │       ├── TeacherProfile.java
│   │   │           │       └── User.java
│   │   │           ├── repository
│   │   │           │   ├── CategoryRepository.java
│   │   │           │   ├── ProjectRepository.java
│   │   │           │   ├── ProjectRequestRepository.java
│   │   │           │   ├── ProjectStatusHistoryRepository.java
│   │   │           │   ├── RequestStatusHistoryRepository.java
│   │   │           │   ├── StudentProfileRepository.java
│   │   │           │   ├── TagRepository.java
│   │   │           │   ├── TeacherProfileRepository.java
│   │   │           │   └── UserRepository.java
│   │   │           ├── result
│   │   │           │   └── Result.java
│   │   │           ├── service
│   │   │           │   ├── AuthService.java
│   │   │           │   ├── CallbackAuthService.java
│   │   │           │   ├── CategoryService.java
│   │   │           │   ├── HistoryService.java
│   │   │           │   ├── impl
│   │   │           │   │   ├── AuthServiceImpl.java
│   │   │           │   │   ├── CategoryServiceImpl.java
│   │   │           │   │   ├── HistoryServiceImpl.java
│   │   │           │   │   ├── Module8ServiceImpl.java
│   │   │           │   │   ├── ProfileServiceImpl.java
│   │   │           │   │   ├── ProjectRequestServiceImpl.java
│   │   │           │   │   ├── ProjectServiceImpl.java
│   │   │           │   │   ├── ProjectTagServiceImpl.java
│   │   │           │   │   ├── RecordServiceImpl.java
│   │   │           │   │   ├── TagServiceImpl.java
│   │   │           │   │   └── UserAdminServiceImpl.java
│   │   │           │   ├── Module8Service.java
│   │   │           │   ├── ProfileService.java
│   │   │           │   ├── ProjectRequestService.java
│   │   │           │   ├── ProjectService.java
│   │   │           │   ├── ProjectTagService.java
│   │   │           │   ├── RecordService.java
│   │   │           │   ├── TagService.java
│   │   │           │   └── UserAdminService.java
│   │   │           └── vo
│   │   │               ├── CategoryVO.java
│   │   │               ├── LoginVO.java
│   │   │               ├── ProjectRequestVO.java
│   │   │               ├── ProjectTagVO.java
│   │   │               ├── ProjectVO.java
│   │   │               ├── RequestStatusHistoryVO.java
│   │   │               ├── StudentProfileVO.java
│   │   │               ├── TagVO.java
│   │   │               ├── TeacherProfileVO.java
│   │   │               └── UserVO.java
│   │   └── resources
│   │       ├── application.properties
│   │       └── firstPage_front
│   │           ├── backimg2.png
│   │           ├── backimg4.png
│   │           ├── backimg5.png
│   │           ├── forgot-password.html
│   │           ├── index.html
│   │           ├── login.html
│   │           └── register.html
│   └── test
│       └── java
│           └── com
│               └── cpt202
│                   └── CPT202ApplicationTests.java
└── target
    ├── classes
    │   ├── application.properties
    │   ├── com
    │   │   └── cpt202
    │   │       ├── controller
    │   │       │   ├── admin
    │   │       │   │   ├── AdminCategoryController.class
    │   │       │   │   ├── AdminRecordController.class
    │   │       │   │   ├── AdminTagController.class
    │   │       │   │   └── AdminUserController.class
    │   │       │   ├── common
    │   │       │   │   └── CommonAuthController.class
    │   │       │   ├── student
    │   │       │   │   ├── StudentProfileController.class
    │   │       │   │   ├── StudentProjectController.class
    │   │       │   │   ├── StudentProjectRequestController.class
    │   │       │   │   └── StudentRequestHistoryController.class
    │   │       │   └── teacher
    │   │       │       ├── TeacherProfileController.class
    │   │       │       ├── TeacherProjectController.class
    │   │       │       ├── TeacherProjectRequestController.class
    │   │       │       └── TeacherProjectTagController.class
    │   │       ├── CPT202Application.class
    │   │       ├── dto
    │   │       │   ├── AdminRequestRecordQueryDTO.class
    │   │       │   ├── AdminUserQueryDTO.class
    │   │       │   ├── CategoryDTO.class
    │   │       │   ├── LoginDTO.class
    │   │       │   ├── ProjectDTO.class
    │   │       │   ├── ProjectRequestCreateDTO.class
    │   │       │   ├── ProjectRequestReviewDTO.class
    │   │       │   ├── ProjectStatusUpdateDTO.class
    │   │       │   ├── ProjectTagBindDTO.class
    │   │       │   ├── RegisterUserDTO.class
    │   │       │   ├── StudentProfileUpdateDTO.class
    │   │       │   ├── StudentProjectQueryDTO.class
    │   │       │   ├── StudentProjectRequestQueryDTO.class
    │   │       │   ├── TagDTO.class
    │   │       │   ├── TeacherProfileUpdateDTO.class
    │   │       │   ├── TeacherProjectQueryDTO.class
    │   │       │   └── TeacherProjectRequestQueryDTO.class
    │   │       ├── exception
    │   │       │   ├── BusinessException.class
    │   │       │   └── RuleViolationException.class
    │   │       ├── handler
    │   │       │   └── GlobalExceptionHandler.class
    │   │       ├── model
    │   │       │   └── entity
    │   │       │       ├── Category.class
    │   │       │       ├── Category$CategoryBuilder.class
    │   │       │       ├── Project.class
    │   │       │       ├── Project$ProjectBuilder.class
    │   │       │       ├── Project$ProjectStatus.class
    │   │       │       ├── ProjectRequest.class
    │   │       │       ├── ProjectRequest$ProjectRequestBuilder.class
    │   │       │       ├── ProjectRequest$RequestStatus.class
    │   │       │       ├── ProjectStatusHistory.class
    │   │       │       ├── ProjectStatusHistory$ProjectStatusHistoryBuilder.class
    │   │       │       ├── ProjectTag.class
    │   │       │       ├── ProjectTag$ProjectTagBuilder.class
    │   │       │       ├── ProjectTagId.class
    │   │       │       ├── ProjectTagId$ProjectTagIdBuilder.class
    │   │       │       ├── RequestStatusHistory.class
    │   │       │       ├── RequestStatusHistory$RequestStatusHistoryBuilder.class
    │   │       │       ├── StudentProfile.class
    │   │       │       ├── StudentProfile$StudentProfileBuilder.class
    │   │       │       ├── Tag.class
    │   │       │       ├── Tag$TagBuilder.class
    │   │       │       ├── TeacherProfile.class
    │   │       │       ├── TeacherProfile$TeacherProfileBuilder.class
    │   │       │       ├── User.class
    │   │       │       ├── User$UserBuilder.class
    │   │       │       └── User$UserRole.class
    │   │       ├── repository
    │   │       │   ├── CategoryRepository.class
    │   │       │   ├── ProjectRepository.class
    │   │       │   ├── ProjectRequestRepository.class
    │   │       │   ├── ProjectStatusHistoryRepository.class
    │   │       │   ├── RequestStatusHistoryRepository.class
    │   │       │   ├── StudentProfileRepository.class
    │   │       │   ├── TagRepository.class
    │   │       │   ├── TeacherProfileRepository.class
    │   │       │   └── UserRepository.class
    │   │       ├── result
    │   │       │   └── Result.class
    │   │       ├── service
    │   │       │   ├── AuthService.class
    │   │       │   ├── CategoryService.class
    │   │       │   ├── HistoryService.class
    │   │       │   ├── impl
    │   │       │   │   ├── AuthServiceImpl.class
    │   │       │   │   ├── CategoryServiceImpl.class
    │   │       │   │   ├── HistoryServiceImpl.class
    │   │       │   │   ├── Module8ServiceImpl.class
    │   │       │   │   ├── ProfileServiceImpl.class
    │   │       │   │   ├── ProjectRequestServiceImpl.class
    │   │       │   │   ├── ProjectServiceImpl.class
    │   │       │   │   ├── ProjectTagServiceImpl.class
    │   │       │   │   ├── RecordServiceImpl.class
    │   │       │   │   ├── TagServiceImpl.class
    │   │       │   │   └── UserAdminServiceImpl.class
    │   │       │   ├── Module8Service.class
    │   │       │   ├── ProfileService.class
    │   │       │   ├── ProjectRequestService.class
    │   │       │   ├── ProjectService.class
    │   │       │   ├── ProjectTagService.class
    │   │       │   ├── RecordService.class
    │   │       │   ├── TagService.class
    │   │       │   └── UserAdminService.class
    │   │       └── vo
    │   │           ├── CategoryVO.class
    │   │           ├── CategoryVO$CategoryVOBuilder.class
    │   │           ├── LoginVO.class
    │   │           ├── LoginVO$LoginVOBuilder.class
    │   │           ├── ProjectRequestVO.class
    │   │           ├── ProjectRequestVO$ProjectRequestVOBuilder.class
    │   │           ├── ProjectTagVO.class
    │   │           ├── ProjectTagVO$ProjectTagVOBuilder.class
    │   │           ├── ProjectVO.class
    │   │           ├── ProjectVO$ProjectVOBuilder.class
    │   │           ├── RequestStatusHistoryVO.class
    │   │           ├── RequestStatusHistoryVO$RequestStatusHistoryVOBuilder.class
    │   │           ├── StudentProfileVO.class
    │   │           ├── StudentProfileVO$StudentProfileVOBuilder.class
    │   │           ├── TagVO.class
    │   │           ├── TagVO$TagVOBuilder.class
    │   │           ├── TeacherProfileVO.class
    │   │           ├── TeacherProfileVO$TeacherProfileVOBuilder.class
    │   │           ├── UserVO.class
    │   │           └── UserVO$UserVOBuilder.class
    │   └── firstPage_front
    │       ├── backimg2.png
    │       ├── backimg4.png
    │       ├── backimg5.png
    │       ├── forgot-password.html
    │       ├── index.html
    │       ├── login.html
    │       └── register.html
    ├── cpt202-program-0.0.1-SNAPSHOT.jar
    ├── cpt202-program-0.0.1-SNAPSHOT.jar.original
    ├── generated-sources
    │   └── annotations
    ├── generated-test-sources
    │   └── test-annotations
    ├── maven-archiver
    │   └── pom.properties
    ├── maven-status
    │   └── maven-compiler-plugin
    │       ├── compile
    │       │   └── default-compile
    │       │       ├── createdFiles.lst
    │       │       └── inputFiles.lst
    │       └── testCompile
    │           └── default-testCompile
    │               ├── createdFiles.lst
    │               └── inputFiles.lst
    ├── surefire-reports
    │   ├── com.cpt202.controller.HelloControllerTest.txt
    │   ├── com.cpt202.controller.ItemControllerTest.txt
    │   ├── com.cpt202.CPT202ApplicationTests.txt
    │   ├── com.cpt202.service.HelloServiceTest.txt
    │   ├── com.cpt202.service.ItemServiceTest.txt
    │   ├── TEST-com.cpt202.controller.HelloControllerTest.xml
    │   ├── TEST-com.cpt202.controller.ItemControllerTest.xml
    │   ├── TEST-com.cpt202.CPT202ApplicationTests.xml
    │   ├── TEST-com.cpt202.service.HelloServiceTest.xml
    │   └── TEST-com.cpt202.service.ItemServiceTest.xml
    └── test-classes
        └── com
            └── cpt202
                └── CPT202ApplicationTests.class

62 directories, 238 files
```