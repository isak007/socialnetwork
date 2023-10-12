package com.ftn.socialnetwork.security.jwt;

import com.ftn.socialnetwork.model.User;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {

    //Secret used for encrypting and decrypting jwt token https://jwt.io/
    @Value("secretKey")
    private String jwtSecret;

    //Duration of JWT token validation
    @Value("60000000") // around 16h 30min
    private Long jwtExpirationMs;

    @Value("300000") // 5mins
    private Long passwordResetJwtExpirationMs;

    // Signature algorithm for signing JWT
    private SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;

    private final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuer("snDOO")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .claim("authorities", user.getAuthorities())
                .claim("userId",user.getId())
                .signWith(SIGNATURE_ALGORITHM, jwtSecret)
                .compact();
    }

    public String generatePasswordResetToken(String email, String resetCode) {
        return Jwts.builder()
                //.setSubject(email)
                .setIssuer("snDOO")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + passwordResetJwtExpirationMs))
                .claim("email", email)
                .claim("resetCode", resetCode)
                .signWith(SIGNATURE_ALGORITHM, jwtSecret)
                .compact();
    }

    public Date getExpirationDate(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }

    public String getUsername(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public String getPasswordResetAttributes(String token, String attribute) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();

        return claims.get(attribute,String.class);
    }

    public Long getUserId(String token) {
        String[] split_string = token.split("\\.");
        String base64EncodedBody = split_string[1];
        Base64 base64Url = new Base64(true);
        String body = new String(base64Url.decode(base64EncodedBody));
        body = body.substring(1, body.length()-1);
        LinkedList<String> myList = new LinkedList<String>(Arrays.asList(body.split(",")));
        String userId = null;
        for(String pair : myList)
        {
            if (pair.contains("userId")){
                userId = pair.split(":")[1];
                break;
            }
        }
        return userId != null ? Long.valueOf(userId) : null;
    }

    public boolean validate(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature - {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token - {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token - {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token - {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty - {}", ex.getMessage());
        }
        return false;
    }

}
