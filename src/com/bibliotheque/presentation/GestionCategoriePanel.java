package com.bibliotheque.presentation;

import com.bibliotheque.model.Categorie;
import com.bibliotheque.model.Document;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.service.DocumentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class GestionCategoriePanel extends JPanel {

    private DocumentService service = new DocumentService();
    private Utilisateur utilisateurConnecte;

    private JTable tableCategories;
    private DefaultTableModel modelCategories;
    private List<Categorie> categories;

    private JButton btnAjouter = new JButton("Ajouter categorie");
    private JButton btnModifier = new JButton("Modifier");
    private JButton btnSupprimer = new JButton("Supprimer");
    private JButton btnAjouterDoc = new JButton("Ajouter document");
    private JButton btnRetirerDoc = new JButton("Retirer document");
    private JButton btnRefresh = new JButton("Rafraichir");

    public GestionCategoriePanel(Utilisateur u) {
        this.utilisateurConnecte = u;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (!utilisateurConnecte.estBibLiothecaire()) {
            add(new JLabel("Acces refuse !", SwingConstants.CENTER));
            return;
        }

        construireTableau();
        construireBoutons();
        charger();
    }

    private void construireTableau() {
        JLabel titre = new JLabel("Gestion des Categories", SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 16));
        add(titre, BorderLayout.NORTH);

        String[] colonnes = {"ID", "Nom", "Description"};
        modelCategories = new DefaultTableModel(colonnes, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tableCategories = new JTable(modelCategories);
        tableCategories.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        tableCategories.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tableCategories.getSelectedRow();
                boolean selection = row != -1;
                btnModifier.setEnabled(selection);
                btnSupprimer.setEnabled(selection);
                btnAjouterDoc.setEnabled(selection);
                btnRetirerDoc.setEnabled(selection);
            }
        });
        
        add(new JScrollPane(tableCategories), BorderLayout.CENTER);
    }

    private void construireBoutons() {
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(btnRefresh);
        btnPanel.add(btnAjouter);
        btnPanel.add(btnModifier);
        btnPanel.add(btnSupprimer);
        btnPanel.add(new JSeparator(SwingConstants.VERTICAL));
        btnPanel.add(btnAjouterDoc);
        btnPanel.add(btnRetirerDoc);

        btnRefresh.addActionListener(e -> charger());
        btnAjouter.addActionListener(e -> ajouterCategorie());
        btnModifier.addActionListener(e -> modifierCategorie());
        btnSupprimer.addActionListener(e -> supprimerCategorie());
        btnAjouterDoc.addActionListener(e -> ajouterDocumentDansCategorie());
        btnRetirerDoc.addActionListener(e -> retirerDocumentDeCategorie());

        btnModifier.setEnabled(false);
        btnSupprimer.setEnabled(false);
        btnAjouterDoc.setEnabled(false);
        btnRetirerDoc.setEnabled(false);

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void charger() {
        try {
            categories = service.listerCategories();
            modelCategories.setRowCount(0);
            for (Categorie c : categories) {
                String desc = c.getDescription() != null ? c.getDescription() : "";
                modelCategories.addRow(new Object[]{
                    c.getId(),
                    c.getNom(),
                    desc.length() > 50 ? desc.substring(0, 50) + "..." : desc
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private Categorie getSelectionne() {
        int row = tableCategories.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selectionnez une categorie !");
            return null;
        }
        return categories.get(row);
    }

    private void ajouterCategorie() {
        JTextField nomField = new JTextField(15);
        JTextArea descArea = new JTextArea(3, 20);

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Nom de la categorie :")); form.add(nomField);
        form.add(new JLabel("Description :")); form.add(new JScrollPane(descArea));

        int result = JOptionPane.showConfirmDialog(this, form,
            "Ajouter une categorie", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            if (nomField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Le nom est obligatoire !");
                return;
            }

            Categorie c = new Categorie();
            c.setNom(nomField.getText().trim());
            c.setDescription(descArea.getText().trim());

            try {
                service.ajouterCategorie(c);
                JOptionPane.showMessageDialog(this, "Categorie ajoutee !");
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void modifierCategorie() {
        Categorie c = getSelectionne();
        if (c == null) return;

        JTextField nomField = new JTextField(c.getNom() != null ? c.getNom() : "", 15);
        JTextArea descArea = new JTextArea(c.getDescription() != null ? c.getDescription() : "", 3, 20);

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Nom :")); form.add(nomField);
        form.add(new JLabel("Description :")); form.add(new JScrollPane(descArea));

        int result = JOptionPane.showConfirmDialog(this, form,
            "Modifier la categorie", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            if (nomField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Le nom est obligatoire !");
                return;
            }
            
            c.setNom(nomField.getText().trim());
            c.setDescription(descArea.getText().trim());
            
            try {
                service.modifierCategorie(c);
                JOptionPane.showMessageDialog(this, "Categorie modifiee !");
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void supprimerCategorie() {
        Categorie c = getSelectionne();
        if (c == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Voulez-vous supprimer la categorie '" + c.getNom() + "' ?",
            "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.supprimerCategorie(c.getId());
                JOptionPane.showMessageDialog(this, "Categorie supprimee !");
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void ajouterDocumentDansCategorie() {
        Categorie c = getSelectionne();
        if (c == null) return;

        try {
            List<Document> docs = service.listerTous();
            
            String[] options = docs.stream()
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
                service.ajouterDocumentDansCategorie(c.getId(), docId);
                JOptionPane.showMessageDialog(this, "Document ajoute a la categorie !");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void retirerDocumentDeCategorie() {
        Categorie c = getSelectionne();
        if (c == null) return;

        try {
            List<Document> docs = service.rechercherParCategorie(c.getId());

            if (docs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucun document dans cette categorie !");
                return;
            }

            String[] options = docs.stream()
                .map(d -> d.getId() + " - " + d.getTitre())
                .toArray(String[]::new);

            String selected = (String) JOptionPane.showInputDialog(this,
                "Choisir un document a retirer :",
                "Retirer un document de la categorie",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

            if (selected != null) {
                Long docId = Long.parseLong(selected.split(" - ")[0]);
                service.retirerDocumentDeCategorie(c.getId(), docId);
                JOptionPane.showMessageDialog(this, "Document retire de la categorie !");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}