-- Room Types
INSERT INTO room_types (name, description, created_at, updated_at,active)
VALUES ('Standard', 'Phòng chiếu tiêu chuẩn', NOW(), NOW(),1),
       ('VIP', 'Phòng chiếu VIP cao cấp', NOW(), NOW(),1),
       ('IMAX', 'Phòng chiếu IMAX', NOW(), NOW(),1);

-- Seat Types
INSERT INTO seat_types (name, description, created_at, updated_at,active)
VALUES ('Standard', 'Ghế tiêu chuẩn', NOW(), NOW(),1),
       ('VIP', 'Ghế VIP', NOW(), NOW(),1);


-- Rooms
INSERT INTO rooms (name, room_type_id, status, created_at, updated_at,rows_count, columns_count)
VALUES ('Phòng 1', 1, 'ACTIVE', NOW(), NOW(),10,12),
       ('Phòng 2', 1, 'ACTIVE', NOW(), NOW(),10,12),
       ('Phòng 3', 2, 'ACTIVE', NOW(), NOW(),10,12),
       ('Phòng 4', 3, 'ACTIVE', NOW(), NOW(),10,12),
       ('Phòng 5', 1, 'ACTIVE', NOW(), NOW(),10,12),
       ('Phòng VIP 1', 2, 'ACTIVE', NOW(), NOW(),10,12),
       ('Phòng IMAX 1', 3, 'ACTIVE', NOW(), NOW(),10,12);


-- Seats for Room 1 (Standard - 10x8 = 80 seats)
INSERT INTO seats ( seat_row, seat_column, status,  room_id, seat_type_id, created_at, updated_at)
VALUES
-- Row A (1-10)
('A', '1', 'AVAILABLE', 1, 1, NOW(), NOW()),
( 'A', '2', 'AVAILABLE',  1, 1, NOW(), NOW()),
( 'A', '3', 'AVAILABLE',  1, 1, NOW(), NOW()),
( 'A', '4', 'AVAILABLE',  1, 1, NOW(), NOW()),
( 'A', '5', 'AVAILABLE',  1, 1, NOW(), NOW()),
( 'A', '6', 'AVAILABLE', 1, 1, NOW(), NOW()),
( 'A', '7', 'AVAILABLE',  1, 1, NOW(), NOW()),
( 'A', '8', 'AVAILABLE',  1, 1, NOW(), NOW()),
('A', '9', 'AVAILABLE', 1, 1, NOW(), NOW()),
('A', '10', 'AVAILABLE',  1, 1, NOW(), NOW()),
-- Row B (1-10)
('B', '1', 'AVAILABLE',  1, 1, NOW(), NOW()),
('B', '2', 'AVAILABLE', 1, 1, NOW(), NOW()),
('B', '3', 'AVAILABLE', 1, 1, NOW(), NOW()),
('B', '4', 'AVAILABLE', 1, 1, NOW(), NOW()),
( 'B', '5', 'AVAILABLE', 1, 1, NOW(), NOW()),
( 'B', '6', 'AVAILABLE', 1, 1, NOW(), NOW()),
( 'B', '7', 'AVAILABLE', 1, 1, NOW(), NOW()),
( 'B', '8', 'AVAILABLE', 1, 1, NOW(), NOW()),
( 'B', '9', 'AVAILABLE', 1, 1, NOW(), NOW()),
('B', '10', 'AVAILABLE', 1, 1, NOW(), NOW()),
-- Row C (1-10) - Some seats booked for testing
( 'C', '1', 'BLOCKED', 1, 1, NOW(), NOW()),
('C', '2', 'BLOCKED', 1, 1, NOW(), NOW()),
('C', '3', 'AVAILABLE',  1, 1, NOW(), NOW()),
( 'C', '4', 'AVAILABLE', 1, 1, NOW(), NOW()),
( 'C', '5', 'AVAILABLE', 1, 1, NOW(), NOW()),
( 'C', '6', 'AVAILABLE',  1, 1, NOW(), NOW()),
( 'C', '7', 'AVAILABLE', 1, 1, NOW(), NOW()),
('C', '8', 'AVAILABLE', 1, 1, NOW(), NOW()),
( 'C', '9', 'AVAILABLE',  1, 1, NOW(), NOW()),
( 'C', '10', 'AVAILABLE', 1, 1, NOW(), NOW()),
-- Row D (1-10)
( 'D', '1', 'AVAILABLE',  1, 1, NOW(), NOW()),
('D', '2', 'AVAILABLE', 1, 1, NOW(), NOW()),
( 'D', '3', 'AVAILABLE', 1, 1, NOW(), NOW()),
('D', '4', 'AVAILABLE',  1, 1, NOW(), NOW()),
('D', '5', 'AVAILABLE',  1, 1, NOW(), NOW()),
( 'D', '6', 'AVAILABLE',  1, 1, NOW(), NOW()),
( 'D', '7', 'AVAILABLE',  1, 1, NOW(), NOW()),
('D', '8', 'AVAILABLE',  1, 1, NOW(), NOW()),
( 'D', '9', 'AVAILABLE',  1, 1, NOW(), NOW()),
( 'D', '10', 'AVAILABLE', 1, 1, NOW(), NOW()),
-- Row E (1-10) - VIP seats
('E', '1', 'AVAILABLE', 1, 2, NOW(), NOW()),
( 'E', '2', 'AVAILABLE',  1, 2, NOW(), NOW()),
('E', '3', 'AVAILABLE',  1, 2, NOW(), NOW()),
('E', '4', 'AVAILABLE',  1, 2, NOW(), NOW()),
( 'E', '5', 'AVAILABLE', 1, 2, NOW(), NOW()),
( 'E', '6', 'AVAILABLE',  1, 2, NOW(), NOW()),
('E', '7', 'AVAILABLE',  1, 2, NOW(), NOW()),
('E', '8', 'AVAILABLE',  1, 2, NOW(), NOW()),
('E', '9', 'AVAILABLE', 1, 2, NOW(), NOW()),
( 'E', '10', 'AVAILABLE',  1, 2, NOW(), NOW()),
-- Row F (1-10)
('F', '1', 'AVAILABLE', 1, 1, NOW(), NOW()),
('F', '2', 'AVAILABLE',  1, 1, NOW(), NOW()),
('F', '3', 'AVAILABLE', 1, 1, NOW(), NOW()),
('F', '4', 'AVAILABLE', 1, 1, NOW(), NOW()),
('F', '5', 'AVAILABLE', 1, 1, NOW(), NOW()),
('F', '6', 'AVAILABLE',  1, 1, NOW(), NOW()),
( 'F', '7', 'AVAILABLE',  1, 1, NOW(), NOW()),
('F', '8', 'AVAILABLE',  1, 1, NOW(), NOW()),
('F', '9', 'AVAILABLE', 1, 1, NOW(), NOW()),
('F', '10', 'AVAILABLE', 1, 1, NOW(), NOW()),
-- Row G (1-10)
( 'G', '1', 'AVAILABLE',  1, 1, NOW(), NOW()),
('G', '2', 'AVAILABLE', 1, 1, NOW(), NOW()),
('G', '3', 'AVAILABLE', 1, 1, NOW(), NOW()),
('G', '4', 'AVAILABLE',  1, 1, NOW(), NOW()),
( 'G', '5', 'AVAILABLE', 1, 1, NOW(), NOW()),
( 'G', '6', 'AVAILABLE', 1, 1, NOW(), NOW()),
( 'G', '7', 'AVAILABLE',  1, 1, NOW(), NOW()),
( 'G', '8', 'AVAILABLE',  1, 1, NOW(), NOW()),
('G', '9', 'AVAILABLE',  1, 1, NOW(), NOW()),

('G', '10', 'AVAILABLE', 1, 1, NOW(), NOW()),
-- Row H (1-10)
('H', '1', 'AVAILABLE', 1, 1, NOW(), NOW()),
('H', '2', 'AVAILABLE', 1, 1, NOW(), NOW()),
('H', '3', 'AVAILABLE', 1, 1, NOW(), NOW()),

( 'H', '4', 'AVAILABLE',  1, 1, NOW(), NOW()),
( 'H', '5', 'AVAILABLE',  1, 1, NOW(), NOW()),
( 'H', '6', 'AVAILABLE', 1, 1, NOW(), NOW()),
( 'H', '7', 'AVAILABLE',  1, 1, NOW(), NOW()),
('H', '8', 'AVAILABLE',  1, 1, NOW(), NOW()),
('H', '9', 'AVAILABLE',  1, 1, NOW(), NOW()),
('H', '10', 'AVAILABLE',  1, 1, NOW(), NOW());