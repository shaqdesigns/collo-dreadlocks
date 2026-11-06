package com.collo.dreadlocks.crm;

import com.collo.dreadlocks.booking.BookingResponse;
import com.collo.dreadlocks.customer.CustomerResponse;
import com.collo.dreadlocks.remark.RemarkResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ClientProfileResponse(
        CustomerResponse customer,
        int visits,
        BigDecimal spent,
        LocalDateTime lastVisitAt,
        LocalDateTime nextVisitAt,
        List<BookingResponse> bookings,
        List<RemarkResponse> remarks,
        List<ClientNoteResponse> notes
) {
}
