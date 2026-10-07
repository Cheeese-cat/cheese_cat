package com.xiaoyan.aiassistant.memory;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
@SuppressWarnings("SqlResolve")
// 长期记忆的 MyBatis 数据访问接口。
public interface LongMemoryMapper {

    // 插入用户主动录入的长期记忆，user_id 用于做到用户级隔离。
    @Insert("""
            insert into long_memory(vector_id, user_id, title, content, tags, created_at, updated_at)
            values(#{vectorId}, #{userId}, #{title}, #{content}, #{tags}, #{createdAt}, #{updatedAt})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(LongMemory memory);

    // 查询指定用户的长期记忆，避免不同用户之间互相召回。
    @Select("""
            select id, vector_id, user_id, title, content, tags, created_at, updated_at
            from long_memory
            where user_id = #{userId}
            order by created_at desc
            """)
    List<LongMemory> findByUserId(@Param("userId") String userId);

    // 根据 id 查询单条长期记忆。
    @Select("""
            select id, vector_id, user_id, title, content, tags, created_at, updated_at
            from long_memory
            where id = #{id}
            """)
    LongMemory findById(@Param("id") Long id);

    // 更新长期记忆的标题、内容、标签和更新时间。
    @Update("""
            update long_memory
            set title = #{title},
                content = #{content},
                tags = #{tags},
                updated_at = #{updatedAt}
            where id = #{id}
            """)
    void update(LongMemory memory);

    // 根据 id 删除长期记忆。
    @Delete("""
            delete from long_memory
            where id = #{id}
            """)
    void deleteById(@Param("id") Long id);
}
