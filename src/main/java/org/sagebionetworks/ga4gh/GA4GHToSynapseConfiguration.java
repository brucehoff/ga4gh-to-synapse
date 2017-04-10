package org.sagebionetworks.ga4gh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GA4GHToSynapseConfiguration {

	public static void main(String[] args) {
		SpringApplication.run(GA4GHToSynapseConfiguration.class, args);
	}

}
