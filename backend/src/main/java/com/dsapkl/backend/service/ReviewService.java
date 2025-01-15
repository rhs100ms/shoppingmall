package com.dsapkl.backend.service;

import com.dsapkl.backend.entity.Review;
import com.dsapkl.backend.entity.Item;
import com.dsapkl.backend.entity.Member;
import com.dsapkl.backend.entity.ReviewImage;
import com.dsapkl.backend.repository.ItemRepository;
import com.dsapkl.backend.repository.MemberRepository;
import com.dsapkl.backend.repository.ReviewImageRepository;
import com.dsapkl.backend.repository.ReviewRepository;
import com.dsapkl.backend.dto.ReviewRequestDto;
import com.dsapkl.backend.dto.ReviewResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final FileHandler fileHandler;
    private final ReviewImageRepository reviewImageRepository;

    // 리뷰 작성
    @Transactional
    public Long createReview(ReviewRequestDto requestDto, Long memberId, List<MultipartFile> images) throws IOException {
        Item item = itemRepository.findById(requestDto.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
        
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // 이미 리뷰를 작성했는지 확인
        if (reviewRepository.findByItemIdAndMemberId(item.getId(), member.getId()).isPresent()) {
            throw new IllegalStateException("이미 리뷰를 작성하셨습니다.");
        }

        Review review = Review.builder()
                .item(item)
                .member(member)
                .content(requestDto.getContent())
                .rating(requestDto.getRating())
                .build();

        // 이미지 처리
        if (images != null && !images.isEmpty()) {
            List<ReviewImage> reviewImages = fileHandler.storeFiles(images);
            for (ReviewImage image : reviewImages) {
                review.addReviewImage(image);
            }
        }

        return reviewRepository.save(review).getId();
    }

    // 리뷰 수정
    public void updateReview(Long reviewId, ReviewRequestDto requestDto, Long memberId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

        if (!review.getMember().getId().equals(memberId)) {
            throw new IllegalStateException("리뷰 작성자만 수정할 수 있습니다.");
        }

        review.update(requestDto.getContent(), requestDto.getRating());
    }

    // 리뷰 삭제
    public void deleteReview(Long reviewId, Long memberId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

        if (!review.getMember().getId().equals(memberId)) {
            throw new IllegalStateException("리뷰 작성자만 삭제할 수 있습니다.");
        }

        reviewRepository.delete(review);
    }

    // 상품의 모든 리뷰 조회
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getItemReviews(Long itemId) {
        return reviewRepository.findByItemIdOrderByCreatedDateDesc(itemId).stream()
                .map(ReviewResponseDto::from)
                .collect(Collectors.toList());
    }

    // 상품의 평균 평점 조회
    @Transactional(readOnly = true)
    public double getItemAverageRating(Long itemId) {
        return reviewRepository.findAverageRatingByItemId(itemId).orElse(0.0);
    }

    @Transactional(readOnly = true)
    public Review findById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));
    }

    // 회원의 모든 리뷰 조회
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getMemberReviews(Long memberId) {
        return reviewRepository.findByMemberIdOrderByCreatedDateDesc(memberId).stream()
                .map(ReviewResponseDto::from)
                .collect(Collectors.toList());
    }
} 