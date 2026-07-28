package com.bibliotheque.model;
import java.time.LocalDate;

public class Penalite {
	
	private Long id;
	private Long empruntId;
	private int joursRetard;
	private double montant ;
	private boolean payee;
	private LocalDate dateCalcul;
	
	public Penalite() {}
	
	public static  final double TARIF_Par_Jours = 50;
	public Penalite(Long empruntId,int joursRetard) {
		
		
		this.empruntId =empruntId;
		this.joursRetard=joursRetard;
		this.montant = joursRetard *TARIF_Par_Jours;
		this.payee =false;
		this.dateCalcul =LocalDate.now();
	}
	
	public Long getId() {		return id;	}
	
	public void setId(Long id) {		this.id = id;	}
	
	public Long getEmpruntId() {		return empruntId;	}
	
	public void setEmpruntId(Long empruntId) {		this.empruntId = empruntId;	}
	
	public int getJoursRetard() {		return joursRetard;	}
	
	public void setJoursRetard(int joursRetard) {		this.joursRetard = joursRetard;	}
	
	public double getMontant() {		return montant;	}
	
	public void setMontant(double montant) {	this.montant = montant;	}
	
	public boolean isPayee() {		return payee;	}
	
	public void setPayee(boolean payee) {		this.payee = payee;	}
	
	public LocalDate getDateCalcul() {		return dateCalcul;	}
	
	public void setDateCalcul(LocalDate dateCalcul) {		this.dateCalcul = dateCalcul;	}
	
	public static double getTarifParJours() {		return TARIF_Par_Jours;}

	@Override
	public String toString() {
		return "Penalite [id=" + id + ", empruntId=" + empruntId + ", joursRetard=" + joursRetard + ", montant="
				+ montant + ", payee=" + payee + ", dateCalcul=" + dateCalcul + "]";
	}
	
	
	
	
	

}
