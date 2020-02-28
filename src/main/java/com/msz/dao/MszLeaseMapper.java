package com.msz.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.msz.VO.LeaseNumVO;
import com.msz.VO.LeaseReturnParamVO;
import com.msz.VO.WeProgramLeaseListVO;
import com.msz.model.MszLease;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * <p>
 * 租约 Mapper 接口
 * </p>
 *
 * @author cww
 * @since 2019-06-03
 */
@Mapper
@Repository
public interface MszLeaseMapper extends BaseMapper<MszLease> {

    /**
     * findList
     * @param returnParamVO
     * @return
     */
    List<LeaseReturnParamVO> findList(LeaseReturnParamVO returnParamVO);

    /**
     * 租约列表
     * @param leaseReturnParamVO
     * @return
     */
    List<LeaseReturnParamVO> getLeaseList(LeaseReturnParamVO leaseReturnParamVO);

    /**
     * 获得租约状态为0预约中/1执行中/2已结束/3异常的数量
     * @param status
     * @return
     */
    Integer getLeaseNum(@Param("status") String status, @Param("orgId") Integer orgId);

    /**
     * 查看租约
     * @param id
     * @return
     */
    LeaseReturnParamVO getLeaseDesc(@Param("id") Integer id);


    /**
    * @Author Maoyy
    * @Description 小程序租约列表
    * @Date 14:39 2019/6/19
    **/
    List<WeProgramLeaseListVO> leaseByTenantId(@Param("id") Integer id);

    /**
     * 房客是否签租约
     * @param tenantId
     * @return
     */
    List<MszLease> signLeaseByTenantId(Integer tenantId);
}
