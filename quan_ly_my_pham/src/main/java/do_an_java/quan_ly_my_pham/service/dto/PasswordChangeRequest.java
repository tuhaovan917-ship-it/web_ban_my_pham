package do_an_java.quan_ly_my_pham.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeRequest(
    @NotBlank(message = "Mật khẩu hiện tại không được để trống")
    String currentPassword,

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, max = 50, message = "Mật khẩu mới phải có ít nhất 6 ký tự")
    String newPassword,

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    String confirmPassword
) {
}
