package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Account;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.AccountRepository;

@RestController
public class AccountController {
	@Autowired
	private AccountRepository accountRepository;
	
	//Get Account by id;
	@GetMapping("/{id}")
	public Account getAccountById(@PathVariable (value = "id") long id) {
		
		return this.accountRepository.findById(id)
				.orElseThrow(() -> new UserNotFoundException("User not found with id :" + id));
	}
	
	//Create an Account
	/*
	@PostMapping
	public Account createUser(@RequestBody Account account) {
		return this.accountRepository.save(account);
	}
	*/

}
