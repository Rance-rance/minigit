package com.minigit.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.minigit.service.MailService;
import com.minigit.common.R;
import com.minigit.entity.User;
import com.minigit.entityService.UserService;
import com.minigit.util.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) throws MessagingException {
        //获取email
        String email = user.getEmail();

        if(StringUtils.isNotEmpty(email)){
            //生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code={}",code);

            mailService.sendMail(email, "minigit验证码", code);

            //需要将生成的验证码保存到Session
            session.setAttribute(email,code);

            return R.success("手机验证码短信发送成功");
        }
        return R.error("短信发送失败");
    }

    /**
     * 用户登录
     * @param map   map中保存着email和密码
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());

        // 获取accountName
        String accountName = map.get("accountName").toString();

        // 获取pwd
        String pwd = map.get("pwd").toString();

        if(accountName == null || pwd == null){
            return R.error("accountName或者pwd为null!");
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccountName, accountName).eq(User::getPwd, pwd);
        User user = userService.getOne(queryWrapper);
        if(user == null){
            return R.error("用户名或密码不正确！");
        }

        session.setAttribute("user",user.getId());

        return R.success(user);
    }


    /**
     * 用户注册
     * @param map   map中保存着email和code，以及accountName和pwd（这里密码应该需要再确认一次，由前端完成）
     * @param session
     * @return
     */
    @PostMapping("/register")
    public R<User> register(@RequestBody Map map, HttpSession session){
        log.info(map.toString());
        //获取email
        String email = map.get("email").toString();
        // 获取code
        String code = map.get("code").toString();
        //从Session中获取保存的验证码
        Object codeInSession = session.getAttribute(email);

        //进行验证码的比对（页面提交的验证码和Session中保存的验证码比对）
        if(codeInSession == null || !codeInSession.equals(code)){
            return R.error("验证码不正确！");
        }

        String accountName = map.get("accountName").toString();
        String pwd = map.get("pwd").toString();
        if(email == null || accountName == null || pwd == null){
            return R.error("email或者accountName或者pwd为null!");
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getEmail, email);

        User user = userService.getOne(queryWrapper);
        if(user != null){
            return R.error("email已经被注册！");
        }
        user = new User();
        user.setEmail(email);
        user.setAccountName(accountName);
        user.setPwd(pwd);
        userService.save(user);

        return R.success(user);
    }
    @GetMapping("/userinfo")
    public R<String> getAccountName(HttpServletRequest request) {
        Long userId= (Long) request.getSession().getAttribute("user");
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getId, userId);
        User user = userService.getOne(queryWrapper);

        return R.success(user.getAccountName());
    }
    @GetMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("user");
        return R.success("退出成功！");
    }

}
