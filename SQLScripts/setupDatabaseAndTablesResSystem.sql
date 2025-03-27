-- create database
DROP DATABASE IF EXISTS reservation_system;
CREATE DATABASE reservation_system;

-- select database
USE reservation_system;

-- customers
CREATE TABLE `customers` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `firstName` varchar(100) NOT NULL,
    `lastName` varchar(100) NOT NULL,
    `email` varchar(100) NOT NULL,
    `vehicle_registration_number` varchar(10) DEFAULT NULL,
    `phone_number` varchar(15) DEFAULT NULL,
    `password` varchar(100) NOT NULL,
    `security_question_id` int(11) NOT NULL,
    `security_question` text NOT NULL,
    `security_answer` text NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `customers_UN` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE customers ADD COLUMN loyalty_tokens INT DEFAULT 0;
-- promotions
CREATE TABLE `promotions` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `description` text,
    `discountPercentage` decimal(5, 2) NOT NULL,
    `validFrom` date NOT NULL,
    `validTo` date NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- workshops
CREATE TABLE `workshops` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `name` varchar(255) NOT NULL,
    `location` varchar(255) NOT NULL,
    `contactInfo` varchar(255) NOT NULL,
    PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- technicians
CREATE TABLE `technicians` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `name` varchar(100) NOT NULL,
    `certifications` varchar(100) NOT NULL,
    `experience` int(3) NOT NULL,
    `workshop_id` int(11) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `technicians_FK` (`workshop_id`),
    CONSTRAINT `technicians_FK` FOREIGN KEY (`workshop_id`) REFERENCES `workshops` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- services
CREATE TABLE `services` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `name` varchar(100) NOT NULL,
    `duration` int(11) NOT NULL,
    `price` decimal(10, 2) NOT NULL,
    `description` text,
    `promotion_id` int(11) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `services_FK` (`promotion_id`),
    CONSTRAINT `services_FK` FOREIGN KEY (`promotion_id`) REFERENCES `promotions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- workshop_services
CREATE TABLE `workshop_services` (
    `workshop_id` int(11) NOT NULL,
    `service_id` int(11) NOT NULL,
    PRIMARY KEY (`workshop_id`, `service_id`),
    KEY `workshop_services_FK_1` (`workshop_id`),
    KEY `workshop_services_FK_2` (`service_id`),
    CONSTRAINT `workshop_services_FK_1` FOREIGN KEY (`workshop_id`) REFERENCES `workshops` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `workshop_services_FK_2` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- reviews
CREATE TABLE `reviews` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `rating` tinyint CHECK (rating BETWEEN 1 AND 5),  -- Rating out of 5
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Timestamp for review creation
    `comment` text DEFAULT NULL,
    `customer_id` int(11) NOT NULL,
    `service_id` int(11) NOT NULL,
    `workshop_id` int(11) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `reviews_FK` (`customer_id`),
    KEY `reviews_FK_1` (`service_id`),
    KEY `reviews_FK_2` (`workshop_id`),
    CONSTRAINT `reviews_FK` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `reviews_FK_1` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `reviews_FK_2` FOREIGN KEY (`workshop_id`) REFERENCES `workshops` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- appointments
CREATE TABLE `appointments` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `customer_id` int(11) NOT NULL,
    `workshop_id` int(11) NOT NULL,
    `service_id` int(11) NOT NULL,
    `technician_id` int(11) NOT NULL,
    `scheduledTime` datetime NOT NULL ,
    `createdAt` datetime NOT NULL,
    `modifiedAt` datetime NOT NULL,
    `appointmentStatus` varchar(100) NOT NULL,
    `paymentMethod` varchar(100) NOT NULL,
    `paymentStatus` varchar(100) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `appointments_FK` (`customer_id`),
    KEY appointments_FK_1 (workshop_id),
    KEY `appointments_FK_2` (`service_id`),
    KEY appointments_FK_3 (technician_id),
    CONSTRAINT `appointments_FK` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT appointments_FK_1 FOREIGN KEY (workshop_id) REFERENCES workshops (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `appointments_FK_2` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT appointments_FK_3 FOREIGN KEY (technician_id) REFERENCES technicians (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- maintenanceReminders
CREATE TABLE `maintenanceReminders` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `intervalMonths` int(3) NOT NULL,
    `serviceType` varchar(100) NOT NULL,
    `nextReminder` date NOT NULL,
    `customer_id` int(11) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `maintenanceReminders_FK` (`customer_id`),
    CONSTRAINT `maintenanceReminders_FK` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- qrCodes
CREATE TABLE `qrCodes` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `codeData` varchar(100) NOT NULL,
    `generationTime` datetime NOT NULL,
    `appointment_id` int(11) NOT NULL,
    PRIMARY KEY (`id`),
    KEY `qrCodes_FK` (`appointment_id`),
    CONSTRAINT `qrCodes_FK` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- INSERT DATA
-- TODO: insert SQL statements to fill tables with exemplary DATA

INSERT INTO reservation_system.customers (firstName, lastName, email, vehicle_registration_number, phone_number, password, security_question_id, security_question, security_answer) VALUES
('Maximilian','Maier','max.m@gmail.com', 'ABC123', '1234567890', 'yxcvuioq', 1,  'What is your pet\'s name?', 'Fluffy'),
('Karla','Arrowsmith','karla.a@gmx.de', 'XYZ789', '9876543210', 'kl,.p=q§we', 2,  'What is your mother\'s maiden name?', 'Smith'),
('Isabel','Raap','raap.i@web.de', 'DEF456', '4567891230', 'vbnmopiu', 3, 'What was the name of your first school?', 'Greenwood'),
('Ted','Runkel','t.r@gmail.com', 'GHI012', '3216549870', 'erzte89&', 4, 'What city were you born in?', 'New York'),
('Josephine','Lukowski','josie.luko@gmx.de', 'LMN345', '1597534682', 'qwerasdf', 5, 'What is your favorite food?', 'Pizza'),
('Hans','Massaro','hans.m@web.de', 'OPQ678', '1478523690', 'qwerqwer', 6,'What is your favorite color?', 'Blue'),
('Syble','Hocking','hocking.s@gmail.com', 'RST910', '7894561230', 'winter20', 7, 'What was your childhood nickname?', 'Toto'),
('Crissy','Deaton','d.crissy@gmx.de', 'UVW234', '3698521470', 'summer21', 8, 'What is your favorite movie?', 'Inception'),
('Edward','Vanmeter','van.ed@web.de', 'XYZ567', '2581473690', 'vfrzum21?', 9, 'What is the name of your best friend?', 'Omar'),
('Jinny','Toews','toews.j@gmail.com', 'BCD890', '1237894560', 'qwec16%9', 10, 'What was your dream job as a child?', 'Astronaut');


INSERT INTO reservation_system.promotions (description, discountPercentage, validFrom, validTo) VALUES
('New Year Discount', 15.00, '2025-01-01', '2025-01-15'),
('Winter Service Offer', 20.00, '2025-01-10', '2025-02-28'),
('Free Pickup Promo', 10.00, '2025-03-01', '2025-03-15'),
('Spring Maintenance Sale', 25.00, '2025-03-20', '2025-04-10'),
('Summer Tune-Up Discount', 30.00, '2025-06-01', '2025-06-30'),
('Loyalty Program Bonus', 5.00, '2025-07-01', '2025-07-15'),
('Early Bird Offer', 10.00, '2025-08-01', '2025-08-10'),
('Back-to-School Special', 15.00, '2025-09-01', '2025-09-15'),
('Festive Season Discount', 20.00, '2025-12-01', '2025-12-25'),
('Year-End Clearance', 50.00, '2025-12-26', '2025-12-31');


INSERT INTO reservation_system.workshops (name, location, contactInfo) VALUES
('Smiths Vehicle Solutions', '123 Elm Street', 'smith@vehicles.com'),
('QuickFix Auto', '456 Oak Avenue', 'contact@quickfix.com'),
('DrivePro Garage', '789 Pine Lane', 'info@drivepro.com'),
('CarCare Center', '101 Maple Boulevard', 'support@carcare.com'),
('AutoMasters', '202 Birch Drive', 'help@automasters.com'),
('Prime Auto Shop', '303 Cedar Road', 'service@primeauto.com'),
('All-In-One Garage', '404 Spruce Street', 'garage@allinone.com'),
('FastTrack Repairs', '505 Fir Way', 'repairs@fasttrack.com'),
('Elite Mechanics', '606 Redwood Street', 'info@elitemechanics.com'),
('Speedy Fixers', '707 Aspen Avenue', 'fix@speedy.com');


INSERT INTO reservation_system.technicians (name, certifications, experience, workshop_id) VALUES
('John Smith', 'ASE Certified', 5, 1),
('Alice Johnson', 'Master Mechanic', 10, 2),
('Bob Williams', 'Engine Specialist', 7, 3),
('Emily Davis', 'Brake Specialist', 4, 4),
('Michael Brown', 'Electrical Systems Expert', 8, 5),
('Sarah Wilson', 'Transmission Specialist', 6, 6),
('James Taylor', 'General Mechanic', 3, 7),
('Jessica White', 'HVAC Systems Certified', 9, 8),
('David Martin', 'Suspension Expert', 6, 9),
('Laura Moore', 'Hybrid Vehicle Technician', 7, 10);

INSERT INTO reservation_system.technicians (name, certifications, experience, workshop_id) VALUES
('David Anderson', 'ASE Master Technician, BMW Certified', 12, 1),
('Maria Rodriguez', 'Mercedes-Benz Certified, Hybrid Specialist', 8, 1),
('Thomas Chen', 'ASE Certified, Electric Vehicle Expert', 6, 2),
('Rachel Thompson', 'Audi Master Guild Technician', 15, 2),
('Kevin O''Brien', 'Volvo Certified Master, ASE Advanced Level', 10, 3),
('Sophie Martin', 'Toyota Master Diagnostic Technician', 7, 3),
('Hassan Ahmed', 'BMW Master Technician, EV Certified', 9, 4),
('Anna Kowalski', 'ASE Master Certified, Porsche Specialist', 11, 4),
('Carlos Mendoza', 'Mercedes-Benz Master Guild, Hybrid Expert', 14, 5),
('Emma Wilson', 'Tesla Certified Technician, ASE Certified', 5, 5),
('Marcus Johnson', 'Volkswagen Master Technician', 8, 6),
('Priya Patel', 'ASE Master Certified, Asian Imports Specialist', 13, 6),
('Lucas Schmidt', 'BMW Diagnostics Expert, Hybrid Certified', 7, 7),
('Sophia Chen', 'Electric Vehicle Specialist, ASE Certified', 6, 7),
('Ryan Murphy', 'Audi Factory Trained, Performance Specialist', 9, 8),
('Isabella Santos', 'Mercedes-Benz Systems Expert', 11, 8),
('William Turner', 'ASE Master Tech, European Specialist', 15, 9),
('Nina Petrova', 'Hybrid Systems Expert, Multi-Brand Certified', 8, 9),
('Mohammed Al-Said', 'Performance Tuning Specialist, ASE Certified', 10, 10),
('Grace Kim', 'Electric/Hybrid Master Technician', 7, 10);

INSERT INTO reservation_system.services (name, duration, price, description, promotion_id) VALUES
('Oil Change', 45, 49.99, 'Change the engine oil and replace the filter.', 2),
('Tire Rotation', 60, 39.99, 'Rotate tires to ensure even wear and prolong their lifespan.', NULL),
('Brake Inspection', 30, 29.99, 'Inspect the brake pads and discs for wear and tear.', 3),
('Battery Replacement', 45, 89.99, 'Replace the old car battery with a new one.', NULL),
('AC System Recharge', 60, 79.99, 'Recharge the air conditioning system to restore cooling efficiency.', 4),
('Transmission Flush', 90, 119.99, 'Flush the transmission fluid to keep the system running smoothly.', NULL),
('Windshield Replacement', 120, 199.99, 'Replace a cracked or damaged windshield with a new one.', 5),
('Wheel Alignment', 75, 69.99, 'Align the wheels to improve handling and tire longevity.', NULL),
('Timing Belt Replacement', 180, 249.99, 'Replace the timing belt to avoid engine damage.', 6),
('Full Service Check-up', 150, 149.99, 'Comprehensive service including fluid checks, tire inspection, and more.', 7);

-- service(8) offered only at workshop(1), service(9) offered only at workshop(2), service(10) offered only at workshop(3)
INSERT INTO reservation_system.workshop_services (workshop_id, service_id) VALUES
(1, 8),
(2, 9),
(3, 10);

-- service(1) offered by 7 workshops
INSERT INTO reservation_system.workshop_services (workshop_id, service_id) VALUES
(4, 1), (5, 1), (6, 1), (7, 1), (8, 1), (9, 1), (10, 1);

-- services(2 and 3) offered by 6 workshops
INSERT INTO reservation_system.workshop_services (workshop_id, service_id) VALUES
(2, 3), (2, 2), (3, 3), (3, 2), (4, 3), (4, 2),
(5, 3), (5, 2), (6, 3), (6, 2), (7, 3), (7, 2);

-- services(4 and 5) offered by 4 workshops
INSERT INTO reservation_system.workshop_services (workshop_id, service_id) VALUES
(3, 5), (3, 4), (4, 5), (4, 4),
(5, 5), (5, 4), (6, 5), (6, 4);

-- services(6 and 7) offered by 2 workshops
INSERT INTO reservation_system.workshop_services (workshop_id, service_id) VALUES
(1, 7), (1, 6),
(2, 7), (2, 6);


INSERT INTO reservation_system.reviews (rating, comment, customer_id, service_id, workshop_id) VALUES
(5, 'Excellent service! Highly recommend.', 1, 2, 3),
(4, 'Great experience, but the waiting time was long.', 2, 3, 1),
(3, 'Average service. Could be improved.', 3, 1, 2),
(2, 'Not satisfied. The service quality was poor.', 4, 4, 3),
(1, 'Terrible experience. Would not recommend.', 5, 5, 1),
(4, 'Good service overall, but a bit expensive.', 6, 2, 2),
(5, 'Amazing! The staff was very friendly.', 7, 3, 3),
(3, 'It was okay. Nothing special.', 8, 4, 1),
(2, 'I had issues with the workshop.', 9, 5, 2),
(5, 'Fantastic job! Worth every penny.', 10, 1, 3);


INSERT INTO reservation_system.appointments(customer_id, workshop_id, service_id, technician_id, scheduledTime, createdAt, modifiedAt, appointmentStatus, paymentMethod, paymentStatus) VALUES
(1, 8, 1, 1, '2025-01-15 10:30', '2025-01-13 09:20', '2025-01-13 09:20', TRUE, 'Cash', FALSE),
(2, 7, 2, 2, '2025-01-16 13:15', '2025-01-14 09:00', '2025-01-14 09:00', FALSE, 'Cash', FALSE),
(3, 2, 3, 3, '2025-01-17 09:30', '2025-01-15 11:10', '2025-01-15 11:30', TRUE, 'Debit Card', TRUE),
(4, 5, 4, 4, '2025-01-18 11:25', '2025-01-16 12:10', '2025-01-16 12:30', TRUE, 'Credit Card', TRUE),
(5, 6, 5, 5, '2025-01-19 13:00', '2025-01-17 08:30', '2025-01-17 09:00', FALSE, 'PayPal', FALSE),
(6, 2, 6, 6, '2025-01-20 10:45', '2025-01-18 13:00', '2025-01-18 13:15', TRUE, 'PayPal', FALSE),
(7, 1, 7, 7, '2025-01-21 15:00', '2025-01-19 14:00', '2025-01-19 14:30', TRUE, 'Debit Card', TRUE),
(8, 1, 8, 8, '2025-01-22 09:45', '2025-01-20 10:10', '2025-01-20 10:30', TRUE, 'Cash', TRUE),
(9, 2, 9, 9, '2025-01-23 12:10', '2025-01-21 11:00', '2025-01-21 11:30', TRUE, 'PayPal', FALSE),
(10, 3, 10, 10, '2025-01-24 14:15', '2025-01-22 10:30', '2025-01-22 11:20', FALSE, 'Cash', FALSE);


INSERT INTO reservation_system.maintenanceReminders (intervalMonths, serviceType, nextReminder, customer_id) VALUES
(6, 'Oil Change', '2025-07-01', 1),
(12, 'Tire Rotation', '2025-12-01', 2),
(3, 'Battery Check', '2025-04-01', 3),
(6, 'Brake Inspection', '2025-06-15', 4),
(12, 'Full Service', '2025-12-15', 5),
(6, 'Coolant Flush', '2025-08-01', 6),
(3, 'Air Filter Replacement', '2025-03-01', 7),
(12, 'Transmission Check', '2025-11-20', 8),
(6, 'Wheel Alignment', '2025-06-10', 9),
(3, 'Wiper Blade Replacement', '2025-03-15', 10);


INSERT INTO reservation_system.qrCodes (codeData, generationTime, appointment_id) VALUES
('https://example.com/appointment/1', '2025-01-01 10:00:00', 1),
('https://example.com/appointment/2', '2025-01-02 11:30:00', 2),
('https://example.com/appointment/3', '2025-01-03 14:00:00', 3),
('https://example.com/appointment/4', '2025-01-04 09:15:00', 4),
('https://example.com/appointment/5', '2025-01-05 16:45:00', 5),
('https://example.com/appointment/6', '2025-01-06 13:20:00', 6),
('https://example.com/appointment/7', '2025-01-07 12:00:00', 7),
('https://example.com/appointment/8', '2025-01-08 15:30:00', 8),
('https://example.com/appointment/9', '2025-01-09 10:45:00', 9),
('https://example.com/appointment/10', '2025-01-10 14:10:00', 10);
