package com.bibliotheque.presentation;

import com.bibliotheque.model.Emprunt;
import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.service.EmpruntService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EmpruntPanel extends JPanel {

    private EmpruntService    service = new EmpruntService();
    private Utilisateur       utilisateurConnecte;

    private JTable            table;
    private DefaultTableModel tableModel;
    private List<Emprunt>     emprunts;

    private JButton btnRetourner = new JButton("Retourner le document");
    private JButton btnRefresh   = new JButton("Rafraichir");
    private JButton btnEnCours   = new JButton("En cours");
    private JButton btnTous      = new JButton("Tous");

    public EmpruntPanel(Utilisateur u) {
        this.utilisateurConnecte = u;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        construireTableau();
        construireBoutons();
        chargerMesEmprunts();
    }

    private void construireTableau() {
        JLabel titre = new JLabel("Mes Emprunts", SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 16));
        add(titre, BorderLayout.NORTH);

        String[] colonnes = {"ID", "Document", "Date Emprunt",
                             "Date Retour Prevue", "Date Retour Reelle",
                             "Statut", "En Retard ?"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void construireBoutons() {
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(btnEnCours);
        btnPanel.add(btnTous);
        btnPanel.add(btnRefresh);
        btnPanel.add(btnRetourner);

        btnEnCours.addActionListener(e -> chargerEnCours());
        btnTous.addActionListener(e -> chargerMesEmprunts());
        btnRefresh.addActionListener(e -> chargerMesEmprunts());
        btnRetourner.addActionListener(e -> retourner());

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void chargerMesEmprunts() {
        try {
            emprunts = service.getMesEmprunts(utilisateurConnecte.getId());
            remplirTableau(emprunts);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void chargerEnCours() {
        try {
            emprunts = service.getMesEmpruntsEnCours(
                utilisateurConnecte.getId());
            remplirTableau(emprunts);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void remplirTableau(List<Emprunt> liste) {
        tableModel.setRowCount(0);
        for (Emprunt e : liste) {
            tableModel.addRow(new Object[]{
                e.getId(),
                // getDocument() retourne objet Document
                e.getDocument().getId(),
                e.getDateEmprunt(),
                e.getDateRetourPreveue(),
                e.getDateRetourReelle() != null
                    ? e.getDateRetourReelle() : "Non rendu",
                e.getStatut(),
                // estEnRetard() methode du modele Emprunt
                e.estEnRetard() ? "OUI" : "NON"
            });
        }
    }

    private void retourner() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez selectionner un emprunt !");
            return;
        }

        Emprunt emprunt = emprunts.get(row);

        // afficher les jours de retard si applicable
        // calculernbrJoursRetard() methode du modele Emprunt
        String msg = "Retourner ce document ?";
        if (emprunt.estEnRetard()) {
            msg += "\nATTENTION : " + emprunt.calculernbrJoursRetard()
                + " jours de retard !";
        }

        int confirm = JOptionPane.showConfirmDialog(this, msg,
            "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // SERVICE applique retourner() + gere la penalite
                service.retournerDocument(emprunt.getId());
                JOptionPane.showMessageDialog(this,
                    "Document retourne avec succes !");
                chargerMesEmprunts();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }
}