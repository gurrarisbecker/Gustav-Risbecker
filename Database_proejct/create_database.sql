-- ================================
-- 1. COURSE & INSTANCE
-- ================================

CREATE TABLE course_layout (
    course_layout_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    course_code      VARCHAR(100) NOT NULL,
    course_name      VARCHAR(100) NOT NULL,
    hp               INT          NOT NULL,
    min_students     INT          NOT NULL,
    max_students     INT          NOT NULL,
    valid_from_date  DATE,
    valid_to_date    DATE
);

CREATE TABLE course_instance (
    instance_id      INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    study_period     VARCHAR(100),
    num_students     INT,
    course_layout_id INT          NOT NULL,
    study_year       INT,
    FOREIGN KEY (course_layout_id) REFERENCES course_layout (course_layout_id)
);

-- ================================
-- 2. DEPARTMENT / JOB TITLE
-- ================================

CREATE TABLE department (
    department_id   INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    department_name VARCHAR(100) NOT NULL,
    manager         VARCHAR(100)
);

CREATE TABLE job_title (
    job_title_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    job_title    VARCHAR(100) NOT NULL
);

-- ================================
-- 3. PERSON / PHONE
-- ================================

CREATE TABLE person (
    person_id       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    personal_number VARCHAR(100) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    address         CHAR(100),
    last_name       CHAR(100)
);

CREATE TABLE phone_number (
    phone_id     INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    phone_number VARCHAR(100) NOT NULL
);

CREATE TABLE person_number (
    person_id INT NOT NULL,
    phone_id  INT NOT NULL,
    PRIMARY KEY (person_id, phone_id),
    FOREIGN KEY (person_id) REFERENCES person (person_id),
    FOREIGN KEY (phone_id)  REFERENCES phone_number (phone_id)
);

-- ================================
-- 4. SKILLS
-- ================================

CREATE TABLE skill_set (
    skill_set_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    skill_set    VARCHAR(100) NOT NULL
);

-- ================================
-- 5. TEACHER RULES (lookup)
-- ================================
-- Defines standardised workload rules applied to teachers,
-- including maximum allowed courses per study period and
-- the factors used to calculate administrative and examination hours.

CREATE TABLE teacher_rules (
    rule_id             INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    max_course_per_period INT        NOT NULL,
    admin_factor        NUMERIC(10,2) NOT NULL,
    examination_factor  NUMERIC(10,2) NOT NULL
);

-- ================================
-- 6. EMPLOYEE & EMPLOYEE_SKILL
-- ================================

CREATE TABLE employee (
    employment_id     VARCHAR(100) PRIMARY KEY,
    person_id         INT          NOT NULL,
    department_id     INT          NOT NULL,
    job_title_id      INT          NOT NULL,
    supervisor_manager VARCHAR(100),
    personal_number   VARCHAR(100),
    rule_id           INT,                   -- FK till teacher_rules

    FOREIGN KEY (person_id)     REFERENCES person (person_id),
    FOREIGN KEY (department_id) REFERENCES department (department_id),
    FOREIGN KEY (job_title_id)  REFERENCES job_title (job_title_id),
    FOREIGN KEY (rule_id)       REFERENCES teacher_rules (rule_id)
);

CREATE TABLE employee_skill (
    skill_set_id  INT          NOT NULL,
    employment_id VARCHAR(100) NOT NULL,
    PRIMARY KEY (skill_set_id, employment_id),
    FOREIGN KEY (skill_set_id)  REFERENCES skill_set (skill_set_id),
    FOREIGN KEY (employment_id) REFERENCES employee (employment_id)
);

-- ================================
-- 7. TEACHING ACTIVITIES & PLANNED ACTIVITIES
-- ================================

CREATE TABLE teaching_activity (
    teaching_activity_id INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    activity_name        VARCHAR(100) NOT NULL,
    factor               INT          -- multiplier for teaching hours
);

CREATE TABLE planned_activity (
    planned_activity_id  INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    planned_hours        INT,
    instance_id          INT NOT NULL,
    teaching_activity_id INT,
    FOREIGN KEY (instance_id)          REFERENCES course_instance (instance_id),
    FOREIGN KEY (teaching_activity_id) REFERENCES teaching_activity (teaching_activity_id)
);

CREATE TABLE planned_employee (
    employment_id       VARCHAR(100) NOT NULL,
    planned_activity_id INT          NOT NULL,
    PRIMARY KEY (employment_id, planned_activity_id),
    FOREIGN KEY (employment_id)       REFERENCES employee (employment_id),
    FOREIGN KEY (planned_activity_id) REFERENCES planned_activity (planned_activity_id)
);

-- ================================
-- 8. SALARY HISTORY
-- ================================

CREATE TABLE salary_history (
    salary_id       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    salary_amount   INT,
    valid_from_date DATE,
    valid_to_date   DATE,
    version_number  INT,
    employment_id   VARCHAR(100) NOT NULL,
    FOREIGN KEY (employment_id) REFERENCES employee (employment_id)
);
