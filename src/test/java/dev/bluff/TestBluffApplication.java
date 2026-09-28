package dev.bluff;

import org.springframework.boot.SpringApplication;

public class TestBluffApplication {

	public static void main(String[] args) {
		SpringApplication.from(BluffApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
