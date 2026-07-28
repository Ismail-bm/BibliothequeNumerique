package com.bibliotheque.model;

import java.time.LocalDate;

public class Recommmandation {
	
	private Long id ;
	private Utilisateur expediteur;
	private Utilisateur destinataire;
	private Document document ;
	private String message ;
	private LocalDate dateEnvoi ;
	
	public Recommmandation() {}

	public Recommmandation( Utilisateur expediteur, Utilisateur destinataire, Document document, String message) {
	
		
		this.expediteur = expediteur;
		this.destinataire = destinataire;
		this.document = document;
		this.message = message;
		this.dateEnvoi = LocalDate.now();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Utilisateur getExpediteur() {
		return expediteur;
	}

	public void setExpediteur(Utilisateur expediteur) {
		this.expediteur = expediteur;
	}

	public Utilisateur getDestinataire() {
		return destinataire;
	}

	public void setDestinataire(Utilisateur destinataire) {
		this.destinataire = destinataire;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public LocalDate getDateEnvoi() {
		return dateEnvoi;
	}

	public void setDateEnvoi(LocalDate dateEnvoi) {
		this.dateEnvoi = dateEnvoi;
	}
	
	
	
	
	
	
	

}
