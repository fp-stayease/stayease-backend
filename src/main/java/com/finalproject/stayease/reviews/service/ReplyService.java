package com.finalproject.stayease.reviews.service;

import com.finalproject.stayease.reviews.dto.AdminReplyReqDto;
import com.finalproject.stayease.reviews.entity.Reply;

public interface ReplyService {
    Reply findReplyById(Long replyId);
    Reply addReply(AdminReplyReqDto reqDto, Long reviewId, Long userId);
    Reply updateReply(AdminReplyReqDto reqDto, Long replyId);
    void deleteReply(Long replyId);
}
