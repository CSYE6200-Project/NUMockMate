package application.InterviewScheduler;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class InterviewForm {

    // Form fields declared as instance variables to allow access for clearing
    private static TextField interviewerField;
    private static TextField intervieweeField;
    private static ComboBox<String> typeComboBox;
    private static DatePicker datePicker;
    private static TextField timeField;
    private static TextField topicField;
    private static TextField roleField;
    private static TextField durationField;
    private static TextField meetingLinkField;
    private static TextArea notesArea;

    public static VBox createForm(Stage primaryStage) {
        GridPane formPane = new GridPane();
        formPane.setPadding(new Insets(20));
        formPane.setHgap(15);
        formPane.setVgap(15);
        formPane.setAlignment(Pos.CENTER);

        // Form Fields
        Label interviewerLabel = new Label("Interviewer Name:");
        interviewerField = new TextField();
        interviewerField.setPromptText("Enter interviewer name");

        Label intervieweeLabel = new Label("Interviewee Name:");
        intervieweeField = new TextField();
        intervieweeField.setPromptText("Enter interviewee name");

        Label typeLabel = new Label("Interview Type:");
        typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll("Behavioral", "Technical", "Role-Specific");
        typeComboBox.setPromptText("Select Interview Type");

        Label dateLabel = new Label("Date:");
        datePicker = new DatePicker();

        Label timeLabel = new Label("Time:");
        timeField = new TextField();
        timeField.setPromptText("e.g., 10:00 AM");

        Label topicLabel = new Label("Topic:");
        topicField = new TextField();
        topicField.setPromptText("Enter topic of the interview");

        Label roleLabel = new Label("Role:");
        roleField = new TextField();
        roleField.setPromptText("e.g., Peer, Mentor, Professional");

        Label durationLabel = new Label("Duration:");
        durationField = new TextField();
        durationField.setPromptText("e.g., 30 minutes");

        Label meetingLinkLabel = new Label("Meeting Link:");
        meetingLinkField = new TextField();
        meetingLinkField.setPromptText("Enter meeting link");

        Label notesLabel = new Label("Notes:");
        notesArea = new TextArea();
        notesArea.setPromptText("Add any notes or preparation details...");
        notesArea.setPrefHeight(100);

        Button scheduleButton = new Button("Schedule Interview");
        scheduleButton.setStyle("-fx-background-color: #0073e6; -fx-text-fill: white; -fx-font-size: 14px;");
        scheduleButton.setOnAction(e -> {
            DatabaseManager.saveInterview(
                interviewerField.getText(),
                intervieweeField.getText(),
                typeComboBox.getValue(),
                datePicker.getValue() != null ? datePicker.getValue().toString() : "Not selected",
                timeField.getText(),
                topicField.getText(),
                roleField.getText(),
                durationField.getText(),
                meetingLinkField.getText(),
                notesArea.getText()
            );
            ScheduledInterviews.refreshScheduledInterviews();
            clearFormFields(); // Clear the form after submission
        });

        Button homeButton = new Button("Back to Home");
        homeButton.setStyle("-fx-background-color: #0073e6; -fx-text-fill: white; -fx-font-size: 14px;");
        homeButton.setOnAction(e -> new application.HomePage().start(primaryStage));

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
        formPane.add(meetingLinkLabel, 0, 8);
        formPane.add(meetingLinkField, 1, 8);
        formPane.add(notesLabel, 0, 9);
        formPane.add(notesArea, 1, 9);
        formPane.add(buttonBox, 1, 10);

        return new VBox(formPane);
    }

    private static void clearFormFields() {
        interviewerField.clear();
        intervieweeField.clear();
        typeComboBox.getSelectionModel().clearSelection();
        datePicker.setValue(null);
        timeField.clear();
        topicField.clear();
        roleField.clear();
        durationField.clear();
        meetingLinkField.clear();
        notesArea.clear();
    }

    public static Text createHeading() {
        Text heading = new Text("Interview Scheduler");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        heading.setStyle("-fx-fill: linear-gradient(from 0% 0% to 100% 100%, #0073e6, #00c4ff);");
        return heading;
    }
}
