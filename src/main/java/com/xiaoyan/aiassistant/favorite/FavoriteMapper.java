package com.xiaoyan.aiassistant.favorite;

import org.apache.ibatis.annotations.*;

import java.util.List;

// 收藏的持久层。
@Mapper
public interface FavoriteMapper {

    @Select("""
        SELECT id, user_id AS userId, type, title, content, tags,
               source_type AS sourceType, created_at AS createdAt, updated_at AS updatedAt
        FROM favorite
        WHERE user_id = #{userId}
        ORDER BY created_at DESC
        """)
    List<Favorite> listByUser(@Param("userId") String userId);

    @Select("""
        SELECT id, user_id AS userId, type, title, content, tags,
               source_type AS sourceType, created_at AS createdAt, updated_at AS updatedAt
        FROM favorite
        WHERE user_id = #{userId} AND type = #{type}
        ORDER BY created_at DESC
        """)
    List<Favorite> listByUserAndType(@Param("userId") String userId, @Param("type") String type);

    @Select("""
        SELECT id, user_id AS userId, type, title, content, tags,
               source_type AS sourceType, created_at AS createdAt, updated_at AS updatedAt
        FROM favorite
        WHERE id = #{id}
        """)
    Favorite findById(@Param("id") Long id);

    @Insert("""
        INSERT INTO favorite (user_id, type, title, content, tags, source_type)
        VALUES (#{userId}, #{type}, #{title}, #{content}, #{tags}, #{sourceType})
        """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Favorite favorite);

    @Update("""
        UPDATE favorite
        SET title = #{title}, tags = #{tags}
        WHERE id = #{id} AND user_id = #{userId}
        """)
    int update(Favorite favorite);

    @Delete("DELETE FROM favorite WHERE id = #{id} AND user_id = #{userId}")
    int delete(@Param("id") Long id, @Param("userId") String userId);
}
