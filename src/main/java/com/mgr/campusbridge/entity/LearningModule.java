package com.mgr.campusbridge.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * A single lesson node on the "Learn Coding" roadmap.
 * Seeded at startup (see DataSeeder) and ordered by {@code orderIndex}.
 */
@Entity
@Table(name = "learning_modules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Sequential position on the path (1, 2, 3 ...). Unique. */
    @Column(name = "order_index", nullable = false, unique = true)
    private int orderIndex;

    @Column(nullable = false)
    private String title;

    /** Short lesson body (markdown/plain text) shown in the reader panel. */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** The exact task the student must solve ("Your Mission"). */
    @Column(columnDefinition = "TEXT")
    private String mission;

    /** Code pre-filled into the editor. */
    @Column(columnDefinition = "TEXT")
    private String starterCode;

    /** Expected stdout the submission must match (trimmed compare). */
    @Column(columnDefinition = "TEXT")
    private String expectedOutput;

    /** Judge0 language id (e.g. 62 = Java, 71 = Python3, 63 = JS). */
    private int languageId;

    /** Display language label. */
    private String language;
}
