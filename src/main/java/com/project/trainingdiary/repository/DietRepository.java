package com.project.trainingdiary.repository;

import com.project.trainingdiary.entity.DietEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DietRepository extends JpaRepository<DietEntity, Long> {

}
