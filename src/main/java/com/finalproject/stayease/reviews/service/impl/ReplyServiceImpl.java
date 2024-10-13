package com.finalproject.stayease.reviews.service.impl;

import com.finalproject.stayease.exceptions.utils.DataNotFoundException;
import com.finalproject.stayease.reviews.entity.Reply;
import com.finalproject.stayease.reviews.entity.Review;
import com.finalproject.stayease.reviews.entity.dto.ReplyDTO;
import com.finalproject.stayease.reviews.entity.dto.request.AdminReplyReqDTO;
import com.finalproject.stayease.reviews.repository.ReplyRepository;
import com.finalproject.stayease.reviews.service.ReplyService;
import com.finalproject.stayease.reviews.service.ReviewService;
import com.finalproject.stayease.users.entity.TenantInfo;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.TenantInfoService;
import lombok.extern.java.Log;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Log
public class ReplyServiceImpl implements ReplyService {
    private final ReplyRepository replyRepository;
    private final ReviewService reviewService;
    private final TenantInfoService tenantInfoService;

    public ReplyServiceImpl(ReplyRepository replyRepository, ReviewService reviewService, TenantInfoService tenantInfoService) {
        this.replyRepository = replyRepository;
        this.reviewService = reviewService;
        this.tenantInfoService = tenantInfoService;
    }

    @Override
    public Reply findReplyById(Long replyId) {
        return replyRepository.findById(replyId)
                .orElseThrow(() -> new DataNotFoundException("Reply Not Found"));
    }

    @Override
    public ReplyDTO addReply(AdminReplyReqDTO reqDto, Long reviewId, Users user) {
        TenantInfo tenant = tenantInfoService.findTenantByUserId(user.getId());
        Review review = reviewService.findReviewById(reviewId);

        if (!Objects.equals(review.getProperty().getTenant().getId(), user.getId())) {
            throw new IllegalArgumentException("Only tenants can reply this reviews");
        }

        Reply reply = new Reply();
        reply.setReview(review);
        reply.setTenant(tenant);
        reply.setComment(reqDto.getComment());

        return new ReplyDTO(replyRepository.save(reply));
    }

    @Override
    public ReplyDTO updateReply(AdminReplyReqDTO reqDto, Long replyId, Users user) {
        TenantInfo tenant = tenantInfoService.findTenantByUserId(user.getId());
        Reply reply = findReplyById(replyId);

        if (!Objects.equals(reply.getTenant().getId(), tenant.getId())) {
            throw new IllegalArgumentException("This is not your reviews, you cannot update this reply");
        }

        reply.setComment(reqDto.getComment());

        return new ReplyDTO(replyRepository.save(reply));
    }

    @Override
    public void deleteReply(Long replyId, Users user) {
        TenantInfo tenant = tenantInfoService.findTenantByUserId(user.getId());
        Reply reply = findReplyById(replyId);

        if (!Objects.equals(reply.getTenant().getId(), tenant.getId())) {
            throw new IllegalArgumentException("This is not your reviews, you cannot delete this reply");
        }

        reply.preRemove();

        replyRepository.save(reply);
    }

    @Override
    public Page<ReplyDTO> findReviewReplies(Long reviewId, Pageable pageable) {
        return replyRepository.findRepliesByReviewId(reviewId, pageable).map(ReplyDTO::new);
    }
}