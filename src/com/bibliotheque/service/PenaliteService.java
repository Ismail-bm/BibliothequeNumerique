package com.bibliotheque.service;

import com.bibliotheque.dao.PenaliteImpl;
import com.bibliotheque.dao.EmpruntImpl;
import com.bibliotheque.dao.UtilisateurDAOImpl;
import com.bibliotheque.model.Penalite;
import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.StatutEnum;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PenaliteService implements IPenaliteService {

    private PenaliteImpl penaliteDao = new PenaliteImpl();
    private EmpruntImpl   empruntDao = new EmpruntImpl();
    private UtilisateurDAOImpl utilisateurDao = new UtilisateurDAOImpl();

   
    // CALCULER PENALITE — BIBLIOTHECAIRE
    
    @Override
    public void calculerPenalite(Long empruntId) throws Exception {

        // regle 1 : emprunt existe ?
        Optional<Emprunt> opt = empruntDao.findById(empruntId);
        if (opt.isEmpty())
            throw new Exception("Emprunt introuvable !");

        Emprunt emprunt = opt.get();

        // regle 2 : emprunt en retard ?
        if (emprunt.getStatut() != StatutEnum.EN_RETARD)
            throw new Exception("Cet emprunt n'est pas en retard !");

        // regle 3 : penalite deja calculee ? → on recalcule
        Optional<Penalite> dejaPenalite = penaliteDao.findByEmprunt(empruntId);
        if (dejaPenalite.isPresent()) {
            if (dejaPenalite.get().isPayee())
                throw new Exception("Cette penalite est deja payee !");
            LocalDate dateRetourPrevue = emprunt.getDateRetourPreveue();
            LocalDate dateAujourdhui   = LocalDate.now();
            int joursRetard = (int) ChronoUnit.DAYS.between(dateRetourPrevue, dateAujourdhui);
            if (joursRetard <= 0) return;
            penaliteDao.delete(dejaPenalite.get().getId());
        }

        // calcul des jours de retard
        LocalDate dateRetourPrevue = emprunt.getDateRetourPreveue();
        LocalDate dateAujourdhui   = LocalDate.now();
        int joursRetard = (int) ChronoUnit.DAYS.between(dateRetourPrevue, dateAujourdhui);

        if (joursRetard <= 0)
            throw new Exception("Pas de retard detecte !");

        // creer la penalite : 50 MAD par jour
        Penalite p = new Penalite(empruntId, joursRetard);
        penaliteDao.save(p);
    }

    // =========================================================
    // PAYER PENALITE — TOUS LES ROLES
    // =========================================================
    @Override
    public void payerPenalite(Long penaliteId) throws Exception {

        // regle 1 : penalite existe ?
        Optional<Penalite> opt = penaliteDao.findById(penaliteId);
        if (opt.isEmpty())
            throw new Exception("Penalite introuvable !");

        // regle 2 : deja payee ?
        if (opt.get().isPayee())
            throw new Exception("Cette penalite est deja payee !");

        penaliteDao.marquerPayee(penaliteId);
    }

    // =========================================================
    // GET PENALITE PAR EMPRUNT
    // =========================================================
    @Override
    public Penalite getPenaliteParEmprunt(Long empruntId) throws Exception {

        if (empruntDao.findById(empruntId).isEmpty())
            throw new Exception("Emprunt introuvable !");

        Optional<Penalite> opt = penaliteDao.findByEmprunt(empruntId);
        if (opt.isEmpty())
            throw new Exception("Aucune penalite pour cet emprunt !");

        return opt.get();
    }

    // =========================================================
    // MES PENALITES — TOUS LES ROLES
    // =========================================================
    @Override
    public List<Penalite> getMesPenalites(Long utilisateurId) throws Exception {

        if (utilisateurDao.findById(utilisateurId).isEmpty())
            throw new Exception("Utilisateur introuvable !");

        // recuperer tous les emprunts de l'utilisateur
        List<Emprunt> emprunts = empruntDao.findByUtilisateur(utilisateurId);
        List<Penalite> mesPenalites = new ArrayList<>();

        for (Emprunt e : emprunts) {
            Optional<Penalite> p = penaliteDao.findByEmprunt(e.getId());
            p.ifPresent(mesPenalites::add);
        }

        if (mesPenalites.isEmpty())
            throw new Exception("Aucune penalite trouvee !");

        return mesPenalites;
    }

    
    // PENALITES NON PAYEES — BIBLIOTHECAIRE
    
    @Override
    public List<Penalite> getPenalitesNonPayees() throws Exception {

        List<Penalite> liste = penaliteDao.findNonPayees();

        if (liste.isEmpty())
            throw new Exception("Aucune penalite non payee !");

        return liste;
    }

    
    // TOUTES LES PENALITES — BIBLIOTHECAIRE
    
    @Override
    public List<Penalite> getToutesPenalites() throws Exception {

        List<Penalite> liste = penaliteDao.findAll();

        if (liste.isEmpty())
            throw new Exception("Aucune penalite trouvee !");

        return liste;
    }

   
    // TOTAL NON PAYE PAR UTILISATEUR — BIBLIOTHECAIRE
   
    @Override
    public double getTotalNonPaye(Long utilisateurId) throws Exception {

        if (utilisateurDao.findById(utilisateurId).isEmpty())
            throw new Exception("Utilisateur introuvable !");

        List<Emprunt> emprunts = empruntDao.findByUtilisateur(utilisateurId);
        double total = 0.0;

        for (Emprunt e : emprunts) {
            Optional<Penalite> p = penaliteDao.findByEmprunt(e.getId());
            if (p.isPresent() && !p.get().isPayee()) {
                total += p.get().getMontant();
            }
        }

        return total;
    }
}