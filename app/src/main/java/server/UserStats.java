package server;

public class UserStats {
    private int gamesPlayed;
    private int wins;
    private int totalScore;
    private int correctAnswers;
    private int totalQuestionsAnswered;
    private int gamesHosted;

    public UserStats() {
        this.gamesPlayed = 0;
        this.wins = 0;
        this.totalScore = 0;
        this.correctAnswers = 0;
        this.totalQuestionsAnswered = 0;
        this.gamesHosted = 0;
    }

    public void addGamePlayed() {
        this.gamesPlayed++;
    }

    public void addWin() {
        this.wins++;
    }

    public void addScore(int score) {
        this.totalScore += score;
    }

    public void addCorrectAnswer() {
        this.correctAnswers++;
    }

    public void addQuestionAnswered() {
        this.totalQuestionsAnswered++;
    }

    public void addGameHosted() {
        this.gamesHosted++;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getWins() {
        return wins;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public int getTotalQuestionsAnswered() {
        return totalQuestionsAnswered;
    }

    public int getGamesHosted() {
        return gamesHosted;
    }

    public double getAccuracy() {
        if (totalQuestionsAnswered == 0)
            return 0.0;
        return (double) correctAnswers / totalQuestionsAnswered * 100.0;
    }

    public int getAverageScore() {
        if (gamesPlayed == 0)
            return 0;
        return totalScore / gamesPlayed;
    }
}
