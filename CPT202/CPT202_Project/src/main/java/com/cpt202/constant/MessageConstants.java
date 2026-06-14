package com.cpt202.constant;

/**
 * Centralized business and validation messages used by the backend.
 */
public final class MessageConstants {

    public static final String INVALID_BEARER_TOKEN = "Missing or invalid Authorization Bearer token.";
    public static final String INVALID_LOGIN_STATE = "Login state is invalid or expired. Please log in again.";
    public static final String USER_NOT_FOUND_OR_DENIED = "User not found. Access denied.";
    public static final String ACCOUNT_DISABLED = "Account is currently unavailable. Access denied.";
    public static final String ROLE_REQUIRED_TEMPLATE = "Insufficient permissions: current role is %s, this operation requires %s role.";
    public static final String ROLE_MISMATCH = "Login role does not match the current account role.";

    public static final String USERNAME_EXISTS = "Username already exists. Please try a different one.";
    public static final String EMAIL_EXISTS = "Email is already registered. Please try a different one.";
    public static final String EMAIL_FORMAT_INVALID = "Invalid email format.";
    public static final String EMAIL_DOMAIN_NOT_ALLOWED = "Email domain is not supported. Please use @student.xjtlu.edu.cn (student) or @xjtlu.edu.cn (teacher) email to register.";
    public static final String DATA_INTEGRITY_VIOLATION = "Data conflicts with existing records. Please check and try again.";
    public static final String INVALID_REQUEST_BODY = "Invalid request body format. Please check fields such as date format.";
    public static final String INTERNAL_SERVER_ERROR = "An unexpected server error occurred. Please try again later.";
    public static final String INVALID_CREDENTIALS = "Invalid username or password.";
    public static final String EMAIL_OTP_SENT = "If the email is associated with an available account, a verification code has been sent. Please check your inbox.";
    public static final String EMAIL_OTP_MAIL_NOT_CONFIGURED = "The system has not configured a sender email. Email OTP login is temporarily unavailable.";
    public static final String EMAIL_OTP_MAIL_SEND_FAILED = "Failed to send verification code email. Please check the email service configuration.";
    public static final String EMAIL_OTP_REQUIRED = "Verification code is required.";
    public static final String EMAIL_OTP_INVALID = "Invalid verification code. Please re-enter.";
    public static final String EMAIL_OTP_EXPIRED = "Verification code has expired. Please request a new one.";
    public static final String EMAIL_OTP_REQUEST_TOO_FREQUENT = "Verification code requests are too frequent. Please wait and try again.";
    public static final String TWO_FACTOR_CODE_REQUIRED = "Two-factor authentication code is required.";
    public static final String TWO_FACTOR_CODE_INVALID = "Invalid two-factor authentication code. Please try again.";
    public static final String TWO_FACTOR_NOT_ENABLED = "Two-factor authentication is not enabled for this account.";
    public static final String TWO_FACTOR_ALREADY_ENABLED = "Two-factor authentication is already enabled for this account.";
    public static final String TWO_FACTOR_SETUP_EXPIRED = "Two-factor authentication setup has expired. Please start the setup process again.";
    public static final String TWO_FACTOR_LOGIN_REQUIRED = "Two-factor authentication is required to complete login for this account.";
    public static final String TWO_FACTOR_CHALLENGE_INVALID = "Two-factor authentication login session is invalid or expired. Please log in again.";
    public static final String PASSWORD_RESET_EMAIL_SENT = "If the email is associated with an account, a reset link has been sent. Please check your inbox.";
    public static final String PASSWORD_RESET_LINK_INVALID = "Invalid reset link. Please request a new one.";
    public static final String PASSWORD_RESET_LINK_EXPIRED = "Reset link has expired. Please request a new one.";
    public static final String PASSWORD_RESET_LINK_ALREADY_USED = "Reset link is no longer valid. Please request a new one.";
    public static final String PASSWORD_RESET_TOKEN_REQUIRED = "Reset token is required.";
    public static final String PASSWORD_RESET_MAIL_NOT_CONFIGURED = "The system has not configured a sender email. Password reset via email is temporarily unavailable.";
    public static final String PASSWORD_RESET_MAIL_SEND_FAILED = "Failed to send password reset email. Please check the email service configuration.";
    public static final String ACCOUNT_UNAVAILABLE_CONTACT_ADMIN = "Account is currently unavailable. Please contact the administrator.";
    public static final String STUDENT_NO_REQUIRED = "Student number is required.";
    public static final String STUDENT_PROGRAMME_REQUIRED = "Student programme is required.";
    public static final String STUDENT_ENROLLMENT_DATE_REQUIRED = "Enrollment date is required.";
    public static final String TEACHER_STAFF_NO_REQUIRED = "Staff number is required.";
    public static final String TEACHER_DEPARTMENT_REQUIRED = "Department is required.";
    public static final String TEACHER_TITLE_REQUIRED = "Title is required.";
    public static final String ENROLLMENT_DATE_CANNOT_BE_FUTURE = "Enrollment date cannot be in the future.";
    public static final String PROJECT_STATUS_REQUESTED_NOT_ALLOWED_MANUALLY = "Teachers cannot manually set project status to REQUESTED.";
    public static final String PROJECT_STATUS_ARCHIVED_DISABLED = "The system currently only supports CLOSED status. Please do not set ARCHIVED.";
    public static final String PROJECT_STATUS_TRANSITION_INVALID = "The current project status does not allow this change.";
    public static final String PROJECT_CLOSE_DATE_INVALID = "Project application deadline must be in the future.";
    public static final String PROJECT_CAPACITY_EXCEEDED = "Project capacity is full. No more requests can be accepted.";
    public static final String PROJECT_REQUEST_ALREADY_EXISTS = "You have already applied for this project. Duplicate requests are not allowed.";
    public static final String PROJECT_PREFERENCE_RANK_DUPLICATED = "Preference rank cannot be duplicated. Please choose a different rank.";
    public static final String PROJECT_CLOSED_AND_REQUEST_CANCELLED = "The project has been closed. Your original request has been cancelled. Please choose another project.";
    public static final String CATEGORY_NAME_EXISTS = "Category name already exists. Please do not create duplicates.";
    public static final String TAG_NAME_EXISTS = "Tag name already exists. Please do not create duplicates.";

    public static final String CATEGORY_NOT_FOUND = "Category not found.";
    public static final String CATEGORY_TO_UPDATE_NOT_FOUND = "Category to update not found.";
    public static final String CATEGORY_TO_DELETE_NOT_FOUND = "Category to delete not found.";
    public static final String TAG_NOT_FOUND = "Tag not found.";
    public static final String TAG_TO_UPDATE_NOT_FOUND = "Tag to update not found.";
    public static final String TAG_TO_DELETE_NOT_FOUND = "Tag to delete not found.";
    public static final String PROJECT_NOT_FOUND = "Project not found.";
    public static final String PROJECT_CATEGORY_NOT_FOUND = "Project category not found.";
    public static final String TEACHER_NOT_FOUND = "Teacher not found.";
    public static final String PARTIAL_TAGS_NOT_FOUND = "Some tags were not found.";
    public static final String REQUEST_NOT_FOUND = "Request record not found.";
    public static final String USER_NOT_FOUND = "User not found.";
    public static final String STUDENT_PROFILE_NOT_FOUND = "Student profile not found.";
    public static final String TEACHER_PROFILE_NOT_FOUND = "Teacher profile not found.";

    public static final String CANNOT_OPERATE_OTHER_TEACHER_PROJECT = "Cannot operate on projects belonging to other teachers.";
    public static final String CANNOT_UPDATE_OTHER_TEACHER_TAGS = "Cannot modify tags for projects belonging to other teachers.";
    public static final String CANNOT_REVIEW_OTHER_TEACHER_REQUEST = "Cannot review requests for projects belonging to other teachers.";
    public static final String CANNOT_WITHDRAW_OTHER_STUDENT_REQUEST = "Cannot withdraw requests belonging to other students.";
    public static final String CANNOT_VIEW_OTHER_STUDENT_HISTORY = "Cannot view request history belonging to other students.";
    public static final String INCORRECT_OLD_PASSWORD = "Incorrect current password.";
    public static final String NEW_PASSWORD_SAME_AS_OLD = "New password cannot be the same as the current password.";
    public static final String TWO_FACTOR_DISABLE_PASSWORD_REQUIRED = "Please enter your current password before disabling two-factor authentication.";

    public static final String NON_STUDENT_PROFILE_ACCESS = "This user is not a student. Cannot access student profile.";
    public static final String NON_STUDENT_PROFILE_UPDATE = "This user is not a student. Cannot update student profile.";
    public static final String NON_TEACHER_PROFILE_ACCESS = "This user is not a teacher. Cannot access teacher profile.";
    public static final String NON_TEACHER_PROFILE_UPDATE = "This user is not a teacher. Cannot update teacher profile.";
    public static final String NON_ADMIN_PROFILE_ACCESS = "This user is not an admin. Cannot access admin profile.";
    public static final String NON_ADMIN_PROFILE_UPDATE = "This user is not an admin. Cannot update admin profile.";

    public static final String REQUEST_DEADLINE_PASSED = "The deadline has passed.";
    public static final String ACTIVE_REQUEST_EXISTS = "Operation failed: You already have an agreed project.";
    public static final String PROJECT_NOT_ACCEPTING_REQUESTS = "This project is closed or archived and cannot accept new requests.";
    public static final String REQUEST_RECORD_NOT_FOUND = "Request record not found.";
    public static final String STUDENT_RECORD_NOT_FOUND = "Student record not found.";
    public static final String PROJECT_RECORD_NOT_FOUND = "Project record not found.";

    public static final String REQUEST_NOT_PENDING_CANNOT_REVIEW = "This request is not in pending status and cannot be reviewed again.";
    public static final String AUTO_REJECT_ALREADY_MATCHED = "System auto-rejected: Already matched elsewhere.";
    public static final String AUTO_REJECT_ALREADY_MATCHED_REMARK = "System auto-rejected: The student has been accepted into another project.";
    public static final String REQUEST_SUBMIT_REMARK = "Student submitted the request.";
    public static final String REQUEST_WITHDRAW_REMARK = "Student withdrew the request.";
    public static final String AUTO_CANCEL_PROJECT_CLOSED_REMARK = "System auto-cancelled: The teacher closed the project.";

    private MessageConstants() {
    }
}
