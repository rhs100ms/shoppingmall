package com.dsapkl.backend.service;

import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
    private final MemberRepository memberRepository;

    //회원가입
    public Long join(Member member) {
        validateDuplicateMember(member);
        Member savedMember = memberRepository.save(member);
        return savedMember.getId();
    }

    //중복 회원 검증
    private void validateDuplicateMember(Member member) {
        Member findMember = memberRepository.findByEmail(member.getEmail()).orElse(null);
        if (findMember != null) {
            throw new IllegalStateException("이미 존재하는 회원");
        }
    }

    public Member findMember(Long id) {
        return memberRepository.findById(id).get();
    }

    public List<Member> findMembers() {
        return memberRepository.findAll();
    }
}
