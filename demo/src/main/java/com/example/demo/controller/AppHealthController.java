package com.example.demo.controller;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.metrics.WebappAppMetrics;

@RestController
@RequestMapping("/api/database") 
public class AppHealthController {
    Logger logger = LoggerFactory.getLogger(AppHealthController.class);
    WebappAppMetrics webappAppMetrics;
    
    private static final String CHECK_IF_DB_CONNECTED = "checkIFDBConnectedOrNot";
	@Autowired
	private DataSource dbdtsource ;
	
	 @Autowired
	    public AppHealthController(WebappAppMetrics webappAppMetrics) {
	        this.webappAppMetrics = webappAppMetrics;
	    }
	
	
	@GetMapping("/healthz")
	public ResponseEntity<Void>  checkIFDBConnectedOrNot(){
		
		HttpHeaders rqstHeader = new HttpHeaders();
		AppHealthResponse appResponse = new AppHealthResponse();
		
		if(checkIfRqstHAsBody()) {
			return ResponseEntity.badRequest().build();
		}
		
		try {
			Connection dbConnection = dbdtsource.getConnection();
			
		      logger.info("AppHealthController: Called Checked If DB Connected API");
		   
		      webappAppMetrics.addCount(CHECK_IF_DB_CONNECTED);
		      
			rqstHeader.setCacheControl("no-cache");
			if(isAppConnectedtoDB(dbConnection) == true) {
				
				 logger.info("DB connection is successful: ");
				
				
				 dbConnection.close();
				return ResponseEntity.ok().build();
				
			}
			else {
				 logger.info("Service Unavailable: DB connection is unsuccessful: ");
				 dbConnection.close();
		
				return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
			}
		}
		catch(SQLException ex) {
			
			 logger.error("DB Service Unavailable: Unable to connect to DB: ");
		
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
