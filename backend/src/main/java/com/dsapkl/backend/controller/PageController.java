package com.dsapkl.backend.controller;

import com.dsapkl.backend.config.AuthenticatedUser;
import com.dsapkl.backend.dto.ItemForm;
import com.dsapkl.backend.dto.ItemImageDto;
import com.dsapkl.backend.entity.*;
import com.dsapkl.backend.repository.ClusterItemPreferenceRepository;
import com.dsapkl.backend.repository.MemberInfoRepository;
import com.dsapkl.backend.repository.MemberRepository;
import com.dsapkl.backend.repository.OrderDto;
import com.dsapkl.backend.repository.query.CartQueryDto;
import com.dsapkl.backend.service.CartService;
import com.dsapkl.backend.service.ItemImageService;
import com.dsapkl.backend.service.ItemService;
import com.dsapkl.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PageController {
    private final ItemService itemService;
    private final CartService cartService;
    private final OrderService orderService;
    private final MemberInfoRepository memberInfoRepository;
    private final ClusterItemPreferenceRepository clusterItemPreferenceRepository;
    private final MemberRepository memberRepository;
    private final ItemImageService itemImageService;

    @GetMapping("/admin")
    public String adminPage(Model model,
                            @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                            @RequestParam(value = "query", required = false) String query,
                            @RequestParam(value = "orderStatus", required = false) OrderStatus orderStatus,
                            @RequestParam(value = "category", required = false) String category) {

        Member member = memberRepository.findByEmail(authenticatedUser.getEmail()).orElseThrow(()-> new IllegalArgumentException("회원 정보를 찾을 수 없습니다. (SECURITY)"));

        List<Item> items = itemService.searchItems(query, category);

        //ItemForm 으로 변환
        List<ItemForm> itemForms = items.stream().map(item -> {
            List<ItemImage> itemImageList = itemImageService.findItemImageDetail(item.getId(), "N");
            List<ItemImageDto> itemImageDtoList = itemImageList.stream().map(ItemImageDto::new).collect(Collectors.toList());

            ItemForm itemForm = ItemForm.from(item);
            itemForm.setItemImageListDto(itemImageDtoList);
            return itemForm;
        })
                .collect(Collectors.toList());
        model.addAttribute("itemForms", itemForms);



        // 카테고리 선택 상태 유지
        if (category != null && !category.trim().isEmpty()) {
            try {
                Category selectedCategory = Category.valueOf(category.toUpperCase());
                model.addAttribute("selectedCategory", selectedCategory);
            } catch (IllegalArgumentException e) {
                // 잘못된 카테고리 값은 무시
            }
        }


        List<CartQueryDto> cartItemListForm = cartService.findCartItems(member.getId());
        model.addAttribute("cartItemListForm", cartItemListForm);
        model.addAttribute("cartItemCount", cartItemListForm.size());

        List<OrderDto> orderDetail = orderService.findOrdersDetail(member.getId(), orderStatus);
        long orderCount = orderDetail.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.ORDER)
                .count();

        model.addAttribute("items", items);
        model.addAttribute("orderCount", orderCount);

        List<Item> recommendedItems = itemService.findLatestItems(4).stream()
                .filter(item -> "ON".equals(item.getShowYn()))
                .collect(Collectors.toList());
        model.addAttribute("recommendedItems", recommendedItems);

        return "auth/admin";
    }

    @GetMapping("/user")
    public String userPage(Model model,
                           @RequestParam(value = "query", required = false) String query,
                           @RequestParam(value = "category", required = false) String category,
                           @RequestParam(value = "orderStatus", required = false) OrderStatus orderStatus,
                           @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        Member member = memberRepository.findByEmail(authenticatedUser.getEmail()).orElseThrow(()-> new IllegalArgumentException("회원 정보를 찾을 수 없습니다. (SECURITY)"));
        MemberInfo memberInfo = memberInfoRepository.findByMemberId(member.getId()).get();

        List<Item> items = itemService.searchItems(query, category);


        //ItemForm 으로 변환
        List<ItemForm> itemForms = items.stream().map(item -> {
                    List<ItemImage> itemImageList = itemImageService.findItemImageDetail(item.getId(), "N");
                    List<ItemImageDto> itemImageDtoList = itemImageList.stream().map(ItemImageDto::new).collect(Collectors.toList());

                    ItemForm itemForm = ItemForm.from(item);
                    itemForm.setItemImageListDto(itemImageDtoList);
                    return itemForm;
                })
                .collect(Collectors.toList());
        model.addAttribute("itemForms", itemForms);


        // 카테고리 선택 상태 유지
        if (category != null && !category.trim().isEmpty()) {
            try {
                Category selectedCategory = Category.valueOf(category.toUpperCase());
                model.addAttribute("selectedCategory", selectedCategory);
            } catch (IllegalArgumentException e) {
                // 잘못된 카테고리 값은 무시
            }
        }

        // 선호도 기반 추천 상품 가져오기
        List<Item> recommendedItems;

        if (memberInfo != null && memberInfo.getCluster_id() != null) {
            // 클러스터의 선호도 높은 상품 4개 조회
            List<ClusterItemPreference> preferences = clusterItemPreferenceRepository
                    .findByClusterIdOrderByPreferenceScoreDesc(memberInfo.getCluster_id().getId());

            if (!preferences.isEmpty()) {
                recommendedItems = preferences.stream()
                        .map(ClusterItemPreference::getItem)
                        .filter(item -> "ON".equals(item.getShowYn()))
                        .limit(4)
                        .collect(Collectors.toList());
                model.addAttribute("isRecommended", true);
            } else {
                recommendedItems = itemService.findLatestItems(4).stream()
                        .filter(item -> "ON".equals(item.getShowYn()))
                        .collect(Collectors.toList());
                model.addAttribute("isRecommended", false);
            }
        } else {
            recommendedItems = itemService.findLatestItems(4).stream()
                    .filter(item -> "ON".equals(item.getShowYn()))
                    .collect(Collectors.toList());
            model.addAttribute("isRecommended", false);
        }

        model.addAttribute("recommendedItems", recommendedItems);


        List<CartQueryDto> cartItemListForm = cartService.findCartItems(member.getId());
        model.addAttribute("cartItemListForm", cartItemListForm);
        model.addAttribute("cartItemCount", cartItemListForm.size());

        List<OrderDto> orderDetail = orderService.findOrdersDetail(member.getId(), orderStatus);
        long orderCount = orderDetail.stream()
                .filter(order -> order.getOrderStatus() == OrderStatus.ORDER)
                .count();

        model.addAttribute("items", items);
        model.addAttribute("orderCount", orderCount);

        return "auth/user";
    }


    @GetMapping("/guest")
    public String homePage(Model model,
                           @RequestParam(value = "query", required = false) String query,
                           @RequestParam(value = "category", required = false) String category) {

        List<Item> items = itemService.searchItems(query, category);


        //ItemForm 으로 변환
        List<ItemForm> itemForms = items.stream().map(item -> {
                    List<ItemImage> itemImageList = itemImageService.findItemImageDetail(item.getId(), "N");
                    List<ItemImageDto> itemImageDtoList = itemImageList.stream().map(ItemImageDto::new).collect(Collectors.toList());

                    ItemForm itemForm = ItemForm.from(item);
                    itemForm.setItemImageListDto(itemImageDtoList);
                    return itemForm;
                })
                .collect(Collectors.toList());
        model.addAttribute("itemForms", itemForms);


        // 카테고리 선택 상태 유지
        if (category != null && !category.trim().isEmpty()) {
            try {
                Category selectedCategory = Category.valueOf(category.toUpperCase());
                model.addAttribute("selectedCategory", selectedCategory);
            } catch (IllegalArgumentException e) {
                // 잘못된 카테고리 값은 무시
            }
        }

        List<Item> recommendedItems = itemService.findLatestItems(4).stream()
                .filter(item -> "ON".equals(item.getShowYn()))
                .collect(Collectors.toList());

        model.addAttribute("recommendedItems", recommendedItems);

        model.addAttribute("items", items);


        return "home";
    }

    private Integer getPreferenceScore(Cluster cluster, Item item) {
        return clusterItemPreferenceRepository.findByClusterAndItem(cluster, item)
                .map(ClusterItemPreference::getPreferenceScore)
                .orElse(0);
    }

}
