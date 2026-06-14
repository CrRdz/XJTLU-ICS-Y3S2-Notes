package com.cpt202.repository;

import com.cpt202.model.entity.Project;
import com.cpt202.repository.specification.ProjectSpecifications;
import com.cpt202.vo.ProjectVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    List<Project> findByTeacher_TeacherIdOrderByCreatedAtDesc(Long teacherId);

    long countByTeacher_TeacherId(Long teacherId);

    List<Project> findByTeacher_TeacherIdAndProjectStatusOrderByCreatedAtDesc(
            Long teacherId, Project.ProjectStatus status);

    default Page<Project> findStudentProjects(String keyword,
                                              Long categoryId,
                                              Project.ProjectStatus status,
                                              List<Long> tagIds,
                                              Pageable pageable) {
        return findAll(ProjectSpecifications.studentQuery(keyword, categoryId, status, tagIds), pageable);
    }

    default List<Project> findStudentProjectCandidates(Long categoryId,
                                                       Project.ProjectStatus status,
                                                       List<Long> tagIds,
                                                       Sort sort) {
        return findAll(ProjectSpecifications.studentQuery(null, categoryId, status, tagIds), sort);
    }

    @Query("""
            select new com.cpt202.vo.ProjectVO(
                p.projectId,
                t.teacherId,
                tu.fullName,
                c.categoryId,
                c.categoryName,
                p.title,
                p.description,
                p.requiredSkills,
                p.topicArea,
                p.maxStudents,
                p.currentAgreedCount,
                p.projectStatus,
                p.publishDate,
                p.closeDate
            )
            from Project p
            left join p.teacher t
            left join t.user tu
            left join p.category c
            order by p.createdAt desc
            """)
    List<ProjectVO> findAllProjectVos();

    @Query(value = """
            select new com.cpt202.vo.ProjectVO(
                p.projectId,
                t.teacherId,
                tu.fullName,
                c.categoryId,
                c.categoryName,
                p.title,
                p.description,
                p.requiredSkills,
                p.topicArea,
                p.maxStudents,
                p.currentAgreedCount,
                p.projectStatus,
                p.publishDate,
                p.closeDate
            )
            from Project p
            left join p.teacher t
            left join t.user tu
            left join p.category c
            order by p.createdAt desc
            """,
            countQuery = "select count(p) from Project p")
    Page<ProjectVO> findAllProjectVos(Pageable pageable);

        @Query(value = """
                        select new com.cpt202.vo.ProjectVO(
                                p.projectId,
                                t.teacherId,
                                tu.fullName,
                                c.categoryId,
                                c.categoryName,
                                p.title,
                                p.description,
                                p.requiredSkills,
                                p.topicArea,
                                p.maxStudents,
                                p.currentAgreedCount,
                                p.projectStatus,
                                p.publishDate,
                                p.closeDate
                        )
                        from Project p
                        left join p.teacher t
                        left join t.user tu
                        left join p.category c
                        where t.teacherId = :teacherId
                            and (:status is null or p.projectStatus = :status)
                        order by p.createdAt desc
                        """,
                        countQuery = """
                        select count(p)
                        from Project p
                        where p.teacher.teacherId = :teacherId
                            and (:status is null or p.projectStatus = :status)
                        """)
        Page<ProjectVO> findTeacherProjectVos(@Param("teacherId") Long teacherId,
                                                                                    @Param("status") Project.ProjectStatus status,
                                                                                    Pageable pageable);

            @Query("""
                select coalesce(sum(p.maxStudents), 0)
                from Project p
                where p.teacher.teacherId = :teacherId
                """)
            Long sumTeacherCapacity(@Param("teacherId") Long teacherId);

            @Query("""
                select coalesce(sum(p.currentAgreedCount), 0)
                from Project p
                where p.teacher.teacherId = :teacherId
                """)
            Long sumTeacherFilledSlots(@Param("teacherId") Long teacherId);

            @Query("""
                select p.projectStatus, count(p)
                from Project p
                where p.teacher.teacherId = :teacherId
                group by p.projectStatus
                """)
            List<Object[]> countTeacherProjectsByStatus(@Param("teacherId") Long teacherId);

            @Query("""
                select p.title, coalesce(p.currentAgreedCount, 0), coalesce(p.maxStudents, 0)
                from Project p
                where p.teacher.teacherId = :teacherId
                order by case
                when coalesce(p.maxStudents, 0) = 0 then 0.0
                else (1.0 * coalesce(p.currentAgreedCount, 0) / p.maxStudents)
                end desc,
                p.title asc
                """)
            List<Object[]> findTeacherProjectFillRates(@Param("teacherId") Long teacherId, Pageable pageable);

            @Query("""
                select coalesce(sum(p.maxStudents), 0)
                from Project p
                """)
            Long sumTotalCapacity();

            @Query("""
                select coalesce(sum(p.currentAgreedCount), 0)
                from Project p
                """)
            Long sumFilledSlots();

            @Query("""
                select p.projectStatus, count(p)
                from Project p
                group by p.projectStatus
                """)
            List<Object[]> countProjectsByStatus();

            @Query("""
                select coalesce(c.categoryName, 'Uncategorized'), count(p)
                from Project p
                left join p.category c
                group by c.categoryName
                order by count(p) desc
                """)
            List<Object[]> countProjectsByCategory();

            @Query("""
                select concat(
                    coalesce(tu.fullName, 'Unknown'),
                    ' (',
                    coalesce(t.staffNo, coalesce(tu.username, 'Unknown')),
                    ')'
                ), count(p)
                from Project p
                left join p.teacher t
                left join t.user tu
                group by tu.fullName, t.staffNo, tu.username
                order by count(p) desc
                """)
            List<Object[]> countProjectsByTeacher(Pageable pageable);

            @Query("""
                select p.title, coalesce(p.currentAgreedCount, 0), coalesce(p.maxStudents, 0)
                from Project p
                order by case
                when coalesce(p.maxStudents, 0) = 0 then 0.0
                else (1.0 * coalesce(p.currentAgreedCount, 0) / p.maxStudents)
                end desc,
                p.title asc
                """)
            List<Object[]> findProjectFillRates(Pageable pageable);
}
