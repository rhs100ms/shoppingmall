package com.dsapkl.backend.restcontroller.user;

import com.dsapkl.backend.dto.PasswordUpdateDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/api")
@RequiredArgsConstructor
public class UserInfoRestController {

    @PostMapping("/member-info/update-password/{memberId}")
    public ResponseEntity<?> updatePassword(@PathVariable("memberId") String memberId, @RequestBody PasswordUpdateDto passwordUpdateDto) {

    }
}
