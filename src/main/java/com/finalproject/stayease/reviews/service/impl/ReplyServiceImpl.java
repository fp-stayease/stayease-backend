package com.finalproject.stayease.reviews.service.impl;

import com.finalproject.stayease.exceptions.DataNotFoundException;
import com.finalproject.stayease.reviews.dto.AdminReplyReqDto;
import com.finalproject.stayease.reviews.entity.Reply;
import com.finalproject.stayease.reviews.entity.Review;
import com.finalproject.stayease.reviews.repository.ReplyRepository;
import com.finalproject.stayease.reviews.service.ReplyService;
import com.finalproject.stayease.reviews.service.ReviewService;
import org.springframework.stereotype.Service;

@Service
public class ReplyServiceImpl implements ReplyService {
    private final ReplyRepository replyRepository;
    private final ReviewService reviewService;

    public ReplyServiceImpl(ReplyRepository replyRepository, ReviewService reviewService) {
        this.replyRepository = replyRepository;
        this.reviewService = reviewService;
    }

    @Override
    public Reply findReplyById(Long replyId) {
        return replyRepository.findById(replyId)
                .orElseThrow(() -> new DataNotFoundException("Reply Not Found"));
    }

    @Override
    public Reply addReply(AdminReplyReqDto reqDto, Long reviewId, Long userId) {
        Review review = reviewService.findReviewById(reviewId);

        Reply reply = new Reply();
        reply.setReview(review);
        reply.setUserId(userId);
        reply.setComment(reqDto.getComment());

        return replyRepository.save(reply);
    }

    @Override
    public Reply updateReply(AdminReplyReqDto reqDto, Long replyId) {
        Reply reply = findReplyById(replyId);
        reply.setComment(reqDto.getComment());

        return replyRepository.save(reply);
    }

    @Override
    public void deleteReply(Long replyId) {
        Reply reply = findReplyById(replyId);
        reply.preRemove();

        replyRepository.save(reply);
    }
}
