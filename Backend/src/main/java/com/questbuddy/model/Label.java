package com.questbuddy.model;

import jakarta.persistence.*;

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

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 40)
    private String name;

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
