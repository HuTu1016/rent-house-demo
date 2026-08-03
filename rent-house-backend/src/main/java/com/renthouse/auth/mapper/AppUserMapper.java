package com.renthouse.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.renthouse.auth.entity.AppUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Optional;

@Mapper
public interface AppUserMapper extends BaseMapper<AppUser> {
    @Select("SELECT * FROM sys_user WHERE mobile = #{mobile} AND deleted_at IS NULL")
    Optional<AppUser> findByMobileAndDeletedAtIsNull(String mobile);
}
