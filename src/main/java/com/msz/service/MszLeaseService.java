package com.msz.service;

import com.baomidou.mybatisplus.service.IService;
import com.github.pagehelper.PageInfo;
import com.msz.VO.*;
import com.msz.common.PagingRequest;
import com.msz.model.MszLease;
import com.msz.model.SysUser;

import java.util.List;

/**
 * <p>
 * 租约 服务类
 * </p>
 *
 * @author cww
 * @since 2019-06-03
 */
public interface MszLeaseService extends IService<MszLease> {

    PageInfo<LeaseReturnParamVO> listPage(PagingRequest pagingRequest, String status, String tenantName, SysUser user);

    /**
     * 新增租约
     * @param  leaseParamVO
     * @return
     */
    boolean insertMszLease(LeaseParamVO leaseParamVO, SysUser user);


   /* *//**
     * 修改租约
     * @param leaseParamVO
     * @return
     *//*
    boolean updateMszLease(LeaseParamVO leaseParamVO);*/

    /**
     * 查看租约
     * @param id
     * @return
     */
    LeaseReturnParamVO getMszLeaseById(Integer id);

    /**
     * @Author Maoyy
     * @Description 代码写了一行又一行
     * @Date 17:34 2019/6/13
     **/
    TenantIdAndUserIdVO tenantIdAndUserId(Integer tenantId, Integer userId);

    /**
     * @Author Maoyy
     * @Description 代码写了一行又一行
     * @Date 9:41 2019/6/14
     **/
    TenantIdAndUserIdVO byTenantId(Integer id);
    /**
     * @Author Maoyy
     * @Description 代码写了一行又一行
     * @Date 9:41 2019/6/14
     **/
    TenantIdAndUserIdVO byId(Integer id);

    /**
    * @Author Maoyy
    * @Description 代码写了一行又一行
    * @Date 14:26 2019/6/19
    **/
    List<WeProgramLeaseListVO> leaseByTenantId(Integer id);

    /**
     * 获得租约状态为0预约中/1执行中/2已结束/3异常的数量
     * @return
     */
    LeaseNumVO getCountNum(SysUser user);

    /**
     * 房客是否签租约
     * @param tenantId
     * @return
     */
    List<MszLease> signLeaseByTenantId(Integer tenantId);
}
