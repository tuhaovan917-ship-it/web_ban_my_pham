/*==============================================================
  Database: QuanLyMyPham
  DBMS: SQL Server
  Purpose: Cosmetics e-commerce management system
==============================================================*/

IF DB_ID(N'QuanLyMyPham') IS NULL
BEGIN
    CREATE DATABASE QuanLyMyPham;
END
GO

USE QuanLyMyPham;
GO

/* Drop tables in dependency order */
IF OBJECT_ID(N'dbo.OrderStatusHistories', N'U') IS NOT NULL DROP TABLE dbo.OrderStatusHistories;
IF OBJECT_ID(N'dbo.Payments', N'U') IS NOT NULL DROP TABLE dbo.Payments;
IF OBJECT_ID(N'dbo.Reviews', N'U') IS NOT NULL DROP TABLE dbo.Reviews;
IF OBJECT_ID(N'dbo.OrderDetails', N'U') IS NOT NULL DROP TABLE dbo.OrderDetails;
IF OBJECT_ID(N'dbo.Orders', N'U') IS NOT NULL DROP TABLE dbo.Orders;
IF OBJECT_ID(N'dbo.CartItems', N'U') IS NOT NULL DROP TABLE dbo.CartItems;
IF OBJECT_ID(N'dbo.Carts', N'U') IS NOT NULL DROP TABLE dbo.Carts;
IF OBJECT_ID(N'dbo.ProductImages', N'U') IS NOT NULL DROP TABLE dbo.ProductImages;
IF OBJECT_ID(N'dbo.Products', N'U') IS NOT NULL DROP TABLE dbo.Products;
IF OBJECT_ID(N'dbo.Promotions', N'U') IS NOT NULL DROP TABLE dbo.Promotions;
IF OBJECT_ID(N'dbo.Brands', N'U') IS NOT NULL DROP TABLE dbo.Brands;
IF OBJECT_ID(N'dbo.Categories', N'U') IS NOT NULL DROP TABLE dbo.Categories;
IF OBJECT_ID(N'dbo.Users', N'U') IS NOT NULL DROP TABLE dbo.Users;
GO

CREATE TABLE dbo.Users
(
    user_id     INT IDENTITY(1,1) NOT NULL,
    user_name   NVARCHAR(50)      NOT NULL,
    password    VARCHAR(255)      NOT NULL,
    full_name   NVARCHAR(100)     NOT NULL,
    email       VARCHAR(100)      NOT NULL,
    phone       VARCHAR(15)       NULL,
    address     NVARCHAR(255)     NULL,
    role        VARCHAR(20)       NOT NULL CONSTRAINT DF_Users_role DEFAULT ('CUSTOMER'),
    is_active   BIT               NOT NULL CONSTRAINT DF_Users_is_active DEFAULT (1),
    created_at  DATETIME2(0)      NOT NULL CONSTRAINT DF_Users_created_at DEFAULT (SYSDATETIME()),
    updated_at  DATETIME2(0)      NULL,
    CONSTRAINT PK_Users PRIMARY KEY (user_id),
    CONSTRAINT UQ_Users_user_name UNIQUE (user_name),
    CONSTRAINT UQ_Users_email UNIQUE (email),
    CONSTRAINT CK_Users_role CHECK (role IN ('ADMIN', 'CUSTOMER')),
    CONSTRAINT CK_Users_phone CHECK (phone IS NULL OR phone NOT LIKE '%[^0-9]%')
);
GO

CREATE TABLE dbo.Categories
(
    category_id   INT IDENTITY(1,1) NOT NULL,
    category_name NVARCHAR(50)      NOT NULL,
    description   NVARCHAR(255)     NULL,
    is_active     BIT               NOT NULL CONSTRAINT DF_Categories_is_active DEFAULT (1),
    CONSTRAINT PK_Categories PRIMARY KEY (category_id),
    CONSTRAINT UQ_Categories_category_name UNIQUE (category_name)
);
GO

CREATE TABLE dbo.Brands
(
    brand_id    INT IDENTITY(1,1) NOT NULL,
    brand_name  NVARCHAR(80)      NOT NULL,
    description NVARCHAR(255)     NULL,
    is_active   BIT               NOT NULL CONSTRAINT DF_Brands_is_active DEFAULT (1),
    CONSTRAINT PK_Brands PRIMARY KEY (brand_id),
    CONSTRAINT UQ_Brands_brand_name UNIQUE (brand_name)
);
GO

CREATE TABLE dbo.Products
(
    product_id      INT IDENTITY(1,1) NOT NULL,
    category_id     INT               NOT NULL,
    brand_id        INT               NULL,
    name            NVARCHAR(100)     NOT NULL,
    price           DECIMAL(18,2)     NOT NULL,
    sale_price      DECIMAL(18,2)     NULL,
    stock_quantity  INT               NOT NULL CONSTRAINT DF_Products_stock_quantity DEFAULT (0),
    description     NVARCHAR(MAX)     NULL,
    main_image_path NVARCHAR(255)     NULL,
    is_featured     BIT               NOT NULL CONSTRAINT DF_Products_is_featured DEFAULT (0),
    is_active       BIT               NOT NULL CONSTRAINT DF_Products_is_active DEFAULT (1),
    created_at      DATETIME2(0)      NOT NULL CONSTRAINT DF_Products_created_at DEFAULT (SYSDATETIME()),
    updated_at      DATETIME2(0)      NULL,
    version         BIGINT            NOT NULL CONSTRAINT DF_Products_version DEFAULT (0),
    CONSTRAINT PK_Products PRIMARY KEY (product_id),
    CONSTRAINT FK_Products_Categories FOREIGN KEY (category_id) REFERENCES dbo.Categories(category_id),
    CONSTRAINT FK_Products_Brands FOREIGN KEY (brand_id) REFERENCES dbo.Brands(brand_id),
    CONSTRAINT CK_Products_price CHECK (price >= 0),
    CONSTRAINT CK_Products_sale_price CHECK (sale_price IS NULL OR (sale_price >= 0 AND sale_price <= price)),
    CONSTRAINT CK_Products_stock_quantity CHECK (stock_quantity >= 0)
);
GO

CREATE TABLE dbo.ProductImages
(
    image_id      INT IDENTITY(1,1) NOT NULL,
    product_id    INT               NOT NULL,
    image_path    NVARCHAR(255)     NOT NULL,
    display_order INT               NOT NULL CONSTRAINT DF_ProductImages_display_order DEFAULT (0),
    is_main       BIT               NOT NULL CONSTRAINT DF_ProductImages_is_main DEFAULT (0),
    CONSTRAINT PK_ProductImages PRIMARY KEY (image_id),
    CONSTRAINT FK_ProductImages_Products FOREIGN KEY (product_id) REFERENCES dbo.Products(product_id) ON DELETE CASCADE,
    CONSTRAINT CK_ProductImages_display_order CHECK (display_order >= 0)
);
GO

CREATE TABLE dbo.Promotions
(
    promotion_id     INT IDENTITY(1,1) NOT NULL,
    code             VARCHAR(30)       NOT NULL,
    description      NVARCHAR(255)     NULL,
    discount_type    VARCHAR(20)       NOT NULL,
    discount_value   DECIMAL(18,2)     NOT NULL,
    min_order_amount DECIMAL(18,2)     NOT NULL CONSTRAINT DF_Promotions_min_order_amount DEFAULT (0),
    max_discount     DECIMAL(18,2)     NULL,
    start_date       DATETIME2(0)      NOT NULL,
    end_date         DATETIME2(0)      NOT NULL,
    usage_limit      INT               NULL,
    used_count       INT               NOT NULL CONSTRAINT DF_Promotions_used_count DEFAULT (0),
    is_active        BIT               NOT NULL CONSTRAINT DF_Promotions_is_active DEFAULT (1),
    CONSTRAINT PK_Promotions PRIMARY KEY (promotion_id),
    CONSTRAINT UQ_Promotions_code UNIQUE (code),
    CONSTRAINT CK_Promotions_discount_type CHECK (discount_type IN ('PERCENT', 'FIXED')),
    CONSTRAINT CK_Promotions_discount_value CHECK (discount_value > 0),
    CONSTRAINT CK_Promotions_percent_value CHECK (discount_type <> 'PERCENT' OR discount_value <= 100),
    CONSTRAINT CK_Promotions_min_order_amount CHECK (min_order_amount >= 0),
    CONSTRAINT CK_Promotions_max_discount CHECK (max_discount IS NULL OR max_discount >= 0),
    CONSTRAINT CK_Promotions_usage CHECK (usage_limit IS NULL OR usage_limit >= 0),
    CONSTRAINT CK_Promotions_used_count CHECK (used_count >= 0),
    CONSTRAINT CK_Promotions_date CHECK (end_date > start_date)
);
GO

CREATE TABLE dbo.Carts
(
    cart_id    INT IDENTITY(1,1) NOT NULL,
    user_id    INT               NOT NULL,
    created_at DATETIME2(0)      NOT NULL CONSTRAINT DF_Carts_created_at DEFAULT (SYSDATETIME()),
    updated_at DATETIME2(0)      NULL,
    CONSTRAINT PK_Carts PRIMARY KEY (cart_id),
    CONSTRAINT UQ_Carts_user_id UNIQUE (user_id),
    CONSTRAINT FK_Carts_Users FOREIGN KEY (user_id) REFERENCES dbo.Users(user_id) ON DELETE CASCADE
);
GO

CREATE TABLE dbo.CartItems
(
    cart_item_id INT IDENTITY(1,1) NOT NULL,
    cart_id      INT               NOT NULL,
    product_id   INT               NOT NULL,
    quantity     INT               NOT NULL,
    added_at     DATETIME2(0)      NOT NULL CONSTRAINT DF_CartItems_added_at DEFAULT (SYSDATETIME()),
    CONSTRAINT PK_CartItems PRIMARY KEY (cart_item_id),
    CONSTRAINT UQ_CartItems_cart_product UNIQUE (cart_id, product_id),
    CONSTRAINT FK_CartItems_Carts FOREIGN KEY (cart_id) REFERENCES dbo.Carts(cart_id) ON DELETE CASCADE,
    CONSTRAINT FK_CartItems_Products FOREIGN KEY (product_id) REFERENCES dbo.Products(product_id),
    CONSTRAINT CK_CartItems_quantity CHECK (quantity > 0)
);
GO

CREATE TABLE dbo.Orders
(
    order_id          INT IDENTITY(1,1) NOT NULL,
    user_id           INT               NOT NULL,
    promotion_id      INT               NULL,
    order_date        DATETIME2(0)      NOT NULL CONSTRAINT DF_Orders_order_date DEFAULT (SYSDATETIME()),
    status            VARCHAR(30)       NOT NULL CONSTRAINT DF_Orders_status DEFAULT ('PENDING_CONFIRMATION'),
    receiver_name     NVARCHAR(100)     NOT NULL,
    receiver_phone    VARCHAR(15)       NOT NULL,
    shipping_address  NVARCHAR(255)     NOT NULL,
    payment_method    VARCHAR(20)       NOT NULL,
    subtotal_amount   DECIMAL(18,2)     NOT NULL,
    discount_amount   DECIMAL(18,2)     NOT NULL CONSTRAINT DF_Orders_discount_amount DEFAULT (0),
    shipping_fee      DECIMAL(18,2)     NOT NULL CONSTRAINT DF_Orders_shipping_fee DEFAULT (0),
    total_amount      DECIMAL(18,2)     NOT NULL,
    note              NVARCHAR(255)     NULL,
    created_at        DATETIME2(0)      NOT NULL CONSTRAINT DF_Orders_created_at DEFAULT (SYSDATETIME()),
    updated_at        DATETIME2(0)      NULL,
    CONSTRAINT PK_Orders PRIMARY KEY (order_id),
    CONSTRAINT FK_Orders_Users FOREIGN KEY (user_id) REFERENCES dbo.Users(user_id),
    CONSTRAINT FK_Orders_Promotions FOREIGN KEY (promotion_id) REFERENCES dbo.Promotions(promotion_id),
    CONSTRAINT CK_Orders_status CHECK (status IN ('PAYMENT_IN_PROGRESS', 'PENDING_CONFIRMATION', 'CONFIRMED', 'SHIPPING', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT CK_Orders_receiver_phone CHECK (receiver_phone NOT LIKE '%[^0-9]%'),
    CONSTRAINT CK_Orders_payment_method CHECK (payment_method IN ('COD', 'CARD', 'EWALLET')),
    CONSTRAINT CK_Orders_amounts CHECK (
        subtotal_amount >= 0
        AND discount_amount >= 0
        AND shipping_fee >= 0
        AND total_amount >= 0
    )
);
GO

CREATE TABLE dbo.OrderDetails
(
    order_detail_id INT IDENTITY(1,1) NOT NULL,
    order_id        INT               NOT NULL,
    product_id      INT               NOT NULL,
    product_name    NVARCHAR(100)     NOT NULL,
    unit_price      DECIMAL(18,2)     NOT NULL,
    quantity        INT               NOT NULL,
    line_total      DECIMAL(18,2)     NOT NULL,
    CONSTRAINT PK_OrderDetails PRIMARY KEY (order_detail_id),
    CONSTRAINT FK_OrderDetails_Orders FOREIGN KEY (order_id) REFERENCES dbo.Orders(order_id) ON DELETE CASCADE,
    CONSTRAINT FK_OrderDetails_Products FOREIGN KEY (product_id) REFERENCES dbo.Products(product_id),
    CONSTRAINT CK_OrderDetails_unit_price CHECK (unit_price >= 0),
    CONSTRAINT CK_OrderDetails_quantity CHECK (quantity > 0),
    CONSTRAINT CK_OrderDetails_line_total CHECK (line_total >= 0)
);
GO

CREATE TABLE dbo.Payments
(
    payment_id     INT IDENTITY(1,1) NOT NULL,
    order_id       INT               NOT NULL,
    method         VARCHAR(20)       NOT NULL,
    status         VARCHAR(20)       NOT NULL CONSTRAINT DF_Payments_status DEFAULT ('UNPAID'),
    amount         DECIMAL(18,2)     NOT NULL,
    transaction_id VARCHAR(100)      NULL,
    paid_at        DATETIME2(0)      NULL,
    created_at     DATETIME2(0)      NOT NULL CONSTRAINT DF_Payments_created_at DEFAULT (SYSDATETIME()),
    CONSTRAINT PK_Payments PRIMARY KEY (payment_id),
    CONSTRAINT UQ_Payments_order_id UNIQUE (order_id),
    CONSTRAINT FK_Payments_Orders FOREIGN KEY (order_id) REFERENCES dbo.Orders(order_id) ON DELETE CASCADE,
    CONSTRAINT CK_Payments_method CHECK (method IN ('COD', 'CARD', 'EWALLET')),
    CONSTRAINT CK_Payments_status CHECK (status IN ('UNPAID', 'PAID', 'FAILED', 'REFUNDED')),
    CONSTRAINT CK_Payments_amount CHECK (amount >= 0)
);
GO

CREATE TABLE dbo.OrderStatusHistories
(
    history_id INT IDENTITY(1,1) NOT NULL,
    order_id   INT               NOT NULL,
    old_status VARCHAR(30)       NULL,
    new_status VARCHAR(30)       NOT NULL,
    changed_by INT               NULL,
    changed_at DATETIME2(0)      NOT NULL CONSTRAINT DF_OrderStatusHistories_changed_at DEFAULT (SYSDATETIME()),
    note       NVARCHAR(255)     NULL,
    CONSTRAINT PK_OrderStatusHistories PRIMARY KEY (history_id),
    CONSTRAINT FK_OrderStatusHistories_Orders FOREIGN KEY (order_id) REFERENCES dbo.Orders(order_id) ON DELETE CASCADE,
    CONSTRAINT FK_OrderStatusHistories_Users FOREIGN KEY (changed_by) REFERENCES dbo.Users(user_id),
    CONSTRAINT CK_OrderStatusHistories_old_status CHECK (old_status IS NULL OR old_status IN ('PAYMENT_IN_PROGRESS', 'PENDING_CONFIRMATION', 'CONFIRMED', 'SHIPPING', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT CK_OrderStatusHistories_new_status CHECK (new_status IN ('PAYMENT_IN_PROGRESS', 'PENDING_CONFIRMATION', 'CONFIRMED', 'SHIPPING', 'COMPLETED', 'CANCELLED'))
);
GO

CREATE TABLE dbo.Reviews
(
    review_id  INT IDENTITY(1,1) NOT NULL,
    user_id    INT               NOT NULL,
    product_id INT               NOT NULL,
    order_id   INT               NULL,
    comment    NVARCHAR(500)     NULL,
    stars      TINYINT           NOT NULL,
    status     VARCHAR(20)       NOT NULL CONSTRAINT DF_Reviews_status DEFAULT ('PENDING'),
    created_at DATETIME2(0)      NOT NULL CONSTRAINT DF_Reviews_created_at DEFAULT (SYSDATETIME()),
    updated_at DATETIME2(0)      NULL,
    CONSTRAINT PK_Reviews PRIMARY KEY (review_id),
    CONSTRAINT FK_Reviews_Users FOREIGN KEY (user_id) REFERENCES dbo.Users(user_id) ON DELETE CASCADE,
    CONSTRAINT FK_Reviews_Products FOREIGN KEY (product_id) REFERENCES dbo.Products(product_id) ON DELETE CASCADE,
    CONSTRAINT FK_Reviews_Orders FOREIGN KEY (order_id) REFERENCES dbo.Orders(order_id),
    CONSTRAINT CK_Reviews_stars CHECK (stars BETWEEN 1 AND 5),
    CONSTRAINT CK_Reviews_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);
GO

CREATE INDEX IX_Products_name ON dbo.Products(name);
CREATE INDEX IX_Products_category ON dbo.Products(category_id);
CREATE INDEX IX_Products_brand ON dbo.Products(brand_id);
CREATE INDEX IX_Products_price ON dbo.Products(price);
CREATE INDEX IX_Products_created_at ON dbo.Products(created_at DESC);
CREATE INDEX IX_Orders_user ON dbo.Orders(user_id);
CREATE INDEX IX_Orders_status ON dbo.Orders(status);
CREATE INDEX IX_Orders_order_date ON dbo.Orders(order_date DESC);
CREATE INDEX IX_Reviews_product ON dbo.Reviews(product_id);
GO

INSERT INTO dbo.Users (user_name, password, full_name, email, phone, role)
VALUES
('admin', '$2a$10$replace_with_bcrypt_hash', N'Administrator', 'admin@example.com', '0900000000', 'ADMIN'),
('customer01', '$2a$10$replace_with_bcrypt_hash', N'Demo Customer', 'customer01@example.com', '0911111111', 'CUSTOMER');

INSERT INTO dbo.Categories (category_name, description)
VALUES
(N'Skincare', N'Facial skincare products'),
(N'Makeup', N'Cosmetics and makeup products'),
(N'Haircare', N'Hair care products'),
(N'Fragrance', N'Perfume and fragrance products');

INSERT INTO dbo.Brands (brand_name, description)
VALUES
(N'Innisfree', N'Cosmetics brand'),
(N'Maybelline', N'Cosmetics brand'),
(N'LOreal', N'Cosmetics brand'),
(N'Cocoon', N'Cosmetics brand');

INSERT INTO dbo.Products
(
    category_id, brand_id, name, price, sale_price, stock_quantity,
    description, main_image_path, is_featured
)
VALUES
(1, 1, N'Green Tea Seed Serum', 350000, 315000, 20, N'Hydrating facial serum', N'/uploads/products/green-tea-serum.jpg', 1),
(2, 2, N'Fit Me Foundation', 250000, NULL, 15, N'Liquid foundation for daily makeup', N'/uploads/products/fit-me-foundation.jpg', 1),
(1, 4, N'Rose Gel Cleanser', 195000, 175000, 4, N'Gentle facial cleanser', N'/uploads/products/rose-cleanser.jpg', 0),
(3, 3, N'Hair Repair Shampoo', 180000, NULL, 8, N'Shampoo for damaged hair', N'/uploads/products/hair-repair-shampoo.jpg', 0);

INSERT INTO dbo.ProductImages (product_id, image_path, display_order, is_main)
VALUES
(1, N'/uploads/products/green-tea-serum.jpg', 1, 1),
(2, N'/uploads/products/fit-me-foundation.jpg', 1, 1),
(3, N'/uploads/products/rose-cleanser.jpg', 1, 1),
(4, N'/uploads/products/hair-repair-shampoo.jpg', 1, 1);

INSERT INTO dbo.Promotions
(
    code, description, discount_type, discount_value, min_order_amount,
    max_discount, start_date, end_date, usage_limit
)
VALUES
('WELCOME10', N'Discount 10 percent for first order', 'PERCENT', 10, 200000, 50000, '2026-01-01', '2026-12-31', 100),
('FREESHIP30', N'Discount fixed amount for shipping support', 'FIXED', 30000, 300000, NULL, '2026-01-01', '2026-12-31', 200);
GO
