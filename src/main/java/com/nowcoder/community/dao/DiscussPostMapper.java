package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {

    // 返回的是一个列表，设计的时候考虑分页
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit);

    // 查询总数，@Param 给参数起别名
    int selectDiscussPostRows(@Param("userId") int userId);
}
