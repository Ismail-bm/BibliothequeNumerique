package com.bibliotheque.dao;

import com.bibliotheque.dao.*;
import com.bibliotheque.model.*;

import java.sql.*;
import java.sql.Date;
import java.util.*;

public class ListeLectureImpl implements IDao<ListeLecture> {
	

    private Connection conn() throws SQLException {
        return DatabaseConnection.getInstance();
    }


// SAVE → creer une nouvelle liste de lecture
  
    @Override
    public void save(ListeLecture l) throws SQLException {
        String sql = "INSERT INTO listes_lecture "
                   + "(nom, est_publique, proprietaire_id, date_creation) "
                   + "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, l.getNom());
            ps.setBoolean(2, l.isEstpublique());
            ps.setLong(3, l.getProprietaireId());
            ps.setDate(4, l.getDateCreation() != null ? 
                Date.valueOf(l.getDateCreation()) : Date.valueOf(java.time.LocalDate.now()));
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) l.setId(keys.getLong(1));
        }
    }


    // FIND BY ID

    @Override
    public Optional<ListeLecture> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM listes_lecture WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return Optional.of(map(rs));
        }
        return Optional.empty();
    }


    // FIND ALL

    @Override
    public List<ListeLecture> findAll() throws SQLException {
        List<ListeLecture> liste = new ArrayList<>();
        String sql = "SELECT * FROM listes_lecture ORDER BY nom";

        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(map(rs));
        }
        return liste;
    }


    // FIND BY PROPRIETAIRE → listes d'un utilisateur

    public List<ListeLecture> findByProprietaire(Long proprietaireId)
            throws SQLException {
        List<ListeLecture> liste = new ArrayList<>();
        String sql = "SELECT * FROM listes_lecture "
                   + "WHERE proprietaire_id = ? ORDER BY nom";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, proprietaireId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(map(rs));
        }
        return liste;
    }

  
    // FIND PUBLIQUES → listes visibles par tous

    public List<ListeLecture> findPubliques() throws SQLException {
        List<ListeLecture> liste = new ArrayList<>();
        String sql = "SELECT * FROM listes_lecture "
                   + "WHERE est_publique = TRUE ORDER BY nom";

        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(map(rs));
        }
        return liste;
    }

   
    // GET DOCUMENTS → tous les documents d'une liste
    // Fait une jointure avec liste_documents et documents

    public List<Document> getDocuments(Long listeId) throws SQLException {
        List<Document> docs = new ArrayList<>();
        String sql = "SELECT d.* FROM documents d "
                   + "JOIN liste_documents ld ON d.id = ld.document_id "
                   + "WHERE ld.liste_id = ? "
                   + "ORDER BY d.titre";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, listeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Document doc = new Document();
                doc.setId(rs.getLong("id"));
                doc.setTitre(rs.getString("titre"));
                doc.setAuteur(rs.getString("auteur"));
                doc.setDatePublication(rs.getDate("date_publication"));
                doc.setCheminFichier(rs.getString("chemin_Fichier"));
                doc.setDisponible(rs.getBoolean("disponible"));
                doc.setTelechargable(rs.getBoolean("telechargeable"));
                doc.setAccessibleEnligne(rs.getBoolean("accessible_en_ligne"));
                doc.setNbrConsultations(rs.getInt("nbr_consultations"));
                doc.setFormat(FormatEnum.valueOf(rs.getString("format")));
                doc.setResume(rs.getString("resume"));
                docs.add(doc);
            }
        }
        return docs;
    }


    // Add DOCUMENT dans une liste
   
    public void ajouterDocument(Long listeId, Long documentId)
            throws SQLException {
        String sql = "INSERT INTO liste_documents (liste_id, document_id) "
                   + "VALUES (?, ?)";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, listeId);
            ps.setLong(2, documentId);
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new SQLException("Ce document est deja dans la liste !");
            }
            throw e;
        }
    }


    // RETIRER DOCUMENT d'une liste

    public void retirerDocument(Long listeId, Long documentId)
            throws SQLException {
        String sql = "DELETE FROM liste_documents "
                   + "WHERE liste_id=? AND document_id=?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, listeId);
            ps.setLong(2, documentId);
            ps.executeUpdate();
        }
    }


    // UPDATE → modifier le nom ou la visibilite

    @Override
    public void update(ListeLecture l) throws SQLException {
        String sql = "UPDATE listes_lecture "
                   + "SET nom=?, est_publique=? WHERE id=?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, l.getNom());
            ps.setBoolean(2, l.isEstpublique());
            ps.setLong(3, l.getId());
            ps.executeUpdate();
        }
    }


    // DELETE : supprimer une liste
  
    @Override
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM listes_lecture WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    // MAP → ligne SQL → objet ListeLecture
    private ListeLecture map(ResultSet rs) throws SQLException {
        ListeLecture l = new ListeLecture();
        l.setId(rs.getLong("id"));
        l.setNom(rs.getString("nom"));
        l.setEstpublique(rs.getBoolean("est_publique"));
        l.setProprietaireId(rs.getLong("proprietaire_id"));
        l.setDateCreation(rs.getDate("date_creation").toLocalDate());
        return l;
    }
}
