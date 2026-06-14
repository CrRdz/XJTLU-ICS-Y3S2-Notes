package com.cpt202.dto;

import com.cpt202.model.entity.ProjectRequest;
import lombok.Getter;
import lombok.Setter;

/**
 * 教师端申请查询参数。
 * <p>
 * 用于封装教师查看待审核申请或指定状态申请时的筛选条件。
 */
@Getter
@Setter
public class TeacherProjectRequestQueryDTO extends PageQueryDTO {
    /** 申请状态筛选条件，可选。 */
    private ProjectRequest.RequestStatus status;

    /** 是否只返回非待处理历史记录。 */
    private Boolean historyOnly = false;
}
