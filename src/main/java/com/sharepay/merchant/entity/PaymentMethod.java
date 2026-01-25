package com.sharepay.merchant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "payment_methods")
public class PaymentMethod {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "payin_percent_fee", precision = 5, scale = 2, nullable = false)
    private BigDecimal payinPercentFee = BigDecimal.ZERO;

    @Column(name = "payout_percent_fee", precision = 5, scale = 2, nullable = false)
    private BigDecimal payoutPercentFee = BigDecimal.ZERO;

    @Column(name = "percent_fee", precision = 5, scale = 2, nullable = false)
    private BigDecimal percentFee = BigDecimal.ZERO;

    @OneToMany(mappedBy = "paymentMethod")
    private Set<Transaction> transactions;
}
