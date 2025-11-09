package com.example.booking.controller;

import com.example.booking.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class HomeController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/accommodations/{id}")
    public String accommodationDetail(@PathVariable Long id, Model model) {
        model.addAttribute("accommodationId", id);
        return "accommodation-detail";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {

        if (error != null) {
            model.addAttribute("errorMessage", "帳號或密碼錯誤！");
        }

        if (logout != null) {
            model.addAttribute("logoutMessage", "已成功登出！");
        }

        return "login";
    }

    @GetMapping("/admin-dashboard")
    public String adminDashboardPage() {
        return "admin-dashboard";
    }

    @GetMapping("/register") public String registerPage() { return "register"; }
    @GetMapping("/admin-bookings") public String adminBookingsPage() { return "admin-bookings"; }
    @GetMapping("/admin-accommodations") public String adminAccommodationsPage() { return "admin-accommodations"; }
    @GetMapping("/owner-dashboard")
    public String ownerDashboard() {
        return "owner-dashboard";
    }

    @GetMapping("/owner-accommodations")
    public String ownerAccommodations() {
        return "owner-accommodations";
    }

    @GetMapping("/owner-bookings")
    public String ownerBookings() {
        return "owner-bookings";
    }

    @GetMapping("/room-type-management")
    public String roomTypeManagement() {
        return "room-type-management";
    }

    @GetMapping("/admin-users")
    public String adminUsersPage() {
        return "admin-users";
    }

    @GetMapping("/user-bookings")
    public String userBookings() {
        return "user-bookings";
    }
}