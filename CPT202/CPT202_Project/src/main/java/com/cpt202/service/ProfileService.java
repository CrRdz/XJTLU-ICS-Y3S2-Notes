package com.cpt202.service;

import com.cpt202.dto.AdminProfileUpdateDTO;
import com.cpt202.dto.ChangePasswordDTO;
import com.cpt202.dto.StudentProfileUpdateDTO;
import com.cpt202.dto.TeacherProfileUpdateDTO;
import com.cpt202.dto.TwoFactorDisableDTO;
import com.cpt202.dto.TwoFactorEnableDTO;
import com.cpt202.vo.AdminProfileVO;
import com.cpt202.vo.StudentProfileVO;
import com.cpt202.vo.TeacherProfileVO;
import com.cpt202.vo.TwoFactorSetupVO;

/**
 * 用户资料服务接口。
 * <p>
 * 该接口统一封装学生端与教师端资料的查询与维护操作。
 */
public interface ProfileService {

    /**
     * 查询学生资料。
     *
     * @param studentId 学生主键
     * @return 学生资料展示对象
     */
    StudentProfileVO getStudentProfile(Long studentId);

    /**
     * 修改学生资料。
     *
     * @param studentId 学生主键
     * @param studentProfileUpdateDTO 学生资料更新参数
     */
    void updateStudentProfile(Long studentId, StudentProfileUpdateDTO studentProfileUpdateDTO);

    /**
     * 查询教师资料。
     *
     * @param teacherId 教师主键
     * @return 教师资料展示对象
     */
    TeacherProfileVO getTeacherProfile(Long teacherId);

    /**
     * 修改教师资料。
     *
     * @param teacherId 教师主键
     * @param teacherProfileUpdateDTO 教师资料更新参数
     */
    void updateTeacherProfile(Long teacherId, TeacherProfileUpdateDTO teacherProfileUpdateDTO);

    /**
     * 查询管理员资料。
     *
     * @param userId 管理员用户主键
     * @return 管理员资料
     */
    AdminProfileVO getAdminProfile(Long userId);

    /**
     * 修改管理员资料。
     *
     * @param userId 管理员用户主键
     * @param adminProfileUpdateDTO 管理员资料更新参数
     */
    void updateAdminProfile(Long userId, AdminProfileUpdateDTO adminProfileUpdateDTO);

    /**
     * 修改当前用户密码。
     * 需要验证旧密码正确后方可更新。
     *
     * @param userId            当前用户主键
     * @param changePasswordDTO 修改密码参数（旧密码 + 新密码）
     */
    void changePassword(Long userId, ChangePasswordDTO changePasswordDTO);

    TwoFactorSetupVO initializeTwoFactorSetup(Long userId);

    void enableTwoFactor(Long userId, TwoFactorEnableDTO twoFactorEnableDTO);

    void disableTwoFactor(Long userId, TwoFactorDisableDTO twoFactorDisableDTO);
}
