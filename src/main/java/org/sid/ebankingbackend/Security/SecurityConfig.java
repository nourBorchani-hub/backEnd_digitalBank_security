package org.sid.ebankingbackend.Security;

import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
	@Value("${jwt.secret}") // @ Value pour importer un variable d'envirennement jwt.secret de fichier
							// proprities (varibale d'envirennement
	public String SecretKey;

	// cree des user en mémoire
	@Bean
	public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
		PasswordEncoder passwordEncoder = passwordEncoder();
		return new InMemoryUserDetailsManager(
				User.withUsername("user1").password(passwordEncoder.encode("1234")).authorities("user").build(),
				User.withUsername("user2").password(passwordEncoder.encode("1234")).authorities("user", "ADMIN")
						.build());

	}

	// encoder le mot de passe
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	// filtre de protection
	public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
		return httpSecurity
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(ar-> ar.requestMatchers("/auth/login**").permitAll())
				.csrf(csrf -> csrf.disable())
				.cors(Customizer.withDefaults()) //Utiliser la configuration CORS par défaut
				.authorizeHttpRequests(ar -> ar.anyRequest().authenticated())
				// .httpBasic(Customizer.withDefaults()) //configuration par défaut
				.oauth2ResourceServer(oa -> oa.jwt(Customizer.withDefaults()))
				.build();
		
	}

	// génerer token
	@Bean
	public JwtEncoder jwtEncoder() {
		// Vous devez fournir une clé secrète ou une paire de clés RSA pour signer le
		// JWT.

		// String
		// SecretKey="cM9tWv2MPhA94uMzPPxEJ7hA+m7E0u2B3wjgD6rnm6tNfN8Vh4hS5UwO7F/j6U0H\r\n";

		// Utilisez NimbusJwtEncoder pour créer un encodeur JWT avec une clé secrète.
		return new NimbusJwtEncoder(new ImmutableSecret<>(SecretKey.getBytes()));

	}

	// filtre intercepte la requette et vérifier le indormation des user a travs son
	// jwt
	@Bean
	JwtDecoder jwtDecoder() {
		// String
		// SecretKey="cM9tWv2MPhA94uMzPPxEJ7hA+m7E0u2B3wjgD6rnm6tNfN8Vh4hS5UwO7F/j6U0H\r\n";
		SecretKeySpec secretKeySpec = new SecretKeySpec(SecretKey.getBytes(), "RSA");
		return NimbusJwtDecoder.withSecretKey(secretKeySpec).macAlgorithm(MacAlgorithm.HS512).build(); // algorithme
																										// symétrique
																										// HS512
	}

	@Bean
	// EndPoint Pour identifier l'utilisateur
	public AuthenticationManager authenticationManager(UserDetailsService userDetailService) {
		DaoAuthenticationProvider daoAuthent = new DaoAuthenticationProvider();
		daoAuthent.setPasswordEncoder(passwordEncoder());
		daoAuthent.setUserDetailsService(userDetailService);
		return new ProviderManager(daoAuthent);

	}
	 @Bean
	    public CorsConfigurationSource corsConfigurationSource() {
	        CorsConfiguration configuration = new CorsConfiguration();
	        configuration.addAllowedOrigin("*"); // authorisez tous origine
	        configuration.addAllowedMethod("*"); // authorisez tout les méthode de tout les origine
	   //     configuration.AllowedHeader("*"); // Vous pouvez spécifier les en-têtes autorisés
	    //  configuration.setExposedHeaders(List.of("x-auth-token")); // Ajoutez les en-têtes à exposer

	        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	        source.registerCorsConfiguration("/**", configuration);

	        return source;
	    }

}
