package com.msz.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.msz.model.MszRoom;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 房源信息 Mapper 接口
 * </p>
 *
 * @author cww
 * @since 2019-06-10
 */
@Mapper
@Repository
public interface MszRoomMapper extends BaseMapper<MszRoom> {

    List<MszRoom> findList(MszRoom room);

}
