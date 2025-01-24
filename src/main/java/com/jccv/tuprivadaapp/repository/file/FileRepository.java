package com.jccv.tuprivadaapp.repository.file;

import com.jccv.tuprivadaapp.model.file.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByCondominiumId(Long condominiumId);  // Cambiado a List en lugar de Page

}
