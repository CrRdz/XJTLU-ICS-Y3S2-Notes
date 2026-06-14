package com.cpt202.util;

import com.cpt202.model.entity.Project;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Lightweight fuzzy scorer for student project search results.
 */
public final class ProjectSearchRelevanceScorer {

    private static final double TITLE_WEIGHT = 4.0;
    private static final double TOPIC_WEIGHT = 2.5;
    private static final double SKILL_WEIGHT = 2.0;
    private static final double DESCRIPTION_WEIGHT = 1.0;

    private ProjectSearchRelevanceScorer() {
    }

    public static double score(Project project, String keyword) {
        String query = normalize(keyword);
        if (project == null || query.isEmpty()) {
            return 0.0;
        }

        List<String> queryTokens = tokenize(query);
        if (queryTokens.isEmpty()) {
            return 0.0;
        }

        return scoreField(project.getTitle(), query, queryTokens, TITLE_WEIGHT)
                + scoreField(project.getTopicArea(), query, queryTokens, TOPIC_WEIGHT)
                + scoreField(project.getRequiredSkills(), query, queryTokens, SKILL_WEIGHT)
                + scoreField(project.getDescription(), query, queryTokens, DESCRIPTION_WEIGHT);
    }

    private static double scoreField(String field, String query, List<String> queryTokens, double weight) {
        String normalizedField = normalize(field);
        if (normalizedField.isEmpty()) {
            return 0.0;
        }

        List<String> fieldTokens = tokenize(normalizedField);
        if (fieldTokens.isEmpty()) {
            return 0.0;
        }

        Set<String> fieldTokenSet = new LinkedHashSet<>(fieldTokens);
        double score = 0.0;
        if (normalizedField.equals(query)) {
            score += weight * 4.0;
        } else if (normalizedField.contains(query)) {
            score += weight * 3.0;
        }

        for (String queryToken : queryTokens) {
            if (fieldTokenSet.contains(queryToken)) {
                score += weight * 1.5;
                continue;
            }

            double similarity = bestTokenSimilarity(queryToken, fieldTokenSet);
            if (similarity >= minimumSimilarity(queryToken)) {
                score += weight * similarity;
            }
        }
        return score;
    }

    private static double bestTokenSimilarity(String queryToken, Set<String> fieldTokens) {
        double best = 0.0;
        for (String fieldToken : fieldTokens) {
            if (Math.abs(queryToken.length() - fieldToken.length()) > Math.max(3, queryToken.length() / 2)) {
                continue;
            }
            int distance = levenshteinDistance(queryToken, fieldToken);
            int maxLength = Math.max(queryToken.length(), fieldToken.length());
            double similarity = maxLength == 0 ? 0.0 : 1.0 - ((double) distance / maxLength);
            if (similarity > best) {
                best = similarity;
            }
        }
        return best;
    }

    private static double minimumSimilarity(String token) {
        return token.length() <= 3 ? 0.86 : 0.72;
    }

    private static int levenshteinDistance(String left, String right) {
        int[] previous = new int[right.length() + 1];
        int[] current = new int[right.length() + 1];

        for (int j = 0; j <= right.length(); j++) {
            previous[j] = j;
        }

        for (int i = 1; i <= left.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= right.length(); j++) {
                int substitution = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(
                        Math.min(current[j - 1] + 1, previous[j] + 1),
                        previous[j - 1] + substitution);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }

        return previous[right.length()];
    }

    private static List<String> tokenize(String text) {
        String normalized = normalize(text);
        List<String> tokens = new ArrayList<>();
        if (normalized.isEmpty()) {
            return tokens;
        }
        for (String token : normalized.split(" ")) {
            if (!token.isBlank()) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
    }
}
