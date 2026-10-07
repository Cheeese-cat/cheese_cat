// src/main/java/com/xiaoyan/aiassistant/auth/service/AuthService.java
package com.xiaoyan.aiassistant.auth.service;

import com.xiaoyan.aiassistant.auth.dto.LoginResponse;
import com.xiaoyan.aiassistant.auth.entity.User;
import com.xiaoyan.aiassistant.auth.util.JwtUtil;
import com.xiaoyan.aiassistant.auth.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordUtil passwordUtil;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        user.setCreatedAt(rs.getLong("created_at"));
        user.setUpdatedAt(rs.getLong("updated_at"));
        return user;
    };

    public LoginResponse login(String username, String password) {
        System.out.println("🔐 登录尝试: " + username);
        try {
            User user = jdbcTemplate.queryForObject(
                "SELECT * FROM users WHERE username = ?",
                userRowMapper,
                username
            );

            if (user == null) {
                System.out.println("❌ 用户不存在: " + username);
                return LoginResponse.error("用户不存在，请检查账号是否正确");
            }

            System.out.println("✅ 找到用户: " + username);
            System.out.println("🔑 数据库加密密码: " + user.getPassword());
            System.out.println("🔑 输入明文密码: " + password);

            // 验证密码
            boolean passwordMatch = passwordUtil.matches(password, user.getPassword());
            System.out.println("🔍 密码验证结果: " + (passwordMatch ? "✅ 匹配" : "❌ 不匹配"));

            if (!passwordMatch) {
                return LoginResponse.error("密码错误");
            }

            System.out.println("✅ 密码验证通过，生成 Token...");
            String token = jwtUtil.generateToken(username);

            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getUsername(),
                user.getRole()
            );

            System.out.println("✅ 登录成功: " + username);
            return LoginResponse.success(token, userInfo);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            System.out.println("❌ 用户不存在: " + username);
            return LoginResponse.error("用户不存在，请检查账号是否正确");
        } catch (Exception e) {
            System.out.println("❌ 登录异常: " + e.getMessage());
            e.printStackTrace();
            return LoginResponse.error("登录失败：" + e.getMessage());
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthService.class);

    public boolean verifyToken(String token) {
        if (token == null || token.isEmpty()) {
            log.debug("Token 为空");
            return false;
        }
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        boolean valid = jwtUtil.validateToken(token);
        log.debug("Token 验证结果: {}", valid ? "有效" : "无效");
        return valid;
    }

    public String extractUsername(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.extractUsername(token);
    }

    public void createDefaultUser() {
        try {
            System.out.println("🔍 检查数据库 users 表...");

            // 检查表是否存在
            try {
                jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
            } catch (Exception e) {
                System.out.println("⚠️ users 表不存在，尝试创建...");
                // 表不存在，创建表
                String createTableSql = "CREATE TABLE IF NOT EXISTS users (" +
                    "id BIGINT PRIMARY KEY AUTO_INCREMENT, " +
                    "username VARCHAR(64) NOT NULL UNIQUE, " +
                    "password VARCHAR(256) NOT NULL, " +
                    "email VARCHAR(128), " +
                    "role VARCHAR(32) DEFAULT 'USER', " +
                    "created_at BIGINT NOT NULL, " +
                    "updated_at BIGINT NOT NULL" +
                    ")";
                jdbcTemplate.execute(createTableSql);
                System.out.println("✅ users 表创建成功");
            }

            // 查询用户数量
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users",
                Integer.class
            );
            System.out.println("📊 当前用户数量: " + count);

            if (count == 0) {
                System.out.println("🔧 创建默认管理员账号...");

                // ============================================================
                // 在这里修改默认账号密码
                // ============================================================
                String defaultUsername = "admin";        // ← 账号
                String defaultPassword = "admin123";     // ← 密码
                String defaultEmail = "admin@xiaoyan.com";
                String defaultRole = "ADMIN";
                // ============================================================

                // 加密密码
                String encryptedPassword = passwordUtil.encrypt(defaultPassword);
                System.out.println("明文密码: " + defaultPassword);
                System.out.println("加密后密码: " + encryptedPassword);

                // 插入数据库
                int result = jdbcTemplate.update(
                    "INSERT INTO users (username, password, email, role, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)",
                    defaultUsername,
                    encryptedPassword,
                    defaultEmail,
                    defaultRole,
                    System.currentTimeMillis(),
                    System.currentTimeMillis()
                );

                if (result > 0) {
                    System.out.println("✅ 默认管理员账号创建成功!");
                    System.out.println(" 账号: " + defaultUsername);
                    System.out.println(" 密码: " + defaultPassword);
                    System.out.println(" 邮箱: " + defaultEmail);
                    System.out.println(" 角色: " + defaultRole);
                } else {
                    System.out.println("❌ 默认管理员账号创建失败");
                }
            } else {
                System.out.println("ℹ️ users 表已有 " + count + " 条数据，跳过创建");
                // 打印已有用户列表
                System.out.println("已有用户列表:");
                jdbcTemplate.query("SELECT username, role, email FROM users", (rs, rowNum) -> {
                    System.out.println("   - 账号: " + rs.getString("username") +
                        ", 角色: " + rs.getString("role") +
                        ", 邮箱: " + rs.getString("email"));
                    return null;
                });
            }
        } catch (Exception e) {
            System.out.println("❌ 创建默认用户失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
