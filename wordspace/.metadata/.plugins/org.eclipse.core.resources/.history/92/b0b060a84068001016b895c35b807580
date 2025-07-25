package com.machine.Service.machine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.machine.Bean.MachinesBean;
import com.machine.Repository.MachinesRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class MachinesService {

    @Autowired
    private MachinesRepository machinesRepository;

    // 查詢所有機台
    public List<MachinesBean> findAllMachines() {
        return machinesRepository.findAll();
    }

    // 依機台ID取得單筆資料
    public MachinesBean findMachineById(int machineId) {
        return machinesRepository.findById(machineId).orElse(null);
    }

    // 新增機台
    public void insertMachine(MachinesBean machine) {
        validateMachine(machine);
        
        // 檢查出廠編號是否已存在
        Optional<MachinesBean> existing = machinesRepository.findBySerialNumber(machine.getSerialNumber());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("出廠編號已存在，請勿重複新增");
        }

        machinesRepository.save(machine);
    }

    // 刪除機台
    public void deleteMachine(int machineId) {
        MachinesBean machine = machinesRepository.findById(machineId)
                .orElseThrow(() -> new IllegalArgumentException("找不到該機台資料，無法刪除"));

        // 檢查狀態，避免刪除運行中的機台
        if ("運行中".equals(machine.getMstatus())) {
            throw new IllegalStateException("運行中的機台不可刪除，請先停止機台");
        }

        machinesRepository.deleteById(machineId);
    }

    // 更新機台
    public void updateMachine(MachinesBean machine) {
        validateMachine(machine);

        // 檢查機台是否存在
        if (!machinesRepository.existsById(machine.getMachineId())) {
            throw new IllegalArgumentException("機台不存在");
        }

        // 檢查出廠編號是否重複（不包含自己）
        Optional<MachinesBean> existing = machinesRepository.findBySerialNumber(machine.getSerialNumber());
        if (existing.isPresent() && existing.get().getMachineId() != machine.getMachineId()) {
            throw new IllegalArgumentException("出廠編號已存在，請勿重複");
        }

        machinesRepository.save(machine);
    }

    // 依狀態查詢機台
    public List<MachinesBean> findMachinesByStatus(String status) {
        return machinesRepository.findByMstatusOrderByMachineId(status);
    }

    // 依出廠編號查詢機台
    public MachinesBean findMachineBySerialNumber(String serialNumber) {
        return machinesRepository.findBySerialNumber(serialNumber).orElse(null);
    }

    // 模糊搜尋機台
    public List<MachinesBean> searchMachines(String keyword) {
        return machinesRepository.findByMachineNameContainingOrSerialNumberContainingOrderByMachineId(keyword);
    }

    // 驗證機台資料
    private void validateMachine(MachinesBean machine) {
        if (machine.getMachineName() == null || machine.getMachineName().trim().isEmpty()) {
            throw new IllegalArgumentException("機台名稱不可為空");
        }
        if (machine.getSerialNumber() == null || machine.getSerialNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("出廠編號不可為空");
        }
        if (machine.getMstatus() == null || machine.getMstatus().trim().isEmpty()) {
            throw new IllegalArgumentException("機台狀態不可為空");
        }
    }
}