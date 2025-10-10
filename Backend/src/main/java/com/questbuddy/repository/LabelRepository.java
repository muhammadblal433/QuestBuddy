package com.questbuddy.repository;

import com.questbuddy.model.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LabelRepository extends JpaRepository<Label, Long> {

    // This gives all labels that belong to a specific user
    // Good for "show my labels" screens or building filter dropdowns
    List<Label> findByUser_Id(Long userId);

    // Quick yes/no to see if a user already has a label with this name
    // (case-insensitive so "Travel" and "travel" count as the same)
    boolean existsByUser_IdAndNameIgnoreCase(Long userId, String name);
}