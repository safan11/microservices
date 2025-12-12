package com.tcs.service;

import java.util.List;

import com.tcs.entity.Product;

public interface ProductService {

	    Product save(Product p);
	    List<Product> getAll();
	    Product getById(Long id);
	    Product update(Long id, Product p);
	    void delete(Long id);
}
