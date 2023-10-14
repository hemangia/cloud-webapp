package com.example.demo.entity;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;

@Entity
@Table(name="assignment")
public class Assignment {
	@Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID assignment_id;

	private String name;
	  
	  @Min(1)
	  @Max(100)
	private int points;
	  
	  @Min(1)
	  @Max(100)
	private int noofattempts;
	  
	  
	public int getNoofattempts() {
		return noofattempts;
	}

	public void setNoofattempts(int noofattempts) {
		this.noofattempts = noofattempts;
	}



	private Date deadline;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "assignment_created")
	private Date assignment_created;
	   




	public UUID getAssignment_id() {
		return assignment_id;
	}

	public void setAssignment_id(UUID assignment_id) {
		this.assignment_id = assignment_id;
	}



	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}



	public Date getDeadline() {
		return deadline;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	public Date getAssignment_created() {
		return assignment_created;
	}

	public void setAssignment_created(Date assignment_created) {
		this.assignment_created = assignment_created;
	}
	

	public Assignment(String name, @Min(1) @Max(100) int points, @Min(1) @Max(100) int noofattempts, Date deadline) {
		super();
		this.name = name;
		this.points = points;
		this.noofattempts = noofattempts;
		this.deadline = deadline;

	}

	public Assignment() {
		super();
	}

	public Date getAssignment_updated() {
		return assignment_updated;
	}

	public void setAssignment_updated(Date assignment_updated) {
		this.assignment_updated = assignment_updated;
	}
	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "assignment_updated")
	private Date assignment_updated;
	
	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn( name = "acct_id", referencedColumnName = "id")
	 private Account account;

}
