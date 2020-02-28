package com.msz.service.impl;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import com.msz.VO.IdleNumAndRentedNumAndObtainedNumVO;
import com.msz.VO.RoomReceive;
import com.msz.common.PagingRequest;
import com.msz.common.UserCommon;
import com.msz.dao.*;
import com.msz.model.MszRoom;
import com.msz.model.SysUser;
import com.msz.service.MszRoomService;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.baomidou.mybatisplus.mapper.Wrapper;

import java.util.List;


/**
 * <p>
 * 房源信息 服务实现类
 * </p>
 *
 * @author cww
 * @since 2019-06-03 ${time}
 */
@Service
public class MszRoomServiceImpl extends ServiceImpl<MszRoomMapper, MszRoom> implements MszRoomService {

    @Autowired
    MszAccountMapper accountMapper;
    @Autowired
    SysOrgMapper orgMapper;
    @Autowired
    SysUserMapper userMapper;
    @Autowired
    MszRoomMapper roomMapper;
    @Autowired
    SysCityMapper sysCityMapper;
    @Autowired
    UserCommon userCommon;

    @Override
    public PageInfo<MszRoom> listPage(PagingRequest pagingRequest,MszRoom room, SysUser user) {
        PageHelper.startPage(pagingRequest.getOffset(), pagingRequest.getLimit());
        String role = user.getRole();
        List<MszRoom> list;
        if (role.equals("管理员")) {
            list = roomMapper.findList(room);
            if (list != null && list.size() > 0) {
                list.forEach(item -> {
                    item.setPublisher(item.getPublisherId() == null ? "" : userMapper.selectById(item.getPublisherId()).getTrueName());
                });
            }
        } else {
            room.setOrgId(user.getOrgId());
            list = roomMapper.findList(room);
            if (list != null && list.size() > 0) {
                list.forEach(item -> {
                    item.setPublisher(item.getPublisherId() == null ? "" : userMapper.selectById(item.getPublisherId()).getTrueName());
                });
            }

        }
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<MszRoom> weProgramList(PagingRequest pagingRequest, RoomReceive roomReceive) {
        PageHelper.startPage(pagingRequest.getOffset(), pagingRequest.getLimit());
        EntityWrapper<MszRoom> where = new EntityWrapper<>();
        if (roomReceive.getText() != null && !roomReceive.getText().equals("")) {
            where.like("name", roomReceive.getText());
        }
        if (roomReceive.getStatus() != null && !roomReceive.getStatus().equals("")) {
            where.eq("status", roomReceive.getStatus());
        }
        if (roomReceive.getHighestPrice() != null && roomReceive.getLowsetPrice() != null) {
            where.ge("rentPrice", roomReceive.getLowsetPrice()).le("rentPrice", roomReceive.getHighestPrice());
        }
        if (roomReceive.getRoom() != null && !roomReceive.getRoom().equals("")) {
            where.eq("room", roomReceive.getRoom());
        }
        if (roomReceive.getOrgId() != null && !roomReceive.getOrgId().equals("")) {
            where.eq("orgId", roomReceive.getOrgId());
        }
        return new PageInfo<>(selectList(where));
    }

    @Override
    public PageInfo<MszRoom> landlordRoom(PagingRequest pagingRequest, RoomReceive roomReceive, Integer landlordId) {
        PageHelper.startPage(pagingRequest.getOffset(), pagingRequest.getLimit());
        EntityWrapper<MszRoom> where = new EntityWrapper<>();
//        if (roomReceive.getText() != null && !roomReceive.getText().equals("")) {
//            where.like("name", roomReceive.getText());
//        }
        if (roomReceive.getStatus() != null) {
            where.eq("status", roomReceive.getStatus());
        }
        if (roomReceive.getHighestPrice() != null && roomReceive.getLowsetPrice() != null) {
            where.ge("rentPrice", roomReceive.getLowsetPrice()).le("rentPrice", roomReceive.getHighestPrice());
        }
        if (roomReceive.getText() != null) {
            where.and().like("community", roomReceive.getText()).or().like("address", roomReceive.getText());
        }
        where.eq("telId", landlordId);
        return new PageInfo<>(selectList(where));
    }

    @Override
    public IdleNumAndRentedNumAndObtainedNumVO getIdleNumAndRentedNumAndObtainedNum(SysUser user) {
        String role = user.getRole();
        IdleNumAndRentedNumAndObtainedNumVO vo = new IdleNumAndRentedNumAndObtainedNumVO();
        if (role.equals("管理员")) {
            vo.setAllNum(selectCount(new EntityWrapper<>()));
            vo.setIdleNum(selectCount(new EntityWrapper<MszRoom>().eq("status", 0)));
            vo.setRentedNum(selectCount(new EntityWrapper<MszRoom>().eq("status", 1)));
            vo.setObtainedNum(selectCount(new EntityWrapper<MszRoom>().eq("status", 2)));
        } else {
            vo.setAllNum(selectCount(new EntityWrapper<MszRoom>().eq("orgId", user.getOrgId())));
            vo.setIdleNum(selectCount(new EntityWrapper<MszRoom>().eq("status", 0)
                    .eq("orgId", user.getOrgId())));
            vo.setRentedNum(selectCount(new EntityWrapper<MszRoom>().eq("status", 1)
                    .eq("orgId", user.getOrgId())));
            vo.setObtainedNum(selectCount(new EntityWrapper<MszRoom>().eq("status", 2)
                    .eq("orgId", user.getOrgId())));
        }
        return vo;
    }

    @Override
    public MszRoom getRoomDesc(Integer id) {

        MszRoom room = roomMapper.selectById(id);
        if (room != null) {
            room.setPublisher(room.getPublisherId() == null ? "" : userMapper.selectById(room.getPublisherId()).getTrueName());
            room.setTelName(room.getTelId() == null ? "" : accountMapper.selectById(room.getTelId()).getName());
            room.setOrgName(room.getOrgId() == null ? "" : orgMapper.selectById(room.getOrgId()).getName());
            room.setUserName(room.getUserId() == null ? "" : userMapper.selectById(room.getUserId()).getTrueName());
            room.setProvinceName(room.getProvinceId() == null ? "" : sysCityMapper.selectById(room.getProvinceId().toString()).getName()); //省份名称
            room.setCityName(room.getCityId() == null ? "" : sysCityMapper.selectById(room.getCityId().toString()).getName()); //城市名称
            room.setCountyName(room.getCountyId() == null ? "" : sysCityMapper.selectById(room.getCountyId().toString()).getName()); //区县名称
            room.setTownName(room.getTownId() == null ? "" : sysCityMapper.selectById(room.getTownId().toString()).getName()); //街道名称
        }

        return room;
    }

}
