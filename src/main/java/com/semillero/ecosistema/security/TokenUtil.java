package com.semillero.ecosistema.security;

import com.semillero.ecosistema.exceptions.ValidateTokenException;
import com.semillero.ecosistema.models.UserModel;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class TokenUtil {

    private static final String SECRET_KEY = "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e";

    public static String getSigningKey() {
        return SECRET_KEY;
    }

    public static String generateToken(UserModel user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600000); // 1 hour expiration
        String authorities = user.getRole().toString();
         return Jwts.builder()
                .setSubject((user.getEmail()))
                 .setId((user.getId().toString()))
                .claim("roles", authorities)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, getSigningKey())
                .compact();
    }

    public String extractTokenFromHeader(String authHeader) {
        if (authHeader.isEmpty()) {
            throw new ValidateTokenException("Token is missing in the request header.");
        } else if (!authHeader.startsWith("Bearer ")){
            throw new ValidateTokenException("Token sent in an invalid format");
        }
        return authHeader.substring(7);
    }

}
