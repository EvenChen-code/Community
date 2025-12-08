package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CookieUtil;
import com.nowcoder.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/*
* Interceptor 拦截器
* 定义拦截器，实现HandlerInterceptor接口
* 配置拦截器，为他指定拦截，排除的路径：在WebMvcConfig中配置
* 拦截器应用：
* --在请求开始时查询登陆用户
* --在本次请求中持有用户数据（HostHolder工具，内部使用ThreadHold<T>工具类实现
* --在模版视图上显示用户数据
* --在请求结束时清理用户数据
* */

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            // 从cookie中获取凭证
            String ticket = CookieUtil.getValue(request,"ticket");
            if(ticket != null){
                // 查询凭证
                LoginTicket loginTicket = userService.findLoginTicket(ticket);
                // 检查凭证是否有效
                if(loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                    // 根据凭证查询用户
                    User user = userService.findUserById(loginTicket.getUserId());
                    // 在本次请求中持有用户（考虑到多线程的情况）暂存到线程对应的对象中
                    hostHolder.setUser(user);
                }
            }
            return true;
    }

    // 在模版引擎前调用，将持有的user对象加入到model中
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user =  hostHolder.getUser();
        if(user != null && modelAndView != null){
            modelAndView.addObject("loginUser",user);
        }
    }

    // 模版引擎都执行完后，清楚掉持有的数据
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}



