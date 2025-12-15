package shared;

import java.io.Serializable;
import java.util.List;

public class Question implements Serializable {

    private String text;
    private List<String> options;
    private int correctIndex;

    private String category;

    public Question(String text, List<String> options, int correctIndex, String category) {
        this.text = text;
        this.options = options;
        this.correctIndex = correctIndex;
        this.category = category;
    }

    public String getText() {
        return text;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public String getCategory() {
        return category;
    }
}
