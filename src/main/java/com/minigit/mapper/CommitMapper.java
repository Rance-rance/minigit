package com.minigit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.minigit.common.R;
import com.minigit.entity.Commit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommitMapper extends BaseMapper<Commit> {
    @Select("SELECT * FROM commit \n" +
            "WHERE branch_id = \n" +
            "   (SELECT id FROM branch \n" +
            "    WHERE name = #{branchName} AND repo_id = \n" +
            "        (SELECT id FROM repo \n" +
            "         WHERE name = #{repoName} AND author_id = \n" +
            "             (SELECT id FROM user WHERE account_name = #{userName}))) \n" +
            "                   ORDER BY create_time ASC;\n")
    List<Commit> getAllCommits(String userName, String repoName, String branchName);
}
