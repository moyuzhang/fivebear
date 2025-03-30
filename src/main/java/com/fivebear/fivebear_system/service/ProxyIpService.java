package com.fivebear.fivebear_system.service;

import com.fivebear.fivebear_system.entity.ProxyIp;
import com.fivebear.fivebear_system.model.IpInfo;
import java.util.List;

public interface ProxyIpService {
    ProxyIp save(ProxyIp proxyIp);
    ProxyIp findById(Long id);
    List<ProxyIp> findAll();
    List<ProxyIp> findByIsActive(Boolean isActive);
    void deleteById(Long id);
    ProxyIp update(ProxyIp proxyIp);
    List<ProxyIp> findByProtocol(String protocol);
    List<ProxyIp> findByCountry(String country);
    List<ProxyIp> findByCity(String city);
    ProxyIp findByIpAndPort(String ip, Integer port);
    List<ProxyIp> findAvailableProxies();
    IpInfo testProxy(String ip, Integer port, String protocol);
    void refreshProxyStatus();
    void removeInactiveProxies();
} 