package com.cpt202.unit.util;

import com.cpt202.model.entity.Project;
import com.cpt202.util.ProjectSearchRelevanceScorer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectSearchRelevanceScorerTest {

    @Test
    void scoreShouldRewardTypoTolerantProjectMatches() {
        Project relevant = project("Machine Learning Recommendation System",
                "Build a recommender with ranking models",
                "Python, SQL",
                "Artificial Intelligence");
        Project weakMatch = project("Learning Management Dashboard",
                "Manage classroom content",
                "Vue",
                "Education");

        double relevantScore = ProjectSearchRelevanceScorer.score(relevant, "machne learning");
        double weakScore = ProjectSearchRelevanceScorer.score(weakMatch, "machne learning");

        assertThat(relevantScore).isGreaterThan(weakScore);
        assertThat(relevantScore).isPositive();
    }

    @Test
    void scoreShouldReturnZeroForUnrelatedProject() {
        Project unrelated = project("Library Seat Booking",
                "Reserve campus study space",
                "Vue",
                "Facilities");

        assertThat(ProjectSearchRelevanceScorer.score(unrelated, "neural network")).isZero();
    }

    private Project project(String title, String description, String requiredSkills, String topicArea) {
        Project project = new Project();
        project.setTitle(title);
        project.setDescription(description);
        project.setRequiredSkills(requiredSkills);
        project.setTopicArea(topicArea);
        return project;
    }
}
