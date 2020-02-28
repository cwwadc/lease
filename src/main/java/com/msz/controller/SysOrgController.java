package com.msz.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.github.pagehelper.PageInfo;
import com.msz.annotation.CurrentUser;
import com.msz.annotation.LoginRequired;
import com.msz.common.PagingRequest;
import com.msz.common.RespEntity;
import com.msz.common.UserCommon;
import com.msz.model.MszLease;
import com.msz.model.SysCity;
import com.msz.model.SysOrg;
import com.msz.model.SysUser;
import com.msz.service.SysOrgService;
import com.msz.VO.SysOrgVo;
import com.msz.service.SysUserService;
import com.msz.util.CodeUtils;
import com.msz.util.DateUtil;
import com.msz.util.FirstCharUtil;
import com.msz.util.RandomUtil;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import java.util.Date;
import java.util.List;


/**
 * <p>
 * 组织机构表 前端控制器
 * </p>
 *
 * @author cww
 * @since 2019-06-03 ${time}
 */
@Api(value = "/sys-orgs", description = "网点管理 接口; Responseble:cww")
@RestController
@RequestMapping("/sys-orgs")
public class SysOrgController {

    /**
     * id  Integer
     * <p>
     * 网点编号  code  String
     * <p>
     * 网点名称  name  String
     * <p>
     * 上级网点ID  parentId  Integer
     * <p>
     * 负责人  stafId  Integer
     * <p>
     * 是否删除  isDel  String
     * <p>
     * 创建时间  createTime  Date
     * <p>
     * 更新时间  updateTime  Date
     */
    @Autowired
    private SysOrgService sysOrgService;
    @Autowired
    SysUserService userService;
    @Autowired
    UserCommon userCommon;


    @GetMapping("{id}")
    @ApiOperation(value = "根据id获取单个SysOrg", notes = "根据id获取单个SysOrg")
    public RespEntity<SysOrg> get(@PathVariable Integer id) {
        return RespEntity.ok().setResponseContent(sysOrgService.selectById(id));
    }

    @GetMapping("/getCollectors")
    @ApiOperation(value = "根据网点id获取采集人", notes = "根据网点id获取采集人")
    @LoginRequired
    public RespEntity<SysOrg> getCollectors(@CurrentUser SysUser user, Integer orgId, String trueName) {
        return RespEntity.ok().setResponseContent(sysOrgService.getCollectors(user, orgId, trueName));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "offset", value = "起始页", required = true, paramType = "query"),
            @ApiImplicitParam(name = "limit", value = "记录数", required = true, paramType = "query")
    })
    @GetMapping("/getCollectorsPage")
    @ApiOperation(value = "根据网点id获取采集人分页", notes = "根据网点id获取采集人分页")
    public RespEntity<SysOrg> getCollectorsPage(PagingRequest pagingRequest, Integer orgId, String trueName) {
        return RespEntity.ok().setResponseContent(sysOrgService.getCollectorsPage(pagingRequest, orgId, trueName));
    }


    @ApiOperation(value = "管理人设置网点列表", notes = "管理人设置网点列表")
    @GetMapping("/adminOrgList")
    public RespEntity<PageInfo<SysOrg>> adminOrgList(String searchField) {

        List<SysOrg> orgList = sysOrgService.selectList(new EntityWrapper<SysOrg>().isNull("stafId").like("name", searchField));
        return RespEntity.ok().setResponseContent(orgList);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "offset", value = "起始页", required = true, paramType = "query"),
            @ApiImplicitParam(name = "limit", value = "记录数", required = true, paramType = "query"),
    })
    @ApiOperation(value = "网点管理列表", notes = "网点管理列表")
    @GetMapping("/list")
    @LoginRequired
    public RespEntity<PageInfo<SysOrg>> list(PagingRequest pagingRequest, String searchField, @CurrentUser SysUser user) {
        PageInfo<SysOrgVo> orgList = sysOrgService.getOrgList(pagingRequest, searchField, user);
        return RespEntity.ok().setResponseContent(orgList);
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "网点名称")
    })
    @ApiOperation(value = "不分页查询所有的网点(按照名称模糊查询网点)-------PC(发布房源的网点)@Author=Maoyy", notes = "不分页查询所有的网点(按照名称模糊查询网点)-------PC(发布房源的网点)@Author=Maoyy")
    @GetMapping("/getAllOrg")
    public RespEntity<PageInfo<SysOrg>> getAllOrg(String name) {
        List<SysOrg> orgList = sysOrgService.selectList(new EntityWrapper<SysOrg>().like("name", name));
        if (orgList != null && orgList.size() > 0) {
            orgList.forEach(item -> {
                item.setStafName(item.getStafId() == null ? "" : userService.selectById(item.getStafId()).getTrueName());
            });
        }
        return RespEntity.ok().setResponseContent(orgList);
    }


    @PostMapping("/insert")
    @ApiOperation(value = "保存SysOrg", notes = "保存SysOrg")
    public RespEntity insert(@RequestBody SysOrg sysOrg) {
        String maxCode = CodeUtils.createCode("code", "sys_org");
        String code;
        if (maxCode != null) {
            int endNum = Integer.parseInt(maxCode); // 把String类型的0001转化为int类型的1
            int tmpNum = 10000 + endNum + 1; // 结果10002
            code = CodeUtils.subStr("" + tmpNum, 1);
        } else {
            int tmpNum = 10000 + 1; // 结果10001
            code = CodeUtils.subStr("" + tmpNum, 1);
        }
        sysOrg.setCreateTime(new Date());
        sysOrg.setCode(code);
        if (sysOrgService.insert(sysOrg)) {
            //绑定网点负责人id
            if (sysOrg.getStafId() != null) {
                SysUser sysUser = userService.selectById(sysOrg.getStafId());
                sysUser.setOrgId(sysOrg.getId());
                userService.updateById(sysUser);
            }
            return RespEntity.ok("保存成功");
        }

        return RespEntity.badRequest("保存失败");
    }


    @PutMapping("/update")
    @ApiOperation(value = "根据ID修改SysOrg", notes = "根据ID修改SysOrg")
    public RespEntity update(@RequestBody SysOrg sysOrg) {
        //修改绑定网点负责人id,将之前绑定人的orgId重置之后再绑定
        SysOrg sysOrg1 = sysOrgService.selectById(sysOrg.getId());
        SysUser sysUser1 = userService.selectById(sysOrg1.getStafId());
        if (sysUser1 != null) {
            sysOrgService.updateSetEmpty(sysUser1);
        }
        if (sysOrgService.updateById(sysOrg)) {
            if (sysOrg.getStafId() != null) {
                SysUser sysUser = userService.selectById(sysOrg.getStafId());
                sysUser.setOrgId(sysOrg.getId());
                userService.updateById(sysUser);
            }
            return RespEntity.ok("更新成功");
        }
        return RespEntity.badRequest("更新失败");
    }

    @PutMapping("/updateIsDel/{id}")
    @ApiOperation(value = "禁用", notes = "根据ID禁用SysOrg")
    public RespEntity updateIsDel(@PathVariable Integer id) {
        //前一个是改变的实体类 后一个值是where条件刷选
        SysOrg sysOrg = new SysOrg();
        sysOrg.setIsDel("1");
        if (!sysOrgService.update(sysOrg, new EntityWrapper<SysOrg>().eq("id", id))) {
            return RespEntity.badRequest("禁用失败");
        }
        return RespEntity.ok("禁用成功");
    }

    @PutMapping("/updateEnable/{id}")
    @ApiOperation(value = "网点启用", notes = "网点启用")
    public RespEntity updateEnable(@PathVariable Integer id) {

        //前一个是改变的实体类 后一个值是where条件刷选
        SysOrg sysOrg = new SysOrg();
        sysOrg.setIsDel("0");
        if (!sysOrgService.update(sysOrg, new EntityWrapper<SysOrg>().eq("id", id))) {
            return RespEntity.badRequest("网点启用失败");
        }
        return RespEntity.ok("网点启用成功");
    }


    @DeleteMapping("{id}")
    @ApiOperation(value = "根据ID删除SysOrg", notes = "根据ID删除SysOrg")
    public RespEntity delete(@PathVariable Integer id) {
        if (!sysOrgService.deleteById(id)) {
            return RespEntity.badRequest("删除失败");
        }
        return RespEntity.ok("删除成功");
    }


}