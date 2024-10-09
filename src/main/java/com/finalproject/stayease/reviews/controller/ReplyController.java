package com.finalproject.stayease.reviews.controller;

import com.finalproject.stayease.responses.Response;
import com.finalproject.stayease.reviews.entity.dto.ReplyDTO;
import com.finalproject.stayease.reviews.entity.dto.request.AdminReplyReqDTO;
import com.finalproject.stayease.reviews.service.ReplyService;
import com.finalproject.stayease.users.entity.Users;
import com.finalproject.stayease.users.service.UsersService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/replies")
public class ReplyController {
    private final ReplyService replyService;
    private final UsersService usersService;

    public ReplyController(ReplyService replyService, UsersService usersService) {
        this.replyService = replyService;
        this.usersService = usersService;
    }

    @PostMapping("/{reviewId}")
    public ResponseEntity<?> addAdminReply(@PathVariable Long reviewId, @RequestBody AdminReplyReqDTO reqDto) {
        Users user = usersService.getLoggedUser();
        var response = replyService.addReply(reqDto, reviewId, user);
        return Response.successfulResponse(HttpStatus.CREATED.value(), "Reply posted", response);
    }

    @GetMapping("/{replyId}")
    public ResponseEntity<?> getAdminReplyDetail(@PathVariable Long replyId) {
        var response = replyService.findReplyById(replyId);
        return Response.successfulResponse("Reply detail fetched", new ReplyDTO(response));
    }

    @PutMapping("/{replyId}")
    public ResponseEntity<?> updateAdminReply(@PathVariable Long replyId, @RequestBody AdminReplyReqDTO reqDto) {
        Users user = usersService.getLoggedUser();
        var response = replyService.updateReply(reqDto, replyId, user);
        return Response.successfulResponse("Reply updated", response);
    }

    @DeleteMapping("/{replyId}")
    public ResponseEntity<?> deleteAdminReply(@PathVariable Long replyId) {
        Users user = usersService.getLoggedUser();
        replyService.deleteReply(replyId, user);
        return Response.successfulResponse("Reply deleted");
    }

    @GetMapping("/list/{reviewId}")
    public ResponseEntity<?> getReviewReplies(@PathVariable Long reviewId,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "5") int size) {
        Sort sort = Sort.by("DESC", "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        var response = replyService.findReviewReplies(reviewId, pageable);

        return Response.successfulResponse(HttpStatus.OK.value(), "Review replies fetched", response);
    }
}