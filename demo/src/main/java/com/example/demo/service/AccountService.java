package com.example.demo.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.demo.entity.Account;
import com.example.demo.repository.AccountRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import java.text.SimpleDateFormat;
import java.io.FileReader;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.io.BufferedReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class AccountService {
	 @Autowired
	private AccountRepository accountRepository;

	  public AccountService(AccountRepository accountRepository) {
	        this.accountRepository = accountRepository;
	    }
	  Logger logger = LoggerFactory.getLogger(AccountService.class);
	 
	  @Autowired
	    private BCryptPasswordEncoder passwordEncoder;
	  
	  public void loadAccountsFromCSV(InputStream inputStream) throws IOException  {
		    try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
		    	  CSVParser csvParser = new CSVParser(fileReader,
		    	            CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
		             List<CSVRecord> records = csvParser.getRecords();
		    	 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		         Date currentDate = Calendar.getInstance().getTime();
		         logger.info("AccountService: reading account information from CSV file");

		         for (CSVRecord csvRecord : records) {
		                String email = csvRecord.get("email");

		                // Check if the account with this email already exists in the database
		                Optional<Account> existingAccount = accountRepository.findByEmail(email);

		                if (!existingAccount.isPresent()) {
		                    // Account with this email doesn't exist, so we can insert it
		                    String firstName = csvRecord.get("first_name");
		                    String lastName = csvRecord.get("last_name");
		                    String password = csvRecord.get("password");

		                    Account account = new Account();
		                    account.setFirst_name(firstName);
		                    account.setLast_name(lastName);
		                    account.setEmail(email);
		                    String hashedPassword = passwordEncoder.encode(password);

		                    account.setPassword(hashedPassword);
		                    account.setAccount_created(currentDate);
		                    account.setAccount_updated(currentDate);

		                    accountRepository.save(account);
		                } else {
		                    // Account with this email already exists, you can log or handle this as needed
		                    logger.info("Account with email " + email + " already exists in the database. Skipping.");
		                }
		            }
		  
	  }

	  }
}
