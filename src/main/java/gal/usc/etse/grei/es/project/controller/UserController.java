package gal.usc.etse.grei.es.project.controller;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Friendship;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
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

            return ResponseEntity.of(users.getAll(page, size, Sort.by(criteria), email, name));
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Get single user
    @GetMapping(
            path = "{email}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN') or #email == principal or @userService.areFriends(#email, principal)")
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

    //Modify user
    @PatchMapping(
            path = "{email}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<User> modifyUser(
            @PathVariable("email") String email,
            @RequestBody List<Map<String, Object>> updates
    ) {
        try {
            if(users.get(email).isEmpty()){ return ResponseEntity.notFound().build(); }
            if(updates.isEmpty() || updates.stream().filter(stringObjectMap -> stringObjectMap.values().contains("/email")).count() > 0){ return ResponseEntity.status(422).build(); }
            return ResponseEntity.of(users.modifyUser(email, updates));
        }catch (JsonPatchException e){
            return ResponseEntity.status(400).build();
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(422).build();
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

    //Get friendships
    @GetMapping(
            path = "{email}/friends",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Page<Friendship>> getFriendships(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @RequestParam(name = "email", defaultValue = "") String email
    ) {
        try{
            List<Sort.Order> criteria = sort.stream().map(string -> {
                if(string.startsWith("+")){
                    return Sort.Order.asc(string.substring(1));
                } else if (string.startsWith("-")) {
                    return Sort.Order.desc(string.substring(1));
                } else return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            return ResponseEntity.of(users.getFriendships(page, size, Sort.by(criteria), email));
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Get friendships
    @GetMapping(
            path = "{email}/friends/{friendEmail}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Friendship> getFriendship(
            @PathVariable("email") String email,
            @PathVariable("friendEmail") String friendEmail
    ) {
        try{
            return ResponseEntity.of(users.getFriendship(email, friendEmail));
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Add friend to user
    @PostMapping(
            path = "{email}/friends/{friendEmail}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Friendship> addFriend(
            @PathVariable("email") String email,
            @PathVariable("friendEmail") String friendEmail
    ) {
        try {
            if(users.get(friendEmail).isEmpty() || users.get(email).isEmpty()){ return ResponseEntity.notFound().build(); }
            if(users.getFriendship(email, friendEmail).isPresent()){ return ResponseEntity.status(409).body(users.getFriendship(email, friendEmail).get()); }
            return ResponseEntity.of(users.addFriend(email, friendEmail));
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Modify user
    @PatchMapping(
            path = "{email}/friends/{friendEmail}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Friendship> modifyFriendship(
            @PathVariable("email") String email,
            @PathVariable("friendEmail") String friendEmail,
            @RequestBody List<Map<String, Object>> updates
    ) {
        try {
            if(users.getFriendship(email, friendEmail).isEmpty()){ return ResponseEntity.notFound().build(); }
            if(updates.isEmpty()
                    || updates.stream().filter(stringObjectMap -> stringObjectMap.values().contains("/id")).count() > 0
                    || updates.stream().filter(stringObjectMap -> stringObjectMap.values().contains("/user")).count() > 0
                    || updates.stream().filter(stringObjectMap -> stringObjectMap.values().contains("/friend")).count() > 0
                    || updates.stream().filter(stringObjectMap -> stringObjectMap.values().contains("/since")).count() > 0
            ){ return ResponseEntity.status(422).build(); }
            return ResponseEntity.of(users.modifyFriendship(email, friendEmail, updates));
        }catch (JsonPatchException e){
            return ResponseEntity.status(400).build();
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(422).build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Delete friend
    @DeleteMapping(path = "{email}/friends/{friendEmail}")
    ResponseEntity<Friendship> deleteFriend(@PathVariable("email") String email, @PathVariable("friendEmail") String friend) {
        try{
            if(users.getFriendship(email, friend).isEmpty()){ return ResponseEntity.notFound().build(); }
            users.deleteFriend(email, friend);
            return ResponseEntity.noContent().build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
