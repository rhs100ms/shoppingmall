//package com.dsapkl.backend.restcontroller.user;
//
//import com.dsapkl.backend.dto.MemberInfoCreateDto;
//import com.dsapkl.backend.dto.MemberInfoResponseDto;
//import com.dsapkl.backend.entity.MemberInfo;
//import com.dsapkl.backend.service.MemberInfoService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("user/api/member-info")
//@RequiredArgsConstructor
//public class MemberInfoRestController {
//
//    private final MemberInfoService memberInfoService;
//
//    @PostMapping("/update/{memberId}")
//    public Map<String, String> updateMemberInfo(@PathVariable Long memberId,
//                                @Valid @ModelAttribute MemberInfoCreateDto memberInfoCreateDto,
//                                BindingResult result) {
//
//        Map<String, String> response = new HashMap<>();
//
//        try {
//            if (result.hasErrors()) {
//                response.put("status", "error");
//                response.put("message", "Validation failed");
//                return response;
//            }
//
//            memberInfoService.updateMemberInfo(memberId, memberInfoCreateDto);
//
//            response.put("status", "success");
//            response.put("message", "변경사항이 저장되었습니다.");
//            return response;
//
//        } catch (Exception e) {
//            response.put("status", "error");
//            response.put("message", e.getMessage());
//            return response;
//        }
//    }
//
//}
