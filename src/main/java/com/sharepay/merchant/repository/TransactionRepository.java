package com.sharepay.merchant.repository;

import com.sharepay.merchant.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    long countByPaymentLink_Id(UUID paymentLinkId);
}
