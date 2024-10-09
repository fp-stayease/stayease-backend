package com.finalproject.stayease.reviews.service;

import com.finalproject.stayease.reviews.entity.Reply;
import com.finalproject.stayease.reviews.entity.dto.ReplyDTO;
import com.finalproject.stayease.reviews.entity.dto.request.AdminReplyReqDTO;
import com.finalproject.stayease.users.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReplyService {
    Reply findReplyById(Long replyId);
    ReplyDTO addReply(AdminReplyReqDTO reqDto, Long reviewId, Users user);
    ReplyDTO updateReply(AdminReplyReqDTO reqDto, Long replyId, Users user);
    void deleteReply(Long replyId, Users user);
    Page<ReplyDTO> findReviewReplies(Long replyId, Pageable pageable);
}
