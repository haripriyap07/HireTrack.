package com.hiretrack.hiretrack.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class ResumeMatchingService {

    private static final Set<String> STOP_WORDS = Set.of(
            "and", "or", "the", "with", "for", "to", "of", "in", "a", "an",
            "on", "at", "is", "are", "be", "as", "by", "from", "this", "that",
            "job", "role", "work", "experience", "years", "year", "required", "skills"
    );

    private final ResumeTextExtractorService textExtractor;

    public ResumeMatchingService(ResumeTextExtractorService textExtractor) {
        this.textExtractor = textExtractor;
    }

    public MatchResult calculate(java.nio.file.Path resumePath, String requiredSkills, String jobDescription) {
        if (resumePath == null) {
            return MatchResult.empty();
        }

        try {
            String resumeText = textExtractor.extract(resumePath);
            Set<String> required = extractSkills(requiredSkills, jobDescription);
            if (required.isEmpty() || resumeText.isBlank()) {
                return MatchResult.empty();
            }

            String normalizedResume = normalize(resumeText);
            List<String> matched = new ArrayList<>();
            List<String> missing = new ArrayList<>();

            for (String skill : required) {
                if (containsSkill(normalizedResume, skill)) {
                    matched.add(skill);
                } else {
                    missing.add(skill);
                }
            }

            double score = Math.round((matched.size() * 10000.0) / required.size()) / 100.0;
            return new MatchResult(score, matched, missing);
        } catch (Exception ex) {
            return MatchResult.empty();
        }
    }

    private Set<String> extractSkills(String skills, String description) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        addSkillTokens(result, skills);
        if (result.isEmpty()) {
            addSkillTokens(result, description);
        }
        return result;
    }

    private void addSkillTokens(Set<String> result, String source) {
        if (source == null || source.isBlank()) return;
        Arrays.stream(source.split("[,;|\\n\\r]+"))
                .map(String::trim)
                .map(this::normalize)
                .filter(token -> token.length() >= 2)
                .filter(token -> !STOP_WORDS.contains(token))
                .limit(40)
                .forEach(result::add);
    }

    private boolean containsSkill(String resume, String skill) {
        String needle = normalize(skill);
        if (needle.isBlank()) return false;
        if (resume.contains(needle)) return true;

        // Also support common variations such as springboot/spring boot and nodejs/node.js.
        String compactResume = resume.replace(" ", "");
        String compactSkill = needle.replace(" ", "").replace(".", "");
        return compactResume.contains(compactSkill);
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9+#. ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    public record MatchResult(double score, List<String> matchedSkills, List<String> missingSkills) {
        public static MatchResult empty() {
            return new MatchResult(0.0, List.of(), List.of());
        }
    }
}
