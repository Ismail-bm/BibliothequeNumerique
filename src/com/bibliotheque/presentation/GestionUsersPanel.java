package com.bibliotheque.presentation;

import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.model.RoleEnum;
import com.bibliotheque.service.UtilisateurService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class GestionUsersPanel extends JPanel {

    private UtilisateurService service = new UtilisateurService();
    private Utilisateur utilisateurConnecte;

    private JTable            table;
    private DefaultTableModel tableModel;
    private List<Utilisateur> utilisateurs;

    private JButton btnActiver    = new JButton("Activer / Desactiver");
    private JButton btnRole       = new JButton("Changer Role");
    private JButton btnSupprimer  = new JButton("Supprimer");
    private JButton btnRefresh    = new JButton("Rafraichir");

    public GestionUsersPanel(Utilisateur u) {
        this.utilisateurConnecte = u;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // SWING verifie le role BIBLIOTHECAIRE
        if (!utilisateurConnecte.estBibLiothecaire()) {
            add(new JLabel("Acces refuse !", SwingConstants.CENTER));
            return;
        }

        construireTableau();
        construireBoutons();
        charger();
    }

    private void construireTableau() {
        JLabel titre = new JLabel("Gestion des Utilisateurs",
            SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 16));
        add(titre, BorderLayout.NORTH);

        String[] colonnes = {"ID", "Nom", "Email", "Role",
                             "Actif", "Max Emprunts"};
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
        btnPanel.add(btnActiver);
        btnPanel.add(btnRole);
        btnPanel.add(btnSupprimer);

        btnRefresh.addActionListener(e -> charger());
        btnActiver.addActionListener(e -> activerDesactiver());
        btnRole.addActionListener(e -> changerRole());
        btnSupprimer.addActionListener(e -> supprimer());

        add(btnPanel, BorderLayout.SOUTH);
    }

    private void charger() {
        try {
            utilisateurs = service.listerTous();
            tableModel.setRowCount(0);
            for (Utilisateur u : utilisateurs) {
                tableModel.addRow(new Object[]{
                    u.getId(),
                    u.getNom(),
                    u.getEmail(),
                    u.getRole(),
                    u.isActif() ? "OUI" : "NON",
                    u.getMaxEmprunt()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private Utilisateur getSelectionne() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez selectionner un utilisateur !");
            return null;
        }
        return utilisateurs.get(row);
    }

    private void activerDesactiver() {
        Utilisateur u = getSelectionne();
        if (u == null) return;

        String action = u.isActif() ? "desactiver" : "activer";
        int confirm = JOptionPane.showConfirmDialog(this,
            "Voulez-vous " + action + " " + u.getNom() + " ?",
            "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // SERVICE fait la verification et l'inverse l'etat
                service.activerDesactiver(u.getId());
                JOptionPane.showMessageDialog(this, "Compte mis a jour !");
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void changerRole() {
        Utilisateur u = getSelectionne();
        if (u == null) return;

        RoleEnum[] roles = RoleEnum.values();
        RoleEnum nouveauRole = (RoleEnum) JOptionPane.showInputDialog(
            this, "Choisir le nouveau role :",
            "Changer Role", JOptionPane.QUESTION_MESSAGE,
            null, roles, u.getRole());

        if (nouveauRole != null) {
            try {
                // SERVICE recalcule max_emprunts selon le nouveau role
                service.changerRole(u.getId(), nouveauRole);
                JOptionPane.showMessageDialog(this, "Role modifie !");
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }

    private void supprimer() {
        Utilisateur u = getSelectionne();
        if (u == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Supprimer definitivement " + u.getNom() + " ?",
            "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // SERVICE refuse de supprimer un BIBLIOTHECAIRE
                service.supprimer(u.getId());
                JOptionPane.showMessageDialog(this, "Utilisateur supprime !");
                charger();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
    }
}