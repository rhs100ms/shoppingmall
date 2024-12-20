package com.dsapkl.backend.controller;

import com.dsapkl.backend.entity.Address;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.service.MemberService;
import groovy.util.logging.Slf4j;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MemberController {
    
    private final MemberService memberService;
    
    //회원가입
    @GetMapping("/members/new")
    public String createMemberForm(@ModelAttribute("memberForm") MemberForm memberForm, Model model) {
        List<RoleCode> roleCodes = new ArrayList<>();
        roleCodes.add(new RoleCode("admin","판매자"));
        roleCodes.add(new RoleCode("user","구매자"));
        model.addAttribute("roleCodes", roleCodes);

        return "members/createMemberForm";
    }

    @Data
    @AllArgsConstructor
    static class RoleCode {
        private String code;
        private String displayName;
    }

    @Getter
    @Setter
    public class MemberForm {

        @NotEmpty(message = "이름은 필수")
        private String name;
        @NotEmpty(message = "이메일 필수")
        @Email
        private String email;
        @NotEmpty(message = "비밀번호 필수")
        private String password;

        private String city;
        private String street;
        private String zipcode;

        private String role;

    }

    public String createMember(@Valid @ModelAttribute MemberForm memberForm,
                               BindingResult bindingResult, Model model,
                               @RequestParam("role") String role) {
        //이름, 이메일, 패스워드 중 하나라도 입력 안했을 때
        if(bindingResult.hasErrors()) {
            List<RoleCode> roleCodes = new ArrayList<>();
            roleCodes.add(new RoleCode("admin", "판매자"));
            roleCodes.add(new RoleCode("user", "구매자"));
            model.addAttribute("roleCodes", roleCodes);
            return "members/createMemberForm";
        }

        Address address = new Address(
                memberForm.getCity(),
                memberForm.getStreet(),
                memberForm.getZipcode());

        try {
            Member member = Member.builder()
                    .name(memberForm.getName())
                    .email(memberForm.getEmail())
                    .password(memberForm.getPassword())
                    .address(address)
                    .build();

            member.changeRole(role);
            memberService.join(member);
        } catch (IllegalStateException e){
            model.addAttribute("errorMessage", e.getMessage());
            return "members/createMemberForm";
        }
        return "redirect:/members";
    }
}
