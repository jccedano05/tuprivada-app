
package com.jccv.tuprivadaapp.repository.amenity;

import com.jccv.tuprivadaapp.model.amenity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    List<Amenity> findByCondominiumId(Long condominiumId);
}
