package coms309.people;

import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Controller used to showcase Create and Read from a LIST
 *
 * @author Vivek Bengre
 */

@RestController
public class PeopleController {

    // Note that there is only ONE instance of PeopleController in 
    // Springboot system.
    HashMap<String, Person> peopleList = new  HashMap<>();

    //CRUDL (create/read/update/delete/list)
    // use POST, GET, PUT, DELETE, GET methods for CRUDL

    // THIS IS THE LIST OPERATION
    // gets all the people in the list and returns it in JSON format
    // This controller takes no input. 
    // Springboot automatically converts the list to JSON format 
    // in this case because of @ResponseBody
    // Note: To LIST, we use the GET method
    @GetMapping("/people")
    public  HashMap<String,Person> getAllPersons() {
        return peopleList;
    }

    // THIS IS THE CREATE OPERATION
    // springboot automatically converts JSON input into a person object and 
    // the method below enters it into the list.
    // It returns a string message in THIS example.
    // Note: To CREATE we use POST method
    @PostMapping("/people")
    public  String createPerson(@RequestBody Person person) {
        System.out.println(person);
        peopleList.put(person.getFirstName(), person);
        String s = "New person "+ person.getFirstName() + " Saved";
        return s;
        //public  ResponseEntity<Map<String, String>>  //unused
        // createPerson(@RequestBody Person person) { // unused
        //Map <String, String> body = new HashMap<>();// unused
        //body.put("message", s); // unused
        //ResponseEntity<>(body, HttpStatus.OK); // unused
    }

    // THIS IS THE READ OPERATION
    // Springboot gets the PATHVARIABLE from the URL
    // We extract the person from the HashMap.
    // springboot automatically converts Person to JSON format when we return it
    // Note: To READ we use GET method
    @GetMapping("/people/{firstName}")
    public Person getPerson(@PathVariable String firstName) {
        Person p = peopleList.get(firstName);
        return p;
    }

    // THIS IS A GET METHOD
    // RequestParam is expected from the request under the key "name"
    // returns all names that contains value passed to the key "name"
    @GetMapping("/people/contains")
    public List<Person> getPersonByParam(@RequestParam("name") String name) {
        List<Person> res = new ArrayList<>(); 
        for (Person p : peopleList.values()) {
            if (p.getFirstName().contains(name) || p.getLastName().contains(name))
                res.add(p);
        }
        return res;
    }

    // THIS IS THE UPDATE OPERATION
    // We extract the person from the HashMap and modify it.
    // Springboot automatically converts the Person to JSON format
    // Springboot gets the PATHVARIABLE from the URL
    // Here we are returning what we sent to the method
    // Note: To UPDATE we use PUT method
    @PutMapping("/people/{firstName}")
    public Person updatePerson(@PathVariable String firstName, @RequestBody Person p) {
        peopleList.replace(firstName, p);
        return peopleList.get(firstName);
    }


    // THIS IS THE DELETE OPERATION
    // Springboot gets the PATHVARIABLE from the URL
    // We return the entire list -- converted to JSON
    // Note: To DELETE we use delete method
    
    @DeleteMapping("/people/{firstName}")
    public ResponseEntity<String> deletePerson(@PathVariable String firstName) {
        if (peopleList.remove(firstName) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No person found with name: " + firstName);
        }
        return ResponseEntity.ok("Deleted person: " + firstName);
    }

    // EXTRA 1: FUN FACT OPERATION/endpoint
    // Gets a fun fact for a Person using the PATHVARIABLE {firstName}.
    // If the Person or their funFact does not exist, return NOT_FOUND.
    // Note: To READ this, we use the GET method.

    @GetMapping("/people/funfact/{firstName}")
    public ResponseEntity<String> funFact(@PathVariable String firstName) {
        Person p = peopleList.get(firstName);
        if (p == null || p.getFunFact() == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No fun fact found for " + firstName);
        }
        return ResponseEntity.ok(firstName + "'s fun fact: " + p.getFunFact());
    }

    // EXTRA 2: OLDER THAN OPERATION
    // Gets all people older than the given {age}.
    // Example: /people/olderthan/20
    // Note: To FILTER, we use the GET method.

    @GetMapping("/people/olderthan/{age}")
    public List<Person> getPeopleOlderThan(@PathVariable int age) {
        List<Person> result = new ArrayList<>();
        for (Person p : peopleList.values()) {
            if (p.getAge() > age) {
                result.add(p);
            }
        }
        return result;
    }

    // EXTRA 3: RANDOM PERSON OPERATION
    // Returns a random Person from the list.
    // If no Person exists, returns NOT_FOUND.
    // Note: To GET RANDOM, we use the GET method.

    @GetMapping("/people/random")
    public ResponseEntity<Person> getRandomPerson() {
        if (peopleList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        List<Person> people = new ArrayList<>(peopleList.values());
        Random rand = new Random();
        return ResponseEntity.ok(people.get(rand.nextInt(people.size())));
    }
}

    }
} // end of people controller

