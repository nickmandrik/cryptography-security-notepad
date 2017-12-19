package mandrik.security.notepad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CryptographySecurityNotepadApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptographySecurityNotepadApplication.class, args);
	}
}
