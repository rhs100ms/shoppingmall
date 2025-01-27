package com.dsapkl.backend.repository;

import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByName(String name);
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    Optional<Member> findByBirthDateAndPhoneNumber(String birthDate, String phoneNumber);
    Optional<Member> findByEmailAndPhoneNumber(String email, String phoneNumber);

    List<Member> findByRole(Role role);

    Page<Member> findByRole(Role role, Pageable pageable);


    List<Member> findByNameContainingOrEmailContainingOrPhoneNumberContaining(String name, String email, String phoneNumber);

    List<Member> findByNameContaining(String searchKeyword);

    List<Member> findByEmailContaining(String searchKeyword);

    List<Member> findByPhoneNumberContaining(String searchKeyword);
}
