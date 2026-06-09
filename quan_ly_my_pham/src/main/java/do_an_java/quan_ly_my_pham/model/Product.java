package do_an_java.quan_ly_my_pham.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "Products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer id;

    @NotNull(message = "San pham phai thuoc mot danh muc")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Brand brand;

    @NotBlank(message = "Ten san pham khong duoc de trong")
    @Size(max = 100, message = "Ten san pham toi da 100 ky tu")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotNull(message = "Gia khong duoc de trong")
    @DecimalMin(value = "0.0", message = "Gia khong duoc am")
    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @DecimalMin(value = "0.0", message = "Gia khuyen mai khong duoc am")
    @Column(name = "sale_price", precision = 18, scale = 2)
    private BigDecimal salePrice;

    @NotNull(message = "So luong ton kho khong duoc de trong")
    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Column(name = "description")
    private String description;

    @Size(max = 255, message = "Duong dan anh toi da 255 ky tu")
    @Column(name = "main_image_path")
    private String mainImagePath;

    @Column(name = "is_featured", nullable = false)
    private Boolean featured = false;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Review> reviews = new ArrayList<>();
}
