package com.example.end.mapper;

import com.example.end.pojo.TaskLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TaskLogMapper {

    @Insert("""
            insert into task_log (operator_id, task_id, progress_percent, content, created_time, updated_time)
            values (#{operatorId}, #{taskId}, #{progressPercent}, #{content}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TaskLog taskLog);

    @Delete("delete from task_log where id = #{id}")
    int deleteById(Integer id);

    @Update("""
            update task_log
            set operator_id = #{operatorId},
                task_id = #{taskId},
                progress_percent = #{progressPercent},
                content = #{content},
                updated_time = now()
            where id = #{id}
            """)
    int updateById(TaskLog taskLog);

    @Select("""
            select id, operator_id, task_id, progress_percent, content, created_time, updated_time
            from task_log
            where id = #{id}
            """)
    TaskLog selectById(Integer id);

    @Select("""
            select id, operator_id, task_id, progress_percent, content, created_time, updated_time
            from task_log
            order by id desc
            """)
    List<TaskLog> selectAll();
}
