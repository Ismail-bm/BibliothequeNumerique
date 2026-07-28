package com.bibliotheque.service;

import com.bibliotheque.model.Document;
import com.bibliotheque.model.Commentaire;
import com.bibliotheque.model.Recommmandation;
import com.bibliotheque.model.Categorie;
import java.util.List;

public interface IDocumentService {

    //  Documents ─────────────────────────────────────────
    void ajouterDocument(Document d) throws Exception;
    void modifierDocument(Document d) throws Exception;
    void supprimerDocument(Long id) throws Exception;
    Document findById(Long id) throws Exception;
    List<Document> listerTous() throws Exception;
    List<Document> listerDisponibles() throws Exception;

    //  Recherches
    List<Document> rechercherParTitre(String titre) throws Exception;
    List<Document> rechercherParAuteur(String auteur) throws Exception;
    List<Document> rechercherParCategorie(Long categorieId) throws Exception;
    List<Document> rechercherParMotCle(String motCle) throws Exception;
    List<Document> rechercherParPlusieursMots(String recherche) throws Exception;

    // Commentaires 
    void ajouterCommentaire(Commentaire c) throws Exception;
    void modifierCommentaire(Commentaire c) throws Exception;
    void supprimerCommentaire(Long id) throws Exception;
    List<Commentaire> getCommentairesDocument(Long documentId) throws Exception;
    List<Commentaire> getCommentairesUtilisateur(Long utilisateurId) throws Exception;
    double getNoteMoyenneDocument(Long documentId) throws Exception;
    void incrementerConsultation(Long documentId) throws Exception;

    //  Recommandations 
    void envoyerRecommandation(Recommmandation r) throws Exception;
    List<Recommmandation> getRecommandationsRecues(Long destinataireId) throws Exception;
    List<Recommmandation> getRecommandationsEnvoyees(Long expediteurId) throws Exception;

    //  Categories 
    void ajouterCategorie(Categorie c) throws Exception;
    List<Categorie> listerCategories() throws Exception;
    void modifierCategorie(Categorie c) throws Exception;
    void supprimerCategorie(Long id) throws Exception;
    void ajouterDocumentDansCategorie(Long categorieId, Long documentId) throws Exception;
    void retirerDocumentDeCategorie(Long categorieId, Long documentId) throws Exception;
}