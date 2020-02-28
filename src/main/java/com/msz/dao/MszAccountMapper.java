package com.msz.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.msz.VO.GuestsVO;
import com.msz.model.MszAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 用户信息 Mapper 接口
 * </p>
 *
 * @author cww
 * @since 2019-06-03
 */
@Mapper
@Repository
public interface MszAccountMapper extends BaseMapper<MszAccount> {

    List<MszAccount> leaseByAccount(@Param("id") Integer id);

    /**
     * 未签约/禁用
     * @param guestsVO
     * @return
     */
    List<GuestsVO> getGuestsListUnsigned(GuestsVO guestsVO);

    /**
     * 已签约
     * @param guestsVO
     * @return
     */
    List<GuestsVO> getGuestsList(GuestsVO guestsVO);

    /**
     * 已签约数量
     * @param  guestsVO
     * @return
     */
    Integer getGuestsNum(GuestsVO guestsVO);

    /**
     * 未签约/禁用 数量
     * @param guestsVO
     * @return
     */
    Integer getUnsignedNum(GuestsVO guestsVO);

    List<MszAccount> getTenantByTel(MszAccount mszAccount);

}
