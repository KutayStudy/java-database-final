package com.project.code.Controller;

import com.project.code.Model.Customer;
import com.project.code.Model.Review;
import com.project.code.Repo.CustomerRepository;
import com.project.code.Repo.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/{storeId}/{productId}")
    public Map<String, Object> getReviews(@PathVariable("storeId") long storeId,
                                          @PathVariable("productId") long productId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Review> reviewList = reviewRepository.findByStoreIdAndProductId(storeId, productId);
            List<Map<String, Object>> filteredReviews = new ArrayList<>();

            for (Review review : reviewList) {
                Map<String, Object> reviewMap = new HashMap<>();
                reviewMap.put("comment", review.getComment());
                reviewMap.put("rating", review.getRating());

                Optional<Customer> customer = customerRepository.findById(review.getCustomerId());
                if (customer.isPresent()) {
                    reviewMap.put("customerName", customer.get().getName());
                } else {
                    reviewMap.put("customerName", "Unknown");
                }

                filteredReviews.add(reviewMap);
            }

            response.put("reviews", filteredReviews);
            return response;

        } catch (Exception e) {
            response.put("reviews", List.of());
            response.put("message", "An error occurred while fetching reviews");
            return response;
        }
    }
}