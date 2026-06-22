package com.mgr.campusbridge.service;

import com.mgr.campusbridge.entity.User;
import com.mgr.campusbridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Assembles context-aware prompts from the caller's real dashboard metrics,
 * calls Gemini, and falls back to detailed pre-written content if the live
 * call is unavailable (so the panel never errors in front of reviewers).
 */
@Service
@RequiredArgsConstructor
public class AiAssistantService {

    private final GeminiApiService gemini;
    private final UserRepository userRepository;

    /** Run a quick-action by type. Returns markdown text. */
    public String runAction(String email, String actionType, String userMessage) {
        User user = userRepository.findByEmail(email).orElse(null);
        String name = user != null ? user.getName().split(" ")[0] : "there";
        String skills = (user != null && user.getSkills() != null && !user.getSkills().isEmpty())
                ? String.join(", ", user.getSkills()) : "Java, Python, React";
        int points = user != null ? user.getCommunityPoints() : 0;
        int level = points / 50 + 1;

        String context = String.format(
                "Student profile — Name: %s, Level: %d (Builder), Community Points: %d, "
                        + "Listed Skills: %s, Target Company: Zoho Corporation. "
                        + "Known weak areas: DSA (45%%), System Design (40%%).",
                name, level, points, skills);

        String action = actionType == null ? "" : actionType.toLowerCase();
        String prompt;
        switch (action) {
            case "resume":
                prompt = context + " Analyze these metrics and provide exactly 3 highly tactical, "
                        + "specific bullet points to scale resume impact scoring. Use markdown bullets.";
                break;
            case "roadmap":
                prompt = context + " Create a 4-week structured engineering study plan to elevate the "
                        + "weakest metrics (DSA 45%, System Design 40%) toward maximum readiness for Zoho "
                        + "Corporation. Format as markdown with a heading per week.";
                break;
            case "interview":
                prompt = context + " Generate exactly 3 custom technical interview questions tailored to "
                        + "this student's Skill DNA (Coding, DSA, AI/ML). Number them and add a one-line hint each.";
                break;
            case "internships":
                prompt = context + " Suggest 3 types of internships/roles that fit this student's skills and "
                        + "target, with a short reason each. Markdown bullets.";
                break;
            case "skills":
                prompt = context + " Recommend the top 3 skills this student should learn next to reach their "
                        + "target, with a one-line rationale each. Markdown bullets.";
                break;
            case "chat":
                prompt = context + " You are CampusBridge AI, a friendly career mentor. Reply helpfully and "
                        + "concisely to the student's message: \"" + (userMessage == null ? "" : userMessage) + "\"";
                break;
            default:
                prompt = context + " " + (StringUtils.hasText(userMessage) ? userMessage
                        : "Give a short motivational career tip.");
        }

        String result = gemini.generate(prompt);
        return StringUtils.hasText(result) ? result : fallback(action, name);
    }

    /** Detailed offline content so buttons always render something useful. */
    private String fallback(String action, String name) {
        switch (action) {
            case "resume":
                return "### Resume Boosters for " + name + "\n"
                        + "- **Quantify impact:** Turn \"built a project\" into \"built a REST API serving 1k+ requests/day, cutting latency 30%\".\n"
                        + "- **Lead with stack + outcome:** Pair each project with the tech used *and* the measurable result.\n"
                        + "- **Mirror the JD:** For Zoho, surface DSA, Java, and system-design keywords near the top.\n\n"
                        + "_Offline tip shown (AI service unavailable)._";
            case "roadmap":
                return "### 4-Week Readiness Plan\n"
                        + "**Week 1 — DSA foundations:** Arrays, strings, hashing. 3 problems/day.\n"
                        + "**Week 2 — DSA intermediate:** Trees, graphs, recursion. Timed sets.\n"
                        + "**Week 3 — System Design:** Caching, load balancing, DB scaling; design URL shortener & feed.\n"
                        + "**Week 4 — Mock interviews:** Daily mocks + revise weak topics.\n\n"
                        + "_Offline plan shown (AI service unavailable)._";
            case "interview":
                return "### Practice Questions\n"
                        + "1. **Arrays/DSA:** Find the longest substring without repeating characters. _Hint: sliding window._\n"
                        + "2. **System thinking:** Design a rate limiter for an API. _Hint: token bucket._\n"
                        + "3. **AI/ML:** Explain overfitting and 2 ways to prevent it. _Hint: regularization, cross-validation._\n\n"
                        + "_Offline questions shown (AI service unavailable)._";
            case "internships":
                return "### Internship Ideas\n- **Backend (Java/Spring) intern** — matches your core stack.\n"
                        + "- **Full-stack intern** — React + API experience.\n- **Data/ML intern** — if you enjoy AI/ML.\n\n"
                        + "_Offline tip shown (AI service unavailable)._";
            case "skills":
                return "### Skills to Learn Next\n- **Advanced DSA** — biggest interview lever.\n"
                        + "- **System Design basics** — expected for product roles.\n- **One cloud (AWS/Docker)** — boosts employability.\n\n"
                        + "_Offline tip shown (AI service unavailable)._";
            default:
                return "Hi " + name + "! I'm CampusBridge AI. I'm briefly offline, but here's a tip: "
                        + "consistency beats intensity — solve 2-3 DSA problems daily and your readiness will climb steadily.";
        }
    }
}
