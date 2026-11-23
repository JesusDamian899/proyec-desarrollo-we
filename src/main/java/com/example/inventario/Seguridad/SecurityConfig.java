package com.example.inventario.Seguridad;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFiltro jwtFiltro;
    private final DetalleUsuarioService detalleUsuarioService;

    public SecurityConfig(JwtFiltro jwtFiltro,
                          DetalleUsuarioService detalleUsuarioService) {
        this.jwtFiltro = jwtFiltro;
        this.detalleUsuarioService = detalleUsuarioService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider proveedor = new DaoAuthenticationProvider(detalleUsuarioService);
        proveedor.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(proveedor);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(
                    "/", "/index.html", "/*.js", "/*.css", "/favicon.ico", "/assets/**",
                    "/login", "/resumen", "/articulos", "/catalogos", "/proveedores", "/seguridad", "/almacen"
                ).permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/api/auth/login").permitAll()

                // Seguridad: Usuarios, Roles y Permisos
                .requestMatchers("/api/permisos/**").hasAuthority("ROL_GESTIONAR")
                .requestMatchers("/api/roles/**").hasAuthority("ROL_GESTIONAR")
                .requestMatchers("/api/usuarios/**").hasAuthority("USUARIO_GESTIONAR")

                // Kardex
                .requestMatchers(HttpMethod.GET, "/api/kardex/**").hasAuthority("KARDEX_VER")
                .requestMatchers(HttpMethod.POST, "/api/kardex/**")
                    .hasAnyAuthority("STOCK_AJUSTAR", "INGRESO_CREAR", "SALIDA_ATENDER", "TRANSFER_CREAR")

                // Articulos
                .requestMatchers(HttpMethod.GET, "/api/articulos/**").hasAuthority("ARTICULO_VER")
                .requestMatchers(HttpMethod.POST, "/api/articulos/**").hasAuthority("ARTICULO_GESTIONAR")
                .requestMatchers(HttpMethod.PUT, "/api/articulos/**").hasAuthority("ARTICULO_GESTIONAR")
                .requestMatchers(HttpMethod.DELETE, "/api/articulos/**").hasAuthority("ARTICULO_GESTIONAR")

                // Catalogos maestros: Categorias, Unidades y Ubicaciones
                .requestMatchers(HttpMethod.GET, "/api/categorias/**", "/api/unidades-medida/**", "/api/ubicaciones/**")
                    .authenticated()
                .requestMatchers(HttpMethod.POST, "/api/categorias/**", "/api/unidades-medida/**", "/api/ubicaciones/**")
                    .hasAuthority("MAESTRO_GESTIONAR")
                .requestMatchers(HttpMethod.PUT, "/api/categorias/**", "/api/unidades-medida/**", "/api/ubicaciones/**")
                    .hasAuthority("MAESTRO_GESTIONAR")
                .requestMatchers(HttpMethod.DELETE, "/api/categorias/**", "/api/unidades-medida/**", "/api/ubicaciones/**")
                    .hasAuthority("MAESTRO_GESTIONAR")

                // Proveedores
                .requestMatchers(HttpMethod.GET, "/api/proveedores/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/proveedores/**").hasAuthority("PROVEEDOR_GESTIONAR")
                .requestMatchers(HttpMethod.PUT, "/api/proveedores/**").hasAuthority("PROVEEDOR_GESTIONAR")
                .requestMatchers(HttpMethod.DELETE, "/api/proveedores/**").hasAuthority("PROVEEDOR_GESTIONAR")

                // Operaciones de Almacen (Ingresos, Salidas, Transferencias)
                .requestMatchers(HttpMethod.GET, "/api/almacen/ingresos/**", "/api/ingresos/**")
                    .hasAnyAuthority("INGRESO_VER", "INGRESO_CREAR")
                .requestMatchers(HttpMethod.POST, "/api/almacen/ingresos/**", "/api/ingresos/**")
                    .hasAuthority("INGRESO_CREAR")

                .requestMatchers(HttpMethod.GET, "/api/almacen/salidas/**", "/api/salidas/**")
                    .hasAnyAuthority("SALIDA_VER", "SALIDA_ATENDER", "SOLICITUD_VER_PROPIAS")
                .requestMatchers(HttpMethod.POST, "/api/almacen/salidas/**", "/api/salidas/**")
                    .hasAnyAuthority("SALIDA_ATENDER", "SALIDA_APROBAR", "SOLICITUD_CREAR")

                .requestMatchers(HttpMethod.GET, "/api/almacen/transferencias/**", "/api/transferencias/**")
                    .hasAnyAuthority("TRANSFER_VER", "TRANSFER_CREAR")
                .requestMatchers(HttpMethod.POST, "/api/almacen/transferencias/**", "/api/transferencias/**")
                    .hasAuthority("TRANSFER_CREAR")

                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(HttpStatus.UNAUTHORIZED.value());
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.setCharacterEncoding("UTF-8");
                    Map<String, Object> cuerpo = new LinkedHashMap<>();
                    cuerpo.put("estado", 401);
                    cuerpo.put("mensaje", "Token ausente, invalido o expirado");
                    res.getWriter().write(mapper.writeValueAsString(cuerpo));
                })
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(HttpStatus.FORBIDDEN.value());
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.setCharacterEncoding("UTF-8");
                    Map<String, Object> cuerpo = new LinkedHashMap<>();
                    cuerpo.put("estado", 403);
                    cuerpo.put("mensaje", "No tiene permiso para esta operacion");
                    res.getWriter().write(mapper.writeValueAsString(cuerpo));
                })
            )
            .addFilterBefore(jwtFiltro, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}