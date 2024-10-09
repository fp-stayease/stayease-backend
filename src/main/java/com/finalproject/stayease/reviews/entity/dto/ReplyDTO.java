package com.finalproject.stayease.reviews.entity.dto;

import com.finalproject.stayease.reviews.entity.Reply;
import com.finalproject.stayease.users.entity.TenantInfo;
import lombok.Data;

import java.time.Instant;

@Data
public class ReplyDTO {
    private Long id;
    private String reply;
    private TenantSummary tenant;
    private Instant createdAt;

    public ReplyDTO(Reply reply) {
        this.id = reply.getId();
        this.reply = reply.getComment();
        this.tenant = new TenantSummary(reply.getTenant());
        this.createdAt = reply.getCreatedAt();
    }

    @Data
    static class TenantSummary {
        private Long id;
        private String businessName;
        private String avatar;

        TenantSummary(TenantInfo tenant) {
            this.id = tenant.getId();
            this.businessName = tenant.getBusinessName();
            this.avatar = tenant.getUser().getAvatar();
        }
    }
}
