package com.bibliotheque.model;

import java.time.LocalDate;

public class ListePartage {
	private Long id;
	private ListeLecture liste;
	private Utilisateur destinnataire;
	private LocalDate datePartage;
	
	public ListePartage() {}

	public ListePartage( ListeLecture liste, Utilisateur destinnataire, LocalDate datePartage) {
		
		this.liste = liste;
		this.destinnataire = destinnataire;
		this.datePartage = datePartage;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ListeLecture getListe() {
		return liste;
	}

	public void setListe(ListeLecture liste) {
		this.liste = liste;
	}

	public Utilisateur getDestinnataire() {
		return destinnataire;
	}

	public void setDestinnataire(Utilisateur destinnataire) {
		this.destinnataire = destinnataire;
	}

	public LocalDate getDatePartage() {
		return datePartage;
	}

	public void setDatePartage(LocalDate datePartage) {
		this.datePartage = datePartage;
	}
	
	
	
	

}
