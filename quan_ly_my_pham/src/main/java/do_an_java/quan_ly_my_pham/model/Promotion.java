package do_an_java.quan_ly_my_pham.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Promotions")
@Data
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "promotion_id")
    private Integer id;

    @NotBlank(message = "Mã khuyến mãi không được để trống")
    @Size(max = 30, message = "Mã khuyến mãi tối đa 30 ký tự")
    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Size(max = 255, message = "Mo ta toi da 255 ky tu")
    @Column(name = "description")
    private String description;

    @NotNull(message = "Loai giam gia khong duoc de trong")
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @NotNull(message = "Gia tri giam gia khong duoc de trong")
    @DecimalMin(value = "0.01", message = "Gia tri giam gia phai lon hon 0")
    @Column(name = "discount_value", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountValue;

    @NotNull(message = "Dieu kien don toi thieu khong duoc de trong")
    @DecimalMin(value = "0.0", message = "Dieu kien don toi thieu khong duoc am")
    @Column(name = "min_order_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Muc giam toi da khong duoc am")
    @Column(name = "max_discount", precision = 18, scale = 2)
    private BigDecimal maxDiscount;

    @NotNull(message = "Ngay bat dau khong duoc de trong")
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @NotNull(message = "Ngay ket thuc khong duoc de trong")
    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "promotion", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Order> orders = new ArrayList<>();
}
