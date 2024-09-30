package com.example.geoquiz_v4_sqlite;

public class UserResponse {
    private String questionUUID;
    private boolean userChoice;
    private boolean correctAnswer;
    private boolean cheated;

    public UserResponse(String questionUUID, boolean userChoice, boolean correctAnswer, boolean cheated) {
        this.questionUUID = questionUUID;
        this.userChoice = userChoice;
        this.correctAnswer = correctAnswer;
        this.cheated = cheated;
    }

    public String getQuestionUUID() {
        return questionUUID;
    }

    public boolean isUserChoice() {
        return userChoice;
    }

    public boolean isCorrectAnswer() {
        return correctAnswer;
    }

    public boolean isCheated() {
        return cheated;
    }
}
