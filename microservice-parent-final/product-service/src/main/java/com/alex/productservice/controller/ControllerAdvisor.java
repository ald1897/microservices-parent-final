package com.alex.productservice.controller;

import com.alex.productservice.model.Product;
import com.alex.productservice.service.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ControllerAdvisor extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Product> handleCatNotFound(ProductNotFoundException exc, WebRequest req) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

}
