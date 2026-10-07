USE lc;

-- 1. 看会话表有没有数据
SELECT * FROM chat_session;

-- 2. 看消息表有没有数据
SELECT id, user_id, session_id, role, LEFT(content, 50) AS preview, created_at
FROM chat_message
ORDER BY id DESC
    LIMIT 20;
