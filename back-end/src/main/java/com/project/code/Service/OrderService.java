package com.project.code.Service;

import com.project.code.Model.*;
import com.project.code.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderService {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private OrderDetailsRepository orderDetailsRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;

    @Transactional
    public void saveOrder(PlaceOrderRequestDTO placeOrderRequest){
        Customer customer = customerRepository.findByEmail(placeOrderRequest.getCustomerEmail());

        if(customer == null){
            customer = new Customer();
            customer.setName(placeOrderRequest.getCustomerName());
            customer.setEmail(placeOrderRequest.getCustomerEmail());
            customer = customerRepository.save(customer);
        }

        Store store = storeRepository.findById(placeOrderRequest.getStoreId()).orElseThrow(() -> new RuntimeException("Store not found with id: " + placeOrderRequest.getStoreId()));

        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setCustomer(customer);
        orderDetails.setStore(store);
        orderDetails.setTotalPrice(placeOrderRequest.getTotalPrice());
        orderDetails.setOrderDate(LocalDateTime.now());

        orderDetails = orderDetailsRepository.save(orderDetails);


        for (PurchasedProductDTO purchasedProduct : placeOrderRequest.getProducts()) {

            Product product = productRepository.findById(purchasedProduct.getProductId())
                    .orElseThrow(() -> new RuntimeException(
                            "Product not found with id: " + purchasedProduct.getProductId()));

            Inventory inventory = inventoryRepository
                    .findByStoreIdAndProductId(store.getId(), product.getId());

            if (inventory == null) {
                throw new RuntimeException(
                        "Inventory not found for store id " + store.getId() +
                        " and product id " + product.getId());
            }

            if (inventory.getStockQuantity() < purchasedProduct.getQuantity()) {
                throw new RuntimeException(
                        "Not enough stock for product id: " + product.getId());
            }

            inventory.setStockQuantity(inventory.getStockQuantity() - purchasedProduct.getQuantity());
            inventoryRepository.save(inventory);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderDetails(orderDetails);
            orderItem.setProduct(product);
            orderItem.setQuantity(purchasedProduct.getQuantity());
            orderItem.setPrice(product.getPrice());

            orderItemRepository.save(orderItem);
        }

    }
   
}
