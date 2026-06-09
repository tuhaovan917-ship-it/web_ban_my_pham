package do_an_java.quan_ly_my_pham.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    private static final Path RUNTIME_UPLOAD_DIRECTORY = Paths.get("uploads");
    private static final Path SOURCE_UPLOAD_DIRECTORY = Paths.get(
        "src",
        "main",
        "resources",
        "static",
        "uploads"
    );

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations(
                toResourceLocation(RUNTIME_UPLOAD_DIRECTORY),
                "classpath:/static/uploads/",
                toResourceLocation(SOURCE_UPLOAD_DIRECTORY),
                toResourceLocation(Paths.get("..", "src", "main", "resources", "static", "uploads"))
            );
    }

    private String toResourceLocation(Path path) {
        return path.toAbsolutePath().normalize().toUri().toString();
    }
}
