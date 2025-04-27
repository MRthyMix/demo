// File: BankApplication.java

import java.io.IOException;
import java.sql.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/BankApplication")
public class BankApplication extends HttpServlet {

    // Database credentials (for demonstration purposes only)
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bankdb";
    private static final String USER = "root";
    private static final String PASS = "password";

    // Simulated database of accounts
    private Map<String, Account> accounts = new HashMap<>();

    public void init() throws ServletException {
        // Initialize the accounts (In a real app, this would query the database)
        accounts.put("user1", new Account("user1", "User One", 1500.75));
        accounts.put("user2", new Account("user2", "User Two", 2350.00));
        accounts.put("admin", new Account("admin", "Administrator", 5000.00));
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Handle actions based on the 'action' parameter
        String action = request.getParameter("action");
        if (action == null) {
            showLoginForm(response);
        } else {
            switch (action) {
                case "login":
                    showLoginForm(response);
                    break;
                case "logout":
                    logout(request, response);
                    break;
                case "view":
                    viewAccount(request, response);
                    break;
                case "transfer":
                    showTransferForm(response);
                    break;
                case "doTransfer":
                    doTransfer(request, response);
                    break;
                default:
                    showError(response, "Unknown action: " + action);
            }
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Handle form submissions
        String action = request.getParameter("action");
        if (action == null) {
            showError(response, "No action specified.");
        } else {
            switch (action) {
                case "doLogin":
                    doLogin(request, response);
                    break;
                case "doTransfer":
                    doTransfer(request, response);
                    break;
                default:
                    showError(response, "Unknown action: " + action);
            }
        }
    }

    private void showLoginForm(HttpServletResponse response) throws IOException {
        response.setContentType("text/html");
        response.getWriter().println("<html><body>");
        response.getWriter().println("<h2>Login</h2>");
        response.getWriter().println("<form method='post' action='?action=doLogin'>");
        response.getWriter().println("Username: <input type='text' name='username'/><br/>");
        response.getWriter().println("Password: <input type='password' name='password'/><br/>");
        response.getWriter().println("<input type='submit' value='Login'/>");
        response.getWriter().println("</form>");
        response.getWriter().println("</body></html>");
    }

    private void doLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Vulnerability: Potential SQL Injection (if using real DB queries)
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Simulate authentication (In a real app, validate credentials securely)
        if (accounts.containsKey(username) && "password123".equals(password)) {
            HttpSession session = request.getSession();
            session.setAttribute("username", username);
            response.sendRedirect("?action=view");
        } else {
            showError(response, "Invalid credentials.");
        }
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Invalidate the session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect("?action=login");
    }

    private void viewAccount(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Display account information
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("?action=login");
            return;
        }

        String username = (String) session.getAttribute("username");
        Account account = accounts.get(username);

        response.setContentType("text/html");
        response.getWriter().println("<html><body>");
        response.getWriter().println("<h2>Account Details</h2>");
        response.getWriter().println("<p>Name: " + account.getFullName() + "</p>");
        response.getWriter().println("<p>Balance: $" + account.getBalance() + "</p>");
        response.getWriter().println("<a href='?action=transfer'>Transfer Funds</a><br/>");
        response.getWriter().println("<a href='?action=logout'>Logout</a>");
        response.getWriter().println("</body></html>");
    }

    private void showTransferForm(HttpServletResponse response) throws IOException {
        // Display the fund transfer form
        response.setContentType("text/html");
        response.getWriter().println("<html><body>");
        response.getWriter().println("<h2>Transfer Funds</h2>");
        response.getWriter().println("<form method='post' action='?action=doTransfer'>");
        response.getWriter().println("Recipient Username: <input type='text' name='recipient'/><br/>");
        response.getWriter().println("Amount: <input type='text' name='amount'/><br/>");
        response.getWriter().println("<input type='submit' value='Transfer'/>");
        response.getWriter().println("</form>");
        response.getWriter().println("<a href='?action=view'>Back to Account</a>");
        response.getWriter().println("</body></html>");
    }

    private void doTransfer(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Process the fund transfer
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendRedirect("?action=login");
            return;
        }

        String senderUsername = (String) session.getAttribute("username");
        Account senderAccount = accounts.get(senderUsername);

        // Source: User input from request parameters
        String recipientUsername = request.getParameter("recipient");
        String amountStr = request.getParameter("amount");

        // Convert amount to double
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            showError(response, "Invalid amount.");
            return;
        }

        // Vulnerability: Information Leakage
        // Sink: Outputting sensitive information without sanitization
        if (!accounts.containsKey(recipientUsername)) {
            response.getWriter().println("Recipient account '" + recipientUsername + "' does not exist.");
            return;
        }

        Account recipientAccount = accounts.get(recipientUsername);

        if (senderAccount.getBalance() < amount) {
            response.getWriter().println("Insufficient funds in your account.");
            return;
        }

        // Perform the transfer
        senderAccount.withdraw(amount);
        recipientAccount.deposit(amount);

        response.setContentType("text/html");
        response.getWriter().println("<html><body>");
        response.getWriter().println("<h2>Transfer Successful</h2>");
        response.getWriter().println("<p>You have transferred $" + amount + " to " + recipientAccount.getFullName() + ".</p>");
        response.getWriter().println("<a href='?action=view'>Back to Account</a>");
        response.getWriter().println("</body></html>");
    }

    private void showError(HttpServletResponse response, String message) throws IOException {
        // Display an error message
        response.setContentType("text/html");
        response.getWriter().println("<html><body>");
        response.getWriter().println("<h2>Error</h2>");
        response.getWriter().println("<p>" + message + "</p>");
        response.getWriter().println("<a href='?action=login'>Login</a>");
        response.getWriter().println("</body></html>");
    }

    // Inner class representing an account
    class Account {
        private String username;
        private String fullName;
        private double balance;

        public Account(String username, String fullName, double balance) {
            this.username = username;
            this.fullName = fullName;
            this.balance = balance;
        }

        public String getUsername() {
            return username;
        }

        public String getFullName() {
            return fullName;
        }

        public double getBalance() {
            return balance;
        }

        public void deposit(double amount) {
            balance += amount;
        }

        public void withdraw(double amount) {
            balance -= amount;
        }
    }
}
