package com.bms.reserva_servicio_backend.security.filter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.bms.reserva_servicio_backend.models.User;
import com.bms.reserva_servicio_backend.service.UserService;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static com.bms.reserva_servicio_backend.security.TokenJWTConfig.*;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private AuthenticationManager authenticationManager;
    private UserService userService;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        User user = null;
        String username = null;
        String password = null;

        try {
            user = new ObjectMapper().readValue(request.getInputStream(), User.class);
            username = user.getUsername();
            password = user.getPassword();
        } catch (StreamReadException e) {
            e.printStackTrace();
        } catch (DatabindException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
                password);
        return authenticationManager.authenticate(authenticationToken);

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {

        org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) authResult
                .getPrincipal();
        String username = userDetails.getUsername();
        Collection<? extends GrantedAuthority> roles = authResult.getAuthorities();

        // Obtener el usuario completo desde la base de datos para tener el ID y email
        User appUser = null;
        try {
            appUser = userService.findByUsername(username);
        } catch (Exception e) {
            System.err.println("Error al obtener usuario: " + e.getMessage());
        }

        // Actualizar último acceso del usuario
        try {
            userService.updateUltimoAcceso(username);
        } catch (Exception e) {
            // Log error pero no interrumpir el login
            System.err.println("Error al actualizar último acceso: " + e.getMessage());
        }

        // Construir los claims del token con toda la información necesaria
        Claims claims = Jwts.claims()
                .add("authorities", new ObjectMapper().writeValueAsString(roles))
                .add("username", username)
                .add("userId", appUser != null ? appUser.getId() : null)
                .add("email", appUser != null ? appUser.getEmail() : username)
                .add("roles", roles.stream()
                        .map(GrantedAuthority::getAuthority)
                        .toArray(String[]::new))
                .build();

        String token = Jwts.builder()
                .subject(username)  // ✅ CORREGIDO: usar username real, no "Joe"
                .claims(claims)
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .issuedAt(new Date())
                .signWith(SECRET_KEY)
                .compact();

        response.addHeader(HEADER_AUTHORIZATION, PREFIX_TOKEN + token);

        Map<String, Object> body = new HashMap<>();
        body.put("token", token);
        body.put("username", username);
        body.put("userId", appUser != null ? appUser.getId() : null);
        body.put("email", appUser != null ? appUser.getEmail() : username);
        body.put("message", String.format("Hola %s, has iniciado sesión con éxito", username));

        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.setStatus(200);
        response.setContentType(CONTENT_TYPE_JSON);

    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) throws IOException, ServletException {
        Map<String, String> body = new HashMap<>();
        body.put("message", "Error de autenticación: username o password incorrecto");
        body.put("error", failed.getMessage());
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
        response.setStatus(401);
        response.setContentType(CONTENT_TYPE_JSON);

    }

}
