
package com.bibliotheque.dao;

import com.bibliotheque.model.Document;
import com.bibliotheque.model.FormatEnum;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.Date;



public class DocumentImpl  implements IDao<Document>{
	
	
    private Connection conn() throws SQLException {
        return DatabaseConnection.getInstance();
    }


    @Override
    public void save(Document doc) throws SQLException {

        String sql = "INSERT INTO documents  (titre, auteur, date_publication,chemin_fichier, disponible,telechargeable,mot_cles,"
        		+ "accessible_en_ligne,nbr_consultations , resume, format) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?,?,?,?,?)";
        
 

        try (PreparedStatement ps = conn().prepareStatement( sql, Statement.RETURN_GENERATED_KEYS)) {

           
            ps.setString(1, doc.getTitre());
            ps.setString(2, doc.getAuteur());
            ps.setDate(3, (java.sql.Date) doc.getDatePublication());
            ps.setString(4,doc.getCheminFichier());
            ps.setBoolean(5, doc.isDisponible());
            ps.setBoolean(6, doc.isTelechargable());
            ps.setString(7,doc.getMotCles());
            ps.setBoolean(8,doc.isAccessibleEnligne());
            ps.setInt(9,doc.getNbrConsultations() );
            ps.setString(10, doc.getResume());
            ps.setString(11, doc.getFormat().name()); // FormatEnum → String
           
           
           
            ps.executeUpdate(); 


            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                doc.setId(keys.getLong(1));
                System.out.println("Document sauvegardé avec ID : " + doc.getId());
            }
        }
    }
    
    

	@Override
	public Optional<Document> findById(Long id) throws SQLException {
		
        String sql = "SELECT * FROM documents WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return Optional.of(map(rs)); 
            }
        }
        return Optional.empty(); // rien trouvé
    }

    // INCREMENTER LE NOMBRE DE CONSULTATIONS

    public void incrementerConsultation(Long documentId) throws SQLException {
        String sql = "UPDATE documents SET nbr_consultations = nbr_consultations + 1 WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, documentId);
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {

        String sql = "DELETE FROM documents WHERE id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Document doc) throws SQLException {

        String sql = "UPDATE documents "
                   + "SET titre=?, auteur=?, date_publication=?, chemin_fichier=?, disponible=?, "
                   + "telechargeable=?, mot_cles=?, accessible_en_ligne=?, nbr_consultations=?, "
                   + "resume=?, format=? WHERE id=?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, doc.getTitre());
            ps.setString(2, doc.getAuteur());
            ps.setDate(3, (java.sql.Date) doc.getDatePublication());
            ps.setString(4, doc.getCheminFichier());
            ps.setBoolean(5, doc.isDisponible());
            ps.setBoolean(6, doc.isTelechargable());
            ps.setString(7, doc.getMotCles());
            ps.setBoolean(8, doc.isAccessibleEnligne());
            ps.setInt(9, doc.getNbrConsultations());
            ps.setString(10, doc.getResume());
            ps.setString(11, doc.getFormat().name());
            ps.setLong(12, doc.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Document> findAll() throws SQLException {
        List<Document> liste = new ArrayList<>();
        String sql = "SELECT * FROM documents ORDER BY titre";

        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(map(rs));
        }
        return liste;
    }

    public List<Document> findDisponibles() throws SQLException {
        List<Document> liste = new ArrayList<>();
        String sql = "SELECT * FROM documents WHERE disponible = TRUE ORDER BY titre";

        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) liste.add(map(rs));
        }
        return liste;
    }

    public List<Document> findByTitre(String titre) throws SQLException {
        List<Document> liste = new ArrayList<>();
        String sql = "SELECT * FROM documents WHERE LOWER(titre) LIKE LOWER(?) ORDER BY titre";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, "%" + titre + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(map(rs));
        }
        return liste;
    }

    public List<Document> findByAuteur(String auteur) throws SQLException {
        List<Document> liste = new ArrayList<>();
        String sql = "SELECT * FROM documents WHERE LOWER(auteur) LIKE LOWER(?) ORDER BY titre";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, "%" + auteur + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(map(rs));
        }
        return liste;
    }

    public List<Document> findByCategorie(Long categorieId) throws SQLException {
        List<Document> liste = new ArrayList<>();
        String sql = "SELECT d.* FROM documents d "
                   + "JOIN document_categorie dc ON d.id = dc.document_id "
                   + "WHERE dc.categorie_id = ? ORDER BY d.titre";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setLong(1, categorieId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(map(rs));
        }
        return liste;
    }
    
    
 // FIND BY MOTS CLES : recherche dans plusieurs colonnes
 // Cherche le mot-clé dans : titre, auteur, resume
 // Un seul mot-clé peut matcher plusieurs colonnes

 public List<Document> findByMotsCles(String motCle) throws SQLException {

     List<Document> liste = new ArrayList<>();

     // OR = si le mot-clé est trouvé dans UNE des colonnes → retourner le document
     // LOWER() = insensible à la casse (Java = java = JAVA)
     String sql = "SELECT * FROM documents "
                + "WHERE LOWER(titre)  LIKE LOWER(?) "
                + "OR    LOWER(auteur) LIKE LOWER(?) "
                + "OR    LOWER(resume) LIKE LOWER(?) "
                + "ORDER BY titre";

     try (PreparedStatement ps = conn().prepareStatement(sql)) {

        
         String motCleFormate = "%" + motCle + "%";
         ps.setString(1, motCleFormate); // pour le titre
         ps.setString(2, motCleFormate); // pour l'auteur
         ps.setString(3, motCleFormate); // pour le resume

         ResultSet rs = ps.executeQuery();
         while (rs.next()) {
             liste.add(map(rs));
         }
     }
     return liste;
 }
 

//FIND BY PLUSIEURS MOTS CLES
//Ex: "java clean" → cherche "java" ET "clean"
//Chaque mot est cherché dans titre, auteur, resume

public List<Document> findByPlusieursMots(String recherche) throws SQLException {

  // Decouper la phrase en mots individuel
  // "java clean code" → ["java", "clean", "code"]
  String[] mots = recherche.trim().split("\\s+");

  List<Document> liste = new ArrayList<>();

  // Construire la requête dynamiquement selon le nombre de mots
  StringBuilder sql = new StringBuilder(
      "SELECT DISTINCT * FROM documents WHERE "
  );

  // Pour chaque mot → ajouter une condition
  for (int i = 0; i < mots.length; i++) {
      if (i > 0) sql.append(" AND "); // ET entre chaque mot
      sql.append("(LOWER(titre)  LIKE LOWER(?) ")
         .append("OR LOWER(auteur) LIKE LOWER(?) ")
         .append("OR LOWER(resume) LIKE LOWER(?)) ");
  }
  sql.append("ORDER BY titre");

  try (PreparedStatement ps = conn().prepareStatement(sql.toString())) {
      int index = 1;
      for (String mot : mots) {
          String motFormate = "%" + mot + "%";
          ps.setString(index++, motFormate); // titre
          ps.setString(index++, motFormate); // auteur
          ps.setString(index++, motFormate); // resume
      }
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
          liste.add(map(rs));
      }
  }
  return liste;
}
    
    
    private Document map(ResultSet rs) throws SQLException {
    	
        Document doc = new Document();
        doc.setId(rs.getLong("id"));
        doc.setTitre(rs.getString("titre"));
        doc.setAuteur(rs.getString("auteur"));
        doc.setDatePublication(rs.getDate("date_publication"));
        doc.setCheminFichier(rs.getString("chemin_Fichier"));
        doc.setDisponible(rs.getBoolean("disponible"));
        doc.setTelechargable(rs.getBoolean("telechargeable"));
        doc.setMotCles(rs.getString("mot_cles"));
        doc.setAccessibleEnligne(rs.getBoolean("accessible_en_ligne"));
        doc.setNbrConsultations(rs.getInt("nbr_consultations"));
        doc.setFormat(FormatEnum.valueOf(rs.getString("format"))); // String → enum     
        doc.setResume(rs.getString("resume"));
      
        return doc;
    }


}
