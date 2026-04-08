-- ============================================
-- DỮ LIỆU MẪU CHO 5 NGƯỜI DÙNG VÀ GIAO DỊCH
-- ============================================

-- 1. Chèn người dùng (các user mới)
INSERT IGNORE INTO users (username, password, full_name, email) VALUES
('admin', 'root', 'Nguyen Van Admin', 'admin@gmail.com'),
('user123', 'root', 'Tran Thi User', 'user@gmail.com'),
('john_doe', '123456', 'John Doe', 'john@example.com'),
('jane_doe', '123456', 'Jane Doe', 'jane@example.com'),
('test_user', 'test123', 'Test User', 'test@example.com');

-- 2. Chèn danh mục (nếu chưa có)
INSERT IGNORE INTO categories (category_name, type) VALUES
('Lương', 'THU'),
('Tiền thưởng', 'THU'),
('Ăn uống', 'CHI'),
('Tiền nhà', 'CHI'),
('Di chuyển', 'CHI'),
('Mua sắm', 'CHI');

-- 3. Chèn ngân sách tháng 3/2026 cho từng user
INSERT IGNORE INTO budgets (month, total_limit, category_limit, user_id, category_id)
SELECT
    '2026-03' AS month,
    12000000 AS total_limit,
    CASE cat_name
        WHEN 'Ăn uống' THEN 5000000
        WHEN 'Tiền nhà' THEN 3000000
        WHEN 'Di chuyển' THEN 2000000
        WHEN 'Mua sắm' THEN 2000000
    END AS category_limit,
    u.user_id,
    c.category_id
FROM users u
CROSS JOIN (
    SELECT 'Ăn uống' AS cat_name
    UNION ALL SELECT 'Tiền nhà'
    UNION ALL SELECT 'Di chuyển'
    UNION ALL SELECT 'Mua sắm'
) t
JOIN categories c ON c.category_name = t.cat_name AND c.type = 'CHI'
WHERE u.username IN ('admin', 'user123', 'john_doe', 'jane_doe', 'test_user');

-- 4. Chèn giao dịch cho tháng 2 và tháng 3/2026
INSERT IGNORE INTO transactions (amount, date, note, type, user_id, category_id)
SELECT amount, date, note, type, user_id, category_id FROM (
    -- admin
    SELECT 15000000 AS amount, '2026-02-01' AS date, 'Lương tháng 2' AS note, 'THU' AS type, (SELECT user_id FROM users WHERE username='admin') AS user_id, (SELECT category_id FROM categories WHERE category_name='Lương') AS category_id
    UNION ALL SELECT 2000000, '2026-02-10', 'Thưởng Tết', 'THU', (SELECT user_id FROM users WHERE username='admin'), (SELECT category_id FROM categories WHERE category_name='Tiền thưởng')
    UNION ALL SELECT 300000, '2026-02-15', 'Ăn tối', 'CHI', (SELECT user_id FROM users WHERE username='admin'), (SELECT category_id FROM categories WHERE category_name='Ăn uống')
    UNION ALL SELECT 150000, '2026-02-20', 'Xăng xe', 'CHI', (SELECT user_id FROM users WHERE username='admin'), (SELECT category_id FROM categories WHERE category_name='Di chuyển')
    UNION ALL SELECT 1000000, '2026-02-25', 'Mua sắm', 'CHI', (SELECT user_id FROM users WHERE username='admin'), (SELECT category_id FROM categories WHERE category_name='Mua sắm')
    -- admin tháng 3
    UNION ALL SELECT 15000000, '2026-03-01', 'Lương tháng 3', 'THU', (SELECT user_id FROM users WHERE username='admin'), (SELECT category_id FROM categories WHERE category_name='Lương')
    UNION ALL SELECT 200000, '2026-03-05', 'Ăn trưa', 'CHI', (SELECT user_id FROM users WHERE username='admin'), (SELECT category_id FROM categories WHERE category_name='Ăn uống')
    UNION ALL SELECT 50000, '2026-03-10', 'Gửi xe', 'CHI', (SELECT user_id FROM users WHERE username='admin'), (SELECT category_id FROM categories WHERE category_name='Di chuyển')
    UNION ALL SELECT 300000, '2026-03-15', 'Quần áo', 'CHI', (SELECT user_id FROM users WHERE username='admin'), (SELECT category_id FROM categories WHERE category_name='Mua sắm')
    -- user123
    UNION ALL SELECT 10000000, '2026-02-01', 'Lương tháng 2', 'THU', (SELECT user_id FROM users WHERE username='user123'), (SELECT category_id FROM categories WHERE category_name='Lương')
    UNION ALL SELECT 500000, '2026-02-12', 'Ăn uống', 'CHI', (SELECT user_id FROM users WHERE username='user123'), (SELECT category_id FROM categories WHERE category_name='Ăn uống')
    UNION ALL SELECT 2000000, '2026-02-20', 'Tiền nhà', 'CHI', (SELECT user_id FROM users WHERE username='user123'), (SELECT category_id FROM categories WHERE category_name='Tiền nhà')
    UNION ALL SELECT 10000000, '2026-03-01', 'Lương tháng 3', 'THU', (SELECT user_id FROM users WHERE username='user123'), (SELECT category_id FROM categories WHERE category_name='Lương')
    UNION ALL SELECT 600000, '2026-03-10', 'Ăn uống', 'CHI', (SELECT user_id FROM users WHERE username='user123'), (SELECT category_id FROM categories WHERE category_name='Ăn uống')
    UNION ALL SELECT 2000000, '2026-03-15', 'Tiền nhà', 'CHI', (SELECT user_id FROM users WHERE username='user123'), (SELECT category_id FROM categories WHERE category_name='Tiền nhà')
    -- john_doe
    UNION ALL SELECT 8000000, '2026-02-01', 'Lương tháng 2', 'THU', (SELECT user_id FROM users WHERE username='john_doe'), (SELECT category_id FROM categories WHERE category_name='Lương')
    UNION ALL SELECT 300000, '2026-02-08', 'Ăn uống', 'CHI', (SELECT user_id FROM users WHERE username='john_doe'), (SELECT category_id FROM categories WHERE category_name='Ăn uống')
    UNION ALL SELECT 1500000, '2026-02-15', 'Tiền nhà', 'CHI', (SELECT user_id FROM users WHERE username='john_doe'), (SELECT category_id FROM categories WHERE category_name='Tiền nhà')
    UNION ALL SELECT 100000, '2026-02-22', 'Xe bus', 'CHI', (SELECT user_id FROM users WHERE username='john_doe'), (SELECT category_id FROM categories WHERE category_name='Di chuyển')
    UNION ALL SELECT 8000000, '2026-03-01', 'Lương tháng 3', 'THU', (SELECT user_id FROM users WHERE username='john_doe'), (SELECT category_id FROM categories WHERE category_name='Lương')
    UNION ALL SELECT 500000, '2026-03-05', 'Ăn uống', 'CHI', (SELECT user_id FROM users WHERE username='john_doe'), (SELECT category_id FROM categories WHERE category_name='Ăn uống')
    UNION ALL SELECT 1500000, '2026-03-10', 'Tiền nhà', 'CHI', (SELECT user_id FROM users WHERE username='john_doe'), (SELECT category_id FROM categories WHERE category_name='Tiền nhà')
    -- jane_doe
    UNION ALL SELECT 12000000, '2026-02-01', 'Lương tháng 2', 'THU', (SELECT user_id FROM users WHERE username='jane_doe'), (SELECT category_id FROM categories WHERE category_name='Lương')
    UNION ALL SELECT 200000, '2026-02-05', 'Ăn uống', 'CHI', (SELECT user_id FROM users WHERE username='jane_doe'), (SELECT category_id FROM categories WHERE category_name='Ăn uống')
    UNION ALL SELECT 2000000, '2026-02-12', 'Tiền nhà', 'CHI', (SELECT user_id FROM users WHERE username='jane_doe'), (SELECT category_id FROM categories WHERE category_name='Tiền nhà')
    UNION ALL SELECT 50000, '2026-02-18', 'Grab', 'CHI', (SELECT user_id FROM users WHERE username='jane_doe'), (SELECT category_id FROM categories WHERE category_name='Di chuyển')
    UNION ALL SELECT 12000000, '2026-03-01', 'Lương tháng 3', 'THU', (SELECT user_id FROM users WHERE username='jane_doe'), (SELECT category_id FROM categories WHERE category_name='Lương')
    UNION ALL SELECT 400000, '2026-03-07', 'Ăn uống', 'CHI', (SELECT user_id FROM users WHERE username='jane_doe'), (SELECT category_id FROM categories WHERE category_name='Ăn uống')
    UNION ALL SELECT 2000000, '2026-03-14', 'Tiền nhà', 'CHI', (SELECT user_id FROM users WHERE username='jane_doe'), (SELECT category_id FROM categories WHERE category_name='Tiền nhà')
    -- test_user
    UNION ALL SELECT 5000000, '2026-02-01', 'Lương tháng 2', 'THU', (SELECT user_id FROM users WHERE username='test_user'), (SELECT category_id FROM categories WHERE category_name='Lương')
    UNION ALL SELECT 100000, '2026-02-10', 'Ăn uống', 'CHI', (SELECT user_id FROM users WHERE username='test_user'), (SELECT category_id FROM categories WHERE category_name='Ăn uống')
    UNION ALL SELECT 800000, '2026-02-20', 'Mua sắm', 'CHI', (SELECT user_id FROM users WHERE username='test_user'), (SELECT category_id FROM categories WHERE category_name='Mua sắm')
    UNION ALL SELECT 5000000, '2026-03-01', 'Lương tháng 3', 'THU', (SELECT user_id FROM users WHERE username='test_user'), (SELECT category_id FROM categories WHERE category_name='Lương')
    UNION ALL SELECT 150000, '2026-03-05', 'Ăn uống', 'CHI', (SELECT user_id FROM users WHERE username='test_user'), (SELECT category_id FROM categories WHERE category_name='Ăn uống')
    UNION ALL SELECT 200000, '2026-03-12', 'Di chuyển', 'CHI', (SELECT user_id FROM users WHERE username='test_user'), (SELECT category_id FROM categories WHERE category_name='Di chuyển')
) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM transactions t
    WHERE t.amount = tmp.amount
      AND t.date = tmp.date
      AND t.user_id = tmp.user_id
      AND t.category_id = tmp.category_id
);