package co.edu.unbosque.config;  // Usa el paquete que prefieras

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(
                "https://website-deltamusic-frontend.vercel.app", // Cambia por tu dominio real de Vercel
                "https://website-deltamusic.vercel.app") // Puedes poner más dominios si tienes varios
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
