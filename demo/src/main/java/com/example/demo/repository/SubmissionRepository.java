package com.example.demo.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Submission; 

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {
	
	 @Query("SELECT COUNT(s) FROM Submission s WHERE s.assignment.id = :assignmentId")
	    long countByAssignmentId(@Param("assignmentId") UUID assignmentId);

  
}
