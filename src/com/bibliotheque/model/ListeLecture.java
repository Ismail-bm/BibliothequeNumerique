package com.bibliotheque.model;

import java.time.LocalDate;

public class ListeLecture {
	
	private Long id;
	private String nom ; // favoirs a lire
	private boolean estpublique; //visible pour autre user
	private Long proprietaireId; // id user quui cree liste
	private LocalDate dateCreation;
	
	public ListeLecture () {}

	
	public ListeLecture(String nom ,Long proprietaireId,boolean estpublique) {

		
		this.nom =nom;
		this.proprietaireId =proprietaireId;
		this .estpublique =estpublique;
		this.dateCreation =LocalDate.now();
		
	}






	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getNom() {
		return nom;
	}


	public void setNom(String nom) {
		this.nom = nom;
	}


	public boolean isEstpublique() {
		return estpublique;
	}


	public void setEstpublique(boolean estpublique) {
		this.estpublique = estpublique;
	}


	public Long getProprietaireId() {
		return proprietaireId;
	}


	public void setProprietaireId(Long proprietaireId) {
		this.proprietaireId = proprietaireId;
	}


	public LocalDate getDateCreation() {
		return dateCreation;
	}


	public void setDateCreation(LocalDate dateCreation) {
		this.dateCreation = dateCreation;
	}


	@Override
	public String toString() {
		return "ListeLecture [id=" + id + ", nom=" + nom + ", estpublique=" + estpublique + ", proprietaireId="
				+ proprietaireId + ", dateCreation=" + dateCreation + "]";
	}
	
	
	
}
