package com.bibliotheque.dao;


import com.bibliotheque.model.*;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class ListePartageImpl implements IDao<ListePartage> {

    private Connection conn() throws SQLException {
        return DatabaseConnection.getInstance();
    }

    @Override
    public void save(ListePartage lp) throws SQLException {
        String sql = "INSERT INTO listes_partages "
                   + "(liste_id, destinataire_id, date_partage) "
                   + "VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, lp.getListe().getId());
            ps.setLong(2, lp.getDestinnataire().getId());
            ps.setDate(3, Date.valueOf(lp.getDatePartage()));
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) lp.setId(keys.getLong(1));
        }
    }

    @Override
    public Optional<ListePartage> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM listes_partages WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        }
        return Optional.empty();
    }

    @Override
    public List<ListePartage> findAll() throws SQLException {
        List<ListePartage> liste = new ArrayList<>();
        String sql = "SELECT * FROM listes_partages";

        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(map(rs));
        }
        return liste;
    }

    // Listes partagees avec un utilisateur
    public List<ListePartage> findByDestinataire(Long destinataireId)
            throws SQLException {
        List<ListePartage> liste = new ArrayList<>();
        String sql = "SELECT * FROM listes_partages "
                   + "WHERE destinataire_id = ? "
                   + "ORDER BY date_partage DESC";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, destinataireId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(map(rs));
        }
        return liste;
    }

    // Verifier si deja partage pour eviter les doublons)
    public boolean existeDejaPartage(Long listeId, Long destinataireId)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM listes_partages "
                   + "WHERE liste_id=? AND destinataire_id=?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, listeId);
            ps.setLong(2, destinataireId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        }
        return false;
    }

    @Override
    public void update(ListePartage lp) throws SQLException {
        // Pas de modification possible sur un partage
        // On supprime et on recree si besoin
    }

    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM listes_partages WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private ListePartage map(ResultSet rs) throws SQLException {
        ListePartage lp = new ListePartage();
        lp.setId(rs.getLong("id"));
        
        ListeLecture liste = new ListeLecture();
        liste.setId(rs.getLong("liste_id"));
        lp.setListe(liste);
        
        Utilisateur dest = new Utilisateur();
        dest.setId(rs.getLong("destinataire_id"));
        lp.setDestinnataire(dest);
        
        lp.setDatePartage(rs.getDate("date_partage").toLocalDate());
        return lp;
    }
}
