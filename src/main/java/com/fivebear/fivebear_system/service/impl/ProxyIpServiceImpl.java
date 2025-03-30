package com.fivebear.fivebear_system.service.impl;

import com.fivebear.fivebear_system.entity.ProxyIp;
import com.fivebear.fivebear_system.model.IpInfo;
import com.fivebear.fivebear_system.repository.redis.ProxyIpRepository;
import com.fivebear.fivebear_system.service.ProxyIpService;
import com.fivebear.fivebear_system.utils.HttpClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProxyIpServiceImpl implements ProxyIpService {

    @Autowired
    private ProxyIpRepository proxyIpRepository;

    @Override
    @Transactional
    public ProxyIp save(ProxyIp proxyIp) {
        return proxyIpRepository.save(proxyIp);
    }

    @Override
    public ProxyIp findById(Long id) {
        return proxyIpRepository.findById(id).orElse(null);
    }

    @Override
    public List<ProxyIp> findAll() {
        return proxyIpRepository.findAll();
    }

    @Override
    public List<ProxyIp> findByIsActive(Boolean isActive) {
        return proxyIpRepository.findByIsActive(isActive);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        proxyIpRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ProxyIp update(ProxyIp proxyIp) {
        return proxyIpRepository.save(proxyIp);
    }

    @Override
    public List<ProxyIp> findByProtocol(String protocol) {
        return proxyIpRepository.findByProtocol(protocol);
    }

    @Override
    public List<ProxyIp> findByCountry(String country) {
        return proxyIpRepository.findByCountry(country);
    }

    @Override
    public List<ProxyIp> findByCity(String city) {
        return proxyIpRepository.findByCity(city);
    }

    @Override
    public ProxyIp findByIpAndPort(String ip, Integer port) {
        return proxyIpRepository.findByIpAndPort(ip, port);
    }

    @Override
    public List<ProxyIp> findAvailableProxies() {
        return proxyIpRepository.findByIsActive(true);
    }

    @Override
    public IpInfo testProxy(String ip, Integer port, String protocol) {
        IpInfo ipInfo = new IpInfo();
        ipInfo.setIp(ip);
        ipInfo.setPort(port);
        ipInfo.setProtocol(protocol);

        try {
            long startTime = System.currentTimeMillis();
            HttpClientUtils.get("http://httpbin.org/ip", ip, port, protocol);
            long endTime = System.currentTimeMillis();
            
            ipInfo.setResponseTime(endTime - startTime);
            ipInfo.setValid(true);
        } catch (Exception e) {
            ipInfo.setValid(false);
        }

        return ipInfo;
    }

    @Override
    @Transactional
    public void refreshProxyStatus() {
        List<ProxyIp> proxies = findAll();
        for (ProxyIp proxy : proxies) {
            IpInfo ipInfo = testProxy(proxy.getIp(), proxy.getPort(), proxy.getProtocol());
            proxy.setActive(ipInfo.isValid());
            proxy.setResponseTime(ipInfo.getResponseTime());
            proxy.setLastCheckTime(LocalDateTime.now());
            update(proxy);
        }
    }

    @Override
    @Transactional
    public void removeInactiveProxies() {
        List<ProxyIp> inactiveProxies = findByIsActive(false);
        for (ProxyIp proxy : inactiveProxies) {
            deleteById(proxy.getId());
        }
    }
} 