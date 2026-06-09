package do_an_java.quan_ly_my_pham.service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Ten dang nhap khong duoc de trong")
    @Size(min = 3, max = 50, message = "Ten dang nhap phai tu 3 den 50 ky tu")
    String userName,

    @NotBlank(message = "Mat khau khong duoc de trong")
    @Size(min = 6, max = 50, message = "Mat khau phai co it nhat 6 ky tu")
    String password,

    @NotBlank(message = "Ho ten khong duoc de trong")
    @Size(max = 100, message = "Ho ten toi da 100 ky tu")
    String fullName,

    @Email(message = "Email khong dung dinh dang")
    @NotBlank(message = "Email khong duoc de trong")
    @Size(max = 100, message = "Email toi da 100 ky tu")
    String email,

    @Pattern(regexp = "^[0-9]{10}$", message = "So dien thoai phai gom 10 chu so")
    String phone,

    @Size(max = 255, message = "Dia chi toi da 255 ky tu")
    String address
) {
}
