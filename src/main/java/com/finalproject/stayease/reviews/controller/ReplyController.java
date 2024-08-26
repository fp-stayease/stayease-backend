package com.finalproject.stayease.reviews.controller;

import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.reviews.dto.AdminReplyReqDto;
import com.finalproject.stayease.reviews.service.ReplyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/replies")
public class ReplyController {
    private final ReplyService replyService;

    public ReplyController(ReplyService replyService) {
        this.replyService = replyService;
    }

    @PostMapping("/{reviewId}")
    public ResponseEntity<?> addAdminReply(@PathVariable Long reviewId, @RequestBody AdminReplyReqDto reqDto) {
        Long userId = 1L;
        return Response.successfulResponse(HttpStatus.CREATED.value(), "Reply posted", replyService.addReply(reqDto, reviewId, userId));
    }

    @GetMapping("/{replyId}")
    public ResponseEntity<?> getAdminReplyDetail(@PathVariable Long replyId) {
        return Response.successfulResponse("Reply detail fetched", replyService.findReplyById(replyId));
    }

    @PutMapping("/{replyId}")
    public ResponseEntity<?> updateAdminReply(@PathVariable Long replyId, @RequestBody AdminReplyReqDto reqDto) {
        return Response.successfulResponse("Reply updated", replyService.updateReply(reqDto, replyId));
    }

    @DeleteMapping("/{replyId}")
    public ResponseEntity<?> deleteAdminReply(@PathVariable Long replyId) {
        replyService.deleteReply(replyId);
        return Response.successfulResponse("Reply deleted");
    }
}
