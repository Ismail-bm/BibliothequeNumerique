package com.bibliotheque.dao;

import com.bibliotheque.dao.*;

import com.bibliotheque.model.*;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class EmpruntImpl implements IDao<Emprunt> {

    // Raccourci connexion
    private Connection conn() throws SQLException {
        return DatabaseConnection.getInstance();
    }
    

    // save INSERT INTO emprunts
    // Appele quand un utilisateur emprunte un document
    // On sauvegarde : qui, quoi, quand, date limite

    @Override
    public void save(Emprunt e) throws SQLException {

        String sql = "INSERT INTO emprunts "
                   + "(id_utilisateur, id_document, date_emprunt, "
                   + " date_retour_prevue, statut) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            // On stocke les IDs, 
            ps.setLong(1, e.getUtilisateur().getId());
            ps.setLong(2, e.getDocument().getId());

            // Conversion LocalDate → java.sql.Date (obligatoire pour JDBC)
            ps.setDate(3, Date.valueOf(e.getDateEmprunt()));
            ps.setDate(4, Date.valueOf(e.getDateRetourPreveue()));

            ps.setString(5, e.getStatut().name()); // enum → String

            ps.executeUpdate();

            // recuperation de l'id generee par my sql ;
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                e.setId(keys.getLong(1));
                System.out.println("Emprunt créé avec ID : " + e.getId());
            }
        }
    }


	@Override
	public Optional<Emprunt> findById(Long id) throws SQLException {

        String sql = "SELECT * FROM emprunts WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(map(rs));
            }
        }
        return Optional.empty();
	}


	@Override
	public List<Emprunt> findAll() throws SQLException {
        List<Emprunt> liste = new ArrayList<>();
        String sql = "SELECT * FROM emprunts ORDER BY date_emprunt DESC";

        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(map(rs));
            }
        }
        return liste;
	}
	
	
	

    // FIND BY UTILISATEUR → tous les emprunts d'une personne
    // Utilise pour afficher "Mes emprunts" dans l'interface

    public List<Emprunt> findByUtilisateur(Long utilisateurId)
            throws SQLException {

        List<Emprunt> liste = new ArrayList<>();
        String sql = "SELECT * FROM emprunts "
                   + "WHERE id_utilisateur = ? "
                   + "ORDER BY date_emprunt DESC";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, utilisateurId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(map(rs));
            }
        }
        return liste;
    }
    
    

    // FIND EN COURS → emprunts pas encore rendus
    // Utilise pour verifier la limite d emprunts
 
    public List<Emprunt> findEnCours(Long utilisateurId)
            throws SQLException {

        List<Emprunt> liste = new ArrayList<>();
        String sql = "SELECT * FROM emprunts "
                   + "WHERE id_utilisateur = ? "
                   + "AND statut = 'EN_COURS' "
                   + "ORDER BY date_retour_prevue ASC";
                   // ASC = du plus urgent au moins urgent

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, utilisateurId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(map(rs));
            }
        }
        return liste;
    }
    
    // COUNT EN COURS → combien d'emprunts actifs ?
    // Utilise pour verifier si la limite est atteinte
    // par exe etudiant max 3 alors  si count = 3  on  refuser
   
    public long countEnCours(Long utilisateurId) throws SQLException {

        String sql = "SELECT COUNT(*) FROM emprunts "
                   + "WHERE id_utilisateur = ? "
                   + "AND statut = 'EN_COURS'";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, utilisateurId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getLong(1); // retourne le nombre
            }
        }
        return 0;
    }
    

    // FIND BY STATUT : filtre par EN_COURS, RETOURNE, EN_RETARD
    // Utilise pour les notifications et rapports
   
    public List<Emprunt> findByStatut(StatutEnum statut)
            throws SQLException {

        List<Emprunt> liste = new ArrayList<>();
        String sql = "SELECT * FROM emprunts WHERE statut = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, statut.name()); // enum → String
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(map(rs));
            }
        }
        return liste;
    }
    
    

    // FIND EN RETARD → date_retour_prevue dépassée
    // Deux facons d'etre en retard :
    //   1. statut = EN_RETARD 
    //   2. statut = EN_COURS mais date depassee
    // On cherche les deux cas

    public List<Emprunt> findEnRetard() throws SQLException {

        List<Emprunt> liste = new ArrayList<>();
        String sql = "SELECT * FROM emprunts "
                   + "WHERE statut = 'EN_RETARD' "
                   + "OR (statut = 'EN_COURS' "
                   + "    AND date_retour_prevue < CURRENT_DATE)";

        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(map(rs));
            }
        }
        return liste;
    }



    // UPDATE → modifier le statut et la date de retour
    // Appelé quand :
    //   - on rend un document (statut → RETOURNE)
    //   - on détecte un retard (statut → EN_RETARD)
  
    @Override
    public void update(Emprunt e) throws SQLException {

        String sql = "UPDATE emprunts "
                   + "SET statut = ?, date_retour_reelle = ? "
                   + "WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, e.getStatut().name());

            // date_retour_reelle peut etre NULL si pas encore rendu
            if (e.getDateRetourReelle() != null) {
                ps.setDate(2, Date.valueOf(e.getDateRetourReelle()));
            } else {
                ps.setNull(2, Types.DATE); // NULL en SQL
            }

            ps.setLong(3, e.getId());
            ps.executeUpdate();
        }
    }


    // On prefere garder l'historique des emprunts attention ?

    @Override
    public void delete(Long id) throws SQLException {

        String sql = "DELETE FROM emprunts WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
    
    
    

    // MAP → convertit une ligne SQL en objet Emprunt
    //
    // IMPORTANT : on cree des objets Utilisateur et Document
     // avec juste leur ID.
    // Le Service chargera les objets complets si besoin.


    private Emprunt map(ResultSet rs) throws SQLException {

        Emprunt e = new Emprunt();
        e.setId(rs.getLong("id"));

        // Creer un Utilisateur avec  l'ID
        Utilisateur u = new Utilisateur();
        u.setId(rs.getLong("id_utilisateur"));
        e.setUtilisateur(u);

        // Creer un Document avec l'ID
        Document doc = new Document();
        doc.setId(rs.getLong("id_document"));
        e.setDocument(doc);

        // Conversion java.sql.Date → LocalDate
        e.setDateEmprunt(rs.getDate("date_emprunt").toLocalDate());
        e.setDateRetourPreveue(rs.getDate("date_retour_prevue").toLocalDate());
        

        // date_retour_reelle peut etre NULL on  verifier avant conversion
        Date retourReelle = rs.getDate("date_retour_reelle");
        if (retourReelle != null) {
            e.setDateRetourReelle(retourReelle.toLocalDate());
        }

        // Conversion String → enum
        e.setStatut(StatutEnum.valueOf(rs.getString("statut")));

        return e;
    }

} 
    
    
    
    
    
    
    

