package com.vehiclebooking.backend.repository;

import com.vehiclebooking.backend.entity.Booking;
import com.vehiclebooking.backend.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.UUID;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByUser_Id(UUID userId);

    List<Booking> findAllByStatusNotIn(Collection<BookingStatus> statuses);

    List<Booking> findByUser_IdIn(List<UUID> userIds);

    List<Booking> findByUser_IdInAndStatusNotIn(List<UUID> userIds, List<BookingStatus> statuses);
    List<Booking> findByAllocatedVehicleNotNullAndStatusNotIn(Collection<BookingStatus> statuses);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.id != :bookingId " +
           "AND b.allocatedVehicle.id = :vehicleId " +
           "AND b.status NOT IN (com.vehiclebooking.backend.entity.BookingStatus.CANCELLED, com.vehiclebooking.backend.entity.BookingStatus.COMPLETED) " +
           "AND (b.startDate < :endDate AND b.endDate > :startDate)")
    boolean hasConflictingBookings(@Param("vehicleId") String vehicleId,
                                   @Param("startDate") LocalDateTime startDate,
                                   @Param("endDate") LocalDateTime endDate,
                                   @Param("bookingId") UUID bookingId);
}
