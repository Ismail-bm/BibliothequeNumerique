package com.bibliotheque.dao;


import com.bibliotheque.dao.*;
import com.bibliotheque.model.*;

import java.sql.*;
import java.util.*;

public class CategorieImpl implements IDao<Categorie> {


    private Connection conn() throws SQLException {
        return DatabaseConnection.getInstance();
    }
    

    // SAVE : ajouter une nouvelle categorie   
    @Override
    public void save(Categorie c) throws SQLException {
        String sql = "INSERT INTO categories (nom, description) VALUES (?, ?)";

        try (PreparedStatement ps = conn().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getDescription());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) c.setId(keys.getLong(1));
        }
    }


    @Override
    public Optional<Categorie> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM categories WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        }
        return Optional.empty();
    }

    @Override
    public List<Categorie> findAll() throws SQLException {
        List<Categorie> liste = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY nom";

        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(map(rs));
        }
        return liste;
    }

    public Optional<Categorie> findByNom(String nom) throws SQLException {
        String sql = "SELECT * FROM categories WHERE LOWER(nom) = LOWER(?)";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, nom);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        }
        return Optional.empty();
    }

    // UPDATE : modifier une categorie
 
    @Override
    public void update(Categorie c) throws SQLException {
        String sql = "UPDATE categories SET nom=?, description=? WHERE id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, c.getNom());
            ps.setString(2, c.getDescription());
            ps.setLong(3, c.getId());
            ps.executeUpdate();
        }
    }


    // DELETE : supprimer une categorie

    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM categories WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

 
    // AJOUTER un document dans une categorie
    // Gere la table de jointure document_categorie
    public void ajouterDocument(Long categorieId, Long documentId)
            throws SQLException {
        String sql = "INSERT INTO document_categorie "
                   + "(categorie_id, document_id) VALUES (?, ?)";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, categorieId);
            ps.setLong(2, documentId);
            ps.executeUpdate();
        }
    }

    // RETIRER un document d'une categorie
    public void retirerDocument(Long categorieId, Long documentId)
            throws SQLException {
        String sql = "DELETE FROM document_categorie "
                   + "WHERE categorie_id=? AND document_id=?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, categorieId);
            ps.setLong(2, documentId);
            ps.executeUpdate();
        }
    }

    // MAP : ligne SQL a une objet Categorie
    private Categorie map(ResultSet rs) throws SQLException {
        Categorie c = new Categorie();
        c.setId(rs.getLong("id"));
        c.setNom(rs.getString("nom"));
        c.setDescription(rs.getString("description"));
        return c;
    }
}