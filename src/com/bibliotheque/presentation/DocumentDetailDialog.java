package com.bibliotheque.presentation;

import com.bibliotheque.model.Document;
import com.bibliotheque.model.Commentaire;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.service.DocumentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class DocumentDetailDialog extends JDialog {

    private DocumentService service = new DocumentService();
    private Document document;
    private Utilisateur utilisateur;
    private JTable tableCommentaires;
    private DefaultTableModel modelCommentaires;
    private List<Commentaire> commentaires;

    private JButton btnOuvrir = new JButton("Ouvrir");
    private JButton btnTelecharger = new JButton("Telecharger");
    private JButton btnEmprunter = new JButton("Emprunter");
    private JButton btnCommenter = new JButton("Ajouter un commentaire");
    private JButton btnFermer = new JButton("Fermer");

    public DocumentDetailDialog(Document doc, Utilisateur u) {
        this.document = doc;
        this.utilisateur = u;
        setTitle("Details du document : " + doc.getTitre());
        setSize(700, 550);
        setLocationRelativeTo(null);
        setModal(true);

        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setContentPane(contentPane);

        construireInterface();
        chargerCommentaires();
        incrementerConsultation();
    }

    private void construireInterface() {
        JPanel nord = construireInfoDocument();
        add(nord, BorderLayout.NORTH);

        JPanel centre = construireCommentaires();
        add(centre, BorderLayout.CENTER);

        JPanel sud = construireBoutons();
        add(sud, BorderLayout.SOUTH);
    }

    private JPanel construireInfoDocument() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Informations du document"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Titre :"), gbc);
        gbc.gridx = 1;
        JLabel titreLabel = new JLabel(document.getTitre() != null ? document.getTitre() : "");
        titreLabel.setFont(new Font("Arial", Font.BOLD, 13));
        panel.add(titreLabel, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Auteur :"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(document.getAuteur() != null ? document.getAuteur() : ""), gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Format :"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(document.getFormat() != null ? document.getFormat().toString() : ""), gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Disponible :"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(document.isDisponible() ? "OUI" : "NON"), gbc);

        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Telechargeable :"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(document.isTelechargable() ? "OUI" : "NON"), gbc);

        gbc.gridx = 0; gbc.gridy = 5;
        panel.add(new JLabel("Accessible en ligne :"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(document.isAccessibleEnligne() ? "OUI" : "NON"), gbc);

        gbc.gridx = 0; gbc.gridy = 6;
        panel.add(new JLabel("Nombre de consultations :"), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(String.valueOf(document.getNbrConsultations())), gbc);

        if (document.getResume() != null && !document.getResume().isEmpty()) {
            gbc.gridx = 0; gbc.gridy = 7;
            gbc.anchor = GridBagConstraints.NORTHWEST;
            panel.add(new JLabel("Resume :"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            JTextArea resumeArea = new JTextArea(document.getResume(), 3, 30);
            resumeArea.setEditable(false);
            resumeArea.setLineWrap(true);
            resumeArea.setWrapStyleWord(true);
            panel.add(new JScrollPane(resumeArea), gbc);
        }

        try {
            double noteMoyenne = service.getNoteMoyenneDocument(document.getId());
            gbc.gridx = 0; gbc.gridy = 8;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(new JLabel("Note moyenne :"), gbc);
            gbc.gridx = 1;
            panel.add(new JLabel(String.format("%.2f / 5", noteMoyenne)), gbc);
        } catch (Exception e) {
        }

        return panel;
    }

    private JPanel construireCommentaires() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Commentaires"));

        String[] colonnes = {"Utilisateur", "Note", "Date", "Contenu"};
        modelCommentaires = new DefaultTableModel(colonnes, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableCommentaires = new JTable(modelCommentaires);
        tableCommentaires.setRowHeight(25);
        panel.add(new JScrollPane(tableCommentaires), BorderLayout.CENTER);

        return panel;
    }

    private JPanel construireBoutons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        btnOuvrir.setBackground(new Color(0, 120, 200));
        btnOuvrir.setForeground(Color.WHITE);
        btnTelecharger.setBackground(new Color(0, 150, 80));
        btnTelecharger.setForeground(Color.WHITE);
        btnEmprunter.setBackground(new Color(100, 100, 180));
        btnEmprunter.setForeground(Color.WHITE);
        btnCommenter.setBackground(new Color(150, 100, 50));
        btnCommenter.setForeground(Color.WHITE);

        boolean peutOuvrir = document.isAccessibleEnligne() && 
                             document.getCheminFichier() != null && 
                             !document.getCheminFichier().isEmpty();
        boolean peutTelecharger = document.isTelechargable() && 
                                   document.getCheminFichier() != null && 
                                   !document.getCheminFichier().isEmpty();
        
        btnOuvrir.setEnabled(peutOuvrir);
        btnTelecharger.setEnabled(peutTelecharger);
        btnEmprunter.setEnabled(document.isDisponible());

        panel.add(btnCommenter);
        panel.add(btnEmprunter);
        panel.add(btnOuvrir);
        panel.add(btnTelecharger);
        panel.add(btnFermer);

        btnOuvrir.addActionListener(e -> ouvrirDocument());
        btnTelecharger.addActionListener(e -> telechargerDocument());
        btnEmprunter.addActionListener(e -> emprunterDocument());
        btnCommenter.addActionListener(e -> ajouterCommentaire());
        btnFermer.addActionListener(e -> dispose());

        return panel;
    }

    private void chargerCommentaires() {
        try {
            commentaires = service.getCommentairesDocument(document.getId());
            modelCommentaires.setRowCount(0);
            for (Commentaire c : commentaires) {
                String nomUser = c.getUtilisateur() != null ? c.getUtilisateur().getNom() : "Inconnu";
                String contenu = c.getContenu() != null ? c.getContenu() : "";
                modelCommentaires.addRow(new Object[]{
                    nomUser,
                    c.getNote() + "/5",
                    c.getDateCreation(),
                    contenu.length() > 50 ? contenu.substring(0, 50) + "..." : contenu
                });
            }
        } catch (Exception ex) {
        }
    }

    private void incrementerConsultation() {
        try {
            service.incrementerConsultation(document.getId());
            document.setNbrConsultations(document.getNbrConsultations() + 1);
        } catch (Exception e) {
        }
    }

    private void ouvrirDocument() {
        if (!document.isAccessibleEnligne()) {
            JOptionPane.showMessageDialog(this, "Ce document n'est pas accessible en ligne !");
            return;
        }

        String chemin = document.getCheminFichier();
        if (chemin == null || chemin.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chemin du fichier introuvable !");
            return;
        }

        File fichier = new File(chemin);
        if (!fichier.exists()) {
            JOptionPane.showMessageDialog(this, "Fichier introuvable sur le disque !", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(this, "Ouverture non supportee sur ce systeme !");
            return;
        }

        try {
            Desktop.getDesktop().open(fichier);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Impossible d'ouvrir le fichier !" + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void telechargerDocument() {
        if (!document.isTelechargable()) {
            JOptionPane.showMessageDialog(this, "Ce document n'est pas telechargeable !");
            return;
        }

        String chemin = document.getCheminFichier();
        if (chemin == null || chemin.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chemin du fichier introuvable !");
            return;
        }

        File source = new File(chemin);
        if (!source.exists()) {
            JOptionPane.showMessageDialog(this, "Fichier introuvable sur le disque !", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(source.getName()));
        fileChooser.setDialogTitle("Enregistrer le document");

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File destination = fileChooser.getSelectedFile();
            try {
                java.nio.file.Files.copy(source.toPath(), destination.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this, "Document telecharge avec succes !");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erreur lors du telechargement ! " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void emprunterDocument() {
        new EmpruntDialog(utilisateur, document).setVisible(true);
        dispose();
    }

    private void ajouterCommentaire() {
        new CommentaireDialog(utilisateur, document).setVisible(true);
        chargerCommentaires();
    }
}