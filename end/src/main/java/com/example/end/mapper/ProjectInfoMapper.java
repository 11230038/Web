package com.example.end.mapper;

import com.example.end.pojo.ProjectInfo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ProjectInfoMapper {

    @Insert("""
            insert into project_info (owner_id, name, description, priority, status, start_date, end_date, created_time, updated_time)
            values (#{ownerId}, #{name}, #{description}, #{priority}, #{status}, #{startDate}, #{endDate}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProjectInfo projectInfo);

    @Delete("delete from project_info where id = #{id}")
    int deleteById(Long id);

    @Update("""
            update project_info
            set owner_id = #{ownerId},
                name = #{name},
                description = #{description},
                priority = #{priority},
                status = #{status},
                start_date = #{startDate},
                end_date = #{endDate},
                updated_time = now()
            where id = #{id}
            """)
    int updateById(ProjectInfo projectInfo);

    @Select("""
            select id, owner_id, name, description, priority, status, start_date, end_date, created_time, updated_time
            from project_info
            where id = #{id}
            """)
    ProjectInfo selectById(Long id);

    @Select("""
            select id, owner_id, name, description, priority, status, start_date, end_date, created_time, updated_time
            from project_info
            order by id desc
            """)
    List<ProjectInfo> selectAll();

    @Select("""
            select id, owner_id, name, description, priority, status, start_date, end_date, created_time, updated_time
            from project_info
            where owner_id = #{ownerId}
            order by id desc
            """)
    List<ProjectInfo> selectAllByOwnerId(Long ownerId);
}
