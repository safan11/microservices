package com.tcs.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tcs.client.ProductClient;
import com.tcs.dto.ProductResponse;
import com.tcs.entity.Order;
import com.tcs.repository.OrderRepository;

@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
    private  OrderRepository repo;
	
	
	@Autowired
    private  ProductClient productClient;

    

    @Override
    public Order placeOrder(Order order) {
        ProductResponse product = productClient.getProduct(order.getProductId());
        if (product == null) return null;

        double total = order.getQuantity() * product.getPrice();
        order.setTotalPrice(total);

        return repo.save(order);
    }

    @Override
    public List<Order> getAll() {
        return repo.findAll();
    }

    @Override
    public Order getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}