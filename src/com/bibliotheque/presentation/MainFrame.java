package com.bibliotheque.presentation;

import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.model.RoleEnum;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private Utilisateur utilisateurConnecte;
    private JPanel menuPanel = new JPanel();
    private JPanel contentPanel = new JPanel(new CardLayout());

    public MainFrame(Utilisateur u) {
        this.utilisateurConnecte = u;

        setTitle("Bibliotheque — " + u.getNom() + " (" + u.getRole() + ")");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 650);
        setLocationRelativeTo(null);

        construireMenu();
        construireContenu();

        add(menuPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void construireMenu() {
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setPreferredSize(new Dimension(190, 0));
        menuPanel.setBackground(new Color(40, 55, 80));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblUser = new JLabel(utilisateurConnecte.getNom());
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new Font("Arial", Font.BOLD, 14));
        lblUser.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuPanel.add(lblUser);

        JLabel lblRole = new JLabel(utilisateurConnecte.getRole().toString());
        lblRole.setForeground(new Color(150, 200, 255));
        lblRole.setFont(new Font("Arial", Font.PLAIN, 11));
        lblRole.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuPanel.add(lblRole);
        menuPanel.add(Box.createVerticalStrut(15));

        ajouterBoutonMenu("Documents", "DOCUMENTS");
        ajouterBoutonMenu("Mes Emprunts", "MES_EMPRUNTS");
        ajouterBoutonMenu("Mon Profil", "PROFIL");

        if (utilisateurConnecte.getRole() == RoleEnum.ENSEIGNANT || 
            utilisateurConnecte.getRole() == RoleEnum.ETUDIANT) {
            ajouterBoutonMenu("Mes Penalites", "MES_PENALITES");

            menuPanel.add(Box.createVerticalStrut(10));
            JLabel lblReco = new JLabel("──── LECTURE ────");
            lblReco.setForeground(new Color(100, 200, 150));
            lblReco.setFont(new Font("Arial", Font.BOLD, 10));
            lblReco.setAlignmentX(Component.CENTER_ALIGNMENT);
            menuPanel.add(lblReco);

            ajouterBoutonMenu("Recommandations", "RECOMMANDATIONS");
            ajouterBoutonMenu("Mes Listes de Lecture", "LISTE_LECTURE");
            ajouterBoutonMenu("Listes Partagees", "LISTES_PARTAGEES");
        }

        if (utilisateurConnecte.getRole() == RoleEnum.BIBLIOTHECAIRE) {
            menuPanel.add(Box.createVerticalStrut(10));
            JLabel lblAdmin = new JLabel("── ADMINISTRATION ──");
            lblAdmin.setForeground(new Color(255, 180, 50));
            lblAdmin.setFont(new Font("Arial", Font.BOLD, 10));
            lblAdmin.setAlignmentX(Component.CENTER_ALIGNMENT);
            menuPanel.add(lblAdmin);

            ajouterBoutonMenu("Gerer Utilisateurs", "GESTION_USERS");
            ajouterBoutonMenu("Gerer Documents", "GESTION_DOCS");
            ajouterBoutonMenu("Gerer Categories", "GESTION_CATEGORIES");
            ajouterBoutonMenu("Emprunts Retard", "EMPRUNTS_RETARD");
            ajouterBoutonMenu("Statistiques", "STATISTIQUES");

            menuPanel.add(Box.createVerticalStrut(10));
            JLabel lblLecture = new JLabel("──── LECTURE ────");
            lblLecture.setForeground(new Color(100, 200, 150));
            lblLecture.setFont(new Font("Arial", Font.BOLD, 10));
            lblLecture.setAlignmentX(Component.CENTER_ALIGNMENT);
            menuPanel.add(lblLecture);

            ajouterBoutonMenu("Mes Listes de Lecture", "LISTE_LECTURE");
            ajouterBoutonMenu("Listes Partagees", "LISTES_PARTAGEES");
        }

        menuPanel.add(Box.createVerticalGlue());

        JButton btnDeco = creerBouton("Deconnexion");
        btnDeco.setBackground(new Color(180, 50, 50));
        btnDeco.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });
        menuPanel.add(btnDeco);
    }

    private void ajouterBoutonMenu(String label, String carte) {
        JButton btn = creerBouton(label);
        btn.addActionListener(e -> {
            CardLayout cl = (CardLayout) contentPanel.getLayout();
            cl.show(contentPanel, carte);
        });
        menuPanel.add(btn);
        menuPanel.add(Box.createVerticalStrut(5));
    }

    private JButton creerBouton(String label) {
        JButton btn = new JButton(label);
        btn.setMaximumSize(new Dimension(170, 32));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(new Color(60, 80, 110));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        return btn;
    }

    private void construireContenu() {
        contentPanel.add(new DocumentPanel(utilisateurConnecte), "DOCUMENTS");
        contentPanel.add(new EmpruntPanel(utilisateurConnecte), "MES_EMPRUNTS");
        contentPanel.add(new ProfilPanel(utilisateurConnecte), "PROFIL");

        if (utilisateurConnecte.getRole() == RoleEnum.ENSEIGNANT || 
            utilisateurConnecte.getRole() == RoleEnum.ETUDIANT) {
            contentPanel.add(new PenalitePanel(utilisateurConnecte), "MES_PENALITES");
        }

        contentPanel.add(new ListeLecturePanel(utilisateurConnecte), "LISTE_LECTURE");
        contentPanel.add(new ListesPartageesPanel(utilisateurConnecte), "LISTES_PARTAGEES");

        if (utilisateurConnecte.getRole() == RoleEnum.ENSEIGNANT || 
            utilisateurConnecte.getRole() == RoleEnum.ETUDIANT) {
            contentPanel.add(new RecommandationPanel(utilisateurConnecte), "RECOMMANDATIONS");
        }

        if (utilisateurConnecte.getRole() == RoleEnum.BIBLIOTHECAIRE) {
            contentPanel.add(new GestionUsersPanel(utilisateurConnecte), "GESTION_USERS");
            contentPanel.add(new GestionDocPanel(utilisateurConnecte), "GESTION_DOCS");
            contentPanel.add(new GestionCategoriePanel(utilisateurConnecte), "GESTION_CATEGORIES");
            contentPanel.add(new EmpruntRetardPanel(utilisateurConnecte), "EMPRUNTS_RETARD");
            contentPanel.add(new StatistiquesPanel(utilisateurConnecte), "STATISTIQUES");
        }

        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "DOCUMENTS");
    }
}