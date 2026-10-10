package com.cloud.demo_blackbaze_b2.controller;

import com.cloud.demo_blackbaze_b2.entity.Candidature;
import com.cloud.demo_blackbaze_b2.service.CandidatureService;
import com.cloud.demo_blackbaze_b2.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class CandidatureController {

    private final CandidatureService service;
    private final StorageService storage;

    /** Upload : nom + fichier → B2 + DB. */
    @PostMapping("/candidatures")
    public String creer(@RequestParam String nom,
                        @RequestParam("fichier") MultipartFile fichier,
                        RedirectAttributes ra) {
        try {
            service.creer(nom, fichier);
            ra.addFlashAttribute("success", "Candidature de " + nom + " enregistrée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/";
    }

    /** Affiche le fichier inline (images, PDF, vidéos). */
    @GetMapping("/candidatures/{id}/voir")
    public ResponseEntity<byte[]> voir(@PathVariable Long id) {
        Candidature c = service.get(id);
        byte[] data = service.lireFichier(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        c.getContentType() == null ? "application/octet-stream" : c.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + c.getNomFichier() + "\"")
                .body(data);
    }

    /** Téléchargement (attachment). */
    @GetMapping("/candidatures/{id}/telecharger")
    public ResponseEntity<byte[]> telecharger(@PathVariable Long id) {
        Candidature c = service.get(id);
        byte[] data = service.lireFichier(id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + c.getNomFichier() + "\"")
                .body(data);
    }

    /** Suppression (B2 + DB). */
    @PostMapping("/candidatures/{id}/supprimer")
    public String supprimer(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.supprimer(id);
            ra.addFlashAttribute("success", "Candidature supprimée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur suppression : " + e.getMessage());
        }
        return "redirect:/";
    }
}