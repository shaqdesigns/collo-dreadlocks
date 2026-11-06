package com.collo.dreadlocks.common;

import com.collo.dreadlocks.auth.AuthConflictException;
import com.collo.dreadlocks.auth.AuthException;
import com.collo.dreadlocks.auth.ForbiddenOperationException;
import com.collo.dreadlocks.booking.BookingConflictException;
import com.collo.dreadlocks.booking.BookingNotFoundException;
import com.collo.dreadlocks.booking.InvalidBookingException;
import com.collo.dreadlocks.availability.AvailabilityConflictException;
import com.collo.dreadlocks.availability.AvailabilityNotFoundException;
import com.collo.dreadlocks.availability.InvalidAvailabilityException;
import com.collo.dreadlocks.customer.CustomerConflictException;
import com.collo.dreadlocks.customer.CustomerNotFoundException;
import com.collo.dreadlocks.customer.InvalidCustomerException;
import com.collo.dreadlocks.media.InvalidMediaException;
import com.collo.dreadlocks.notification.NotificationNotFoundException;
import com.collo.dreadlocks.remark.InvalidRemarkException;
import com.collo.dreadlocks.remark.RemarkConflictException;
import com.collo.dreadlocks.revenue.InvalidManualWorkException;
import com.collo.dreadlocks.salonist.InvalidSalonistException;
import com.collo.dreadlocks.salonist.SalonistConflictException;
import com.collo.dreadlocks.salonist.SalonistNotFoundException;
import com.collo.dreadlocks.service.InvalidServiceException;
import com.collo.dreadlocks.service.ServiceConflictException;
import com.collo.dreadlocks.service.ServiceNotFoundException;
import com.collo.dreadlocks.unavailability.InvalidUnavailabilityException;
import com.collo.dreadlocks.unavailability.UnavailabilityConflictException;
import com.collo.dreadlocks.unavailability.UnavailabilityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleServiceNotFound(
            ServiceNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "status", "404",
                        "error", "Service Not Found",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(SalonistNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSalonistNotFound(
            SalonistNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "status", "404",
                        "error", "Salonist Not Found",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(AvailabilityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAvailabilityNotFound(
            AvailabilityNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "status", "404",
                        "error", "Availability Not Found",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(InvalidAvailabilityException.class)
    public ResponseEntity<Map<String, String>> handleInvalidAvailability(
            InvalidAvailabilityException exception) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", "400",
                        "error", "Invalid Availability",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(AvailabilityConflictException.class)
    public ResponseEntity<Map<String, String>> handleAvailabilityConflict(
            AvailabilityConflictException exception) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "status", "409",
                        "error", "Availability Conflict",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCustomerNotFound(
            CustomerNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "status", "404",
                        "error", "Customer Not Found",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(InvalidCustomerException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCustomer(
            InvalidCustomerException exception) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", "400",
                        "error", "Invalid Customer",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(CustomerConflictException.class)
    public ResponseEntity<Map<String, String>> handleCustomerConflict(
            CustomerConflictException exception) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "status", "409",
                        "error", "Customer Conflict",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleBookingNotFound(
            BookingNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "status", "404",
                        "error", "Booking Not Found",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(InvalidBookingException.class)
    public ResponseEntity<Map<String, String>> handleInvalidBooking(
            InvalidBookingException exception) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", "400",
                        "error", "Invalid Booking",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(BookingConflictException.class)
    public ResponseEntity<Map<String, String>> handleBookingConflict(
            BookingConflictException exception) {

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "status", "409",
                        "error", "Booking Conflict",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, String>> handleAuth(AuthException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "status", "401",
                        "error", "Unauthorized",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(
            ForbiddenOperationException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "status", "403",
                        "error", "Forbidden",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(AuthConflictException.class)
    public ResponseEntity<Map<String, String>> handleAuthConflict(
            AuthConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "status", "409",
                        "error", "Account Conflict",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(UnavailabilityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUnavailabilityNotFound(
            UnavailabilityNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "status", "404",
                        "error", "Unavailability Not Found",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(InvalidUnavailabilityException.class)
    public ResponseEntity<Map<String, String>> handleInvalidUnavailability(
            InvalidUnavailabilityException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", "400",
                        "error", "Invalid Unavailability",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(UnavailabilityConflictException.class)
    public ResponseEntity<Map<String, String>> handleUnavailabilityConflict(
            UnavailabilityConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "status", "409",
                        "error", "Unavailability Conflict",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(InvalidSalonistException.class)
    public ResponseEntity<Map<String, String>> handleInvalidSalonist(InvalidSalonistException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("status", "400", "error", "Invalid Salonist", "message", exception.getMessage()));
    }

    @ExceptionHandler(SalonistConflictException.class)
    public ResponseEntity<Map<String, String>> handleSalonistConflict(SalonistConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("status", "409", "error", "Salonist Conflict", "message", exception.getMessage()));
    }

    @ExceptionHandler(InvalidServiceException.class)
    public ResponseEntity<Map<String, String>> handleInvalidService(InvalidServiceException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("status", "400", "error", "Invalid Service", "message", exception.getMessage()));
    }

    @ExceptionHandler(ServiceConflictException.class)
    public ResponseEntity<Map<String, String>> handleServiceConflict(ServiceConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("status", "409", "error", "Service Conflict", "message", exception.getMessage()));
    }

    @ExceptionHandler(InvalidManualWorkException.class)
    public ResponseEntity<Map<String, String>> handleInvalidManualWork(InvalidManualWorkException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("status", "400", "error", "Invalid Work", "message", exception.getMessage()));
    }

    @ExceptionHandler(InvalidMediaException.class)
    public ResponseEntity<Map<String, String>> handleInvalidMedia(InvalidMediaException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("status", "400", "error", "Invalid Photo", "message", exception.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleUploadTooLarge(MaxUploadSizeExceededException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("status", "400", "error", "Invalid Photo", "message", "Photo must be 5 MB or smaller"));
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotificationNotFound(NotificationNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("status", "404", "error", "Notification Not Found", "message", exception.getMessage()));
    }

    @ExceptionHandler(InvalidRemarkException.class)
    public ResponseEntity<Map<String, String>> handleInvalidRemark(
            InvalidRemarkException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", "400",
                        "error", "Invalid Remark",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(RemarkConflictException.class)
    public ResponseEntity<Map<String, String>> handleRemarkConflict(
            RemarkConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "status", "409",
                        "error", "Remark Conflict",
                        "message", exception.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage() == null
                        ? error.getField() + " is required"
                        : error.getDefaultMessage())
                .orElse("Invalid request");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", "400",
                        "error", "Invalid Request",
                        "message", message
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadableMessage(
            HttpMessageNotReadableException exception) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "status", "400",
                        "error", "Invalid Request",
                        "message", "Request body is invalid"
                ));
    }
}
