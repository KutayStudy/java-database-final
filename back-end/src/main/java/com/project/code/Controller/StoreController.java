package com.project.code.Controller;

import com.project.code.Model.Store;
import com.project.code.Model.PlaceOrderRequestDTO;
import com.project.code.Repo.StoreRepository;
import com.project.code.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/store")
public class StoreController {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private OrderService orderService;

    @PostMapping
    public Map<String, String> addStore(@RequestBody Store store) {
        Map<String, String> response = new HashMap<>();

        try {
            Store savedStore = storeRepository.save(store);
            response.put("message", "Store created successfully with id " + savedStore.getId());
            return response;
        } catch (Exception e) {
            response.put("Error", e.getMessage());
            return response;
        }
    }

    @GetMapping("/validate/{storeId}")
    public boolean validateStore(@PathVariable("storeId") long storeId) {
        try {
            Optional<Store> store = storeRepository.findById(storeId);
            return store.isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping("/placeOrder")
    public Map<String, String> placeOrder(@RequestBody PlaceOrderRequestDTO placeOrderRequestDTO) {
        Map<String, String> response = new HashMap<>();

        try {
            orderService.placeOrder(placeOrderRequestDTO);
            response.put("message", "Order placed successfully");
            return response;
        } catch (Exception e) {
            response.put("Error", e.getMessage());
            return response;
        }
    }
   
}
