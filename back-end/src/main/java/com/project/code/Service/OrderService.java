package com.project.code.Service;

import com.project.code.Model.Customer;
import com.project.code.Model.Inventory;
import com.project.code.Model.OrderDetails;
import com.project.code.Model.OrderItem;
import com.project.code.Model.PlaceOrderRequestDTO;
import com.project.code.Model.Product;
import com.project.code.Model.PurchaseProductDTO;
import com.project.code.Model.Store;
import com.project.code.Repo.CustomerRepository;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.OrderDetailsRepository;
import com.project.code.Repo.OrderItemRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Repo.StoreRepository;
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
    public void placeOrder(PlaceOrderRequestDTO placeOrderRequest) {
        Customer customer = customerRepository.findByEmail(placeOrderRequest.getCustomerEmail());

        if (customer == null) {
            customer = new Customer();
            customer.setName(placeOrderRequest.getCustomerName());
            customer.setEmail(placeOrderRequest.getCustomerEmail());
            customer = customerRepository.save(customer);
        }

        Store store = storeRepository.findById(placeOrderRequest.getStoreId())
                .orElseThrow(() -> new RuntimeException(
                        "Store not found with id: " + placeOrderRequest.getStoreId()));

        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setCustomer(customer);
        orderDetails.setStore(store);
        orderDetails.setTotalPrice(placeOrderRequest.getTotalPrice());
        orderDetails.setDate(LocalDateTime.now());

        orderDetails = orderDetailsRepository.save(orderDetails);

        for (PurchaseProductDTO purchasedProduct : placeOrderRequest.getPurchaseProduct()) {

            Product product = productRepository.findById(purchasedProduct.getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Product not found with id: " + purchasedProduct.getId()));

            Inventory inventory = inventoryRepository.findByProductIdandStoreId(
                    product.getId(),
                    store.getId()
            );

            if (inventory == null) {
                throw new RuntimeException(
                        "Inventory not found for store id " + store.getId() +
                        " and product id " + product.getId());
            }

            if (inventory.getStockLevel() < purchasedProduct.getQuantity()) {
                throw new RuntimeException(
                        "Not enough stock for product id: " + product.getId());
            }

            inventory.setStockLevel(inventory.getStockLevel() - purchasedProduct.getQuantity());
            inventoryRepository.save(inventory);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(orderDetails);
            orderItem.setProduct(product);
            orderItem.setQuantity(purchasedProduct.getQuantity());
            orderItem.setPrice(product.getPrice());

            orderItemRepository.save(orderItem);
        }
    }
}