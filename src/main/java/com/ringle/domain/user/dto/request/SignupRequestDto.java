package com.ringle.domain.user.dto.request;

import com.ringle.domain.user.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * 회원가입 요청 DTO
 * - 이메일/비밀번호 유효성 검증 포함
 */
@Getter
@Setter
public class SignupRequestDto {

    @NotBlank(message = "이메일은 필수 입력입니다.")
    @Email(message = "올바른 이메일 형식을 입력해주세요.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[\\W_])[A-Za-z\\d\\W_]{8,}$",
            message = "비밀번호는 8자 이상, 대소문자, 숫자, 특수문자를 포함해야 합니다."
    )
    private String password;

    @NotBlank(message = "비밀번호 확인은 필수 입력입니다.")
    private String passwordConfirm;

    @NotBlank(message = "이름은 필수 입력입니다.")
    private String name;

    @NotNull(message = "사용자 역할은 필수입니다.")
    private Role role; // STUDENT or TUTOR
}
