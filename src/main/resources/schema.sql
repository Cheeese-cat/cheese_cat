-- noinspection SqlNoDataSourceInspectionForFile
-- noinspection SqlResolveForFile
CREATE DATABASE IF NOT EXISTS lc DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lc;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(256) NOT NULL COMMENT '加密密码',
  email VARCHAR(128) COMMENT '邮箱',
  role VARCHAR(32) DEFAULT 'USER' COMMENT '角色: ADMIN/USER',
  created_at BIGINT NOT NULL COMMENT '创建时间戳',
  updated_at BIGINT NOT NULL COMMENT '更新时间戳',
  INDEX idx_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS kb_document (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  file_name VARCHAR(255) NOT NULL,
  content_type VARCHAR(120),
  file_size BIGINT NOT NULL,
  status VARCHAR(32) NOT NULL,
  chunk_count INT NOT NULL DEFAULT 0,
  error_message TEXT,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS kb_chunk (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  document_id BIGINT NOT NULL,
  vector_id VARCHAR(120) NOT NULL,
  chunk_index INT NOT NULL,
  title VARCHAR(500),
  section_path VARCHAR(1000),
  content MEDIUMTEXT NOT NULL,
  token_estimate INT NOT NULL,
  created_at DATETIME NOT NULL,
  INDEX idx_kb_chunk_document_id (document_id),
  UNIQUE KEY uk_kb_chunk_vector_id (vector_id)
);

CREATE TABLE IF NOT EXISTS long_memory (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vector_id VARCHAR(120) NOT NULL,
  user_id VARCHAR(120) NOT NULL,
  title VARCHAR(255),
  content TEXT NOT NULL,
  tags VARCHAR(500),
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_long_memory_user_id (user_id),
  UNIQUE KEY uk_long_memory_vector_id (vector_id)
);
-- 会话表：记录每个会话的元信息
CREATE TABLE IF NOT EXISTS chat_session (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id VARCHAR(120) NOT NULL,
  session_id VARCHAR(120) NOT NULL,
  title VARCHAR(255),
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_chat_session_user (user_id),
  UNIQUE KEY uk_chat_session (user_id, session_id)
);

-- 消息表：记录每条对话
CREATE TABLE IF NOT EXISTS chat_message (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id VARCHAR(120) NOT NULL,
  session_id VARCHAR(120) NOT NULL,
  role VARCHAR(20) NOT NULL,
  content MEDIUMTEXT,
  image MEDIUMTEXT,
  created_at DATETIME NOT NULL,
  INDEX idx_chat_message_user_session (user_id, session_id),
  INDEX idx_chat_message_created (created_at)
);
