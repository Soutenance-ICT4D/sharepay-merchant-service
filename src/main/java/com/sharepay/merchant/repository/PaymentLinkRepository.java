package com.sharepay.merchant.repository;

import com.sharepay.merchant.entity.PaymentLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentLinkRepository extends JpaRepository<PaymentLink, UUID> {

    List<PaymentLink> findAllByApp_OwnerUser_Id(UUID ownerUserId);

    java.util.Optional<PaymentLink> findByIdAndApp_OwnerUser_Id(UUID id, UUID ownerUserId);

    java.util.Optional<PaymentLink> findByCodeAndApp_OwnerUser_Id(String code, UUID ownerUserId);

    java.util.Optional<PaymentLink> findByCode(String code);

    boolean existsByCode(String code);
}
