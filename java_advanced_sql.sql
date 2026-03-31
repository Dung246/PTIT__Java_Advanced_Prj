-- =========================
-- RESET DATABASE
-- =========================
DROP DATABASE IF EXISTS meeting_management;
CREATE DATABASE meeting_management;
USE meeting_management;

-- =========================
-- USERS
-- =========================
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,

    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,

    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),

    department VARCHAR(100),

    role ENUM('EMPLOYEE', 'SUPPORT', 'ADMIN') NOT NULL DEFAULT 'EMPLOYEE',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_username ON users(username);

-- =========================
-- ROOMS
-- =========================
CREATE TABLE rooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    capacity INT NOT NULL,
    location VARCHAR(100),
    description TEXT
);

-- =========================
-- EQUIPMENT
-- =========================
CREATE TABLE equipment (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    total_quantity INT NOT NULL CHECK (total_quantity >= 0),
    available_quantity INT NOT NULL CHECK (available_quantity >= 0),
    status ENUM('AVAILABLE', 'MAINTENANCE') DEFAULT 'AVAILABLE'
);

-- =========================
-- SERVICES
-- =========================
CREATE TABLE services (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    description TEXT
);

-- =========================
-- BOOKINGS (FIX FULL)
-- =========================
CREATE TABLE bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    room_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    participant_count INT NOT NULL CHECK (participant_count > 0),

    -- trạng thái duyệt (admin)
    status ENUM(
        'PENDING','APPROVED','REJECTED'
    ) DEFAULT 'PENDING',

    -- trạng thái chuẩn bị (support)
    support_status ENUM(
        'PREPARING','READY','MISSING'
    ) DEFAULT 'PREPARING',

    support_staff_id INT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    FOREIGN KEY (support_staff_id) REFERENCES users(id),

    CHECK (end_time > start_time)
);

CREATE INDEX idx_booking_room_time 
ON bookings(room_id, start_time, end_time);

-- =========================
-- BOOKING SERVICES
-- =========================
CREATE TABLE booking_services (
    booking_id INT,
    service_id INT,
    quantity INT DEFAULT 1 CHECK (quantity > 0),

    PRIMARY KEY (booking_id, service_id),

    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES services(id)
);

-- =========================
-- BOOKING EQUIPMENT
-- =========================
CREATE TABLE booking_equipment (
    booking_id INT,
    equipment_id INT,
    quantity INT DEFAULT 1 CHECK (quantity > 0),

    PRIMARY KEY (booking_id, equipment_id),

    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (equipment_id) REFERENCES equipment(id)
);

-- =========================
-- FEEDBACK
-- =========================
CREATE TABLE feedbacks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_id INT NOT NULL,
    user_id INT NOT NULL,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- =========================
-- USER SCHEDULE
-- =========================
CREATE TABLE user_schedules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    note VARCHAR(255),

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CHECK (end_time > start_time)
);

-- =========================
-- TRIGGER: TRỪ THIẾT BỊ
-- =========================
DELIMITER $$

CREATE TRIGGER trg_after_booking_equipment
AFTER INSERT ON booking_equipment
FOR EACH ROW
BEGIN
    UPDATE equipment
    SET available_quantity = available_quantity - NEW.quantity
    WHERE id = NEW.equipment_id;
END$$

-- =========================
-- TRIGGER: HOÀN THIẾT BỊ
-- =========================
CREATE TRIGGER trg_return_equipment
AFTER DELETE ON booking_equipment
FOR EACH ROW
BEGIN
    UPDATE equipment
    SET available_quantity = available_quantity + OLD.quantity
    WHERE id = OLD.equipment_id;
END$$

DELIMITER ;

-- =========================
-- VIEW: XEM BOOKING
-- =========================
CREATE VIEW v_booking_detail AS
SELECT 
    b.id,
    u.full_name,
    r.name AS room_name,
    b.start_time,
    b.end_time,
    b.status
FROM bookings b
JOIN users u ON b.user_id = u.id
JOIN rooms r ON b.room_id = r.id;

-- =========================
-- SAMPLE DATA
-- =========================
INSERT INTO users(username, password, role, full_name, email,phone)
VALUES
('Admin','Admin246','ADMIN','Admin','admin@gmail.com','0946475866'),
('Dũng','123456','EMPLOYEE','Trần Anh Dũng','dung@gmail.com','0957464231'),
('Khánh','123456','SUPPORT','Nguyễn Trần Bảo Khánh','khanhtrang@gmail.com','0931245142'),
('Thành','123456','SUPPORT','Nguyễn Tiến Thành','Thanhdepzai@gmail.com','0913204957'),
('Tú','123456','EMPLOYEE','Bàng Trọng Tú','bangdetam@gmail.com','0921624143');



INSERT INTO rooms(name,capacity,location)
VALUES
('Phòng Đào Tạo',10,'Tầng 1'),
('Phòng Thích Nghi',20,'Tầng 2'),
('Phòng Sáng Tạo',10,'Tầng 1'),
('Phòng Liên Hệ',20,'Tầng 2'),
('Phòng Can Đảm',10,'Tầng 1'),
('Phòng Truyền Thống',20,'Tầng 2');

INSERT INTO equipment(name,total_quantity,available_quantity)
VALUES
('Điều hoà',5,5),
('Quạt',10,9),
('Ti vi',2,2),
('Máy chiếu',1,1),
('Laptop',10,10);

INSERT INTO services(name,price)
VALUES
('Nước',10000),
('Đồ ăn vặt',50000),
('Giải lao',14000),
('Chơi game',30000),
('Cafe',20000);

-- =========================
-- SAMPLE BOOKING (QUAN TRỌNG)
-- =========================

-- Booking 1: PENDING (chưa phân công)
INSERT INTO bookings (
    user_id,
    room_id,
    start_time,
    end_time,
    participant_count,
    status,
    support_status,
    support_staff_id
)
VALUES (
    2,
    1,
    '2026-04-01 09:00:00',
    '2026-04-01 11:00:00',
    5,
    'PENDING',
    'PREPARING',
    NULL
);

-- Booking 2: PENDING (khác thời gian để tránh trùng)
INSERT INTO bookings (
    user_id,
    room_id,
    start_time,
    end_time,
    participant_count,
    status,
    support_status,
    support_staff_id
)
VALUES (
    5,
    2,
    '2026-04-02 14:00:00',
    '2026-04-02 16:00:00',
    8,
    'PENDING',
    'PREPARING',
    NULL
);

-- =========================
-- SAMPLE BOOKING - PENDING (CHO ADMIN DUYỆT)
-- =========================

INSERT INTO bookings (user_id, room_id, start_time, end_time, participant_count, status)
VALUES
(2, 1, '2026-04-05 08:00:00', '2026-04-05 10:00:00', 5, 'PENDING'),
(5, 2, '2026-04-05 10:30:00', '2026-04-05 12:00:00', 8, 'PENDING'),
(2, 3, '2026-04-06 13:00:00', '2026-04-06 15:00:00', 6, 'PENDING'),
(5, 4, '2026-04-06 15:30:00', '2026-04-06 17:00:00', 10, 'PENDING'),
(2, 5, '2026-04-07 09:00:00', '2026-04-07 11:00:00', 4, 'PENDING');

