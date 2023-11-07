package com.example.demo.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Account;
import com.example.demo.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
	  Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AccountRepository accountRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public boolean authenticate(String email, String password) {
        // Check if the provided email exists in the database
        Optional<Account> optionalAccount = accountRepository.findByEmail(email);

        if (optionalAccount.isPresent()) {
            Account account = optionalAccount.get();
            
            // Verify the provided password against the hashed password in the database
            if (passwordEncoder.matches(password, account.getPassword())) {
                // Authentication successful
            	 logger.info("AuthService: Password matches, Authentication Successful");
                return true;
            }
        }
        
        // Authentication failed
        return false;
    }
}




