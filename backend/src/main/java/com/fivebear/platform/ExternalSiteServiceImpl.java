package com.fivebear.platform;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 站点数据库服务实现类
 */
@Service
public class ExternalSiteServiceImpl implements ExternalSiteService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private final RowMapper<SiteConfig> siteConfigRowMapper = (rs, rowNum) -> {
        SiteConfig config = new SiteConfig();
        config.setId(rs.getLong("id"));
        config.setUrl(rs.getString("url"));
        config.setUsername(rs.getString("username"));
        config.setPassword(rs.getString("password"));
        config.setRebateRate(rs.getDouble("rebate_rate"));
        config.setLotteryType(rs.getInt("lottery_type"));
        config.setClientType(rs.getInt("client_type"));
        config.setClientRole(rs.getInt("client_role"));
        config.setDomainAccount(rs.getString("domain_account"));
        config.setCreatedByUserId(rs.getString("created_by_user_id"));
        config.setMultiplier(rs.getDouble("multiplier"));
        return config;
    };
    
    @Override
    @Transactional
    public Long saveSite(Site site) {
        try {
            String sql = """
                INSERT INTO external_sites (
                    url, username, password, rebate_rate, lottery_type,
                    client_type, client_role, domain_account, created_by_user_id,
                    multiplier, status, last_operation_time, last_operation_user_id,
                    last_operation_type
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
            
            KeyHolder keyHolder = new GeneratedKeyHolder();
            
            jdbcTemplate.update(connection -> {
                var ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, site.getUrl());
                ps.setString(2, site.getUsername());
                ps.setString(3, site.getPassword());
                ps.setDouble(4, site.getRebateRate());
                ps.setInt(5, site.getLotteryType().ordinal() + 1);
                ps.setInt(6, determineClientType(site));
                ps.setInt(7, 1); // 默认client_role为1
                ps.setString(8, site.getUniqueKey());
                ps.setString(9, site.getUserId());
                ps.setDouble(10, 1.0); // 默认multiplier
                ps.setInt(11, site.getSiteStatus().ordinal());
                ps.setTimestamp(12, Timestamp.valueOf(LocalDateTime.now()));
                ps.setString(13, site.getUserId());
                ps.setString(14, "CREATE");
                return ps;
            }, keyHolder);
            
            return keyHolder.getKey().longValue();
            
        } catch (DataAccessException e) {
            throw new RuntimeException("保存站点到数据库失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<SiteConfig> loadSitesByUserId(String userId) {
        try {
            String sql = """
                SELECT id, url, username, password, rebate_rate, lottery_type,
                       client_type, client_role, domain_account, created_by_user_id,
                       multiplier
                FROM external_sites 
                WHERE created_by_user_id = ?
                ORDER BY id DESC
                """;
            
            return jdbcTemplate.query(sql, siteConfigRowMapper, userId);
            
        } catch (DataAccessException e) {
            throw new RuntimeException("从数据库加载站点失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean existsByUserIdAndDomainAccount(String userId, String domainAccount) {
        try {
            String sql = """
                SELECT COUNT(*) FROM external_sites 
                WHERE created_by_user_id = ? AND domain_account = ?
                """;
            
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, domainAccount);
            return count != null && count > 0;
            
        } catch (DataAccessException e) {
            throw new RuntimeException("检查站点唯一性失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<SiteConfig> findByUserIdAndDomainAccount(String userId, String domainAccount) {
        try {
            String sql = """
                SELECT id, url, username, password, rebate_rate, lottery_type,
                       client_type, client_role, domain_account, created_by_user_id,
                       multiplier
                FROM external_sites 
                WHERE created_by_user_id = ? AND domain_account = ?
                """;
            
            List<SiteConfig> results = jdbcTemplate.query(sql, siteConfigRowMapper, userId, domainAccount);
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
            
        } catch (DataAccessException e) {
            throw new RuntimeException("查找站点失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public void updateSiteStatus(Long databaseId, int status, String operatorUserId, String operationType) {
        try {
            String sql = """
                UPDATE external_sites 
                SET status = ?, last_operation_time = ?, last_operation_user_id = ?, 
                    last_operation_type = ?
                WHERE id = ?
                """;
            
            int rowsAffected = jdbcTemplate.update(sql, 
                status, 
                Timestamp.valueOf(LocalDateTime.now()),
                operatorUserId,
                operationType,
                databaseId
            );
            
            if (rowsAffected == 0) {
                throw new RuntimeException("站点不存在，ID: " + databaseId);
            }
            
        } catch (DataAccessException e) {
            throw new RuntimeException("更新站点状态失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public void deleteSite(Long databaseId, String operatorUserId) {
        try {
            String sql = "DELETE FROM external_sites WHERE id = ?";
            int rowsAffected = jdbcTemplate.update(sql, databaseId);
            
            if (rowsAffected == 0) {
                throw new RuntimeException("站点不存在，ID: " + databaseId);
            }
            
        } catch (DataAccessException e) {
            throw new RuntimeException("删除站点失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean checkSiteOwnership(Long databaseId, String userId) {
        try {
            String sql = """
                SELECT COUNT(*) FROM external_sites 
                WHERE id = ? AND created_by_user_id = ?
                """;
            
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, databaseId, userId);
            return count != null && count > 0;
            
        } catch (DataAccessException e) {
            throw new RuntimeException("检查站点所有权失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 根据站点类型确定client_type
     */
    private int determineClientType(Site site) {
        if (site instanceof AdminSite) {
            return 2; // ADMIN类型
        } else if (site instanceof MemberSite) {
            return 1; // MEMBER类型
        } else {
            return 1; // 默认MEMBER类型
        }
    }
    
    /**
     * 批量更新站点状态（用于批量操作）
     */
    @Transactional
    public void batchUpdateSiteStatus(List<Long> databaseIds, int status, String operatorUserId, String operationType) {
        try {
            String sql = """
                UPDATE external_sites 
                SET status = ?, last_operation_time = ?, last_operation_user_id = ?, 
                    last_operation_type = ?
                WHERE id = ?
                """;
            
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            
            for (Long databaseId : databaseIds) {
                jdbcTemplate.update(sql, status, now, operatorUserId, operationType, databaseId);
            }
            
        } catch (DataAccessException e) {
            throw new RuntimeException("批量更新站点状态失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取用户站点统计信息
     */
    public SiteStatistics getUserSiteStatistics(String userId) {
        try {
            String sql = """
                SELECT 
                    COUNT(*) as total_count,
                    SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as not_logged_in_count,
                    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as logging_in_count,
                    SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as logged_in_count,
                    SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as logged_out_count,
                    SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) as error_count,
                    SUM(CASE WHEN status = 5 THEN 1 ELSE 0 END) as network_error_count
                FROM external_sites 
                WHERE created_by_user_id = ?
                """;
            
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                SiteStatistics stats = new SiteStatistics();
                stats.setTotalCount(rs.getInt("total_count"));
                stats.setNotLoggedInCount(rs.getInt("not_logged_in_count"));
                stats.setLoggingInCount(rs.getInt("logging_in_count"));
                stats.setLoggedInCount(rs.getInt("logged_in_count"));
                stats.setLoggedOutCount(rs.getInt("logged_out_count"));
                stats.setErrorCount(rs.getInt("error_count"));
                stats.setNetworkErrorCount(rs.getInt("network_error_count"));
                return stats;
            }, userId);
            
        } catch (DataAccessException e) {
            throw new RuntimeException("获取站点统计信息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 站点统计信息类
     */
    public static class SiteStatistics {
        private int totalCount;
        private int notLoggedInCount;
        private int loggingInCount;
        private int loggedInCount;
        private int loggedOutCount;
        private int errorCount;
        private int networkErrorCount;
        
        // Getters and Setters
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        
        public int getNotLoggedInCount() { return notLoggedInCount; }
        public void setNotLoggedInCount(int notLoggedInCount) { this.notLoggedInCount = notLoggedInCount; }
        
        public int getLoggingInCount() { return loggingInCount; }
        public void setLoggingInCount(int loggingInCount) { this.loggingInCount = loggingInCount; }
        
        public int getLoggedInCount() { return loggedInCount; }
        public void setLoggedInCount(int loggedInCount) { this.loggedInCount = loggedInCount; }
        
        public int getLoggedOutCount() { return loggedOutCount; }
        public void setLoggedOutCount(int loggedOutCount) { this.loggedOutCount = loggedOutCount; }
        
        public int getErrorCount() { return errorCount; }
        public void setErrorCount(int errorCount) { this.errorCount = errorCount; }
        
        public int getNetworkErrorCount() { return networkErrorCount; }
        public void setNetworkErrorCount(int networkErrorCount) { this.networkErrorCount = networkErrorCount; }
    }
} 