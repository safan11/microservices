package com.tcs.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tcs.entity.Product;
import com.tcs.repository.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
    private  ProductRepository repo;

    @Override
    public Product save(Product product) {
        return repo.save(product);
    }

    @Override
    public List<Product> getAll() {
        return repo.findAll();
    }

    @Override
    public Product getById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public Product update(Long id, Product updated) {
        Product existing = repo.findById(id).orElse(null);
        if (existing == null) return null;

        existing.setName(updated.getName());
        existing.setPrice(updated.getPrice());
        return repo.save(existing);
    }

    @Override
    public void delete(Long id) {
        repo.deleteById(id);
    }
}