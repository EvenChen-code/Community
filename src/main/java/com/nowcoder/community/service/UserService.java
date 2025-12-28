package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if(user == null){
            throw new IllegalArgumentException("user参数不能为空");
        }
        // StringUtils.isBlank 是 commons-lang3 中的方法
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空！");
            return map;
        }

        // 验证账号是否存在
        User u =  userMapper.selectByName(user.getUsername());
        if(u != null){
            map.put("usernameMsg","该帐号已经存在");
            return map;
        }

        // 验证邮箱是否存在
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null){
            map.put("emailMsg","该邮箱已经被注册");
            return map;
        }

        // 注册用户（将用户信息存入库中）
        // 密码要进行加密 原始密码+随机字符串salt
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);    // 普通用户
        user.setStatus(1);  // 初始未激活是0，但项目中直接写死为1，方便测试
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        // 插入库中
        userMapper.insertUser(user);

        // 发送激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        // url:http://locaolhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账户",content);


        return map;
    }

    public int activation(int userId, String code){
        // 查到这个id对应的user对象
        User user = userMapper.selectById(userId);

        if(user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }

    }

    // 登陆
    public Map<String,Object> login(String username, String password, int enpiredSeconds){
        Map<String,Object> map = new HashMap<>();

        // 空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }

        // 账号合法性验证
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg","该用户不存在");
            return map;
        }
        if(user.getStatus() == 0){  // 验证状态（是否激活）
            map.put("usernameMsg","该用户未激活");
            return map;
        }
        // 密码合法性验证
        // 加密后的密码
        password = CommunityUtil.md5(password+user.getSalt());
        if(!user.getPassword().equals(password)) {
            map.put("passwordMsg","密码错误");
            return map;
        }

        // 生成登陆凭证login_ticket，并存入库中
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + enpiredSeconds * 1000));   // 单位是毫秒
        loginTicketMapper.insertLoginTicket(loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    // 登出
    public void logout(String ticket) {
        loginTicketMapper.updateStatus(ticket,1);
    }

    // 查询登陆信息（通过ticket）
    public LoginTicket findLoginTicket(String ticket) {
        return loginTicketMapper.selectByTicket(ticket);
    }

    // 更新修改头像的路径
    public int updataHeader(int userId,String headerUrl) {
        return userMapper.updateHeader(userId,headerUrl);
    }
}
