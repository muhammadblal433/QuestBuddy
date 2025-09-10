package coms309;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Map;

/**
 * Time Based Greeting with name
 *
 * @author Ayaan Syed
 */

/**
 * Endpoints for exp1
 */
@RestController
@RequestMapping("/api/v1")

class FullGreetingController {

    private static final String DEFAULT_TZ = "America/Chicago";

    // Default greeting without time and name
    @GetMapping("/full")
    public String defaultHello() {
        return "Hi! Nice to meet you!";
    }

    // Default greeting without time
    @GetMapping("/full/{name}")
    public String namedHello(@PathVariable String name) {
        return "Hi " + name + "! Nice to meet you!";
    }

    // Time-aware greeting without a name
    // e.g. GET /smart        or  /smart?tz=Asia/Singapore
    @GetMapping("/full/smart")
    public String smartGreetingDefault(@RequestParam(required = false) String tz) {
        ZoneId zone = zoneOrDefault(tz);
        LocalTime now = LocalTime.now(zone);
        return "Good " + partOfDay(now) + ", World!";
    }

    // Time-aware greeting with a name
    // e.g. GET /smart/Ayaan  or  /smart/Ayaan?tz=America/Chicago
    @GetMapping("/full/smart/{name}")
    public String smartGreeting(@PathVariable String name,
                                @RequestParam(required = false) String tz) {
        ZoneId zone = zoneOrDefault(tz);
        LocalTime now = LocalTime.now(zone);
        return "Good " + partOfDay(now) + ", " + name + "!";
    }

    // Falls back to DEFAULT_TZ if input is blank or invalid
    private ZoneId zoneOrDefault(String tz) {
        try {
            if (tz == null || tz.isBlank()) {
                return ZoneId.of(DEFAULT_TZ);
            }
            return ZoneId.of(tz.trim());
        }
        catch (DateTimeException ex) {
            return ZoneId.of(DEFAULT_TZ);
        }
    }

    // Early morning 00:00–04:59, Morning 05:00–11:59, Afternoon 12:00–16:59, Evening 17:00–23:59
    private String partOfDay(LocalTime t) {
        int h = t.getHour();
        if (h < 5)  return "early morning";
        if (h < 12) return "morning";
        if (h < 17) return "afternoon";
        return "evening";
    }
}