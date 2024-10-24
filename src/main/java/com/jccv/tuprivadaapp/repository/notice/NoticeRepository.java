package com.jccv.tuprivadaapp.repository.notice;

import com.jccv.tuprivadaapp.model.notice.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    //NO TRAE LA LISTA ORDENADA DE LA FECHA MAS ACTUAL A LA MAS VIEJA
    @Query("SELECT n FROM Notice n WHERE n.condominium.id = :condominiumId ORDER BY n.date DESC")
    Page<Notice> findByCondominiumIdOrderByDateDesc(@Param("condominiumId") Long condominiumId, Pageable pageable);



}
