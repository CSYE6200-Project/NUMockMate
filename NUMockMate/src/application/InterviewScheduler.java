package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.sql.*;

public class InterviewScheduler extends Application {

    private VBox scheduledInterviewsContainer;
    private Label messageLabel; // Label for showing feedback messages
    private static final String DB_URL = "jdbc:sqlite:numockmate.db";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Interview Scheduler");

        // Full-Screen Setup
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        // Heading
        Text heading = new Text("Interview Scheduler");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        heading.setStyle("-fx-fill: linear-gradient(from 0% 0% to 100% 100%, #0073e6, #00c4ff);");

        // Message Label for Feedback
        messageLabel = new Label();
        messageLabel.setFont(Font.font("Arial", 16));
        messageLabel.setWrapText(true);

        // Scheduled interviews container with scroll functionality
        scheduledInterviewsContainer = new VBox(10);
        scheduledInterviewsContainer.setPadding(new Insets(10));
        scheduledInterviewsContainer.setStyle("-fx-padding: 15px;");
        ScrollPane scrollPane = new ScrollPane(scheduledInterviewsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-border-color: #0073e6; -fx-border-width: 2px; -fx-padding: 10;");

        // Load scheduled interviews
        loadScheduledInterviews();

        // Form Fields
        GridPane formPane = new GridPane();
        formPane.setPadding(new Insets(20));
        formPane.setHgap(15);
        formPane.setVgap(15);
        formPane.setAlignment(Pos.CENTER);

        Label interviewerLabel = new Label("Interviewer Name:");
        TextField interviewerField = new TextField();
        interviewerField.setPromptText("Enter interviewer name");

        Label intervieweeLabel = new Label("Interviewee Name:");
        TextField intervieweeField = new TextField();
        intervieweeField.setPromptText("Enter interviewee name");

        Label typeLabel = new Label("Interview Type:");
        ComboBox<String> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Behavioral", "Technical", "Role-Specific");
        typeComboBox.setPromptText("Select Interview Type");

        Label dateLabel = new Label("Date:");
        DatePicker datePicker = new DatePicker();

        Label timeLabel = new Label("Time:");
        TextField timeField = new TextField();
        timeField.setPromptText("e.g., 10:00 AM");

        Label topicLabel = new Label("Topic:");
        TextField topicField = new TextField();
        topicField.setPromptText("Enter topic of the interview");

        Label roleLabel = new Label("Role:");
        TextField roleField = new TextField();
        roleField.setPromptText("e.g., Peer, Mentor, Professional");

        Label durationLabel = new Label("Duration:");
        TextField durationField = new TextField();
        durationField.setPromptText("e.g., 30 minutes");

        Label notesLabel = new Label("Notes:");
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Add any notes or preparation details...");
        notesArea.setPrefHeight(100);

        Button scheduleButton = new Button("Schedule Interview");
        scheduleButton.setStyle("-fx-background-color: #0073e6; -fx-text-fill: white; -fx-font-size: 14px;");
        scheduleButton.setOnAction(e -> {
            String interviewer = interviewerField.getText();
            String interviewee = intervieweeField.getText();
            String type = typeComboBox.getValue();
            String date = datePicker.getValue() != null ? datePicker.getValue().toString() : "Not selected";
            String time = timeField.getText();
            String topic = topicField.getText();
            String role = roleField.getText();
            String duration = durationField.getText();
            String notes = notesArea.getText();

            if (interviewer.isEmpty() || interviewee.isEmpty() || type == null || time.isEmpty() || topic.isEmpty() || role.isEmpty() || duration.isEmpty()) {
                setMessage("Please fill out all required fields.", "error");
            } else {
                saveInterview(interviewer, interviewee, type, date, time, topic, role, duration, notes);
                loadScheduledInterviews();
                setMessage("Interview scheduled successfully!", "success");
            }
        });

        Button homeButton = new Button("Back to Home");
        homeButton.setStyle("-fx-background-color: #0073e6; -fx-text-fill: white; -fx-font-size: 14px;");
        homeButton.setOnAction(e -> new HomePage().start(primaryStage));

        VBox buttonBox = new VBox(10, scheduleButton, homeButton);
        buttonBox.setAlignment(Pos.CENTER);

        formPane.add(interviewerLabel, 0, 0);
        formPane.add(interviewerField, 1, 0);
        formPane.add(intervieweeLabel, 0, 1);
        formPane.add(intervieweeField, 1, 1);
        formPane.add(typeLabel, 0, 2);
        formPane.add(typeComboBox, 1, 2);
        formPane.add(dateLabel, 0, 3);
        formPane.add(datePicker, 1, 3);
        formPane.add(timeLabel, 0, 4);
        formPane.add(timeField, 1, 4);
        formPane.add(topicLabel, 0, 5);
        formPane.add(topicField, 1, 5);
        formPane.add(roleLabel, 0, 6);
        formPane.add(roleField, 1, 6);
        formPane.add(durationLabel, 0, 7);
        formPane.add(durationField, 1, 7);
        formPane.add(notesLabel, 0, 8);
        formPane.add(notesArea, 1, 8);
        formPane.add(buttonBox, 1, 9);

        // Main Layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(formPane);
        mainLayout.setCenter(scrollPane);
        mainLayout.setTop(new VBox(10, heading, messageLabel));

        Scene scene = new Scene(mainLayout, screenBounds.getWidth(), screenBounds.getHeight());
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS interviews (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    interviewer TEXT,
                    interviewee TEXT,
                    type TEXT,
                    date TEXT,
                    time TEXT,
                    topic TEXT,
                    role TEXT,
                    duration TEXT,
                    notes TEXT
                );
            """;
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveInterview(String interviewer, String interviewee, String type, String date, String time,
                               String topic, String role, String duration, String notes) {
        String insertSQL = """
            INSERT INTO interviews (interviewer, interviewee, type, date, time, topic, role, duration, notes)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);
        """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setString(1, interviewer);
            pstmt.setString(2, interviewee);
            pstmt.setString(3, type);
            pstmt.setString(4, date);
            pstmt.setString(5, time);
            pstmt.setString(6, topic);
            pstmt.setString(7, role);
            pstmt.setString(8, duration);
            pstmt.setString(9, notes);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteInterview(int id) {
        String deleteSQL = "DELETE FROM interviews WHERE id = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadScheduledInterviews() {
        scheduledInterviewsContainer.getChildren().clear();
        String selectSQL = "SELECT * FROM interviews";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {
            while (rs.next()) {
                VBox interviewBox = new VBox(5);
                interviewBox.setStyle("-fx-background-color: #f0f8ff; -fx-padding: 10; -fx-border-color: #0073e6; -fx-border-radius: 5; -fx-background-radius: 5;");

                Label interviewer = new Label("Interviewer: " + rs.getString("interviewer"));
                Label interviewee = new Label("Interviewee: " + rs.getString("interviewee"));
                Label type = new Label("Type: " + rs.getString("type"));
                Label date = new Label("Date: " + rs.getString("date"));
                Label time = new Label("Time: " + rs.getString("time"));
                Label topic = new Label("Topic: " + rs.getString("topic"));
                Label role = new Label("Role: " + rs.getString("role"));
                Label duration = new Label("Duration: " + rs.getString("duration"));
                Label notes = new Label("Notes: " + rs.getString("notes"));

                Font labelFont = Font.font("Arial", FontWeight.BOLD, 14);
                for (Label label : new Label[]{interviewer, interviewee, type, date, time, topic, role, duration, notes}) {
                    label.setFont(labelFont);
                }

                // Get the id of the current interview
                int interviewId = rs.getInt("id");

                // Delete button
                Button deleteButton = new Button("Delete");
                deleteButton.setStyle("-fx-background-color: #ff4d4d; -fx-text-fill: white;");
                deleteButton.setOnAction(e -> {
                    deleteInterview(interviewId); // Pass the id to deleteInterview
                    loadScheduledInterviews();
                });

                interviewBox.getChildren().addAll(interviewer, interviewee, type, date, time, topic, role, duration, notes, deleteButton);
                scheduledInterviewsContainer.getChildren().add(interviewBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    private void setMessage(String message, String type) {
        messageLabel.setText(message);
        if (type.equals("success")) {
            messageLabel.setStyle("-fx-text-fill: green;");
        } else {
            messageLabel.setStyle("-fx-text-fill: red;");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
