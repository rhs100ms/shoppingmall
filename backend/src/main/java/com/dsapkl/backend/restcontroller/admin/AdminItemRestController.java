package com.dsapkl.backend.restcontroller.admin;

import com.dsapkl.backend.entity.Item;
import com.dsapkl.backend.repository.ItemRepository;
import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.service.ItemImageService;
import com.dsapkl.backend.service.ItemService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminItemRestController {

    private final ItemService itemService;
    private final ItemImageService itemImageService;
    private final CartService cartService;
    private final ItemRepository itemRepository;

    @GetMapping("/api/items/{itemId}/rating")
    public ResponseEntity<Map<String, Object>> getItemRating(@PathVariable Long itemId) {
        Item item = itemService.findItem(itemId);
        Map<String, Object> response = new HashMap<>();
        response.put("averageRating", item.getAverageRating());
        response.put("reviewCount", item.getReviewCount());
        return ResponseEntity.ok(response);
    }

//    /**
//     * 상품 표시 상태 업데이트 (Show ON/OFF)
//     */
//    @PostMapping("/items/{itemId}/update-show-status")
//    @ResponseBody
//    public ResponseEntity<?> updateShowStatus(@PathVariable Long itemId, @RequestBody Map<String, String> payload) {
//        try {
//            String showYn = payload.get("showYn");
//            if (showYn == null || (!showYn.equals("Y") && !showYn.equals("N"))) {
//                return ResponseEntity.badRequest().body("Invalid show status value. Must be 'Y' or 'N'.");
//            }
//
//            itemService.updateShowStatus(itemId, showYn);
//            return ResponseEntity.ok().build();
//        } catch (EntityNotFoundException e) {
//            return ResponseEntity.notFound().build();
//        } catch (Exception e) {
//            log.error("Error updating show status for item {}: {}", itemId, e.getMessage(), e);
//            return ResponseEntity.internalServerError().body("Failed to update show status: " + e.getMessage());
//        }
//    }

}
