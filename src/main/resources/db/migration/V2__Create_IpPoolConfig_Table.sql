CREATE TABLE IF NOT EXISTS ip_pool_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    auto_fetch BOOLEAN DEFAULT TRUE,
    fetch_interval INT DEFAULT 30,
    max_ip_count INT DEFAULT 100,
    min_speed INT DEFAULT 1000,
    test_url VARCHAR(255) DEFAULT 'https://www.baidu.com',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 插入默认配置
INSERT INTO ip_pool_config (auto_fetch, fetch_interval, max_ip_count, min_speed, test_url)
VALUES (true, 30, 100, 1000, 'https://www.baidu.com')
ON DUPLICATE KEY UPDATE
    auto_fetch = VALUES(auto_fetch),
    fetch_interval = VALUES(fetch_interval),
    max_ip_count = VALUES(max_ip_count),
    min_speed = VALUES(min_speed),
    test_url = VALUES(test_url); 