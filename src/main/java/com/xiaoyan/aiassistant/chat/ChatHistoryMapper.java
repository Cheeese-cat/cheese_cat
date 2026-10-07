package com.xiaoyan.aiassistant.chat;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
@SuppressWarnings("SqlResolve")
public interface ChatHistoryMapper {

    // ---------- 会话 ----------

    @Insert("""
            insert into chat_session(user_id, session_id, title, created_at, updated_at)
            values(#{userId}, #{sessionId}, #{title}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertSession(ChatSession session);

    @Select("""
            select id, user_id, session_id, title, created_at, updated_at
            from chat_session
            where user_id = #{userId}
            order by updated_at desc
            """)
    List<ChatSession> listSessions(@Param("userId") String userId);

    @Select("""
            select id, user_id, session_id, title, created_at, updated_at
            from chat_session
            where user_id = #{userId} and session_id = #{sessionId}
            """)
    ChatSession findSession(@Param("userId") String userId,
                            @Param("sessionId") String sessionId);

    @Update("""
            update chat_session
            set title = #{title}, updated_at = #{updatedAt}
            where user_id = #{userId} and session_id = #{sessionId}
            """)
    void updateSessionTitle(@Param("userId") String userId,
                            @Param("sessionId") String sessionId,
                            @Param("title") String title,
                            @Param("updatedAt") java.time.LocalDateTime updatedAt);

    @Update("""
            update chat_session
            set updated_at = #{updatedAt}
            where user_id = #{userId} and session_id = #{sessionId}
            """)
    void touchSession(@Param("userId") String userId,
                      @Param("sessionId") String sessionId,
                      @Param("updatedAt") java.time.LocalDateTime updatedAt);

    @Delete("""
            delete from chat_session
            where user_id = #{userId} and session_id = #{sessionId}
            """)
    void deleteSession(@Param("userId") String userId,
                       @Param("sessionId") String sessionId);

    // ---------- 消息 ----------

    @Insert("""
            insert into chat_message(user_id, session_id, role, content, image, created_at)
            values(#{userId}, #{sessionId}, #{role}, #{content}, #{image}, #{createdAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insertMessage(ChatMessage message);

    @Select("""
            select id, user_id, session_id, role, content, image, created_at
            from chat_message
            where user_id = #{userId} and session_id = #{sessionId}
            order by created_at asc
            """)
    List<ChatMessage> listMessages(@Param("userId") String userId,
                                   @Param("sessionId") String sessionId);

    @Select("""
            select id, user_id, session_id, role, content, image, created_at
            from chat_message
            where id = #{id}
            """)
    ChatMessage findMessage(@Param("id") Long id);

    @Delete("""
            delete from chat_message
            where id = #{id}
            """)
    void deleteMessage(@Param("id") Long id);

    @Delete("""
            delete from chat_message
            where id >= #{fromId} and user_id = #{userId} and session_id = #{sessionId}
            """)
    void deleteMessagesFrom(@Param("userId") String userId,
                            @Param("sessionId") String sessionId,
                            @Param("fromId") Long fromId);

    @Delete("""
            delete from chat_message
            where user_id = #{userId} and session_id = #{sessionId}
            """)
    void deleteMessagesBySession(@Param("userId") String userId,
                                 @Param("sessionId") String sessionId);
}
