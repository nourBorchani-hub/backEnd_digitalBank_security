package org.sid.ebankingbackend.Security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties.Build;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties.Jwt;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mysql.cj.protocol.x.XpluginStatementCommand;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RestController
@RequestMapping("/auth")
public class SecurityController {

	@Autowired
	private AuthenticationManager authMang;
	@Autowired
	private JwtEncoder jwtEncoder;
// consulter tout les users authentifié
	@GetMapping("/profile")
	public Authentication authentication(Authentication authentication) {
		return authentication;

	}

	// Endpoint :*récupere le requéte http
	// *authentifier l'utilisateur
	// *si l'existe il va génere le token avec ses paramétre
	// *retourner dans un objet json comme clé acces token
	@PostMapping("/login")
	public Map<String, String> login(String username, String password) {
		org.springframework.security.core.Authentication authentication = authMang
				.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		Instant instant = Instant.now();// importer le date systéme pour crée le jwt
		String scop = authentication.getAuthorities().stream().map(a -> a.getAuthority())
				.collect(Collectors.joining(" ")); // génerer le list role user
		JwtClaimsSet jwtClaimeSet = JwtClaimsSet.builder().issuedAt(instant)
				.expiresAt(instant.plus(10, ChronoUnit.MINUTES)).subject(username).claim("scop", scop) // ajouter les
																										// roles de
																										// l'utilisateur
				.build();
		JwtEncoderParameters jwtEncoderParametres = JwtEncoderParameters
				.from(JwsHeader.with(MacAlgorithm.HS512).build(), jwtClaimeSet);
		String jwt = jwtEncoder.encode(jwtEncoderParametres).getTokenValue(); // retourner le token sous forme de string
		return Map.of("accessToken", jwt);

	}

}
