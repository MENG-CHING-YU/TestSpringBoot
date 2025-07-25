package com.machine.controller;

import com.machine.Bean.MachinesBean;
import com.machine.Service.machine.MachinesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/machine")
public class MachineController {

    @Autowired
    private MachinesService machinesService;

    // 後台管理頁面 - 替換 BackstageServlet
    @GetMapping("/backstage")
    public String backstage(@RequestParam(required = false) String search,
            @RequestParam(required = false) String statusFilter,
            Model model) {
        try {
            List<MachinesBean> machinesList = machinesService.findAllMachines();

            // 搜尋篩選
            if (search != null && !search.trim().isEmpty()) {
                String keyword = search.trim().toLowerCase();
                machinesList = machinesList.stream()
                        .filter(m -> m.getMachineName().toLowerCase().contains(keyword) ||
                                m.getSerialNumber().toLowerCase().contains(keyword) ||
                                String.valueOf(m.getMachineId()).contains(keyword))
                        .collect(Collectors.toList());
            }

            if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                machinesList = machinesList.stream()
                        .filter(m -> m.getMstatus().equals(statusFilter))
                        .collect(Collectors.toList());
            }

            model.addAttribute("machines", machinesList);
            return "machine/backstageMachine";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "載入機台資料時發生錯誤：" + e.getMessage());
            return "machine/backstageMachine";
        }
    }

    // 前台查詢系統 - 替換 FrontendServlet
    @GetMapping("/frontend")
    public String frontend(@RequestParam(required = false) String search,
            @RequestParam(required = false) String statusFilter,
            Model model) {
        List<MachinesBean> machinesList = machinesService.findAllMachines();

        if (search != null && !search.trim().isEmpty()) {
            String keyword = search.trim().toLowerCase();
            machinesList = machinesList.stream()
                    .filter(m -> m.getMachineName().toLowerCase().contains(keyword) ||
                            m.getSerialNumber().toLowerCase().contains(keyword))
                    .collect(Collectors.toList());
        }

        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            machinesList = machinesList.stream()
                    .filter(m -> m.getMstatus().equals(statusFilter))
                    .collect(Collectors.toList());
        }

        model.addAttribute("machines", machinesList);
        return "machine/frontend";
    }

    // 新增機台頁面 - 替換 GoInsertMachineServlet
    @GetMapping("/insert")
    public String showInsertForm() {
        return "machine/insertMachine";
    }

    // 處理新增機台 - 替換 InsertMachineServlet
    @PostMapping("/insert")
    public String insertMachine(@RequestParam String machineName,
            @RequestParam String serialNumber,
            @RequestParam String mstatus,
            @RequestParam(required = false) String machineLocation,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            MachinesBean machine = new MachinesBean(machineName, serialNumber, mstatus, machineLocation);
            machinesService.insertMachine(machine);
            redirectAttributes.addAttribute("success", "insert");
            return "redirect:/machine/backstage";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "machine/insertMachine";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "系統錯誤，請稍後再試");
            return "machine/insertMachine";
        }
    }

    // 編輯機台頁面 - 替換 GoUpdateMachineServlet
    @GetMapping("/update/{id}")
    public String showUpdateForm(@PathVariable int id, Model model) {
        try {
            MachinesBean machine = machinesService.findMachineById(id);
            if (machine == null) {
                return "redirect:/machine/backstage";
            }
            model.addAttribute("machine", machine);
            return "machine/updateMachine";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/machine/backstage";
        }
    }

    // 處理更新機台 - 替換 UpdateMachineServlet
    @PostMapping("/update")
    public String updateMachine(@RequestParam int machineId,
            @RequestParam String machineName,
            @RequestParam String serialNumber,
            @RequestParam String mstatus,
            @RequestParam(required = false) String machineLocation,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            MachinesBean machine = new MachinesBean(machineId, machineName, serialNumber, mstatus, machineLocation);
            machinesService.updateMachine(machine);
            redirectAttributes.addAttribute("success", "update");
            return "redirect:/machine/backstage";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "系統錯誤，請稍後再試");
            return "error";
        }
    }

    // 刪除機台 - 替換 ConfirmDeleteMachineServlet
    @GetMapping("/delete/{id}")
    public String deleteMachine(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            machinesService.deleteMachine(id);
            redirectAttributes.addAttribute("success", "delete");
            return "redirect:/machine/backstage";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/machine/backstage";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/machine/backstage";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "系統錯誤，請稍後再試");
            return "redirect:/machine/backstage";
        }
    }

    // 機台詳細資料 - 替換 FindMachineByIdServlet
    @GetMapping("/detail/{id}")
    public String findMachineById(@PathVariable int id, Model model) {
        try {
            MachinesBean machine = machinesService.findMachineById(id);
            if (machine == null) {
                model.addAttribute("error", "找不到該機台資料");
                return "error";
            }
            model.addAttribute("machine", machine);
            return "machine/findMachineById";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "系統錯誤，請稍後再試");
            return "error";
        }
    }
}