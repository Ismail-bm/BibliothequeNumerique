package com.bibliotheque.presentation;

import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.service.UtilisateurService;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private UtilisateurService utilisateurService = new UtilisateurService();

    private JTextField      emailField = new JTextField(20);
    private JPasswordField  passField  = new JPasswordField(20);
    private JButton         btnConnecter   = new JButton("Se connecter");
    private JButton         btnInscrire    = new JButton("S'inscrire");

    public LoginFrame() {
        setTitle("Bibliotheque — Connexion");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // Titre
        JLabel titre = new JLabel("Connexion", SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titre, gbc);

        // Email
        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Email :"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        // Mot de passe
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Mot de passe :"), gbc);
        gbc.gridx = 1;
        panel.add(passField, gbc);

        // Boutons
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(btnConnecter, gbc);
        gbc.gridx = 1;
        panel.add(btnInscrire, gbc);

        add(panel);

        // Actions
        btnConnecter.addActionListener(e -> seConnecter());
        btnInscrire.addActionListener(e -> {
            new InscriptionFrame().setVisible(true);
        });
    }

    private void seConnecter() {
        String email = emailField.getText().trim();
        String mdp   = new String(passField.getPassword());

        try {
            // SERVICE verifie email + mdp + compte actif
            Utilisateur u = utilisateurService.connecter(email, mdp);

            // connexion reussie → ouvrir MainFrame avec l'utilisateur
            new MainFrame(u).setVisible(true);
            this.dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new LoginFrame().setVisible(true));
    }
}