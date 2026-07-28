package com.bibliotheque.service;

import com.bibliotheque.dao.UtilisateurDAOImpl;
import com.bibliotheque.dao.EmpruntImpl;
import com.bibliotheque.dao.PenaliteImpl;
import com.bibliotheque.dao.DocumentImpl;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Penalite;
import com.bibliotheque.model.Document;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.model.StatutEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AdminService implements IAdminService {

    private UtilisateurDAOImpl utilisateurDao = new UtilisateurDAOImpl();
    private EmpruntImpl empruntDao = new EmpruntImpl();
    private PenaliteImpl penaliteDao = new PenaliteImpl();
    private DocumentImpl  documentDao  = new DocumentImpl();

    
    // NOMBRE TOTAL UTILISATEURS
   
    @Override
    public int getNombreUtilisateurs() throws Exception {
        return utilisateurDao.findAll().size();
    }

 
    // NOMBRE EMPRUNTS EN COURS
    
    @Override
    public int getNombreEmpruntsEnCours() throws Exception {
        return empruntDao.findByStatut(StatutEnum.EN_COURS).size();
    }

   
    // NOMBRE EMPRUNTS EN RETARD
  
    @Override
    public int getNombreEmpruntsEnRetard() throws Exception {
        return empruntDao.findByStatut(StatutEnum.EN_RETARD).size();
    }

  
    // TOTAL PENALITES NON PAYEES

    @Override
    public double getTotalPenalitesNonPayees() throws Exception {

        List<Penalite> nonPayees = penaliteDao.findNonPayees();
        double total = 0.0;

        for (Penalite p : nonPayees) {
            total += p.getMontant();
        }

        return total;
    }

  
    // DOCUMENT LE PLUS EMPRUNTE
   
    @Override
    public String getDocumentPlusEmprunte() throws Exception {

        List<Emprunt> tousEmprunts = empruntDao.findAll();

        if (tousEmprunts.isEmpty())
            throw new Exception("Aucun emprunt enregistre !");

        // compter les emprunts par document
        Map<Long, Integer> compteur = new HashMap<>();
        for (Emprunt e : tousEmprunts) {
            compteur.merge(e.getDocument().getId(), 1, Integer::sum);
        }

        // trouver le document avec le max
        Long maxDocId  = null;
        int  maxCount  = 0;
        for (Map.Entry<Long, Integer> entry : compteur.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount  = entry.getValue();
                maxDocId  = entry.getKey();
            }
        }

        Optional<Document> doc = documentDao.findById(maxDocId);
        if (doc.isEmpty())
            throw new Exception("Document introuvable !");

        return doc.get().getTitre() + " (" + maxCount + " emprunts)";
    }

    
    // UTILISATEUR LE PLUS ACTIF
   
    @Override
    public String getUtilisateurPlusActif() throws Exception {

        List<Emprunt> tousEmprunts = empruntDao.findAll();

        if (tousEmprunts.isEmpty())
            throw new Exception("Aucun emprunt enregistre !");

        // compter les emprunts par utilisateur
        Map<Long, Integer> compteur = new HashMap<>();
        for (Emprunt e : tousEmprunts) {
            compteur.merge(e.getUtilisateur().getId(), 1, Integer::sum);
        }

        // trouver l'utilisateur avec le max
        Long maxUserId = null;
        int  maxCount  = 0;
        for (Map.Entry<Long, Integer> entry : compteur.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount  = entry.getValue();
                maxUserId = entry.getKey();
            }
        }

        Optional<Utilisateur> user = utilisateurDao.findById(maxUserId);
        if (user.isEmpty())
            throw new Exception("Utilisateur introuvable !");

        return user.get().getNom() + " (" + maxCount + " emprunts)";
    }

  
    // NOMBRE EMPRUNTS PAR MOIS
   
    @Override
    public int getNombreEmpruntsParMois(int mois, int annee) throws Exception {

        if (mois < 1 || mois > 12)
            throw new Exception("Mois invalide ! (1-12)");

        if (annee < 2000)
            throw new Exception("Annee invalide !");

        List<Emprunt> tousEmprunts = empruntDao.findAll();
        int count = 0;

        for (Emprunt e : tousEmprunts) {
            if (e.getDateEmprunt().getMonthValue() == mois
                    && e.getDateEmprunt().getYear() == annee) {
                count++;
            }
        }

        return count;
    }
}