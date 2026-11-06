package com.collo.dreadlocks.remark;

import com.collo.dreadlocks.auth.AuthUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/salonist/remarks")
public class SalonistRemarkController {

    private final RemarkService remarkService;

    public SalonistRemarkController(RemarkService remarkService) {
        this.remarkService = remarkService;
    }

    @GetMapping
    public ResponseEntity<List<RemarkResponse>> mine(@AuthenticationPrincipal AuthUser current) {
        return ResponseEntity.ok(remarkService.listForSalonist(current.getSalonistId()));
    }
}
