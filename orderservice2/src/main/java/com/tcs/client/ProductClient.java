package com.tcs.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.tcs.dto.ProductResponse;

@FeignClient(name = "product-service", url = "http://localhost:9001")
public interface ProductClient {
   
	@GetMapping("/products/{id}")
    ProductResponse getProduct(@PathVariable Long id);

	

}
