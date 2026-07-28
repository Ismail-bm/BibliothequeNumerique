package com.bibliotheque.presentation;

// ═══════════════════════════════════════════════════════
// PenalitePanel.java — mes penalites
// ═══════════════════════════════════════════════════════
import com.bibliotheque.model.*;
import com.bibliotheque.service.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PenalitePanel extends JPanel {

    private PenaliteService   service = new PenaliteService();
    private Utilisateur       utilisateurConnecte;
    private JTable            table;
    private DefaultTableModel tableModel;
    private List<Penalite>    penalites;
    private JButton btnPayer    = new JButton("Payer la penalite");
    private JButton btnRefresh  = new JButton("Rafraichir");
    private JLabel  lblTotal    = new JLabel("Total du : 0.00 MAD");

    PenalitePanel(Utilisateur u) {
        this.utilisateurConnecte = u;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titre = new JLabel("Mes Penalites", SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 16));
        add(titre, BorderLayout.NORTH);

        String[] colonnes = {"ID", "Emprunt ID", "Jours Retard",
                             "Montant (MAD)", "Payee", "Date"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel sud = new JPanel(new FlowLayout());
        sud.add(lblTotal);
        sud.add(btnRefresh);
        sud.add(btnPayer);
        add(sud, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> charger());
        btnPayer.addActionListener(e -> payer());
        charger();
    }

    private void charger() {
        try {
            penalites = service.getMesPenalites(utilisateurConnecte.getId());
            tableModel.setRowCount(0);
            for (Penalite p : penalites) {
                tableModel.addRow(new Object[]{
                    p.getId(), p.getEmpruntId(), p.getJoursRetard(),
                    p.getMontant(), p.isPayee() ? "OUI" : "NON",
                    p.getDateCalcul()
                });
            }
            double total = service.getTotalNonPaye(utilisateurConnecte.getId());
            lblTotal.setText("Total du : " + total + " MAD");
        } catch (Exception ex) {
            tableModel.setRowCount(0);
            lblTotal.setText("Total du : 0.00 MAD");
        }
    }

    private void payer() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez selectionner une penalite !");
            return;
        }
        Penalite p = penalites.get(row);
        try {
            // SERVICE verifie si deja payee
            service.payerPenalite(p.getId());
            JOptionPane.showMessageDialog(this,
                "Penalite de " + p.getMontant() + " MAD payee !");
            charger();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}

// ═══════════════════════════════════════════════════════
// EmpruntRetardPanel.java — BIBLIOTHECAIRE seulement
// ═══════════════════════════════════════════════════════
 class EmpruntRetardPanel extends JPanel {

    private EmpruntService    empruntService  = new EmpruntService();
    private PenaliteService   penaliteService = new PenaliteService();
    private Utilisateur       utilisateurConnecte;
    private JTable            table;
    private DefaultTableModel tableModel;
    private List<Emprunt>     emprunts;
    private JButton btnCalculerPenalite = new JButton("Calculer Penalite");
    private JButton btnRefresh          = new JButton("Rafraichir");

    EmpruntRetardPanel(Utilisateur u) {
        this.utilisateurConnecte = u;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (!u.estBibLiothecaire()) {
            add(new JLabel("Acces refuse !", SwingConstants.CENTER));
            return;
        }

        JLabel titre = new JLabel("Emprunts en Retard", SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 16));
        add(titre, BorderLayout.NORTH);

        String[] colonnes = {"ID Emprunt", "Utilisateur ID",
                             "Document ID", "Date Prevue", "Jours Retard",
                             "Penalite (MAD)", "Payee"};
        tableModel = new DefaultTableModel(colonnes, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel sud = new JPanel(new FlowLayout());
        sud.add(btnRefresh);
        sud.add(btnCalculerPenalite);
        add(sud, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> charger());
        btnCalculerPenalite.addActionListener(e -> calculerPenalite());
        charger();
    }

    private void charger() {
        try {
            emprunts = empruntService.getEmpruntsEnRetard();
            tableModel.setRowCount(0);
            for (Emprunt e : emprunts) {
                String montant = "-";
                String payee = "-";
                try {
                    Penalite p = penaliteService.getPenaliteParEmprunt(e.getId());
                    montant = String.valueOf(p.getMontant());
                    payee = p.isPayee() ? "OUI" : "NON";
                } catch (Exception ex) {}
                tableModel.addRow(new Object[]{
                    e.getId(),
                    e.getUtilisateur().getId(),
                    e.getDocument().getId(),
                    e.getDateRetourPreveue(),
                    e.calculernbrJoursRetard(),
                    montant,
                    payee
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void calculerPenalite() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez selectionner un emprunt !");
            return;
        }
        Emprunt e = emprunts.get(row);
        try {
            // SERVICE calcule et sauvegarde la penalite
            penaliteService.calculerPenalite(e.getId());
            JOptionPane.showMessageDialog(this,
                "Penalite calculee : "
                + (e.calculernbrJoursRetard() * Penalite.TARIF_Par_Jours)
                + " MAD");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}

// ═══════════════════════════════════════════════════════
// ProfilPanel.java — modifier son profil
// ═══════════════════════════════════════════════════════
 class ProfilPanel extends JPanel {

    private UtilisateurService service = new UtilisateurService();
    private Utilisateur        utilisateurConnecte;
    private JTextField         nomField;
    private JTextField         emailField;
    private JPasswordField     passField;
    private JButton            btnSauvegarder = new JButton("Sauvegarder");

    ProfilPanel(Utilisateur u) {
        this.utilisateurConnecte = u;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        JLabel titre = new JLabel("Mon Profil", SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(titre, gbc);

        nomField   = new JTextField(u.getNom(), 20);
        emailField = new JTextField(u.getEmail(), 20);
        passField  = new JPasswordField(20);

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1; add(new JLabel("Nom :"), gbc);
        gbc.gridx = 1;                add(nomField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; add(new JLabel("Email :"), gbc);
        gbc.gridx = 1;                add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; add(new JLabel("Nouveau mdp :"), gbc);
        gbc.gridx = 1;                add(passField, gbc);

        // infos non modifiables
        gbc.gridx = 0; gbc.gridy = 4;
        add(new JLabel("Role : " + u.getRole()), gbc);
        gbc.gridx = 1;
        add(new JLabel("Max emprunts : " + u.getMaxEmprunt()), gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        add(btnSauvegarder, gbc);

        btnSauvegarder.addActionListener(e -> sauvegarder());
    }

    private void sauvegarder() {
        utilisateurConnecte.setNom(nomField.getText().trim());
        utilisateurConnecte.setEmail(emailField.getText().trim());

        String mdp = new String(passField.getPassword());
        if (!mdp.isEmpty()) {
            utilisateurConnecte.setMotDepasse(mdp);
        }

        try {
            // SERVICE verifie email non pris par autre utilisateur
            service.modifierProfil(utilisateurConnecte);
            JOptionPane.showMessageDialog(this, "Profil mis a jour !");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}

// ═══════════════════════════════════════════════════════
// StatistiquesPanel.java — BIBLIOTHECAIRE seulement
// ═══════════════════════════════════════════════════════
 class StatistiquesPanel extends JPanel {

    private AdminService service = new AdminService();
    private Utilisateur  utilisateurConnecte;

    StatistiquesPanel(Utilisateur u) {
        this.utilisateurConnecte = u;
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if (!u.estBibLiothecaire()) {
            add(new JLabel("Acces refuse !", SwingConstants.CENTER));
            return;
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(10, 10, 10, 10);
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;

        JLabel titre = new JLabel("Tableau de Bord", SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0;
        add(titre, gbc);

        try {
            gbc.gridwidth = 1;

            ajouterStat(gbc, 1, "Utilisateurs total :",
                String.valueOf(service.getNombreUtilisateurs()));

            ajouterStat(gbc, 2, "Emprunts en cours :",
                String.valueOf(service.getNombreEmpruntsEnCours()));

            ajouterStat(gbc, 3, "Emprunts en retard :",
                String.valueOf(service.getNombreEmpruntsEnRetard()));

            ajouterStat(gbc, 4, "Penalites non payees :",
                service.getTotalPenalitesNonPayees() + " MAD");

            ajouterStat(gbc, 5, "Document + emprunte :",
                service.getDocumentPlusEmprunte());

            ajouterStat(gbc, 6, "Utilisateur + actif :",
                service.getUtilisateurPlusActif());

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void ajouterStat(GridBagConstraints gbc,
                              int ligne, String label, String valeur) {
        gbc.gridx = 0; gbc.gridy = ligne;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 13));
        add(lbl, gbc);

        gbc.gridx = 1;
        JLabel val = new JLabel(valeur);
        val.setFont(new Font("Arial", Font.PLAIN, 13));
        val.setForeground(new Color(0, 100, 0));
        add(val, gbc);
    }
}

// ═══════════════════════════════════════════════════════
// RecommandationPanel.java — ENSEIGNANT, ETUDIANT et BIBLIOTHECAIRE
// ═══════════════════════════════════════════════════════
 class RecommandationPanel extends JPanel {

    private DocumentService service = new DocumentService();
    private UtilisateurService utilisateurService = new UtilisateurService();
    private Utilisateur utilisateurConnecte;
    private JButton btnEnvoyer = new JButton("Envoyer une recommandation");
    private JButton btnRefresh = new JButton("Rafraichir");

    private JTextArea recuesArea;
    private JTextArea envoyeesArea;
    private JTabbedPane tabs;

    RecommandationPanel(Utilisateur u) {
        this.utilisateurConnecte = u;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        construireInterface();
        chargerRecommandations();
    }

    private void construireInterface() {
        JLabel titre = new JLabel("Recommandations", SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 16));
        add(titre, BorderLayout.NORTH);

        tabs = new JTabbedPane();

        recuesArea = new JTextArea();
        recuesArea.setEditable(false);
        recuesArea.setLineWrap(true);
        recuesArea.setWrapStyleWord(true);
        tabs.add("Recues", new JScrollPane(recuesArea));

        envoyeesArea = new JTextArea();
        envoyeesArea.setEditable(false);
        envoyeesArea.setLineWrap(true);
        envoyeesArea.setWrapStyleWord(true);
        tabs.add("Envoyees", new JScrollPane(envoyeesArea));

        add(tabs, BorderLayout.CENTER);

        JPanel sud = new JPanel(new FlowLayout());
        sud.add(btnEnvoyer);
        sud.add(btnRefresh);

        btnEnvoyer.addActionListener(e -> envoyerRecommandation());
        btnRefresh.addActionListener(e -> chargerRecommandations());

        add(sud, BorderLayout.SOUTH);
    }

    private void chargerRecommandations() {
        try {
            var recues = service.getRecommandationsRecues(utilisateurConnecte.getId());
            recuesArea.setText("");
            if (recues.isEmpty()) {
                recuesArea.setText("Aucune recommandation recue.");
            } else {
                for (var r : recues) {
                    String expediteurNom = "Inconnu";
                    try {
                        if (r.getExpediteur() != null && r.getExpediteur().getId() != null) {
                            Utilisateur exp = utilisateurService.findById(r.getExpediteur().getId());
                            expediteurNom = exp.getNom() + " (" + exp.getRole() + ")";
                        }
                    } catch (Exception e) {
                        expediteurNom = "Utilisateur ID: " + r.getExpediteur().getId();
                    }

                    String docTitre = "Document ID: " + r.getDocument().getId();
                    try {
                        Document doc = service.findById(r.getDocument().getId());
                        docTitre = doc.getTitre();
                    } catch (Exception e) {
                    }

                    recuesArea.append("De : " + expediteurNom + "\n");
                    recuesArea.append("Document : " + docTitre + "\n");
                    recuesArea.append("Message : " + r.getMessage() + "\n");
                    recuesArea.append("Date : " + r.getDateEnvoi() + "\n");
                    recuesArea.append("-----------------------------------\n");
                }
            }
        } catch (Exception ex) {
            recuesArea.setText("Erreur lors du chargement : " + ex.getMessage());
        }

        try {
            var envoyees = service.getRecommandationsEnvoyees(utilisateurConnecte.getId());
            envoyeesArea.setText("");
            if (envoyees.isEmpty()) {
                envoyeesArea.setText("Aucune recommandation envoyee.");
            } else {
                for (var r : envoyees) {
                    String destinataireNom = "Inconnu";
                    try {
                        if (r.getDestinataire() != null && r.getDestinataire().getId() != null) {
                            Utilisateur dest = utilisateurService.findById(r.getDestinataire().getId());
                            destinataireNom = dest.getNom() + " (" + dest.getRole() + ")";
                        }
                    } catch (Exception e) {
                        destinataireNom = "Utilisateur ID: " + r.getDestinataire().getId();
                    }

                    String docTitre = "Document ID: " + r.getDocument().getId();
                    try {
                        Document doc = service.findById(r.getDocument().getId());
                        docTitre = doc.getTitre();
                    } catch (Exception e) {
                    }

                    envoyeesArea.append("A : " + destinataireNom + "\n");
                    envoyeesArea.append("Document : " + docTitre + "\n");
                    envoyeesArea.append("Message : " + r.getMessage() + "\n");
                    envoyeesArea.append("Date : " + r.getDateEnvoi() + "\n");
                    envoyeesArea.append("-----------------------------------\n");
                }
            }
        } catch (Exception ex) {
            envoyeesArea.setText("Erreur lors du chargement : " + ex.getMessage());
        }
    }

    private void envoyerRecommandation() {
        try {
            List<Utilisateur> utilisateurs = utilisateurService.listerTous();
            
            String[] nomsUsers = utilisateurs.stream()
                .filter(u -> !u.getId().equals(utilisateurConnecte.getId()))
                .map(u -> u.getId() + " - " + u.getNom() + " (" + u.getRole() + ")")
                .toArray(String[]::new);

            if (nomsUsers.length == 0) {
                JOptionPane.showMessageDialog(this, "Aucun autre utilisateur trouve !");
                return;
            }

            String selectedUser = (String) JOptionPane.showInputDialog(this,
                "Choisir le destinataire :", "Envoyer une recommandation",
                JOptionPane.QUESTION_MESSAGE, null, nomsUsers, nomsUsers[0]);

            if (selectedUser == null) return;

            Long destId = Long.parseLong(selectedUser.split(" - ")[0]);

            List<Document> documents = service.listerTous();
            String[] docTitles = documents.stream()
                .map(d -> d.getId() + " - " + d.getTitre())
                .toArray(String[]::new);

            String selectedDoc = (String) JOptionPane.showInputDialog(this,
                "Choisir le document a recommander :", "Choisir document",
                JOptionPane.QUESTION_MESSAGE, null, docTitles, docTitles[0]);

            if (selectedDoc == null) return;

            Long docId = Long.parseLong(selectedDoc.split(" - ")[0]);

            String message = JOptionPane.showInputDialog(this, 
                "Entrez votre message de recommandation :");

            if (message == null || message.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Le message est obligatoire !");
                return;
            }

            Utilisateur destinataire = new Utilisateur();
            destinataire.setId(destId);

            Document doc = new Document();
            doc.setId(docId);

            Recommmandation r = new Recommmandation(utilisateurConnecte, destinataire, doc, message.trim());
            service.envoyerRecommandation(r);

            JOptionPane.showMessageDialog(this, "Recommandation envoyee avec succes !");
            chargerRecommandations();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// ═══════════════════════════════════════════════════════
// CommentaireDialog.java — dialog ajout commentaire
// ═══════════════════════════════════════════════════════
 class CommentaireDialog extends JDialog {

    private DocumentService service = new DocumentService();

    CommentaireDialog(Utilisateur u, Document doc) {
        setTitle("Commenter : " + doc.getTitre());
        setSize(400, 250);
        setLocationRelativeTo(null);
        setModal(true);

        JTextArea contenuArea = new JTextArea(5, 30);
        SpinnerNumberModel noteModel =
            new SpinnerNumberModel(3, 1, 5, 1);
        JSpinner noteSpinner = new JSpinner(noteModel);
        JButton btnEnvoyer = new JButton("Envoyer");

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel nord = new JPanel(new FlowLayout());
        nord.add(new JLabel("Note (1-5) :"));
        nord.add(noteSpinner);
        panel.add(nord, BorderLayout.NORTH);
        panel.add(new JScrollPane(contenuArea), BorderLayout.CENTER);
        panel.add(btnEnvoyer, BorderLayout.SOUTH);

        btnEnvoyer.addActionListener(e -> {
            Commentaire c = new Commentaire();
            c.setUtilisateur(u);
            c.setDocument(doc);
            c.setContenu(contenuArea.getText().trim());
            c.setNote((Integer) noteSpinner.getValue());

            try {
                // SERVICE verifie contenu + note + document existe
                service.ajouterCommentaire(c);
                JOptionPane.showMessageDialog(this, "Commentaire ajoute !");
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        add(panel);
    }
}

// ═══════════════════════════════════════════════════════
// EmpruntDialog.java — dialog emprunt d'un document
// ═══════════════════════════════════════════════════════
 class EmpruntDialog extends JDialog {

    private EmpruntService service = new EmpruntService();

    EmpruntDialog(Utilisateur u, Document doc) {
        setTitle("Emprunter : " + doc.getTitre());
        setSize(350, 200);
        setLocationRelativeTo(null);
        setModal(true);

        SpinnerNumberModel joursModel =
            new SpinnerNumberModel(14, 1, 30, 1);
        JSpinner joursSpinner = new JSpinner(joursModel);
        JButton  btnEmprunter = new JButton("Emprunter");

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Document : " + doc.getTitre()), gbc);
        gbc.gridy = 1;
        panel.add(new JLabel("Nombre de jours :"), gbc);
        gbc.gridx = 1;
        panel.add(joursSpinner, gbc);
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(btnEmprunter, gbc);

        btnEmprunter.addActionListener(e -> {
            int nbrJours = (Integer) joursSpinner.getValue();

            // SWING construit l'objet Emprunt avec les vrais objets
            Emprunt emprunt = new Emprunt(u, doc, nbrJours);

            try {
                // SERVICE verifie max_emprunts + disponibilite
                service.emprunterDocument(emprunt);
                JOptionPane.showMessageDialog(this,
                    "Emprunt effectue ! Retour prevu le : "
                    + emprunt.getDateRetourPreveue());
                dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(panel);
    }
}