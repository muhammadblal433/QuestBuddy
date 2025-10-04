package coms309.people;

import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * Just for fun: greet people in different ways.
 *
 * This controller basically shows how we can
 * build extra endpoints that "talk" to the data in PeopleController
 * without duplicating the data logic.
 */
@RestController
public class GreetingController {

    private final PeopleController peopleController;

    public GreetingController(PeopleController peopleController) {
        this.peopleController = peopleController;
    }

    // GREET ONE PERSON
    // Takes a firstName as a PATHVARIABLE.
    // If that person exists, greet them by name and mention their favorite food.
    // Otherwise, return a "not found" style message.
    // Comment for myself: Try greeting someone who exists, then a random name that doesn't.

    @GetMapping("/greet/{firstName}")
    public String greetPerson(@PathVariable String firstName) {
        Person p = peopleController.getAllPersons().get(firstName);
        if (p == null) {
            return "No one named " + firstName + " was found to greet!";
        }
        return "Hello, " + p.getFirstName() + "! I hope you enjoy some " + p.getFavoriteFood() + " today!";
    }

    // GREET ALL PEOPLE
    // Loops over all the people currently in the system and greets them by name.
    // Comment for myself: add like 2–3 people first, then call this endpoint and you'll see one big greeting message.

    @GetMapping("/greet/all")
    public String greetAll() {
        return peopleController.getAllPersons()
                .values()
                .stream()
                .map(p -> "Hello " + p.getFirstName())
                .collect(Collectors.joining(", "));
    }
}
