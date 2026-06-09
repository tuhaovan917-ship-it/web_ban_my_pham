USE QuanLyMyPham;
GO

IF COL_LENGTH('dbo.Products', 'version') IS NULL
BEGIN
    ALTER TABLE dbo.Products
        ADD version BIGINT NOT NULL
            CONSTRAINT DF_Products_version DEFAULT (0);
END
GO
