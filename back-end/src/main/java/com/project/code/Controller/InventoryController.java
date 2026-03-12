package com.project.code.Controller;

import com.project.code.Model.CombinedRequest;
import com.project.code.Model.Inventory;
import com.project.code.Model.Product;
import com.project.code.Repo.InventoryRepository;
import com.project.code.Repo.ProductRepository;
import com.project.code.Service.ServiceClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ServiceClass serviceClass;

    @PutMapping
    public Map<String, String> updateInventory(@RequestBody CombinedRequest combinedRequest) {
        Map<String, String> response = new HashMap<>();

        try {
            Product product = combinedRequest.getProduct();
            Inventory inventory = combinedRequest.getInventory();

            if (product == null || inventory == null) {
                response.put("message", "Invalid request body");
                return response;
            }

            if (!serviceClass.validateProductId(product.getId())) {
                response.put("message", "Product not present in database");
                return response;
            }

            if (inventory.getStore() == null) {
                response.put("message", "Invalid inventory data");
                return response;
            }

            Inventory existingInventory =
                    inventoryRepository.findByProductIdandStoreId(product.getId(), inventory.getStore().getId());

            if (existingInventory == null) {
                response.put("message", "No data available");
                return response;
            }

            productRepository.save(product);

            existingInventory.setProduct(product);
            existingInventory.setStore(inventory.getStore());
            existingInventory.setStockLevel(inventory.getStockLevel());

            inventoryRepository.save(existingInventory);

            response.put("message", "Successfully updated product");
            return response;

        } catch (DataIntegrityViolationException e) {
            response.put("message", "Data integrity violation");
            return response;
        } catch (Exception e) {
            response.put("message", "An error occurred while updating product");
            return response;
        }
    }

    @PostMapping
    public Map<String, String> saveInventory(@RequestBody Inventory inventory) {
        Map<String, String> response = new HashMap<>();

        try {
            if (inventory == null) {
                response.put("message", "Invalid inventory data");
                return response;
            }

            boolean isValid = serviceClass.validateInventory(inventory);

            if (!isValid) {
                response.put("message", "Data already present");
                return response;
            }

            inventoryRepository.save(inventory);
            response.put("message", "Data saved successfully");
            return response;

        } catch (DataIntegrityViolationException e) {
            response.put("message", "Data integrity violation");
            return response;
        } catch (Exception e) {
            response.put("message", "An error occurred while saving inventory");
            return response;
        }
    }

    @GetMapping("/{storeid}")
    public Map<String, Object> getAllProducts(@PathVariable("storeid") long storeid) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Product> products = productRepository.findProductsByStoreId(storeid);
            response.put("products", products);
            return response;
        } catch (Exception e) {
            response.put("products", List.of());
            response.put("message", "An error occurred while fetching products");
            return response;
        }
    }

    @GetMapping("/filter/{category}/{name}/{storeid}")
    public Map<String, Object> getProductName(@PathVariable("category") String category,
                                              @PathVariable("name") String name,
                                              @PathVariable("storeid") long storeid) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Product> products;

            if ("null".equalsIgnoreCase(category)) {
                products = productRepository.findByNameLike(storeid, name);
            } else if ("null".equalsIgnoreCase(name)) {
                products = productRepository.findByCategoryAndStoreId(storeid, category);
            } else {
                products = productRepository.findByNameAndCategory(storeid, name, category);
            }

            response.put("product", products);
            return response;

        } catch (Exception e) {
            response.put("product", List.of());
            response.put("message", "An error occurred while filtering products");
            return response;
        }
    }

    @GetMapping("/search/{name}/{storeId}")
    public Map<String, Object> searchProduct(@PathVariable("name") String name,
                                             @PathVariable("storeId") long storeId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Product> products = productRepository.findByNameLike(storeId, name);
            response.put("product", products);
            return response;

        } catch (Exception e) {
            response.put("product", List.of());
            response.put("message", "An error occurred while searching product");
            return response;
        }
    }

    @DeleteMapping("/{id}")
    public Map<String, String> removeProduct(@PathVariable("id") long id) {
        Map<String, String> response = new HashMap<>();

        try {
            if (!serviceClass.validateProductId(id)) {
                response.put("message", "Product not present in database");
                return response;
            }

            inventoryRepository.deleteByProductId(id);
            productRepository.deleteById(id);

            response.put("message", "Product deleted successfully");
            return response;

        } catch (Exception e) {
            response.put("message", "An error occurred while deleting product");
            return response;
        }
    }

    @GetMapping("/validate/{quantity}/{storeId}/{productId}")
    public boolean validateQuantity(@PathVariable("quantity") int quantity,
                                    @PathVariable("storeId") long storeId,
                                    @PathVariable("productId") long productId) {
        try {
            Inventory inventory = inventoryRepository.findByProductIdandStoreId(productId, storeId);

            if (inventory == null || inventory.getStockLevel() == null) {
                return false;
            }

            return inventory.getStockLevel() >= quantity;

        } catch (Exception e) {
            return false;
        }
    }
}