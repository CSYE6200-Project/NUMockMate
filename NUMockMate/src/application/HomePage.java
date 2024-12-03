package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class HomePage extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("NuMockMate - Home Page");

        // Full-Screen Setup
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        // Heading
        Text heading = new Text("NUMockMate");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 50));
        heading.setStyle("-fx-fill: linear-gradient(from 0% 0% to 100% 100%, #0073e6, #00c4ff);");

        // Navigation buttons
        Button applicationTrackerButton = new Button("Application Tracker");
        Button interviewQuestionsButton = new Button("Interview Questions");
        Button interviewSchedulerButton = new Button("Interview Scheduler");
        Button taskTrackerButton = new Button("Task Tracker");

        // Button Styling
        Button[] buttons = {applicationTrackerButton, interviewQuestionsButton, interviewSchedulerButton, taskTrackerButton};
        for (Button button : buttons) {
            button.setMinWidth(screenBounds.getWidth() / 4); // Set button width to 1/4 of the screen width
            button.setMinHeight(50); // Set button height
            button.setStyle("-fx-font-size: 18px; -fx-background-color: #0073e6; -fx-text-fill: white;");
        }

        // Set button actions
        applicationTrackerButton.setOnAction(e -> new ApplicationTracker().start(primaryStage));
        interviewQuestionsButton.setOnAction(e -> new InterviewQuestions().start(primaryStage));
        interviewSchedulerButton.setOnAction(e -> new InterviewScheduler().start(primaryStage));
        taskTrackerButton.setOnAction(e -> new TaskTracker().start(primaryStage));

        // Root Layout
        VBox root = new VBox(40, heading, applicationTrackerButton, interviewQuestionsButton, interviewSchedulerButton, taskTrackerButton);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        // Scene Setup
        Scene scene = new Scene(root, screenBounds.getWidth(), screenBounds.getHeight());
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true); // Enable full-screen mode
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
