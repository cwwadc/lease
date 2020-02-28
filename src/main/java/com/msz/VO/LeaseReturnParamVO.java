package com.msz.VO;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class LeaseReturnParamVO {


    private Integer id;
    /**
     * 租约编号
     */
    private String no;
    /**
     * 房间ID
     */
    private Integer roomId;
    /**
     * 房东ID
     */
    private Integer landlordId;
    /**
     * 房东名称
     */
    private String landlordName;
    /**
     * 房客ID
     */
    private Integer tenantId;
    /**
     * 业务员
     */
    private Integer userId;
    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 结束时间
     */
    private Date endTime;
    /**
     * 中止时间
     */
    private Date sotpTime;
    /**
     * 应付款
     */
    private BigDecimal duePrice;
    /**
     * 合同照片
     */
    private String contractPicture;
    /**
     * 租约状态 0预约中 1执行中 2已结束 3异常
     */
    private String status;
    /**
     * 创建人
     */
    private Integer operator;
    /**
     * 创建人名称
     */
    private String operatorName;
    /**
     * 是否删除
     *
     */
    private String isDel;
    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 生成时间
     */
    private Date createTime;

    //房屋信息
    /**
     * 房源编号
     */
    private String roomNo;
    /**
     * 房间名称
     */
    private String roomName;
    /**
     * 省份id
     */
    private Integer provinceId;
    /**
     * 身份
     */
    private String province;
    /**
     * 城市id
     */
    private Integer cityId;
    /**
     * 城市
     */
    private String city;
    /**
     * 区县id
     */
    private Integer countyId;
    /**
     * 区县
     */
    private String county;
    /**
     * 街道id
     */
    private Integer townId;
    /**
     * 街道
     */
    private String town;
    /**
     * 门牌号
     */
    private String houseNumber;
    /**
     * 地址
     */
    private String address;
    /**
     * 小区
     */
    private String community;
    /**
     * 室
     */
    private String room;
    /**
     * 厅
     */
    private String hall;
    /**
     * 卫
     */
    private String toilet;
    /**
     * 楼层
     */
    private String floor;
    /**
     * 共几层
     */
    private String whichFloor;
    /**
     * 房间面积
     */
    private Double area;
    /**
     * 网点编号
     */
    private Integer orgId;
    /**
     * 网点名称
     */
    private String orgName;
    /**
     * 网点编号
     */
    private String orgCode;
    /**
     * 付款方式
     */
    private Integer payWay;
    /**
     * 付款类型
     */
    private Integer payType;
    /**
     * 租金价格
     */
    private BigDecimal rentPrice;
    /**
     * 押金
     */
    private BigDecimal depositPrice;

    //房客信息
    /**
     * 房客名称
     */
    private String tenantName;
    /**
     * 房客账号
     */
    private String tel;
    /**
     * 房客身份证
     */
    private String idCard;

    /**
     * 房客手机号
     */
    private String tenantPhone;


    //业务员名称
    private String salesmanName;

}
