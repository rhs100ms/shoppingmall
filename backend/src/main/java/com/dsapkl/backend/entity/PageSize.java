package com.dsapkl.backend.entity;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

public enum PageSize {
    TEN(10, "10 per page"),
    TWENTY(20, "20 per page"),
    FIFTY(50, "50 per page");

    private final int value;
    private final String displayName;

    PageSize(int value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }
}
