package com.mgr.campusbridge.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "forum_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForumGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private boolean isPrivate;
    private int memberCount;

    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> members;
}