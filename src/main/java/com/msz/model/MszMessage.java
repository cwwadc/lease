package com.msz.model;

import com.baomidou.mybatisplus.enums.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableName;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * <p>
 * 消息
 * </p>
 *
 * @author cww
 * @since 2019-06-03
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "MszMessage对象", description = "消息")
@TableName("msz_message")
public class MszMessage extends Model<MszMessage> {

    private static final long serialVersionUID = 1L;


	@TableId(value="id", type= IdType.AUTO)
	@ApiModelProperty(value = "", name = "id")
	private Integer id;
    /**
     * 发布信息的账号
     */
	@ApiModelProperty(value = "发布信息的账号", name = "promulgatorId")
	private Integer promulgatorId;
    /**
     * 接收信息的账号
     */
	@ApiModelProperty(value = "接收信息的账号", name = "receiverId")
	private Integer receiverId;
    /**
     * 是否是 sys_user表的消息 0不是 1是
     */
	@ApiModelProperty(value = "是否是sys_user表的消息 0不是 1是", name = "isUser")
	private String isUser;
    /**
     * 消息类型，0：系统消息，1：租约消息 , 2 : 提现 3：退款
     */
	@ApiModelProperty(value = "消息类型，0：系统消息，1：租约消息 , 2 : 提现 3：退款", name = "type")
	private String type;
    /**
     * 标题
     */
	@ApiModelProperty(value = "标题", name = "title")
	private String title;
    /**
     * 文本内容
     */
	@ApiModelProperty(value = "文本内容", name = "contentText")
	private String contentText;
    /**
     * 关联ID
     */
	@ApiModelProperty(value = "关联ID", name = "relId")
	private Integer relId;
    /**
     * 0未读1已读
     */
	@ApiModelProperty(value = "0未读1已读", name = "isRead")
	private Integer isRead;
	@ApiModelProperty(value = "", name = "isDel")
	private String isDel;
    /**
     * 更新时间
     */
	@ApiModelProperty(value = "更新时间", name = "updateTime")
	private Date updateTime;
    /**
     * 消息创建时间
     */
	@ApiModelProperty(value = "消息创建时间", name = "createTime")
	private Date createTime;


	@Override
	protected Serializable pkVal() {
		return this.id;
	}

}
