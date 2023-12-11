package com.example.shopapp.services.interfaces;

import com.example.shopapp.dtos.OrderDetailDTO;
import com.example.shopapp.models.OrderDetail;

import java.util.List;

public interface IOrderDetailService {
    OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) throws Exception;
    OrderDetail getOrderDetail(Long id) throws Exception;
    OrderDetail updateOrderDetail(Long id, OrderDetailDTO OrderDetailData)
            throws Exception;
    void deleteById(Long id);
    List<OrderDetail> findByOrderId(Long orderId) throws Exception;
}
