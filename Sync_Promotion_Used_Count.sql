USE QuanLyMyPham;
GO

UPDATE p
SET used_count = usage_stats.used_count
FROM dbo.Promotions p
INNER JOIN (
    SELECT
        p2.promotion_id,
        COUNT(o.order_id) AS used_count
    FROM dbo.Promotions p2
    LEFT JOIN dbo.Orders o
        ON o.promotion_id = p2.promotion_id
       AND o.discount_amount > 0
       AND o.status <> 'CANCELLED'
    GROUP BY p2.promotion_id
) usage_stats
    ON usage_stats.promotion_id = p.promotion_id;
GO

