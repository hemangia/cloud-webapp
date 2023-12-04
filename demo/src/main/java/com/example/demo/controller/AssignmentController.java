package com.example.demo.controller;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.example.demo.entity.Submission;
import com.example.demo.exception.AssignmentNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.metrics.WebappAppMetrics;
import com.example.demo.repository.AccountRepository;
import com.example.demo.repository.AssignmentRepository;
import com.example.demo.repository.SubmissionRepository;
import com.example.demo.service.AccountService;
import com.example.demo.service.AuthService;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.ListTopicsResponse;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.Topic;


@RestController
@RequestMapping("/v24/assignments")
public class AssignmentController {
	@Autowired
	private AssignmentRepository assignmentRepository;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private SubmissionRepository submissionRepository ;
	

    Logger logger = LoggerFactory.getLogger(AssignmentController.class);
    @Autowired
    WebappAppMetrics webappAppMetrics;
    
    private static final String GET_ALL_ASSIGNMENTS = "getAllAssignment";
    private static final String GET_ASSIGNMENT_BY_ID = "getAssignmentById";
    private static final String CREATE_USER = "createUser";
    private static final String UPDATE_USER = "updateUser";
    private static final String DELETE_USER = "deleteUser";
    
    @Value("${sns_topic_name}")
    private String snsTopicName;
    
	
	  private final AuthService authService;
	  @Autowired
	    public AssignmentController(AuthService authService) {
	        this.authService = authService;
	    }
	
	  @GetMapping
	  public ResponseEntity<List<Assignment>> getAllAssignment(
	      @RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorizationHeader
	  ) {
		  
	      logger.info("AssignmentController: Called Get All Assignments API");
	   //   logger.info(authorizationHeader);
	      webappAppMetrics.addCount(GET_ALL_ASSIGNMENTS);
	        
	      // Extract the Basic Auth credentials from the header
	      String[] credentials = extractBasicAuthCredentials(authorizationHeader);

	      if (credentials.length == 2) {
	          String username = credentials[0];
	          String password = credentials[1];

	          boolean isAuthenticated = authService.authenticate(username, password);

	          if (isAuthenticated) {
	              List<Assignment> assignments = this.assignmentRepository.findAll();
	              logger.info("All assignment info fetched from DB");
	            
	              return ResponseEntity.ok(assignments);
	          } else {
	              // Authentication failed
	        	  logger.error("Unauthorized to access the records, Please enter correct credentials ");
	              return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	          }
	      } else {
	          // Invalid Authorization header
	     	  logger.error("Unauthorized to access the records, Please enter correct credentials ");
	          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	      }
	  }
	
	// Get Assignment by id
	  @GetMapping("/{id}")
	  public ResponseEntity<Assignment> getAssignmentById(
	      @PathVariable(value = "id") UUID id,
	      @RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorizationHeader
	  ) {
		  
	      logger.info("AssignmentController: Called Get Assignment ID API");
	    //  logger.info(authorizationHeader);
	      webappAppMetrics.addCount(GET_ASSIGNMENT_BY_ID);
	      
	      // Extract the Basic Auth credentials from the header
	      String[] credentials = extractBasicAuthCredentials(authorizationHeader);

	      if (credentials.length == 2) {
	          String username = credentials[0];
	          String password = credentials[1];

	          boolean isAuthenticated = authService.authenticate(username, password);

	          if (isAuthenticated) {
	              Assignment assignment = this.assignmentRepository.findById(id)
	                      .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found with id :" + id));
	              
	              logger.info("Assignment with id  " + id + " info fetched from DB");
	              
	              return ResponseEntity.ok(assignment);
	          } else {
	              // Authentication failed
	         	  logger.error("Unauthorized to access the records, Please enter correct credentials ");
	              return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	          }
	      } else {
	          // Invalid Authorization header
	     	  logger.error("Unauthorized to access the records, Please enter correct credentials ");
	          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	      }
	  }


	
	@PostMapping
	public ResponseEntity createUser(
	    @RequestBody Assignment assignment,
	    @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
	) {
		
		  logger.info("AssignmentController: Called Create User API");
	     // logger.info(authorizationHeader);
	      webappAppMetrics.addCount(CREATE_USER);
	      
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
	               
	                logger.info("Assignment with Name: " + savedAssignment.getName() + " info saved intp DB");
	                
	                return ResponseEntity.status(HttpStatus.CREATED).build();
	            } else {
	                // Account not found for the provided username
	           	  logger.error("Account not found ");
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	            }
	        } else {
	            // Authentication failed
	       	  logger.error("Unauthorized to access the records, Please enter correct credentials ");
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	        }
	    } else {
	        // Invalid Authorization header
	   	  logger.error("Unauthorized to access the records, Please enter correct credentials ");
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
	    @PathVariable("id") UUID id, // Change the data type to UUID
	    @RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorizationHeader
	) {
		
		  logger.info("AssignmentController: Called Update User API");
	     // logger.info(authorizationHeader);
	      webappAppMetrics.addCount(UPDATE_USER);
	      
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
	                        .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found with id: " + id.toString()));

	                // Update the fields of the existing assignment with the values from the updatedAssignment
	                existingAssignment.setName(updatedAssignment.getName());
	                existingAssignment.setPoints(updatedAssignment.getPoints());
	                existingAssignment.setNoofattempts(updatedAssignment.getNoofattempts());
	                existingAssignment.setDeadline(updatedAssignment.getDeadline());

	                // Save the updated assignment
	                Assignment savedAssignment = assignmentRepository.save(existingAssignment);
	                logger.info("Assignment with Name: " + savedAssignment.getName() + " info updated into DB");
	                return ResponseEntity.ok(savedAssignment);
	            } else {
	                // User is not authorized to update this assignment
	            	  logger.error("Record is Forbidden ");
	                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	            }
	        } else {
	            // Authentication failed
	       	  logger.error("Unauthorized to access the records, Please enter correct credentials ");
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	        }
	    } else {
	        // Invalid Authorization header
	   	  logger.error("Unauthorized to access the records, Please enter correct credentials ");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	    }
	}
	
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(
	    @PathVariable("id") UUID id,
	    @RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorizationHeader
	) {
	    // Extract the Basic Auth credentials from the header
	    String[] credentials = extractBasicAuthCredentials(authorizationHeader);
	    
		  logger.info("AssignmentController: Called Delete User API");
	   //   logger.info(authorizationHeader);
	      webappAppMetrics.addCount(DELETE_USER);

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
	                    logger.info("Assignment with id: " + id + " info deleted from DB");
	                    return ResponseEntity.noContent().build();
	                } catch (EmptyResultDataAccessException e) {
	                    // Assignment with the given ID was not found
	                	  logger.error("Record Not found ");
	                    return ResponseEntity.notFound().build();
	                }
	            } else {
	                // User is not authorized to delete this assignment
	           	  logger.error("Record is Forbidden ");
	                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	            }
	        } else {
	            // Authentication failed
	       	  logger.error("Unauthorized to access the records, Please enter correct credentials ");
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	        }
	    } else {
	        // Invalid Authorization header
	   	  logger.error("Unauthorized to access the records, Please enter correct credentials ");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	    }
	}
	
	@PostMapping("/{id}/submission")
	public ResponseEntity<Submission> submitAssignment(
	        @PathVariable("id") UUID assignmentId,
	        @RequestBody Submission submission,
	        @RequestHeader(name = HttpHeaders.AUTHORIZATION) String authorizationHeader
	) {
	    logger.info("AssignmentController: Called Submit Assignment API");
	    // logger.info(authorizationHeader);
	    webappAppMetrics.addCount("submitAssignment");

	    // Extract the Basic Auth credentials from the header
	    String[] credentials = extractBasicAuthCredentials(authorizationHeader);

	    if (credentials.length == 2) {
	        String username = credentials[0];
	        String password = credentials[1];

	        boolean isAuthenticated = authService.authenticate(username, password);

	        if (isAuthenticated) {
	            Optional<Assignment> assignmentOptional = assignmentRepository.findById(assignmentId);

	            if (assignmentOptional.isPresent()) {
	                Assignment assignment = assignmentOptional.get();

	                // Check if the authenticated user matches the creator of the assignment
	                boolean isAuthorized = assignment.getAccount() != null &&
	                        assignment.getAccount().getEmail() != null &&
	                        assignment.getAccount().getEmail().equals(username);

	                if (isAuthorized) {
	                    long submissionCount = submissionRepository.countByAssignmentId(assignmentId);

	                    if (submissionCount < assignment.getNoofattempts()) {

	                        Date deadlinedt = assignment.getDeadline();

	                        Date standardizedDeadline = standardizeDate(deadlinedt);

	                        if (standardizedDeadline != null && standardizedDeadline.before(new Date())) {
	                            logger.error("Submission rejected. Due date has passed.");
	                            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	                        }
	                       
	                        
	                        // Set the assignment for the submission
	                        submission.setAssignment(assignment);

	                        // Set the submission date to the current date
	                        submission.setSubmissionDate(new Date());

	                        // Set the assignment updated date
	                        assignment.setAssignment_updated(new Date());

	                        // Set the submission number
	                        long submissionNumber = submissionRepository.countByAssignmentId(assignmentId) + 1;
	                        submission.setSubmissionNumber((int) submissionNumber);

	                        // Save the submission
	                        Submission savedSubmission = submissionRepository.save(submission);
	                        logger.info("Submission with id: " + savedSubmission.getId() + " saved into DB");

	                        postUrlToSnsTopic(savedSubmission, username, assignmentId, assignment.getName(), snsTopicName, submissionNumber);

	                        return ResponseEntity.status(HttpStatus.CREATED).body(savedSubmission);
	                    } else {
	                        // Too many submission attempts
	                        logger.error("Exceeded the maximum number of submission attempts for the assignment");
	                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	                    }

	                } else {
	                    // User is not authorized to submit to this assignment
	                    logger.error("Record is Forbidden ");
	                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
	                }
	            } else {
	                // Assignment not found with the given ID
	                logger.error("Assignment not found with id: " + assignmentId.toString());
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
	            }
	        } else {
	            // Authentication failed
	            logger.error("Unauthorized to access the records, Please enter correct credentials ");
	            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	        }
	    } else {
	        // Invalid Authorization header
	        logger.error("Unauthorized to access the records, Please enter correct credentials ");
	        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
	    }
	}



	 

	private boolean userIsAuthorizedToDeleteAssignment(UUID assignmentId, String username) {
	 
	    Optional<Assignment> assignmentOptional = assignmentRepository.findById(assignmentId);

	    if (assignmentOptional.isPresent()) {
	        Assignment assignment = assignmentOptional.get();
	        
	     
	        return assignment.getAccount() != null && assignment.getAccount().getEmail() != null &&
	               assignment.getAccount().getEmail().equals(username);
	    } else {
	      
	        return false;
	    }
	}




private boolean userIsAuthorizedToUpdateAssignment(UUID assignmentId, String username) {
   
    Optional<Assignment> assignmentOptional = assignmentRepository.findById(assignmentId);

    if (assignmentOptional.isPresent()) {
        Assignment assignment = assignmentOptional.get();
        
       
        return assignment.getAccount().getEmail().equals(username);
    } else {
   
        return false;
    }
}
private Date standardizeDate(Date date) {
    if (date != null) {
        // Format the date to a common format and set the time zone to UTC (or your preferred time zone)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            return sdf.parse(sdf.format(date));
        } catch (ParseException e) {
            // Handle parsing exception as needed
            e.printStackTrace();
        }
    }
    return null;
}


private void postUrlToSnsTopic(Submission submission, String username, UUID assignmentId, String assignmentName, @Value("${sns_topic_name}") String snsTopicName, long submissionNumber) {
    SnsClient snsClient = SnsClient.create();
    
    //String topicName = "my-assignment-sns-topic";

    // Get the ARN of the SNS topic
    ListTopicsResponse listTopicsResponse = snsClient.listTopics();
    String topicArn = listTopicsResponse.topics().stream()
            .filter(topic -> topic.topicArn().endsWith(snsTopicName))
            .findFirst()
            .map(Topic::topicArn)
            .orElseThrow(() -> new RuntimeException("SNS topic not found"));

   
    String submissionUrl = submission.getSubmissionUrl();

    // Create the message to be sent to the SNS topic
    String message = String.format("New submission;username=%s;assignmentId=%s;assignmentName=%s;submissionId=%s;submissionUrl=%s;submissionNo=%d",
            username, assignmentId.toString(),assignmentName,submission.getId().toString(), submissionUrl, submissionNumber);


    // Publish the message to the SNS topic
    snsClient.publish(PublishRequest.builder().topicArn(topicArn).message(message).build());

    // Log the SNS response
    logger.info("Message sent to SNS for assignmentId: " + assignmentId.toString());
}



}
