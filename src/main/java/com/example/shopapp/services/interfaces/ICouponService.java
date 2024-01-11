package com.example.shopapp.services.interfaces;

public interface ICouponService {
    Double calculateCouponValue(String couponCode, Double totalAmount);
}
