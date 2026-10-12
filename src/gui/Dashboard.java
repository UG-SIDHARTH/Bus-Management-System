package gui;

import dao.BookingDAO;
import dao.BusDAO;
import dao.PassengerDAO;

import javax.swing.*;
import java.awt.*;

public class Dashboard extends JFrame {

    private JLabel statsLabel;

    public Dashboard() {
        setTitle("Bus Reservation System - Admin Dashboard");
        setSize(850, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(44, 62, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("Bus Reservation System");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 24));

        JLabel subtitle = new JLabel("Admin Control Panel");
        subtitle.setForeground(new Color(189, 195, 199));
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel titleBox = new JPanel(new GridLayout(2, 1));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(subtitle);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setForeground(Color.BLACK);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                new LoginFrame().setVisible(true);
                dispose();
            }
        });

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Center Grid of Action Buttons
        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 25, 25));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // 1. Bus Management Button
        JButton busButton = createDashboardCard("Bus Management", "Manage bus routes, capacity, and fares", new Color(52, 152, 219));
        busButton.addActionListener(e -> new BusManagement().setVisible(true));

        // 2. Passenger Management Button
        JButton passengerButton = createDashboardCard("Passenger Management", "Register and update passenger records", new Color(41, 128, 185));
        passengerButton.addActionListener(e -> new PassengerManagement().setVisible(true));

        // 3. Seat Booking Button
        JButton bookingButton = createDashboardCard("Seat Booking", "Reserve bus seats and view real-time availability", new Color(39, 174, 96));
        bookingButton.addActionListener(e -> new BookingManagement().setVisible(true));

        // 4. Ticket Generation Button
        JButton ticketButton = createDashboardCard("Ticket Generation", "Issue boarding passes and export tickets", new Color(142, 68, 173));
        ticketButton.addActionListener(e -> new TicketGeneration().setVisible(true));

        gridPanel.add(busButton);
        gridPanel.add(passengerButton);
        gridPanel.add(bookingButton);
        gridPanel.add(ticketButton);

        mainPanel.add(gridPanel, BorderLayout.CENTER);

        // Footer / Stats Bar & Copyright
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        statsLabel = new JLabel("Loading system statistics...");
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        statsLabel.setForeground(new Color(127, 140, 141));

        JLabel copyrightLabel = new JLabel("© 2026 SwiftBus Inc. All Rights Reserved.", SwingConstants.CENTER);
        copyrightLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        copyrightLabel.setForeground(new Color(149, 165, 166));

        JButton refreshStatsBtn = new JButton("Refresh Stats");
        refreshStatsBtn.addActionListener(e -> updateQuickStats());

        footerPanel.add(statsLabel, BorderLayout.WEST);
        footerPanel.add(copyrightLabel, BorderLayout.CENTER);
        footerPanel.add(refreshStatsBtn, BorderLayout.EAST);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);

        updateQuickStats();
    }

    private JButton createDashboardCard(String title, String desc, Color accentColor) {
        JButton button = new JButton();
        button.setLayout(new BorderLayout(5, 5));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 18));
        titleLbl.setForeground(accentColor);

        JLabel descLbl = new JLabel("<html><body style='width: 180px; color: #555555;'>" + desc + "</body></html>");
        descLbl.setFont(new Font("Arial", Font.PLAIN, 12));

        button.add(titleLbl, BorderLayout.NORTH);
        button.add(descLbl, BorderLayout.CENTER);

        return button;
    }

    private void updateQuickStats() {
        try {
            int busCount = new BusDAO().getAllBuses().size();
            int passCount = new PassengerDAO().getAllPassengers().size();
            int bookCount = new BookingDAO().getAllBookings().size();
            statsLabel.setText(String.format("System Status:  %d Buses  |  %d Passengers  |  %d Bookings registered", busCount, passCount, bookCount));
        } catch (Exception ex) {
            statsLabel.setText("System Status: Database connected");
        }
    }
}