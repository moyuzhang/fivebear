package com.fivebear.fivebear_system.controller;

import com.fivebear.fivebear_system.entity.ProxyIp;
import com.fivebear.fivebear_system.model.IpInfo;
import com.fivebear.fivebear_system.service.ProxyIpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proxy-ips")
public class ProxyIpController {

    @Autowired
    private ProxyIpService proxyIpService;

    @GetMapping
    public ResponseEntity<List<ProxyIp>> getAllProxies() {
        return ResponseEntity.ok(proxyIpService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProxyIp> getProxyById(@PathVariable Long id) {
        ProxyIp proxy = proxyIpService.findById(id);
        return proxy != null ? ResponseEntity.ok(proxy) : ResponseEntity.notFound().build();
    }

    @GetMapping("/active")
    public ResponseEntity<List<ProxyIp>> getActiveProxies() {
        return ResponseEntity.ok(proxyIpService.findAvailableProxies());
    }

    @GetMapping("/protocol/{protocol}")
    public ResponseEntity<List<ProxyIp>> getProxiesByProtocol(@PathVariable String protocol) {
        return ResponseEntity.ok(proxyIpService.findByProtocol(protocol));
    }

    @GetMapping("/country/{country}")
    public ResponseEntity<List<ProxyIp>> getProxiesByCountry(@PathVariable String country) {
        return ResponseEntity.ok(proxyIpService.findByCountry(country));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<ProxyIp>> getProxiesByCity(@PathVariable String city) {
        return ResponseEntity.ok(proxyIpService.findByCity(city));
    }

    @PostMapping
    public ResponseEntity<ProxyIp> createProxy(@RequestBody ProxyIp proxyIp) {
        return ResponseEntity.ok(proxyIpService.save(proxyIp));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProxyIp> updateProxy(@PathVariable Long id, @RequestBody ProxyIp proxyIp) {
        proxyIp.setId(id);
        return ResponseEntity.ok(proxyIpService.update(proxyIp));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProxy(@PathVariable Long id) {
        proxyIpService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test")
    public ResponseEntity<IpInfo> testProxy(@RequestParam String ip,
                                          @RequestParam Integer port,
                                          @RequestParam String protocol) {
        return ResponseEntity.ok(proxyIpService.testProxy(ip, port, protocol));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshProxies() {
        proxyIpService.refreshProxyStatus();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cleanup")
    public ResponseEntity<Void> cleanupInactiveProxies() {
        proxyIpService.removeInactiveProxies();
        return ResponseEntity.ok().build();
    }
} 