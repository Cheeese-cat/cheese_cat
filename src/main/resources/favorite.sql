CREATE TABLE favorite (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  user_id      VARCHAR(64)  NOT NULL,
  type         VARCHAR(16)  NOT NULL,   -- 'image' | 'chat'
  title        VARCHAR(128),
  content      TEXT,                    -- image 存 base64；chat 存 JSON
  tags         VARCHAR(255),
  source_type  VARCHAR(32),             -- 'upload' | 'draw' | 'chat'
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_user_created (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
