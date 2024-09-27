package com.minigit.entityService.serviceImpl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minigit.common.R;
import com.minigit.entity.Commit;
import com.minigit.mapper.CommitMapper;
import com.minigit.entityService.CommitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class CommitServiceImpl extends ServiceImpl<CommitMapper, Commit> implements CommitService {
    @Autowired
    CommitMapper commitMapper;
    @Override
    public List<Commit> getAllCommits(String userName, String repoName, String branchName) {
        return commitMapper.getAllCommits(userName,repoName, branchName);
    }
}
