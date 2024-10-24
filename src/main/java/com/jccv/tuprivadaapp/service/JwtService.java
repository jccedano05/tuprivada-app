package com.jccv.tuprivadaapp.service;

import com.jccv.tuprivadaapp.jwt.config.CustomLogoutHandler;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.repository.TokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret.key}")
    private String SECRET_KEY;

    @Value("${jwt.time.expiration}")
    private String timeExpiration;


    private Logger logger = LoggerFactory.getLogger(JwtService.class);
    private final TokenRepository tokenRepository;

    @Autowired
    CustomLogoutHandler logoutHandler;

    public JwtService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public String extractUsername(String token){
        try {
            return extractClaim(token, Claims::getSubject);
    } catch (SignatureException e) {
            logger.debug("signature exception"+e);
        } catch (MalformedJwtException e) {
            logger.debug("token malformed"+e);
        } catch (ExpiredJwtException e) {
            logger.debug("token expired"+e);
        } catch (UnsupportedJwtException e) {
            logger.debug("unsupported"+e);
        } catch (IllegalArgumentException e) {
            logger.debug("Illegal"+e);
        }
    return null;
    }

    public boolean isValid(String token, UserDetails user){
        String username = extractUsername(token);

        boolean isValidToken = tokenRepository.findByToken(token).map(t-> !t.isLoggedOut()).orElse(false);
        return (username.equals(user.getUsername()) && !isTokenExpired(token) && isValidToken);
    }

    public boolean isValid(String token){
        try{
//
        boolean isValidToken = tokenRepository.findByToken(token).map(t-> !t.isLoggedOut()).orElse(false);

        return (isValidToken && !isTokenExpired(token) );
    } catch (SignatureException e) {
            System.out.println("SignatureException");
        logger.debug("signature exception"+e);
    } catch (MalformedJwtException e) {
            System.out.println("MalformedJwtException");
        logger.debug("token malformed"+e);

    } catch (ExpiredJwtException e) {
            System.out.println("ExpiredJwtException");
        logger.debug("token expired"+e);

    } catch (UnsupportedJwtException e) {
            System.out.println("UnsupportedJwtException");
        logger.debug("unsupported"+e);

    } catch (IllegalArgumentException e) {
            System.out.println("IllegalArgumentException");
            logger.debug("Illegal"+e);

        } catch (Exception e) {
            System.out.println(e);
            logger.debug("Another Exception"+e);
        }
    return false;
    }

    private boolean isTokenExpired(String token) {
        if (extractExpiration(token).before(new Date())){
            System.out.println("token date: " + extractExpiration(token));
            System.out.println("actual date: " + new Date());
            System.out.println("token expirado");
        }
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver){
        Claims claims = extraAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extraAllClaims(String token)  {
            Claims claims = Jwts
                    .parser()
                    .verifyWith(getSigninKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims;
    }

    public String generateToken(User user){
        return   Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date( System.currentTimeMillis() + Long.parseLong(timeExpiration)))
                .signWith(getSigninKey())
                .compact();
    }


    private SecretKey getSigninKey(){
        byte[] keyBytes = Decoders.BASE64URL.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
