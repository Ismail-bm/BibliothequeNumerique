package com.bibliotheque.dao;

import com.bibliotheque.dao.*;
import com.bibliotheque.model.*;

import java.sql.*;
import java.util.*;

public class CommentaireImpl implements IDao<Commentaire> {
	
    private Connection conn() throws SQLException {
        return DatabaseConnection.getInstance();
    }


    // SAVE : publier un commentaire  
    @Override
    public void save(Commentaire c) throws SQLException {
        String sql = "INSERT INTO commentaires "
                   + "(utilisateur_id, document_id, contenu, note) "
                   + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, c.getUtilisateur().getId());
            ps.setLong(2, c.getDocument().getId());
            ps.setString(3, c.getContenu());
            ps.setInt(4, c.getNote());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) c.setId(keys.getLong(1));
        }
    }

    // FIND BY ID
    @Override
    public Optional<Commentaire> findById(Long id) throws SQLException {
        String sql = "SELECT c.*, u.nom AS utilisateur_nom "
                   + "FROM commentaires c "
                   + "JOIN utilisateurs u ON c.utilisateur_id = u.id "
                   + "WHERE c.id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        }
        return Optional.empty();
    }


    // FIND ALL
    @Override
    public List<Commentaire> findAll() throws SQLException {
        List<Commentaire> liste = new ArrayList<>();
        String sql = "SELECT c.*, u.nom AS utilisateur_nom "
                   + "FROM commentaires c "
                   + "JOIN utilisateurs u ON c.utilisateur_id = u.id "
                   + "ORDER BY c.date_creation DESC";

        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(map(rs));
        }
        return liste;
    }


    // FIND BY DOCUMENT : tous les commentaires d'un document
    // Appele quand on affiche la fiche d'un document

    public List<Commentaire> findByDocument(Long documentId)
            throws SQLException {
        List<Commentaire> liste = new ArrayList<>();
        String sql = "SELECT c.*, u.nom AS utilisateur_nom "
                   + "FROM commentaires c "
                   + "JOIN utilisateurs u ON c.utilisateur_id = u.id "
                   + "WHERE c.document_id = ? "
                   + "ORDER BY c.date_creation DESC";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, documentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(map(rs));
        }
        return liste;
    }


    // FIND BY UTILISATEUR → tous les commentaires d'un user

    public List<Commentaire> findByUtilisateur(Long utilisateurId)
            throws SQLException {
        List<Commentaire> liste = new ArrayList<>();
        String sql = "SELECT c.*, u.nom AS utilisateur_nom "
                   + "FROM commentaires c "
                   + "JOIN utilisateurs u ON c.utilisateur_id = u.id "
                   + "WHERE c.utilisateur_id = ? "
                   + "ORDER BY c.date_creation DESC";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, utilisateurId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(map(rs));
        }
        return liste;
    }


    // NOTE MOYENNE d'un document
    // Retourne un double entre 1.0 et 5.0

    public double getNoteMoyenne(Long documentId) throws SQLException {
        String sql = "SELECT AVG(note) FROM commentaires WHERE document_id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, documentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        }
        return 0.0; // aucun commentaire
    }

   
    // UPDATE : modifier un commentaire 
    @Override
    public void update(Commentaire c) throws SQLException {
        String sql = "UPDATE commentaires SET contenu=?, note=? WHERE id=?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, c.getContenu());
            ps.setInt(2, c.getNote());
            ps.setLong(3, c.getId());
            ps.executeUpdate();
        }
    }


    // DELETE : supprimer un commentaire
    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM commentaires WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    // MAP : ligne SQL  objet Commentaire
    private Commentaire map(ResultSet rs) throws SQLException {
        Commentaire c = new Commentaire();
        c.setId(rs.getLong("id"));
        c.setContenu(rs.getString("contenu"));
        c.setNote(rs.getInt("note"));
        c.setDateCreation(rs.getDate("date_creation") != null ? rs.getDate("date_creation").toLocalDate() : null);

        // Charger le nom de l'utilisateur
        Utilisateur u = new Utilisateur();
        u.setId(rs.getLong("utilisateur_id"));
        u.setNom(rs.getString("utilisateur_nom"));
        c.setUtilisateur(u);

        Document doc = new Document();
        doc.setId(rs.getLong("document_id"));
        c.setDocument(doc);

        return c;
    }
}
