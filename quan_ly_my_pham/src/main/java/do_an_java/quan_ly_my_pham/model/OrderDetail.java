package do_an_java.quan_ly_my_pham.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "OrderDetails")
@Data
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Integer id;

    @NotNull(message = "Chi tiet don hang phai thuoc ve mot don hang")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    @NotNull(message = "Chi tiet don hang phai co san pham")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    @NotBlank(message = "Ten san pham tai thoi diem mua khong duoc de trong")
    @Size(max = 100, message = "Ten san pham toi da 100 ky tu")
    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @NotNull(message = "Don gia khong duoc de trong")
    @DecimalMin(value = "0.0", message = "Don gia khong duoc am")
    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @NotNull(message = "So luong khong duoc de trong")
    @Min(value = 1, message = "So luong phai it nhat la 1")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull(message = "Thanh tien khong duoc de trong")
    @DecimalMin(value = "0.0", message = "Thanh tien khong duoc am")
    @Column(name = "line_total", nullable = false, precision = 18, scale = 2)
    private BigDecimal lineTotal;
}
