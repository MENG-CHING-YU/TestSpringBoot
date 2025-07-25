package com.machine.controller;

import com.machine.Bean.MachineMaintenanceBean;
import com.machine.Service.maintenance.MachineMaintenanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/maintenance")
public class MaintenanceController {

    @Autowired
    private MachineMaintenanceService maintenanceService;

    // 管理員保養列表 - 替換 AdminMaintenanceServlet
    @GetMapping("/admin/list")
    public String adminMaintenanceList(Model model) {
        try {
            List<MachineMaintenanceBean> maintenanceList = maintenanceService.findAllMaintenances();
            model.addAttribute("maintenanceList", maintenanceList);
            return "maintenance/adminDashboard";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "載入保養資料失敗：" + e.getMessage());
            return "maintenance/adminDashboard";
        }
    }

    // 保養詳細資料 - 替換 AdminMaintenanceDetailServlet
    @GetMapping("/admin/detail/{id}")
    public String adminMaintenanceDetail(@PathVariable int id, Model model) {
        try {
            MachineMaintenanceBean maintenance = maintenanceService.findMaintenanceDetailById(id);
            if (maintenance == null) {
                model.addAttribute("error", "查無該筆保養資料");
                return "error";
            }
            model.addAttribute("maintenance", maintenance);
            return "maintenance/adminMaintenanceDetail";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "系統錯誤：" + e.getMessage());
            return "error";
        }
    }

    // 新增保養頁面 - 替換 AdminInsertMaintenanceServlet
    @GetMapping("/admin/insert")
    public String showInsertForm() {
        return "maintenance/maintenanceInsert";
    }

    // 處理新增保養 - 替換 InsertMaintenance
    @PostMapping("/insert")
    public String insertMaintenance(@RequestParam String machineId,
            @RequestParam String maintenanceDescription,
            @RequestParam String employeeId,
            Model model) {
        try {
            // 驗證必填欄位
            if (machineId == null || machineId.trim().isEmpty() ||
                    maintenanceDescription == null || maintenanceDescription.trim().isEmpty() ||
                    employeeId == null || employeeId.trim().isEmpty()) {
                model.addAttribute("errorMessage", "必填欄位不可為空白");
                return "maintenance/maintenanceInsert";
            }

            int machineIdInt = Integer.parseInt(machineId.trim());
            int employeeIdInt = Integer.parseInt(employeeId.trim());
            String maintenanceStatus = "待處理"; // 預設狀態
            LocalDateTime now = LocalDateTime.now();

            MachineMaintenanceBean maintenance = new MachineMaintenanceBean(
                    machineIdInt, now, maintenanceDescription, maintenanceStatus, employeeIdInt);

            maintenanceService.insertMaintenance(maintenance);
            return "maintenance/maintenanceSucceed";

        } catch (NumberFormatException e) {
            model.addAttribute("errorMessage", "數字欄位格式錯誤");
            return "maintenance/maintenanceInsert";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "資料插入失敗：" + e.getMessage());
            return "maintenance/maintenanceInsert";
        }
    }

    // 編輯保養頁面 - 替換 AdminUpdateMaintenanceServlet
    @GetMapping("/admin/update/{id}")
    public String showUpdateForm(@PathVariable int id, Model model) {
        try {
            MachineMaintenanceBean maintenance = maintenanceService.findMaintenanceDetailById(id);
            if (maintenance == null) {
                model.addAttribute("error", "查無該筆保養資料");
                return "error";
            }
            model.addAttribute("maintenance", maintenance);
            return "maintenance/UpdateMaintenance";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "讀取資料失敗: " + e.getMessage());
            return "error";
        }
    }

    // 處理更新保養 - 替換 UpdateMaintenanceServlet
    @PostMapping("/update")
    public String updateMaintenance(@RequestParam int scheduleId,
            @RequestParam int machineId,
            @RequestParam String maintenanceDescription,
            @RequestParam String maintenanceStatus,
            @RequestParam int employeeId,
            Model model) {
        try {
            MachineMaintenanceBean maintenance = new MachineMaintenanceBean(
                    scheduleId, machineId, maintenanceDescription, maintenanceStatus, employeeId);

            maintenanceService.updateMaintenance(maintenance);
            model.addAttribute("maintenance", maintenance);
            return "maintenance/maintenanceUpdateSucceed";

        } catch (NumberFormatException e) {
            model.addAttribute("error", "資料格式錯誤");
            return "error";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "更新失敗：" + e.getMessage());
            return "error";
        }
    }

    // 刪除確認頁面 - 替換 DeleteMaintenanceConfirmServlet
    @GetMapping("/delete/confirm/{id}")
    public String deleteConfirm(@PathVariable int id, Model model) {
        try {
            MachineMaintenanceBean maintenance = maintenanceService.findMaintenanceById(id);
            if (maintenance == null) {
                model.addAttribute("error", "找不到該保養資料");
                return "error";
            }
            model.addAttribute("maintenance", maintenance);
            return "maintenance/deleteConfirm";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "伺服器錯誤");
            return "error";
        }
    }

    // 刪除保養 - 替換 DeleteMaintenanceServlet
    @PostMapping("/delete")
    public String deleteMaintenance(@RequestParam int scheduleId,
            RedirectAttributes redirectAttributes) {
        try {
            maintenanceService.deleteMaintenance(scheduleId);
            redirectAttributes.addFlashAttribute("successMessage", "刪除成功");
            return "redirect:/maintenance/admin/list";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "刪除失敗：" + e.getMessage());
            return "redirect:/maintenance/admin/list";
        }
    }

    // 保養列表查詢 - 替換 FindAllMachineMaintenance
    @GetMapping("/list")
    public String findAllMaintenance(Model model) {
        try {
            List<MachineMaintenanceBean> maintenanceList = maintenanceService.findAllMaintenances();
            model.addAttribute("maintenanceList", maintenanceList);
            return "maintenance/maintenanceList";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "載入保養資料失敗：" + e.getMessage());
            return "maintenance/maintenanceList";
        }
    }

    // 保養詳細資料 - 替換 MaintenanceDetailServlet
    @GetMapping("/detail/{id}")
    public String maintenanceDetail(@PathVariable int id, Model model) {
        try {
            MachineMaintenanceBean maintenance = maintenanceService.findMaintenanceDetailById(id);
            if (maintenance == null) {
                model.addAttribute("error", "查無該筆保養資料");
                return "error";
            }
            model.addAttribute("maintenance", maintenance);
            return "maintenance/maintenanceDetail";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "系統錯誤：" + e.getMessage());
            return "error";
        }
    }
}