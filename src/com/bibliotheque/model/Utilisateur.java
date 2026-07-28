package com.bibliotheque.model;

import java.time.LocalDate;

public class Utilisateur {
	
	private Long id;
	private String nom;
	private String email;


	private String motDepasse;
	private  RoleEnum role;
	private boolean actif;
	private LocalDate dateInscription ;
	private  int maxEmprunt ;
	
	public Utilisateur() {}
			
	public Utilisateur( String nom, String email, String motDepasse, RoleEnum role) {
		
		this.nom = nom;
		this.email = email;
		this.motDepasse = motDepasse;
		this.role = role;
		this.actif = true;
		this.dateInscription =LocalDate.now();
		this.maxEmprunt=(role == RoleEnum.ENSEIGNANT)? 5:3;

	}
	
	public Long getId() {	return id;	}

	public void setId(Long id) {		this.id = id;}

	public String getNom() {	return nom;	}

	public void setNom(String nom) {	this.nom = nom;}

	public String getEmail() {		return email;	}

	public void setEmail(String email) {		this.email = email;	}

	public String getMotDepasse() {		return motDepasse;	}

	public void setMotDepasse(String motDepasse) {	this.motDepasse = motDepasse;}

	public RoleEnum getRole() {		return role;}

	public void setRole(RoleEnum role) {		this.role = role;	}

	public boolean isActif() {		return actif;	}

	public void setActif(boolean actif) {	this.actif = actif;}

	public int getMaxEmprunt() {	return maxEmprunt;}

	public void setMaxEmprunt(int maxEmprunt) {
		this.maxEmprunt = maxEmprunt;	}
	
	public boolean  estBibLiothecaire() {
		
		return this.role == RoleEnum.BIBLIOTHECAIRE;
	}
	
	public LocalDate getDateInscription() {		return dateInscription;	}

	public void setDateInscription(LocalDate dateInscription) {		this.dateInscription = dateInscription;	}



	


	

	
}
