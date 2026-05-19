package com.kartibrown.simulants;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableScheduling
@SpringBootApplication
public class Start
{

	public static void main(final String[] args)
	{
		SpringApplication.run(Start.class, args);

	}
}
