-- FILE: insert_data_fixed.sql
-- Anpassad så att den passar ditt nuvarande databasschema
-- Viktig ändring: teacher_rules.rule_id genereras nu automatiskt (IDENTITY)

-- =========================================
-- COURSE_LAYOUT
-- =========================================
INSERT INTO course_layout (course_code, course_name, hp, min_students, max_students, valid_from_date, valid_to_date) VALUES
('CS101','Introduction to Computer Science',5,10,50,'2023-01-01','2025-12-31'),
('MATH201','Linear Algebra',4,15,60,'2022-09-01','2025-12-31'),
('PHYS101','Physics I',5,10,40,'2023-01-01','2026-06-30'),
('CHEM101','Chemistry Basics',4,12,50,'2023-03-01','2025-12-31'),
('ENG101','English Literature',3,10,35,'2023-01-01','2025-12-31'),
('HIST201','Modern History',4,10,40,'2023-01-01','2025-12-31'),
('BIO101','Biology I',5,15,45,'2023-02-01','2025-12-31'),
('CS201','Data Structures',6,10,50,'2023-09-01','2026-12-31'),
('MATH301','Probability',4,10,40,'2024-01-01','2027-06-30'),
('PHIL101','Philosophy',3,10,30,'2023-01-01','2025-12-31');

-- =========================================
-- DEPARTMENT
-- =========================================
INSERT INTO department (department_name, manager) VALUES
('Computer Science','Alice Johnson'),
('Mathematics','Bob Smith'),
('Physics','Carol White'),
('Chemistry','David Brown'),
('English','Eva Green'),
('History','Frank Black'),
('Biology','Grace Lee'),
('Philosophy','Henry Adams'),
('Economics','Ivy Wilson'),
('Art','Jack Thomas');

-- =========================================
-- JOB_TITLE
-- =========================================
INSERT INTO job_title (job_title) VALUES
('Professor'),
('Assistant Professor'),
('Lecturer'),
('Lab Assistant'),
('Administrator'),
('Researcher'),
('Teaching Assistant'),
('Coordinator'),
('Dean'),
('Tutor');

-- =========================================
-- PERSON
-- =========================================
INSERT INTO person (personal_number, first_name, address, last_name) VALUES
('PN1001','John','123 Main St','Doe'),
('PN1002','Jane','456 Oak Ave','Smith'),
('PN1003','Michael','789 Pine Rd','Brown'),
('PN1004','Emily','321 Maple St','Johnson'),
('PN1005','Daniel','654 Elm St','Davis'),
('PN1006','Sophia','987 Cedar St','Miller'),
('PN1007','William','147 Spruce St','Wilson'),
('PN1008','Olivia','258 Birch St','Moore'),
('PN1009','James','369 Walnut St','Taylor'),
('PN1010','Emma','159 Chestnut St','Anderson');

-- =========================================
-- PHONE_NUMBER
-- =========================================
INSERT INTO phone_number (phone_number) VALUES
('555-1001'),('555-1002'),('555-1003'),('555-1004'),('555-1005'),
('555-1006'),('555-1007'),('555-1008'),('555-1009'),('555-1010');

-- =========================================
-- SKILL_SET
-- =========================================
INSERT INTO skill_set (skill_set) VALUES
('Python'),('C Programming'),('Data Analysis'),('Machine Learning'),
('Teaching'),('Research'),('Project Management'),('Communication'),
('Leadership'),('Lab Work');

-- =========================================
-- TEACHER_RULES (REALISTISKA FAKTORER)
-- OBS: rule_id tas bort så att IDENTITY-kolumnen får generera värdet
-- =========================================
INSERT INTO teacher_rules (max_course_per_period, admin_factor, examination_factor) VALUES
(3,1.80,1.40),
(4,2.00,1.75),
(2,1.50,1.25),
(5,2.30,2.00),
(3,1.90,1.60),
(4,2.10,1.80),
(2,1.60,1.30),
(5,2.40,2.10),
(3,1.85,1.55),
(4,2.05,1.70);

-- =========================================
-- TEACHING_ACTIVITY
-- =========================================
INSERT INTO teaching_activity (activity_name, factor) VALUES
('Lecture',2),
('Seminar',1),
('Lab',3),
('Workshop',2),
('Tutorial',1),
('Online',1),
('Practical',3),
('Group Work',2),
('Project',3),
('Exam',1);

-- =========================================
-- COURSE_INSTANCE (P1 / P2 / P3 / P4)
-- =========================================
INSERT INTO course_instance (study_period, num_students, course_layout_id, study_year) VALUES
('P1',25,1,2024),
('P1',30,2,2024),
('P2',20,3,2024),
('P2',18,4,2024),
('P1',15,5,2024),
('P3',28,6,2024),
('P1',22,7,2024),
('P2',35,8,2024),
('P1',12,9,2024),
('P3',20,10,2024);

-- =========================================
-- EMPLOYEE
-- =========================================
INSERT INTO employee (employment_id, person_id, department_id, job_title_id, supervisor_manager) VALUES
('E001',1,1,1,'Alice Johnson'),
('E002',2,2,2,'Bob Smith'),
('E003',3,3,3,'Carol White'),
('E004',4,4,4,'David Brown'),
('E005',5,5,5,'Eva Green'),
('E006',6,6,6,'Frank Black'),
('E007',7,7,7,'Grace Lee'),
('E008',8,8,8,'Henry Adams'),
('E009',9,9,9,'Ivy Wilson'),
('E010',10,10,10,'Jack Thomas');

-- =========================================
-- EMPLOYEE_SKILL
-- =========================================
INSERT INTO employee_skill (skill_set_id, employment_id) VALUES
(1,'E001'),
(2,'E002'),
(3,'E003'),
(4,'E004'),
(5,'E005'),
(6,'E006'),
(7,'E007'),
(8,'E008'),
(9,'E009'),
(10,'E010');

-- =========================================
-- PERSON_NUMBER
-- =========================================
INSERT INTO person_number (person_id, phone_id) VALUES
(1,1),(2,2),(3,3),(4,4),(5,5),
(6,6),(7,7),(8,8),(9,9),(10,10);

-- =========================================
-- PLANNED_ACTIVITY
-- =========================================
INSERT INTO planned_activity (planned_hours, instance_id, teaching_activity_id) VALUES
(20,1,1),
(15,2,2),
(25,3,3),
(18,4,4),
(12,5,5),
(22,6,6),
(16,7,7),
(30,8,8),
(10,9,9),
(14,10,10);

INSERT INTO planned_activity (planned_hours, instance_id, teaching_activity_id) VALUES
(10,1,5),(8,1,3),(6,1,8),
(12,2,1),(6,2,9),(4,2,10),
(8,3,1),(6,3,2),(5,3,7),
(10,4,3),(6,4,1),(4,4,2),
(8,5,1),(5,5,6),(4,5,2),
(10,6,1),(6,6,8),(5,6,5),
(9,7,3),(7,7,1),(4,7,2),
(14,8,1),(10,8,3),(6,8,5),
(10,9,1),(6,9,2),(5,9,10),
(8,10,1),(6,10,2),(4,10,6);

-- =========================================
-- PLANNED_EMPLOYEE
INSERT INTO planned_employee (employment_id, planned_activity_id) VALUES
-- E001 arbetar på kursinstans 1 och 2
('E001',1),
('E001',2),

-- E002 arbetar på kursinstans 2 och 3
('E002',2),
('E002',3),

-- E003 arbetar på kursinstans 3 och 4
('E003',3),
('E003',4),

-- E004 arbetar på kursinstans 4 och 5
('E004',4),
('E004',5),

-- E005 arbetar på kursinstans 5 och 6
('E005',5),
('E005',6),

-- E006 arbetar på kursinstans 6 och 7
('E006',6),
('E006',7),

-- E007 arbetar på kursinstans 7 och 8
('E007',7),
('E007',8),

-- E008 arbetar på kursinstans 8 och 9
('E008',8),
('E008',9),

-- E009 arbetar på kursinstans 9 och 10
('E009',9),
('E009',10),

-- E010 arbetar på kursinstans 10 och 1
('E010',10),
('E010',1);


-- =========================================
-- SALARY_HISTORY
-- =========================================
INSERT INTO salary_history (salary_amount, valid_from_date, valid_to_date, version_number, employment_id) VALUES
(50000,'2023-01-01','2023-12-31',1,'E001'),
(55000,'2023-01-01','2023-12-31',1,'E002'),
(60000,'2023-01-01','2023-12-31',1,'E003'),
(52000,'2023-01-01','2023-12-31',1,'E004'),
(58000,'2023-01-01','2023-12-31',1,'E005'),
(61000,'2023-01-01','2023-12-31',1,'E006'),
(53000,'2023-01-01','2023-12-31',1,'E007'),
(57000,'2023-01-01','2023-12-31',1,'E008'),
(59000,'2023-01-01','2023-12-31',1,'E009'),
(62000,'2023-01-01','2023-12-31',1,'E010');
