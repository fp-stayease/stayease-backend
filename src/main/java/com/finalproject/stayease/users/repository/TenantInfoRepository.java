package com.finalproject.stayease.users.repository;

import com.finalproject.stayease.users.entity.TenantInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantInfoRepository extends JpaRepository<TenantInfo, Long> {

}
