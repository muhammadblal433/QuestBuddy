package coms309;

import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

/** These are some custom endpoints for exp1. */
@RestController
@RequestMapping("/api/v1")
public class HelloController {

    // GET http://localhost:8081/api/v1/hello?name=Muhammad
    @GetMapping("/hello")
    public Map<String, Object> hello(@RequestParam(defaultValue = "World") String name) {
        return Map.of(
                "message", "Hello " + name + " from exp1!",
                "author", System.getProperty("user.name"),
                "timestamp", Instant.now().toString()
        );
    }

    // GET http://localhost:8081/api/v1/health
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "Health is UP. We going to the gym!");
    }
}
