package com.bibliotheque.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Emprunt {
	
	private Long id;
	private Utilisateur utilisateur;
	private LocalDate dateEmprunt;
	private Document document;
	private LocalDate dateRetourPreveue;
	private LocalDate dateRetourReelle;
	private StatutEnum statut;
	
	public Emprunt() {}
	
	
	public Emprunt( Utilisateur utilisateur, Document document ,int nbrjours ){

		this.utilisateur = utilisateur;
		this.document =document;
		this.dateEmprunt= LocalDate.now();
		this.dateRetourPreveue=LocalDate.now().plusDays(nbrjours);
		this.dateRetourReelle=null;
		this.statut=StatutEnum.EN_COURS;
	}
	
	public boolean estEnRetard() {
		if( statut == StatutEnum.RETOURNE) {
			return false;
		}else  {     return true    ; }  
	}

	public long calculernbrJoursRetard() {
		if(!estEnRetard()) {			return 0;
		}else { 
			LocalDate reference = (dateRetourReelle != null) ? dateRetourReelle :LocalDate.now();			
			return  ChronoUnit.DAYS.between(dateRetourPreveue, reference);
		}
	}
		
	public void retourner() {
		this.dateRetourReelle=LocalDate.now();
		this.statut=StatutEnum.RETOURNE;		
	}


	public Long getId() {		return id;	}

	public void setId(Long id) {		this.id = id;	}

	public Utilisateur getUtilisateur() {		return utilisateur;	}

	public void setUtilisateur(Utilisateur utilisateur) {		this.utilisateur = utilisateur;	}

	public LocalDate getDateEmprunt() {		return dateEmprunt;	}

	public void setDateEmprunt(LocalDate dateEmprunt) {		this.dateEmprunt = dateEmprunt;	}

	public Document getDocument() {		return document;	}

	public void setDocument(Document document) {		this.document = document;	}

	public LocalDate getDateRetourPreveue() {		return dateRetourPreveue; }

	public void setDateRetourPreveue(LocalDate dateRetourPreveue) {		this.dateRetourPreveue = dateRetourPreveue;	}

	public LocalDate getDateRetourReelle() {		return dateRetourReelle;	}

	public void setDateRetourReelle(LocalDate dateRetourReelle) {		this.dateRetourReelle = dateRetourReelle;	}

	public StatutEnum getStatut() {		return statut;	}


	public void setStatut(StatutEnum statut) {		this.statut = statut;	}


	@Override
	public String toString() {
		return "Emprunt [id=" + id + ", utilisateur=" + utilisateur + ", dateEmprunt=" + dateEmprunt + ", document="
				+ document + ", dateRetourPreveue=" + dateRetourPreveue + ", dateRetourReelle=" + dateRetourReelle
				+ ", statut=" + statut + "]";
	}
	
	
	
		
	

}
