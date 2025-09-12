package coms309.people;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Extra Controller that provides statistics about the people list.
 *
 * DEMO TIP: Great for showing how we can calculate or summarize data
 * instead of just returning it directly.
 */
@RestController
public class StatsController {

    private final PeopleController peopleController;

    public StatsController(PeopleController peopleController) {
        this.peopleController = peopleController;
    }

    // GET TOTAL COUNT
    // Returns how many people are currently stored in the system.
    // Comment for myself: Add people, delete one, then show how the count changes live.

    @GetMapping("/stats/count")
    public int getCount() {
        return peopleController.getAllPersons().size();
    }

    // GET LIST OF FAVORITE FOODS
    // Returns a list of favorite foods from all people.
    // Comment for myself: Add 2–3 people with different favorite foods,
    // then call this endpoint to show how we can extract just one property.

    @GetMapping("/stats/foods")
    public List<String> getFavoriteFoods() {
        return peopleController.getAllPersons()
                .values()
                .stream()
                .map(Person::getFavoriteFood)
                .collect(Collectors.toList());
    }
}