package com.machine.controller;

import com.machine.Bean.MachineRepairBean;
import com.machine.Service.repair.MachineRepairService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/repair")
public class RepairController {

    @Autowired
    private MachineRepairService repairService;

    // 報修表單頁面 - 替換 RepairFormServlet
    @GetMapping("/form")
    public String showRepairForm() {
        return "repair/repairForm";
    }

    // 提交報修 - 替換 InsertRepairServlet
    @PostMapping("/insert")
    public String insertRepair(@RequestParam String machineId,
            @RequestParam String repairDescription,
            @RequestParam String employeeId,
            Model model) {
        try {
            if (machineId == null || machineId.trim().isEmpty() ||
                    repairDescription == null || repairDescription.trim().isEmpty() ||
                    employeeId == null || employeeId.trim().isEmpty()) {
                model.addAttribute("errorMessage", "必填欄位不可為空白");
                return "repair/repairForm";
            }

            int machineIdInt = Integer.parseInt(machineId.trim());
            int employeeIdInt = Integer.parseInt(employeeId.trim());
            String repairStatus = "待處理";
            LocalDateTime now = LocalDateTime.now();

            MachineRepairBean repair = new MachineRepairBean(machineIdInt, repairDescription,
                    now, repairStatus, employeeIdInt);
            repairService.insertRepair(repair);
            return "repair/repairSuccess";

        } catch (NumberFormatException e) {
            model.addAttribute("errorMessage", "數字欄位格式錯誤");
            return "repair/repairForm";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "資料插入失敗：" + e.getMessage());
            return "repair/repairForm";
        }
    }

    // 維修記錄列表 - 替換 MachineRepairViewServlet
    @GetMapping("/list")
    public String viewRepairList(Model model) {
        try {
            List<MachineRepairBean> repairList = repairService.machineRepairView();
            model.addAttribute("repairList", repairList);
            return "repair/repairList";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "伺服器錯誤：" + e.getMessage());
            return "repair/repairList";
        }
    }

    // 維修詳細資料 - 替換 RepairDetailServlet
    @GetMapping("/detail/{id}")
    public String repairDetail(@PathVariable int id, Model model) {
        try {
            MachineRepairBean repair = repairService.findRepairById(id);
            if (repair == null) {
                model.addAttribute("error", "找不到報修資料");
                return "error";
            }
            model.addAttribute("repair", repair);
            return "repair/repairDetail";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "系統錯誤：" + e.getMessage());
            return "error";
        }
    }

    // 管理員維修列表 - 替換 AdminRepairListServlet
    @GetMapping("/admin/list")
    public String adminRepairList(Model model) {
        try {
            List<MachineRepairBean> repairList = repairService.getAllRepairsForAdmin();
            model.addAttribute("repairList", repairList);
            return "repair/adminRepairList";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "載入資料失敗：" + e.getMessage());
            return "repair/adminRepairList";
        }
    }

    // 按機台ID搜尋 - 替換 SearchRepairByMachineServlet
    @GetMapping("/search/machine")
    public String searchByMachine(@RequestParam String machineId, Model model) {
        try {
            if (machineId == null || machineId.trim().isEmpty()) {
                model.addAttribute("errorMessage", "請輸入機台ID");
                return "repair/adminRepairList";
            }

            int machineIdInt = Integer.parseInt(machineId.trim());
            List<MachineRepairBean> repairList = repairService.findRepairsByMachineId(machineIdInt);

            model.addAttribute("repairList", repairList);
            model.addAttribute("searchType", "按機台查詢");
            model.addAttribute("searchValue", "機台ID: " + machineIdInt);
            return "repair/adminRepairList";

        } catch (NumberFormatException e) {
            model.addAttribute("errorMessage", "機台ID必須為數字");
            return "repair/adminRepairList";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "查詢失敗：" + e.getMessage());
            return "repair/adminRepairList";
        }
    }

    // 按狀態搜尋 - 替換 SearchRepairByStatusServlet
    @GetMapping("/search/status")
    public String searchByStatus(@RequestParam String status, Model model) {
        try {
            if (status == null || status.trim().isEmpty()) {
                model.addAttribute("errorMessage", "請選擇狀態");
                return "repair/adminRepairList";
            }

            List<MachineRepairBean> repairList = repairService.findRepairsByStatus(status);
            model.addAttribute("repairList", repairList);
            model.addAttribute("searchType", "按狀態篩選");
            model.addAttribute("searchValue", "狀態: " + status);
            return "repair/adminRepairList";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "查詢失敗：" + e.getMessage());
            return "repair/adminRepairList";
        }
    }

    // 更新維修狀態 - 替換 UpdateRepairStatusServlet
    @PostMapping("/update/status")
    public String updateRepairStatus(@RequestParam String repairId,
            @RequestParam String newStatus,
            RedirectAttributes redirectAttributes) {
        try {
            if (repairId == null || repairId.trim().isEmpty() ||
                    newStatus == null || newStatus.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "必填欄位不可為空白");
                return "redirect:/repair/admin/list";
            }

            int repairIdInt = Integer.parseInt(repairId);
            repairService.updateRepairStatus(repairIdInt, newStatus);
            redirectAttributes.addFlashAttribute("successMessage", "狀態更新成功");
            return "redirect:/repair/admin/list";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "更新失敗");
            return "redirect:/repair/admin/list";
        }
    }
}