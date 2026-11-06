package com.collo.dreadlocks.revenue;

import com.collo.dreadlocks.booking.Booking;
import com.collo.dreadlocks.booking.BookingRepository;
import com.collo.dreadlocks.booking.BookingStatus;
import com.collo.dreadlocks.salonist.Salonist;
import com.collo.dreadlocks.salonist.SalonistCatalogService;
import com.collo.dreadlocks.service.ServiceNotFoundException;
import com.collo.dreadlocks.service.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RevenueService {

    public static final BigDecimal PAYOUT_RATE = new BigDecimal("0.40");

    private static final List<BookingStatus> SOUGHT = List.copyOf(EnumSet.of(
            BookingStatus.PENDING,
            BookingStatus.CONFIRMED,
            BookingStatus.COMPLETED
    ));

    private final BookingRepository bookingRepository;
    private final ManualWorkRepository manualWorkRepository;
    private final SalonistCatalogService salonistCatalogService;
    private final ServiceRepository serviceRepository;

    public RevenueService(
            BookingRepository bookingRepository,
            ManualWorkRepository manualWorkRepository,
            SalonistCatalogService salonistCatalogService,
            ServiceRepository serviceRepository) {
        this.bookingRepository = bookingRepository;
        this.manualWorkRepository = manualWorkRepository;
        this.salonistCatalogService = salonistCatalogService;
        this.serviceRepository = serviceRepository;
    }

    public RevenueReportResponse report() {
        List<Booking> soughtBookings = bookingRepository.findByStatusIn(SOUGHT);
        List<Booking> completed = soughtBookings.stream()
                .filter(booking -> booking.getStatus() == BookingStatus.COMPLETED)
                .toList();
        List<ManualWork> manualWorks = manualWorkRepository.findAllByOrderByWorkedAtDesc();

        Map<Integer, MutableService> services = new HashMap<>();
        Map<Integer, MutableSalonist> salonists = new HashMap<>();

        for (Booking booking : soughtBookings) {
            MutableService service = services.computeIfAbsent(booking.getServiceId(), this::serviceBucket);
            service.soughtCount++;
            MutableSalonist salonist = salonists.computeIfAbsent(booking.getSalonistId(), this::salonistBucket);
            if (booking.getStatus() == BookingStatus.COMPLETED) {
                service.revenue = service.revenue.add(booking.getPrice());
                salonist.revenue = salonist.revenue.add(booking.getPrice());
                salonist.completedBookings++;
            }
        }
        for (ManualWork work : manualWorks) {
            MutableService service = services.computeIfAbsent(work.getServiceId(), this::serviceBucket);
            service.soughtCount++;
            service.revenue = service.revenue.add(work.getPrice());
            MutableSalonist salonist = salonists.computeIfAbsent(work.getSalonistId(), this::salonistBucket);
            salonist.revenue = salonist.revenue.add(work.getPrice());
            salonist.manualWorks++;
        }

        List<ServiceRevenueResponse> serviceRows = services.values().stream()
                .map(row -> new ServiceRevenueResponse(row.id, row.name, row.soughtCount, money(row.revenue)))
                .sorted(Comparator.comparing(ServiceRevenueResponse::revenue).reversed()
                        .thenComparing(ServiceRevenueResponse::soughtCount, Comparator.reverseOrder()))
                .toList();
        List<SalonistRevenueResponse> salonistRows = salonists.values().stream()
                .map(row -> new SalonistRevenueResponse(
                        row.id,
                        row.name,
                        row.completedBookings,
                        row.manualWorks,
                        money(row.revenue),
                        payout(row.revenue)
                ))
                .sorted(Comparator.comparing(SalonistRevenueResponse::revenue).reversed())
                .toList();

        BigDecimal bookingRevenue = completed.stream()
                .map(Booking::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal manualRevenue = manualWorks.stream()
                .map(ManualWork::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal total = bookingRevenue.add(manualRevenue);

        return new RevenueReportResponse(
                PAYOUT_RATE,
                money(total),
                payout(total),
                money(total.subtract(payout(total))),
                money(bookingRevenue),
                money(manualRevenue),
                mostSought(serviceRows),
                highestRevenue(serviceRows),
                serviceRows,
                salonistRows,
                manualWorks.stream().map(this::toResponse).toList()
        );
    }

    public SalonistEarningsResponse earningsFor(Integer salonistId) {
        salonistCatalogService.requireSalonistEntity(salonistId);
        long completed = bookingRepository.findBySalonistIdOrderByStartAtDesc(salonistId).stream()
                .filter(booking -> booking.getStatus() == BookingStatus.COMPLETED)
                .count();
        List<ManualWork> manual = manualWorkRepository.findBySalonistIdOrderByWorkedAtDesc(salonistId);
        BigDecimal revenue = bookingRepository.findBySalonistIdOrderByStartAtDesc(salonistId).stream()
                .filter(booking -> booking.getStatus() == BookingStatus.COMPLETED)
                .map(Booking::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        revenue = revenue.add(manual.stream().map(ManualWork::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
        return new SalonistEarningsResponse(PAYOUT_RATE, money(revenue), payout(revenue), completed, manual.size());
    }

    public List<ManualWorkResponse> listManual() {
        return manualWorkRepository.findAllByOrderByWorkedAtDesc().stream().map(this::toResponse).toList();
    }

    public List<ManualWorkResponse> listManualForSalonist(Integer salonistId) {
        return manualWorkRepository.findBySalonistIdOrderByWorkedAtDesc(salonistId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ManualWorkResponse record(ManualWorkRequest request, Integer salonistId) {
        if (request.price() == null || request.price().signum() <= 0) {
            throw new InvalidManualWorkException("price must be greater than 0");
        }
        Salonist salonist = salonistCatalogService.requireSalonistEntity(salonistId);
        com.collo.dreadlocks.service.Service service = serviceRepository.findById(request.serviceId())
                .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + request.serviceId()));

        ManualWork work = new ManualWork();
        work.setSalonistId(salonist.getId());
        work.setServiceId(service.getId());
        work.setPrice(request.price().setScale(2, RoundingMode.HALF_UP));
        work.setWorkedAt(request.workedAt());
        work.setCustomerName(blankToNull(request.customerName()));
        work.setNote(blankToNull(request.note()));
        return toResponse(manualWorkRepository.save(work));
    }

    private ServiceRevenueResponse mostSought(List<ServiceRevenueResponse> rows) {
        return rows.stream()
                .max(Comparator.comparingLong(ServiceRevenueResponse::soughtCount)
                        .thenComparing(ServiceRevenueResponse::revenue))
                .orElse(null);
    }

    private ServiceRevenueResponse highestRevenue(List<ServiceRevenueResponse> rows) {
        return rows.stream()
                .max(Comparator.comparing(ServiceRevenueResponse::revenue)
                        .thenComparingLong(ServiceRevenueResponse::soughtCount))
                .orElse(null);
    }

    private MutableService serviceBucket(Integer id) {
        com.collo.dreadlocks.service.Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + id));
        MutableService bucket = new MutableService();
        bucket.id = id;
        bucket.name = service.getName();
        return bucket;
    }

    private MutableSalonist salonistBucket(Integer id) {
        Salonist salonist = salonistCatalogService.requireSalonistEntity(id);
        MutableSalonist bucket = new MutableSalonist();
        bucket.id = id;
        bucket.name = salonist.getName();
        return bucket;
    }

    private ManualWorkResponse toResponse(ManualWork work) {
        Salonist salonist = salonistCatalogService.requireSalonistEntity(work.getSalonistId());
        com.collo.dreadlocks.service.Service service = serviceRepository.findById(work.getServiceId())
                .orElseThrow(() -> new ServiceNotFoundException("Service not found: " + work.getServiceId()));
        return new ManualWorkResponse(
                work.getId(),
                salonist.getId(),
                salonist.getName(),
                service.getId(),
                service.getName(),
                work.getCustomerName(),
                work.getPrice(),
                work.getWorkedAt(),
                work.getNote()
        );
    }

    private BigDecimal payout(BigDecimal revenue) {
        return money(revenue.multiply(PAYOUT_RATE));
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static final class MutableService {
        private Integer id;
        private String name;
        private long soughtCount;
        private BigDecimal revenue = BigDecimal.ZERO;
    }

    private static final class MutableSalonist {
        private Integer id;
        private String name;
        private long completedBookings;
        private long manualWorks;
        private BigDecimal revenue = BigDecimal.ZERO;
    }
}
