package com.galvanize.cars;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CarsApiDemo05282020ApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;

	@Test
	void getCarDetails_shouldReturnCar() {
		ResponseEntity<Car> response = restTemplate.getForEntity("/cars/prius", Car.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody().getName()).isEqualTo("prius");
		assertThat(response.getBody().getType()).isEqualTo("hybrid");
	}
}
