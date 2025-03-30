package com.fivebear.fivebear_system.repository;

import com.fivebear.fivebear_system.entity.IpPoolSettings;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IpPoolRepository extends KeyValueRepository<IpPoolSettings, String> {
    // 继承 KeyValueRepository 以支持 Redis 操作
    // 泛型参数：<实体类型, ID类型>
} 