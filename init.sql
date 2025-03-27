-- 데이터베이스 선택
USE ringle;

-- 사용자 테이블
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       name VARCHAR(255) NOT NULL,
                       role VARCHAR(20),
                       created_at DATETIME NOT NULL,
                       updated_at DATETIME
);

-- 수업 테이블
CREATE TABLE lessons (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         student_id BIGINT NOT NULL,
                         tutor_id BIGINT NOT NULL,
                         start_time DATETIME NOT NULL,
                         end_time DATETIME NOT NULL,
                         duration_type VARCHAR(10) NOT NULL,
                         status VARCHAR(20) NOT NULL,
                         created_at DATETIME NOT NULL,
                         CONSTRAINT fk_lessons_student FOREIGN KEY (student_id) REFERENCES users(id),
                         CONSTRAINT fk_lessons_tutor FOREIGN KEY (tutor_id) REFERENCES users(id)
);

-- 수업 가능 시간 테이블
CREATE TABLE availabilities (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                tutor_id BIGINT,
                                start_time DATETIME,
                                end_time DATETIME,
                                is_booked BOOLEAN,
                                created_at DATETIME,
                                CONSTRAINT fk_availabilities_tutor FOREIGN KEY (tutor_id) REFERENCES users(id)
);
