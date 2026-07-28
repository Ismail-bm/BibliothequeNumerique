package com.bibliotheque.model;

import java.util.*;

// type de document  info ,math , Science ,Histoire ...;

public class Categorie {
	
	private Long id ;
	private String nom;
	private String description;
	
	private List<Document> documents = new ArrayList<>();
	
	public Categorie() {	}
	
	public Categorie(String nom) {
		this.nom = nom;
	}

	public Long getId() {		return id;	}

	public void setId(Long id) {		this.id = id;	}

	public String getNom() { 	return nom;	}

	public void setNom(String nom) { 	this.nom = nom;	}

	public String getDescription() {  	return description; }

	public void setDescription(String description) { 	this.description = description; }
	

	@Override
	public String toString() {
		return "Categorie [id=" + id + ", nom=" + nom + ", description=" + description + ", documents=" + documents
				+ "]";
	}
	
	
	
	
	
	
	
	
	
	
	
	
	

		
	
	
	

}
