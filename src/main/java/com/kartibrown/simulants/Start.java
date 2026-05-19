package com.kartibrown.simulants;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class Start
{

	public static void main(final String[] args)
	{
		SpringApplication.run(Start.class, args);
		System.out.println("UYA");

		//final World world = new World();

		//world.start();
	}
}

@RestController
class TestController
{
	@GetMapping("/hello")
	public String hello()
	{
		return "Hello";
	}
}
