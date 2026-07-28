package com.bibliotheque.presentation;



import java.time.LocalDate;

import com.bibliotheque.model.*;


public class MainApk{
	
	public static void main(String[] args) {
		
		Utilisateur u1 = new Utilisateur("ali","ali213@.com","ali2005",RoleEnum.ETUDIANT);
		Utilisateur u2 = new Utilisateur();
		//System.out.println(u1.toString());
		
		Document d1  = new Document("python"," jhon Doe",FormatEnum.PDF);
		//System.out.println(d1.toString());
		
		Emprunt e = new Emprunt(u1,d1,0);
		//System.out.println(e.toString());
		//e.retourner();
		e.calculernbrJoursRetard();
		//System.out.println(e.toString());
		
		Commentaire comment =new Commentaire(u1,d1," C'est un commentaire",4) ;
		System.out.println(comment.toString());
		
		
		
		
		
		

		

	}
}