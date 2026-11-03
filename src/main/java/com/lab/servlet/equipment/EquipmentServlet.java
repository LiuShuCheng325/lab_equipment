package com.lab.servlet.equipment;

import com.lab.entity.BorrowRecord;
import com.lab.entity.Equipment;
import com.lab.entity.User;
import com.lab.exception.BusinessException;
import com.lab.service.BorrowService;
import com.lab.service.EquipmentService;
import com.lab.service.impl.BorrowServiceImpl;
import com.lab.service.impl.EquipmentServiceImpl;
import com.lab.util.Pagination;
import com.lab.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 设备控制器（/equipment）。
 *
 * <p>list 支持 keyword / eqType / status / pageNum；delete 仅接受 POST，
 * 存在未结束借用记录时拒绝删除并给出提示。
 */
@MultipartConfig(maxFileSize = 5 * 1024 * 1024)
@WebServlet("/equipment")
public class EquipmentServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(EquipmentServlet.class.getName());

    private final EquipmentService equipmentService = new EquipmentServiceImpl();
    private final BorrowService borrowService = new BorrowServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        WebUtil.consumeFlash(request);

        String action = WebUtil.str(request, "action", "list");
        String keyword = WebUtil.str(request, "keyword", "");
        String eqType = WebUtil.str(request, "eqType", "");
        Integer status = WebUtil.intOrNull(request, "status");
        int pageNum = WebUtil.intParam(request, "pageNum", 1);
        int pageSize = WebUtil.intParam(request, "pageSize", Pagination.DEFAULT_PAGE_SIZE);

        try {
            switch (action) {
                case "list":
                    Pagination<Equipment> page =
                            equipmentService.findPage(keyword, eqType, status, pageNum, pageSize);
                    Map<Integer, Integer> borrowingMap = equipmentService.countBorrowingByEquipment();
                    for (Equipment equipment : page.getData()) {
                        Integer borrowed = borrowingMap.get(equipment.getId());
                        equipment.setBorrowCount(borrowed == null ? 0 : borrowed);
                    }
                    request.setAttribute("equipmentList", page.getData());
                    request.setAttribute("pageNum", page.getPageNum());
                    request.setAttribute("pageTotal", page.getTotalPages());
                    request.setAttribute("pageSize", page.getPageSize());
                    request.setAttribute("total", page.getTotal());
                    request.setAttribute("keyword", keyword);
                    request.setAttribute("eqType", eqType);
                    request.setAttribute("status", status);
                    request.setAttribute("categoryList", equipmentService.findCategories());
                    request.setAttribute("borrowingMap", borrowingMap);
                    request.getRequestDispatcher("/equipment/list.jsp").forward(request, response);
                    break;
                case "detail":
                    Integer detailId = WebUtil.intOrNull(request, "id");
                    if (detailId == null) {
                        throw new BusinessException("缺少设备ID");
                    }
                    request.setAttribute("equipment", equipmentService.findById(detailId));
                    // 最近 5 条借用记录（新增变量，不影响已有页面）
                    request.setAttribute("recentList", recentBorrows(detailId));
                    request.getRequestDispatcher("/equipment/detail.jsp").forward(request, response);
                    break;
                case "add":
                    requireAdmin(user);
                    request.getRequestDispatcher("/equipment/add.jsp").forward(request, response);
                    break;
                case "edit":
                    requireAdmin(user);
                    Integer editId = WebUtil.intOrNull(request, "id");
                    if (editId == null) {
                        throw new BusinessException("缺少设备ID");
                    }
                    request.setAttribute("equipment", equipmentService.findById(editId));
                    request.getRequestDispatcher("/equipment/edit.jsp").forward(request, response);
                    break;
                case "delete":
                    // 删除是危险写操作，GET 只给提示，不执行删除
                    WebUtil.flashErr(request.getSession(), "删除设备已升级为 POST 提交，请使用列表页中的删除按钮");
                    response.sendRedirect(request.getContextPath() + "/equipment?action=list");
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/equipment?action=list");
                    break;
            }
        } catch (BusinessException e) {
            WebUtil.flashErr(request.getSession(), e.getMessage());
            response.sendRedirect(request.getContextPath() + "/equipment?action=list");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "设备模块请求处理失败，action=" + action, e);
            WebUtil.flashErr(request.getSession(), "操作失败，请稍后重试");
            response.sendRedirect(request.getContextPath() + "/equipment?action=list");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String action = WebUtil.str(request, "action", "");
        try {
            if ("delete".equals(action)) {
                Integer id = WebUtil.intOrNull(request, "id");
                if (id == null) {
                    throw new BusinessException("缺少设备ID");
                }
                requireAdmin(user);
                try {
                    equipmentService.delete(id);
                    WebUtil.flashMsg(request.getSession(), "设备已删除");
                } catch (BusinessException e) {
                    WebUtil.flashErr(request.getSession(), e.getMessage());
                }
                response.sendRedirect(request.getContextPath() + "/equipment?action=list");
                return;
            }

            // add / edit 仅管理员
            requireAdmin(user);
            if ("add".equals(action)) {
                buildFromRequest(request, null);
            } else if ("edit".equals(action)) {
                Integer id = WebUtil.intOrNull(request, "id");
                if (id == null) {
                    throw new BusinessException("缺少设备ID");
                }
                buildFromRequest(request, id);
            } else {
                throw new BusinessException("未知的操作类型：" + action);
            }
            response.sendRedirect(request.getContextPath() + "/equipment?action=list");
        } catch (BusinessException e) {
            WebUtil.flashErr(request.getSession(), e.getMessage());
            response.sendRedirect(request.getContextPath() + "/equipment?action=list");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "设备模块写操作失败，action=" + action, e);
            WebUtil.flashErr(request.getSession(), "操作失败，请稍后重试");
            response.sendRedirect(request.getContextPath() + "/equipment?action=list");
        }
    }

    /**
     * 从请求参数组装设备实体并执行新增/更新，校验失败抛出业务异常。
     *
     * @param request 请求
     * @param id      设备ID，null 表示新增
     */
    private void buildFromRequest(HttpServletRequest request, Integer id) {
        Equipment equipment = new Equipment();
        equipment.setId(id);
        equipment.setEqName(WebUtil.str(request, "eqName", ""));
        equipment.setModel(WebUtil.str(request, "model", ""));
        equipment.setEqType(WebUtil.str(request, "eqType", ""));
        equipment.setLocation(WebUtil.str(request, "location", ""));
        equipment.setRemark(WebUtil.limit(request.getParameter("description"), 500));
        equipment.setImg(WebUtil.limit(request.getParameter("img"), 255));

        int stock = WebUtil.intParam(request, "stock", -1);
        Integer available = WebUtil.intOrNull(request, "available");
        Integer statusParam = WebUtil.intOrNull(request, "status");

        if (stock < 0) {
            throw new BusinessException("总库存必须是不小于 0 的整数");
        }
        equipment.setStock(stock);
        // 新增时可用数量默认等于总库存；编辑时以表单为准
        if (id == null) {
            equipment.setAvailable(stock);
        } else {
            if (available == null) {
                throw new BusinessException("可用数量必须填写合法数字");
            }
            if (available < 0 || available > stock) {
                throw new BusinessException("可用数量不合法：不能为负数，且不能超过总库存 " + stock);
            }
            equipment.setAvailable(available);
        }
        equipment.setStatus(statusParam == null ? Equipment.STATUS_NORMAL : statusParam);

        boolean ok = (id == null) ? equipmentService.add(equipment) : equipmentService.update(equipment);
        if (!ok) {
            throw new BusinessException("保存设备失败，请检查填写内容");
        }
        WebUtil.flashMsg(request.getSession(), id == null ? "设备新增成功" : "设备修改成功");
    }

    /**
     * 管理员校验（内部使用）。
     *
     * @param user 当前登录用户
     */
    private void requireAdmin(User user) {
        if (user == null || user.getRole() == null || user.getRole() != 1) {
            throw new BusinessException("无权限执行该操作");
        }
    }

    /**
     * 取某台设备最近 5 条借用记录（详情页小表使用）。
     *
     * @param equipmentId 设备ID
     * @return 最近的借用记录列表
     */
    private List<BorrowRecord> recentBorrows(Integer equipmentId) {
        List<BorrowRecord> result = new ArrayList<>();
        List<BorrowRecord> all = borrowService.findAll();
        if (all == null) {
            return result;
        }
        for (BorrowRecord record : all) {
            if (record.getEquipmentId() != null && record.getEquipmentId().equals(equipmentId)) {
                result.add(record);
                if (result.size() >= 5) {
                    break;
                }
            }
        }
        return result;
    }
}
