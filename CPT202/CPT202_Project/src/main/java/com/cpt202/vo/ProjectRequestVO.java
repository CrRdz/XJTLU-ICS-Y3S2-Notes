package com.cpt202.vo;

import com.cpt202.model.entity.ProjectRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 项目申请展示对象。
 * <p>
 * 用于向学生端、教师端和管理端返回申请信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequestVO {

    /** 申请主键。 */
    private Long requestId;
    /** 关联项目主键。 */
    private Long projectId;
    /** 项目标题。 */
    private String projectTitle;
    /** 学生主键。 */
    private Long studentId;
    /** 学生姓名。 */
    private String studentName;
    /** 学生学号。 */
    private String studentNo;
    /** 学生邮箱。 */
    private String studentEmail;
    /** 学生专业。 */
    private String studentProgramme;
    /** 学生联系电话。 */
    private String studentPhone;
    /** 学生兴趣方向。 */
    private String studentInterests;
    /** 审核教师主键。 */
    private Long reviewedByTeacherId;
    /** 志愿顺位。 */
    private Integer preferenceRank;
    /** 申请备注。 */
    private String notes;
    /** 当前申请状态。 */
    private ProjectRequest.RequestStatus requestStatus;
    /** 审核意见。 */
    private String decisionComment;
    /** 提交时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime submittedAt;
    /** 审核时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime reviewedAt;
    /** 撤回时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime withdrawnAt;
}
