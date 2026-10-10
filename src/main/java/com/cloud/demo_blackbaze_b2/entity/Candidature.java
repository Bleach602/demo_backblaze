package com.cloud.demo_blackbaze_b2.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "candidatures")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Candidature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(name = "nom_fichier", nullable = false)
    private String nomFichier;

    @Column(name = "cle_b2", nullable = false, unique = true, length = 500)
    private String cleB2;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long taille;

    @Column(name = "date_depot", nullable = false, updatable = false)
    private LocalDateTime dateDepot;

    @PrePersist
    void onCreate() {
        this.dateDepot = LocalDateTime.now();
    }
}