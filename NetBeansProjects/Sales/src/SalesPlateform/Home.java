package SalesPlateform;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.*;

public class Home extends JFrame {

    private JPanel productsPanel;
    private JButton showPurchasesButton;
    private Conn conn;

    public Home() {
        // Setup the JFrame
        setTitle("Online Sales Platform");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize the Conn class to manage database connection
        conn = new Conn();

        // Create the products panel to hold product details
        productsPanel = new JPanel(new GridLayout(0, 2)); // Two columns for products
        loadProducts();

        // Create "Show Purchases" button
        showPurchasesButton = new JButton("Show Purchases");
        showPurchasesButton.addActionListener(e -> showPurchases());

        // Add components to the main frame
        add(new JScrollPane(productsPanel), BorderLayout.CENTER);
        add(showPurchasesButton, BorderLayout.SOUTH);
    }

    // Load products from the database and display them on the home page
   // Modify loadProducts method to use ClassLoader.getSystemResource for images
private void loadProducts() {
    try {
        Statement statement = conn.s;
        String query = "SELECT * FROM products";
        ResultSet resultSet = statement.executeQuery(query);

        while (resultSet.next()) {
            int productId = resultSet.getInt("id");
            String productName = resultSet.getString("name");
            String imagePath = resultSet.getString("image_path");
            double price = resultSet.getDouble("price");

            // Print the path to verify it's correct
            System.out.println("Loading image from path: " + imagePath);

            // Create UI components for each product
            JPanel productPanel = new JPanel();
            JLabel nameLabel = new JLabel(productName);
            JLabel priceLabel = new JLabel("Price: $" + price);

            // Adjust path based on where images are stored
            URL imageUrl = ClassLoader.getSystemResource(imagePath); // Remove leading '/'
            if (imageUrl != null) {
                ImageIcon imageIcon = new ImageIcon(imageUrl);
                Image image = imageIcon.getImage().getScaledInstance(150, 150, Image.SCALE_DEFAULT);
                ImageIcon scaledIcon = new ImageIcon(image);
                JLabel imageLabel = new JLabel(scaledIcon);

                productPanel.add(imageLabel);
            } else {
                JLabel imageLabel = new JLabel("Image not found");
                productPanel.add(imageLabel);
            }

            productPanel.add(nameLabel);
            productPanel.add(priceLabel);

            JButton addButton = new JButton("Add to Buy");
            addButton.addActionListener(e -> addToCart(productId));
            productPanel.add(addButton);

            productsPanel.add(productPanel);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    } catch (NullPointerException e) {
        JOptionPane.showMessageDialog(this, "Image not found: " + e.getMessage());
    }
}



    // Method to add a product to the cart
    private void addToCart(int productId) {
        try {
            // Check if the product is already in the cart
            String checkQuery = "SELECT * FROM cart WHERE product_id = ?";
            PreparedStatement checkStmt = conn.c.prepareStatement(checkQuery);
            checkStmt.setInt(1, productId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // If product is already in the cart, increase the quantity
                String updateQuery = "UPDATE cart SET quantity = quantity + 1 WHERE product_id = ?";
                PreparedStatement updateStmt = conn.c.prepareStatement(updateQuery);
                updateStmt.setInt(1, productId);
                updateStmt.executeUpdate();
            } else {
                // If product is not in the cart, add it
                String insertQuery = "INSERT INTO cart (product_id) VALUES (?)";
                PreparedStatement insertStmt = conn.c.prepareStatement(insertQuery);
                insertStmt.setInt(1, productId);
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to show the products added to the cart
    private void showPurchases() {
        try {
            String query = "SELECT products.name, products.price, cart.quantity FROM cart JOIN products ON cart.product_id = products.id";
            Statement statement = conn.s;
            ResultSet resultSet = statement.executeQuery(query);

            StringBuilder purchases = new StringBuilder();
            double total = 0;
            while (resultSet.next()) {
                String productName = resultSet.getString("name");
                double price = resultSet.getDouble("price");
                int quantity = resultSet.getInt("quantity");

                purchases.append(productName).append(" - $").append(price).append(" x ").append(quantity).append("\n");
                total += price * quantity;
            }

            purchases.append("\nTotal: $").append(total);

            // Show the purchases in a message dialog
            JOptionPane.showMessageDialog(this, purchases.toString(), "Purchases", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Home().setVisible(true));
    }
}
