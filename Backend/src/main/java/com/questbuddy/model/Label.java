package com.questbuddy.model;

import jakarta.persistence.*;

/**
 * This class represents a user-owned label/tag used to categorize items in QuestBuddy (e.g., tasks).
 *
 *  - Each label belongs to exactly one user and its name is unique per user
 *  - Enables filtering, grouping, and color-coded UI chips (e.g., “show all tasks with label Travel”)
 *    without bloating the Task table
 *  - The dedicated table and the (user_id, name) uniqueness constraint preserve data integrity,
 *    ensure labels can’t exist without an owning user, and keep lookups efficient as data grows.
 */

/**
 * User-owned label/tag entity.
 *
 * Uniqueness: a user cannot have two labels with the same name (enforced via the composite UNIQUE constraint on (user_id, name)).
 *
 */
@Entity
@Table(
        name = "labels",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_label_user_name",
                columnNames = {"user_id", "name"}
        )
)
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owner of the label. Non-null by design
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Length capped to 40 to keep UI concise and indexes small and to make it human readable
    @Column(nullable = false, length = 40)
    private String name;

    // Length 16 provides room for prefixes and 8-digit hex
    @Column(length = 16)
    private String color;

    public Long getId() {
        return id; }

    public User getUser() {
        return user; }

    public void setUser(User user) {
        this.user = user; }

    public String getName() {
        return name; }

    public void setName(String name) {
        this.name = name; }

    public String getColor() {
        return color; }

    public void setColor(String color) {
        this.color = color; }
}
