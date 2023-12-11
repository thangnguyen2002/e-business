package com.example.shopapp.services;

import com.example.shopapp.dtos.OrderDetailDTO;
import com.example.shopapp.exceptions.DataNotFoundException;
import com.example.shopapp.models.Order;
import com.example.shopapp.models.OrderDetail;
import com.example.shopapp.models.Product;
import com.example.shopapp.repositories.OrderDetailRepository;
import com.example.shopapp.repositories.OrderRepository;
import com.example.shopapp.repositories.ProductRepository;
import com.example.shopapp.services.interfaces.IOrderDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderDetailService implements IOrderDetailService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;

    @Override
    public OrderDetail createOrderDetail(OrderDetailDTO orderDetailDTO) throws Exception {
        Order order = orderRepository.findById(orderDetailDTO.getOrderId())
                .orElseThrow(() -> new DataNotFoundException(
                        "Cannot find Order with id : " + orderDetailDTO.getOrderId()));
        Product product = productRepository.findById(orderDetailDTO.getProductId())
                .orElseThrow(() -> new DataNotFoundException(
                        "Cannot find product with id: " + orderDetailDTO.getProductId()));
        OrderDetail orderDetail = OrderDetail.builder()
                .order(order)
                .product(product)
                .numberOfProducts(orderDetailDTO.getNumberOfProducts())
                .price(orderDetailDTO.getPrice())
                .totalMoney(orderDetailDTO.getTotalMoney())
                .color(orderDetailDTO.getColor())
                .build();
        return orderDetailRepository.save(orderDetail);
    }

    @Override
    public OrderDetail getOrderDetail(Long id) throws Exception {
        return orderDetailRepository.findById(id)
                .orElseThrow(()->new DataNotFoundException("Cannot find OrderDetail with id: "+id));
    }

    @Override
    public OrderDetail updateOrderDetail(Long id, OrderDetailDTO orderDetailData) throws Exception {
        OrderDetail orderDetail = getOrderDetail(id);
        Order order = orderRepository.findById(orderDetailData.getOrderId())
                .orElseThrow(() -> new DataNotFoundException(
                        "Cannot find Order with id : " + orderDetailData.getOrderId()));
        Product product = productRepository.findById(orderDetailData.getProductId())
                .orElseThrow(() -> new DataNotFoundException(
                        "Cannot find product with id: " + orderDetailData.getProductId()));
        orderDetail.setPrice(orderDetailData.getPrice());
        orderDetail.setNumberOfProducts(orderDetailData.getNumberOfProducts());
        orderDetail.setTotalMoney(orderDetailData.getTotalMoney());
        orderDetail.setColor(orderDetailData.getColor());
        orderDetail.setOrder(order);
        orderDetail.setProduct(product);
        return orderDetailRepository.save(orderDetail);
    }

    @Override
    public void deleteById(Long id) {
        orderDetailRepository.deleteById(id);
    }

    @Override
    public List<OrderDetail> findByOrderId(Long orderId) throws Exception {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(orderId);
        if (orderDetails.isEmpty()) {
            throw new DataNotFoundException(
                    "Cannot find Order detail with order id : " + orderId);
        }
        return orderDetails;
    }
}
