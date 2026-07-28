package com.bibliotheque.service;

import com.bibliotheque.dao.EmpruntImpl;
import com.bibliotheque.dao.ListeLectureImpl;
import com.bibliotheque.dao.ListePartageImpl;
import com.bibliotheque.dao.DocumentImpl;
import com.bibliotheque.dao.UtilisateurDAOImpl;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.ListeLecture;
import com.bibliotheque.model.ListePartage;
import com.bibliotheque.model.Document;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.model.StatutEnum;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class EmpruntService implements IEmpruntService {

    // ── les DAO ───────────────────────────────────────────
    private EmpruntImpl   empruntDao      = new EmpruntImpl();
    private ListeLectureImpl  listeLectureDao = new ListeLectureImpl();
    private ListePartageImpl  listePartageDao = new ListePartageImpl();
    private DocumentImpl documentDao = new DocumentImpl();
    private UtilisateurDAOImpl utilisateurDao  = new UtilisateurDAOImpl();

    // =========================================================
    // EMPRUNTER DOCUMENT
   
    @Override
    public void emprunterDocument(Emprunt e) throws Exception {

        if (e.getUtilisateur() == null)
            throw new Exception("Utilisateur obligatoire !");

        Optional<Utilisateur> userOpt = utilisateurDao.findById(
                e.getUtilisateur().getId());
        if (userOpt.isEmpty())
            throw new Exception("Utilisateur introuvable !");

        Utilisateur u = userOpt.get();

        if (!u.isActif())
            throw new Exception("Compte desactive !");

        if (e.getDocument() == null)
            throw new Exception("Document obligatoire !");

        Optional<Document> docOpt = documentDao.findById(
                e.getDocument().getId());
        if (docOpt.isEmpty())
            throw new Exception("Document introuvable !");

        if (!docOpt.get().isDisponible())
            throw new Exception("Document non disponible !");

        long empruntsEnCours = empruntDao.countEnCours(u.getId());
        if (empruntsEnCours >= u.getMaxEmprunt())
            throw new Exception("Maximum d'emprunts atteint ! ("
                + empruntsEnCours + "/" + u.getMaxEmprunt() + ")");

        if (e.getDateRetourPreveue() == null)
            throw new Exception("La date de retour prevue est obligatoire !");

        Document doc = docOpt.get();
        doc.emprunter(); 
        documentDao.update(doc);

        e.setStatut(StatutEnum.EN_COURS);
        e.setDateEmprunt(LocalDate.now());

        empruntDao.save(e);
    }

    // RETOURNER DOCUMENT
    
    @Override
    public void retournerDocument(Long empruntId) throws Exception {

        // regle 1 : emprunt existe ?
        Optional<Emprunt> opt = empruntDao.findById(empruntId);
        if (opt.isEmpty())
            throw new Exception("Emprunt introuvable !");

        Emprunt emprunt = opt.get();

        if (emprunt.getStatut() == StatutEnum.RETOURNE)
            throw new Exception("Ce document est deja retourne !");

        
        emprunt.retourner();

        if (emprunt.estEnRetard()) {
            emprunt.setStatut(StatutEnum.EN_RETARD);
        }

        empruntDao.update(emprunt);

        Optional<Document> docOpt = documentDao.findById(
                emprunt.getDocument().getId());
        if (docOpt.isPresent()) {
            Document doc = docOpt.get();
            doc.rendre(); 
            documentDao.update(doc);
        }
    }

    // FIND BY ID
 
    @Override
    public Emprunt findById(Long empruntId) throws Exception {

        if (empruntId == null || empruntId <= 0)
            throw new Exception("ID invalide !");

        Optional<Emprunt> opt = empruntDao.findById(empruntId);
        if (opt.isEmpty())
            throw new Exception("Emprunt introuvable ID = " + empruntId);

        return opt.get();
    }

    // MES EMPRUNTS — TOUS LES ROLES
   
    @Override
    public List<Emprunt> getMesEmprunts(Long utilisateurId) throws Exception {

        if (utilisateurDao.findById(utilisateurId).isEmpty())
            throw new Exception("Utilisateur introuvable !");

        List<Emprunt> liste = empruntDao.findByUtilisateur(utilisateurId);

        if (liste.isEmpty())
            throw new Exception("Aucun emprunt trouve !");

        return liste;
    }

    // MES EMPRUNTS EN COURS — TOUS LES ROLES
    
    @Override
    public List<Emprunt> getMesEmpruntsEnCours(Long utilisateurId) throws Exception {

        if (utilisateurDao.findById(utilisateurId).isEmpty())
            throw new Exception("Utilisateur introuvable !");

        List<Emprunt> liste = empruntDao.findEnCours(utilisateurId);

        if (liste.isEmpty())
            throw new Exception("Aucun emprunt en cours !");

        return liste;
    }

    // TOUS LES EMPRUNTS EN COURS — BIBLIOTHECAIRE
  
    @Override
    public List<Emprunt> getEmpruntsEnCours() throws Exception {

        List<Emprunt> liste = empruntDao.findByStatut(StatutEnum.EN_COURS);

        if (liste.isEmpty())
            throw new Exception("Aucun emprunt en cours !");

        return liste;
    }

    // TOUS LES EMPRUNTS EN RETARD — BIBLIOTHECAIRE

    @Override
    public List<Emprunt> getEmpruntsEnRetard() throws Exception {

        return empruntDao.findEnRetard();
    }

    
    // CREER LISTE LECTURE
    
    @Override
    public void creerListeLecture(ListeLecture l) throws Exception {

        if (l.getNom() == null || l.getNom().trim().isEmpty())
            throw new Exception("Le nom de la liste est obligatoire !");

        if (l.getProprietaireId() == null)
            throw new Exception("Proprietaire obligatoire !");

        if (utilisateurDao.findById(l.getProprietaireId()).isEmpty())
            throw new Exception("Utilisateur introuvable !");

        listeLectureDao.save(l);
    }

    @Override
    public void modifierListeLecture(ListeLecture l) throws Exception {

        if (l.getId() == null || l.getId() <= 0)
            throw new Exception("ID de liste invalide !");

        if (listeLectureDao.findById(l.getId()).isEmpty())
            throw new Exception("Liste introuvable !");

        if (l.getNom() == null || l.getNom().trim().isEmpty())
            throw new Exception("Le nom de la liste est obligatoire !");

        listeLectureDao.update(l);
    }

    @Override
    public void supprimerListeLecture(Long id) throws Exception {

        if (id == null || id <= 0)
            throw new Exception("ID de liste invalide !");

        if (listeLectureDao.findById(id).isEmpty())
            throw new Exception("Liste introuvable !");

        listeLectureDao.delete(id);
    }

    
    // AJOUTER DOCUMENT DANS LISTE
 
    @Override
    public void ajouterDocumentDansListe(Long listeId, Long documentId)
            throws Exception {

        if (listeLectureDao.findById(listeId).isEmpty())
            throw new Exception("Liste de lecture introuvable !");

        if (documentDao.findById(documentId).isEmpty())
            throw new Exception("Document introuvable !");

        listeLectureDao.ajouterDocument(listeId, documentId);
    }

    // RETIRER DOCUMENT DE LISTE

    @Override
    public void retirerDocumentDeListe(Long listeId, Long documentId)
            throws Exception {

        if (listeLectureDao.findById(listeId).isEmpty())
            throw new Exception("Liste de lecture introuvable !");

        if (documentDao.findById(documentId).isEmpty())
            throw new Exception("Document introuvable !");

        listeLectureDao.retirerDocument(listeId, documentId);
    }

 
    // MES LISTES DE LECTURE
   
    @Override
    public List<ListeLecture> getMesListesLecture(Long proprietaireId)
            throws Exception {

        if (utilisateurDao.findById(proprietaireId).isEmpty())
            throw new Exception("Utilisateur introuvable !");

        return listeLectureDao.findByProprietaire(proprietaireId);
    }

    
    // LISTES PUBLIQUES — TOUS
    
    @Override
    public List<ListeLecture> getListesPubliques() throws Exception {

        List<ListeLecture> liste = listeLectureDao.findPubliques();

        if (liste.isEmpty())
            throw new Exception("Aucune liste publique trouvee !");

        return liste;
    }

 
    // DOCUMENTS D'UNE LISTE
 
    @Override
    public List<Document> getDocumentsDeListe(Long listeId) throws Exception {

        if (listeLectureDao.findById(listeId).isEmpty())
            throw new Exception("Liste de lecture introuvable !");

        return listeLectureDao.getDocuments(listeId);
    }

    // PARTAGER LISTE
    
    
    @Override
    public void partagerListe(ListePartage lp) throws Exception {

        if (lp.getListe() == null)
            throw new Exception("Liste obligatoire !");

        if (listeLectureDao.findById(lp.getListe().getId()).isEmpty())
            throw new Exception("Liste de lecture introuvable !");

        if (lp.getDestinnataire() == null)
            throw new Exception("Destinataire obligatoire !");

        if (utilisateurDao.findById(lp.getDestinnataire().getId()).isEmpty())
            throw new Exception("Destinataire introuvable !");

        // regle 5 : deja partage ?
        boolean dejaPartage = listePartageDao.existeDejaPartage(
                lp.getListe().getId(),
                lp.getDestinnataire().getId());
        if (dejaPartage)
            throw new Exception("Cette liste est deja partagee avec cet utilisateur !");

        lp.setDatePartage(LocalDate.now());

        listePartageDao.save(lp);
    }

    
    // LISTES PARTAGEES AVEC MOI
   
    @Override
    public List<ListePartage> getListesPartagees(Long destinataireId)
            throws Exception {

        if (utilisateurDao.findById(destinataireId).isEmpty())
            throw new Exception("Utilisateur introuvable !");

        return listePartageDao.findByDestinataire(destinataireId);
    }

    @Override
    public ListeLecture getListeLectureById(Long listeId) throws Exception {
        Optional<ListeLecture> opt = listeLectureDao.findById(listeId);
        return opt.orElse(null);
    }
}