package com.msz.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.github.pagehelper.PageInfo;
import com.msz.VO.*;
import com.msz.annotation.CurrentUser;
import com.msz.annotation.LoginRequired;
import com.msz.common.PagingRequest;
import com.msz.common.RespEntity;
import com.msz.common.UserCommon;
import com.msz.dao.MszAccountMapper;
import com.msz.dao.MszMessageMapper;
import com.msz.dao.MszOrderInfoMapper;
import com.msz.dao.MszRoomMapper;
import com.msz.model.*;
import com.msz.service.MszLeaseService;
import com.msz.service.MszOrderInfoService;
import com.msz.service.SysUserService;
import com.msz.util.MessageTeamplateUtil;
import com.msz.util.RedisUtil;
import com.msz.util.SendMessage;
import com.msz.util.StringUtil;
import org.apache.ibatis.annotations.Lang;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 租约 前端控制器
 * </p>
 *
 * @author cww
 * @since 2019-06-03 ${time}
 */
@Api(value = "/msz-leases", description = "租约 接口; Responseble:cww")
@RestController
@RequestMapping("/msz-leases")
public class MszLeaseController {

    /**
     * 房间ID  id  Integer
     * <p>
     * 租约编号  no  String
     * <p>
     * 房间ID  roomId  Integer
     * <p>
     * 房东ID  landlordId  Integer
     * <p>
     * 房客ID  tenantId  Integer
     * <p>
     * 业务员  userId  Integer
     * <p>
     * 开始时间  startTime  Date
     * <p>
     * 结束时间  endTime  Date
     * <p>
     * 应付款  duePrice  BigDecimal
     * <p>
     * 合同照片  contractPicture  String
     * <p>
     * 租约状态 0预约中 1执行中 2已结束 3异常  status  String
     * <p>
     * 创建人  operator  Integer
     * <p>
     * 是否删除  isDel  String
     * <p>
     * 更新时间  updateTime  Date
     * <p>
     * 生成时间  createTime  Date
     */
    @Autowired
    private MszLeaseService mszLeaseService;
    @Autowired
    UserCommon userCommon;
    @Autowired
    MszMessageMapper messageMapper;
    @Autowired
    MszAccountMapper accountMapper;
    @Autowired
    SysUserService userService;
    @Autowired
    MszRoomMapper mszRoomMapper;
    @Autowired
    MszOrderInfoService orderInfoService;
    @Autowired
    RedisUtil redisUtil;

    @GetMapping("{id}")
    @ApiOperation(value = "查看", notes = "查看")
    public RespEntity<MszLease> get(@PathVariable Integer id) {

        return RespEntity.ok().setResponseContent(mszLeaseService.getMszLeaseById(id));
    }

   /* @GetMapping("/testLease")
    @ApiOperation(value = "测试接口", notes = "测试接口")
    public RespEntity<MszLease> testLease(){
        List<MszLease> leaseList = mszLeaseService.selectList(new EntityWrapper<MszLease>()
                .eq("status", "1")
                .isNull("stopTime")
                .le(true, "endTime", new Date()));
        return RespEntity.ok().setResponseContent(leaseList);
    }*/


    @GetMapping("tenantIdAndUserId")
    @ApiOperation(value = "房客Id和业务员Id查询租约详情-------小程序(业务员)@Author=Maoyy", notes = "房客Id和业务员Id查询租约详情-------小程序(业务员)@Author=Maoyy")
    public RespEntity<MszLease> tenantIdAndUserId(Integer tenantId, Integer userId) {
        return RespEntity.ok().setResponseContent(mszLeaseService.tenantIdAndUserId(tenantId, userId));
    }

    @GetMapping("byTenantId/{id}")
    @ApiOperation(value = "通过租约房客方Id得到 房源 业务员 租约信息-------小程序(房客)@Author=Maoyy", notes = "通过租约房客方Id得到 房源 业务员 租约信息-------小程序(房客)@Author=Maoyy")
    public RespEntity<TenantIdAndUserIdVO> byTenantId(@PathVariable Integer id) {
        return RespEntity.ok().setResponseContent(mszLeaseService.byTenantId(id));
    }

    @GetMapping("byId/{id}")
    @ApiOperation(value = "租约Id得到 房源 业务员 租约信息-------小程序(房客)@Author=Maoyy", notes = "通过租约房客方Id得到 房源 业务员 租约信息-------小程序(房客)@Author=Maoyy")
    public RespEntity<TenantIdAndUserIdVO> byId(@PathVariable Integer id) {
        return RespEntity.ok().setResponseContent(mszLeaseService.byId(id));
    }

    @GetMapping("leaseByTenantId/{id}")
    @ApiOperation(value = "根据房客Id得到他所有的租约-------小程序(房客)@Author=Maoyy", notes = "根据房客Id得到他所有的租约-------小程序(房客)@Author=Maoyy")
    public RespEntity<WeProgramLeaseListVO> leaseByTenantId(@PathVariable Integer id) {
        return RespEntity.ok().setResponseContent(mszLeaseService.leaseByTenantId(id));
    }

    @PutMapping("updateLeaseStatus/{id}")
    @ApiOperation(value = "租约状态修改-------小程序(房客)@Author=Maoyy", notes = "租约状态修改 房源 业务员 租约信息-------小程序(房客)@Author=Maoyy")
    public RespEntity<TenantIdAndUserIdVO> updateLeaseStatus(@PathVariable Integer id) {
        MszLease lease = new MszLease();
        lease.setStatus("1");
        mszLeaseService.update(lease,new EntityWrapper<MszLease>().eq("id",id));//把租约状态改为已签约
        MszLease mszLease = mszLeaseService.selectById(id);
        //解除房源锁定
        MszRoom mszRoom = mszRoomMapper.selectById(mszLease.getRoomId());
        mszRoom.setIsLock("0");//解除锁定
        mszRoomMapper.updateById(mszRoom);
        //新建租约正常交费 发送给房客
        MszMessage message = new MszMessage();
        message.setCreateTime(new Date());
        message.setPromulgatorId(3);
        message.setType("1");//租约消息
        String leaseNo = mszLease.getNo();//租约编号
        String tenantName = accountMapper.selectById(mszLease.getTenantId()).getName();//房客名称
        MessageTeamplate messageTeamplate = MessageTeamplateUtil.orderInfoSuccessTenant(tenantName, leaseNo);
        message.setReceiverId(mszLease.getTenantId());//房客id
        message.setTitle(messageTeamplate.getTitle());
        message.setContentText(messageTeamplate.getContentText());
        messageMapper.insert(message);
        //新建租约正常交费 发送给房东
        String landlordName = accountMapper.selectById(mszLease.getLandlordId()).getName();//房东名称
        MessageTeamplate messageTeamplate1 = MessageTeamplateUtil.orderInfoSuccessByLandlord(landlordName, leaseNo);
        message.setReceiverId(mszLease.getLandlordId());//房东id
        message.setTitle(messageTeamplate1.getTitle());
        message.setContentText(messageTeamplate1.getContentText());
        messageMapper.insert(message);
        //新建租约正常交费 发送给业务员
        Integer userId = mszLease.getUserId();//业务员id
        String salesmanName = userService.selectById(userId).getTrueName();//业务员名称
        MessageTeamplate sendMessage = SendMessage.orderInfoSuccessSalesman(salesmanName, tenantName,  leaseNo);
        message.setReceiverId(userId);
        message.setTitle(sendMessage.getTitle());
        message.setContentText(sendMessage.getContentText());
        message.setIsUser("1");
        messageMapper.insert(message);
        return RespEntity.ok().setResponseContent(mszLeaseService.updateById(lease));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "offset", value = "起始页", required = true, paramType = "query"),
            @ApiImplicitParam(name = "limit", value = "记录数", required = true, paramType = "query"),
            @ApiImplicitParam(name = "status", value = "租约状态 0预约中 1执行中 2已结束 3异常")
    })
    @ApiOperation(value = "分页查询MszLease", notes = "分页查询MszLease")
    @GetMapping("/getMszLeaseList")
    @LoginRequired
    public RespEntity<PageInfo<LeaseReturnParamVO>> list(@CurrentUser SysUser user, PagingRequest pagingRequest, String status, String tenantName) {
        return RespEntity.ok().setResponseContent(mszLeaseService.listPage(pagingRequest, status, tenantName, user));
    }

    @ApiOperation(value = "获得租约状态为0预约中/1执行中/2" +
            "结束/3异常的数量", notes = "获得租约状态为0预约中/1执行中/2已结束/3异常的数量")
    @GetMapping("/getCountNum")
    @LoginRequired
    public RespEntity<PageInfo<LeaseNumVO>> getCountNum(@CurrentUser SysUser user) {

        return RespEntity.ok().setResponseContent(mszLeaseService.getCountNum(user));
    }

    @PostMapping("/insertMszLease")
    @ApiOperation(value = "新增租约", notes = "新增租约")
    @LoginRequired
    public RespEntity insertMszLease(@RequestBody LeaseParamVO leaseParamVO,@CurrentUser SysUser user) {
        if (leaseParamVO.getTenantId() == null){
            return RespEntity.badRequest("参数不能为空");
        }
        if (leaseParamVO.getUserId() == null || leaseParamVO.getLandlordId() == null){
            return RespEntity.badRequest("参数不能为空");
        }
        List<MszLease> mszLeaseList = mszLeaseService.signLeaseByTenantId(leaseParamVO.getTenantId());
        if (mszLeaseList.size() == 0) {
            if (!mszLeaseService.insertMszLease(leaseParamVO,user)) {
                return RespEntity.badRequest("新增租约成功失败");
            }
            return RespEntity.ok("新增租约成功");
        }
        return RespEntity.badRequest("您已经有租约了，不能再新增租约");
    }

    @PutMapping("/stopLease/{id}")
    @ApiOperation(value = "取消租约", notes = "取消租约")
    public RespEntity stopLease(@PathVariable Integer id) {
        MszLease lease = mszLeaseService.selectById(id);
        if (lease == null) {
            return RespEntity.badRequest("租约id不存在");
        }
        //释放房源
        MszRoom mszRoom = mszRoomMapper.selectById(lease.getRoomId());
        mszRoom.setStatus("0");
        mszRoomMapper.updateById(mszRoom);
        //删除缴费记录
        MszOrderInfo orderInfo = orderInfoService.selectOne(new EntityWrapper<MszOrderInfo>().eq("leaseId", lease.getId()).eq("roomId", lease.getRoomId()));
        if (orderInfo != null) {
            orderInfoService.deleteById(orderInfo.getId());
        }
        lease.setStatus("3");
        if (!mszLeaseService.updateById(lease)) {
            return RespEntity.badRequest("更新失败");
        }
        return RespEntity.ok("更新成功");
    }

    @PutMapping("/update")
    @ApiOperation(value = "编辑", notes = "编辑")
    public RespEntity update(@RequestBody MszLease mszLease) {
        if (!mszLeaseService.updateById(mszLease)) {
            return RespEntity.ok("编辑失败");
        }
        return RespEntity.ok("编辑成功");
    }


    @DeleteMapping("{id}")
    @ApiOperation(value = "根据ID删除MszLease", notes = "根据ID删除MszLease")
    public RespEntity delete(@PathVariable Integer id) {
        if (!mszLeaseService.deleteById(id)) {
            return RespEntity.badRequest("删除失败");
        }
        return RespEntity.ok("删除成功");
    }


}