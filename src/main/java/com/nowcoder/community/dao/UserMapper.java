package com.nowcoder.community.dao;

import com.nowcoder.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

/*
* 实现mapper，需要提供一个配置文件，配置文件中需要给出每一个方法的sql，mybatis底层会自动帮助生成一个实现类
* */

@Mapper
public interface UserMapper {

    User selectById(Integer id);

    User selectByName(String name);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(int id, int status);

    int updateHeader(int id, String headerUrl);

    int updatePassword(int id, String password);

}
