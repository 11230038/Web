package com.example.end.mapper;

import com.example.end.pojo.OperateLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OperateLogMapper {
    @Insert("""
            insert into operate_log
            (operate_emp_id, operate_time, class_name, method_name, method_params, return_value, cost_time, operate_emp_name)
            values
            (#{operateEmpId}, now(), #{className}, #{methodName}, #{methodParams}, #{returnValue}, #{costTime}, #{operateEmpName})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int add(OperateLog operateLog);

    @Select("""
            select count(*)
            from operate_log
            """)
    int count();

    @Select("""
            select id, operate_emp_id, operate_time, class_name, method_name, method_params, return_value, cost_time, operate_emp_name
            from operate_log
            order by id desc
            limit #{pageSize} offset #{offset}
            """)
    List<OperateLog> selectPage(@Param("offset") int offset, @Param("pageSize") int pageSize);
}
