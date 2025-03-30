-- IP池表
CREATE TABLE ip_pool (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip VARCHAR(50) NOT NULL,
    port INT NOT NULL,
    location VARCHAR(255),
    speed INT,
    last_verify_time DATETIME,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_status (status),
    INDEX idx_ip_port (ip, port)
);

-- IP来源表
CREATE TABLE ip_source (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url VARCHAR(255) NOT NULL,
    description TEXT,
    last_fetch_time DATETIME,
    success_count INT DEFAULT 0,
    fail_count INT DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    UNIQUE INDEX idx_url (url)
);

-- IP池配置表
CREATE TABLE ip_pool_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    api_key VARCHAR(255),
    request_interval INT DEFAULT 60,
    max_concurrency INT DEFAULT 3,
    test_url VARCHAR(255),
    test_timeout INT DEFAULT 10,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

-- 插入默认配置
INSERT INTO ip_pool_config (
    api_key,
    request_interval,
    max_concurrency,
    test_url,
    test_timeout,
    created_at,
    updated_at
) VALUES (
    NULL,
    60,
    3,
    'https://www.baidu.com',
    10,
    NOW(),
    NOW()
); 