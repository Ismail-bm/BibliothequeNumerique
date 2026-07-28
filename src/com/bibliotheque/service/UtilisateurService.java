package com.bibliotheque.service;

import com.bibliotheque.dao.UtilisateurDAOImpl;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.model.RoleEnum;

import java.util.List;
import java.util.Optional;

public class UtilisateurService implements IUtilisateurService {

    private UtilisateurDAOImpl dao = new UtilisateurDAOImpl();

    // INSCRIRE
    @Override
    public void inscrire(Utilisateur u) throws Exception {

        if (u.getNom() == null || u.getNom().trim().isEmpty())
            throw new Exception("Le nom est obligatoire !");

        if (u.getEmail() == null || u.getEmail().trim().isEmpty())
            throw new Exception("L'email est obligatoire !");

        if (u.getMotDepasse() == null || u.getMotDepasse().trim().isEmpty())
            throw new Exception("Le mot de passe est obligatoire !");

        if (u.getRole() == null)
            throw new Exception("Le role est obligatoire !");

        if (dao.existeEmail(u.getEmail()))
            throw new Exception("Email deja utilise !");

        // max_emprunts selon le role
        if      (u.getRole() == RoleEnum.ENSEIGNANT)       u.setMaxEmprunt(5);
        else if (u.getRole() == RoleEnum.BIBLIOTHECAIRE)   u.setMaxEmprunt(10);
        else                                               u.setMaxEmprunt(3);

        u.setActif(true);

        dao.save(u);
    }

    // CONNECTER
    @Override
    public Utilisateur connecter(String email, String motDePasse) throws Exception {

        if (email == null || email.trim().isEmpty())
            throw new Exception("Email obligatoire !");

        if (motDePasse == null || motDePasse.trim().isEmpty())
            throw new Exception("Mot de passe obligatoire !");

        Optional<Utilisateur> opt = dao.findByEmail(email);
        if (opt.isEmpty())
            throw new Exception("Email introuvable !");

        Utilisateur u = opt.get();

        if (!u.getMotDepasse().equals(motDePasse))
            throw new Exception("Mot de passe incorrect !");

        if (!u.isActif())
            throw new Exception("Ce compte est desactive !");

        return u;
    }

    // FIND BY ID
    @Override
    public Utilisateur findById(Long id) throws Exception {

        if (id == null || id <= 0)
            throw new Exception("ID invalide !");

        Optional<Utilisateur> opt = dao.findById(id);
        if (opt.isEmpty())
            throw new Exception("Utilisateur introuvable ID = " + id);

        return opt.get();
    }

    // FIND BY EMAIL
    @Override
    public Utilisateur findByEmail(String email) throws Exception {

        if (email == null || email.trim().isEmpty())
            throw new Exception("Email obligatoire !");

        Optional<Utilisateur> opt = dao.findByEmail(email);
        if (opt.isEmpty())
            throw new Exception("Aucun utilisateur avec cet email !");

        return opt.get();
    }

    // LISTER TOUS
    @Override
    public List<Utilisateur> listerTous() throws Exception {

        List<Utilisateur> liste = dao.findAll();

        if (liste.isEmpty())
            throw new Exception("Aucun utilisateur trouve !");

        return liste;
    }

    // MODIFIER PROFIL
    @Override
    public void modifierProfil(Utilisateur u) throws Exception {

        if (dao.findById(u.getId()).isEmpty())
            throw new Exception("Utilisateur introuvable !");

        if (u.getNom() == null || u.getNom().trim().isEmpty())
            throw new Exception("Le nom est obligatoire !");

        if (u.getEmail() == null || u.getEmail().trim().isEmpty())
            throw new Exception("L'email est obligatoire !");

        // email deja pris par un autre ?
        Optional<Utilisateur> emailExist = dao.findByEmail(u.getEmail());
        if (emailExist.isPresent() && !emailExist.get().getId().equals(u.getId()))
            throw new Exception("Email deja utilise par un autre utilisateur !");

        dao.update(u);
    }

    // ACTIVER / DESACTIVER
    
    @Override
    public void activerDesactiver(Long id) throws Exception {

        Optional<Utilisateur> opt = dao.findById(id);
        if (opt.isEmpty())
            throw new Exception("Utilisateur introuvable !");

        Utilisateur u = opt.get();
        u.setActif(!u.isActif());
        dao.update(u);
    }

    // CHANGER ROLE
    @Override
    public void changerRole(Long id, RoleEnum nouveauRole) throws Exception {

        Optional<Utilisateur> opt = dao.findById(id);
        if (opt.isEmpty())
            throw new Exception("Utilisateur introuvable !");

        if (nouveauRole == null)
            throw new Exception("Le role est obligatoire !");

        Utilisateur u = opt.get();
        u.setRole(nouveauRole);

        if      (nouveauRole == RoleEnum.ENSEIGNANT)       u.setMaxEmprunt(5);
        else if (nouveauRole == RoleEnum.BIBLIOTHECAIRE)   u.setMaxEmprunt(10);
        else                                               u.setMaxEmprunt(3);

        dao.update(u);
    }

    // SUPPRIMER
    
    @Override
    public void supprimer(Long id) throws Exception {

        if (id == null || id <= 0)
            throw new Exception("ID invalide !");

        Optional<Utilisateur> opt = dao.findById(id);
        if (opt.isEmpty())
            throw new Exception("Utilisateur introuvable !");

        if (opt.get().getRole() == RoleEnum.BIBLIOTHECAIRE)
            throw new Exception("Impossible de supprimer un BIBLIOTHECAIRE !");

        dao.delete(id);
    }
}