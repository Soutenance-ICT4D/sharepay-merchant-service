package com.sharepay.merchant.repository;

import com.sharepay.merchant.entity.App;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import java.util.UUID;

public interface AppRepository extends JpaRepository<App, UUID> {
    long countByOwnerUser_Id(UUID ownerUserId);

    List<App> findAllByOwnerUser_Id(UUID ownerUserId);

    java.util.Optional<App> findByIdAndOwnerUser_Id(UUID id, UUID ownerUserId);
}
