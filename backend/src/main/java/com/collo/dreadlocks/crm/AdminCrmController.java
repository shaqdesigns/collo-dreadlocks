package com.collo.dreadlocks.crm;

import com.collo.dreadlocks.customer.CustomerResponse;
import com.collo.dreadlocks.customer.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/crm/clients")
public class AdminCrmController {

    private final CrmService crmService;
    private final CustomerService customerService;

    public AdminCrmController(CrmService crmService, CustomerService customerService) {
        this.crmService = crmService;
        this.customerService = customerService;
    }

    @GetMapping
    public ResponseEntity<List<ClientSummaryResponse>> list(@RequestParam(required = false) String q) {
        return ResponseEntity.ok(crmService.list(q));
    }

    @GetMapping("/birthdays")
    public ResponseEntity<List<CustomerResponse>> birthdays() {
        return ResponseEntity.ok(customerService.getUpcomingBirthdays(14));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientProfileResponse> profile(@PathVariable Integer id) {
        return ResponseEntity.ok(crmService.profile(id));
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<ClientNoteResponse> addNote(
            @PathVariable Integer id,
            @Valid @RequestBody ClientNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(crmService.addNote(id, request));
    }
}
