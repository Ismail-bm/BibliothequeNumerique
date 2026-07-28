package com.bibliotheque.service;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.model.RoleEnum;
import java.util.List;
public interface IUtilisateurService {
	 void inscrire(Utilisateur u) throws Exception;
	    Utilisateur connecter(String email, String motDePasse) throws Exception;
	    Utilisateur findById(Long id) throws Exception;
	    Utilisateur findByEmail(String email) throws Exception;
	    List<Utilisateur> listerTous() throws Exception;
	    void modifierProfil(Utilisateur u) throws Exception;
	    void activerDesactiver(Long id) throws Exception;
	    void changerRole(Long id, RoleEnum nouveauRole) throws Exception;
	    void supprimer(Long id) throws Exception;
}
