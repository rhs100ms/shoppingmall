package com.dsapkl.backend.repository;

import com.dsapkl.backend.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findByReviewIdAndDeleteYN(Long reviewId, String deleteYN);
} 