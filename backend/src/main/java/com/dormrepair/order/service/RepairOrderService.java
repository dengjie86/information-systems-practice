package com.dormrepair.order.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormrepair.category.entity.RepairCategoryEntity;
import com.dormrepair.category.mapper.RepairCategoryMapper;
import com.dormrepair.common.constant.OrderStatuses;
import com.dormrepair.common.constant.UserRoles;
import com.dormrepair.common.exception.BusinessException;
import com.dormrepair.common.result.PageResult;
import com.dormrepair.common.result.ResultCode;
import com.dormrepair.order.dto.AdminOrderAssignRequest;
import com.dormrepair.order.dto.AdminOrderAuditRequest;
import com.dormrepair.order.dto.CreateRepairOrderRequest;
import com.dormrepair.order.dto.WorkerAcceptRequest;
import com.dormrepair.order.dto.WorkerFinishRequest;
import com.dormrepair.order.dto.WorkerRecordRequest;
import com.dormrepair.order.dto.WorkerRejectRequest;
import com.dormrepair.record.entity.RepairRecordEntity;
import com.dormrepair.record.mapper.RepairRecordMapper;
import com.dormrepair.order.entity.RepairOrderEntity;
import com.dormrepair.order.mapper.RepairOrderMapper;
import com.dormrepair.order.vo.AdminRepairOrderDetailVO;
import com.dormrepair.order.vo.AdminRepairOrderListItemVO;
import com.dormrepair.order.vo.CreateRepairOrderResponse;
import com.dormrepair.order.vo.RepairOrderDetailVO;
import com.dormrepair.order.vo.RepairOrderListItemVO;
import com.dormrepair.order.vo.RepairRecordVO;
import com.dormrepair.user.entity.UserEntity;
import com.dormrepair.user.mapper.UserMapper;
import java.util.ArrayList;
import java.util.UUID;
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

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final RepairOrderMapper repairOrderMapper;
    private final UserMapper userMapper;
    private final RepairCategoryMapper categoryMapper;
    private final RepairRecordMapper repairRecordMapper;

    public RepairOrderService(
        RepairOrderMapper repairOrderMapper,
        UserMapper userMapper,
        RepairCategoryMapper categoryMapper,
        RepairRecordMapper repairRecordMapper
    ) {
        this.repairOrderMapper = repairOrderMapper;
        this.userMapper = userMapper;
        this.categoryMapper = categoryMapper;
        this.repairRecordMapper = repairRecordMapper;
    }

    @Transactional
    public CreateRepairOrderResponse create(Long loginUserId, String role, CreateRepairOrderRequest request) {
        if (!UserRoles.STUDENT.equals(role)) {
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
        order.setStatus(OrderStatuses.PENDING_AUDIT);
        order.setPriority(StringUtils.hasText(request.priority()) ? request.priority() : "NORMAL");
        order.setSubmitTime(LocalDateTime.now());
        repairOrderMapper.insert(order);

        return new CreateRepairOrderResponse(order.getId(), order.getOrderNo(), order.getStatus());
    }

    public PageResult<RepairOrderListItemVO> pageMyOrders(Long loginUserId, String role, long pageNum, long pageSize, String status) {
        if (!UserRoles.STUDENT.equals(role) && !UserRoles.WORKER.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅学生和维修人员可以查看自己的工单");
        }

        LambdaQueryWrapper<RepairOrderEntity> queryWrapper = new LambdaQueryWrapper<RepairOrderEntity>()
            .eq(StringUtils.hasText(status), RepairOrderEntity::getStatus, status)
            .orderByDesc(RepairOrderEntity::getSubmitTime)
            .orderByDesc(RepairOrderEntity::getId);

        if (UserRoles.STUDENT.equals(role)) {
            queryWrapper.eq(RepairOrderEntity::getUserId, loginUserId);
        } else {
            queryWrapper.eq(RepairOrderEntity::getAssignedWorkerId, loginUserId);
        }

        Page<RepairOrderEntity> page = repairOrderMapper.selectPage(
            new Page<>(pageNum, pageSize),
            queryWrapper
        );

        Map<Long, RepairCategoryEntity> categoryMap = loadCategories(page.getRecords());
        List<RepairOrderListItemVO> records = page.getRecords().stream()
            .map(order -> toListItem(order, categoryMap.get(order.getCategoryId())))
            .toList();
        return new PageResult<>(records, page.getTotal(), pageNum, pageSize);
    }

    public RepairOrderDetailVO getMyOrderDetail(Long loginUserId, String role, Long orderId) {
        if (!UserRoles.STUDENT.equals(role) && !UserRoles.WORKER.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅学生和维修人员可以查看自己的工单");
        }

        RepairOrderEntity order = repairOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "工单不存在");
        }
        if (UserRoles.STUDENT.equals(role) && !Objects.equals(order.getUserId(), loginUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看其他学生的工单");
        }
        if (UserRoles.WORKER.equals(role) && !Objects.equals(order.getAssignedWorkerId(), loginUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看未分配给自己的工单");
        }

        RepairCategoryEntity category = categoryMapper.selectById(order.getCategoryId());
        List<RepairRecordVO> records = loadRepairRecords(orderId);
        return toDetail(order, category, records);
    }

    public PageResult<AdminRepairOrderListItemVO> pageAdminOrders(
        String role,
        long pageNum,
        long pageSize,
        String status,
        Long userId,
        Long assignedWorkerId
    ) {
        checkAdmin(role);

        LambdaQueryWrapper<RepairOrderEntity> queryWrapper = new LambdaQueryWrapper<RepairOrderEntity>()
            .eq(StringUtils.hasText(status), RepairOrderEntity::getStatus, status)
            .eq(userId != null, RepairOrderEntity::getUserId, userId)
            .eq(assignedWorkerId != null, RepairOrderEntity::getAssignedWorkerId, assignedWorkerId)
            .orderByDesc(RepairOrderEntity::getSubmitTime)
            .orderByDesc(RepairOrderEntity::getId);

        Page<RepairOrderEntity> page = repairOrderMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        Map<Long, RepairCategoryEntity> categoryMap = loadCategories(page.getRecords());
        Map<Long, UserEntity> userMap = loadUsers(page.getRecords());

        List<AdminRepairOrderListItemVO> records = page.getRecords().stream()
            .map(order -> toAdminListItem(order, categoryMap.get(order.getCategoryId()), userMap))
            .toList();
        return new PageResult<>(records, page.getTotal(), pageNum, pageSize);
    }

    public AdminRepairOrderDetailVO getAdminOrderDetail(String role, Long orderId) {
        checkAdmin(role);

        RepairOrderEntity order = repairOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "工单不存在");
        }

        RepairCategoryEntity category = categoryMapper.selectById(order.getCategoryId());
        Map<Long, UserEntity> userMap = loadUsers(List.of(order));
        List<RepairRecordVO> records = loadRepairRecords(orderId);
        return toAdminDetail(order, category, userMap, records);
    }

    @Transactional
    public void approveOrder(String role, Long orderId, AdminOrderAuditRequest request) {
        checkAdmin(role);
        RepairOrderEntity order = getOrderForAdminAction(orderId, OrderStatuses.PENDING_AUDIT, "只有待审核工单才可以审核通过");
        order.setStatus(OrderStatuses.PENDING_ASSIGN);
        order.setRejectReason(null);
        order.setAdminRemark(request == null ? null : request.trimAdminRemark());
        repairOrderMapper.updateById(order);
    }

    @Transactional
    public void rejectOrder(String role, Long orderId, AdminOrderAuditRequest request) {
        checkAdmin(role);
        RepairOrderEntity order = getOrderForAdminAction(orderId, OrderStatuses.PENDING_AUDIT, "只有待审核工单才可以驳回");
        String rejectReason;
        try {
            rejectReason = request.requireRejectReason();
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "驳回原因不能为空");
        }
        order.setStatus(OrderStatuses.REJECTED);
        order.setAssignedWorkerId(null);
        order.setAssignTime(null);
        order.setRejectReason(rejectReason);
        order.setAdminRemark(null);
        repairOrderMapper.updateById(order);
    }

    @Transactional
    public void assignOrder(String role, Long orderId, AdminOrderAssignRequest request) {
        checkAdmin(role);
        RepairOrderEntity order = getOrderForAdminAction(orderId, OrderStatuses.PENDING_ASSIGN, "只有待分派工单才可以分派维修人员");
        UserEntity worker = userMapper.selectById(request.workerId());
        if (worker == null || !UserRoles.WORKER.equals(worker.getRole()) || worker.getStatus() == null || worker.getStatus() != 1) {
            throw new BusinessException(ResultCode.CONFLICT, "维修人员不存在或不可用");
        }

        order.setAssignedWorkerId(worker.getId());
        order.setAssignTime(LocalDateTime.now());
        order.setStatus(OrderStatuses.PENDING_ACCEPT);
        order.setRejectReason(null);
        order.setDispatchRemark(request.trimDispatchRemark());
        repairOrderMapper.updateById(order);
    }

    @Transactional
    public void acceptOrder(Long loginUserId, Long orderId, WorkerAcceptRequest request) {
        RepairOrderEntity order = getOrderForWorkerAction(
            loginUserId, orderId, OrderStatuses.PENDING_ACCEPT, "只有待接单工单才可以接单");
        order.setStatus(OrderStatuses.PROCESSING);
        order.setAcceptTime(LocalDateTime.now());
        repairOrderMapper.updateById(order);

        saveRepairRecord(orderId, loginUserId, "ACCEPT",
            request == null ? null : request.trimRemark(), null,
            OrderStatuses.PENDING_ACCEPT, OrderStatuses.PROCESSING);
    }

    @Transactional
    public void rejectByWorker(Long loginUserId, Long orderId, WorkerRejectRequest request) {
        RepairOrderEntity order = getOrderForWorkerAction(
            loginUserId, orderId, OrderStatuses.PENDING_ACCEPT, "只有待接单工单才可以拒单");
        String rejectReason = request.trimRejectReason();
        order.setStatus(OrderStatuses.PENDING_ASSIGN);
        order.setAssignedWorkerId(null);
        order.setAssignTime(null);
        order.setRejectReason(rejectReason);
        repairOrderMapper.updateById(order);

        saveRepairRecord(orderId, loginUserId, "REJECT",
            rejectReason, null,
            OrderStatuses.PENDING_ACCEPT, OrderStatuses.PENDING_ASSIGN);
    }

    @Transactional
    public void finishOrder(Long loginUserId, Long orderId, WorkerFinishRequest request) {
        RepairOrderEntity order = getOrderForWorkerAction(
            loginUserId, orderId, OrderStatuses.PROCESSING, "只有处理中工单才可以完成维修");
        order.setStatus(OrderStatuses.PENDING_CONFIRM);
        order.setFinishTime(LocalDateTime.now());
        repairOrderMapper.updateById(order);

        String actionDesc = request == null ? null : (request.actionDesc() == null ? null : request.actionDesc().trim());
        String resultImage = request == null ? null : (request.resultImage() == null ? null : request.resultImage().trim());
        saveRepairRecord(orderId, loginUserId, "FINISH",
            actionDesc, resultImage,
            OrderStatuses.PROCESSING, OrderStatuses.PENDING_CONFIRM);
    }

    @Transactional
    public void addRepairRecord(Long loginUserId, Long orderId, WorkerRecordRequest request) {
        RepairOrderEntity order = getOrderForWorkerAction(
            loginUserId, orderId, OrderStatuses.PROCESSING, "只有处理中的工单才可以填写维修记录");
        String actionDesc = request.actionDesc() == null ? null : request.actionDesc().trim();
        String resultImage = request.resultImage() == null ? null : request.resultImage().trim();
        saveRepairRecord(orderId, loginUserId, "RECORD",
            actionDesc, resultImage,
            OrderStatuses.PROCESSING, OrderStatuses.PROCESSING);
    }

    private List<RepairRecordVO> loadRepairRecords(Long orderId) {
        List<RepairRecordEntity> records = repairRecordMapper.selectList(
            new LambdaQueryWrapper<RepairRecordEntity>()
                .eq(RepairRecordEntity::getOrderId, orderId)
                .orderByAsc(RepairRecordEntity::getActionTime)
                .orderByAsc(RepairRecordEntity::getId)
        );
        return records.stream()
            .map(r -> new RepairRecordVO(
                r.getId(), r.getOrderId(), r.getWorkerId(),
                r.getActionType(), r.getActionDesc(), r.getResultImage(),
                r.getStatusBefore(), r.getStatusAfter(), r.getActionTime()
            ))
            .toList();
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

    private Map<Long, UserEntity> loadUsers(List<RepairOrderEntity> orders) {
        List<Long> userIds = new ArrayList<>();
        for (RepairOrderEntity order : orders) {
            if (order.getUserId() != null) {
                userIds.add(order.getUserId());
            }
            if (order.getAssignedWorkerId() != null) {
                userIds.add(order.getAssignedWorkerId());
            }
        }
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return userMapper.selectBatchIds(userIds.stream().distinct().toList()).stream()
            .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
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

    private RepairOrderDetailVO toDetail(RepairOrderEntity order, RepairCategoryEntity category, List<RepairRecordVO> records) {
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
            order.getCloseTime(),
            records
        );
    }

    private AdminRepairOrderListItemVO toAdminListItem(
        RepairOrderEntity order,
        RepairCategoryEntity category,
        Map<Long, UserEntity> userMap
    ) {
        UserEntity student = userMap.get(order.getUserId());
        UserEntity worker = userMap.get(order.getAssignedWorkerId());
        return new AdminRepairOrderListItemVO(
            order.getId(),
            order.getOrderNo(),
            order.getUserId(),
            student == null ? null : student.getRealName(),
            order.getTitle(),
            order.getCategoryId(),
            category == null ? null : category.getCategoryName(),
            order.getDormBuilding(),
            order.getDormRoom(),
            order.getStatus(),
            order.getPriority(),
            order.getAssignedWorkerId(),
            worker == null ? null : worker.getRealName(),
            order.getRejectReason(),
            order.getAdminRemark(),
            order.getDispatchRemark(),
            order.getSubmitTime(),
            order.getAssignTime()
        );
    }

    private AdminRepairOrderDetailVO toAdminDetail(
        RepairOrderEntity order,
        RepairCategoryEntity category,
        Map<Long, UserEntity> userMap,
        List<RepairRecordVO> records
    ) {
        UserEntity student = userMap.get(order.getUserId());
        UserEntity worker = userMap.get(order.getAssignedWorkerId());
        return new AdminRepairOrderDetailVO(
            order.getId(),
            order.getOrderNo(),
            order.getUserId(),
            student == null ? null : student.getRealName(),
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
            order.getAssignedWorkerId(),
            worker == null ? null : worker.getRealName(),
            order.getRejectReason(),
            order.getAdminRemark(),
            order.getDispatchRemark(),
            order.getSubmitTime(),
            order.getAssignTime(),
            order.getAcceptTime(),
            order.getFinishTime(),
            order.getCloseTime(),
            records
        );
    }

    private void checkAdmin(String role) {
        if (!UserRoles.ADMIN.equals(role)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅管理员可以执行该操作");
        }
    }

    private RepairOrderEntity getOrderForAdminAction(Long orderId, String expectedStatus, String message) {
        RepairOrderEntity order = repairOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "工单不存在");
        }
        if (!expectedStatus.equals(order.getStatus())) {
            throw new BusinessException(ResultCode.CONFLICT, message);
        }
        return order;
    }

    private RepairOrderEntity getOrderForWorkerAction(Long loginUserId, Long orderId, String expectedStatus, String message) {
        RepairOrderEntity order = repairOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "工单不存在");
        }
        if (!Objects.equals(order.getAssignedWorkerId(), loginUserId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作未分配给自己的工单");
        }
        if (!expectedStatus.equals(order.getStatus())) {
            throw new BusinessException(ResultCode.CONFLICT, message);
        }
        return order;
    }

    private void saveRepairRecord(Long orderId, Long workerId, String actionType,
                                   String actionDesc, String resultImage,
                                   String statusBefore, String statusAfter) {
        RepairRecordEntity record = new RepairRecordEntity();
        record.setOrderId(orderId);
        record.setWorkerId(workerId);
        record.setActionType(actionType);
        record.setActionDesc(actionDesc);
        record.setResultImage(resultImage);
        record.setStatusBefore(statusBefore);
        record.setStatusAfter(statusAfter);
        record.setActionTime(LocalDateTime.now());
        record.setCreateTime(LocalDateTime.now());
        repairRecordMapper.insert(record);
    }

    private String generateOrderNo() {
        return "WO" + LocalDateTime.now().format(ORDER_NO_FORMATTER)
            + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
