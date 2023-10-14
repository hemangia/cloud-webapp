package com.example.demo.entity;

import java.util.Date;

import jakarta.persistence.*;

@Entity
@Table(name="account")

public class Account {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	private String first_name;
	private String last_name;
	private String email;
	private String password;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "account_created")
	private Date account_created;
	   
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "account_updated")
	private Date account_updated;
	
	
	public Account(String first_name, String last_name, String email, String password) {
		super();
		this.first_name = first_name;
		this.last_name = last_name;
		this.email = email;
		this.password = password;
	}
	
	public Account() {
		super();
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getFirst_name() {
		return first_name;
	}
	public void setFirst_name(String first_name) {
		this.first_name = first_name;
	}
	public String getLast_name() {
		return last_name;
	}
	public void setLast_name(String last_name) {
		this.last_name = last_name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}

	public Date getAccount_created() {
		return account_created;
	}

	public void setAccount_created(Date account_created) {
		this.account_created = account_created;
	}

	public Date getAccount_updated() {
		return account_updated;
	}

	public void setAccount_updated(Date account_updated) {
		this.account_updated = account_updated;
	}


}
