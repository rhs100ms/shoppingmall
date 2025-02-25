package com.dsapkl.backend.dto;

import com.dsapkl.backend.entity.CompareStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompareResult {
    private CompareStatus status;
    private int difference;
}
