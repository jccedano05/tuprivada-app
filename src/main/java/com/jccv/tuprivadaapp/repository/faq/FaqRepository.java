package com.jccv.tuprivadaapp.repository.faq;

import com.jccv.tuprivadaapp.model.faq.Faq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {
    @Query("SELECT f FROM Faq f WHERE f.condominium.id = :condominiumId ORDER BY f.question ASC")
    Page<Faq> findByCondominiumIdOrderByQuestionAsc(@Param("condominiumId") Long condominiumId, Pageable pageable);
}
