package com.msz.controller;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.msz.VO.*;
import com.msz.annotation.CurrentUser;
import com.msz.annotation.LoginRequired;
import com.msz.common.PagingRequest;
import com.msz.common.RespEntity;
import com.msz.common.UserCommon;
import com.msz.model.*;
import com.msz.service.*;
import com.msz.util.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * <p>
 * 提现记录 前端控制器
 * </p>
 *
 * @author cww
 * @since 2019-06-03 ${time}
 */
@Api(value = "/msz-withdraws", description = "提现记录 接口; Responseble:cww")
@RestController
@RequestMapping("/msz-withdraws")
public class MszWithdrawController {

    /**
     * id  Integer
     * <p>
     * 编号  no  String
     * <p>
     * 用户id  accountId  Integer
     * <p>
     * 提现金额  amt  BigDecimal
     * <p>
     * 收款账号  cardNo  String
     * <p>
     * 户名  accountName  String
     * <p>
     * 状态：0待处理，1成功，2拒绝提现  status  String
     * <p>
     * 处理人  operator  String
     * <p>
     * 备注  note  String
     * <p>
     * 处理时间  updateTime  Date
     * <p>
     * 申请时间  createTime  Date
     */
    @Autowired
    private MszWithdrawService mszWithdrawService;
    @Autowired
    MszAccountService accountService;
    @Autowired
    private MszWalletService mszWalletService;
    @Autowired
    private MszMessageService messageService;
    @Autowired
    UserCommon userCommon;
    @Autowired
    SysOrgService orgService;

    @GetMapping("/getListByYearMonth")
    @ApiOperation(value = "支付报表汇总", notes = "支付报表汇总")
    public RespEntity<WithdrawVO> getListByYearMonth(String createTimeMin, String createTimeMax) {

        return RespEntity.ok().setResponseContent(mszWithdrawService.getListByYearMonth(createTimeMin, createTimeMax));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "offset", value = "起始页", required = true, paramType = "query"),
            @ApiImplicitParam(name = "limit", value = "记录数", required = true, paramType = "query"),
    })
    @GetMapping("/payList")
    @ApiOperation(value = "支付明细列表", notes = "支付明细列表")
    public RespEntity<MszWithdraw> payList(PagingRequest pagingRequest, String createTimeMin,
                                           String createTimeMax, String date) {

        return RespEntity.ok().setResponseContent(mszWithdrawService.findList(pagingRequest, createTimeMin, createTimeMax, date));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "offset", value = "起始页", required = true, paramType = "query"),
            @ApiImplicitParam(name = "limit", value = "记录数", required = true, paramType = "query"),
            @ApiImplicitParam(name = "status", value = "状态：0待处理，1成功，2拒绝提现")
    })
    @GetMapping("/listAll")
    @ApiOperation(value = "提现记录列表", notes = "提现记录列表")
    public RespEntity<WithdrawReturnParamVO> listAll(PagingRequest pagingRequest, String status) {

        return RespEntity.ok().setResponseContent(mszWithdrawService.listAll(pagingRequest, status));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "属于谁的提现记录")
    })
    @GetMapping("/list/{id}")
    @ApiOperation(value = "不分页按照Id找到提现记录*******小程序已使用(租客)@Author=Maoyy", notes = "不分页按照Id找到提现记录*******小程序已使用(租客)@Author=Maoyy")
    public RespEntity<MszWithdraw> list(@PathVariable Integer id) {
        return RespEntity.ok().setResponseContent(mszWithdrawService.selectList(new EntityWrapper<MszWithdraw>().eq("accountId", id)));
    }

    @GetMapping("/getCountNum")
    @ApiOperation(value = "查询 待审核/已同意/已拒绝 数量", notes = "查询 待审核/已同意/已拒绝 数量")
    public RespEntity<MszWithdrawStatusNum> getCountNum() {

        return RespEntity.ok().setResponseContent(mszWithdrawService.getCountNum());
    }


    @PutMapping("/updateStatus")
    @ApiOperation(value = "同意/拒绝", notes = "同意/拒绝")
    @LoginRequired
    public RespEntity updateStatus(@RequestParam Integer id, @RequestParam Integer operator, @RequestParam String status, @CurrentUser SysUser user) {
        MszWithdraw withdraw = mszWithdrawService.selectById(id);
        withdraw.setStatus(status);
        withdraw.setOperator(operator);
        if (!mszWithdrawService.updateById(withdraw)) {
            return RespEntity.badRequest("更新失败");
        }
        WithdrawReturnParamVO vo = new WithdrawReturnParamVO();
        BeanUtils.copyProperties(withdraw, vo);
        MszMessage message = new MszMessage();
        message.setType("2");//提现消息
        message.setCreateTime(new Date());
        message.setReceiverId(withdraw.getAccountId());
        message.setPromulgatorId(user.getId());
        if (status.equals("1")) {//提现同意
            MessageTeamplate messageTeamplate = MessageTeamplateUtil.withdrawSuccess(withdraw.getAmt(), withdraw.getNo());
            message.setTitle(messageTeamplate.getTitle());
            message.setContentText(messageTeamplate.getContentText());
            messageService.insert(message);
            MszAccount account = accountService.selectById(withdraw.getAccountId());
            vo.setMszAccount(account);
            return RespEntity.ok().setResponseContent(vo);
        }
        if (status.equals("2")) {//提现拒绝
            MszWallet wallet = mszWalletService.selectOne(new EntityWrapper<MszWallet>().eq("accountId", withdraw.getAccountId()));
            wallet.setBalance(wallet.getBalance().add(withdraw.getAmt()));
            mszWalletService.updateById(wallet);
            MessageTeamplate messageTeamplate = MessageTeamplateUtil.withdrawFailure(withdraw.getNo());
            message.setTitle(messageTeamplate.getTitle());
            message.setContentText(messageTeamplate.getContentText());
            messageService.insert(message);
            return RespEntity.ok("提现拒绝成功");
        }
        return RespEntity.ok("更新成功");
    }


    @ApiImplicitParams({
            @ApiImplicitParam(name = "offset", value = "起始页", required = true, paramType = "query"),
            @ApiImplicitParam(name = "limit", value = "记录数", required = true, paramType = "query")
    })
    @GetMapping("getByAccountId/{id}")
    @ApiOperation(value = "按照用户Id找到所有的提现记录-------小程序(房东)@Author=Maoyy", notes = "按照用户Id找到所有的提现记录-------小程序(房东)@Author=Maoyy")
    public RespEntity<MszWithdraw> getByAccountId(PagingRequest pagingRequest, @PathVariable Integer id) {
        return RespEntity.ok().setResponseContent(mszWithdrawService.listPage(pagingRequest, new EntityWrapper<MszWithdraw>().eq("accountId", id)));
    }

    @GetMapping("{id}")
    @ApiOperation(value = "按照Id找到提现记录*******小程序已使用(房东)", notes = "按照Id找到提现记录*******小程序已使用(房东)")
    public RespEntity<MszWithdraw> get(@PathVariable Integer id) {
        return RespEntity.ok().setResponseContent(mszWithdrawService.selectById(id));
    }


    @PostMapping
    @ApiOperation(value = "添加提现记录+提现扣款*******小程序已使用(房东)", notes = "添加提现记录+提现扣款*******小程序已使用(房东)")
    public RespEntity insert(@RequestBody MszWithdraw mszWithdraw) {
        String code = UUIDUtils.getOrderIdByTime();
        mszWithdraw.setNo(code);
        if (!mszWithdrawService.insert(mszWithdraw)) {
            return RespEntity.badRequest("保存失败");
        }
        //向后台管理系统发送提现消息(管理员和财务人员)
        MszMessage message = new MszMessage();
        message.setType("2");//提现消息
        message.setCreateTime(new Date());
        //TODO
        Integer orgId = accountService.selectById(mszWithdraw.getAccountId()).getOrgId();
        Integer stafId = orgService.selectById(orgId).getStafId();
        message.setReceiverId(stafId);//后台管理系统
        message.setPromulgatorId(mszWithdraw.getAccountId());
        message.setIsUser("1");
        MessageTeamplateUtil teamplateUtil = new MessageTeamplateUtil();
        MessageTeamplate teamplate = teamplateUtil.withdrawApply(mszWithdraw.getAccountName(), mszWithdraw.getAmt(), mszWithdraw.getNo());
        message.setContentText(teamplate.getContentText());
        message.setTitle(teamplate.getTitle());
        messageService.insert(message);
        if (mszWalletService.withdraw(mszWithdraw.getAccountId(), mszWithdraw.getAmt())) {
            return RespEntity.ok("保存成功");
        } else {
            return RespEntity.ok("保存失败");
        }
    }


    @PutMapping
    @ApiOperation(value = "根据ID修改MszWithdraw", notes = "根据ID修改MszWithdraw")
    public RespEntity update(@RequestBody MszWithdraw mszWithdraw) {
        if (!mszWithdrawService.updateById(mszWithdraw)) {
            return RespEntity.badRequest("更新失败");
        }
        return RespEntity.ok("更新成功");
    }


    @DeleteMapping("{id}")
    @ApiOperation(value = "根据ID删除MszWithdraw", notes = "根据ID删除MszWithdraw")
    public RespEntity delete(@PathVariable Integer id) {
        if (!mszWithdrawService.deleteById(id)) {
            return RespEntity.badRequest("删除失败");
        }
        return RespEntity.ok("删除成功");
    }


    @ApiOperation(value = "支出明细导出excel", notes = "支出明细导出excel")
    @GetMapping("/exportExcelData")
    public void exportExcelData(String date, HttpServletResponse response) {
        //excel标题
        String title = "支付报表";
        //excel表名
        String[] headers = {"名称", "交易账号", "姓名", "金额", "交易时间", "流水号"};
        //excel文件名
        String fileName = title + System.currentTimeMillis() + ".xls";
        List<WithdrawReturnParamVO> list = mszWithdrawService.exportExcelData(date);
        //excel元素
        String content[][] = new String[list.size()][6];
        for (int i = 0; i < list.size(); i++) {
            content[i] = new String[headers.length];
            String type = "提现";
            content[i][0] = type;
            content[i][1] = list.get(i).getAccountTel();
            content[i][2] = list.get(i).getAccountName();
            content[i][3] = list.get(i).getAmt().toString();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String format = sdf.format(list.get(i).getCreateTime());
            content[i][4] = format;
            content[i][5] = list.get(i).getNo();
        }

        //创建HSSFWorkbook
        XSSFWorkbook hssfWorkbook = ExportExcel.getHSSFWorkbook(title, headers, content);

        //响应到客户端
        try {
            this.setResponseHeader(response, fileName);
            OutputStream os = response.getOutputStream();
            hssfWorkbook.write(os);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 设置响应头
     */
    private void setResponseHeader(HttpServletResponse aResponse, String aFileName) {
        try {
            try {
                aFileName = new String(aFileName.getBytes(), "ISO8859-1");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            aResponse.setContentType("application/octet-stream;charset=ISO8859-1");
            aResponse.setHeader("Content-Disposition", "attachment;filename=" + aFileName);
            aResponse.addHeader("Pargam", "no-cache");
            aResponse.addHeader("Cache-Control", "no-cache");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}