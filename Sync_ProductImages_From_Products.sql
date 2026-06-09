USE QuanLyMyPham;
GO

INSERT INTO dbo.ProductImages (product_id, image_path, display_order, is_main)
SELECT p.product_id, p.main_image_path, 1, 1
FROM dbo.Products p
WHERE p.main_image_path IS NOT NULL
  AND LTRIM(RTRIM(p.main_image_path)) <> ''
  AND NOT EXISTS (
      SELECT 1
      FROM dbo.ProductImages pi
      WHERE pi.product_id = p.product_id
        AND pi.image_path = p.main_image_path
  );
GO

