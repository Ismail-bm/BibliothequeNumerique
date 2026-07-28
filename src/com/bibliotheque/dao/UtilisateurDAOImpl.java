package com.bibliotheque.dao;

import com.bibliotheque.dao.DatabaseConnection;
import com.bibliotheque.dao.IDao;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.model.RoleEnum;


import java.sql.*;
import java.util.*;

public class UtilisateurDAOImpl implements IDao<Utilisateur> {

    
    private Connection conn() throws SQLException {
        return DatabaseConnection.getInstance();
    }

    
 
    @Override
    public void save(Utilisateur u) throws SQLException {
        String sql = "INSERT INTO utilisateurs "
                   + "(nom, email, mot_de_passe, role, actif,date_inscription, max_emprunts) "
                   + "VALUES (?, ?, ?, ?, ?,?, ?)";

        
        try (PreparedStatement ps = conn().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, u.getNom());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getMotDepasse());
            ps.setString(4, u.getRole().name()); 
            ps.setBoolean(5, u.isActif());
            ps.setDate(6, new java.sql.Date(System.currentTimeMillis()));
            ps.setInt(7, u.getMaxEmprunt());

            ps.executeUpdate(); 

            // Recuperer l ID auto genere par MySQL
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                u.setId(keys.getLong(1));
            }
        }
    }


    @Override
    public Optional<Utilisateur> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM utilisateurs WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(map(rs)); 
            }
        }
        return Optional.empty(); // non trouve  retourne vide
    }


    @Override
    public List<Utilisateur> findAll() throws SQLException {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs ORDER BY nom";

        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(map(rs));
            }
        }
        return liste;
    }


    @Override
    public void update(Utilisateur u) throws SQLException {
        String sql = "UPDATE utilisateurs "
                   + "SET nom=?, email=?, mot_de_passe=?, role=?, actif=?, max_emprunts=? "
                   + "WHERE id=?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, u.getNom());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getMotDepasse());
            ps.setString(4, u.getRole().name());
            ps.setBoolean(5, u.isActif());
            ps.setInt(6, u.getMaxEmprunt());
            ps.setLong(7, u.getId());

            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM utilisateurs WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

  

    // Cherche par email : utilise pour la connexion
    public Optional<Utilisateur> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM utilisateurs WHERE email = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    // Verifie si un email existe deja → utilise pour l inscription
    public boolean existeEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM utilisateurs WHERE email = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0; // true si count > 0
            }
        }
        return false;
    }


    // MAP convertit une ligne SQL en objet Java
    // Méthode prive reutilisee par tous les findXxx()
  
    private Utilisateur map(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getLong("id"));
        u.setNom(rs.getString("nom"));
        u.setEmail(rs.getString("email"));
        u.setMotDepasse(rs.getString("mot_de_passe"));
        u.setRole(RoleEnum.valueOf(rs.getString("role"))); // String → enum
        u.setActif(rs.getBoolean("actif"));
        u.setMaxEmprunt(rs.getInt("max_emprunts"));
        return u;
    }
}