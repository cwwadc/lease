package com.msz.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.msz.annotation.CurrentUser;
import com.msz.annotation.LoginRequired;
import com.msz.common.RespEntity;
import com.msz.common.UserCommon;
import com.msz.model.MszToken;
import com.msz.model.SysLog;
import com.msz.model.SysUser;
import com.msz.service.MszTokenService;
import com.msz.service.SysLogService;
import com.msz.service.SysUserService;
import com.msz.util.CookieUtil;
import com.msz.util.IpUtils;
import com.msz.util.MD5Util;
import com.msz.util.RedisUtil;
import com.msz.util.aop.MyLog;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Api(value = "/admin", description = " 登陆; Responseble:cww")
@RestController
@RequestMapping("/admin")
public class LoginController {

    @Autowired
    private SysUserService userService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private MszTokenService tokenService;
    @Autowired
    SysLogService logService;


    @ApiOperation(value = "登录", notes = "登录")
    @PostMapping("/login")
    public RespEntity login(HttpServletRequest request,String username, String password) throws Exception {
        RespEntity respEntity = new RespEntity();
        SysLog sysLog = new SysLog();
        sysLog.setCreateTime(new Date());
        sysLog.setType(1);
        sysLog.setIp(IpUtils.getIpAddr(request));
        SysUser sysUser = userService.selectOne(new EntityWrapper<SysUser>().eq("type", "1").eq("username", username).eq("state", "0"));
        if (sysUser != null) {
            String md5Password = MD5Util.md5(username, password, new SimpleDateFormat("yyyyMMddHHmmss").format(sysUser.getCreateTime()));
            if (MD5Util.verify(sysUser.getPassword(), md5Password) == true) {
                SysUser user = userService.login(username, md5Password);
                Integer userId = user.getId();
                if ((user.getOrgId() == null || user.getOrgId() == 0) && user.getRole().equals("网点负责人") ){
                    respEntity.setStatusCode("0");
                    respEntity.setStatusMessage("你还没有负责的网点，暂时不能登录！");
                    sysLog.setUserId(userId);
                    sysLog.setInfo(username + "登录失败，还没有负责的网点，暂时不能登录");
                    return respEntity;
                }
                respEntity.setStatusCode("10000");
                respEntity.setStatusMessage("登录成功");
                sysLog.setUserId(userId);
                sysLog.setInfo(username + "成功登录马上租后台系统");
                Map<String, Object> map = new HashMap<>();
                redisUtil.set("user", user);
                MszToken token = tokenService.findByUserId(user.getId());
                String TokenStr = "";
                Date date = new Date();
                Long nowtime = (date.getTime() / 1000);
                //生成Token
                TokenStr = creatToken(user, date);
                if (null == token) {
                    //第一次登陆
                    token = new MszToken();
                    token.setToken(TokenStr);
                    token.setBulidTime(nowtime);
                    token.setUserId(user.getId());
                    tokenService.insert(token);
                } else {
                    //登陆就更新Token信息
                    TokenStr = creatToken(user, date);
                    token.setToken(TokenStr);
                    token.setBulidTime(nowtime);
                    tokenService.updateById(token);
                }
                map.put("token", TokenStr);
                map.put("user", user);
                respEntity.setResponseContent(map);
                logService.insert(sysLog);//登录操作日志
                return respEntity;
            }
        }
        respEntity.setStatusCode("0");
        respEntity.setStatusMessage("用户名或密码错误");
        return respEntity;
    }


    @ApiOperation(value = "测试获取用户信息", notes = "登测试获取用户信息录")
    @GetMapping("/test")
    @LoginRequired
    public RespEntity login(@CurrentUser SysUser user){
        SysUser currentUser = UserCommon.getCurrentUser();
        return RespEntity.ok().setResponseContent(currentUser);
    }

    //生成Token信息方法（根据有效的用户信息）
    private String creatToken(SysUser user, Date date) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        JwtBuilder builder = Jwts.builder().setHeaderParam("typ", "JWT") // 设置header
                .setHeaderParam("alg", "HS256").setIssuedAt(date) // 设置签发时间
                /*.setExpiration(new Date(date.getTime() + 1000 * 60 * 60 * 24 * 3))*/
                .setExpiration(new Date(date.getTime() + 1000 * 60 * 30))
                .claim("userid",String.valueOf(user.getId()) ) // 设置内容
                .setIssuer("cww")// 设置签发人
                .signWith(signatureAlgorithm, "dahao"); // 签名，需要算法和key
        String jwt = builder.compact();
        return jwt;
    }

}
