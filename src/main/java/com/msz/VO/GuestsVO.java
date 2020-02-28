package com.msz.VO;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.enums.IdType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Author Maoyy
 * @Description PC房客
 * @Date 2019/6/17 17:56
 */
@Data
public class GuestsVO {

    /**
     * 登录密码
     */
    private String pwd;
    /**
     * 头像
     */
    private String logo;
    /**
     * 账号类型：1房东，2房客
     */
    private String type;
    /**
     * 联系地址
     */
    private String address;
    /**
     * 身份证照片正面
     */
    private String imgIdcard1;
    /**
     * 身份证照片正面
     */
    private String imgIdcard2;
    /**
     * 微信关联ID
     */
    private String openId;
    /**
     * 是否删除
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

    private Integer id;
    private String tel;
    private String idCard;
    private Integer state;
    private Integer orgId;
    private String orgName;
    private String phone;
    private String sex;
    private String name;
    private String salesmanName;
    private String houseNumber;
    private String room;
}
