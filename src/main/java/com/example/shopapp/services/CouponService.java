package com.example.shopapp.services;

import com.example.shopapp.models.Coupon;
import com.example.shopapp.models.CouponCondition;
import com.example.shopapp.repositories.CouponConditionRepository;
import com.example.shopapp.repositories.CouponRepository;
import com.example.shopapp.services.interfaces.ICouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService implements ICouponService {
    @Autowired
    private final CouponRepository couponRepository;

    @Autowired
    private final CouponConditionRepository couponConditionRepository;
    @Override
    public Double calculateCouponValue(String couponCode, Double totalAmount) {
        Coupon coupon = couponRepository.findByCode(couponCode)
                .orElseThrow(()-> new IllegalArgumentException("Coupon not found"));
        if (!coupon.isActive()) {
            throw new IllegalArgumentException("Coupon is not active");
        }
        double discount = calculateDiscount(coupon, totalAmount);
        return totalAmount - discount;
    }

    private Double calculateDiscount(Coupon coupon, Double totalAmount) {
        List<CouponCondition> conditions = couponConditionRepository.findByCouponId(coupon.getId());
        double discount = 0.0;
        double updatedTotalAmount = totalAmount;
        for (CouponCondition condition : conditions) {
            //EAV(Entity - Attribute - Value) Model
            //Mỗi condition của coupon lấy ra các trường để tính toán discount
            String attribute = condition.getAttribute();
            String operator = condition.getOperator();
            String value = condition.getValue();

            double discountPercent = Double.parseDouble(String.valueOf(condition.getDiscountAmount()));

            if (attribute.equals("minimum_amount")) {
                if (operator.equals(">") && updatedTotalAmount > Double.parseDouble(value)) {
                    discount += updatedTotalAmount * discountPercent / 100; //update discount
                } 
            } else if (attribute.equals("applicable_date")) {
                LocalDate applicableDate = LocalDate.parse(value);
                LocalDate currentDate = LocalDate.now();
                if (operator.equalsIgnoreCase("BETWEEN") && applicableDate.isEqual(currentDate)) {
                    discount += updatedTotalAmount * discountPercent / 100; //update discount
                }
            }
            //còn nhiều nhiều điều kiện khác nữa
            updatedTotalAmount -= discount;
            //update total amount để nếu còn mã khác -> giảm tiếp dựa trên tổng tiền đã giảm trước đó
        }
        return discount;
    }
}
