package com.example.end.mapper;

import com.example.end.pojo.SysUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SysUserMapper {

    @Insert("""
            insert into sys_user (username, password, real_name, role, email, phone, created_time, updated_time)
            values (#{username}, #{password}, #{realName}, #{role}, #{email}, #{phone}, now(), now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysUser sysUser);

    @Delete("delete from sys_user where id = #{id}")
    int deleteById(Long id);

    @Update("""
            update sys_user
            set username = #{username},
                password = #{password},
                real_name = #{realName},
                role = #{role},
                email = #{email},
                phone = #{phone},
                updated_time = now()
            where id = #{id}
            """)
    int updateById(SysUser sysUser);

    @Select("select id, username, password, real_name, role, email, phone, created_time, updated_time from sys_user where id = #{id}")
    SysUser selectById(Long id);

    @Select("select id, username, password, real_name, role, email, phone, created_time, updated_time from sys_user order by id desc")
    List<SysUser> selectAll();
}
