package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import java.util.List;
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
    ResponseEntity<Page<User>> get(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort
    ) {
        List<Sort.Order> criteria = sort.stream().map(string -> {
            if(string.startsWith("+")){
                return Sort.Order.asc(string.substring(1));
            } else if (string.startsWith("-")) {
                return Sort.Order.desc(string.substring(1));
            } else return null;
        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return ResponseEntity.of(users.get(page, size, Sort.by(criteria)));
    }

    //Get single user
    @GetMapping(
            path = "{email}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<User> get(@PathVariable("email") String email) {
        if (email.equals("tea")){return ResponseEntity.status(418).build();}
        return ResponseEntity.of(users.get(email));
    }

    //Create user
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<User> post(@RequestBody @Valid User user) {
        try {
            if(users.get(user.getEmail()).isPresent()){
                return ResponseEntity.status(409).build();
            }
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.of(users.post(user));
    }

    //Modify user
    @PatchMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<User> patch(@RequestBody @Valid User user) {
        try {
            if(users.get(user.getEmail()).isEmpty()){
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.of(users.patch(user));
    }

    //Add friend to user
    @PatchMapping(
            path = "{email}/friends",
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<User> patch(@PathVariable("email") String email, @RequestBody @Valid User friend) {
        try {
            if(users.get(friend.getEmail()).isEmpty() || users.get(email).isEmpty()){
                return ResponseEntity.notFound().build();
            }
            if(!users.get(friend.getEmail()).get().equals(friend)){
                return ResponseEntity.status(409).build();
            }
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.of(users.patch(email, friend));
    }

    //Delete user
    @DeleteMapping(path = "{email}")
    ResponseEntity<Object> delete(@PathVariable("email") String email) {
        try{
            users.delete(email);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    //Delete friend
    @DeleteMapping(path = "{email}/friends/{friendEmail}")
    ResponseEntity<User> delete(@PathVariable("email") String email, @PathVariable("friendEmail") String friend) {
        try{
            return ResponseEntity.of(users.delete(email, friend));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
