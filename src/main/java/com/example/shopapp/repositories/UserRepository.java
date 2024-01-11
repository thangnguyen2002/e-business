package com.example.shopapp.repositories;

import com.example.shopapp.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    Boolean existsByPhoneNumber(String phoneNumber);
    User findByPhoneNumber(String phoneNumber);
    //SELECT * FROM users WHERE phoneNumber=?
    //query command
    @Query("SELECT o FROM User o WHERE o.active = true AND (:keyword IS NULL OR :keyword = '' OR " +
            "o.fullName LIKE %:keyword% " +
            "OR o.address LIKE %:keyword% " +
            "OR o.phoneNumber LIKE %:keyword%) " +
            "AND LOWER(o.role.name) = 'user'") //vdu: admin chi co the lay cac user, ko dc xem cac admin khac
    Page<User> findAll(String keyword, Pageable pageable);
}
