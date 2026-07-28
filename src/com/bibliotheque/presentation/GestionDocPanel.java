package com.bibliotheque.presentation;

import com.bibliotheque.model.Document;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.model.FormatEnum;
import com.bibliotheque.service.DocumentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class GestionDocPanel extends JPanel {

    private DocumentService   service = new DocumentService();
    private Utilisateur       utilisateurConnecte;

    private JTable            table;
    private DefaultTableModel tableModel;
    private List<Document>    documents;

    private JButton btnAjouter   = new JButton("Ajouter");
    private JButton btnModifier  = new JButton("Modifier");
    private JButton btnSupprimer = new JButton("Supprimer");
    private JButton btnRefresh   = new JButton("Rafraichir");

    public GestionDocPanel(Utilisateur u) {
        this.utilisateurConnecte = u;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // SWING verifie le role
        if (!utilisateurConnecte.estBibLiothecaire()) {
            add(new JLabel("Acces refuse !", SwingConstants.CENTER));
            return;
        }

        construireTableau();
        construireBoutons();
        charger();
    }

    private void construireTableau() {
        JLabel titre = new JLabel("Gestion des Documents",
            SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 16));
        add(titre, BorderLayout.NORTH);

        String[] colonnes = {"ID", "Titre", "Auteur",
                             "Format", "Disponible"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void construireBoutons() {
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(btnRefresh);
        btnPanel.add(btnAjouter);
        btnPanel.add(btnModifier);
        btnPanel.add(btnSupprimer);

        btnRefresh.addActionListener(e -> charger());
        btnAjouter.addActionListener(e -> ajouterDocument());
        btnModifier.addActionListener(e -> modifierDocument());
        btnSupprimer.addActionListener(e -> supprimerDocument());

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void charger() {
        try {
            documents = service.listerTous();
            tableModel.setRowCount(0);
            for (Document d : documents) {
                tableModel.addRow(new Object[]{
                    d.getId(),
                    d.getTitre(),
                    d.getAuteur(),
                    d.getFormat(),
                    d.isDisponible() ? "OUI" : "NON"
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private Document getSelectionne() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez selectionner un document !");
            return null;
        }
        return documents.get(row);
    }

    private void ajouterDocument() {
        // SWING verifie connexion + role avant tout
        if (!utilisateurConnecte.estBibLiothecaire()) {
            JOptionPane.showMessageDialog(this, "Acces refuse !");
            return;
        }

        // formulaire d'ajout
        JTextField titreField   = new JTextField(15);
        JTextField auteurField  = new JTextField(15);
        JTextField cheminField  = new JTextField(15);
        JTextField resumeField  = new JTextField(15);
        JComboBox<FormatEnum> formatCombo =
            new JComboBox<>(FormatEnum.values());
        JCheckBox  telechargeCB = new JCheckBox("Telechargeable");
        JCheckBox  ligneCB      = new JCheckBox("Accessible en ligne");

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Titre :")); form.add(titreField);
        form.add(new JLabel("Auteur :")); form.add(auteurField);
        form.add(new JLabel("Format :")); form.add(formatCombo);
        form.add(new JLabel("Chemin fichier :")); form.add(cheminField);
        form.add(new JLabel("Resume :")); form.add(resumeField);
        form.add(telechargeCB); form.add(ligneCB);

        int result = JOptionPane.showConfirmDialog(this, form,
            "Ajouter un document", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            // SWING construit l'objet Document
            Document d = new Document();
            d.setTitre(titreField.getText().trim());
            d.setAuteur(auteurField.getText().trim());
            d.setFormat((FormatEnum) formatCombo.getSelectedItem());
            d.setCheminFichier(cheminField.getText().trim());
            d.setResume(resumeField.getText().trim());
            d.setTelechargable(telechargeCB.isSelected());
            d.setAccessibleEnligne(ligneCB.isSelected());

            try {
                // SERVICE verifie les regles metier
                service.ajouterDocument(d);
                JOptionPane.showMessageDialog(this, "Document ajoute !");
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void modifierDocument() {
        Document doc = getSelectionne();
        if (doc == null) return;

        JTextField titreField  = new JTextField(doc.getTitre(), 15);
        JTextField auteurField = new JTextField(doc.getAuteur(), 15);

        JPanel form = new JPanel(new GridLayout(0, 2, 5, 5));
        form.add(new JLabel("Titre :"));  form.add(titreField);
        form.add(new JLabel("Auteur :")); form.add(auteurField);

        int result = JOptionPane.showConfirmDialog(this, form,
            "Modifier le document", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            doc.setTitre(titreField.getText().trim());
            doc.setAuteur(auteurField.getText().trim());

            try {
                service.modifierDocument(doc);
                JOptionPane.showMessageDialog(this, "Document modifie !");
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void supprimerDocument() {
        Document doc = getSelectionne();
        if (doc == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Supprimer \"" + doc.getTitre() + "\" ?",
            "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // SERVICE refuse si document est emprunte
                service.supprimerDocument(doc.getId());
                JOptionPane.showMessageDialog(this, "Document supprime !");
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }
}