package cpt204.collections;

import java.util.*;

interface Countable {
    int getKeywordCount();
}

public class KeywordCounter implements Countable {
    private String text;
    private HashSet<String> keywords;
    private HashMap<String, Integer> wordCounts;

    public KeywordCounter(String text, Collection<String> keywords) {
        this.text = text;
        this.keywords = new HashSet<>(keywords);
        this.wordCounts = new HashMap<>();
        countWords();
    }

    private void countWords() {
        if (text == null || text.isEmpty()) {
            return;
        }
        for (String word : text.split(" ")) {
            if (wordCounts.containsKey(word)) {
                wordCounts.put(word, wordCounts.get(word) + 1);
            } else {
                wordCounts.put(word, 1);
            }
        }
    }

    @Override
    public int getKeywordCount() {
        int total = 0;
        for (String keyword : keywords) {
            if (wordCounts.containsKey(keyword)) {
                total += wordCounts.get(keyword);
            }
        }
        return total;
    }
}
