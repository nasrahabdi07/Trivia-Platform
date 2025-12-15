package server;

import shared.Question;

import java.util.*;

public class QuestionBank {

    private static final Map<String, List<Question>> QUESTIONS_BY_CATEGORY = new HashMap<>();

    static {
        // General Knowledge
        addQuestion("General Knowledge", "Capital city of Kenya?",
                Arrays.asList("Nairobi", "Mombasa", "Kisumu", "Eldoret"), 0);
        addQuestion("General Knowledge", "How many continents are on Earth?", Arrays.asList("5", "6", "7", "8"), 2);
        addQuestion("General Knowledge", "What is the capital of France?",
                Arrays.asList("London", "Berlin", "Paris", "Madrid"), 2);
        addQuestion("General Knowledge", "What is the capital of Japan?",
                Arrays.asList("Tokyo", "Osaka", "Kyoto", "Nagoya"), 0);
        addQuestion("General Knowledge", "Language with most native speakers?",
                Arrays.asList("English", "Mandarin Chinese", "Spanish", "Hindi"), 1);

        // Science
        addQuestion("Science", "Which planet is known as the Red Planet?",
                Arrays.asList("Earth", "Mars", "Jupiter", "Saturn"), 1);
        addQuestion("Science", "Chemical symbol for Gold?", Arrays.asList("Au", "Ag", "Gd", "Go"), 0);
        addQuestion("Science", "Fastest land animal?", Arrays.asList("Cheetah", "Leopard", "Antelope", "Lion"), 0);
        addQuestion("Science", "What is the approximate value of PI?", Arrays.asList("2.14", "3.14", "3.41", "4.13"),
                1);
        addQuestion("Science", "How many sides does a hexagon have?", Arrays.asList("5", "6", "7", "8"), 1);
        addQuestion("Science", "What is the smallest prime number?", Arrays.asList("0", "1", "2", "3"), 2);
        addQuestion("Science", "Which element has the chemical symbol 'O'?",
                Arrays.asList("Gold", "Oxygen", "Osmium", "Oganesson"), 1);
        addQuestion("Science", "What is the largest mammal in the world?",
                Arrays.asList("Elephant", "Blue Whale", "Giraffe", "Polar Bear"), 1);
        addQuestion("Science", "What is the hardest natural substance?",
                Arrays.asList("Gold", "Iron", "Diamond", "Platinum"), 2);
        addQuestion("Science", "What is the speed of light?",
                Arrays.asList("300,000 km/s", "150,000 km/s", "450,000 km/s", "600,000 km/s"), 0);

        // History
        addQuestion("History", "Who wrote 'Romeo and Juliet'?",
                Arrays.asList("Charles Dickens", "Leo Tolstoy", "William Shakespeare", "Mark Twain"), 2);
        addQuestion("History", "What year did World War II end?", Arrays.asList("1943", "1944", "1945", "1946"), 2);

        // Geography
        addQuestion("Geography", "Largest ocean on Earth?", Arrays.asList("Atlantic", "Indian", "Pacific", "Arctic"),
                2);
        addQuestion("Geography", "Which country is home to the kangaroo?",
                Arrays.asList("New Zealand", "Australia", "South Africa", "Brazil"), 1);

        // Sports
        addQuestion("Sports", "How many players are on a soccer team?", Arrays.asList("9", "10", "11", "12"), 2);
    }

    private static void addQuestion(String category, String text, List<String> options, int correctIndex) {
        QUESTIONS_BY_CATEGORY.computeIfAbsent(category, k -> new ArrayList<>())
                .add(new Question(text, options, correctIndex, category));
    }

    public static List<Question> getAll() {
        List<Question> all = new ArrayList<>();
        for (List<Question> list : QUESTIONS_BY_CATEGORY.values()) {
            all.addAll(list);
        }
        return all;
    }

    public static List<Question> getByCategory(String category) {
        if (category == null || category.equals("Any") || !QUESTIONS_BY_CATEGORY.containsKey(category)) {
            return getAll();
        }
        return new ArrayList<>(QUESTIONS_BY_CATEGORY.get(category));
    }
}
