package com.msz.service;

import com.baomidou.mybatisplus.mapper.Wrapper;
import com.baomidou.mybatisplus.service.IService;
import com.github.pagehelper.PageInfo;
import com.msz.common.PagingRequest;
import com.msz.model.SysCity;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author cww
 * @since 2019-06-03
 */
public interface SysCityService extends IService<SysCity> {

    PageInfo<SysCity> listPage(PagingRequest pagingRequest, Wrapper wrapper);

    PageInfo<SysCity> getCityList();
}
