package com.tcs.service;

import java.util.List;

import com.tcs.entity.Order;

public interface OrderService {
    Order placeOrder(Order order);
    List<Order> getAll();
    Order getById(Long id);
    void delete(Long id);
}