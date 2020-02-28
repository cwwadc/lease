package com.msz.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.github.pagehelper.PageInfo;
import com.msz.VO.GuestsVO;
import com.msz.VO.LeaseReturnParamVO;
import com.msz.VO.SignedNumAndNotSignedNumAndDisableNumVO;
import com.msz.common.PagingRequest;
import com.msz.model.MszAccount;
import com.msz.model.MszLease;
import com.msz.model.MszRoom;
import com.msz.model.SysUser;

import java.util.List;

/**
 * <p>
 * 用户信息 服务类
 * </p>
 *
 * @author cww
 * @since 2019-06-03
 */
public interface MszAccountService extends IService<MszAccount> {


    PageInfo<MszAccount> listPage(PagingRequest pagingRequest, Wrapper wrapper);

    /**
     * @Author Maoyy
     * @Description 代码写了一行又一行
     * @Date 17:36 2019/6/13
     **/
    PageInfo<MszAccount> leaseByAccount(Integer id);
    /**
    * @Author Maoyy
    * @Description 代码写了一行又一行
    * @Date 10:03 2019/6/18
    **/
    PageInfo<GuestsVO> getGuestsList(PagingRequest pagingRequest,String name,String state, SysUser user);

    /**
    * @Author Maoyy
    * @Description 代码写了一行又一行
    * @Date 10:03 2019/6/18
    **/

    SignedNumAndNotSignedNumAndDisableNumVO getSignedNumAndNotSignedNumAndDisableNum(SysUser user);

    /**
     * 根据房客id查询租约信息
     * @param id
     * @param status
     * @return
     */
    List<LeaseReturnParamVO> getMszLeaseByTenantId(Integer id, String status);

    /**
     * 根据房东id查询房源列表
     * @param  pagingRequest
     * @param landlordId
     * @return
     */
    PageInfo<MszRoom> getMszRoomByLandlordId(PagingRequest pagingRequest,Integer landlordId);

    /**
     * 根据账户tel电话查询没有签约的房客
     * @param tel
     * @return
     */
    List<MszAccount> getTenantByTel(String tel, SysUser user);
}
