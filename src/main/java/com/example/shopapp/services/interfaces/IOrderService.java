package com.example.shopapp.services.interfaces;

import com.example.shopapp.dtos.OrderDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Order;

import java.util.List;

public interface IOrderService {
    Order createOrder(OrderDTO orderDTO) throws Exception;
    Order getOrder(Long id) throws Exception;
    Order updateOrder(Long id, OrderDTO orderDTO) throws Exception;
    void deleteOrder(Long id) throws Exception;
    List<Order> findByUserId(Long userId) throws Exception;
}
