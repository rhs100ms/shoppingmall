package com.dsapkl.backend.controller;

import com.dsapkl.backend.controller.dto.MemberInfoCreateDto;
import com.dsapkl.backend.controller.dto.MemberInfoResponseDto;
import com.dsapkl.backend.entity.MemberInfo;
import com.dsapkl.backend.service.MemberInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/member-info")
@RequiredArgsConstructor
public class MemberInfoController {

    private final MemberInfoService memberInfoService;

//    @GetMapping("/{memberId}")
//    public ResponseEntity<MemberInfoResponseDto> getMemberInfo(@PathVariable Long memberId) {
//        MemberInfo memberInfo = memberInfoService.findMemberInfo(memberId);
//        return ResponseEntity.ok(MemberInfoResponseDto.of(memberInfo));
//    }
//
//    @PostMapping("/update-login/{memberId}")
//    public ResponseEntity<Void> updateLoginInfo(@PathVariable Long memberId) {
//        memberInfoService.updateMemberLoginInfo(memberId);
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/update-statistics/{memberId}")
//    public ResponseEntity<Void> updateStatistics(@PathVariable Long memberId) {
//        memberInfoService.updateAllStatistics(memberId);
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/update-purchase/{memberId}")
//    public ResponseEntity<Void> updatePurchaseInfo(@PathVariable Long memberId) {
//        memberInfoService.updatePurchaseStatistics(memberId);
//        return ResponseEntity.ok().build();
//    }
//
//    @PostMapping("/update-preference/{memberId}")
//    public ResponseEntity<Void> updatePreference(@PathVariable Long memberId) {
//        memberInfoService.updateProductPreference(memberId);
//        return ResponseEntity.ok().build();
//    }

    @GetMapping("/update/{memberId}")
    public String updateMemberInfoForm(@PathVariable Long memberId, Model model) {
        MemberInfo memberInfo = memberInfoService.findMemberInfo(memberId);
        model.addAttribute("memberInfo", MemberInfoResponseDto.of(memberInfo));
        model.addAttribute("memberInfoCreateDto", new MemberInfoCreateDto());
        return "members/updateMemberInfoForm";
    }

    @PostMapping("/update/{memberId}")
    public String updateMemberInfo(@PathVariable Long memberId, 
                                 @Valid @ModelAttribute MemberInfoCreateDto memberInfoCreateDto,
                                 BindingResult result,
                                 Model model) {
        try {
            if (result.hasErrors()) {
                MemberInfo memberInfo = memberInfoService.findMemberInfo(memberId);
                model.addAttribute("memberInfo", MemberInfoResponseDto.of(memberInfo));
                return "members/updateMemberInfoForm";
            }

            memberInfoService.updateMemberInfo(memberId, memberInfoCreateDto);
            return "redirect:/";  // 메인 페이지로 리다이렉트
        } catch (Exception e) {
            MemberInfo memberInfo = memberInfoService.findMemberInfo(memberId);
            model.addAttribute("memberInfo", MemberInfoResponseDto.of(memberInfo));
            model.addAttribute("errorMessage", e.getMessage());
            return "members/updateMemberInfoForm";
        }
    }
} 