package com.fivebear.fivebear_system.repository.redis;

import com.fivebear.fivebear_system.entity.ProxyIp;
import org.springframework.data.keyvalue.repository.KeyValueRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProxyIpRepository extends KeyValueRepository<ProxyIp, Long> {
    List<ProxyIp> findByIsActive(Boolean isActive);
    List<ProxyIp> findByProtocol(String protocol);
    List<ProxyIp> findByCountry(String country);
    List<ProxyIp> findByCity(String city);
    ProxyIp findByIpAndPort(String ip, Integer port);
} 