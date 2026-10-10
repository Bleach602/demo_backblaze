package com.cloud.demo_blackbaze_b2.service;

import com.cloud.demo_blackbaze_b2.entity.Candidature;
import com.cloud.demo_blackbaze_b2.repository.CandidatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CandidatureService {

    private final CandidatureRepository repo;
    private final StorageService storage;

    @Transactional
    public Candidature creer(String nom, MultipartFile fichier) throws IOException {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }
        if (fichier == null || fichier.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est obligatoire");
        }

        String key = storage.upload(fichier, "candidatures");

        Candidature c = Candidature.builder()
                .nom(nom.trim())
                .nomFichier(fichier.getOriginalFilename())
                .cleB2(key)
                .contentType(fichier.getContentType())
                .taille(fichier.getSize())
                .build();

        return repo.save(c);
    }

    public List<Candidature> lister() {
        return repo.findAllByOrderByDateDepotDesc();
    }

    public Candidature get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Candidature introuvable"));
    }

    public byte[] lireFichier(Long id) {
        Candidature c = get(id);
        return storage.download(c.getCleB2());
    }

    @Transactional
    public void supprimer(Long id) {
        Candidature c = get(id);
        storage.delete(c.getCleB2());
        repo.delete(c);
    }
}