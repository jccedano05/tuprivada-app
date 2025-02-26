package com.jccv.tuprivadaapp.repository.auth;

import com.jccv.tuprivadaapp.dto.user.UserDataToShowDto;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.pushNotification.OneSignalPushNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.condominium.id = :condominiumId AND u.role = 'RESIDENT'")
    List<User> findResidentsByCondominiumId(Long condominiumId);

    @Query("SELECT u FROM User u WHERE u.id IN :userIds")
    List<User> findAllByUserIds(List<Long> userIds);

    @Query("SELECT new com.jccv.tuprivadaapp.dto.user.UserDataToShowDto(u.id, u.firstName, u.lastName, u.email, u.bankPersonalReference, u.role) " +
            "FROM User u WHERE u.condominium.id = :condominiumId")
    List<UserDataToShowDto> findUsersByCondominiumId(Long condominiumId);
}
