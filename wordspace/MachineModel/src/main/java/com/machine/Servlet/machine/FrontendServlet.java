package com.machine.Servlet.machine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import com.machine.Bean.MachinesBean;
import com.machine.Service.machine.MachinesService;

@RestController
@RequestMapping("/FrontendServlet")
public class FrontendServlet {

    @Autowired
    private MachinesService machinesService;

    @GetMapping
    public List<MachinesBean> getMachines(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String statusFilter) {

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

        return machinesList;
    }
}