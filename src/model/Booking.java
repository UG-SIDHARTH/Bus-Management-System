package model;

public class Booking {

    // Core entity fields
    private int bookingId;
    private int passengerId;
    private Integer userId;
    private int busId;
    private int seatNumber;
    private String bookingDate;
    private double fare;
    private String status;

    // Supplementary detail fields for views and ticket generation
    private String passengerName;
    private String passengerPhone;
    private String busName;
    private String source;
    private String destination;

    public Booking() {
    }

    public Booking(int bookingId, int passengerId, int busId, int seatNumber, String bookingDate, double fare, String status) {
        this(bookingId, passengerId, null, busId, seatNumber, bookingDate, fare, status);
    }

    public Booking(int bookingId, int passengerId, Integer userId, int busId, int seatNumber, String bookingDate, double fare, String status) {
        this.bookingId = bookingId;
        this.passengerId = passengerId;
        this.userId = userId;
        this.busId = busId;
        this.seatNumber = seatNumber;
        this.bookingDate = bookingDate;
        this.fare = fare;
        this.status = status;
    }

    // Getters
    public int getBookingId() {
        return bookingId;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public Integer getUserId() {
        return userId;
    }

    public int getBusId() {
        return busId;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public double getFare() {
        return fare;
    }

    public String getStatus() {
        return status;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public String getPassengerPhone() {
        return passengerPhone;
    }

    public String getBusName() {
        return busName;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    // Setters
    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public void setPassengerId(int passengerId) {
        this.passengerId = passengerId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setBusId(int busId) {
        this.busId = busId;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public void setPassengerPhone(String passengerPhone) {
        this.passengerPhone = passengerPhone;
    }

    public void setBusName(String busName) {
        this.busName = busName;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        return "Booking #" + bookingId + " [User: " + userId + ", Passenger: " + passengerName + ", Bus: " + busName + ", Seat: " + seatNumber + "]";
    }
}
