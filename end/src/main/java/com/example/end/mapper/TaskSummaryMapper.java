package com.example.end.mapper;

import com.example.end.pojo.TaskSummary;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TaskSummaryMapper {

    @Insert("""
            insert into task_summary (creator_id, project_id, task_id, summary_type, content, created_time, updated_time)
            values (#{creatorId}, #{projectId}, #{taskId}, #{summaryType}, #{content}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TaskSummary taskSummary);

    @Delete("delete from task_summary where id = #{id}")
    int deleteById(Long id);

    @Update("""
            update task_summary
            set creator_id = #{creatorId},
                project_id = #{projectId},
                task_id = #{taskId},
                summary_type = #{summaryType},
                content = #{content},
                updated_time = now()
            where id = #{id}
            """)
    int updateById(TaskSummary taskSummary);

    @Select("""
            select id, creator_id, project_id, task_id, summary_type, content, created_time, updated_time
            from task_summary
            where id = #{id}
            """)
    TaskSummary selectById(Long id);

    @Select("""
            select id, creator_id, project_id, task_id, summary_type, content, created_time, updated_time
            from task_summary
            order by id desc
            """)
    List<TaskSummary> selectAll();
}
