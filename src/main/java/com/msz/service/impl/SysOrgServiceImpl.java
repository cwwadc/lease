package com.msz.service.impl;

import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.msz.VO.SysOrgVo;
import com.msz.common.PagingRequest;
import com.msz.common.UserCommon;
import com.msz.dao.SysOrgMapper;
import com.msz.model.SysOrg;
import com.msz.model.SysUser;
import com.msz.service.SysOrgService;
import com.msz.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.baomidou.mybatisplus.mapper.Wrapper;

import java.util.List;


/**
 * <p>
 * 组织机构表 服务实现类
 * </p>
 *
 * @author cww
 * @since 2019-06-03 ${time}
 */
@Service
public class SysOrgServiceImpl extends ServiceImpl<SysOrgMapper, SysOrg> implements SysOrgService {

    @Autowired
    SysOrgMapper sysOrgMapper;
    @Autowired
    UserCommon userCommon;

    @Override
    public PageInfo<SysOrg> listPage(PagingRequest pagingRequest, Wrapper wrapper){
        PageHelper.startPage( pagingRequest.getOffset(), pagingRequest.getLimit() );
        return new PageInfo<>(super.selectList(wrapper));
    }

    @Override
    public PageInfo<SysOrgVo> getOrgList(PagingRequest pagingRequest, String searchField, SysUser user) {
        PageHelper.startPage( pagingRequest.getOffset(), pagingRequest.getLimit() );
        SysOrgVo sysOrgVo = new SysOrgVo();
        String role = user.getRole();
        if (role.equals("管理员")){
            sysOrgVo.setSearchField(searchField);
        }else {
            sysOrgVo.setStafId(user.getId());
            sysOrgVo.setSearchField(searchField);
        }
        List<SysOrgVo> orgList = sysOrgMapper.getOrgList(sysOrgVo);
        return new PageInfo<>(orgList);
    }

    @Override
    public List<SysUser> getCollectors(SysUser user,Integer orgId, String trueName) {
        String role = user.getRole();
        List<SysUser> collectors;
        if (role.equals("管理员")){
            SysUser sysUser = new SysUser();
            sysUser.setOrgId(orgId);
            sysUser.setTrueName(trueName);
            collectors = sysOrgMapper.getCollectors(sysUser);
        }else {
            SysUser sysUser = new SysUser();
            sysUser.setOrgId(user.getOrgId());
            collectors = sysOrgMapper.getCollectors(sysUser);
        }
        return collectors;
    }

    @Override
    public PageInfo<SysUser> getCollectorsPage(PagingRequest pagingRequest, Integer orgId, String trueName) {

        PageHelper.startPage( pagingRequest.getOffset(), pagingRequest.getLimit() );
        SysUser user = new SysUser();
        user.setOrgId(orgId);
        user.setTrueName(trueName);
        List<SysUser> collectors = sysOrgMapper.getCollectors(user);
        return new PageInfo<>(collectors);
    }

    @Override
    public boolean updateSetEmpty(SysUser user) {
        boolean flag = sysOrgMapper.updateSetEmpty(user);
        return flag;
    }


}
