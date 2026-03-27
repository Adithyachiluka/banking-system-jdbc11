package BankingSystemReporting;

import java.sql.*;
import java.util.Scanner;

public class BankSystem {

    static final String url = "jdbc:mysql://localhost:3306/bankdb";
    static final String user = "root";
    static final String pass = "pass123";

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection con = DriverManager.getConnection(url, user, pass);

            System.out.println("✅ Connected to Database!");

            while (true) {
                System.out.println("\n===== BANK SYSTEM =====");
                System.out.println("1. Add Customer");
                System.out.println("2. Create Account");
                System.out.println("3. Deposit");
                System.out.println("4. Withdraw");
                System.out.println("5. View Transactions");
                System.out.println("6. Regulatory Report ⭐");
                System.out.println("7. Exit");
                System.out.println("8. View Accounts");
                System.out.print("Enter choice: ");

                int ch = sc.nextInt();

                switch (ch) {

                    case 1:
                        sc.nextLine();
                        System.out.print("Enter name: ");
                        String name = sc.nextLine();

                        System.out.print("Enter email: ");
                        String email = sc.nextLine();

                        PreparedStatement ps1 = con.prepareStatement(
                                "INSERT INTO customers(name,email) VALUES(?,?)",
                                Statement.RETURN_GENERATED_KEYS);

                        ps1.setString(1, name);
                        ps1.setString(2, email);
                        ps1.executeUpdate();

                        ResultSet rs1 = ps1.getGeneratedKeys();
                        if (rs1.next()) {
                            System.out.println("✅ Customer ID: " + rs1.getInt(1));
                        }
                        break;

                    case 2:
                        System.out.print("Enter customer ID: ");
                        int cid = sc.nextInt();

                        PreparedStatement check = con.prepareStatement(
                                "SELECT * FROM customers WHERE customer_id=?");
                        check.setInt(1, cid);
                        ResultSet rsCheck = check.executeQuery();

                        if (!rsCheck.next()) {
                            System.out.println("❌ Customer not found");
                            break;
                        }

                        PreparedStatement ps2 = con.prepareStatement(
                                "INSERT INTO accounts(customer_id,balance) VALUES(?,0)",
                                Statement.RETURN_GENERATED_KEYS);

                        ps2.setInt(1, cid);
                        ps2.executeUpdate();

                        ResultSet rs2 = ps2.getGeneratedKeys();
                        if (rs2.next()) {
                            System.out.println("✅ Account ID: " + rs2.getInt(1));
                        }
                        break;

                    case 3:
                        showAccounts(con);
                        System.out.print("Enter account ID: ");
                        int accId = sc.nextInt();

                        System.out.print("Enter amount: ");
                        double dep = sc.nextDouble();

                        PreparedStatement ps3 = con.prepareStatement(
                                "UPDATE accounts SET balance = balance + ? WHERE account_id=?");
                        ps3.setDouble(1, dep);
                        ps3.setInt(2, accId);

                        if (ps3.executeUpdate() == 0) {
                            System.out.println("❌ Account not found");
                            break;
                        }

                        PreparedStatement txn1 = con.prepareStatement(
                                "INSERT INTO transactions(account_id,type,amount) VALUES(?,?,?)");
                        txn1.setInt(1, accId);
                        txn1.setString(2, "DEPOSIT");
                        txn1.setDouble(3, dep);
                        txn1.executeUpdate();

                        System.out.println("✅ Deposit successful");
                        break;

                    case 4:
                        showAccounts(con);
                        System.out.print("Enter account ID: ");
                        int accId2 = sc.nextInt();

                        System.out.print("Enter amount: ");
                        double wd = sc.nextDouble();

                        PreparedStatement bal = con.prepareStatement(
                                "SELECT balance FROM accounts WHERE account_id=?");
                        bal.setInt(1, accId2);
                        ResultSet rsBal = bal.executeQuery();

                        if (!rsBal.next()) {
                            System.out.println("❌ Account not found");
                            break;
                        }

                        if (rsBal.getDouble(1) < wd) {
                            System.out.println("❌ Insufficient Balance");
                            break;
                        }

                        PreparedStatement ps4 = con.prepareStatement(
                                "UPDATE accounts SET balance = balance - ? WHERE account_id=?");
                        ps4.setDouble(1, wd);
                        ps4.setInt(2, accId2);
                        ps4.executeUpdate();

                        PreparedStatement txn2 = con.prepareStatement(
                                "INSERT INTO transactions(account_id,type,amount) VALUES(?,?,?)");
                        txn2.setInt(1, accId2);
                        txn2.setString(2, "WITHDRAW");
                        txn2.setDouble(3, wd);
                        txn2.executeUpdate();

                        System.out.println("✅ Withdraw successful");
                        break;

                    case 5:
                        showAccounts(con);
                        System.out.print("Enter account ID: ");
                        int acc = sc.nextInt();

                        PreparedStatement ps5 = con.prepareStatement(
                                "SELECT * FROM transactions WHERE account_id=?");
                        ps5.setInt(1, acc);
                        ResultSet rs5 = ps5.executeQuery();

                        while (rs5.next()) {
                            System.out.println(
                                    rs5.getInt("txn_id") + " | " +
                                    rs5.getString("type") + " | " +
                                    rs5.getDouble("amount") + " | " +
                                    rs5.getTimestamp("txn_date"));
                        }
                        break;

                    case 6:
                        generateReport(con);
                        break;

                    case 7:
                        con.close();
                        System.exit(0);

                    case 8:
                        showAccounts(con);
                        break;

                    default:
                        System.out.println("❌ Invalid Choice");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void showAccounts(Connection con) throws Exception {
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM accounts");

        System.out.println("\n--- ACCOUNTS ---");
        while (rs.next()) {
            System.out.println("Account ID: " + rs.getInt(1) +
                    " | Customer ID: " + rs.getInt(2) +
                    " | Balance: " + rs.getDouble(3));
        }
    }

    static void generateReport(Connection con) throws Exception {
        Statement st = con.createStatement();

        ResultSet rs1 = st.executeQuery("SELECT SUM(amount) FROM transactions WHERE type='DEPOSIT'");
        if (rs1.next())
            System.out.println("Total Deposits: " + rs1.getDouble(1));

        ResultSet rs2 = st.executeQuery("SELECT SUM(amount) FROM transactions WHERE type='WITHDRAW'");
        if (rs2.next())
            System.out.println("Total Withdrawals: " + rs2.getDouble(1));
    }
}