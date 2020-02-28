package com.msz.controller;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.msz.common.RespEntity;
import com.msz.model.MszAccount;
import com.msz.model.MszMsm;
import com.msz.model.SysUser;
import com.msz.service.MszAccountService;
import com.msz.service.MszMsmService;
import com.msz.service.SysUserService;
import com.msz.util.MD5Util;
import com.msz.util.MoblieMessageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


/**
 * @Author Maoyy
 * @Description 生活不止眼前的苟且
 * @Date 2019/7/16 9:54
 */

@Api(value = "/retrievePassword", description = "小程序找回密码; Responseble:Maoyy")
@RestController
@RequestMapping("/retrievePassword")
public class RetrievePasswordController {

    @Autowired
    private MszMsmService mszMsmService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone", value = "手机号", required = true, paramType = "query"),
            @ApiImplicitParam(name = "type", value = "0.account表1.user表", required = true, paramType = "query"),
    })
    @PostMapping("sendVerify")
    @ApiOperation(value = "发送验证码-------小程序(房东)@Author=Maoyy", notes = "发送验证码-------小程序(房东)@Author=Maoyy")
    public RespEntity<MszMsm> sendVerify(String phone, String type) throws ClientException {
        MszMsm msm = new MszMsm();
        int code = new Random().nextInt(899999) + 100000;
        msm.setCode(new Long(code));
        msm.setMsg("您的验证码是:" + code + "(切勿将验证码告知他人)请在页面中输入完成验证)");
        msm.setIsUser(type);
        msm.setUsername(phone);
        msm.setCreateTime(new Date());
        Map<String, Object> map = new HashMap<>();
        map.put("code", code);
        SendSmsResponse sendSms = MoblieMessageUtil.sendSms(phone, map, "SMS_171354925");
        if (sendSms.getCode().equals("OK")) {
            if (mszMsmService.insert(msm)) {
                //这里发送验证码
                return RespEntity.ok().setResponseContent(msm);
            } else {
                //验证码添加数据库失败
                return RespEntity.unauthorized("发送验证码失败");
            }
        } else {
            return RespEntity.unauthorized("发送验证码失败");
        }
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone", value = "手机号", required = true, paramType = "query"),
            @ApiImplicitParam(name = "type", value = "0.account表1.user表", required = true, paramType = "query"),
            @ApiImplicitParam(name = "code", value = "验证码", required = true, paramType = "query"),
    })
    @PostMapping("verify")
    @ApiOperation(value = "验证-------小程序(房东)@Author=Maoyy", notes = "验证-------小程序(房东)@Author=Maoyy")
    public RespEntity<MszMsm> verify(String phone, String type, String code) {
        MszMsm msm = mszMsmService.selectOne(new EntityWrapper<MszMsm>().eq("username", phone).eq("isUser", type).eq("code", code));
        Long date = new Long(10 * 60 * 1000); //过期时间
        if (msm != null) {
            Long createTime = new Date().getTime() - msm.getCreateTime().getTime(); //距离发送验证码过去的时间
            if (msm != null && date >= createTime) {
                return RespEntity.ok().setResponseContent(true);
            }
        }
        return RespEntity.ok().setResponseContent(false);
    }

    @Autowired
    private MszAccountService mszAccountService;

    @Autowired
    private SysUserService sysUserService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "phone", value = "手机号", required = true, paramType = "query"),
            @ApiImplicitParam(name = "pwd", value = "密码", required = true, paramType = "query"),
            @ApiImplicitParam(name = "type", value = "0.account表1.user表", required = true, paramType = "query"),
    })
    @PostMapping("setPassword")
    @ApiOperation(value = "重置密码-------小程序(房东,业务员)@Author=Maoyy", notes = "重置密码-------小程序(房东,业务员)@Author=Maoyy")
    public RespEntity<MszMsm> setPassword(String phone, String pwd, String type) throws Exception {
        if (type.equals("0")) {
            MszAccount acc = mszAccountService.selectOne(new EntityWrapper<MszAccount>().eq("tel", phone));
            if (acc != null) {
                acc.setPwd(MD5Util.md5(acc.getTel(), pwd, new SimpleDateFormat("yyyyMMddHHmmss").format(acc.getCreateTime())));
                if (mszAccountService.updateById(acc)) {
                    return RespEntity.ok().setResponseContent(true);
                }
            }
            return RespEntity.ok().setResponseContent(false);
        } else if (type.equals("1")) {
            SysUser user = sysUserService.selectOne(new EntityWrapper<SysUser>().eq("username", phone));
            if (user != null) {
                user.setPassword(MD5Util.md5(user.getUsername(), pwd, new SimpleDateFormat("yyyyMMddHHmmss").format(user.getCreateTime())));
                if (sysUserService.updateById(user)) {
                    return RespEntity.ok().setResponseContent(true);
                }
            }
            return RespEntity.ok().setResponseContent(false);
        }
        return RespEntity.ok().setResponseContent(false);

    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, paramType = "query"),
            @ApiImplicitParam(name = "pwd", value = "密码", required = true, paramType = "query"),
            @ApiImplicitParam(name = "type", value = "0.account表1.user表", required = true, paramType = "query"),
    })
    @PostMapping("setPasswordPC")
    @ApiOperation(value = "修改密码-------PC@Author=Maoyy", notes = "修改密码-------PC@Author=Maoyy")
    public RespEntity<MszMsm> setPasswordPC(Integer id, String pwd, String type) throws Exception {
        if (type.equals("0")) {
            MszAccount acc = mszAccountService.selectOne(new EntityWrapper<MszAccount>().eq("id", id));
            if (acc != null) {
                acc.setPwd(MD5Util.md5(acc.getTel(), pwd, new SimpleDateFormat("yyyyMMddHHmmss").format(acc.getCreateTime())));
                if (mszAccountService.updateById(acc)) {
                    return RespEntity.ok().setResponseContent(true);
                }
            }
            return RespEntity.ok().setResponseContent(false);
        } else if (type.equals("1")) {
            SysUser user = sysUserService.selectOne(new EntityWrapper<SysUser>().eq("id", id));
            if (user != null) {
                user.setPassword(MD5Util.md5(user.getUsername(), pwd, new SimpleDateFormat("yyyyMMddHHmmss").format(user.getCreateTime())));
                if (sysUserService.updateById(user)) {
                    return RespEntity.ok().setResponseContent(true);
                }
            }
            return RespEntity.ok().setResponseContent(false);
        }
        return RespEntity.ok().setResponseContent(false);

    }
}
