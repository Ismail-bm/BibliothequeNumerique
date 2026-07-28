package com.bibliotheque.dao;

import com.bibliotheque.model.*;


import java.sql.*;
import java.util.*;

public class RecommandationImpl implements IDao<Recommmandation> {
	

    private Connection conn() throws SQLException {
        return DatabaseConnection.getInstance();
    }

    @Override
    public void save(Recommmandation r) throws SQLException {
        String sql = "INSERT INTO recommandations "
                   + "(expediteur_id, destinataire_id, document_id, message) "
                   + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, r.getExpediteur().getId());
            ps.setLong(2, r.getDestinataire().getId());
            ps.setLong(3, r.getDocument().getId());
            ps.setString(4, r.getMessage());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) r.setId(keys.getLong(1));
        }
    }

    @Override
    public Optional<Recommmandation> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM recommandations WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        }
        return Optional.empty();
    }

    @Override
    public List<Recommmandation> findAll() throws SQLException {
        List<Recommmandation> liste = new ArrayList<>();
        String sql = "SELECT * FROM recommandations ORDER BY date_envoi DESC";

        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(map(rs));
        }
        return liste;
    }

    // Recommandations recues par un utilisateur
    public List<Recommmandation> findByDestinataire(Long destinataireId)
            throws SQLException {
        List<Recommmandation> liste = new ArrayList<>();
        String sql = "SELECT * FROM recommandations "
                   + "WHERE destinataire_id = ? "
                   + "ORDER BY date_envoi DESC";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, destinataireId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(map(rs));
        }
        return liste;
    }

    // Recommandations envoyees par un utilisateur
    public List<Recommmandation> findByExpediteur(Long expediteurId)
            throws SQLException {
        List<Recommmandation> liste = new ArrayList<>();
        String sql = "SELECT * FROM recommandations "
                   + "WHERE expediteur_id = ? "
                   + "ORDER BY date_envoi DESC";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, expediteurId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(map(rs));
        }
        return liste;
    }

    @Override
    public void update(Recommmandation r) throws SQLException {
        String sql = "UPDATE recommandations SET message=? WHERE id=?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, r.getMessage());
            ps.setLong(2, r.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM recommandations WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Recommmandation map(ResultSet rs) throws SQLException {
    	Recommmandation r = new Recommmandation();
        r.setId(rs.getLong("id"));
        r.setMessage(rs.getString("message"));
        r.setDateEnvoi(rs.getDate("date_envoi").toLocalDate());

        Utilisateur exp = new Utilisateur();
        exp.setId(rs.getLong("expediteur_id"));
        r.setExpediteur(exp);

        Utilisateur dest = new Utilisateur();
        dest.setId(rs.getLong("destinataire_id"));
        r.setDestinataire(dest);

        Document doc = new Document();
        doc.setId(rs.getLong("document_id"));
        r.setDocument(doc);

        return r;
    }
}
