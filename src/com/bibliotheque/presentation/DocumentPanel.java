package com.bibliotheque.presentation;

import com.bibliotheque.model.Document;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.model.FormatEnum;
import com.bibliotheque.model.ListeLecture;
import com.bibliotheque.service.DocumentService;
import com.bibliotheque.service.EmpruntService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class DocumentPanel extends JPanel {

    private DocumentService   documentService = new DocumentService();
    private EmpruntService    empruntService = new EmpruntService();
    private Utilisateur        utilisateurConnecte;
    private JTable            table;
    private DefaultTableModel tableModel;
    private List<Document>     documentsAffiches;

    private JTextField        rechercheField  = new JTextField(20);
    private JComboBox<String> typeRecherche   = new JComboBox<>(
        new String[]{"Titre", "Auteur", "Mot cle", "Plusieurs mots"});
    
    private JComboBox<FormatEnum> formatFilter = new JComboBox<>(new FormatEnum[]{
        null, FormatEnum.PDF, FormatEnum.DOCX
    });
    private JComboBox<String> dispoFilter = new JComboBox<>(
        new String[]{"Tous", "Disponible", "Non disponible"});

    private JButton btnRechercher  = new JButton("Rechercher");
    private JButton btnTous        = new JButton("Tous");
    private JButton btnDisponibles = new JButton("Disponibles");
    private JButton btnOuvrir      = new JButton("Ouvrir");
    private JButton btnTelecharger = new JButton("Telecharger");
    private JButton btnEmprunter   = new JButton("Emprunter");
    private JButton btnRetourner   = new JButton("Retourner");
    private JButton btnDetails     = new JButton("Details");
    private JButton btnCommenter   = new JButton("Commenter");
    private JButton btnAjouterListe = new JButton("Ajouter a une liste");
    private JButton btnAppliquerFiltre = new JButton("Appliquer filtres");

    public DocumentPanel(Utilisateur u) {
        this.utilisateurConnecte = u;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        construireRecherche();
        construireTableau();
        construireBoutons();
        chargerTousDocuments();
    }

    private void construireRecherche() {
        JPanel rechPanel = new JPanel();
        rechPanel.setLayout(new BoxLayout(rechPanel, BoxLayout.Y_AXIS));
        rechPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel ligne1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ligne1.add(new JLabel("Rechercher :"));
        ligne1.add(typeRecherche);
        ligne1.add(rechercheField);
        ligne1.add(btnRechercher);
        ligne1.add(btnTous);
        ligne1.add(btnDisponibles);

        JPanel ligne2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ligne2.add(new JLabel("Format :"));
        ligne2.add(formatFilter);
        ligne2.add(new JLabel("Disponibilite :"));
        ligne2.add(dispoFilter);
        ligne2.add(btnAppliquerFiltre);

        rechPanel.add(ligne1);
        rechPanel.add(ligne2);

        btnRechercher.addActionListener(e -> rechercher());
        btnTous.addActionListener(e -> chargerTousDocuments());
        btnDisponibles.addActionListener(e -> chargerDisponibles());
        btnAppliquerFiltre.addActionListener(e -> appliquerFiltres());

        add(rechPanel, BorderLayout.NORTH);
    }

    private void construireTableau() {
        String[] colonnes = {
            "ID", "Titre", "Auteur", "Format",
            "Disponible", "Telechargeable", "En Ligne", "Chemin"
        };
        tableModel = new DefaultTableModel(colonnes, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(22);

        table.getColumnModel().getColumn(7).setMinWidth(0);
        table.getColumnModel().getColumn(7).setMaxWidth(0);
        table.getColumnModel().getColumn(7).setWidth(0);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Document doc = getDocumentSelectionne();
                if (doc != null) {
                    boolean peutOuvrir = doc.isAccessibleEnligne() &&
                        doc.getCheminFichier() != null &&
                        !doc.getCheminFichier().isEmpty();
                    boolean peutTel = doc.isTelechargable() &&
                        doc.getCheminFichier() != null &&
                        !doc.getCheminFichier().isEmpty();

                    btnOuvrir.setEnabled(peutOuvrir);
                    btnTelecharger.setEnabled(peutTel);
                    btnEmprunter.setEnabled(doc.isDisponible());
                    btnDetails.setEnabled(true);
                }
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void construireBoutons() {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnDetails.setBackground(new Color(80, 80, 120));
        btnDetails.setForeground(Color.WHITE);
        
        btnOuvrir.setBackground(new Color(0, 120, 200));
        btnOuvrir.setForeground(Color.WHITE);
        btnOuvrir.setEnabled(false);

        btnTelecharger.setBackground(new Color(0, 150, 80));
        btnTelecharger.setForeground(Color.WHITE);
        btnTelecharger.setEnabled(false);

        btnEmprunter.setBackground(new Color(100, 100, 180));
        btnEmprunter.setForeground(Color.WHITE);
        btnEmprunter.setEnabled(false);

        btnRetourner.setBackground(new Color(120, 80, 80));
        btnRetourner.setForeground(Color.WHITE);

        btnCommenter.setBackground(new Color(150, 100, 50));
        btnCommenter.setForeground(Color.WHITE);

        btnAjouterListe.setBackground(new Color(70, 120, 70));
        btnAjouterListe.setForeground(Color.WHITE);

        btnPanel.add(btnDetails);
        btnPanel.add(btnAjouterListe);
        btnPanel.add(btnCommenter);
        btnPanel.add(btnEmprunter);
        btnPanel.add(btnRetourner);
        btnPanel.add(btnOuvrir);
        btnPanel.add(btnTelecharger);

        btnDetails.addActionListener(e -> voirDetails());
        btnAjouterListe.addActionListener(e -> ajouterAListe());
        btnCommenter.addActionListener(e -> commenter());
        btnEmprunter.addActionListener(e -> emprunter());
        btnRetourner.addActionListener(e -> retourner());
        btnOuvrir.addActionListener(e -> ouvrirDocument());
        btnTelecharger.addActionListener(e -> telechargerDocument());

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void voirDetails() {
        Document doc = getDocumentSelectionne();
        if (doc == null) {
            JOptionPane.showMessageDialog(this, "Veuillez selectionner un document !");
            return;
        }
        new DocumentDetailDialog(doc, utilisateurConnecte).setVisible(true);
    }

    private void ajouterAListe() {
        Document doc = getDocumentSelectionne();
        if (doc == null) {
            JOptionPane.showMessageDialog(this, "Veuillez selectionner un document !");
            return;
        }

        try {
            List<ListeLecture> listes = empruntService.getMesListesLecture(utilisateurConnecte.getId());
            if (listes.isEmpty()) {
                int choix = JOptionPane.showConfirmDialog(this,
                    "Vous n'avez pas de listes de lecture. Voulez-vous en creer une ?",
                    "Aucune liste", JOptionPane.YES_NO_OPTION);
                if (choix == JOptionPane.YES_OPTION) {
                    creerEtAjouterAListe(doc);
                }
                return;
            }

            String[] nomsListes = listes.stream()
                .map(l -> l.getNom())
                .toArray(String[]::new);

            String selected = (String) JOptionPane.showInputDialog(this,
                "Choisir une liste de lecture :",
                "Ajouter a une liste",
                JOptionPane.QUESTION_MESSAGE,
                null,
                nomsListes,
                nomsListes[0]);

            if (selected != null) {
                ListeLecture listeChoisie = listes.stream()
                    .filter(l -> l.getNom().equals(selected))
                    .findFirst()
                    .orElse(null);

                if (listeChoisie != null) {
                    empruntService.ajouterDocumentDansListe(listeChoisie.getId(), doc.getId());
                    JOptionPane.showMessageDialog(this, "Document ajoute a la liste '" + selected + "' !");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void creerEtAjouterAListe(Document doc) {
        JTextField nomField = new JTextField(15);
        JCheckBox publicCB = new JCheckBox("Rendre publique ?");

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Nom de la liste :"));
        form.add(nomField);
        form.add(publicCB);

        int result = JOptionPane.showConfirmDialog(this, form,
            "Creer une liste", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                ListeLecture l = new ListeLecture(
                    nomField.getText().trim(),
                    utilisateurConnecte.getId(),
                    publicCB.isSelected()
                );
                empruntService.creerListeLecture(l);

                List<ListeLecture> listes = empruntService.getMesListesLecture(utilisateurConnecte.getId());
                ListeLecture nouvelleListe = listes.get(listes.size() - 1);
                empruntService.ajouterDocumentDansListe(nouvelleListe.getId(), doc.getId());

                JOptionPane.showMessageDialog(this, "Liste creee et document ajoute !");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void ouvrirDocument() {
        Document doc = getDocumentSelectionne();
        if (doc == null) return;

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
            JOptionPane.showMessageDialog(this, "Fichier introuvable !\nChemin : " + chemin, "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(this, "Ouverture non supportee !");
            return;
        }

        try {
            Desktop.getDesktop().open(fichier);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void telechargerDocument() {
        Document doc = getDocumentSelectionne();
        if (doc == null) return;

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
            try {
                Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this, "Document telecharge avec succes !");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void remplirTableau(List<Document> liste) {
        tableModel.setRowCount(0);
        for (Document d : liste) {
            tableModel.addRow(new Object[]{
                d.getId(),
                d.getTitre(),
                d.getAuteur(),
                d.getFormat(),
                d.isDisponible() ? "OUI" : "NON",
                d.isTelechargable() ? "OUI" : "NON",
                d.isAccessibleEnligne() ? "OUI" : "NON",
                d.getCheminFichier()
            });
        }
        btnOuvrir.setEnabled(false);
        btnTelecharger.setEnabled(false);
        btnEmprunter.setEnabled(false);
        btnDetails.setEnabled(false);
    }

    private Document getDocumentSelectionne() {
        int row = table.getSelectedRow();
        if (row == -1 || documentsAffiches == null || row >= documentsAffiches.size()) return null;
        return documentsAffiches.get(row);
    }

    private void chargerTousDocuments() {
        try {
            documentsAffiches = documentService.listerTous();
            remplirTableau(documentsAffiches);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void chargerDisponibles() {
        try {
            documentsAffiches = documentService.listerDisponibles();
            remplirTableau(documentsAffiches);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void rechercher() {
        String terme = rechercheField.getText().trim();
        if (terme.isEmpty()) {
            chargerTousDocuments();
            return;
        }

        String type = (String) typeRecherche.getSelectedItem();
        try {
            switch (type) {
                case "Titre" -> documentsAffiches = documentService.rechercherParTitre(terme);
                case "Auteur" -> documentsAffiches = documentService.rechercherParAuteur(terme);
                case "Mot cle" -> documentsAffiches = documentService.rechercherParMotCle(terme);
                case "Plusieurs mots" -> documentsAffiches = documentService.rechercherParPlusieursMots(terme);
            }
            remplirTableau(documentsAffiches);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void appliquerFiltres() {
        try {
            List<Document> tous = documentService.listerTous();
            FormatEnum formatChoisi = (FormatEnum) formatFilter.getSelectedItem();
            String dispoChoisi = (String) dispoFilter.getSelectedItem();

            List<Document> filtres = tous.stream()
                .filter(d -> {
                    if (formatChoisi != null && d.getFormat() != formatChoisi) return false;
                    if (dispoChoisi.equals("Disponible") && !d.isDisponible()) return false;
                    if (dispoChoisi.equals("Non disponible") && d.isDisponible()) return false;
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());

            documentsAffiches = filtres;
            remplirTableau(documentsAffiches);

            if (filtres.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucun document ne correspond aux filtres !");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void emprunter() {
        Document doc = getDocumentSelectionne();
        if (doc == null) {
            JOptionPane.showMessageDialog(this, "Veuillez selectionner un document !");
            return;
        }
        new EmpruntDialog(utilisateurConnecte, doc).setVisible(true);
    }

    private void retourner() {
        try {
            List<com.bibliotheque.model.Emprunt> emprunts = empruntService.getMesEmpruntsEnCours(utilisateurConnecte.getId());
            if (emprunts.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vous n'avez aucun emprunt en cours !");
                return;
            }

            String[] options = emprunts.stream()
                .map(e -> "Doc ID: " + e.getDocument().getId() + " - " + e.getDocument().getTitre())
                .toArray(String[]::new);

            String selected = (String) JOptionPane.showInputDialog(this,
                "Selectionner un document a retourner :",
                "Retourner un document",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

            if (selected != null) {
                int index = java.util.Arrays.asList(options).indexOf(selected);
                com.bibliotheque.model.Emprunt emp = emprunts.get(index);

                String msg = "Retourner ce document ?";
                if (emp.estEnRetard()) {
                    msg += "\nATTENTION : " + emp.calculernbrJoursRetard() + " jours de retard !";
                }

                int confirm = JOptionPane.showConfirmDialog(this, msg, "Confirmation", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    empruntService.retournerDocument(emp.getId());
                    JOptionPane.showMessageDialog(this, "Document retourne avec succes !");
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void commenter() {
        Document doc = getDocumentSelectionne();
        if (doc == null) {
            JOptionPane.showMessageDialog(this, "Veuillez selectionner un document !");
            return;
        }
        new CommentaireDialog(utilisateurConnecte, doc).setVisible(true);
    }
}