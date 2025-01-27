package com.dsapkl.backend.controller.admin;

import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.entity.Order;
import com.dsapkl.backend.repository.OrderDto;
import com.dsapkl.backend.repository.OrderRepository;
import com.dsapkl.backend.service.MemberService;
import com.dsapkl.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/manage")
@RequiredArgsConstructor
public class ManageController {


    private final MemberService memberService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;


    @GetMapping("/info")
    public String manageMembers(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "all") String searchType,
            @RequestParam(required = false) String searchKeyword,
            Model model) {
        List<Member> members;

        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {

            switch (searchType) {
                case "name":
                    members = memberService.findByNameContaining(searchKeyword);
                    break;
                case "email":
                    members = memberService.findByEmailContaing(searchKeyword);
                    break;
                case "phoneNumber":
                    members = memberService.findByPhoneNumberContaining(searchKeyword);
                    break;
                default:
                    members = memberService.searchMembers(searchKeyword);
            }
        } else {
            members = memberService.findOnlyUser();
        }

        model.addAttribute("members", members);

//         페이지네이션을 위한 PageRequest 객체 생성
//        PageRequest pageRequest = PageRequest.of(page, size);
//        Page<Member> userPage = memberService.findOnlyUsers(pageRequest);

        // 회원 목록 조회 (검색어가 있는 경우 필터링)
//        Page<Member> memberPage = memberService.getMembers(searchKeyword, pageRequest);

        // 통계 데이터 조회
        long totalMembers = memberService.findMembers().size() - 1;
//        long newMembers = memberService.getNewMembersToday();

        // 모델에 데이터 추가
//        model.addAttribute("membersOnlyUser", userPage.getContent());
//        model.addAttribute("totalPages", memberPage.getTotalPages());
//        model.addAttribute("currentPage", page);
        model.addAttribute("totalMembers", totalMembers);
//        model.addAttribute("newMembers", newMembers);

        return "manage/memberManage";
    }

    @GetMapping("/members/{memberId}/orders")
    public String viewMemberOrders(@PathVariable Long memberId, Model model) {
        Member member = memberService.findMember(memberId);
//        List<Order> orders = orderService.findOrders(memberId);
        List<OrderDto> orderDetails = orderRepository.findOrderDetail(memberId, null);

        model.addAttribute("member", member);
        model.addAttribute("orderDetails", orderDetails);

        return "manage/memberOrders";  // 새로운 뷰 페이지
    }

}

