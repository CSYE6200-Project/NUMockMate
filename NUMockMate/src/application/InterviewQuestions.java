package application;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.sql.*;
import java.util.*;

public class InterviewQuestions extends Application {

    private static final String DB_URL = "jdbc:sqlite:numockmate.db";
    private ListView<Question> questionListView;
    private TextField newQuestionField;
    private ComboBox<String> questionTypeComboBox;
    private Label statusLabel;
    private Set<String> uniqueQuestions = new HashSet<>();
    private Map<String, List<String>> questionResponses = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Interview Questions Manager");

        initializeDatabase();

        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        Text heading = new Text("Interview Questions");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 40));
        heading.setStyle("-fx-fill: linear-gradient(from 0% 0% to 100% 100%, #0073e6, #00c4ff);");

        questionListView = new ListView<>();
        loadQuestionsFromDatabase();

        newQuestionField = new TextField();
        newQuestionField.setPromptText("Enter a new question...");
        questionTypeComboBox = new ComboBox<>();
        questionTypeComboBox.getItems().addAll("General", "Technical", "Behavioral");
        questionTypeComboBox.setValue("General");

        Button addButton = new Button("Add Question");
        Button deleteButton = new Button("Delete Selected");
        Button homeButton = new Button("Back to Home");

        Button[] buttons = { addButton, deleteButton, homeButton };
        for (Button button : buttons) {
            button.setStyle("-fx-background-color: #0073e6; -fx-text-fill: white; -fx-font-size: 14px;");
            button.setMinHeight(40);
        }

        addButton.setOnAction(e -> addQuestionToDatabase());
        deleteButton.setOnAction(e -> deleteSelectedQuestion());
        homeButton.setOnAction(e -> new HomePage().start(primaryStage));

        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: green;");

        HBox actionButtonsBox = new HBox(10, addButton, deleteButton);
        actionButtonsBox.setAlignment(Pos.CENTER);

        HBox addQuestionBox = new HBox(10, newQuestionField, questionTypeComboBox, actionButtonsBox);
        addQuestionBox.setAlignment(Pos.CENTER);
        addQuestionBox.setPadding(new Insets(10));

        VBox centerLayout = new VBox(20, questionListView, addQuestionBox, statusLabel);
        centerLayout.setPadding(new Insets(20));
        centerLayout.setAlignment(Pos.CENTER);

        BorderPane layout = new BorderPane();
        layout.setTop(new VBox(20, heading, homeButton));
        layout.setCenter(centerLayout);

        Scene scene = new Scene(layout, screenBounds.getWidth(), screenBounds.getHeight());
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS questions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    question_text TEXT UNIQUE,
                    question_type TEXT
                );
            """;
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addQuestionToDatabase() {
        String newQuestionText = newQuestionField.getText().trim();
        String questionType = questionTypeComboBox.getValue();
        if (!newQuestionText.isEmpty() && uniqueQuestions.add(newQuestionText)) {
            String insertSQL = "INSERT INTO questions (question_text, question_type) VALUES (?, ?)";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, newQuestionText);
                pstmt.setString(2, questionType);
                pstmt.executeUpdate();
                loadQuestionsFromDatabase();
                newQuestionField.clear();
                statusLabel.setText("Question added successfully!");
                questionResponses.put(newQuestionText, new ArrayList<>());
            } catch (SQLException e) {
                e.printStackTrace();
                statusLabel.setText("Error adding question.");
            }
        } else {
            statusLabel.setText("Please enter a unique question.");
        }
    }

    private void deleteSelectedQuestion() {
        Question selectedQuestion = questionListView.getSelectionModel().getSelectedItem();
        if (selectedQuestion != null) {
            String deleteSQL = "DELETE FROM questions WHERE question_text = ? AND question_type = ?";
            try (Connection conn = DriverManager.getConnection(DB_URL);
                 PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                pstmt.setString(1, selectedQuestion.getQuestionText());
                pstmt.setString(2, selectedQuestion.getQuestionType());
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    loadQuestionsFromDatabase();
                    statusLabel.setText("Question deleted successfully!");
                    uniqueQuestions.remove(selectedQuestion.getQuestionText());
                    questionResponses.remove(selectedQuestion.getQuestionText());
                } else {
                    statusLabel.setText("Error deleting question.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                statusLabel.setText("Error deleting question.");
            }
        } else {
            statusLabel.setText("Please select a question to delete.");
        }
    }

    private void loadQuestionsFromDatabase() {
        questionListView.getItems().clear();
        uniqueQuestions.clear();
        questionResponses.clear();
        String selectSQL = "SELECT question_text, question_type FROM questions";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {
            while (rs.next()) {
                String questionText = rs.getString("question_text");
                String questionType = rs.getString("question_type");
                Question question;
                switch (questionType) {
                    case "Technical":
                        question = new TechnicalQuestion(questionText);
                        break;
                    case "Behavioral":
                        question = new BehavioralQuestion(questionText);
                        break;
                    default:
                        question = new GeneralQuestion(questionText);
                }
                questionListView.getItems().add(question);
                uniqueQuestions.add(questionText);
                questionResponses.put(questionText, new ArrayList<>());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

interface QuestionManager {
    void addQuestion(Question question);
    void removeQuestion(Question question);
    List<Question> getAllQuestions();
}

abstract class Question {
    protected String questionText;

    public Question(String questionText) {
        this.questionText = questionText;
    }

    public abstract String getQuestionType();

    public String getQuestionText() {
        return questionText;
    }

    @Override
    public String toString() {
        return getQuestionType() + ": " + questionText;
    }
}

class GeneralQuestion extends Question {
    public GeneralQuestion(String questionText) {
        super(questionText);
    }

    @Override
    public String getQuestionType() {
        return "General";
    }
}

class TechnicalQuestion extends Question {
    public TechnicalQuestion(String questionText) {
        super(questionText);
    }

    @Override
    public String getQuestionType() {
        return "Technical";
    }
}

class BehavioralQuestion extends Question {
    public BehavioralQuestion(String questionText) {
        super(questionText);
    }

    @Override
    public String getQuestionType() {
        return "Behavioral";
    }
}