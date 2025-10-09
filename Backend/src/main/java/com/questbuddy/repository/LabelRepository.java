package com.questbuddy.repository;

import com.questbuddy.model.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LabelRepository extends JpaRepository<Label, Long> {
    List<Label> findByUser_Id(Long userId);
    boolean existsByUser_IdAndNameIgnoreCase(Long userId, String name);
}