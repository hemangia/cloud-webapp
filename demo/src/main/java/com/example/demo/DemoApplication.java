package com.example.demo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;

import com.example.demo.service.AccountService;


import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.demo")
public class DemoApplication {
	@Autowired
	 private ResourceLoader resourceLoader;


		public static void main(String[] args) {
	        ConfigurableApplicationContext context = SpringApplication.run(DemoApplication.class,args);
	        AccountService accountService = context.getBean(AccountService.class);

	        try {
	           System.out.println("Hello world test 2 dev 8");
	            Resource resource = context.getResource("classpath:inputfiles/users.csv");
	            InputStream inputStream = resource.getInputStream();

	            accountService.loadAccountsFromCSV(inputStream);
	            inputStream.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }


	}