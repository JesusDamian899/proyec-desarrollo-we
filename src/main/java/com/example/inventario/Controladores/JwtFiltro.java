package com.example.inventario.Controladores;


import com.example.inventario.Servicios.DetalleUsuarioService;
import com.example.inventario.Servicios.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFiltro extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final DetalleUsuarioService detalleUsuarioService;

    public JwtFiltro(JwtService jwtService,
                     DetalleUsuarioService detalleUsuarioService) {
        this.jwtService = jwtService;
        this.detalleUsuarioService = detalleUsuarioService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        String cabecera = request.getHeader("Authorization");

        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            String token = cabecera.substring(7);

            if (jwtService.esValido(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    UserDetails detalle = detalleUsuarioService
                            .loadUserByUsername(jwtService.extraerUsername(token));

                    if (detalle.isEnabled()) {
                        var auth = new UsernamePasswordAuthenticationToken(
                                detalle, null, detalle.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                } catch (Exception e) {
                    SecurityContextHolder.clearContext();
                }
            }
        }

        chain.doFilter(request, response);
    }
}
