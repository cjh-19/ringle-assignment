package com.ringle.domain.lesson.repository;

import com.ringle.domain.lesson.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 수업을 위한 JPA Repository
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    // 특정 학생이 신청한 수업을 시간 역순으로 조회
    List<Lesson> findByStudentIdOrderByStartTimeDesc(Long studentId);
}
