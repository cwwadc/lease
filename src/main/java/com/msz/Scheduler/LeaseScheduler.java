package com.msz.Scheduler;

import com.aliyuncs.exceptions.ClientException;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.msz.VO.MessageTeamplate;
import com.msz.common.UserCommon;
import com.msz.dao.*;
import com.msz.model.*;
import com.msz.service.MszLeaseService;
import com.msz.service.MszOrderHistoryService;
import com.msz.service.SysUserService;
import com.msz.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.awt.dnd.DropTarget;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 房源定时器
 */
@Component
public class LeaseScheduler {

    private static Logger logger = LoggerFactory.getLogger(LeaseScheduler.class);

    MszRoomMapper mszRoomMapper;
    MszMessageMapper messageMapper;
    MszOrderInfoMapper orderInfoMapper;
    MszLeaseMapper leaseMapper;
    MszRefundInfoMapper refundInfoMapper;
    MszAccountMapper accountMapper;
    MszOrderHistoryMapper historyMapper;
    MszLeaseService leaseService;
    MszOrderHistoryService historyService;
    SysUserService userService;
    UserCommon userCommon;

    @Autowired
    public LeaseScheduler(MszRoomMapper mszRoomMapper, MszMessageMapper messageMapper, MszRefundInfoMapper refundInfoMapper,
                          MszOrderInfoMapper orderInfoMapper, MszLeaseMapper leaseMapper, MszAccountMapper accountMapper,
                          MszOrderHistoryMapper historyMapper, MszLeaseService leaseService, MszOrderHistoryService historyService,
                          SysUserService userService, UserCommon userCommon) {
        this.mszRoomMapper = mszRoomMapper;
        this.messageMapper = messageMapper;
        this.refundInfoMapper = refundInfoMapper;
        this.leaseMapper = leaseMapper;
        this.orderInfoMapper = orderInfoMapper;
        this.accountMapper = accountMapper;
        this.historyMapper = historyMapper;
        this.leaseService = leaseService;
        this.historyService = historyService;
        this.userService=userService;
        this.userCommon=userCommon;
    }

    /**
     * 48小时到期释放房源
     */
    /*@Scheduled(fixedRate = 60000)*/
    @Scheduled(cron = "${lease.update.room.crop}")
    public void updateRoom() {
        //释放房源
        List<MszRoom> mszRoomList = mszRoomMapper.selectList(new EntityWrapper<MszRoom>()
                .eq("status", "1")
                .eq("isLock", "1"));
        if (mszRoomList != null && mszRoomList.size() > 0) {
            mszRoomList.forEach(item -> {
                Date endTime = item.getEndTime();
                Date date = new Date();
                int i = date.compareTo(endTime);//compareTo()方法的返回值，date1小于date2返回-1，date1大于date2返回1，相等返回0
                if (i > 0) {
                    logger.info("锁定时间超过24小时未交费");
                    item.setStatus("0");
                    item.setIsLock("0");
                    mszRoomMapper.updateById(item);//释放房源（闲置）
                    MszLease lease = leaseService.selectOne(new EntityWrapper<MszLease>().eq("roomId", item.getId()).eq("status", "0"));
                    lease.setStatus("3");
                    leaseService.updateById(lease);
                    //新建租约逾期未交费 发送给房客
                    MszMessage message = new MszMessage();
                    message.setCreateTime(new Date());
                    message.setReceiverId(lease.getTenantId());//房客id
                    message.setPromulgatorId(UserCommon.getCurrentUser().getId());
                    message.setType("1");//租约消息
                    String leaseNo = lease.getNo();//租约编号
                    String tenantName = accountMapper.selectById(lease.getTenantId()).getName();//房客名称
                    MessageTeamplate messageTeamplate = MessageTeamplateUtil.overdueTenant(tenantName, leaseNo);
                    message.setTitle(messageTeamplate.getTitle());
                    message.setContentText(messageTeamplate.getContentText());
                    messageMapper.insert(message);
                    //新建租约逾期未交费 发送给房东
                    message.setReceiverId(lease.getLandlordId());//房东id
                    String landlordName = accountMapper.selectById(lease.getLandlordId()).getName();//房东名称
                    MessageTeamplate messageTeamplate1 = MessageTeamplateUtil.overdueLandlord(landlordName, leaseNo);
                    message.setTitle(messageTeamplate1.getTitle());
                    message.setContentText(messageTeamplate1.getContentText());
                    messageMapper.insert(message);
                    //新建租约逾期未交费 发送给业务员
                    Integer userId = lease.getUserId();//业务员id
                    String salesmanName = userService.selectById(userId).getTrueName();//业务员名称
                    MessageTeamplate sendMessage = SendMessage.overdueSalesman(salesmanName, tenantName, leaseNo);
                    message.setReceiverId(userId);
                    message.setTitle(sendMessage.getTitle());
                    message.setContentText(sendMessage.getContentText());
                    message.setIsUser("1");
                    messageMapper.insert(message);
                }
            });
        }
    }

    /**
     * 租金到期、交费提醒  (每月交租日前三天晚上8点钟进行下月交租费用提醒 并生成缴费记录)
     */
    @Scheduled(cron = "${lease.insert.message.crop}")
    public void senOrderInfoMessage() {
        List<MszLease> leaseList = leaseMapper.selectList(new EntityWrapper<MszLease>().eq("status", "1"));
        if (leaseList != null && leaseList.size() > 0) {
            leaseList.forEach(item -> {
                /*MszRoom mszRoom = mszRoomMapper.selectById(item.getRoomId());*/
                Date startTime = item.getStartTime();
                Integer payWay = item.getPayWay();
                BigDecimal rentPrice = item.getRentPrice();
                Date recentDate = null;
                BigDecimal total = null;
                if (payWay == 0) {
                    recentDate =new Date (DateUtil.getAfterMonth(startTime, 1).getTime()- 3 * 24 * 60 * 60 * 1000);
                    total = rentPrice.multiply(new BigDecimal(1));
                }
                if (payWay == 1) {
                    recentDate =new Date (DateUtil.getAfterMonth(startTime, 3).getTime()- 3 * 24 * 60 * 60 * 1000);
                    total = rentPrice.multiply(new BigDecimal(3));
                }
                if (payWay == 2) {
                    recentDate =new Date (DateUtil.getAfterMonth(startTime, 6).getTime()- 3 * 24 * 60 * 60 * 1000);
                    total = rentPrice.multiply(new BigDecimal(6));
                }
                if (payWay == 3) {
                    recentDate =new Date (DateUtil.getAfterMonth(startTime, 12).getTime()- 3 * 24 * 60 * 60 * 1000);
                    total = rentPrice.multiply(new BigDecimal(12));
                }
                SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
                if(myFormatter.format(recentDate).equals(myFormatter.format(new Date()))){
                    //1.租金到期、交费提醒 房客消息提醒
                    MszMessage message = new MszMessage();
                    message.setCreateTime(new Date());
                    message.setReceiverId(item.getTenantId());
                    message.setPromulgatorId(UserCommon.getCurrentUser().getId());
                    message.setType("1");//租约消息
                    MszAccount account = accountMapper.selectById(item.getTenantId());
                    String tenantName =account.getName();
                    MessageTeamplate messageTeamplate = MessageTeamplateUtil.rentExpiresLeaseTenant(total, recentDate);
                    message.setTitle(messageTeamplate.getTitle());
                    message.setContentText(messageTeamplate.getContentText());
                    messageMapper.insert(message);
                    //TODO
                    //2.租金到期、交费提醒 短信提醒
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(recentDate);
                    int year = calendar.get(Calendar.YEAR);//年
                    int month = calendar.get(Calendar.MONTH)+1;//月
                    int day = calendar.get(Calendar.DAY_OF_MONTH);//日
                    Map<String, Object> map = new HashMap<>();
                    map.put("year",year);
                    map.put("month1",month);
                    map.put("rentPrice",item.getRentPrice());
                    map.put("month2",month);
                    map.put("day",total);
                    try {
                        MoblieMessageUtil.sendSms( account.getTel(), map, "SMS_171540954");
                    } catch (ClientException e) {
                        e.printStackTrace();
                    }
                    //租金到期、交费提醒 发送给业务员
                    Integer userId = item.getUserId();//业务员id
                    String salesmanName = userService.selectById(userId).getTrueName();//业务员名称
                    MessageTeamplate sendMessage = SendMessage.rentExpiresLeaseSalesman(salesmanName, tenantName,total, recentDate);
                    message.setReceiverId(userId);
                    message.setTitle(sendMessage.getTitle());
                    message.setContentText(sendMessage.getContentText());
                    message.setIsUser("1");
                    messageMapper.insert(message);
                    //3.新增交费记录
                    String orderNo = UUIDUtils.getOrderIdByTime();//缴费编号
                    MszOrderInfo mszOrderInfo = new MszOrderInfo();
                    mszOrderInfo.setRoomId(item.getRoomId());
                    mszOrderInfo.setCreateTime(new Date());
                    mszOrderInfo.setLeaseId(item.getId());//获取插入后生成主键租约id
                    mszOrderInfo.setRentPrice(item.getRentPrice());//租金
                    mszOrderInfo.setDepositPrice(item.getDepositPrice());//押金
                    mszOrderInfo.setTotal(total);
                    mszOrderInfo.setNo(orderNo);//缴费编号
                    mszOrderInfo.setTenantId(item.getTenantId());//房客ID(电话号码)
                    mszOrderInfo.setLandlordId(item.getRoomId());//房东ID
                    mszOrderInfo.setEndTime(item.getEndTime());//结束时间
                    orderInfoMapper.insert(mszOrderInfo);
                }
            });
        }
    }

    /**
     * 中止租约
     */
    @Scheduled(cron = "${lease.update.data.crop}")
    public void stopMszLease() {
        List<MszLease> leaseList = leaseMapper.selectList(new EntityWrapper<MszLease>()
                .eq("status", "1")
                .le(true, "stopTime", new Date()));
        if (leaseList != null && leaseList.size() > 0) {
            leaseList.forEach(item -> {
                //1.提前终止合同 把租约状态改为异常
                item.setStatus("3");
                leaseMapper.updateById(item);
                //释放房源吧已出租改为闲置
                MszRoom mszRoom = mszRoomMapper.selectById(item.getRoomId());
                mszRoom.setStatus("0");
                mszRoomMapper.updateById(mszRoom);
                //2.并生成退款记录
                MszRefundInfo refundInfo = new MszRefundInfo();
                refundInfo.setCreateTime(new Date());
                refundInfo.setApplyTime(new Date());
                refundInfo.setDepositPrice(item.getDepositPrice());//租金
                refundInfo.setRentPrice(new BigDecimal(0));//租金默认为0
                refundInfo.setTotal(new BigDecimal(0).add(item.getDepositPrice()));
                refundInfo.setLeaseId(item.getId());
                String refundNo =UUIDUtils.getOrderIdByTime();
                refundInfo.setNo(refundNo);
                refundInfo.setLandlordId(item.getLandlordId());
                refundInfo.setTenantId(item.getTenantId());
                refundInfoMapper.insert(refundInfo);
                //3.中止租约 并向房客推送消息
                MszMessage message = new MszMessage();
                message.setType("3");//退款消息
                message.setCreateTime(new Date());
                message.setPromulgatorId(UserCommon.getCurrentUser().getId());
                String leaseNo = item.getNo();//租约编号
                BigDecimal total = item.getDepositPrice();//退款金额
                Integer tenantId = item.getTenantId();//房客id
                String tenantName = accountMapper.selectById(tenantId).getName();//房客名称
                MessageTeamplate messageTeamplate = MessageTeamplateUtil.terminationLeaseTenant(tenantName, leaseNo, total);
                message.setReceiverId(tenantId);
                message.setTitle(messageTeamplate.getTitle());
                message.setContentText(messageTeamplate.getContentText());
                messageMapper.insert(message);
                //4.中止租约 并向房东推送消息
                Integer landlordId = item.getLandlordId();//房东id
                String landlordName = accountMapper.selectById(landlordId).getName();//房东名称
                MessageTeamplate messageTeamplate1 = MessageTeamplateUtil.terminationLeaseLandlord(landlordName, leaseNo, total);
                message.setReceiverId(landlordId);
                message.setTitle(messageTeamplate1.getTitle());
                message.setContentText(messageTeamplate1.getContentText());
                messageMapper.insert(message);
                //5.中止租约 并向业务员推送消息
                Integer userId = item.getUserId();//业务员id
                String salesmanName = userService.selectById(userId).getTrueName();//业务员名称
                MessageTeamplate sendMessage = SendMessage.terminationLeaseSalesman(salesmanName, tenantName,leaseNo, total);
                message.setReceiverId(userId);
                message.setTitle(sendMessage.getTitle());
                message.setContentText(sendMessage.getContentText());
                message.setIsUser("1");
                messageMapper.insert(message);
            });
        }
    }

    /**
     * 租约即将到期(前五天生成缴费信息)
     */
    @Scheduled(cron = "${lease.insert.message.crop}")
    public void expireDueTask() {
        List<MszLease> leaseList = leaseMapper.selectList(new EntityWrapper<MszLease>()
                .eq("status", "1")
                .isNull("stopTime")
                .ge(true, "endTime", new Date()));
        if (leaseList != null && leaseList.size() > 0) {
            leaseList.forEach(item -> {
                Date endTime = new Date(item.getEndTime().getTime() - 5 * 24 * 60 * 60 * 1000);
                SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
                if (myFormatter.format(new Date()).equals(myFormatter.format(endTime.getTime()))) {
                   /* MszRoom mszRoom = mszRoomMapper.selectById(item.getRoomId());*/
                    //1.生成交费信息
                    MszOrderInfo orderInfo = new MszOrderInfo();
                    MszOrderInfo mszOrderInfo = new MszOrderInfo();
                    mszOrderInfo.setRoomId(item.getRoomId());
                    mszOrderInfo.setCreateTime(new Date());
                    mszOrderInfo.setLeaseId(item.getId());
                    mszOrderInfo.setRentPrice(item.getRentPrice());//租金
                    mszOrderInfo.setDepositPrice(item.getDepositPrice());//押金
                    mszOrderInfo.setTotal(item.getRentPrice());
                    String orderNo = UUIDUtils.getOrderIdByTime();
                    mszOrderInfo.setNo(orderNo);//缴费编号
                    mszOrderInfo.setTenantId(item.getTenantId());//房客ID(电话号码)
                    mszOrderInfo.setLandlordId(item.getRoomId());//房东ID
                    mszOrderInfo.setEndTime(item.getEndTime());//结束时间
                    orderInfoMapper.insert(orderInfo);
                    //2.租约即将到期(前五天) 向房客推送消息
                    MszMessage message = new MszMessage();
                    message.setType("1");//租约消息
                    message.setCreateTime(new Date());
                    message.setPromulgatorId(UserCommon.getCurrentUser().getId());
                    String leaseNo = item.getNo();//租约编号
                    Integer tenantId = item.getTenantId();//房客id
                    MszAccount account = accountMapper.selectById(tenantId);
                    String tenantName = account.getName();//房客名称
                    MessageTeamplate messageTeamplate = MessageTeamplateUtil.dueLeaseTenant(tenantName, leaseNo);
                    message.setReceiverId(tenantId);
                    message.setTitle(messageTeamplate.getTitle());
                    message.setContentText(messageTeamplate.getContentText());
                    messageMapper.insert(message);
                    //TODO
                    //3.租约即将到期 向房客发送短信
                    Map<String, Object> map = new HashMap<>();
                    map.put("leaseNo",item.getNo());
                    try {
                        MoblieMessageUtil.sendSms(account.getTel(), map,  "SMS_171540957");
                    } catch (ClientException e) {
                        e.printStackTrace();
                    }
                    //4.租约即将到期 并向业务员推送消息
                    Integer userId = item.getUserId();//业务员id
                    String salesmanName = userService.selectById(userId).getTrueName();//业务员名称
                    MessageTeamplate teamplate1 = SendMessage.dueLeaseSalesman(salesmanName, tenantName,leaseNo);
                    message.setReceiverId(userId);
                    message.setTitle(teamplate1.getTitle());
                    message.setContentText(teamplate1.getContentText());
                    message.setIsUser("1");
                    messageMapper.insert(message);
                }
            });
        }
    }

    /**
     * 租约到期
     */
    @Scheduled(cron = "${lease.update.data.crop}")
    public void updateStatusTask() {
        List<MszLease> leaseList = leaseMapper.selectList(new EntityWrapper<MszLease>()
                .eq("status", "1")
                .isNull("stopTime")
                .le(true, "endTime", new Date()));
        if (leaseList != null && leaseList.size() > 0) {
            leaseList.forEach(item -> {
                //修改租约状态为已结束
                item.setStatus("2");
                leaseMapper.updateById(item);
                //释放房源
                MszRoom mszRoom = mszRoomMapper.selectById(item.getRoomId());
                mszRoom.setStatus("0");
                mszRoomMapper.updateById(mszRoom);
                //1.同时生成退款记录
                MszRefundInfo refundInfo = new MszRefundInfo();
                refundInfo.setCreateTime(new Date());
                refundInfo.setApplyTime(new Date());
                refundInfo.setDepositPrice(item.getDepositPrice());
                refundInfo.setRentPrice(new BigDecimal(0));//租金默认为0
                BigDecimal total = new BigDecimal(0).add(item.getDepositPrice()); //退款金额
                refundInfo.setTotal(total);
                refundInfo.setLeaseId(item.getId());
                String refundNo =UUIDUtils.getOrderIdByTime();
                refundInfo.setNo(refundNo);
                refundInfo.setUserId(item.getUserId());
                refundInfo.setTenantId(item.getTenantId());
                refundInfoMapper.insert(refundInfo);
                //2.租约到期向房客推送消息
                MszMessage message = new MszMessage();
                message.setType("3");//退款消息
                message.setCreateTime(new Date());
                message.setPromulgatorId(UserCommon.getCurrentUser().getId());
                String leaseNo = item.getNo();//租约编号
                Integer tenantId = item.getTenantId();//房客id
                MszAccount tenantAccount = accountMapper.selectById(tenantId);
                String tenantName = tenantAccount.getName();//房客名称
                MessageTeamplate messageTeamplate = MessageTeamplateUtil.becomeDueLeaseTenant(tenantName, leaseNo, total);
                message.setReceiverId(tenantId);
                message.setTitle(messageTeamplate.getTitle());
                message.setContentText(messageTeamplate.getContentText());
                messageMapper.insert(message);
                //3.租约到期向房东推送消息
                Integer landlordId = item.getLandlordId();//房东id
                String landlordName = accountMapper.selectById(landlordId).getName();//房东名称
                MessageTeamplate messageTeamplate1 = MessageTeamplateUtil.becomeDueLeaseLandlord(landlordName, leaseNo, total);
                message.setReceiverId(landlordId);
                message.setTitle(messageTeamplate1.getTitle());
                message.setContentText(messageTeamplate1.getContentText());
                messageMapper.insert(message);
                //TODO
                // 4.租约到期发送短信给房客
                Map<String, Object> map = new HashMap<>();
                map.put("leaseNo",leaseNo);
                map.put("total",total);
                try {
                    MoblieMessageUtil.sendSms( tenantAccount.getTel(), map, "SMS_171540962");
                } catch (ClientException e) {
                    e.printStackTrace();
                }
                //5.租约到期向业务员推送消息
                Integer userId = item.getUserId();//业务员id
                String salesmanName = userService.selectById(userId).getTrueName();//业务员名称
                MessageTeamplate teamplate2 = SendMessage.becomeDueLeaseSalesman(salesmanName, tenantName,leaseNo, total);
                message.setReceiverId(userId);
                message.setTitle(teamplate2.getTitle());
                message.setContentText(teamplate2.getContentText());
                message.setIsUser("1");
                messageMapper.insert(message);
            });
        }
    }


}
