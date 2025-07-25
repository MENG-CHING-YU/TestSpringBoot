package com.machine.Service.repair;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.machine.Bean.MachineRepairBean;
import com.machine.Repository.MachineRepairRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MachineRepairService {

    @Autowired
    private MachineRepairRepository repairRepository;

    // 新增維修記錄
    public boolean insertRepair(MachineRepairBean repair) {
        try {
            validateRepair(repair);
            
            if (repair.getRepairTime() == null) {
                repair.setRepairTime(LocalDateTime.now());
            }
            
            repairRepository.save(repair);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 查詢所有維修記錄
    public List<MachineRepairBean> machineRepairView() {
        return repairRepository.findAllWithMachineInfo();
    }

    // 根據ID查詢維修記錄
    public MachineRepairBean findRepairById(int repairId) {
        Optional<MachineRepairBean> repair = repairRepository.findByIdWithMachineInfo(repairId);
        return repair.orElse(null);
    }

    // 更新維修狀態
    public boolean updateRepairStatus(int repairId, String newStatus) {
        try {
            if (newStatus == null || newStatus.trim().isEmpty()) {
                throw new IllegalArgumentException("維修狀態不可為空");
            }
            
            int updatedRows = repairRepository.updateRepairStatus(repairId, newStatus);
            return updatedRows > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 根據狀態查詢維修記錄
    public List<MachineRepairBean> getRepairsByStatus(String status) {
        return repairRepository.findByRepairStatusWithMachineInfo(status);
    }

    // 管理員查詢所有維修記錄
    public List<MachineRepairBean> getAllRepairsForAdmin() {
        return machineRepairView();
    }

    // 根據機台ID查詢維修記錄
    public List<MachineRepairBean> findRepairsByMachineId(int machineId) {
        return repairRepository.findByMachineIdWithMachineInfo(machineId);
    }

    // 根據狀態查詢維修記錄 (別名方法)
    public List<MachineRepairBean> findRepairsByStatus(String status) {
        return getRepairsByStatus(status);
    }

    // 根據員工ID查詢維修記錄
    public List<MachineRepairBean> findRepairsByEmployeeId(int employeeId) {
        return repairRepository.findByEmployeeIdWithMachineInfo(employeeId);
    }

    // 多條件搜尋維修記錄
    public List<MachineRepairBean> findRepairsByConditions(Integer machineId, Integer employeeId, String status) {
        return repairRepository.findByConditions(machineId, employeeId, status);
    }

    // 更新完整維修記錄
    public boolean updateRepair(MachineRepairBean repair) {
        try {
            validateRepair(repair);
            
            if (!repairRepository.existsById(repair.getRepairId())) {
                return false;
            }
            
            repairRepository.save(repair);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 刪除維修記錄
    public boolean deleteRepair(int repairId) {
        try {
            if (!repairRepository.existsById(repairId)) {
                return false;
            }
            
            repairRepository.deleteById(repairId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 驗證維修記錄資料
    private void validateRepair(MachineRepairBean repair) {
        if (repair.getMachineId() <= 0) {
            throw new IllegalArgumentException("機台ID必須為正整數");
        }
        if (repair.getRepairDescription() == null || repair.getRepairDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("維修描述不可為空");
        }
        if (repair.getRepairStatus() == null || repair.getRepairStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("維修狀態不可為空");
        }
        // employeeId 可以為 null，所以不驗證
    }
}