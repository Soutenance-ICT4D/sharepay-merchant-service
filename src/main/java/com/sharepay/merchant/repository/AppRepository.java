package com.sharepay.merchant.repository;

import com.sharepay.merchant.entity.App;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AppRepository extends JpaRepository<App, UUID> {
}
