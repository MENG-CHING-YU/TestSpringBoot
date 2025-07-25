package com.machine.controller;

import com.machine.Bean.MachineFilesBean;
import com.machine.Service.files.MachineFilesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/files")
public class FilesController {

    @Autowired
    private MachineFilesService machineFilesService;

    // 檔案管理主頁 - 替換 FileManagementServlet
    @GetMapping("/management")
    public String fileManagement(@RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer machineId,
            Model model) {
        try {
            List<MachineFilesBean> files;

            // 搜尋邏輯
            if ((keyword != null && !keyword.trim().isEmpty()) || machineId != null) {
                if (machineId != null) {
                    files = machineFilesService.getFilesByMachineId(machineId);
                } else {
                    files = machineFilesService.searchFiles(keyword);
                }
                model.addAttribute("searchMode", true);
                model.addAttribute("keyword", keyword);
                model.addAttribute("selectedMachineId", machineId);
            } else {
                files = machineFilesService.getFilesWithMachineInfo();
            }

            model.addAttribute("files", files);
            model.addAttribute("totalFiles", files.size());
            return "files/fileManagement";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "載入檔案列表失敗：" + e.getMessage());
            return "files/fileManagement";
        }
    }

    // 檔案列表 - 替換 FindFilesServlet
    @GetMapping("/list")
    public String fileList(Model model) {
        try {
            List<MachineFilesBean> fileList = machineFilesService.getFilesWithMachineInfo();
            model.addAttribute("fileList", fileList);
            return "files/fileList";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "載入檔案列表失敗：" + e.getMessage());
            return "files/fileList";
        }
    }

    // 新增檔案頁面 - 替換 InsertFilesServlet GET
    @GetMapping("/insert")
    public String showInsertForm() {
        return "files/fileInsert";
    }

    // 處理檔案上傳 - 替換 InsertFilesServlet POST
    @PostMapping("/insert")
    public String insertFile(@RequestParam String fileName,
            @RequestParam int machineId,
            @RequestParam("fileUpload") MultipartFile file,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            // 驗證必填欄位
            if (fileName == null || fileName.trim().isEmpty()) {
                model.addAttribute("errorMessage", "檔案名稱不可為空");
                return "files/fileInsert";
            }

            if (machineId <= 0) {
                model.addAttribute("errorMessage", "請輸入有效的機台ID");
                return "files/fileInsert";
            }

            if (file.isEmpty()) {
                model.addAttribute("errorMessage", "請選擇要上傳的檔案");
                return "files/fileInsert";
            }

            // 生成檔案路徑
            String uploadDir = "uploads/machine-files/machine-" + machineId + "/";
            String originalFileName = file.getOriginalFilename();
            String timestamp = String.valueOf(System.currentTimeMillis());
            String savedFileName = timestamp + "_" + originalFileName;
            String filePath = uploadDir + savedFileName;

            // 確保目錄存在
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 儲存檔案
            File savedFile = new File(filePath);
            file.transferTo(savedFile);

            // 建立資料庫記錄
            MachineFilesBean fileBean = new MachineFilesBean();
            fileBean.setFileName(fileName);
            fileBean.setFilePath(filePath);
            fileBean.setMachineId(machineId);
            fileBean.setUploadTime(LocalDateTime.now());

            boolean success = machineFilesService.addFile(fileBean);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "檔案上傳成功！");
                return "redirect:/files/management";
            } else {
                model.addAttribute("errorMessage", "檔案資料儲存失敗");
                return "files/fileInsert";
            }

        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "檔案上傳失敗：" + e.getMessage());
            return "files/fileInsert";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "系統錯誤：" + e.getMessage());
            return "files/fileInsert";
        }
    }

    // 編輯檔案頁面 - 替換 UpdateFilesServlet GET
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        try {
            MachineFilesBean file = machineFilesService.getFileById(id);
            if (file == null) {
                model.addAttribute("errorMessage", "找不到指定的檔案");
                return "redirect:/files/management";
            }
            model.addAttribute("file", file);
            return "files/fileUpdate";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/files/management";
        }
    }

    // 處理檔案更新 - 替換 UpdateFilesServlet POST
    @PostMapping("/update")
    public String updateFile(@RequestParam int fileId,
            @RequestParam String fileName,
            @RequestParam int machineId,
            @RequestParam(required = false) boolean keepOriginalPath,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            MachineFilesBean originalFile = machineFilesService.getFileById(fileId);
            if (originalFile == null) {
                model.addAttribute("errorMessage", "找不到要更新的檔案");
                return "redirect:/files/management";
            }

            String filePath;
            if (keepOriginalPath) {
                filePath = originalFile.getFilePath();
            } else {
                // 生成新的檔案路徑
                String uploadDir = "uploads/machine-files/machine-" + machineId + "/";
                String timestamp = String.valueOf(System.currentTimeMillis());
                filePath = uploadDir + timestamp + "_" + fileName;
            }

            boolean success = machineFilesService.updateFile(fileId, fileName, filePath, machineId);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "檔案資料更新成功！");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "檔案資料更新失敗");
            }

            return "redirect:/files/management";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "更新失敗：" + e.getMessage());
            return "files/fileUpdate";
        }
    }

    // 刪除確認頁面 - 替換 DeleteFilesConfirmServlet
    @GetMapping("/delete/confirm/{id}")
    public String deleteConfirm(@PathVariable int id, Model model) {
        try {
            MachineFilesBean file = machineFilesService.getFileById(id);
            if (file == null) {
                model.addAttribute("errorMessage", "找不到要刪除的檔案");
                return "redirect:/files/management";
            }
            model.addAttribute("file", file);
            return "files/fileDeleteConfirm";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/files/management";
        }
    }

    // 處理檔案刪除 - 替換 DeleteFilesServlet
    @PostMapping("/delete")
    public String deleteFile(@RequestParam int fileId,
            @RequestParam(required = false) boolean confirmDelete,
            RedirectAttributes redirectAttributes) {
        try {
            if (!confirmDelete) {
                redirectAttributes.addFlashAttribute("errorMessage", "刪除操作被取消");
                return "redirect:/files/management";
            }

            boolean success = machineFilesService.deleteFile(fileId);

            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "檔案刪除成功！");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "檔案刪除失敗");
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "刪除失敗：" + e.getMessage());
        }

        return "redirect:/files/management";
    }

    // 後台管理主頁
    @GetMapping("/admin")
    public String adminMain() {
        return "files/adminMain";
    }
}