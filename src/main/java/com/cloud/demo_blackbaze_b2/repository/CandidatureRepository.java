package com.cloud.demo_blackbaze_b2.repository;


import com.cloud.demo_blackbaze_b2.entity.Candidature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidatureRepository extends JpaRepository<Candidature, Long> {
    List<Candidature> findAllByOrderByDateDepotDesc();
}