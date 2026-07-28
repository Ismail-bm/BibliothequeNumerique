package com.bibliotheque.model;

import java.time.LocalDate;
import java.util.*;

public class Document {
	
	private Long id;
	private String titre ;
	private String auteur;
	private Date datePublication;
	private String cheminFichier;
	private boolean disponible ;
	private boolean telechargable ;
	private String motCles;
	private boolean accessibleEnligne;
	private int  nbrConsultations ;
	private String resume;
	
	private FormatEnum  format;	
	private List<Categorie> categories ;
		
	public Document(	) {}

	public Document( String titre, String auteur, FormatEnum  format) {
			
		this.titre = titre;
		this.auteur = auteur;
		this.disponible = true;
		this.format=format;
	}
	
	public void rendre() {		this.disponible=true;	}
	
	public void emprunter() {
		if(!disponible) {
			throw new IllegalStateException("Document Deja Emprunte");	
		}
		this.disponible =false;
		
	}
	
	
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitre() {
		return titre;
	}

	public void setTitre(String titre) {
		this.titre = titre;
	}

	public String getAuteur() {
		return auteur;
	}

	public void setAuteur(String auteur) {
		this.auteur = auteur;
	}

	public Date getDatePublication() {
		return datePublication;
	}

	public void setDatePublication(java.sql.Date date) {
		this.datePublication = date;
	}

	public String getCheminFichier() {
		return cheminFichier;
	}

	public void setCheminFichier(String cheminFichier) {
		this.cheminFichier = cheminFichier;
	}

	public boolean isDisponible() {
		return disponible;
	}

	public void setDisponible(boolean disponible) {
		this.disponible = disponible;
	}

	public boolean isTelechargable() {
		return telechargable;
	}

	public void setTelechargable(boolean telechargable) {
		this.telechargable = telechargable;
	}

	public String getMotCles() {
		return motCles;
	}

	public void setMotCles(String motCles) {
		this.motCles = motCles;
	}

	public boolean isAccessibleEnligne() {
		return accessibleEnligne;
	}

	public void setAccessibleEnligne(boolean accessibleEnligne) {
		this.accessibleEnligne = accessibleEnligne;
	}

	public int getNbrConsultations() {
		return nbrConsultations;
	}

	public void setNbrConsultations(int nbrConsultations) {
		this.nbrConsultations = nbrConsultations;
	}

	public String getResume() {
		return resume;
	}

	public void setResume(String resume) {
		this.resume = resume;
	}

	public FormatEnum getFormat() {
		return format;
	}

	public void setFormat(FormatEnum format) {
		this.format = format;
	}

	public List<Categorie> getCategories() {
		return categories;
	}

	public void setCategories(List<Categorie> categories) {
		this.categories = categories;
	}

	@Override
	public String toString() {
		return "Document [id=" + id + ", titre=" + titre + ", auteur=" + auteur + ", datePublication=" + datePublication
				+ ", cheminFichier=" + cheminFichier + ", disponible=" + disponible + ", resume=" + resume + ", format="
				+ format + ", categories=" + categories + "]";
	}
	
	


	
	
	

}
