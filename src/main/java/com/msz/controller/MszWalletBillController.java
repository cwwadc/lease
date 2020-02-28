package com.msz.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.github.pagehelper.PageInfo;
import com.msz.common.PagingRequest;
import com.msz.common.RespEntity;
import com.msz.model.MszWalletBill;
import com.msz.service.MszWalletBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import java.util.Date;


/**
 * <p>
 * 钱包流水 前端控制器
 * </p>
 *
 * @author cww
 * @since 2019-06-03 ${time}
 */
@Api(value = "/msz-wallet-bills", description = "钱包流水 接口; Responseble:cww")
@RestController
@RequestMapping("/msz-wallet-bills")
public class MszWalletBillController {

    /**
     * id  Integer
     * <p>
     * 账号id  accountId  Integer
     * <p>
     * 金额  amt  BigDecimal
     * <p>
     * 账单类型：1充值，2提现，3月租，4退款  type  String
     * <p>
     * createTime  Date
     * <p>
     * 关联id  receiverId  String
     * <p>
     * 关联类型  receiverType  String
     * <p>
     * 流水号  no  String
     */
    @Autowired
    private MszWalletBillService mszWalletBillService;


    @GetMapping("{id}")
    @ApiOperation(value = "根据id获取单个MszWalletBill", notes = "根据id获取单个MszWalletBill")
    public RespEntity<MszWalletBill> get(@PathVariable Integer id) {
        return RespEntity.ok().setResponseContent(mszWalletBillService.selectById(id));
    }

    @GetMapping("byAccountId/{id}")
    @ApiOperation(value = "查询交易记录*******小程序已使用(租客)@Author=Maoyy", notes = "查询交易记录*******小程序已使用(租客)@Author=Maoyy")
    public RespEntity<MszWalletBill> byAccountId(@PathVariable Integer id) {
        return RespEntity.ok().setResponseContent(mszWalletBillService.selectList(new EntityWrapper<MszWalletBill>().eq("accountId", id).orderBy("createTime", false)));
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "offset", value = "起始页", required = true, paramType = "query"),
            @ApiImplicitParam(name = "limit", value = "记录数", required = true, paramType = "query")
    })
    @ApiOperation(value = "分页查询MszWalletBill", notes = "分页查询MszWalletBill")
    @GetMapping
    public RespEntity<PageInfo<MszWalletBill>> list(PagingRequest pagingRequest) {
        return RespEntity.ok().setResponseContent(mszWalletBillService.listPage(pagingRequest, null));
    }


    @PostMapping("/insert")
    @ApiOperation(value = "保存MszWalletBill", notes = "保存MszWalletBill")
    public RespEntity insert(@RequestBody MszWalletBill mszWalletBill) {
        mszWalletBill.setCreateTime(new Date());
        if (!mszWalletBillService.insert(mszWalletBill)) {
            return RespEntity.badRequest("保存失败");
        }
        return RespEntity.ok("保存成功");
    }

    @PutMapping
    @ApiOperation(value = "根据ID修改MszWalletBill", notes = "根据ID修改MszWalletBill")
    public RespEntity update(@RequestBody MszWalletBill mszWalletBill) {
        if (!mszWalletBillService.updateById(mszWalletBill)) {
            return RespEntity.badRequest("更新失败");
        }
        return RespEntity.ok("更新成功");
    }


    @DeleteMapping("{id}")
    @ApiOperation(value = "根据ID删除MszWalletBill", notes = "根据ID删除MszWalletBill")
    public RespEntity delete(@PathVariable Integer id) {
        if (!mszWalletBillService.deleteById(id)) {
            return RespEntity.badRequest("删除失败");
        }
        return RespEntity.ok("删除成功");
    }


}