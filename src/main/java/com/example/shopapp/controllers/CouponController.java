package com.example.shopapp.controllers;

import com.example.shopapp.responses.CouponCalculationResponse;
import com.example.shopapp.services.interfaces.ICouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/coupons")
//Dependency Injection
@RequiredArgsConstructor
public class CouponController {
    @Autowired
    private final ICouponService iCouponService;
    @PostMapping("/calculate")
    public ResponseEntity<CouponCalculationResponse> calculateCouponValue(
            @RequestParam("couponCode") String couponCode,
            @RequestParam("totalAmount") Double totalAmount
    ) {
        try {
            Double finalAmount = iCouponService.calculateCouponValue(couponCode, totalAmount);
            CouponCalculationResponse couponCalculationResponse = CouponCalculationResponse.builder()
                    .result(finalAmount)
                    .errorMessage("")
                    .build();
            return new ResponseEntity<>(couponCalculationResponse, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(CouponCalculationResponse.builder()
                    .result(totalAmount)
                    .errorMessage(e.getMessage())
                    .build(),
                    HttpStatus.BAD_REQUEST);
        }

    }
}
