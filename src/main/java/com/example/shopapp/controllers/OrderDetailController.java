package com.example.shopapp.controllers;

import com.example.shopapp.dtos.*;
import com.example.shopapp.models.OrderDetail;
import com.example.shopapp.services.interfaces.IOrderDetailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/order_details")
@RequiredArgsConstructor
public class OrderDetailController {
    @Autowired
    private final IOrderDetailService iOrderDetailService;

    //Thêm mới 1 order detail
    @PostMapping()
    public ResponseEntity<?> createOrderDetail(
            @Valid @RequestBody OrderDetailDTO newOrderDetail) {
        try {
            OrderDetail orderDetail = iOrderDetailService.createOrderDetail(newOrderDetail);
            return new ResponseEntity<>(orderDetail, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetail(
            @Valid @PathVariable("id") Long id) {
        try {
            OrderDetail orderDetail = iOrderDetailService.getOrderDetail(id);
            return new ResponseEntity<>(orderDetail, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    //lấy ra danh sách các order_details của 1 order nào đó
    @GetMapping("/order/{orderId}")
    public ResponseEntity<?> getOrderDetails(@Valid @PathVariable("orderId") Long orderId) {
        try {
            List<OrderDetail> orderDetails = iOrderDetailService.findByOrderId(orderId);
            return new ResponseEntity<>(orderDetails, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrderDetail(
            @Valid @PathVariable("id") Long id,
            @RequestBody OrderDetailDTO newOrderDetailData) {
        try {
            OrderDetail orderDetail = iOrderDetailService.updateOrderDetail(id, newOrderDetailData);
            return new ResponseEntity<>(orderDetail, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrderDetail(
            @Valid @PathVariable("id") Long id) {
        iOrderDetailService.deleteById(id);
        return new ResponseEntity<>("Delete Order detail with id : " + id + " successfully", HttpStatus.OK);
//        return ResponseEntity.noContent().build();
    }
}
