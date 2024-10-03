package com.finalproject.stayease.payment.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.finalproject.stayease.bookings.entity.Booking;
import com.finalproject.stayease.payment.entity.dto.PaymentDTO;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Entity
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "amount")
    private Double amount;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "payment_proof")
    private String paymentProof;

    @Column(name = "bank_va")
    private String bankVa;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "payment_expiration_at")
    private Instant paymentExpirationAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    @ColumnDefault("CURRENT_TIMESTAMP")
    private Instant updatedAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }

    @PreRemove
    public void preRemove() {
        this.deletedAt = Instant.now();
    }

    public PaymentDTO toResDto() {
        PaymentDTO resDto = new PaymentDTO();
        resDto.setId(id);
        resDto.setAmount(amount);
        resDto.setPaymentMethod(paymentMethod);
        resDto.setPaymentStatus(paymentStatus);
        resDto.setPaymentProof(paymentProof);
        resDto.setBankVa(bankVa);
        resDto.setPaymentExpirationAt(paymentExpirationAt);
        return resDto;
    }
}
