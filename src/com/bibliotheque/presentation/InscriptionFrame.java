package com.bibliotheque.presentation;

import com.bibliotheque.model.Utilisateur;
import com.bibliotheque.model.RoleEnum;
import com.bibliotheque.service.UtilisateurService;

import javax.swing.*;
import java.awt.*;

public class InscriptionFrame extends JFrame {

    private UtilisateurService service = new UtilisateurService();

    private JTextField     nomField   = new JTextField(20);
    private JTextField     emailField = new JTextField(20);
    private JPasswordField passField  = new JPasswordField(20);
    private JComboBox<RoleEnum> roleCombo = new JComboBox<>(new RoleEnum[]{
        RoleEnum.ETUDIANT, RoleEnum.ENSEIGNANT, RoleEnum.PERSONNEL
    });
    private JButton btnInscrire = new JButton("S'inscrire");
    private JButton btnRetour   = new JButton("Retour");

    public InscriptionFrame() {
        setTitle("Bibliotheque — Inscription");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(420, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        JLabel titre = new JLabel("Inscription", SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titre, gbc);

        gbc.gridwidth = 1;

        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Nom :"), gbc);
        gbc.gridx = 1;                panel.add(nomField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Email :"), gbc);
        gbc.gridx = 1;                panel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Mot de passe :"), gbc);
        gbc.gridx = 1;                panel.add(passField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; panel.add(new JLabel("Role :"), gbc);
        gbc.gridx = 1;                panel.add(roleCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 5; panel.add(btnRetour, gbc);
        gbc.gridx = 1;                panel.add(btnInscrire, gbc);

        add(panel);

        btnInscrire.addActionListener(e -> inscrire());
        btnRetour.addActionListener(e -> this.dispose());
    }

    private void inscrire() {
        // SWING recupere les donnees du formulaire
        Utilisateur u = new Utilisateur();
        u.setNom(nomField.getText().trim());
        u.setEmail(emailField.getText().trim());
        u.setMotDepasse(new String(passField.getPassword()));
        u.setRole((RoleEnum) roleCombo.getSelectedItem());

        try {
            // SERVICE verifie email unique + champs obligatoires
            service.inscrire(u);
            JOptionPane.showMessageDialog(this,
                "Inscription reussie ! Vous pouvez vous connecter.");
            this.dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}