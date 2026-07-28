package com.bibliotheque.presentation;

import com.bibliotheque.model.*;
import com.bibliotheque.service.EmpruntService;
import com.bibliotheque.service.DocumentService;
import com.bibliotheque.service.UtilisateurService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ListesPartageesPanel extends JPanel {

    private EmpruntService empruntService = new EmpruntService();
    private DocumentService documentService = new DocumentService();
    private UtilisateurService utilisateurService = new UtilisateurService();
    private Utilisateur utilisateurConnecte;

    private JTable tableListes;
    private DefaultTableModel modelListes;
    private JTable tableDocuments;
    private DefaultTableModel modelDocuments;
    private List<ListePartage> listesPartagees;

    private JButton btnVoirDetails = new JButton("Details");
    private JButton btnOuvrirDoc = new JButton("Ouvrir");
    private JButton btnTelechargerDoc = new JButton("Telecharger");
    private JButton btnRefresh = new JButton("Rafraichir");

    public ListesPartageesPanel(Utilisateur u) {
        this.utilisateurConnecte = u;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titre = new JLabel("Listes Partagees avec Moi", SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 16));
        add(titre, BorderLayout.NORTH);

        construireTableaux();
        construireBoutons();
        chargerListesPartagees();
    }

    private void construireTableaux() {
        String[] colListes = {"ID", "Nom de la liste", "Proprietaire", "Date partage"};
        modelListes = new DefaultTableModel(colListes, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableListes = new JTable(modelListes);
        tableListes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableListes.setRowHeight(25);

        String[] colDocs = {"ID", "Titre", "Auteur", "Format", "Disponible"};
        modelDocuments = new DefaultTableModel(colDocs, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableDocuments = new JTable(modelDocuments);
        tableDocuments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableDocuments.setRowHeight(25);

        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            new JScrollPane(tableListes),
            new JScrollPane(tableDocuments));
        split.setDividerLocation(350);
        add(split, BorderLayout.CENTER);

        tableListes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                voirDocumentsListe();
                updateBoutons();
            }
        });

        tableDocuments.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateBoutonsDocuments();
            }
        });
    }

    private void construireBoutons() {
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(btnRefresh);
        btnPanel.add(btnVoirDetails);
        btnPanel.add(btnOuvrirDoc);
        btnPanel.add(btnTelechargerDoc);

        btnRefresh.addActionListener(e -> chargerListesPartagees());
        btnVoirDetails.addActionListener(e -> voirDetailsListe());
        btnOuvrirDoc.addActionListener(e -> ouvrirDocument());
        btnTelechargerDoc.addActionListener(e -> telechargerDocument());

        add(btnPanel, BorderLayout.SOUTH);

        btnVoirDetails.setEnabled(false);
        btnOuvrirDoc.setEnabled(false);
        btnTelechargerDoc.setEnabled(false);
    }

private void chargerListesPartagees() {
        try {
            listesPartagees = empruntService.getListesPartagees(utilisateurConnecte.getId());
            modelListes.setRowCount(0);
            
            if (listesPartagees.isEmpty()) {
                modelListes.addRow(new Object[]{
                    "-", "Aucune liste partagee", "-", "-"
                });
            } else {
                for (ListePartage lp : listesPartagees) {
                    String nomListe = "Inconnu";
                    String proprietaire = "Inconnu";
                    
                    if (lp.getListe() != null && lp.getListe().getId() != null) {
                        try {
                            ListeLecture liste = empruntService.getListeLectureById(lp.getListe().getId());
                            if (liste != null) {
                                nomListe = liste.getNom() != null ? liste.getNom() : "Liste #" + liste.getId();
                                
                                if (liste.getProprietaireId() != null) {
                                    try {
                                        Utilisateur u = utilisateurService.findById(liste.getProprietaireId());
                                        proprietaire = u.getNom() + " (" + u.getRole() + ")";
                                    } catch (Exception e) {
                                        proprietaire = "ID: " + liste.getProprietaireId();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            nomListe = "Liste #" + lp.getListe().getId();
                        }
                    }
                    
                    String dateStr = lp.getDatePartage() != null ? lp.getDatePartage().toString() : "-";
                    
                    modelListes.addRow(new Object[]{
                        lp.getId(), nomListe, proprietaire, dateStr
                    });
                }
            }
            
            btnVoirDetails.setEnabled(false);
            btnOuvrirDoc.setEnabled(false);
            btnTelechargerDoc.setEnabled(false);
            
        } catch (Exception ex) {
            modelListes.setRowCount(0);
            modelListes.addRow(new Object[]{"-", "Aucune liste partagee", "-", "-"});
        }
    }

    private void voirDocumentsListe() {
        int row = tableListes.getSelectedRow();
        if (row == -1 || listesPartagees.isEmpty() || row >= listesPartagees.size()) {
            modelDocuments.setRowCount(0);
            return;
        }

        try {
            ListePartage lp = listesPartagees.get(row);
            
            if (lp == null) {
                modelDocuments.setRowCount(0);
                modelDocuments.addRow(new Object[]{"-", "Partage invalide", "-", "-", "-"});
                return;
            }
            
            ListeLecture liste = lp.getListe();
            if (liste == null) {
                modelDocuments.setRowCount(0);
                modelDocuments.addRow(new Object[]{"-", "Liste supprimee", "-", "-", "-"});
                return;
            }
            
            Long listeId = liste.getId();
            if (listeId == null) {
                modelDocuments.setRowCount(0);
                return;
            }

            List<Document> docs = new java.util.ArrayList<>();
            
            try {
                docs = empruntService.getDocumentsDeListe(listeId);
            } catch (Exception e) {
            }
            
            modelDocuments.setRowCount(0);
            if (docs == null || docs.isEmpty()) {
                modelDocuments.addRow(new Object[]{"-", "Aucun document", "-", "-", "-"});
            } else {
                for (Document d : docs) {
                    modelDocuments.addRow(new Object[]{
                        d.getId(),
                        d.getTitre() != null ? d.getTitre() : "",
                        d.getAuteur() != null ? d.getAuteur() : "",
                        d.getFormat() != null ? d.getFormat().toString() : "",
                        d.isDisponible() ? "OUI" : "NON"
                    });
                }
            }
            btnVoirDetails.setEnabled(true);
            
        } catch (Exception ex) {
            modelDocuments.setRowCount(0);
            modelDocuments.addRow(new Object[]{"-", "Erreur: " + ex.getMessage(), "-", "-", "-"});
        }
    }

    private void updateBoutons() {
        int row = tableListes.getSelectedRow();
        btnVoirDetails.setEnabled(row != -1 && !listesPartagees.isEmpty() && 
                                  row < listesPartagees.size() && 
                                  listesPartagees.get(row).getListe() != null);
    }

private void updateBoutonsDocuments() {
        int row = tableDocuments.getSelectedRow();
        if (row == -1) {
            btnOuvrirDoc.setEnabled(false);
            btnTelechargerDoc.setEnabled(false);
            return;
        }

        try {
            int listeRow = tableListes.getSelectedRow();
            if (listeRow == -1 || listeRow >= listesPartagees.size()) {
                btnOuvrirDoc.setEnabled(false);
                btnTelechargerDoc.setEnabled(false);
                return;
            }

            ListePartage lp = listesPartagees.get(listeRow);
            if (lp == null || lp.getListe() == null) {
                btnOuvrirDoc.setEnabled(false);
                btnTelechargerDoc.setEnabled(false);
                return;
            }

            btnOuvrirDoc.setEnabled(true);
            btnTelechargerDoc.setEnabled(true);

        } catch (Exception ex) {
            btnOuvrirDoc.setEnabled(false);
            btnTelechargerDoc.setEnabled(false);
        }
    }

    private void voirDetailsListe() {
        int row = tableListes.getSelectedRow();
        if (row == -1 || row >= listesPartagees.size()) return;

        ListePartage lp = listesPartagees.get(row);
        if (lp.getListe() == null || lp.getListe().getId() == null) {
            JOptionPane.showMessageDialog(this, "Cette liste a ete supprimee !");
            return;
        }

        String nomListe = "Inconnu";
        String proprietaire = "Inconnu";
        boolean estPublique = false;
        String dateCreation = "-";
        
        try {
            ListeLecture liste = empruntService.getListeLectureById(lp.getListe().getId());
            if (liste != null) {
                nomListe = liste.getNom() != null ? liste.getNom() : "Liste #" + liste.getId();
                estPublique = liste.isEstpublique();
                
                if (liste.getDateCreation() != null) {
                    dateCreation = liste.getDateCreation().toString();
                }
                
                if (liste.getProprietaireId() != null) {
                    try {
                        Utilisateur u = utilisateurService.findById(liste.getProprietaireId());
                        proprietaire = u.getNom() + " (" + u.getRole() + ")";
                    } catch (Exception e) {
                        proprietaire = "ID: " + liste.getProprietaireId();
                    }
                }
            }
        } catch (Exception e) {
            nomListe = "Liste #" + lp.getListe().getId();
        }

        String info = "Nom: " + nomListe + "\n" +
                      "Publique: " + (estPublique ? "OUI" : "NON") + "\n" +
                      "Proprietaire: " + proprietaire + "\n" +
                      "Date de creation: " + dateCreation + "\n" +
                      "Date de partage: " + lp.getDatePartage();

        JOptionPane.showMessageDialog(this, info, "Details de la liste", JOptionPane.INFORMATION_MESSAGE);
    }

    private void ouvrirDocument() {
        int listeRow = tableListes.getSelectedRow();
        int docRow = tableDocuments.getSelectedRow();
        if (listeRow == -1 || docRow == -1 || listeRow >= listesPartagees.size()) return;

        try {
            ListePartage lp = listesPartagees.get(listeRow);
            if (lp == null || lp.getListe() == null) return;

            List<Document> docs = null;
            try {
                docs = empruntService.getDocumentsDeListe(lp.getListe().getId());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
                return;
            }
            
            if (docs == null || docRow >= docs.size()) return;

            Document doc = docs.get(docRow);

            String chemin = doc.getCheminFichier();
            if (chemin == null || chemin.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Chemin du fichier introuvable !");
                return;
            }

            File fichier = new File(chemin);
            if (!fichier.exists()) {
                JOptionPane.showMessageDialog(this, "Fichier introuvable !", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!java.awt.Desktop.isDesktopSupported()) {
                JOptionPane.showMessageDialog(this, "Ouverture non supportee !");
                return;
            }

            java.awt.Desktop.getDesktop().open(fichier);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void telechargerDocument() {
        int listeRow = tableListes.getSelectedRow();
        int docRow = tableDocuments.getSelectedRow();
        if (listeRow == -1 || docRow == -1 || listeRow >= listesPartagees.size()) return;

        try {
            ListePartage lp = listesPartagees.get(listeRow);
            if (lp == null || lp.getListe() == null) return;

            List<Document> docs = null;
            try {
                docs = empruntService.getDocumentsDeListe(lp.getListe().getId());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage());
                return;
            }
            
            if (docs == null || docRow >= docs.size()) return;

            Document doc = docs.get(docRow);

            String chemin = doc.getCheminFichier();
            if (chemin == null || chemin.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Chemin du fichier introuvable !");
                return;
            }

            File source = new File(chemin);
            if (!source.exists()) {
                JOptionPane.showMessageDialog(this, "Fichier introuvable !", "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File(source.getName()));
            fileChooser.setDialogTitle("Enregistrer le document");

            int result = fileChooser.showSaveDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File destination = fileChooser.getSelectedFile();
                Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this, "Document telecharge avec succes !");
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}