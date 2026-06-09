package com.mgr.campusbridge.config;

import com.mgr.campusbridge.entity.LearningModule;
import com.mgr.campusbridge.repository.LearningModuleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the "Learn Coding" roadmap once if it's empty. Lessons use Python
 * (Judge0 language id 71) so the starter tasks are short and demo-friendly.
 */
@Component
@RequiredArgsConstructor
public class LearningSeeder {

    private static final Logger log = LoggerFactory.getLogger(LearningSeeder.class);
    private static final int PYTHON = 71;

    private final LearningModuleRepository repository;

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        if (repository.count() > 0) return;
        log.info("[LearningSeeder] Seeding coding roadmap…");

        List<LearningModule> modules = List.of(
                LearningModule.builder()
                        .orderIndex(1).title("Introduction to Programming")
                        .language("Python").languageId(PYTHON)
                        .content("Welcome! Every program starts by printing output. In Python, `print()` writes text to the console. Strings are wrapped in quotes.")
                        .mission("Print exactly: Hello, CampusBridge!")
                        .starterCode("# Print the greeting\n")
                        .expectedOutput("Hello, CampusBridge!")
                        .build(),
                LearningModule.builder()
                        .orderIndex(2).title("Variables & Math")
                        .language("Python").languageId(PYTHON)
                        .content("Variables store values. You can do arithmetic with +, -, *, /. Print the result of a calculation.")
                        .mission("Compute 12 * 8 and print only the number.")
                        .starterCode("a = 12\nb = 8\n# print the product\n")
                        .expectedOutput("96")
                        .build(),
                LearningModule.builder()
                        .orderIndex(3).title("Arrays / Lists")
                        .language("Python").languageId(PYTHON)
                        .content("Lists hold ordered items. `sum(list)` adds numbers; `len(list)` counts them.")
                        .mission("Given nums = [4, 8, 15, 16, 23, 42], print their sum.")
                        .starterCode("nums = [4, 8, 15, 16, 23, 42]\n# print the sum\n")
                        .expectedOutput("108")
                        .build(),
                LearningModule.builder()
                        .orderIndex(4).title("String Manipulation")
                        .language("Python").languageId(PYTHON)
                        .content("Strings have handy methods. `.upper()` uppercases text, and slicing/reversing is common in interviews.")
                        .mission("Print the word 'campus' fully UPPERCASE.")
                        .starterCode("word = 'campus'\n# print it uppercase\n")
                        .expectedOutput("CAMPUS")
                        .build(),
                LearningModule.builder()
                        .orderIndex(5).title("Loops & Logic")
                        .language("Python").languageId(PYTHON)
                        .content("Loops repeat work. Use a for-loop with range() and an if-condition to filter values.")
                        .mission("Print the count of even numbers from 1 to 10 (inclusive).")
                        .starterCode("# count evens in 1..10 and print the count\n")
                        .expectedOutput("5")
                        .build()
        );
        repository.saveAll(modules);
        log.info("[LearningSeeder] Seeded {} lessons.", modules.size());
    }
}
