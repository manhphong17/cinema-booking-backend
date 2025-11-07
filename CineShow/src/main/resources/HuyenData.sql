insert into concession_type
(name)
values ("đồ uống"),
       ("đồ ăn nhẹ"),
       ("combo");


INSERT INTO concession
(id, created_at, updated_at, concession_status, description, name, price, stock_status, unit_in_stock, url_image, concession_type_id)
VALUES
    (1, '2025-01-01 00:42:14', '2025-01-01 00:07:29', 'DELETED', 'Xúc xích phô mai que nóng hổi, nhân phô mai tan chảy thơm béo.', 'Xúc Xích Phô Mai Que', 50000, 'IN_STOCK', 150, 'https://d28ic9z7shvx92.cloudfront.net/uploads/97b444d2-30b4-4c77-9d0b-f62835b153bc-image-base64.jpg',2),
    (2, '2025-01-01 00:42:57', '2025-01-01 00:33:20', 'DELETED', 'Khoai tây chiên giòn rụm, phủ lớp phô mai đậm đà, ăn kèm tương ớt.', 'Khoai Tây Lắc Phô Mai', 49000, 'IN_STOCK', 50, 'https://d28ic9z7shvx92.cloudfront.net/uploads/7d352d32-b596-49a8-a50c-5b11b35c9c30-image-base64.jpg',2),
    (3, '2025-01-01 00:44:40', '2025-01-01 00:37:25', 'INACTIVE', 'Bắp nổ phủ phô mai mặn ngọt hòa quyện, mùi thơm hấp dẫn khó cưỡng', 'Bắp Phô Mai', 69000, 'IN_STOCK', 280, 'https://d28ic9z7shvx92.cloudfront.net/uploads/82be49a0-5ced-4c2f-995b-8347656578f9-image-base64.jpg',2),
    (4, '2025-01-01 00:45:20', '2025-01-01 00:17:33', 'DELETED', 'Bắp nổ phủ caramel ngọt dịu, thơm giòn – món ăn biểu tượng khi xem phim.', 'Bắp Caramel', 69000, 'IN_STOCK', 260, 'https://d28ic9z7shvx92.cloudfront.net/uploads/a253855c-e596-4adc-8b86-ff6ed99800d2-image-base64.jpg',2),
    (5, '2025-01-01 00:45:59', '2025-01-01 00:45:59', 'ACTIVE', 'Ly Coca-Cola 700ml mát lạnh, hương vị quen thuộc cho mọi buổi chiếu phim.', 'Coca-Cola Ly Lớn', 49000, 'IN_STOCK', 300, 'https://d28ic9z7shvx92.cloudfront.net/uploads/7de607f8-e102-4efa-a812-eb90f02adc10-image-base64.jpg',1),
    (6, '2025-01-01 00:46:58', '2025-01-01 00:16:03', 'ACTIVE', 'Combo lãng mạn cho 2 người: bắp caramel lớn, 2 lon Pepsi, và 1 snack chiên giòn', 'Combo Hẹn Hò', 199000, 'IN_STOCK', 300, 'https://d28ic9z7shvx92.cloudfront.net/uploads/39193105-45f5-426c-af51-5275c5771b7f-image-base64.jpg',3),
    (7, '2025-01-01 00:47:40', '2025-01-01 00:47:40', 'ACTIVE', 'Dành cho 2 người: 1 bắp lớn vị phô mai và 2 ly Coca-Cola 700ml.', 'Combo Tình Bạn', 199000, 'IN_STOCK', 329, 'https://d28ic9z7shvx92.cloudfront.net/uploads/4de8c9e9-aee4-448b-87b1-4109b273c6c3-image-base64.jpg',3),
    (8, '2025-01-01 00:48:35', '2025-01-01 00:43:56', 'ACTIVE', 'Thức uống giải nhiệt nhẹ nhàng, vị cam tươi và sả thơm mát.', 'Trà Cam Sả', 59000, 'IN_STOCK', 201, 'https://d28ic9z7shvx92.cloudfront.net/uploads/be74b5d3-c5d1-4e5c-95c1-c2dba58d10ec-image-base64.jpg',1),
    (9, '2025-01-01 00:49:31', '2025-01-01 00:49:31', 'ACTIVE', 'Gồm 1 phần bắp ngọt lớn và 1 lon Pepsi 330ml – lựa chọn lý tưởng cho một mình xem phim', 'Combo Vui Vẻ', 149000, 'IN_STOCK', 379, 'https://d28ic9z7shvx92.cloudfront.net/uploads/bd12be7e-41b7-4200-9f08-91cbb63847b0-image-base64.jpg',3),
    (10, '2025-01-01 00:51:15', '2025-01-01 00:51:15', 'ACTIVE', 'Giòn rụm', 'Snack khoai tây', 29000, 'IN_STOCK', 401, 'https://d28ic9z7shvx92.cloudfront.net/uploads/6f1aca92-11cd-4e7e-968c-fb03a51386de-image-base64.jpg',2),
    (11, '2025-01-01 00:52:59', '2025-01-01 00:18:05', 'ACTIVE', 'Bắp nổ ngọt hài hòa, món ăn vặt yêu thích.', 'Bắp thường', 59000, 'IN_STOCK', 417, 'https://d28ic9z7shvx92.cloudfront.net/uploads/7edc3d0f-646c-460e-be0a-8e517e7f05a3-image-base64.jpg',2),
    (12, '2025-01-01 00:38:56', '2025-01-01 00:38:56', 'ACTIVE', 'nachos', 'nachos', 78000, 'IN_STOCK', 102, 'https://d28ic9z7shvx92.cloudfront.net/uploads/40746e11-28bd-439b-aeb7-b84b95b1850c-image-base64.jpg',2),
    (13, '2025-01-01 00:44:51', '2025-01-01 00:22:07', 'INACTIVE', 'mát lạnh', 'Spirte 300ml', 20000, 'IN_STOCK', 150, 'https://d28ic9z7shvx92.cloudfront.net/uploads/99747049-dab2-43b3-8170-7e4cec095bf7-image-base64.jpg',1),
    (14, '2025-01-01 00:35:51', '2025-01-01 00:35:51', 'ACTIVE', 'test', 'test', 10, 'IN_STOCK', 10, 'https://d28ic9z7shvx92.cloudfront.net/uploads/c49e9b89-1a23-4e55-8260-8f5776d05f30-Screenshot 2024-05-28 090323.png',1);

INSERT INTO cineshow.holidays (id, created_at, updated_at, day_of_month, description, holiday_date, is_recurring, month_of_year) VALUES
(16, '2025-10-19 11:55:19.014997', '2025-10-19 11:55:19.014997', NULL, 'm1 tết', '2026-01-22', 0, NULL),
(17, '2025-10-19 11:55:19.043074', '2025-10-19 11:55:19.043074', NULL, 'm2 tết', '2026-01-23', 0, NULL),
(21, '2025-10-19 12:16:08.744589', '2025-10-19 12:16:08.744589', 30, 'thống nhất đất nước', NULL, 1, 4),
(22, '2025-10-19 12:19:36.301333', '2025-10-19 12:19:36.301333', 2, 'quốc khánh', NULL, 1, 9),
(28, '2025-10-19 13:07:33.952130', '2025-10-19 13:07:33.952130', 1, 'quốc tế lao động', NULL, 1, 5),
(29, '2025-10-19 13:17:56.852561', '2025-10-19 13:17:56.852561', 1, 'tết dương lịch', NULL, 1, 1),
(30, '2025-10-19 13:20:29.628547', '2025-10-19 13:20:29.628547', NULL, 'm3 tết', '2026-01-24', 0, NULL),
(31, '2025-10-19 13:20:29.630933', '2025-10-19 13:20:29.630933', NULL, 'm4 tết', '2026-01-25', 0, NULL),
(32, '2025-10-19 13:33:35.861772', '2025-10-19 13:33:35.861772', NULL, 'giỗ tổ hùng vương', '2025-04-07', 0, NULL),
(33, '2025-10-19 16:38:13.319874', '2025-10-19 16:38:13.319874', 1, 'tết thiếu nhi', NULL, 1, 6),
(35, '2025-10-19 17:49:33.809065', '2025-10-19 17:49:33.809065', 31, 'ko ngày gì', '2025-01-31', 0, NULL);


INSERT INTO payment_methods (method_name, payment_code, is_active, created_at, updated_at, image_url)
VALUES
    ('Thanh toán quét mã QR', 'VNPAYQR', 1, NOW(), NOW(),'/vnpay-logo.png'),
    ('Thẻ ATM - Tài khoản ngân hàng nội địa', 'VNBANK', 1, NOW(), NOW()'/vnpay-logo.png'),
    ('Thẻ thanh toán quốc tế', 'INTCARD', 1, NOW(), NOW(),'/vnpay-logo.png'),
    ('Tiền mặt', 'CASH', 1, NOW(), NOW(),'/cash.png');
