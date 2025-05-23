package hanu.fit.iws_final_project.controller;

import hanu.fit.iws_final_project.model.Booking;
import hanu.fit.iws_final_project.model.BookingStatus;
import hanu.fit.iws_final_project.model.User;
import hanu.fit.iws_final_project.repository.BookingRepository;
import hanu.fit.iws_final_project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
public class BookingController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(@RequestBody Booking booking) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) return ResponseEntity.badRequest().body("User not found");

        booking.setUserId(user.get().getId());
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setBookingCode(generateBookingCode());

        List<Booking> existingBookings = bookingRepository.findAll();
        for (Booking b : existingBookings) {
            if (b.getRoomId().equals(booking.getRoomId()) &&
                    (b.getStatus() == BookingStatus.PENDING || b.getStatus() == BookingStatus.ACCEPTED)) {

                boolean isOverlapping =
                        !booking.getCheckOutDate().isBefore(b.getCheckInDate()) &&
                                !booking.getCheckInDate().isAfter(b.getCheckOutDate());

                if (isOverlapping) {
                    return ResponseEntity.badRequest()
                            .body("The room is already booked for the selected dates.");
                }
            }
        }

        Booking saved = bookingRepository.save(booking);
        return ResponseEntity.ok(saved);
    }

    private String generateBookingCode() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code;
        Random random = new Random();
        do {
            code = new StringBuilder();
            for (int i = 0; i < 6; i++) {
                code.append(characters.charAt(random.nextInt(characters.length())));
            }
        } while (bookingRepository.existsByBookingCode(code.toString()));
        return code.toString();
    }

    @GetMapping("/customer/bookings")
    public ResponseEntity<List<Booking>> getCustomerBookings() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) return ResponseEntity.badRequest().build();
        List<Booking> bookings = bookingRepository.findByUserId(user.get().getId());
        return ResponseEntity.ok(bookings);
    }

    @PutMapping("/customer/bookings/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(@PathVariable Long id) {
        Optional<Booking> optionalBooking = bookingRepository.findById(id);
        if (optionalBooking.isEmpty()) return ResponseEntity.notFound().build();

        Booking booking = optionalBooking.get();
        if (booking.getStatus() == BookingStatus.ACCEPTED) {
            return ResponseEntity.badRequest().body(null);
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        return ResponseEntity.ok(booking);
    }

    @DeleteMapping("/customer/bookings/{id}")
    public ResponseEntity<Void> deleteCustomerBooking(@PathVariable Long id) {
        Optional<Booking> optionalBooking = bookingRepository.findById(id);
        if (optionalBooking.isEmpty()) return ResponseEntity.notFound().build();

        Booking booking = optionalBooking.get();
        if (booking.getStatus() == BookingStatus.ACCEPTED) {
            return ResponseEntity.badRequest().build();
        }

        bookingRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/bookings")
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return ResponseEntity.ok(bookings);
    }

    @PutMapping("/admin/bookings/{id}")
    public ResponseEntity<Booking> updateBooking(@PathVariable Long id, @RequestBody Booking updatedBooking) {
        Optional<Booking> optionalBooking = bookingRepository.findById(id);
        if (optionalBooking.isEmpty()) return ResponseEntity.notFound().build();

        Booking booking = optionalBooking.get();
        booking.setCheckInDate(updatedBooking.getCheckInDate());
        booking.setCheckOutDate(updatedBooking.getCheckOutDate());
        booking.setRoomId(updatedBooking.getRoomId());
        bookingRepository.save(booking);
        return ResponseEntity.ok(booking);
    }

    @PutMapping("/admin/bookings/{id}/status")
    public ResponseEntity<Booking> updateBookingStatus(@PathVariable Long id, @RequestParam String status) {
        Optional<Booking> optionalBooking = bookingRepository.findById(id);
        if (optionalBooking.isEmpty()) return ResponseEntity.notFound().build();

        Booking booking = optionalBooking.get();
        try {
            BookingStatus newStatus = BookingStatus.valueOf(status.toUpperCase());
            booking.setStatus(newStatus);
            bookingRepository.save(booking);
            return ResponseEntity.ok(booking);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @DeleteMapping("/admin/bookings/{id}")
    public ResponseEntity<Void> deleteAdminBooking(@PathVariable Long id) {
        if (!bookingRepository.existsById(id)) return ResponseEntity.notFound().build();
        bookingRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
