package com.bibliotheque.service;

import com.bibliotheque.dao.DocumentImpl;
import com.bibliotheque.dao.CommentaireImpl;
import com.bibliotheque.dao.RecommandationImpl;
import com.bibliotheque.dao.CategorieImpl;
import com.bibliotheque.model.Document;
import com.bibliotheque.model.Commentaire;
import com.bibliotheque.model.Recommmandation;
import com.bibliotheque.model.Categorie;

import java.util.List;
import java.util.Optional;

public class DocumentService implements IDocumentService {

   
    private DocumentImpl    documentDao  = new DocumentImpl();
    private CommentaireImpl commentaireDao = new CommentaireImpl();
    private RecommandationImpl recommandationDao = new RecommandationImpl();
    private CategorieImpl categorieDao = new CategorieImpl();

   
    // AJOUTER DOCUMENT — BIBLIOTHECAIRE
  
    @Override
    public void ajouterDocument(Document d) throws Exception {

        if (d.getTitre() == null || d.getTitre().trim().isEmpty())
            throw new Exception("Le titre est obligatoire !");

        if (d.getAuteur() == null || d.getAuteur().trim().isEmpty())
            throw new Exception("L'auteur est obligatoire !");

        if (d.getFormat() == null)
            throw new Exception("Le format est obligatoire !");

        if (d.getCheminFichier() == null || d.getCheminFichier().trim().isEmpty())
            throw new Exception("Le chemin du fichier est obligatoire !");

        // valeurs par defaut
        d.setDisponible(true);
        d.setNbrConsultations(0);

        documentDao.save(d);
    }

    // MODIFIER DOCUMENT — BIBLIOTHECAIRE
    @Override
    public void modifierDocument(Document d) throws Exception {

        if (documentDao.findById(d.getId()).isEmpty())
            throw new Exception("Document introuvable !");

        if (d.getTitre() == null || d.getTitre().trim().isEmpty())
            throw new Exception("Le titre est obligatoire !");

        if (d.getAuteur() == null || d.getAuteur().trim().isEmpty())
            throw new Exception("L'auteur est obligatoire !");

        documentDao.update(d);
    }

   
    // SUPPRIMER DOCUMENT — BIBLIOTHECAIRE
   
    @Override
    public void supprimerDocument(Long id) throws Exception {

        if (id == null || id <= 0)
            throw new Exception("ID invalide !");

        Optional<Document> opt = documentDao.findById(id);
        if (opt.isEmpty())
            throw new Exception("Document introuvable !");

        // regle : document disponible ? (pas en cours d'emprunt)
        if (!opt.get().isDisponible())
            throw new Exception("Impossible : document actuellement emprunte !");

        documentDao.delete(id);
    }

    // FIND BY ID — TOUS
    
    @Override
    public Document findById(Long id) throws Exception {

        if (id == null || id <= 0)
            throw new Exception("ID invalide !");

        Optional<Document> opt = documentDao.findById(id);
        if (opt.isEmpty())
            throw new Exception("Document introuvable ID = " + id);

        return opt.get();
    }

   
    // LISTER TOUS — TOUS
   
    @Override
    public List<Document> listerTous() throws Exception {

        List<Document> liste = documentDao.findAll();

        if (liste.isEmpty())
            throw new Exception("Aucun document trouve !");

        return liste;
    }

 
    // LISTER DISPONIBLES — TOUS
    // utilise findDisponibles() → WHERE disponible = TRUE
   
    @Override
    public List<Document> listerDisponibles() throws Exception {

        List<Document> liste = documentDao.findDisponibles();

        if (liste.isEmpty())
            throw new Exception("Aucun document disponible !");

        return liste;
    }

    
    // RECHERCHER PAR TITRE — TOUS
    
    
    @Override
    public List<Document> rechercherParTitre(String titre) throws Exception {

        if (titre == null || titre.trim().isEmpty())
            throw new Exception("Le titre est obligatoire !");

        List<Document> liste = documentDao.findByTitre(titre);

        if (liste.isEmpty())
            throw new Exception("Aucun document trouve avec ce titre !");

        return liste;
    }

    // RECHERCHER PAR AUTEUR — TOUS
    
    @Override
    public List<Document> rechercherParAuteur(String auteur) throws Exception {

        if (auteur == null || auteur.trim().isEmpty())
            throw new Exception("L'auteur est obligatoire !");

        List<Document> liste = documentDao.findByAuteur(auteur);

        if (liste.isEmpty())
            throw new Exception("Aucun document trouve pour cet auteur !");

        return liste;
    }

 
    // RECHERCHER PAR CATEGORIE — TOUS
    
   
    @Override
    public List<Document> rechercherParCategorie(Long categorieId) throws Exception {

        if (categorieId == null || categorieId <= 0)
            throw new Exception("ID categorie invalide !");

        if (categorieDao.findById(categorieId).isEmpty())
            throw new Exception("Categorie introuvable !");

        List<Document> liste = documentDao.findByCategorie(categorieId);

        if (liste.isEmpty())
            throw new Exception("Aucun document dans cette categorie !");

        return liste;
    }

   
    // RECHERCHER PAR MOT CLE — TOUS
   
    @Override
    public List<Document> rechercherParMotCle(String motCle) throws Exception {

        if (motCle == null || motCle.trim().isEmpty())
            throw new Exception("Le mot cle est obligatoire !");

        List<Document> liste = documentDao.findByMotsCles(motCle);

        if (liste.isEmpty())
            throw new Exception("Aucun document trouve avec ce mot cle !");

        return liste;
    }


    // RECHERCHER PAR PLUSIEURS MOTS — TOUS
    
    @Override
    public List<Document> rechercherParPlusieursMots(String recherche) throws Exception {

        if (recherche == null || recherche.trim().isEmpty())
            throw new Exception("La recherche est obligatoire !");

        List<Document> liste = documentDao.findByPlusieursMots(recherche);

        if (liste.isEmpty())
            throw new Exception("Aucun document trouve pour : " + recherche);

        return liste;
    }

    
    // AJOUTER COMMENTAIRE — TOUS LES ROLES
   
    @Override
    public void ajouterCommentaire(Commentaire c) throws Exception {

        if (c.getContenu() == null || c.getContenu().trim().isEmpty())
            throw new Exception("Le commentaire ne peut pas etre vide !");

        if (c.getNote() < 1 || c.getNote() > 5)
            throw new Exception("La note doit etre entre 1 et 5 !");

        if (c.getDocument() == null)
            throw new Exception("Document obligatoire !");

        if (documentDao.findById(c.getDocument().getId()).isEmpty())
            throw new Exception("Document introuvable !");

        if (c.getUtilisateur() == null)
            throw new Exception("Utilisateur obligatoire !");

        commentaireDao.save(c);
    }

    // =========================================================
    // MODIFIER COMMENTAIRE — TOUS
    
    @Override
    public void modifierCommentaire(Commentaire c) throws Exception {

        if (commentaireDao.findById(c.getId()).isEmpty())
            throw new Exception("Commentaire introuvable !");

        if (c.getContenu() == null || c.getContenu().trim().isEmpty())
            throw new Exception("Le commentaire ne peut pas etre vide !");

        if (c.getNote() < 1 || c.getNote() > 5)
            throw new Exception("La note doit etre entre 1 et 5 !");

        commentaireDao.update(c);
    }

    // SUPPRIMER COMMENTAIRE — TOUS
    @Override
    public void supprimerCommentaire(Long id) throws Exception {

        if (id == null || id <= 0)
            throw new Exception("ID invalide !");

        if (commentaireDao.findById(id).isEmpty())
            throw new Exception("Commentaire introuvable !");

        commentaireDao.delete(id);
    }

    // COMMENTAIRES D'UN DOCUMENT — TOUS
   
    @Override
    public List<Commentaire> getCommentairesDocument(Long documentId) throws Exception {

        if (documentId == null || documentId <= 0)
            throw new Exception("ID document invalide !");

        if (documentDao.findById(documentId).isEmpty())
            throw new Exception("Document introuvable !");

        return commentaireDao.findByDocument(documentId);
    }

    // COMMENTAIRES D'UN UTILISATEUR — TOUS
    
    @Override
    public List<Commentaire> getCommentairesUtilisateur(Long utilisateurId) throws Exception {

        if (utilisateurId == null || utilisateurId <= 0)
            throw new Exception("ID utilisateur invalide !");

        List<Commentaire> liste = commentaireDao.findByUtilisateur(utilisateurId);

        if (liste.isEmpty())
            throw new Exception("Aucun commentaire trouve pour cet utilisateur !");

        return liste;
    }

    // NOTE MOYENNE D'UN DOCUMENT — TOUS
    
    @Override
    public double getNoteMoyenneDocument(Long documentId) throws Exception {

        if (documentId == null || documentId <= 0)
            throw new Exception("ID document invalide !");

        if (documentDao.findById(documentId).isEmpty())
            throw new Exception("Document introuvable !");

        return commentaireDao.getNoteMoyenne(documentId);
    }

    @Override
    public void incrementerConsultation(Long documentId) throws Exception {

        if (documentId == null || documentId <= 0)
            throw new Exception("ID document invalide !");

        if (documentDao.findById(documentId).isEmpty())
            throw new Exception("Document introuvable !");

        documentDao.incrementerConsultation(documentId);
    }

    
    // ENVOYER RECOMMANDATION 
    
    @Override
    public void envoyerRecommandation(Recommmandation r) throws Exception {

        if (r.getExpediteur() == null)
            throw new Exception("Expediteur obligatoire !");

        if (r.getDestinataire() == null)
            throw new Exception("Destinataire obligatoire !");

        if (r.getExpediteur().getId().equals(r.getDestinataire().getId()))
            throw new Exception("Impossible de vous recommander a vous-meme !");

        if (r.getDocument() == null)
            throw new Exception("Document obligatoire !");

        if (documentDao.findById(r.getDocument().getId()).isEmpty())
            throw new Exception("Document introuvable !");

        if (r.getMessage() == null || r.getMessage().trim().isEmpty())
            throw new Exception("Le message est obligatoire !");

        recommandationDao.save(r);
    }

    // RECOMMANDATIONS RECUES — TOUS

    @Override
    public List<Recommmandation> getRecommandationsRecues(Long destinataireId)
            throws Exception {

        if (destinataireId == null || destinataireId <= 0)
            throw new Exception("ID invalide !");

        List<Recommmandation> liste =
            recommandationDao.findByDestinataire(destinataireId);

        if (liste.isEmpty())
            throw new Exception("Aucune recommandation recue !");

        return liste;
    }

    // RECOMMANDATIONS ENVOYEES — TOUS
   
    @Override
    public List<Recommmandation> getRecommandationsEnvoyees(Long expediteurId)
            throws Exception {

        if (expediteurId == null || expediteurId <= 0)
            throw new Exception("ID invalide !");

        List<Recommmandation> liste =
            recommandationDao.findByExpediteur(expediteurId);

        if (liste.isEmpty())
            throw new Exception("Aucune recommandation envoyee !");

        return liste;
    }

    // AJOUTER CATEGORIE — BIBLIOTHECAIRE


    @Override
    public void ajouterCategorie(Categorie c) throws Exception {

        if (c.getNom() == null || c.getNom().trim().isEmpty())
            throw new Exception("Le nom de la categorie est obligatoire !");

        if (categorieDao.findByNom(c.getNom()).isPresent())
            throw new Exception("Cette categorie existe deja !");

        categorieDao.save(c);
    }

   
    // LISTER CATEGORIES — TOUS
    @Override
    public List<Categorie> listerCategories() throws Exception {

        List<Categorie> liste = categorieDao.findAll();

        if (liste.isEmpty())
            throw new Exception("Aucune categorie trouvee !");

        return liste;
    }

    // AJOUTER DOCUMENT DANS CATEGORIE — BIBLIOTHECAIRE
   
    @Override
    public void ajouterDocumentDansCategorie(Long categorieId, Long documentId)
            throws Exception {

        if (categorieDao.findById(categorieId).isEmpty())
            throw new Exception("Categorie introuvable !");

        if (documentDao.findById(documentId).isEmpty())
            throw new Exception("Document introuvable !");

        categorieDao.ajouterDocument(categorieId, documentId);
    }

    // RETIRER DOCUMENT DE CATEGORIE — BIBLIOTHECAIRE

    @Override
    public void retirerDocumentDeCategorie(Long categorieId, Long documentId)
            throws Exception {

        if (categorieDao.findById(categorieId).isEmpty())
            throw new Exception("Categorie introuvable !");

        if (documentDao.findById(documentId).isEmpty())
            throw new Exception("Document introuvable !");

        categorieDao.retirerDocument(categorieId, documentId);
    }

    // MODIFIER CATEGORIE — BIBLIOTHECAIRE
    @Override
    public void modifierCategorie(Categorie c) throws Exception {

        if (c.getId() == null || c.getId() <= 0)
            throw new Exception("ID invalide !");

        if (categorieDao.findById(c.getId()).isEmpty())
            throw new Exception("Categorie introuvable !");

        if (c.getNom() == null || c.getNom().trim().isEmpty())
            throw new Exception("Le nom de la categorie est obligatoire !");

        categorieDao.update(c);
    }

    // SUPPRIMER CATEGORIE — BIBLIOTHECAIRE
    @Override
    public void supprimerCategorie(Long id) throws Exception {

        if (id == null || id <= 0)
            throw new Exception("ID invalide !");

        if (categorieDao.findById(id).isEmpty())
            throw new Exception("Categorie introuvable !");

        categorieDao.delete(id);
    }
}