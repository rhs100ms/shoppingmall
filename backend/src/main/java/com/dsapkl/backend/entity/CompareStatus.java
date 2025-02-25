package com.dsapkl.backend.entity;

public enum CompareStatus {
    SHEET_MORE,   // 구글 시트의 행이 더 많음 (새상품)
    DB_MORE,      // DB의 행이 더 많음 (삭제된 상품)
    EQUAL         // 행 수가 같음 (상세 비교 필요)
}
