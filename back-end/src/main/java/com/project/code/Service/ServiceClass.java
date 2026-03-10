package com.project.code.Service;

import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Repository.InventoryRepository;
import com.project.code.Repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ServiceClass {

    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    public ServiceClass(ProductRepository productRepository,
                        InventoryRepository inventoryRepository) {
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
    }

    public boolean validateInventory(Inventory inventory) {
        boolean exists = inventoryRepository.existsByProductIdAndStoreId(
                inventory.getProduct().getId(),
                inventory.getStore().getId()
        );
        return !exists;
    }

    public boolean validateProduct(Product product) {
        boolean exists = productRepository.existsByName(product.getName());
        return !exists;
    }

    public boolean validateProductId(long id) {
        return productRepository.existsById(id);
    }

    public Inventory getInventoryId(Inventory inventory) {
        return inventoryRepository.findByProductIdAndStoreId(
                inventory.getProduct().getId(),
                inventory.getStore().getId()
        );
    }
}