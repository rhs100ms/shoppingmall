//package com.dsapkl.backend.service;
//
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//import lombok.RequiredArgsConstructor;
//import com.dsapkl.backend.entity.Member;
//import com.dsapkl.backend.repository.MemberRepository;
//import org.springframework.security.core.userdetails.User;
//
//@Service
//@RequiredArgsConstructor
//public class CustomUserDetailsService implements UserDetailsService {
//    private final MemberRepository memberRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        Member member = memberRepository.findByEmail(username)
//            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        return User.builder()
//            .username(member.getEmail())
//            .password(member.getPassword())
//            .roles(member.getRole().name())
//            .build();
//    }
//}