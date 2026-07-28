package com.bibliotheque.dao;

import java.sql.Connection;

public class TestConnexion {
	
	public static void main(String[] args) {
		
		try {
			
			Connection conn = DatabaseConnection.getInstance();
			
			System.out.println("Base Connectee :"
			+conn.getMetaData().getDatabaseProductName()
			+"Version "+conn.getMetaData().getDatabaseProductVersion());
			
			
			 DatabaseConnection.fermer();
			
			
		}catch(Exception e) {
			System.out.println("ERREUR : "+ e.getMessage());
			
		}
		
		
	}

}
