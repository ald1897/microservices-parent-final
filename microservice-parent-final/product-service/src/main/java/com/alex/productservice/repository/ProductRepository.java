package com.alex.productservice.repository;


import com.alex.productservice.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String > {


}
