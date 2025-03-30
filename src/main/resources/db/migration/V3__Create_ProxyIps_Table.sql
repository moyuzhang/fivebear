CREATE TABLE IF NOT EXISTS proxy_ips (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip VARCHAR(50) NOT NULL,
    port INT NOT NULL,
    type VARCHAR(10) NOT NULL,
    location VARCHAR(100),
    available BOOLEAN DEFAULT TRUE,
    request_time BIGINT,
    response_time BIGINT,
    use_time BIGINT,
    last_validate_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_ip_port (ip, port)
); 