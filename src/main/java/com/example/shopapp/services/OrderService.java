package com.example.shopapp.services;

import com.example.shopapp.dtos.CartItemDTO;
import com.example.shopapp.dtos.OrderDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.*;
import com.example.shopapp.repositories.OrderDetailRepository;
import com.example.shopapp.repositories.OrderRepository;
import com.example.shopapp.repositories.ProductRepository;
import com.example.shopapp.repositories.UserRepository;
import com.example.shopapp.services.interfaces.IOrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final OrderRepository orderRepository;
    @Autowired
    private final ProductRepository productRepository;
    @Autowired
    private final OrderDetailRepository orderDetailRepository;
    @Transactional
    @Override
    public Order createOrder(OrderDTO orderDTO) throws Exception {
        //tìm xem user'id có tồn tại ko
        User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new DataNotFoundException("Cannot find user with id: " + orderDTO.getUserId()));
        //convert orderDTO => Order
        Order order = Order.builder()
                .user(user)
                .fullName(orderDTO.getFullName())
                .email(orderDTO.getEmail())
                .phoneNumber(orderDTO.getPhoneNumber())
                .address(orderDTO.getAddress())
                .note(orderDTO.getNote())
                .orderDate(LocalDateTime.now())
                .status(OrderStatus.PENDING)
                .totalMoney(orderDTO.getTotalMoney())
                .shippingMethod(orderDTO.getShippingMethod())
                .shippingAddress(orderDTO.getShippingAddress())
                .paymentMethod(orderDTO.getPaymentMethod())
                .build();
        //Kiểm tra shipping date phải >= ngày hôm nay
        LocalDate shippingDate = orderDTO.getShippingDate() == null ? LocalDate.now() : orderDTO.getShippingDate();
        if (shippingDate.isBefore(LocalDate.now())) {
            throw new DataNotFoundException("Date must be at least today !");
        }
        order.setShippingDate(shippingDate);
        order.setActive(true); //nên set trạng thái đơn hàng mặc định true trong sql
//        còn chuyển pending thì do admín
        orderRepository.save(order);

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItemDTO cartItemDTO : orderDTO.getCartItems()) {
            Product exProduct = productRepository.findById(cartItemDTO.getProductId())
                    .orElseThrow(() -> new DataNotFoundException("Cannot find product with id: " + cartItemDTO.getProductId()));

            OrderDetail orderDetail = OrderDetail.builder()
                    .order(order)
                    .product(exProduct)
                    .price(exProduct.getPrice())
//                    .totalMoney(order.getTotalMoney()) //csdl tu sinh ra dua tren discount
                    .numberOfProducts(cartItemDTO.getQuantity())
                    .build();
            //them order details vao danh sach
            orderDetails.add(orderDetail);
        }
        //luu danh sach order details vao csdl
        orderDetailRepository.saveAll(orderDetails);

        return order;
    }

    @Override
    public Order getOrder(Long id) throws Exception {
        return orderRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Cannot find with order id: " + id));
    }
    @Transactional
    @Override
    public Order updateOrder(Long id, OrderDTO orderDTO) throws Exception {
        Order order = orderRepository.findById(id).orElseThrow(() ->
                new DataNotFoundException("Cannot find order with id: " + id));
        User existingUser = userRepository.findById(
                orderDTO.getUserId()).orElseThrow(() ->
                new DataNotFoundException("Cannot find user with id: " + id));
        order.setUser(existingUser);
        order.setFullName(orderDTO.getFullName());
        order.setEmail(orderDTO.getEmail());
        order.setPhoneNumber(orderDTO.getPhoneNumber());
        order.setAddress(orderDTO.getAddress());
        order.setNote(orderDTO.getNote());
        order.setTotalMoney(orderDTO.getTotalMoney());
        order.setShippingMethod(orderDTO.getShippingMethod());
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setShippingDate(orderDTO.getShippingDate());
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        return orderRepository.save(order);
    }
    @Transactional
    @Override
    public void deleteOrder(Long id) throws Exception {
        Order order = getOrder(id);
        //soft-delete
        if (order != null) {
            order.setActive(false);
            orderRepository.save(order);
        }
    }

    @Override
    public List<Order> findByUserId(Long userId) throws Exception {
        List<Order> orders = orderRepository.findByUserId(userId);
        if (orders.isEmpty()) {
            throw new DataNotFoundException("Cannot find orders with user id: " + userId);

        }
        return orders;
    }
}
