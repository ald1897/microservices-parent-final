package com.alex.productservice.service;

import com.alex.productservice.dto.ProductRequest;
import com.alex.productservice.dto.ProductResponse;
import com.alex.productservice.model.Product;
import com.alex.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public void createProduct(ProductRequest productRequest) {
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .build();

        productRepository.save(product);
        log.info("Product {} is saved", product.getId());
    }

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();

        log.info("Got All Products!");

        return products.stream().map(this::mapToProductResponse).toList();
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
    }
    public Product findProductById(String id) throws ProductNotFoundException {

        Optional<Product> oProduct = productRepository.findById(id);

        if (oProduct.isEmpty()) {
            log.info("Product found");
            throw new ProductNotFoundException();
        }
        return oProduct.get();
        }

    public void deleteProductById(String id) throws ProductNotFoundException {
        Optional<Product> oProduct = productRepository.findById(id);

        if (oProduct.isEmpty()) {
            throw new ProductNotFoundException();
        }
        Product product = oProduct.get();
        log.info("Product {} with ID {} is being Deleted", product.getName(), product.getId());
        productRepository.delete(product);

    }

}
