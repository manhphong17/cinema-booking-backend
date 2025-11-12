package vn.cineshow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.cineshow.dto.response.BDashbroad.BusinessDashboardResponse;
import vn.cineshow.dto.response.ResponseData;
import vn.cineshow.service.BusinessDashboardService;

@RestController
@RequestMapping("/business")
@RequiredArgsConstructor
@Slf4j(topic = "BUSINESS-DASHBOARD-CONTROLLER")
public class BusinessDashbroadController {
    private final BusinessDashboardService businessDashboardService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('BUSINESS')")
    public ResponseData<BusinessDashboardResponse> getBusinessDashboard() {
        BusinessDashboardResponse dashboard = businessDashboardService.getBusinessDashboard();
        return new ResponseData<>(HttpStatus.OK.value(), "Lấy dữ liệu dashboard thành công", dashboard);
    }

}
