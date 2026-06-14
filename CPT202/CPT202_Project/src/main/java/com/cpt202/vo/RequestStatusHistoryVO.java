package com.cpt202.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 申请状态历史展示对象。
 * <p>
 * 用于返回申请状态变化过程中的关键记录。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestStatusHistoryVO {

    /** 历史记录主键。 */
    private Long historyId;
    /** 申请主键。 */
    private Long requestId;
    /** 修改前状态。 */
    private String oldStatus;
    /** 修改后状态。 */
    private String newStatus;
    /** 操作学生主键。 */
    private Long changedByStudentId;
    /** 操作学生姓名。 */
    private String changedByStudentName;
    /** 备注。 */
    private String remark;
    /** 变更时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime changedAt;
}
