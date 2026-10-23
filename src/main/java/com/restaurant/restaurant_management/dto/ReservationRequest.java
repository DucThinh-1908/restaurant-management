package com.restaurant.restaurant_management.dto;

public class ReservationRequest {
    private String name;
    private String phone;
    private String date;
    private String time;
    private String guests;
    private String email;
    private String message;

    public ReservationRequest() {
    }

    public ReservationRequest(String name, String phone, String date, String time, String guests, String email, String message) {
        this.name = name;
        this.phone = phone;
        this.date = date;
        this.time = time;
        this.guests = guests;
        this.email = email;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getGuests() {
        return guests;
    }

    public void setGuests(String guests) {
        this.guests = guests;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
