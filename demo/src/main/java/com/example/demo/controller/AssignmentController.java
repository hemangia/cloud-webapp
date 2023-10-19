package com.example.demo.controller;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Account;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus.Series;

import com.example.demo.entity.Assignment;
import com.example.demo.exception.AssignmentNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.AssignmentRepository;
import com.example.demo.service.AccountService;
import com.example.demo.service.AuthService;

@RestController
@RequestMapping("/v1/assignments")
public class AssignmentController {
	@Autowired
	private AssignmentRepository assignmentRepository;
	
	@Autowired
	private AccountRepository accountRepository;
	
	
	  private final AuthService authService;
	  @Autowired
	    public AssignmentController(AuthService authService) {
	        this.authService = authService;
	    }
	
	  @GetMapping
	  public ResponseEntity<List<Assignment>> getAllAssignment(
	      @RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorizationHeader
	  ) {
	      // Extract the Basic Auth credentials from the header
	      String[] credentials = extractBasicAuthCredentials(authorizationHeader);

	      if (credentials.length == 2) {
	          String username = credentials[0];
	          String password = credentials[1];

	          boolean isAuthenticated = authService.authenticate(username, password);

	          if (isAuthenticated) {
	              List<Assignment> assignments = this.assignmentRepository.findAll();
	              return ResponseEntity.ok(assignments);
	          } else {
	              // Authentication failed
	              return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	          }
	      } else {
	          // Invalid Authorization header
	          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	      }
	  }
	
	// Get Assignment by id
	  @GetMapping("/{id}")
	  public ResponseEntity<Assignment> getAssignmentById(
	      @PathVariable(value = "id") long id,
	      @RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorizationHeader
	  ) {
	      // Extract the Basic Auth credentials from the header
	      String[] credentials = extractBasicAuthCredentials(authorizationHeader);

	      if (credentials.length == 2) {
	          String username = credentials[0];
	          String password = credentials[1];

	          boolean isAuthenticated = authService.authenticate(username, password);

	          if (isAuthenticated) {
	              Assignment assignment = this.assignmentRepository.findById(id)
	                      .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found with id :" + id));
	              return ResponseEntity.ok(assignment);
	          } else {
	              // Authentication failed
	              return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	          }
	      } else {
	          // Invalid Authorization header
	          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	      }
	  }


	
	@PostMapping
	public ResponseEntity<Void> createUser(
	    @RequestBody Assignment assignment,
	    @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
	) {
	    // Extract the Basic Auth credentials from the header
	    String[] credentials = extractBasicAuthCredentials(authorizationHeader);

	    if (credentials.length == 2) {
	        String username = credentials[0];
	        String password = credentials[1];

	        boolean isAuthenticated = authService.authenticate(username, password);

	        if (isAuthenticated) {
	            Date currentDate = Calendar.getInstance().getTime();
	            assignment.setAssignment_created(currentDate);
	            assignment.setAssignment_updated(currentDate);

	            Optional<Account> accountOptional = accountRepository.findByEmail(username);

	            if (accountOptional.isPresent()) {
	                Account account = accountOptional.get();
	                assignment.setAccount(account);
	                Assignment savedAssignment = assignmentRepository.save(assignment); // Save the assignment with associated account
	                return ResponseEntity.status(HttpStatus.CREATED).build();
	            } else {
	                // Account not found for the provided username
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	            }
	        } else {
	            // Authentication failed
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	        }
	    } else {
	        // Invalid Authorization header
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	    }
	}


	
	
	private String[] extractBasicAuthCredentials(String authorizationHeader) {
	    if (authorizationHeader != null && authorizationHeader.startsWith("Basic ")) {
	        String base64Credentials = authorizationHeader.substring("Basic ".length()).trim();
	        byte[] credentialsBytes = Base64.getDecoder().decode(base64Credentials);
	        String credentials = new String(credentialsBytes, StandardCharsets.UTF_8);
	        return credentials.split(":", 2);
	    }
	    return new String[0];
	}
	
	 @PutMapping("/{id}")
	    public ResponseEntity<Assignment> updateUser(
	        @RequestBody Assignment updatedAssignment,
	        @PathVariable("id") long id,
	        @RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorizationHeader
	    ) {
	        // Extract the Basic Auth credentials from the header
	        String[] credentials = extractBasicAuthCredentials(authorizationHeader);

	        if (credentials.length == 2) {
	            String username = credentials[0];
	            String password = credentials[1];

	            boolean isAuthenticated = authService.authenticate(username, password);

	            if (isAuthenticated) {
	                // Check if the authenticated user matches the creator of the assignment
	                boolean isAuthorized = userIsAuthorizedToUpdateAssignment(id, username);

	                if (isAuthorized) {
	                    Assignment existingAssignment = this.assignmentRepository.findById(id)
	                            .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found with id: " + id));

	                    // Update the fields of the existing assignment with the values from the updatedAssignment
	                    existingAssignment.setName(updatedAssignment.getName());
	                    existingAssignment.setPoints(updatedAssignment.getPoints());
	                    existingAssignment.setNoofattempts(updatedAssignment.getNoofattempts());
	                    existingAssignment.setDeadline(updatedAssignment.getDeadline());

	                    // Save the updated assignment
	                    Assignment savedAssignment = assignmentRepository.save(existingAssignment);

	                    return ResponseEntity.ok(savedAssignment);
	                } else {
	                    // User is not authorized to update this assignment
	                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	                }
	            } else {
	                // Authentication failed
	                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	            }
	        } else {
	            // Invalid Authorization header
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	        }
	    }
	
	
	// Delete assignment
	 @DeleteMapping("/{id}")
	 public ResponseEntity<Void> deleteUser(
	     @PathVariable("id") long id,
	     @RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorizationHeader
	 ) {
	     // Extract the Basic Auth credentials from the header
	     String[] credentials = extractBasicAuthCredentials(authorizationHeader);

	     if (credentials.length == 2) {
	         String username = credentials[0];
	         String password = credentials[1];

	         boolean isAuthenticated = authService.authenticate(username, password);

	         if (isAuthenticated) {
	             // Check if the authenticated user matches the creator of the assignment
	             boolean isAuthorized = userIsAuthorizedToDeleteAssignment(id, username);

	             if (isAuthorized) {
	                 try {
	                     // Attempt to delete the assignment
	                     assignmentRepository.deleteById(id);
	                     return ResponseEntity.noContent().build();
	                 } catch (EmptyResultDataAccessException e) {
	                     // Assignment with the given ID was not found
	                     return ResponseEntity.notFound().build();
	                 }
	             } else {
	                 // User is not authorized to delete this assignment
	                 return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	             }
	         } else {
	             // Authentication failed
	             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	         }
	     } else {
	         // Invalid Authorization header
	         return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	     }
	 }

	 

private boolean userIsAuthorizedToDeleteAssignment(Assignment assignment, String username) {
    // Implement your authorization logic here
    // For example, check if the assignment's associated account's email matches the username
    // Return true if authorized, false otherwise
    if (assignment.getAccount() != null && assignment.getAccount().getEmail() != null) {
        return assignment.getAccount().getEmail().equals(username);
    }
    return false; // Default to false if the assignment or associated account is null
}

private boolean userIsAuthorizedToDeleteAssignment(long assignmentId, String username) {
    // Step 1: Get the Assignment by its ID
    Optional<Assignment> assignmentOptional = assignmentRepository.findById(assignmentId);

    if (assignmentOptional.isPresent()) {
        Assignment assignment = assignmentOptional.get();
        
        // Step 2: Check if the authenticated user's username matches the Assignment's username
        return assignment.getAccount().getEmail().equals(username);
    } else {
        // Step 3: Assignment with the given ID was not found
        return false;
    }
}


private boolean userIsAuthorizedToUpdateAssignment(long assignmentId, String username) {
    // Step 1: Get the Assignment by its ID
    Optional<Assignment> assignmentOptional = assignmentRepository.findById(assignmentId);

    if (assignmentOptional.isPresent()) {
        Assignment assignment = assignmentOptional.get();
        
        // Step 2: Check if the authenticated user's username matches the Assignment's username
        return assignment.getAccount().getEmail().equals(username);
    } else {
        // Step 3: Assignment with the given ID was not found
        return false;
    }
}





}
