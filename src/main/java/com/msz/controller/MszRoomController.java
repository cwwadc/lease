package com.msz.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.github.pagehelper.PageInfo;
import com.msz.VO.IdleNumAndRentedNumAndObtainedNumVO;
import com.msz.annotation.CurrentUser;
import com.msz.annotation.LoginRequired;
import com.msz.model.*;
import com.msz.service.MszAccountService;
import com.msz.service.SysCityService;
import com.msz.util.*;
import com.msz.VO.RoomReceive;
import com.msz.VO.RoomVO;
import com.msz.common.PagingRequest;
import com.msz.common.RespEntity;
import com.msz.service.MszRoomImgService;
import com.msz.service.MszRoomService;
import com.msz.service.SysCityService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import java.util.Date;


/**
 * <p>
 * 房源信息 前端控制器
 * </p>
 *
 * @author cww
 * @since 2019-06-03 ${time}
 */
@Api(value = "/msz-rooms", description = "房源信息 接口; Responseble:cww")
@RestController
@RequestMapping("/msz-rooms")
public class MszRoomController {

    /**
     * 编号  id  Integer
     * <p>
     * 房东 id   long
     * <p>
     * 房源编号  no  String
     * <p>
     * 房间名称  name  String
     * <p>
     * 省份id  provinceId  Integer
     * <p>
     * 城市id  cityId  Integer
     * <p>
     * 区县id  countyId  Integer
     * <p>
     * 街道  townId  Integer
     * <p>
     * 门牌号  houseNumber  String
     * <p>
     * 地址  address  String
     * <p>
     * 室  room  String
     * <p>
     * 厅  hall  String
     * <p>
     * 卫  toilet  String
     * <p>
     * 楼层  floor  String
     * <p>
     * 共几层  whichFloor  String
     * <p>
     * 房间面积  area  Double
     * <p>
     * 是否有电梯：1-是，2-否  elevator  boolean
     * <p>
     * 装修情况  decorate  Integer
     * <p>
     * 朝向  toward  Integer
     * <p>
     * 房间配置（1：全部，2：标配，3：电视，4：空调，5：热水器，6：沙发，7：床，8：暖气，9：衣柜，10：可做饭，11：独立卫生间，12：独立阳台，13：冰箱，14：洗衣机）  configuration  String
     * <p>
     * 付款方式  payWay  Integer
     * <p>
     * 付款类型  payType  Integer
     * <p>
     * 租金价格  rentPrice  BigDecimal
     * <p>
     * 押金  depositPrice  BigDecimal
     * <p>
     * 底价  FloorPrice  BigDecimal
     * <p>
     * 差价  diffPrice  BigDecimal
     * <p>
     * 标题  title  String
     * <p>
     * 房间描述  description  String
     * <p>
     * 所属网点ID  orgId  Integer
     * <p>
     * 采集人ID  userId  Integer
     * <p>
     * 房源状态 0闲置 1已出租 2下架  status  String
     * <p>
     * 是否删除  isDel  String
     * <p>
     * 更新时间  updateTime  Date
     * <p>
     * 生成时间  createTime  Date
     */
    @Autowired
    private MszRoomService mszRoomService;

    @Autowired
    private MszRoomImgService mszRoomImgService;
    @Autowired
    private MszAccountService mszAccountService;

    @Autowired
    private SysCityService sysCityService;

    @GetMapping("{id}")
    @ApiOperation(value = "查询单个房源信息-------小程序(房东)@Author=Maoyy", notes = "查询单个房源信息-------小程序(房东)@Author=Maoyy")
    public RespEntity<MszRoom> get(@PathVariable Integer id) {
        RoomVO vo = new RoomVO();
        BeanUtils.copyProperties(mszRoomService.selectById(id), vo);
        //图片
        vo.setMszRoomImg(mszRoomImgService.selectList(new EntityWrapper<MszRoomImg>().eq("roomId", vo.getId())));
        vo.setProvinceName(vo.getProvinceId() == null ? "" : sysCityService.selectById(vo.getProvinceId().toString()).getName()); //省份名称
        vo.setCityName(vo.getCityId() == null ? "" : sysCityService.selectById(vo.getCityId().toString()).getName()); //城市名称
        vo.setCountyName(vo.getCountyId() == null ? "" : sysCityService.selectById(vo.getCountyId().toString()).getName()); //区县名称
        vo.setTownName(vo.getTownId() == null ? "" : sysCityService.selectById(vo.getTownId().toString()).getName()); //街道名称
        vo.setTelInfo(mszAccountService.selectById(vo.getTelId()));
        return RespEntity.ok().setResponseContent(vo);
    }

    @GetMapping("/getRoomDesc/{id}")
    @ApiOperation(value = "查看房源信息", notes = "查看房源信息")
    public RespEntity<MszRoom> getRoomDesc(@PathVariable Integer id) {

        return RespEntity.ok().setResponseContent(mszRoomService.getRoomDesc(id));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "offset", value = "起始页", required = true, paramType = "query"),
            @ApiImplicitParam(name = "limit", value = "记录数", required = true, paramType = "query"),
            @ApiImplicitParam(name = "status", value = "0未租 1已出租 2下架")
    })
    @ApiOperation(value = "分页查询MszRoom", notes = "分页查询MszRoom")
    @GetMapping("/getMszRoomList")
    @LoginRequired
    public RespEntity<PageInfo<MszRoom>> list(@CurrentUser SysUser user, PagingRequest pagingRequest, String status, String address, Integer telId) {
        MszRoom room = new MszRoom();
        room.setStatus(status);
        room.setAddress(address);
        room.setTelId(telId);
        return RespEntity.ok().setResponseContent(mszRoomService.listPage(pagingRequest, room, user));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "offset", value = "起始页", required = true, paramType = "query"),
            @ApiImplicitParam(name = "limit", value = "记录数", required = true, paramType = "query"),
    })
    @ApiOperation(value = "查询闲置的房屋", notes = "查询闲置的房屋")
    @GetMapping("/getLockRoomList")
    @LoginRequired
    public RespEntity<PageInfo<MszRoom>> getLockRoomList(@CurrentUser SysUser user,PagingRequest pagingRequest, String address) {
        MszRoom room = new MszRoom();
        room.setStatus("0");
        room.setAddress(address);
        return RespEntity.ok().setResponseContent(mszRoomService.listPage(pagingRequest, room,user));
    }

    @ApiOperation(value = "查询闲置/已出租/下架/全部 数量-------PC(房源管理)@Author=Maoyy", notes = "查询闲置/已出租/下架/全部 数量-------PC(房源管理)@Author=Maoyy")
    @GetMapping("/getIdleNumAndRentedNumAndObtainedNum")
    @LoginRequired
    public RespEntity<PageInfo<IdleNumAndRentedNumAndObtainedNumVO>> getIdleNumAndRentedNumAndObtainedNum(@CurrentUser SysUser user) {
        return RespEntity.ok().setResponseContent(mszRoomService.getIdleNumAndRentedNumAndObtainedNum(user));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "offset", value = "起始页", required = true, paramType = "query"),
            @ApiImplicitParam(name = "limit", value = "记录数", required = true, paramType = "query")
    })
    @ApiOperation(value = "查询房源信息-------小程序(业务员)@Author=Maoyy", notes = "查询房源信息-------小程序(业务员)@Author=Maoyy")
    @GetMapping("weProgramList")
    public RespEntity<PageInfo<MszRoom>> weProgramList(PagingRequest pagingRequest, RoomReceive roomReceive) {
        return RespEntity.ok().setResponseContent(mszRoomService.weProgramList(pagingRequest, roomReceive));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "offset", value = "起始页", required = true, paramType = "query"),
            @ApiImplicitParam(name = "limit", value = "记录数", required = true, paramType = "query")
    })
    @ApiOperation(value = "查询房源信息-------小程序(房东)@Author=Maoyy", notes = "查询房源信息-------小程序(房东)@Author=Maoyy")
    @GetMapping("landlordRoom")
    public RespEntity<PageInfo<MszRoom>> landlordRoom(PagingRequest pagingRequest, RoomReceive roomReceive,@RequestParam(required = true) Integer landlordId) {
        return RespEntity.ok().setResponseContent(mszRoomService.landlordRoom(pagingRequest, roomReceive, landlordId));
    }

    @PostMapping("/insert")
    @ApiOperation(value = "保存MszRoom", notes = "保存MszRoom")
    public RespEntity insert(@RequestBody MszRoom mszRoom) {
        //1.城市首字母
        SysCity sysCity = sysCityService.selectOne(new EntityWrapper<SysCity>().eq("id", mszRoom.getCityId()));
        String pyIndexStr = FirstCharUtil.getPYIndexStr(sysCity.getName(), true);
        //2.年月日
        String dayStr = DateUtil.getDayStr(new Date(), "yyyyMMddHHmmss");
        //3.随机数
        String randomStr = RandomUtil.getRandomStr(6);
        String no = pyIndexStr + dayStr + randomStr;
        mszRoom.setNo(no);
        mszRoom.setCreateTime(new Date());
        if (!mszRoomService.insert(mszRoom)) {
            return RespEntity.badRequest("保存失败");
        }
        return RespEntity.ok("保存成功");
    }


    @PutMapping("/update")
    @ApiOperation(value = "根据ID修改MszRoom", notes = "根据ID修改MszRoom")
    public RespEntity update(@RequestBody MszRoom mszRoom) {

        if (!mszRoomService.updateById(mszRoom)) {
            return RespEntity.badRequest("更新失败");
        }
        return RespEntity.ok("更新成功");
    }

    @PutMapping("/updateStatus")
    @ApiOperation(value = "下架", notes = "下架")
    public RespEntity updateStatus(@RequestParam Integer id) {
        MszRoom mszRoom = new MszRoom();
        mszRoom.setStatus("2");
        if (!mszRoomService.update(mszRoom, new EntityWrapper<MszRoom>().eq("id", id))) {
            return RespEntity.badRequest("下架失败");
        }
        return RespEntity.ok("下架成功");
    }

    @PutMapping("/updateShelf")
    @ApiOperation(value = "上架", notes = "上架")
    public RespEntity updateShelf(@RequestParam Integer id) {
        MszRoom mszRoom = new MszRoom();
        mszRoom.setStatus("0");
        if (!mszRoomService.update(mszRoom, new EntityWrapper<MszRoom>().eq("id", id))) {
            return RespEntity.badRequest("上架失败");
        }
        return RespEntity.ok("上架成功");
    }


    @DeleteMapping("{id}")
    @ApiOperation(value = "根据ID删除MszRoom", notes = "根据ID删除MszRoom")
    public RespEntity delete(@PathVariable Integer id) {
        if (!mszRoomService.deleteById(id)) {
            return RespEntity.badRequest("删除失败");
        }
        return RespEntity.ok("删除成功");
    }


}