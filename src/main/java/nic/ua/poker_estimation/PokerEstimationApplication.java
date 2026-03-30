package nic.ua.poker_estimation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PokerEstimationApplication {

	public static void main(String[] args) {
		SpringApplication.run(PokerEstimationApplication.class, args);
	}

}
