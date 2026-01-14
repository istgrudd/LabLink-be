package com.mbclab.lablink.features.letter;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LetterRepository extends JpaRepository<Letter, String> {
    Optional<Letter> findByLetterNumber(String letterNumber);
    List<Letter> findByLetterType(String letterType);
    List<Letter> findByCategory(String category);
    List<Letter> findByEventId(String eventId);
    
    // Count letters by type, category, year, and month for number generation
    @Query("SELECT COUNT(l) FROM Letter l WHERE l.letterType = ?1 AND l.category = ?2 " +
           "AND YEAR(l.issueDate) = ?3 AND MONTH(l.issueDate) = ?4")
    long countByTypeAndCategoryAndYearAndMonth(String letterType, String category, int year, int month);
}
