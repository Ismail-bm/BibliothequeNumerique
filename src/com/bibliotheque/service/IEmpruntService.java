package com.bibliotheque.service;

import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.ListeLecture;
import com.bibliotheque.model.ListePartage;
import com.bibliotheque.model.Document;
import java.util.List;

public interface IEmpruntService {

    // ── Emprunts ──────────────────────────────────────────
    void emprunterDocument(Emprunt e) throws Exception;
    void retournerDocument(Long empruntId) throws Exception;
    Emprunt findById(Long empruntId) throws Exception;
    List<Emprunt> getMesEmprunts(Long utilisateurId) throws Exception;
    List<Emprunt> getMesEmpruntsEnCours(Long utilisateurId) throws Exception;
    List<Emprunt> getEmpruntsEnCours() throws Exception;
    List<Emprunt> getEmpruntsEnRetard() throws Exception;

    // ── Liste Lecture ─────────────────────────────────────
    void creerListeLecture(ListeLecture l) throws Exception;
    void modifierListeLecture(ListeLecture l) throws Exception;
    void supprimerListeLecture(Long id) throws Exception;
    void ajouterDocumentDansListe(Long listeId, Long documentId) throws Exception;
    void retirerDocumentDeListe(Long listeId, Long documentId) throws Exception;
    List<ListeLecture> getMesListesLecture(Long proprietaireId) throws Exception;
    List<ListeLecture> getListesPubliques() throws Exception;
    List<Document> getDocumentsDeListe(Long listeId) throws Exception;

    // ── Liste Partage ─────────────────────────────────────
    void partagerListe(ListePartage lp) throws Exception;
    List<ListePartage> getListesPartagees(Long destinataireId) throws Exception;
    ListeLecture getListeLectureById(Long listeId) throws Exception;
}