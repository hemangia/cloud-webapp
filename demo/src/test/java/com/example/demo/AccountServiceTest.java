package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.demo.repository.AccountRepository;
import com.example.demo.service.AccountService;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
	
    @InjectMocks
    private AccountService accountService;
    
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
    	accountRepository = Mockito.mock(AccountRepository.class);
    	passwordEncoder = Mockito.mock(BCryptPasswordEncoder.class);
        // Create an instance of AccountService with dependencies injected
        accountService = new AccountService(accountRepository, passwordEncoder);
    }

    @Test
    void testLoadAccountsFromCSV() throws IOException {
        String csvData = "first_name,last_name,email,password\n" +
                "John,Doe,john.doe@example.com,abc123\n" +
                "Jane,Doe,jane.doe@example.com,xyz456";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(csvData.getBytes(StandardCharsets.UTF_8));

        // Mock the behavior of passwordEncoder
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");


        // Call the method under test
        accountService.loadAccountsFromCSV(inputStream);

        // Verify that save method was called with the expected number of times
        verify(accountRepository, times(2)).save(any());

        // You can add more assertions to verify other aspects of the method's behavior
        // For example, you can verify that the correct data was saved to the repository.
    }
}
