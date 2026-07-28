package com.bibliotheque.service;

import com.bibliotheque.model.Penalite;
import java.util.List;

public interface IPenaliteService {
    void calculerPenalite(Long empruntId) throws Exception;
    void payerPenalite(Long penaliteId) throws Exception;
    Penalite getPenaliteParEmprunt(Long empruntId) throws Exception;
    List<Penalite> getMesPenalites(Long utilisateurId) throws Exception;
    List<Penalite> getPenalitesNonPayees() throws Exception;
    List<Penalite> getToutesPenalites() throws Exception;
    double getTotalNonPaye(Long utilisateurId) throws Exception;
}