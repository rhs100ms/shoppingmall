package com.dsapkl.backend.dto;

import com.dsapkl.backend.entity.Interest;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MemberInfoCreateDto {

    private Integer age;
    private String gender;
    private Interest interests;

    // 주소 정보 추가
    private String city;
    private String street;
    private String zipcode;

} 