package com.claimsplatform.policyservice.controller;

import com.claimsplatform.common.dto.ApiResponse;
import com.claimsplatform.common.exception.ResourceNotFoundException;
import com.claimsplatform.policyservice.entity.Customer;
import com.claimsplatform.policyservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Customer>>> getAllCustomers(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(customerRepository.findAll(pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> getCustomerById(@PathVariable Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return ResponseEntity.ok(ApiResponse.ok(customer));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Customer>> createCustomer(@RequestBody Customer customer) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Customer created", customerRepository.save(customer)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Customer>> updateCustomer(@PathVariable Long id, @RequestBody Customer updated) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        customer.setFirstName(updated.getFirstName());
        customer.setLastName(updated.getLastName());
        customer.setPhone(updated.getPhone());
        customer.setAddress(updated.getAddress());
        customer.setCity(updated.getCity());
        customer.setProvince(updated.getProvince());
        customer.setPostalCode(updated.getPostalCode());
        return ResponseEntity.ok(ApiResponse.ok("Customer updated", customerRepository.save(customer)));
    }
}
