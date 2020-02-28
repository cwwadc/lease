package com.msz.VO;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class LeaseParamVO {

    /**
     * 租约id
     */
    private Integer id;
    /**
     * 所属网点id
     */
    private Integer orgId;
    /**
     * 房间ID
     */
    private Integer roomId;
    /**
     * 房东ID
     */
    private Integer landlordId;
    /**
     * 房客ID
     */
    private Integer tenantId;
    /**
     * 业务员id
     */
    private Integer userId;
    /**
     * 截止时间
     */
    private Date stopTime;
    /**
     * 开始时间
     */
    private Date startTime;
    /**
     * 结束时间
     */
    private Date endTime;
    /**
     * 提前终止时间
     */
    private Date terminationTime;
    /**
     * 合同照片
     */
    private String contractPicture;
    /**
     * 创建人
     */
    private Integer operator;
    /**
     * 生成时间
     */
    private Date createTime;

    //租约修改过后的租金押金
    /**
     * 付款方式(0: 月付  1 季付 2: 半年付 3: 一年付  )
     */
    private Integer payWay;
    /**
     * 押金
     */
    private String depositPrice;
    /**
     * 租金
     */
    private String rentPrice;

    //房客信息
    /**
     * 房客电话
     */
    private String tenantName;
    /**
     * 房客电话
     */
    private String tel;
    /**
     * 房客电话
     */
    private String idCard;

}
