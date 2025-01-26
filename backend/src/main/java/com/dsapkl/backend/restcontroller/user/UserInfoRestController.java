package com.dsapkl.backend.restcontroller.user;

import com.dsapkl.backend.dto.PasswordUpdateDto;
import com.dsapkl.backend.service.MemberInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user/api")
@RequiredArgsConstructor
public class UserInfoRestController {

    private final MemberInfoService memberInfoService;

    @PostMapping("/member-info/update-password/{memberId}")
    public ResponseEntity<?> updatePassword(@PathVariable("memberId") Long memberId, @RequestBody PasswordUpdateDto passwordUpdateDto) {

        try {
            memberInfoService.updatePassword(memberId, passwordUpdateDto.getCurrentPassword(), passwordUpdateDto.getNewPassword());

            return ResponseEntity.ok().body(Map.of("message", "Password updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }

    }
}
