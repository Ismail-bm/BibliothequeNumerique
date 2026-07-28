package com.bibliotheque.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConnection {
	
	
	private static final String URL = "jdbc:mysql://localhost:3306/bibliotheque_numerique?useSSL=false&serverTimezone=UTC";
	
	private static final String USER = "root";
	
	private static final String PASSWORD = "";
	
	//connexion Unique  -> une seule dans tout l'apk;
	private static   Connection instance = null;
	
	private DatabaseConnection() {};
	
	public static Connection getInstance() throws SQLException {
		
		if(instance == null || instance.isClosed()) {
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				
				instance = DriverManager.getConnection(URL,USER,PASSWORD);
				
				System.out.println(" Connexion MySQL reussie ");
				
			}catch(ClassNotFoundException e) {
				throw new SQLException(
						" Driver MySQL introuvable !"+e.getMessage());			
			}
		}
		
		return instance;
	}
	
	public static void fermer () {
		
		try {
			if(instance != null && !instance.isClosed()) {
				instance.close();
				System.out.println("Connexion fermee");
			}
		}catch(SQLException e) {
			System.err.println(" Erreur fermeture : "+ e.getMessage());
			
		}
	}
	

}
