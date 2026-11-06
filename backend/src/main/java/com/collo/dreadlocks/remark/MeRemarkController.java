package com.collo.dreadlocks.remark;

import com.collo.dreadlocks.auth.AuthUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/me")
public class MeRemarkController {

    private final RemarkService remarkService;

    public MeRemarkController(RemarkService remarkService) {
        this.remarkService = remarkService;
    }

    @GetMapping("/remarks")
    public ResponseEntity<List<RemarkResponse>> mine(@AuthenticationPrincipal AuthUser current) {
        return ResponseEntity.ok(remarkService.listForCustomer(current.getCustomerId()));
    }

    @PostMapping("/bookings/{bookingId}/remarks")
    public ResponseEntity<RemarkResponse> create(
            @AuthenticationPrincipal AuthUser current,
            @PathVariable Integer bookingId,
            @Valid @RequestBody RemarkCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(remarkService.create(current.getCustomerId(), bookingId, request));
    }
}
