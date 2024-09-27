package com.minigit.entityService.serviceImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minigit.entity.User;
import com.minigit.mapper.UserMapper;
import com.minigit.entityService.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
