package client;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PlayerScore {
    private final StringProperty name = new SimpleStringProperty();
    private final IntegerProperty score = new SimpleIntegerProperty();
    private final DoubleProperty avgTime = new SimpleDoubleProperty();

    public PlayerScore(String name, int score) {
        this.name.set(name);
        this.score.set(score);
        this.avgTime.set(0.0);
    }

    public PlayerScore(String name, int score, double avgTime) {
        this.name.set(name);
        this.score.set(score);
        this.avgTime.set(avgTime);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String n) {
        name.set(n);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public int getScore() {
        return score.get();
    }

    public void setScore(int s) {
        score.set(s);
    }

    public IntegerProperty scoreProperty() {
        return score;
    }

    public double getAvgTime() {
        return avgTime.get();
    }

    public void setAvgTime(double t) {
        avgTime.set(t);
    }

    public DoubleProperty avgTimeProperty() {
        return avgTime;
    }
}