package com.bibliotheque.service;

public interface IAdminService {
    int getNombreUtilisateurs() throws Exception;
    int getNombreEmpruntsEnCours() throws Exception;
    int getNombreEmpruntsEnRetard() throws Exception;
    double getTotalPenalitesNonPayees() throws Exception;
    String getDocumentPlusEmprunte() throws Exception;
    String getUtilisateurPlusActif() throws Exception;
    int getNombreEmpruntsParMois(int mois, int annee) throws Exception;
}