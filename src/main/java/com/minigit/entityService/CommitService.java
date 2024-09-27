package com.minigit.entityService;

import com.baomidou.mybatisplus.extension.service.IService;
import com.minigit.common.R;
import com.minigit.entity.Commit;

import javax.servlet.http.HttpSession;
import java.util.List;


public interface CommitService extends IService<Commit> {

    List<Commit> getAllCommits(String userName, String repoName, String branchName);
}
