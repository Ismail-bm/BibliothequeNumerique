package com.bibliotheque.dao;

import com.bibliotheque.model.*;

import java.sql.*;
import java.sql.Date;
import java.util.*;


public class PenaliteImpl implements IDao<Penalite> {

    private Connection conn() throws SQLException {
        return DatabaseConnection.getInstance();
    }

    @Override
    public void save(Penalite p) throws SQLException {
        String sql = "INSERT INTO penalites "
                   + "(id_emprunt, jours_retard, montant, payee, date_calcul) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, p.getEmpruntId());
            ps.setInt(2, p.getJoursRetard());
            ps.setDouble(3, p.getMontant());
            ps.setBoolean(4, p.isPayee());
            ps.setDate(5, Date.valueOf(p.getDateCalcul()));
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) p.setId(keys.getLong(1));
        }
    }

    @Override
    public Optional<Penalite> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM penalites WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        }
        return Optional.empty();
    }

    @Override
    public List<Penalite> findAll() throws SQLException {
        List<Penalite> liste = new ArrayList<>();
        String sql = "SELECT * FROM penalites ORDER BY date_calcul DESC";

        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(map(rs));
        }
        return liste;
    }

    // Penalite liee a un emprunt precis
    public Optional<Penalite> findByEmprunt(Long empruntId)
            throws SQLException {
        String sql = "SELECT * FROM penalites WHERE id_emprunt = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, empruntId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        }
        return Optional.empty();
    }

    // Toutes les penalites non payees
    public List<Penalite> findNonPayees() throws SQLException {
        List<Penalite> liste = new ArrayList<>();
        String sql = "SELECT * FROM penalites WHERE payee = FALSE";

        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(map(rs));
        }
        return liste;
    }

    // Marquer une penalite comme payee
    public void marquerPayee(Long penaliteId) throws SQLException {
        String sql = "UPDATE penalites SET payee = TRUE WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, penaliteId);
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Penalite p) throws SQLException {
        String sql = "UPDATE penalites SET payee=? WHERE id=?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setBoolean(1, p.isPayee());
            ps.setLong(2, p.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM penalites WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Penalite map(ResultSet rs) throws SQLException {
        Penalite p = new Penalite();
        p.setId(rs.getLong("id"));
        p.setEmpruntId(rs.getLong("id_emprunt"));
        p.setJoursRetard(rs.getInt("jours_retard"));
        p.setMontant(rs.getDouble("montant"));
        p.setPayee(rs.getBoolean("payee"));
        p.setDateCalcul(rs.getDate("date_calcul").toLocalDate());
        return p;
    }
    
    public Optional<Utilisateur> findUtilisateurByEmpruntId(Long empruntId) throws SQLException {
        String sql = "SELECT u.* FROM utilisateurs u "
                   + "JOIN emprunts e ON e.id_utilisateur = u.id "
                   + "WHERE e.id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, empruntId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Utilisateur u = new Utilisateur();
                u.setId(rs.getLong("id"));
                u.setNom(rs.getString("nom"));
                u.setEmail(rs.getString("email"));
                // ajoutez les autres champs selon votre modèle
                return Optional.of(u);
            }
        }
        return Optional.empty();
    }
}
