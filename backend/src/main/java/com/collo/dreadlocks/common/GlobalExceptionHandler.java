package com.collo.dreadlocks.common;

import com.collo.dreadlocks.booking.BookingConflictException;
import com.collo.dreadlocks.booking.BookingNotFoundException;
import com.collo.dreadlocks.booking.InvalidBookingException;
import com.collo.dreadlocks.availability.AvailabilityConflictException;
import com.collo.dreadlocks.availability.AvailabilityNotFoundException;
import com.collo.dreadlocks.availability.InvalidAvailabilityException;
import com.collo.dreadlocks.customer.CustomerConflictException;
import com.collo.dreadlocks.customer.CustomerNotFoundException;
import com.collo.dreadlocks.customer.InvalidCustomerException;
import com.collo.dreadlocks.salonist.SalonistNotFoundException;
import com.collo.dreadlocks.service.ServiceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
