package coms309;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
class WelcomeController {

    // GET http://localhost:8081/
    @GetMapping("/")
    public String welcome() {
        return "Hello and welcome to COMS 309";
    }

    // GET http://localhost:8081/Muhammad
    @GetMapping("/{name}")
    public String welcome(@PathVariable String name) {
        return "Hello and welcome to COMS 309: " + name;
    }
}
