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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import application.InterviewScheduler.InterviewScheduler;
import application.TaskTracker.TaskTracker;

public class HomePage extends Application {

    private static final String DB_URL = "jdbc:sqlite:user.db";
    private static boolean isLoggedIn = false;

    static {
        try (Connection connection = DriverManager.getConnection(DB_URL)) {
            String createTableSQL = "CREATE TABLE IF NOT EXISTS users (username TEXT PRIMARY KEY, password TEXT)";
            connection.createStatement().execute(createTableSQL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) {
        if (isLoggedIn) {
            showHomePage(primaryStage);
        } else {
            showLoginScreen(primaryStage);
        }
    }

    private void showLoginScreen(Stage primaryStage) {
        primaryStage.setTitle("Login/Sign Up");

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        Text heading = new Text("Welcome to NUMockMate");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 50));
        heading.setStyle("-fx-fill: linear-gradient(from 0% 0% to 100% 100%, #0073e6, #00c4ff);");

        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        TextField usernameField = new TextField();
        usernameField.setMaxWidth(screenBounds.getWidth() / 4);

        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        PasswordField passwordField = new PasswordField();
        passwordField.setMaxWidth(screenBounds.getWidth() / 4);

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: yellow;");

        Button loginButton = new Button("Login");
        Button signupButton = new Button("Sign Up");

        Button[] buttons = {loginButton, signupButton};
        for (Button button : buttons) {
            button.setStyle("-fx-font-size: 18px; -fx-background-color: #0073e6; -fx-text-fill: white;");
            button.setMinWidth(screenBounds.getWidth() / 8);
            button.setMinHeight(50);
        }

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Username and Password cannot be empty.");
            } else if (validateUser(username, password)) {
                isLoggedIn = true;
                showHomePage(primaryStage);
            } else {
                messageLabel.setText("Invalid Credentials. Please try again.");
            }
        });
        
        signupButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Username and Password cannot be empty.");
            } else if (addUser(username, password)) {
                messageLabel.setText("Sign Up Successful! You can now log in.");
            } else {
                messageLabel.setText("Sign Up Failed. Username already exists.");
            }
        });

        VBox formLayout = new VBox(20, heading, usernameLabel, usernameField, passwordLabel, passwordField, loginButton, signupButton, messageLabel);
        formLayout.setAlignment(Pos.CENTER);
        formLayout.setPadding(new Insets(40));
        formLayout.setStyle("-fx-background-color: #222222;");

        Scene loginScene = new Scene(formLayout, screenBounds.getWidth(), screenBounds.getHeight());
        primaryStage.setScene(loginScene);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    private void showHomePage(Stage primaryStage) {
        primaryStage.setTitle("NuMockMate - Home Page");

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        Text heading = new Text("NUMockMate");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 50));
        heading.setStyle("-fx-fill: linear-gradient(from 0% 0% to 100% 100%, #0073e6, #00c4ff);");

        Button applicationTrackerButton = new Button("Application Tracker");
        Button interviewQuestionsButton = new Button("Interview Questions");
        Button interviewSchedulerButton = new Button("Interview Scheduler");
        Button taskTrackerButton = new Button("Task Tracker");
        Button logoutButton = new Button("Logout");

        Button[] buttons = {applicationTrackerButton, interviewQuestionsButton, interviewSchedulerButton, taskTrackerButton};
        for (Button button : buttons) {
            button.setMinWidth(screenBounds.getWidth() / 4);
            button.setMinHeight(50);
            button.setStyle("-fx-font-size: 18px; -fx-background-color: #0073e6; -fx-text-fill: white;");
        }

        logoutButton.setStyle("-fx-font-size: 14px; -fx-background-color: #FF0000; -fx-text-fill: white;");

        applicationTrackerButton.setOnAction(e -> new ApplicationTracker().start(primaryStage));
        interviewQuestionsButton.setOnAction(e -> new InterviewQuestions().start(primaryStage));
        interviewSchedulerButton.setOnAction(e -> new InterviewScheduler().start(primaryStage));
        taskTrackerButton.setOnAction(e -> new TaskTracker().start(primaryStage));

        logoutButton.setOnAction(e -> {
            isLoggedIn = false;
            showLoginScreen(primaryStage);
        });

        VBox centerLayout = new VBox(40, heading, applicationTrackerButton, interviewQuestionsButton, interviewSchedulerButton, taskTrackerButton);
        centerLayout.setPadding(new Insets(20));
        centerLayout.setAlignment(Pos.CENTER);

        BorderPane root = new BorderPane();
        root.setTop(logoutButton);
        BorderPane.setAlignment(logoutButton, Pos.TOP_RIGHT);
        BorderPane.setMargin(logoutButton, new Insets(10));
        root.setCenter(centerLayout);

        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    private boolean addUser(String username, String password) {
        try (Connection connection = DriverManager.getConnection(DB_URL)) {
            String query = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean validateUser(String username, String password) {
        try (Connection connection = DriverManager.getConnection(DB_URL)) {
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = connection.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
