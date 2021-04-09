package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("users")
public class UserController {
    private final UserService users;

    @Autowired
    public UserController(UserService users) {
        this.users = users;
    }

    //get all users
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Page<User>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @RequestParam(name = "email", defaultValue = "") String email,
            @RequestParam(name = "name", defaultValue = "") String name
    ) {
        try{
            List<Sort.Order> criteria = sort.stream().map(string -> {
                if(string.startsWith("+")){
                    return Sort.Order.asc(string.substring(1));
                } else if (string.startsWith("-")) {
                    return Sort.Order.desc(string.substring(1));
                } else return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            return ResponseEntity.of(users.get(page, size, Sort.by(criteria), email, name));
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Get single user
    @GetMapping(
            path = "{email}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<User> getUser(@PathVariable("email") String email) {
        try{
            if (email.equals("tea")){return ResponseEntity.status(418).build();}
            else if(users.get(email).isEmpty()){return ResponseEntity.notFound().build();}
            return ResponseEntity.of(users.get(email));
        }catch(Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Get user's assessments
    @GetMapping(
            path = "{email}/assessments",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Page<Assessment>> getAssessments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @PathVariable("email") String email
    ) {
        try {
            if(users.get(email).isEmpty()){return ResponseEntity.notFound().build();}
            List<Sort.Order> criteria = sort.stream().map(string -> {
                if(string.startsWith("+")){
                    return Sort.Order.asc(string.substring(1));
                } else if (string.startsWith("-")) {
                    return Sort.Order.desc(string.substring(1));
                } else return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            return ResponseEntity.of(users.getAssessments(page, size, Sort.by(criteria), email));
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Create user
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<User> post(@RequestBody @Valid User user) {
        try {
            if(users.get(user.getEmail()).isPresent()){ return ResponseEntity.status(409).body(users.get(user.getEmail()).get()); }
            return ResponseEntity.of(users.post(user));
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Update user
    @PutMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<User> updateUser(@RequestBody @Valid User user) {
        try {
            if(users.get(user.getEmail()).isEmpty()){ return ResponseEntity.notFound().build(); }
            return ResponseEntity.of(users.updateUser(user));
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Add friend to user
    @PutMapping(
            path = "{email}/friends",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<User> addFriend(@PathVariable("email") String email, @RequestBody @Valid User friend) {
        try {
            if(users.get(friend.getEmail()).isEmpty() || users.get(email).isEmpty()){ return ResponseEntity.notFound().build(); }
            else if(!users.get(friend.getEmail()).get().equals(friend)){ return ResponseEntity.status(409).build(); }
            return ResponseEntity.of(users.addFriend(email, friend));
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Delete user
    @DeleteMapping(path = "{email}")
    ResponseEntity<Object> deleteUser(@PathVariable("email") String email) {
        try{
            if(users.get(email).isEmpty()){ return ResponseEntity.notFound().build(); }
            users.deleteUser(email);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    //Delete friend
    @DeleteMapping(path = "{email}/friends/{friendEmail}")
    ResponseEntity<User> deleteFriend(@PathVariable("email") String email, @PathVariable("friendEmail") String friend) {
        try{
            if(users.get(email).isEmpty() || users.get(email).get().getFriend(friend).getEmail().isEmpty()){ return ResponseEntity.notFound().build(); }
            return ResponseEntity.of(users.deleteFriend(email, friend));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
