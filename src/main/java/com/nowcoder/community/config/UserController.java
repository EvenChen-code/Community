package com.nowcoder.community.config;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    /*
    * 上传并更新头像
    * MultipartFile,SpringMVC提供的接受传递文件的类
    * */
    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        // 判断是否上传的图片
        if(headerImage==null){
            model.addAttribute("error","您还没有选择图片");
            return "/site/setting";
        }

        // 获取后缀，判断文件格式是否正确
        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf(".")+1);
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件的格式不正确");
            return "/site/setting";
        }

        // 生成随机文件名
        filename = CommunityUtil.generateUUID()+suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + filename);
        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败" + e.getMessage());
            // 先抛出异常，后序统一处理Controller中异常
            throw new RuntimeException("上传文件失败，服务器发生异常" + e.getMessage());
        }

        // 更新当前用户的头像路径(web路径)
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header"+filename;
        userService.updataHeader(user.getId(),headerUrl);
        return "redirect:/index";
    }

    // 获取头像
    // 头像（静态资源）的动态加载
    @RequestMapping(path = "header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {
        // 服务器存放路径写·
        filename = uploadPath + "/" + filename;
        // 文件的后缀
        String surffix = filename.substring(filename.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + surffix);
        // 输出字节流（图片二进制）
        try (
                // java7中的方法，在这里创建输入输出流，会自动关闭
                FileInputStream fis = new FileInputStream(filename);    // 读取filename文件得到一个输入流
                OutputStream os = response.getOutputStream();
                ) {
            byte[] buffer = new byte[1024]; // 缓冲区
            int b = 0;  // 游标
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败" + e.getMessage());
        }
    }


}
