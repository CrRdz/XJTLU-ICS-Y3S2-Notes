package com.cpt202.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.dto.AdminUserQueryDTO;
import com.cpt202.dto.AdminUserUpdateDTO;
import com.cpt202.exception.NotFoundException;
import com.cpt202.exception.RuleViolationException;
import com.cpt202.model.entity.User;
import com.cpt202.result.PageResult;
import com.cpt202.repository.UserRepository;
import com.cpt202.service.UserAuthStateService;
import com.cpt202.service.UserAdminService;
import com.cpt202.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端用户服务实现类。
 * 用于承接用户列表查询与账号状态修改相关业务。
 */
@Service
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepository;
    private final UserAuthStateService userAuthStateService;

    /**
     * 查询用户列表。
     */
    @Override
    public List<UserVO> listUsers(User.UserRole role, String accountStatus) {
        String normalizedAccountStatus = accountStatus == null ? null : accountStatus.trim();
        return userRepository.findUserVos(role, normalizedAccountStatus);
    }

    @Override
    public PageResult<UserVO> listUsersPage(AdminUserQueryDTO queryDTO) {
        String normalizedAccountStatus = queryDTO.getAccountStatus() == null ? null : queryDTO.getAccountStatus().trim();
        Pageable pageable = PageRequest.of(
                Math.max(0, queryDTO.getPageNum() - 1),
                queryDTO.getPageSize(),
                Sort.by(Sort.Direction.ASC, "userId"));
        Page<UserVO> userPage = userRepository.findUserVos(queryDTO.getRole(), normalizedAccountStatus, pageable);
        return PageResult.fromPage(userPage);
    }

    /**
     * 修改用户状态。
     */
    @Override
    public void updateStatus(Long userId, String accountStatus) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.USER_NOT_FOUND));
        user.setAccountStatus(accountStatus);
        userRepository.save(user);
        userAuthStateService.evictUserAuthState(userId);
    }

    /**
     * 修改用户基础信息。
     */
    @Override
    @Transactional
    public void updateUser(Long userId, AdminUserUpdateDTO updateDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(MessageConstants.USER_NOT_FOUND));

        String username = updateDTO.getUsername().trim();
        String email = updateDTO.getEmail().trim();
        String fullName = updateDTO.getFullName().trim();

        if (userRepository.existsByUsernameAndUserIdNot(username, userId)) {
            throw new RuleViolationException(MessageConstants.USERNAME_EXISTS);
        }
        if (userRepository.existsByEmailIgnoreCaseAndUserIdNot(email, userId)) {
            throw new RuleViolationException(MessageConstants.EMAIL_EXISTS);
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
