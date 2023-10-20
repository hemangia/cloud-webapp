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
import java.io.BufferedReader;


@Service
@Transactional
public class AccountService {
	 @Autowired
	private AccountRepository accountRepository;

	  public AccountService(AccountRepository accountRepository) {
	        this.accountRepository = accountRepository;
	    }
	 
	  @Autowired
	    private BCryptPasswordEncoder passwordEncoder;
	  
	  public void loadAccountsFromCSV(InputStream inputStream) throws IOException  {
		    try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
		    	  CSVParser csvParser = new CSVParser(fileReader,
		    	            CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
		             List<CSVRecord> records = csvParser.getRecords();
		    	 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		         Date currentDate = Calendar.getInstance().getTime();


		            for (CSVRecord csvRecord : records) {
		                String firstName = csvRecord.get("first_name");
		                String lastName = csvRecord.get("last_name");
		                String email = csvRecord.get("email");
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
		            }
		  
	  }

	  }
}
