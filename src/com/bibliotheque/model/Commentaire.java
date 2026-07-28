package com.bibliotheque.model;

import java.time.LocalDate;

public class Commentaire {
	
	
	private Long id;
	private Utilisateur utilisateur;
	private  Document document;
	private String contenu;
	private int note;
	private LocalDate dateCreation;
	
	public Commentaire () {}
	
	public Commentaire( Utilisateur utilisateur, Document document, String contenu, int note) {
		
		if( note > 5 || note <1) {
			throw new IllegalArgumentException("Note entre 1 et 5");
		}
		this.utilisateur = utilisateur;
		this.document = document;
		this.contenu = contenu;
		this.note = note;
		this.dateCreation=LocalDate.now();
		
	}
	
	

	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Utilisateur getUtilisateur() {
		return utilisateur;
	}

	public void setUtilisateur(Utilisateur utilisateur) {
		this.utilisateur = utilisateur;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public String getContenu() {
		return contenu;
	}

	public void setContenu(String contenu) {
		this.contenu = contenu;
	}

	public int getNote() {
		return note;
	}

	public void setNote(int note) {
		this.note = note;
	}

	public LocalDate getDateCreation() {
		return dateCreation;
	}

	public void setDateCreation(LocalDate dateCreation) {
		this.dateCreation = dateCreation;
	}

	@Override
	public String toString() {
		return "Commentaire [id=" + id + ", utilisateur=" + utilisateur + ", document=" + document + ", contenu="
				+ contenu + ", note=" + note + ", dateCreation=" + dateCreation + "]";
	}


	
	
	
	

}
