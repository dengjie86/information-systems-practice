package com.dormrepair.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormrepair.category.entity.RepairCategoryEntity;
import com.dormrepair.category.mapper.RepairCategoryMapper;
import com.dormrepair.common.exception.BusinessException;
import com.dormrepair.common.result.PageResult;
import com.dormrepair.common.result.ResultCode;
import com.dormrepair.order.dto.CreateRepairOrderRequest;
import com.dormrepair.order.entity.RepairOrderEntity;
import com.dormrepair.order.mapper.RepairOrderMapper;
import com.dormrepair.order.vo.CreateRepairOrderResponse;
import com.dormrepair.order.vo.RepairOrderDetailVO;
import com.dormrepair.order.vo.RepairOrderListItemVO;
import com.dormrepair.user.entity.UserEntity;
import com.dormrepair.user.mapper.UserMapper;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class RepairOrderService {

    private static final String ROLE_STUDENT = "STUDENT";
    private static final String STATUS_PENDING_AUDIT = "PENDING_AUDIT";
    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final RepairOrderMapper repairOrderMapper;
    private final UserMapper userMapper;
    private final RepairCategoryMapper categoryMapper;

    public RepairOrderService(
        RepairOrderMapper repairOrderMapper,
        UserMapper userMapper,
        RepairCategoryMapper categoryMapper
    ) {
        this.repairOrderMapper = repairOrderMapper;
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
    }

    @Transactional
    public CreateRepairOrderResponse create(Long loginUserId, String role, CreateRepairOrderRequest request) {
        if (!ROLE_STUDENT.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅学生可以提交报修工单");
        }

        UserEntity student = userMapper.selectById(loginUserId);
        if (student == null || student.getStatus() == null || student.getStatus() != 1) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "登录状态已失效，请重新登录");
        }
        if (!StringUtils.hasText(student.getDormBuilding()) || !StringUtils.hasText(student.getDormRoom())) {
            throw new BusinessException(ResultCode.CONFLICT, "请先完善宿舍信息后再提交报修");
        }

        RepairCategoryEntity category = categoryMapper.selectById(request.categoryId());
        if (category == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "故障分类不存在");
        }
        if (category.getStatus() == null || category.getStatus() != 1) {
            throw new BusinessException(ResultCode.CONFLICT, "该故障分类已停用");
        }

        RepairOrderEntity order = new RepairOrderEntity();
        order.setOrderNo(generateOrderNo());
        order.setUserId(loginUserId);
        order.setTitle(request.title().trim());
        order.setCategoryId(request.categoryId());
        order.setDescription(StringUtils.hasText(request.description()) ? request.description().trim() : null);
        order.setImageUrl(StringUtils.hasText(request.imageUrl()) ? request.imageUrl().trim() : null);
        order.setDormBuilding(student.getDormBuilding());
        order.setDormRoom(student.getDormRoom());
        if (StringUtils.hasText(request.contactPhone())) {
            order.setContactPhone(request.contactPhone().trim());
        } else if (StringUtils.hasText(student.getPhone())) {
            order.setContactPhone(student.getPhone().trim());
        }
        order.setStatus(STATUS_PENDING_AUDIT);
        order.setPriority(StringUtils.hasText(request.priority()) ? request.priority() : "NORMAL");
        order.setSubmitTime(LocalDateTime.now());
        repairOrderMapper.insert(order);

        return new CreateRepairOrderResponse(order.getId(), order.getOrderNo(), order.getStatus());
    }

    public PageResult<RepairOrderListItemVO> pageMyOrders(Long loginUserId, String role, long pageNum, long pageSize, String status) {
        if (!ROLE_STUDENT.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅学生可以查看自己的工单");
        }

        Page<RepairOrderEntity> page = repairOrderMapper.selectPage(
            new Page<>(pageNum, pageSize),
            new LambdaQueryWrapper<RepairOrderEntity>()
                .eq(RepairOrderEntity::getUserId, loginUserId)
                .eq(StringUtils.hasText(status), RepairOrderEntity::getStatus, status)
                .orderByDesc(RepairOrderEntity::getSubmitTime)
                .orderByDesc(RepairOrderEntity::getId)
        );

        Map<Long, RepairCategoryEntity> categoryMap = loadCategories(page.getRecords());
        List<RepairOrderListItemVO> records = page.getRecords().stream()
            .map(order -> toListItem(order, categoryMap.get(order.getCategoryId())))
            .toList();
        return new PageResult<>(records, page.getTotal(), pageNum, pageSize);
    }

    public RepairOrderDetailVO getMyOrderDetail(Long loginUserId, String role, Long orderId) {
        if (!ROLE_STUDENT.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅学生可以查看自己的工单");
        }

        RepairOrderEntity order = repairOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "工单不存在");
        }
        if (!Objects.equals(order.getUserId(), loginUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看其他学生的工单");
        }

        RepairCategoryEntity category = categoryMapper.selectById(order.getCategoryId());
        return toDetail(order, category);
    }

    private Map<Long, RepairCategoryEntity> loadCategories(List<RepairOrderEntity> orders) {
        List<Long> categoryIds = orders.stream()
            .map(RepairOrderEntity::getCategoryId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (categoryIds.isEmpty()) {
            return Map.of();
        }
        return categoryMapper.selectBatchIds(categoryIds).stream()
            .collect(Collectors.toMap(RepairCategoryEntity::getId, Function.identity()));
    }

    private RepairOrderListItemVO toListItem(RepairOrderEntity order, RepairCategoryEntity category) {
        return new RepairOrderListItemVO(
            order.getId(),
            order.getOrderNo(),
            order.getTitle(),
            order.getCategoryId(),
            category == null ? null : category.getCategoryName(),
            order.getDescription(),
            order.getImageUrl(),
            order.getDormBuilding(),
            order.getDormRoom(),
            order.getContactPhone(),
            order.getStatus(),
            order.getPriority(),
            order.getRejectReason(),
            order.getSubmitTime()
        );
    }

    private RepairOrderDetailVO toDetail(RepairOrderEntity order, RepairCategoryEntity category) {
        return new RepairOrderDetailVO(
            order.getId(),
            order.getOrderNo(),
            order.getTitle(),
            order.getCategoryId(),
            category == null ? null : category.getCategoryName(),
            order.getDescription(),
            order.getImageUrl(),
            order.getDormBuilding(),
            order.getDormRoom(),
            order.getContactPhone(),
            order.getStatus(),
            order.getPriority(),
            order.getRejectReason(),
            order.getAdminRemark(),
            order.getSubmitTime(),
            order.getAssignTime(),
            order.getAcceptTime(),
            order.getFinishTime(),
            order.getCloseTime()
        );
    }

    private String generateOrderNo() {
        return "WO" + LocalDateTime.now().format(ORDER_NO_FORMATTER) + String.format("%04d", (int) (Math.random() * 10000));
    }
}
