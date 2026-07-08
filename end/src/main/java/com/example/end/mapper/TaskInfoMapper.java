package com.example.end.mapper;

import com.example.end.pojo.TaskInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TaskInfoMapper {

    @Insert("""
            insert into task_info (creator_id, assignee_id, project_id, parent_id, title, description, priority, status, due_date, ai_suggestion, created_time, updated_time)
            values (#{creatorId}, #{assigneeId}, #{projectId}, #{parentId}, #{title}, #{description}, #{priority}, #{status}, #{dueDate}, #{aiSuggestion}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TaskInfo taskInfo);

    @Delete("delete from task_info where id = #{id}")
    int deleteById(Long id);

    @Update("""
            update task_info
            set creator_id = #{creatorId},
                assignee_id = #{assigneeId},
                project_id = #{projectId},
                parent_id = #{parentId},
                title = #{title},
                description = #{description},
                priority = #{priority},
                status = #{status},
                due_date = #{dueDate},
                ai_suggestion = #{aiSuggestion},
                updated_time = now()
            where id = #{id}
            """)
    int updateById(TaskInfo taskInfo);

    @Select("""
            select id, creator_id, assignee_id, project_id, parent_id, title, description, priority, status, due_date, ai_suggestion, created_time, updated_time
            from task_info
            where id = #{id}
            """)
    TaskInfo selectById(Long id);

    @Select("""
            select id, creator_id, assignee_id, project_id, parent_id, title, description, priority, status, due_date, ai_suggestion, created_time, updated_time
            from task_info
            order by id desc
            """)
    List<TaskInfo> selectAll();
}
