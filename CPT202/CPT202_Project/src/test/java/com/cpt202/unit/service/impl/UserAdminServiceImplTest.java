package com.cpt202.unit.service.impl;

import com.cpt202.constant.MessageConstants;
import com.cpt202.exception.NotFoundException;
import com.cpt202.model.entity.User;
import com.cpt202.repository.UserRepository;
import com.cpt202.service.UserAuthStateService;
import com.cpt202.service.impl.UserAdminServiceImpl;
import com.cpt202.vo.UserVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for administrator-owned user listing and status management. */
@ExtendWith(MockitoExtension.class)
class UserAdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAuthStateService userAuthStateService;

    @InjectMocks
    private UserAdminServiceImpl userAdminService;

    /** Trims the requested account status before repository lookup. */
    @Test
    void listUsersShouldTrimAccountStatusBeforeQueryingRepository() {
        UserVO userVO = UserVO.builder()
                .userId(1L)
                .role(User.UserRole.STUDENT)
                .accountStatus("ACTIVE")
                .build();

        when(userRepository.findUserVos(User.UserRole.STUDENT, "ACTIVE")).thenReturn(List.of(userVO));

        List<UserVO> results = userAdminService.listUsers(User.UserRole.STUDENT, "  ACTIVE  ");

        assertThat(results).containsExactly(userVO);
        verify(userRepository).findUserVos(User.UserRole.STUDENT, "ACTIVE");
    }

    /** Rejects status updates for users that do not exist. */
    @Test
    void updateStatusShouldRejectMissingUser() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userAdminService.updateStatus(2L, "DISABLED"));

        assertThat(exception.getMessage()).isEqualTo(MessageConstants.USER_NOT_FOUND);
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any(User.class));
        verify(userAuthStateService, never()).evictUserAuthState(2L);
    }

    /** Saves the new account status and evicts cached auth state. */
    @Test
    void updateStatusShouldSaveUserAndEvictAuthState() {
        User user = new User();
        user.setUserId(3L);
        user.setAccountStatus("ACTIVE");

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));

        userAdminService.updateStatus(3L, "DISABLED");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(userAuthStateService).evictUserAuthState(3L);
        assertThat(userCaptor.getValue().getAccountStatus()).isEqualTo("DISABLED");
    }
}