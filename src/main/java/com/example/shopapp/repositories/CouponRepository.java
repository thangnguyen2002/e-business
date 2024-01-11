package com.example.shopapp.repositories;

import com.example.shopapp.models.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String couponCode); //neu ko co optional -> ko dung duoc orElseThrow
}
