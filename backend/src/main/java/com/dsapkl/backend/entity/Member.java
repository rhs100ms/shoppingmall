package com.dsapkl.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.amqp.RabbitConnectionDetails;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String name;
    private String email;
    private String password;

    @Embedded
    private Address address;

    @Enumerated(value = EnumType.STRING)
    private Role role;

    @Builder
    private Member(String name, Address address, String email, String password, Role role) {
        this.name = name;
        this.address = address;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public void changeRole(String role) {
        if (role.equals("admin")) {
            this.role = Role.Admin;
        } else {
            this.role = Role.User;
        }
    }


}
