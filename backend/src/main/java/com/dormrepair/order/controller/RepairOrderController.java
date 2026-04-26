package com.dormrepair.order.controller;

import com.dormrepair.common.result.PageResult;
import com.dormrepair.common.result.Result;
import com.dormrepair.order.dto.CreateRepairOrderRequest;
import com.dormrepair.order.service.RepairOrderService;
import com.dormrepair.order.vo.CreateRepairOrderResponse;
import com.dormrepair.order.vo.RepairOrderDetailVO;
import com.dormrepair.order.vo.RepairOrderListItemVO;
import com.dormrepair.security.LoginUser;
import com.dormrepair.security.LoginUserContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/orders")
public class RepairOrderController {

    private final RepairOrderService repairOrderService;

    public RepairOrderController(RepairOrderService repairOrderService) {
        this.repairOrderService = repairOrderService;
    }

    @PostMapping
    public Result<CreateRepairOrderResponse> create(@Valid @RequestBody CreateRepairOrderRequest request) {
        LoginUser loginUser = LoginUserContext.requireUser();
        return Result.success(repairOrderService.create(loginUser.userId(), loginUser.role(), request));
    }

    @GetMapping("/my")
    public Result<PageResult<RepairOrderListItemVO>> myOrders(
        @RequestParam(defaultValue = "1") @Min(value = 1, message = "pageNum must be >= 1") long pageNum,
        @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize must be >= 1") @Max(value = 100, message = "pageSize must be <= 100") long pageSize,
        @RequestParam(required = false) String status
    ) {
        LoginUser loginUser = LoginUserContext.requireUser();
        return Result.success(repairOrderService.pageMyOrders(loginUser.userId(), loginUser.role(), pageNum, pageSize, status));
    }

    @GetMapping("/{id}")
    public Result<RepairOrderDetailVO> detail(@PathVariable Long id) {
        LoginUser loginUser = LoginUserContext.requireUser();
        return Result.success(repairOrderService.getMyOrderDetail(loginUser.userId(), loginUser.role(), id));
    }
}
