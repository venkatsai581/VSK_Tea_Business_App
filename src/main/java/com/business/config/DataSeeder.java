package com.business.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.business.entities.Admin;
import com.business.entities.Product;
import com.business.entities.User;
import com.business.repositories.AdminRepository;
import com.business.repositories.ProductRepository;
import com.business.repositories.UserRepository;

/** On an empty database creates a first admin (so you can log in) and a few sample products. */
@Component
public class DataSeeder implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

	@Autowired
	private AdminRepository adminRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private PasswordEncoder encoder;

	@Value("${app.default-admin.email:admin@business.com}")
	private String adminEmail;
	@Value("${app.default-admin.password:Admin@123}")
	private String adminPassword;
	@Value("${app.seed.sample-products:true}")
	private boolean sampleProducts;
	@Value("${app.seed.demo-user:false}")
	private boolean demoUser;

	@Override
	public void run(String... args) {
		if (adminRepository.count() == 0) {
			Admin a = new Admin();
			a.setAdminName("Administrator");
			a.setAdminEmail(adminEmail);
			a.setAdminNumber("9999999999");
			a.setAdminPassword(encoder.encode(adminPassword));
			adminRepository.save(a);
			log.warn("No admin existed, so one was created: {} / {}  -> change this password after logging in.",
					adminEmail, adminPassword);
		}
		if (sampleProducts && productRepository.count() == 0) {
			add("Assam Gold Tea", 420, "Strong, malty CTC leaves from Assam. Great with milk.");
			add("Darjeeling First Flush", 780, "Light, floral and fragrant. Best enjoyed without milk.");
			add("Nilgiri Fresh Tea", 360, "Bright and brisk everyday tea from the Nilgiri hills.");
			add("Masala Chai Blend", 450, "Tea leaves blended with cardamom, ginger and cloves.");
			add("Green Tea Classic", 520, "Smooth, fresh green tea leaves rich in antioxidants.");
			add("Lemon Ginger Tea", 480, "A zesty, soothing blend for cold days.");
		}
		if (demoUser && userRepository.count() == 0) {
			User u = new User();
			u.setUname("Demo User");
			u.setUemail("user@business.com");
			u.setUnumber(9876543210L);
			u.setUpassword(encoder.encode("User@123"));
			userRepository.save(u);
			log.warn("Demo user created: user@business.com / User@123");
		}
	}

	private void add(String name, double price, String description) {
		Product p = new Product();
		p.setPname(name);
		p.setPprice(price);
		p.setPdescription(description);
		productRepository.save(p);
	}
}
