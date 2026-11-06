package com.collo.dreadlocks.remark;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/remarks")
public class AdminRemarkController {

    private final RemarkService remarkService;

    public AdminRemarkController(RemarkService remarkService) {
        this.remarkService = remarkService;
    }

    @GetMapping
    public ResponseEntity<List<RemarkResponse>> list() {
        return ResponseEntity.ok(remarkService.listForAdmin());
    }
}
