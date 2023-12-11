package com.example.shopapp.repositories;

import com.example.shopapp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    Boolean existsByPhoneNumber(String phoneNumber);
    User findByPhoneNumber(String phoneNumber);
    //SELECT * FROM users WHERE phoneNumber=?
}
