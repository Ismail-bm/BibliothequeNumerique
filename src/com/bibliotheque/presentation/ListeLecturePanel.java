package com.bibliotheque.presentation;

import com.bibliotheque.model.*;
import com.bibliotheque.service.EmpruntService;
import com.bibliotheque.service.DocumentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class ListeLecturePanel extends JPanel {

    private EmpruntService    service = new EmpruntService();
    private DocumentService    documentService = new DocumentService();
    private Utilisateur        utilisateurConnecte;

    private JTable            tableListes;
    private DefaultTableModel modelListes;
    private JTable            tableDocs;
    private DefaultTableModel modelDocs;
    private List<ListeLecture> mesListes;

    private JButton btnCreer       = new JButton("Creer");
    private JButton btnModifier    = new JButton("Modifier");
    private JButton btnSupprimer   = new JButton("Supprimer");
    private JButton btnAjouterDoc  = new JButton("Ajouter document");
    private JButton btnRetirerDoc  = new JButton("Retirer document");
    private JButton btnPartager    = new JButton("Partager");
    private JButton btnPubliques   = new JButton("Listes publiques");
    private JButton btnRechercher  = new JButton("Rechercher");
    private JButton btnOuvrir      = new JButton("Ouvrir");
    private JButton btnTelecharger = new JButton("Telecharger");
    private JButton btnRefresh     = new JButton("Rafraichir");

    private JTextField rechercheField = new JTextField(15);

    public ListeLecturePanel(Utilisateur u) {
        this.utilisateurConnecte = u;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titre = new JLabel("Mes Listes de Lecture", SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 16));
        add(titre, BorderLayout.NORTH);

        construireTableaux();
        construireRecherche();
        construireBoutons();
        chargerMesListes();
    }

    private void construireTableaux() {
        String[] colListes = {"ID", "Nom", "Publique", "Date Creation"};
        modelListes = new DefaultTableModel(colListes, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableListes = new JTable(modelListes);
        tableListes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableListes.setRowHeight(25);

        String[] colDocs = {"ID", "Titre", "Auteur", "Format", "Disponible"};
        modelDocs = new DefaultTableModel(colDocs, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableDocs = new JTable(modelDocs);
        tableDocs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableDocs.setRowHeight(25);

        tableListes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                voirDocumentsListe();
                updateBoutons();
            }
        });

        tableDocs.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateBoutonsDocuments();
            }
        });

        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            new JScrollPane(tableListes),
            new JScrollPane(tableDocs));
        split.setDividerLocation(300);
        add(split, BorderLayout.CENTER);
    }

    private void construireRecherche() {
        JPanel rechPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rechPanel.add(new JLabel("Rechercher dans la liste :"));
        rechPanel.add(rechercheField);
        rechPanel.add(btnRechercher);

        btnRechercher.addActionListener(e -> rechercherDansListe());
        rechPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(rechPanel, BorderLayout.BEFORE_FIRST_LINE);
    }

    private void construireBoutons() {
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        
        JPanel ligne1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ligne1.add(btnRefresh);
        ligne1.add(btnCreer);
        ligne1.add(btnModifier);
        ligne1.add(btnSupprimer);
        
        JPanel ligne2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ligne2.add(btnPartager);
        ligne2.add(btnPubliques);
        ligne2.add(new JSeparator(SwingConstants.VERTICAL));
        ligne2.add(btnAjouterDoc);
        ligne2.add(btnRetirerDoc);
        
        JPanel ligne3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ligne3.add(btnOuvrir);
        ligne3.add(btnTelecharger);
        
        btnPanel.add(ligne1);
        btnPanel.add(ligne2);
        btnPanel.add(ligne3);

        btnCreer.addActionListener(e -> creerListe());
        btnModifier.addActionListener(e -> modifierListe());
        btnSupprimer.addActionListener(e -> supprimerListe());
        btnPartager.addActionListener(e -> partager());
        btnPubliques.addActionListener(e -> voirPubliques());
        btnRefresh.addActionListener(e -> chargerMesListes());
        btnAjouterDoc.addActionListener(e -> ajouterDocument());
        btnRetirerDoc.addActionListener(e -> retirerDocument());
        btnOuvrir.addActionListener(e -> ouvrirDocument());
        btnTelecharger.addActionListener(e -> telechargerDocument());

        btnModifier.setEnabled(false);
        btnSupprimer.setEnabled(false);
        btnPartager.setEnabled(false);
        btnAjouterDoc.setEnabled(false);
        btnRetirerDoc.setEnabled(false);
        btnOuvrir.setEnabled(false);
        btnTelecharger.setEnabled(false);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void updateBoutons() {
        int row = tableListes.getSelectedRow();
        boolean selection = row != -1;
        btnModifier.setEnabled(selection);
        btnSupprimer.setEnabled(selection);
        btnAjouterDoc.setEnabled(selection);
        btnPartager.setEnabled(selection && row >= 0 && row < mesListes.size());
        
        if (!selection) {
            modelDocs.setRowCount(0);
            btnRetirerDoc.setEnabled(false);
            btnOuvrir.setEnabled(false);
            btnTelecharger.setEnabled(false);
        } else {
            voirDocumentsListe();
        }
    }

    private void updateBoutonsDocuments() {
        int row = tableDocs.getSelectedRow();
        boolean selection = row != -1;
        btnRetirerDoc.setEnabled(selection);
        
        if (!selection) {
            btnOuvrir.setEnabled(false);
            btnTelecharger.setEnabled(false);
            return;
        }

        try {
            int listeRow = tableListes.getSelectedRow();
            if (listeRow == -1) return;

            ListeLecture liste = mesListes.get(listeRow);
            List<Document> docs = service.getDocumentsDeListe(liste.getId());
            Document doc = docs.get(row);

            boolean peutOuvrir = doc.isAccessibleEnligne() && 
                                 doc.getCheminFichier() != null && 
                                 !doc.getCheminFichier().isEmpty();
            boolean peutTel = doc.isTelechargable() && 
                             doc.getCheminFichier() != null && 
                             !doc.getCheminFichier().isEmpty();

            btnOuvrir.setEnabled(peutOuvrir);
            btnTelecharger.setEnabled(peutTel);
        } catch (Exception ex) {
            btnOuvrir.setEnabled(false);
            btnTelecharger.setEnabled(false);
        }
    }

    private void chargerMesListes() {
        try {
            mesListes = service.getMesListesLecture(utilisateurConnecte.getId());
            modelListes.setRowCount(0);
            for (ListeLecture l : mesListes) {
                modelListes.addRow(new Object[]{
                    l.getId(),
                    l.getNom(),
                    l.isEstpublique() ? "OUI" : "NON",
                    l.getDateCreation()
                });
            }
            modelDocs.setRowCount(0);
            updateBoutons();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
            modelListes.setRowCount(0);
        }
    }

    private void creerListe() {
        JTextField nomField = new JTextField(15);
        JCheckBox publicCB = new JCheckBox("Rendre cette liste publique ?");

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Nom de la liste :")); form.add(nomField);
        form.add(new JLabel("")); form.add(publicCB);

        int result = JOptionPane.showConfirmDialog(this, form,
            "Creer une liste de lecture", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            if (nomField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Le nom est obligatoire !");
                return;
            }

            ListeLecture l = new ListeLecture(
                nomField.getText().trim(),
                utilisateurConnecte.getId(),
                publicCB.isSelected()
            );
            try {
                service.creerListeLecture(l);
                JOptionPane.showMessageDialog(this, "Liste creee avec succes !");
                chargerMesListes();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void modifierListe() {
        int row = tableListes.getSelectedRow();
        if (row == -1) return;

        ListeLecture l = mesListes.get(row);

        JTextField nomField = new JTextField(l.getNom(), 15);
        JCheckBox publicCB = new JCheckBox("Rendre cette liste publique ?");
        publicCB.setSelected(l.isEstpublique());

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Nom de la liste :")); form.add(nomField);
        form.add(new JLabel("")); form.add(publicCB);

        int result = JOptionPane.showConfirmDialog(this, form,
            "Modifier la liste", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            if (nomField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Le nom est obligatoire !");
                return;
            }
            l.setNom(nomField.getText().trim());
            l.setEstpublique(publicCB.isSelected());
            try {
                service.modifierListeLecture(l);
                JOptionPane.showMessageDialog(this, "Liste modifiee !");
                chargerMesListes();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void supprimerListe() {
        int row = tableListes.getSelectedRow();
        if (row == -1) return;

        ListeLecture l = mesListes.get(row);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Voulez-vous vraiment supprimer la liste '" + l.getNom() + "' ?",
            "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.supprimerListeLecture(l.getId());
                JOptionPane.showMessageDialog(this, "Liste supprimee !");
                chargerMesListes();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void voirDocumentsListe() {
        int row = tableListes.getSelectedRow();
        if (row == -1) {
            modelDocs.setRowCount(0);
            btnOuvrir.setEnabled(false);
            btnTelecharger.setEnabled(false);
            btnRetirerDoc.setEnabled(false);
            return;
        }

        try {
            ListeLecture l = mesListes.get(row);
            List<Document> docs = null;
            try {
                docs = service.getDocumentsDeListe(l.getId());
            } catch (Exception e) {
                docs = new java.util.ArrayList<>();
            }
            
            modelDocs.setRowCount(0);
            if (docs == null || docs.isEmpty()) {
                modelDocs.addRow(new Object[]{"-", "Aucun document", "-", "-", "-"});
                btnOuvrir.setEnabled(false);
                btnTelecharger.setEnabled(false);
                btnRetirerDoc.setEnabled(false);
            } else {
                for (Document d : docs) {
                    System.out.println("Document recu - ID: " + d.getId() + ", Titre: " + d.getTitre() + ", Format: " + d.getFormat());
                    modelDocs.addRow(new Object[]{
                        d.getId(),
                        d.getTitre() != null ? d.getTitre() : "",
                        d.getAuteur() != null ? d.getAuteur() : "",
                        d.getFormat() != null ? d.getFormat().toString() : "",
                        d.isDisponible() ? "OUI" : "NON"
                    });
                }
                btnOuvrir.setEnabled(true);
                btnTelecharger.setEnabled(true);
            }
        } catch (Exception ex) {
            modelDocs.setRowCount(0);
            modelDocs.addRow(new Object[]{"-", "Erreur", "-", "-", "-"});
            btnOuvrir.setEnabled(false);
            btnTelecharger.setEnabled(false);
            btnRetirerDoc.setEnabled(false);
        }
    }
    

    private void rechercherDansListe() {
        String terme = rechercheField.getText().trim();
        if (terme.isEmpty()) {
            voirDocumentsListe();
            return;
        }

        int row = tableListes.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selectionnez d'abord une liste !");
            return;
        }

        try {
            ListeLecture l = mesListes.get(row);
            List<Document> tousDocs = service.getDocumentsDeListe(l.getId());

            List<Document> filtres = tousDocs.stream()
                .filter(d -> {
                    String titre = d.getTitre() != null ? d.getTitre().toLowerCase() : "";
                    String auteur = d.getAuteur() != null ? d.getAuteur().toLowerCase() : "";
                    String recherche = terme.toLowerCase();
                    return titre.contains(recherche) || auteur.contains(recherche);
                })
                .collect(java.util.stream.Collectors.toList());

            modelDocs.setRowCount(0);
            for (Document d : filtres) {
                modelDocs.addRow(new Object[]{
                    d.getId(),
                    d.getTitre(),
                    d.getAuteur(),
                    d.getFormat(),
                    d.isDisponible() ? "OUI" : "NON"
                });
            }

            if (filtres.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucun document ne correspond !");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void ajouterDocument() {
        int row = tableListes.getSelectedRow();
        if (row == -1) return;

        ListeLecture liste = mesListes.get(row);

        try {
            List<Document> tousDocs = documentService.listerTous();
            
            String[] options = tousDocs.stream()
                .map(d -> d.getId() + " - " + d.getTitre())
                .toArray(String[]::new);

            String selected = (String) JOptionPane.showInputDialog(this,
                "Choisir un document a ajouter :",
                "Ajouter un document",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

            if (selected != null) {
                Long docId = Long.parseLong(selected.split(" - ")[0]);
                int selectedRow = tableListes.getSelectedRow();
                try {
                    service.ajouterDocumentDansListe(liste.getId(), docId);
                    JOptionPane.showMessageDialog(this, "Document ajoute a la liste !");
                    tableListes.setRowSelectionInterval(selectedRow, selectedRow);
                    voirDocumentsListe();
                } catch (Exception addEx) {
                    JOptionPane.showMessageDialog(this, "Erreur: " + addEx.getMessage());
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void retirerDocument() {
        int listeRow = tableListes.getSelectedRow();
        int docRow = tableDocs.getSelectedRow();
        
        if (listeRow == -1 || docRow == -1) return;

        try {
            ListeLecture liste = mesListes.get(listeRow);
            List<Document> docs = service.getDocumentsDeListe(liste.getId());
            Document doc = docs.get(docRow);

            int confirm = JOptionPane.showConfirmDialog(this,
                "Retirer '" + doc.getTitre() + "' de la liste ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                service.retirerDocumentDeListe(liste.getId(), doc.getId());
                JOptionPane.showMessageDialog(this, "Document retire !");
                voirDocumentsListe();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void partager() {
        int row = tableListes.getSelectedRow();
        if (row == -1) return;

        ListeLecture l = mesListes.get(row);

        String idStr = JOptionPane.showInputDialog(this,
            "Entrer l'ID de l'utilisateur destinataire :");
        if (idStr == null) return;

        try {
            Long destId = Long.parseLong(idStr);

            Utilisateur dest = new Utilisateur();
            dest.setId(destId);

            ListePartage lp = new ListePartage();
            lp.setListe(l);
            lp.setDestinnataire(dest);

            service.partagerListe(lp);
            JOptionPane.showMessageDialog(this, "Liste partagee avec succes !");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void voirPubliques() {
        try {
            var publiques = service.getListesPubliques();
            modelListes.setRowCount(0);
            mesListes = publiques;
            for (ListeLecture l : publiques) {
                modelListes.addRow(new Object[]{
                    l.getId(), l.getNom(), "OUI", l.getDateCreation()
                });
            }
            
            if (publiques.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucune liste publique trouvee.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void ouvrirDocument() {
        int listeRow = tableListes.getSelectedRow();
        int docRow = tableDocs.getSelectedRow();
        
        if (listeRow == -1 || docRow == -1) return;

        try {
            ListeLecture liste = mesListes.get(listeRow);
            List<Document> docs = service.getDocumentsDeListe(liste.getId());
            Document doc = docs.get(docRow);

            if (!doc.isAccessibleEnligne()) {
                JOptionPane.showMessageDialog(this, "Ce document n'est pas accessible en ligne !");
                return;
            }

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

            if (!Desktop.isDesktopSupported()) {
                JOptionPane.showMessageDialog(this, "Ouverture non supportee !");
                return;
            }

            Desktop.getDesktop().open(fichier);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void telechargerDocument() {
        int listeRow = tableListes.getSelectedRow();
        int docRow = tableDocs.getSelectedRow();
        
        if (listeRow == -1 || docRow == -1) return;

        try {
            ListeLecture liste = mesListes.get(listeRow);
            List<Document> docs = null;
			try {
				docs = service.getDocumentsDeListe(liste.getId());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            Document doc = docs.get(docRow);

            if (!doc.isTelechargable()) {
                JOptionPane.showMessageDialog(this, "Ce document n'est pas telechargeable !");
                return;
            }

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