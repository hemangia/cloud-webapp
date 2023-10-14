package com.example.demo.controller;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/database") 
public class AppHealthController {
	@Autowired
	private DataSource dbdtsource ;
	@GetMapping("/healthz")
	public ResponseEntity<Void>  checkIFDBConnectedOrNot(){
		
		HttpHeaders rqstHeader = new HttpHeaders();
		AppHealthResponse appResponse = new AppHealthResponse();
		
		if(checkIfRqstHAsBody()) {
			return ResponseEntity.badRequest().build();
		}
		
		try {
			Connection dbConnection = dbdtsource.getConnection();
			rqstHeader.setCacheControl("no-cache");
			if(isAppConnectedtoDB(dbConnection) == true) {
			
				return ResponseEntity.ok().build();
				
			}
			else {
		
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
			}
		}
		catch(SQLException ex) {
		
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
		}
		
	}
	private boolean checkIfRqstHAsBody() {
		return false;
	}
	public boolean isAppConnectedtoDB(Connection con) {
		try {
			con.createStatement().execute("Select 1");
			return true;
		}
		catch(SQLException e) {
			return false;
		}
		
	}

}
