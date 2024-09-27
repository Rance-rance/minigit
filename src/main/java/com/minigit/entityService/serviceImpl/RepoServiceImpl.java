package com.minigit.entityService.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minigit.entity.Repo;
import com.minigit.mapper.RepoMapper;
import com.minigit.entityService.RepoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RepoServiceImpl extends ServiceImpl<RepoMapper, Repo> implements RepoService {
}
