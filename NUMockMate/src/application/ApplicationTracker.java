package application;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.sql.*;

public class ApplicationTracker extends Application {

    private static final String DB_URL = "jdbc:sqlite:numockmate.db";
    private ObservableList<JobApplication> applications = FXCollections.observableArrayList();
    private TableView<JobApplication> tableView;

    public static class JobApplication {
        private final String company;
        private final String position;
        private final String status;
        private final String deadline;

        public JobApplication(String company, String position, String status, String deadline) {
            this.company = company;
            this.position = position;
            this.status = status;
            this.deadline = deadline;
        }

        public String getCompany() {
            return company;
        }

        public String getPosition() {
            return position;
        }

        public String getStatus() {
            return status;
        }

        public String getDeadline() {
            return deadline;
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Application Tracker");

        // Initialize SQLite database
        initializeDatabase();

        // Full-Screen Setup
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        // Heading
        Text heading = new Text("Application Tracker");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        heading.setStyle("-fx-fill: linear-gradient(from 0% 0% to 100% 100%, #0073e6, #00c4ff);");

        // TableView to display applications
        tableView = new TableView<>();
        tableView.setItems(applications);
        loadApplicationsFromDatabase(); // Load applications from database

        // Columns for the TableView
        TableColumn<JobApplication, String> companyColumn = new TableColumn<>("Company");
        companyColumn.setCellValueFactory(new PropertyValueFactory<>("company"));

        TableColumn<JobApplication, String> positionColumn = new TableColumn<>("Position");
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));

        TableColumn<JobApplication, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        TableColumn<JobApplication, String> deadlineColumn = new TableColumn<>("Deadline");
        deadlineColumn.setCellValueFactory(new PropertyValueFactory<>("deadline"));

        tableView.getColumns().addAll(companyColumn, positionColumn, statusColumn, deadlineColumn);

        // Form for adding a new application
        TextField companyField = new TextField();
        companyField.setPromptText("Company");
        TextField positionField = new TextField();
        positionField.setPromptText("Position");
        TextField statusField = new TextField();
        statusField.setPromptText("Status");
        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setPromptText("Pick a deadline");

        Button addButton = new Button("Add");
        addButton.setStyle("-fx-background-color: #0073e6; -fx-text-fill: white; -fx-font-size: 14px;");
        addButton.setOnAction(e -> {
            String company = companyField.getText();
            String position = positionField.getText();
            String status = statusField.getText();
            String deadline = deadlinePicker.getValue() != null ? deadlinePicker.getValue().toString() : "";

            if (!company.isEmpty() && !position.isEmpty() && !status.isEmpty() && !deadline.isEmpty()) {
                addApplicationToDatabase(company, position, status, deadline);
                loadApplicationsFromDatabase(); // Reload applications
                companyField.clear();
                positionField.clear();
                statusField.clear();
                deadlinePicker.setValue(null);
            } else {
                showAlert("Please fill in all required fields.");
            }
        });

        HBox form = new HBox(15, companyField, positionField, statusField, deadlinePicker, addButton);
        form.setPadding(new Insets(20));
        form.setAlignment(Pos.CENTER);

        // Reminder Section
        Label reminderLabel = new Label();
        reminderLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        reminderLabel.setStyle("-fx-text-fill: green;");

        TextField reminderField = new TextField();
        reminderField.setPromptText("Reminder text");

        DatePicker reminderDatePicker = new DatePicker();
        reminderDatePicker.setPromptText("Pick a date");

        Button setReminderButton = new Button("Set Reminder");
        setReminderButton.setStyle("-fx-background-color: #0073e6; -fx-text-fill: white; -fx-font-size: 14px;");
        setReminderButton.setOnAction(e -> {
            String reminderText = reminderField.getText();
            String reminderDate = reminderDatePicker.getValue() != null ? reminderDatePicker.getValue().toString() : "";

            if (!reminderText.isEmpty() && !reminderDate.isEmpty()) {
                reminderLabel.setText("Reminder for \"" + reminderText + "\" has been set for " + reminderDate + ".");
                reminderField.clear();
                reminderDatePicker.setValue(null);
            } else {
                showAlert("Please enter reminder text and pick a date.");
            }
        });

        HBox reminderBox = new HBox(15, reminderField, reminderDatePicker, setReminderButton);
        reminderBox.setAlignment(Pos.CENTER);
        reminderBox.setPadding(new Insets(10));

        VBox reminderSection = new VBox(10, new Label("Set a Reminder:"), reminderBox, reminderLabel);
        reminderSection.setPadding(new Insets(20));
        reminderSection.setAlignment(Pos.CENTER);

        // Back to Home Button
        Button homeButton = new Button("Back to Home");
        homeButton.setStyle("-fx-background-color: #0073e6; -fx-text-fill: white; -fx-font-size: 16px;");
        homeButton.setOnAction(e -> new HomePage().start(primaryStage));

        // Main Layout
        BorderPane layout = new BorderPane();
        layout.setTop(new VBox(20, heading, homeButton)); // Added heading and navigation
        layout.setCenter(tableView);
        layout.setBottom(new VBox(form, reminderSection));

        // Scene Setup
        Scene scene = new Scene(layout, screenBounds.getWidth(), screenBounds.getHeight());
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true); // Enable full-screen mode
        primaryStage.show();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS job_applications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    company TEXT,
                    position TEXT,
                    status TEXT,
                    deadline TEXT
                );
            """;
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addApplicationToDatabase(String company, String position, String status, String deadline) {
        String insertSQL = """
            INSERT INTO job_applications (company, position, status, deadline)
            VALUES (?, ?, ?, ?);
        """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, company);
            pstmt.setString(2, position);
            pstmt.setString(3, status);
            pstmt.setString(4, deadline);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadApplicationsFromDatabase() {
        applications.clear();
        String selectSQL = "SELECT company, position, status, deadline FROM job_applications";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {
            while (rs.next()) {
                applications.add(new JobApplication(
                        rs.getString("company"),
                        rs.getString("position"),
                        rs.getString("status"),
                        rs.getString("deadline")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}