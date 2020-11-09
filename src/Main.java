import org.mindrot.jbcrypt.BCrypt;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.InputMismatchException;
import java.util.Scanner;

import static java.awt.SystemColor.menu;

public class Main {

    public static void listOfProduct() throws SQLException {
        DBConnection db = new DBConnection();
        try {
            Statement st = db.getConnection().createStatement();
            String query = "SELECT * FROM \"product\";";
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                System.out.print("Id =" + rs.getInt("id")+ " ");
                System.out.print(rs.getString("name")+ " ");
                System.out.print("Price =" + rs.getString("price")+ " ");
                System.out.println("Amount =" + rs.getString("amount"));
                System.out.println();
            }
        }catch (SQLException e) {
            System.out.println("SQLException");
        }
    }
    public static void signIn() throws SQLException {
        DBConnection db = new DBConnection();
        Scanner scan = new Scanner(System.in);
        try {
            System.out.println("Enter your name");
            String name = scan.next();
            System.out.println("Enter your password");
            String password = scan.next();
            Statement st = db.getConnection().createStatement();
            String query = "SELECT name, password, role, money FROM \"customer\" WHERE name = '"+ name +"';";
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                if (BCrypt.checkpw(password, rs.getString("password"))
                        && rs.getString("name").equals(name)
                        && rs.getString("role").equals("admin")){
                    System.out.println("It matches");
                    boolean check = true;
                    while (check) {
                        System.out.println("Add product = 1, Update product = 2, List of Product = 3, Exit = 0");
                        int x = scan.nextInt();
                        switch (x) {
                            case 1:
                                newProduct();
                                break;
                            case 2:
                                updateProduct();
                                break;
                            case 3:
                                listOfProduct();
                                break;
                            case 0:
                                check = false;
                        }
                    }
                }
                else if(BCrypt.checkpw(password, rs.getString("password"))
                        && rs.getString("name").equals(name)
                        && rs.getString("role").equals("customer")) {
                    boolean check = true;
                    while (check) {
                        System.out.println("Buy product = 1, List of Product = 2, Exit = 0");
                        int x = scan.nextInt();
                        switch (x) {
                            case 1:
                                buyProduct(name, rs.getInt("money"));
                                break;
                            case 2:
                                listOfProduct();
                                break;
                            case 0:
                                check = false;
                                break;
                        }
                    }
                }
                else {
                    System.out.println("It does not match");
                    break;
                }
            }
        } catch (SQLException e) {
            System.out.println("SQLException");
        }
    }
    public static int newInput(){
        Scanner scan = new Scanner(System.in);
        int newMoney;
        try {
            String money = scan.next();
            newMoney = Integer.parseInt(money);
            return newMoney;
        } catch (NumberFormatException e) {
            System.out.println("Please enter number");
            return newInput();
        }
    }
    public static void signUp(String admin) throws SQLException {
        DBConnection db = new DBConnection();
        Scanner scan = new Scanner(System.in);
        int newMoney;
        boolean check = true;
        try {

                System.out.println("Enter your name");
                String name = scan.next();
                System.out.println("Enter your money");
                int money = newInput();
                System.out.println("Enter your password");
                String password = scan.next();
                String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));
                String query = String.format("Insert into \"customer\" (name, money, role, password) values (?,?,'%s',?); ", admin);
                PreparedStatement stmt = db.getConnection().prepareStatement(query);
                stmt.setString(1, name);
                stmt.setInt(2, money);
                stmt.setString(3, hashed);
                stmt.executeUpdate();
                System.out.println("Data Saved");
        } catch (SQLException e) {
            System.out.println("SQLException");
        } catch (InputMismatchException e) {
            System.out.println("Enter number please");
        }
    }
    public static void updateProduct() throws SQLException{
        DBConnection db = new DBConnection();
        Scanner scan = new Scanner(System.in);
        try {
            System.out.println("Enter product name");
            String name = scan.next();
            System.out.println("Enter product price: ");
            int price = scan.nextInt();
            System.out.println("Enter product amount: ");
            int amount = scan.nextInt();
            String updateData = "UPDATE \"product\" SET amount = '"+ amount +"', price = '"+ price +"' WHERE name = '" + name + "';";
            PreparedStatement stmt = db.getConnection().prepareStatement(updateData);
            stmt.executeUpdate();
            System.out.println("Database updated successfully");
        } catch (SQLException e) {
            System.out.println("SQLException");
        }
    }
    public static void newProduct() throws SQLException{
        DBConnection db = new DBConnection();
        Scanner scan = new Scanner(System.in);
        try {
            System.out.println("Enter product name");
            String name = scan.next();
            System.out.println("Enter product price: ");
            int price = scan.nextInt();
            System.out.println("Enter product amount: ");
            int amount = scan.nextInt();
            String query = "Insert into \"product\" (name, price, amount) values (?,?,?); ";
            PreparedStatement stmt =  db.getConnection().prepareStatement(query);
            stmt.setString(1,name);
            stmt.setInt(2,price);
            stmt.setInt(3,amount);
            stmt.executeUpdate();
            System.out.println("Data Saved");
        } catch (SQLException e) {
            System.out.println("SQLException");
        }
    }

    public static void conditionOfBuy(String customerName, String productName, int productAmount) throws SQLException{
        DBConnection db = new DBConnection();
        int customerBalance;
        int productBalance;
        try{
            /*ResultSet rsCustomer = selectQuery("customer", "customerName");
            ResultSet rsProduct = selectQuery("product", "productName");*/
            String customerQuery = "SELECT * FROM \"customer\" WHERE name = '" + customerName +"';";
            String productQuery = "SELECT * FROM \"product\" WHERE name = '" + productName + "';";
            Statement st =  db.getConnection().createStatement();
            ResultSet rsCustomer = st.executeQuery(customerQuery);
            ResultSet rsProduct = st.executeQuery(productQuery);
            while (rsCustomer.next() && rsProduct.next()){
                if(customerName.equals(rsCustomer.getString("name"))
                        && productName.equals(rsProduct.getString("name"))
                        && rsCustomer.getInt("money") >= rsProduct.getInt("price")
                        && rsProduct.getInt("amount") >= productAmount){
                    customerBalance = rsCustomer.getInt("money") - (rsProduct.getInt("price") * productAmount);
                    productBalance = rsProduct.getInt("amount") - productAmount;
                    System.out.println("Your balance = " + customerBalance + " and you bought " + rsProduct.getString("name") + " in amount = " + productAmount + " pieces");
                    String updateData = "UPDATE \"customer\" SET money = '"+ customerBalance +"'WHERE name = '" + customerName + "';";
                    String updateData2 = "UPDATE \"product\" SET amount = '"+ productBalance +"'WHERE name = '" + productName + "';";
                    PreparedStatement stmt = db.getConnection().prepareStatement(updateData);
                    PreparedStatement stmt2 = db.getConnection().prepareStatement(updateData2);
                    stmt.executeUpdate();
                    stmt2.executeUpdate();
                    System.out.println("Database updated successfully");
                }
                else if(rsCustomer.getInt("money") <= rsProduct.getInt("price" ) * productAmount)
                    System.out.println("You don't have enough money");
                else if(rsProduct.getInt("amount") >= productAmount)
                    System.out.println("Shop have " + rsProduct.getInt("amount") + "products left, please select a number less than " + rsProduct.getInt("amount"));
                else
                    System.out.println("Error");
            }
        }catch (SQLException e){
            e.printStackTrace();

        }
    }
    public static void buyProduct(String customerName, int money){
        Scanner scan = new Scanner(System.in);
        System.out.println("Hello " + customerName + " you have " + money);
        System.out.println("Enter product name which your want buy");
        String productName = scan.next();
        System.out.println("Enter amount of product");
        int productAmount = scan.nextInt();
        try {
            conditionOfBuy(customerName, productName, productAmount);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
    public static void menu(){
        Scanner scan = new Scanner(System.in);
        boolean check = true;
        boolean check2 = true;
        while(check) {
            System.out.println("Sign in = 1, Sign up = 2 , Exit = 0");
            int x = scan.nextInt();
            switch (x) {
                case 1:
                    try {
                        signIn();
                        break;
                    } catch (SQLException exception) {
                        exception.printStackTrace();
                    }
                    break;
                case 2:
                    while(check2){
                        System.out.println("Sign up as admin = 1, Sign up as customer = 2, Exit = 0");
                        int y = scan.nextInt();
                        switch (y) {
                            case 1:
                                try {
                                    signUp("admin");
                                } catch (SQLException exception) {
                                    exception.printStackTrace();
                                }
                                break;
                            case 2:
                                try {
                                    signUp("customer");
                                } catch (SQLException exception) {
                                    exception.printStackTrace();
                                }
                                break;
                            case 0:
                                check2 = false;
                                menu();
                        }
                    }
                case 0:
                    check =false;
                    break;
            }
        }
    }
    public static void main(String[] args) {
        menu();
    }
}
