# 1. Overview:
The BankApplication servlet handles user login, viewing account details, and transferring funds between accounts.
An inner class Account represents a user's bank account.

## 1. Code Structure

- **Servlet Class**: `BankApplication` extends `HttpServlet` and handles HTTP GET and POST requests.
- **Inner Class**: `Account` represents a user's bank account with methods to deposit and withdraw funds.
- **Initialization**: The `init()` method initializes a simulated database of user accounts stored in a `HashMap`.

## 2. Initialization

In the `init()` method, a simulated database of accounts is created.
In a real application, accounts would be loaded from a database.

## 3. Request Handling

The `doGet` and `doPost` methods handle various actions based on the action parameter in the request.

## 4. Actions

- **Login**: Displays a login form and authenticates users.
- **View Account**: Shows account details to the logged-in user.
- **Transfer Funds**: Allows users to transfer funds to another account.
- **Logout**: Invalidates the user's session.

## 5. Vulnerability

In the `doTransfer` method, there is an information leakage vulnerability.
When a user attempts to transfer funds to a recipient that does not exist, the application reveals sensitive information.

```java
if (!accounts.containsKey(recipientUsername)) {
    response.getWriter().println("Recipient account '" + recipientUsername + "' does not exist.");
    return;
}
```

The application outputs the recipient's username directly without sanitization, which can be exploited for XSS attacks.

### Fixing the Vulnerability

#### Input Validation

Implement strict validation of user inputs:

```java
private boolean isValidUsername(String username) {
    // Allow only alphanumeric characters, 3-20 characters long
    return username != null && username.matches("^[a-zA-Z0-9]{3,20}$");
}
```

**Purpose**: Prevents injection of malicious characters.

**Application**:

```java
if (!isValidUsername(recipientUsername)) {
    showError(response, "Invalid recipient username.");
    return;
}
```

#### Input Sanitization

Escape special characters before outputting to the response:

```java
private String sanitizeForHTML(String input) {
    return input.replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;");
}
```

**Purpose**: Prevents execution of injected scripts.

**Application**:

```java
String sanitizedRecipient = sanitizeForHTML(recipientUsername);
```

#### Use Sanitized Input in Responses

```java
if (!accounts.containsKey(recipientUsername)) {
    response.getWriter().println("Recipient account '" + sanitizedRecipient + "' does not exist.");
    return;
}
```
