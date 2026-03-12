package com.project.code.Controller;

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
import java.util.Optional;

@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ServiceClass serviceClass;
    @Autowired
    private InventoryRepository inventoryRepository;

    @PostMapping
    public Map<String,String> addProduct(@RequestBody Product product){
        Map<String,String> response = new HashMap<>();

        try {
            if (product == null) {
                response.put("message", "Invalid product data");
                return response;
            }

            boolean isValid = serviceClass.validateProduct(product);

            if (!isValid) {
                response.put("message", "Product already exists");
                return response;
            }

            productRepository.save(product);
            response.put("message", "Product added successfully");
            return response;

        } catch (DataIntegrityViolationException e) {
            response.put("message", "Data integrity violation");
            return response;
        } catch (Exception e) {
            response.put("message", "An error occurred while adding product");
            return response;
        }
    }

    @GetMapping("/product/{id}")
    public Map<String, Object> getProductbyId(@PathVariable("id") long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Product> product = productRepository.findById(id);

            if (product.isPresent()) {
                response.put("products", product.get());
            } else {
                response.put("products", null);
                response.put("message", "Product not found");
            }

            return response;

        } catch (Exception e) {
            response.put("products", null);
            response.put("message", "An error occurred while fetching product");
            return response;
        }
    }


    @PutMapping
    public Map<String, String> updateProduct(@RequestBody Product product) {
        Map<String, String> response = new HashMap<>();

        try {
            if (product == null) {
                response.put("message", "Invalid product data");
                return response;
            }

            productRepository.save(product);
            response.put("message", "Product updated successfully");
            return response;

        } catch (DataIntegrityViolationException e) {
            response.put("message", "Data integrity violation");
            return response;
        } catch (Exception e) {
            response.put("message", "An error occurred while updating product");
            return response;
        }
    }


    @GetMapping("/category/{name}/{category}")
    public Map<String, Object> filterbyCategoryProduct(@PathVariable("name") String name,
                                                       @PathVariable("category") String category) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Product> products;

            if ("null".equalsIgnoreCase(name) && "null".equalsIgnoreCase(category)) {
                products = productRepository.findAll();
            } else if ("null".equalsIgnoreCase(name)) {
                products = productRepository.findByCategory(category);
            } else if ("null".equalsIgnoreCase(category)) {
                products = productRepository.findProductBySubName(name);
            } else {
                products = productRepository.findProductBySubNameAndCategory(name, category);
            }

            response.put("products", products);
            return response;

        } catch (Exception e) {
            response.put("products", List.of());
            response.put("message", "An error occurred while filtering products");
            return response;
        }
    }


    @GetMapping
    public Map<String, Object> listProduct() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Product> products = productRepository.findAll();
            response.put("products", products);
            return response;

        } catch (Exception e) {
            response.put("products", List.of());
            response.put("message", "An error occurred while fetching products");
            return response;
        }
    }


    @GetMapping("/filter/{category}/{storeid}")
    public Map<String, Object> getProductbyCategoryAndStoreId(@PathVariable("category") String category,@PathVariable("storeid") long storeid) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Product> products = productRepository.findProductByCategory(category, storeid);
            response.put("product", products);
            return response;

        } catch (Exception e) {
            response.put("product", List.of());
            response.put("message", "An error occurred while fetching filtered products");
            return response;
        }
    }


    @DeleteMapping("/{id}")
    public Map<String, String> deleteProduct(@PathVariable("id") long id) {
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


    @GetMapping("/searchProduct/{name}")
    public Map<String, Object> searchProduct(@PathVariable("name") String name) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Product> products = productRepository.findProductBySubName(name);
            response.put("products", products);
            return response;

        } catch (Exception e) {
            response.put("products", List.of());
            response.put("message", "An error occurred while searching product");
            return response;
        }
    }

}
