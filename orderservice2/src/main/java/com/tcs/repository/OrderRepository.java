package com.tcs.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tcs.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
