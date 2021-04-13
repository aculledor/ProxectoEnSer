package gal.usc.etse.grei.es.project.controller;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.model.Friendship;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.AssessmentService;
import gal.usc.etse.grei.es.project.service.MovieService;
import gal.usc.etse.grei.es.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("users")
public class UserController {
    private final UserService users;
    private final LinkRelationProvider relationProvider;
    private final AssessmentService assessments;
    private final MovieService movies;

    @Autowired
    public UserController(UserService users, LinkRelationProvider relationProvider, AssessmentService assessments, MovieService movies) {
        this.users = users;
        this.relationProvider = relationProvider;
        this.movies = movies;
        this.assessments = assessments;
    }

    //get all users
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<MappingJacksonValue> getAll(
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

            Optional<Page<User>> result = users.getAll(page, size, Sort.by(criteria), email, name);
            SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.filterOutAllExcept("name", "country", "birthday", "picture");
            FilterProvider filterProvider = new SimpleFilterProvider().addFilter("userFilter", filter);

            if(result.isPresent()) {
                Page<User> data = result.get();
                Pageable metadata = data.getPageable();

                Link self = linkTo(
                        methodOn(UserController.class).getAll(page, size, sort, email, name)
                ).withSelfRel();
                Link first = linkTo(
                        methodOn(UserController.class).getAll(metadata.first().getPageNumber(), size, sort, email, name)
                ).withRel(IanaLinkRelations.FIRST);
                Link last = linkTo(
                        methodOn(UserController.class).getAll(data.getTotalPages() - 1, size, sort, email, name)
                ).withRel(IanaLinkRelations.LAST);
                Link next = linkTo(
                        methodOn(UserController.class).getAll(metadata.next().getPageNumber(), size, sort, email, name)
                ).withRel(IanaLinkRelations.NEXT);
                Link previous = linkTo(
                        methodOn(UserController.class).getAll(metadata.previousOrFirst().getPageNumber(), size, sort, email, name)
                ).withRel(IanaLinkRelations.PREVIOUS);

                Link one = linkTo(
                        methodOn(UserController.class).getUser(null)
                ).withRel(relationProvider.getItemResourceRelFor(User.class));

                MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(result.get());
                mappingJacksonValue.setFilters(filterProvider);

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, first.toString())
                        .header(HttpHeaders.LINK, next.toString())
                        .header(HttpHeaders.LINK, previous.toString())
                        .header(HttpHeaders.LINK, last.toString())
                        .header(HttpHeaders.LINK, one.toString())
                        .body(mappingJacksonValue);
            }

            return ResponseEntity.notFound().build();
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
    ResponseEntity<MappingJacksonValue> getUser(@PathVariable("email") String email) {
        try{
            if (email.equals("tea")){return ResponseEntity.status(418).build();}
            else if(users.get(email).isEmpty()){return ResponseEntity.notFound().build();}
            //Crear o filtro iltros
            SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.serializeAllExcept("password");
            FilterProvider filterProvider = new SimpleFilterProvider().addFilter("userFilter", filter);

            Optional<User> user = users.get(email);
            if (user.isPresent()) {
                Link self = linkTo(methodOn(UserController.class).getUser(email)).withSelfRel();
                Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(User.class));

                //Filtrar a password
                MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(user.get());
                mappingJacksonValue.setFilters(filterProvider);

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .body(mappingJacksonValue);
            }
            return ResponseEntity.notFound().build();
        }catch(Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Create user
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<MappingJacksonValue> post(@RequestBody @Valid User user) {
        try {
            if(users.get(user.getEmail()).isPresent()){ return ResponseEntity.status(409).build(); }

            Optional<User> userAux = users.post(user);
            SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.serializeAllExcept("password");
            FilterProvider filterProvider = new SimpleFilterProvider().addFilter("userFilter", filter);

            if(userAux.isPresent()) {
                Link self = linkTo(methodOn(UserController.class).getUser(userAux.get().getEmail())).withSelfRel();
                Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(User.class));
                MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(userAux.get());
                mappingJacksonValue.setFilters(filterProvider);

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .body(mappingJacksonValue);
            }

            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Update user
    @PutMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<MappingJacksonValue> updateUser(@RequestBody @Valid User user) {
        try {
            if(users.get(user.getEmail()).isEmpty()){ return ResponseEntity.notFound().build(); }

            Optional<User> userAux = users.updateUser(user);
            SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.serializeAllExcept("password");
            FilterProvider filterProvider = new SimpleFilterProvider().addFilter("userFilter", filter);

            if(userAux.isPresent()) {
                Link self = linkTo(methodOn(UserController.class).getUser(userAux.get().getEmail())).withSelfRel();
                Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(User.class));
                MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(userAux.get());
                mappingJacksonValue.setFilters(filterProvider);

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .body(mappingJacksonValue);
            }

            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Modify user
    @PatchMapping(
            path = "{email}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#email == principal")
    ResponseEntity<MappingJacksonValue> modifyUser(
            @PathVariable("email") String email,
            @RequestBody List<Map<String, Object>> updates
    ) {
        try {
            if(users.get(email).isEmpty()){ return ResponseEntity.notFound().build(); }
            if(updates.isEmpty() || updates.stream().filter(stringObjectMap -> stringObjectMap.values().contains("/email")).count() > 0){ return ResponseEntity.status(422).build(); }

            Optional<User> userAux = users.modifyUser(email, updates);
            SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.serializeAllExcept("password");
            FilterProvider filterProvider = new SimpleFilterProvider().addFilter("userFilter", filter);

            if(userAux.isPresent()) {
                Link self = linkTo(methodOn(UserController.class).getUser(userAux.get().getEmail())).withSelfRel();
                Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(User.class));
                MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(userAux.get());
                mappingJacksonValue.setFilters(filterProvider);

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .body(mappingJacksonValue);
            }

            return ResponseEntity.notFound().build();
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
    @PreAuthorize("#email == principal")
    ResponseEntity<Object> deleteUser(@PathVariable("email") String email) {
        try{
            if(users.get(email).isEmpty()){ return ResponseEntity.notFound().build(); }
            users.deleteUser(email);
            Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(User.class));
            return ResponseEntity.noContent().header(HttpHeaders.LINK, all.toString()).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }



    //-----------------------ASSESSMENTS-----------------------

    //Get user's assessments
    @GetMapping(
            path = "{email}/assessments",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN') or #email == principal or @userService.areFriends(#email, principal)")
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

            Optional<Page<Assessment>> result = users.getAssessments(page, size, Sort.by(criteria), email);

            if(result.isPresent()) {
                Page<Assessment> data = result.get();
                Pageable metadata = data.getPageable();

                Link first = linkTo(
                        methodOn(UserController.class).getAssessments(metadata.first().getPageNumber(), size, sort, email)
                ).withRel(IanaLinkRelations.FIRST);
                Link last = linkTo(
                        methodOn(UserController.class).getAssessments(data.getTotalPages() - 1, size, sort, email)
                ).withRel(IanaLinkRelations.LAST);
                Link next = linkTo(
                        methodOn(UserController.class).getAssessments(metadata.next().getPageNumber(), size, sort, email)
                ).withRel(IanaLinkRelations.NEXT);
                Link previous = linkTo(
                        methodOn(UserController.class).getAssessments(metadata.previousOrFirst().getPageNumber(), size, sort, email)
                ).withRel(IanaLinkRelations.PREVIOUS);

                Link user = linkTo(
                        methodOn(UserController.class).getUser(email)
                ).withRel(relationProvider.getItemResourceRelFor(User.class));

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, user.toString())
                        .header(HttpHeaders.LINK, first.toString())
                        .header(HttpHeaders.LINK, next.toString())
                        .header(HttpHeaders.LINK, previous.toString())
                        .header(HttpHeaders.LINK, last.toString())
                        .body(result.get());
            }

            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Modify assessment
    @PatchMapping(
            path = "{email}/assessments/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#email == principal")
    ResponseEntity<Assessment> modifyAssessment(
            @PathVariable("id") long id,
            @RequestBody List<Map<String, Object>> updates
    ) {
        try {
            if (assessments.get(id).isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            if (updates.isEmpty()
                    || updates.stream().filter(stringObjectMap -> stringObjectMap.values().contains("/id")).count() > 0
                    || updates.stream().filter(stringObjectMap -> stringObjectMap.values().contains("/email")).count() > 0
            ) {
                return ResponseEntity.status(422).build();
            }
            Optional<Assessment> assessment = assessments.modifyAssessment(id, updates);
            if (assessment.isPresent()) {
                Link self = linkTo(methodOn(AssessmentController.class).getAssessment(id)).withSelfRel();
                Link movieAssessments = linkTo(
                        methodOn(MovieController.class).getAssessments(0, 20, null, assessment.get().getMovie().getId())
                ).withRel(relationProvider.getItemResourceRelFor(Assessment.class));
                Link userAssessments = linkTo(
                        methodOn(UserController.class).getAssessments(0, 20, null, assessment.get().getUser().getEmail())
                ).withRel(relationProvider.getItemResourceRelFor(Assessment.class));
                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, movieAssessments.toString())
                        .header(HttpHeaders.LINK, userAssessments.toString())
                        .body(assessment.get());
            }

            return ResponseEntity.notFound().build();
        } catch (JsonPatchException e) {
            return ResponseEntity.status(400).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(422).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    //Update assessment
    @PutMapping(
            path = "{email}/assessments/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<Assessment> updateAssessment(@RequestBody @Valid Assessment assessment) {
        try {
            if(assessments.get(assessment.getId()).isEmpty()){return ResponseEntity.notFound().build();}
            Optional<Assessment> assessmentAux = assessments.updateAssessment(assessment);
            if (assessmentAux.isPresent()) {
                Link self = linkTo(methodOn(AssessmentController.class).getAssessment(assessmentAux.get().getId())).withSelfRel();
                Link movieAssessments = linkTo(
                        methodOn(MovieController.class).getAssessments(0, 20, null, assessmentAux.get().getMovie().getId())
                ).withRel(relationProvider.getItemResourceRelFor(Assessment.class));
                Link userAssessments = linkTo(
                        methodOn(UserController.class).getAssessments(0, 20, null, assessmentAux.get().getUser().getEmail())
                ).withRel(relationProvider.getItemResourceRelFor(Assessment.class));
                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, movieAssessments.toString())
                        .header(HttpHeaders.LINK, userAssessments.toString())
                        .body(assessmentAux.get());
            }

            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }


    //Delete one
    @DeleteMapping(
            path = "{email}/assessments/{id}"
    )
    @PreAuthorize("hasRole('ADMIN') or #email == principal")
    ResponseEntity<Object> delete(@PathVariable("id") long id) {
        try{
            if(assessments.get(id).isEmpty()){return ResponseEntity.notFound().build();}
            Optional<Assessment> assessment = assessments.get(id);

            if (assessment.isPresent()) {
                assessments.delete(id);
                Link movieAssessments = linkTo(
                        methodOn(MovieController.class).getAssessments(0, 20, null, assessment.get().getMovie().getId())
                ).withRel(relationProvider.getItemResourceRelFor(Assessment.class));
                Link userAssessments = linkTo(
                        methodOn(UserController.class).getAssessments(0, 20, null, assessment.get().getUser().getEmail())
                ).withRel(relationProvider.getItemResourceRelFor(Assessment.class));
                return ResponseEntity
                        .noContent()
                        .header(HttpHeaders.LINK, movieAssessments.toString())
                        .header(HttpHeaders.LINK, userAssessments.toString())
                        .build();
            }

            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }



    //----------------------FRIENDSHIPS------------------

    //Get friendships
    @GetMapping(
            path = "{email}/friends",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#email == principal")
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

            Optional<Page<Friendship>> result = users.getFriendships(page, size, Sort.by(criteria), email);

            if(result.isPresent()) {
                Page<Friendship> data = result.get();
                Pageable metadata = data.getPageable();

                Link self = linkTo(
                        methodOn(UserController.class).getFriendships(page, size, sort, email)
                ).withSelfRel();
                Link first = linkTo(
                        methodOn(UserController.class).getFriendships(metadata.first().getPageNumber(), size, sort, email)
                ).withRel(IanaLinkRelations.FIRST);
                Link last = linkTo(
                        methodOn(UserController.class).getFriendships(data.getTotalPages() - 1, size, sort, email)
                ).withRel(IanaLinkRelations.LAST);
                Link next = linkTo(
                        methodOn(UserController.class).getFriendships(metadata.next().getPageNumber(), size, sort, email)
                ).withRel(IanaLinkRelations.NEXT);
                Link previous = linkTo(
                        methodOn(UserController.class).getFriendships(metadata.previousOrFirst().getPageNumber(), size, sort, email)
                ).withRel(IanaLinkRelations.PREVIOUS);

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, first.toString())
                        .header(HttpHeaders.LINK, next.toString())
                        .header(HttpHeaders.LINK, previous.toString())
                        .header(HttpHeaders.LINK, last.toString())
                        .body(result.get());
            }

            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Get friendship
    @GetMapping(
            path = "{email}/friends/{friendEmail}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#email == principal or #friendEmail == principal")
    ResponseEntity<Friendship> getFriendship(
            @PathVariable("email") String email,
            @PathVariable("friendEmail") String friendEmail
    ) {
        try{
            Optional<Friendship> friendship = users.getFriendship(email, friendEmail);

            if(friendship.isPresent()) {
                Link self = linkTo(methodOn(UserController.class).getFriendship(email, friendEmail)).withSelfRel();
                Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(Friendship.class));
                Link user = linkTo(
                        methodOn(UserController.class).getUser(email)
                ).withRel(relationProvider.getItemResourceRelFor(User.class));
                Link friend = linkTo(
                        methodOn(UserController.class).getUser(friendEmail)
                ).withRel(relationProvider.getItemResourceRelFor(User.class));

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .header(HttpHeaders.LINK, user.toString())
                        .header(HttpHeaders.LINK, friend.toString())
                        .body(friendship.get());
            }

            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Add friend to user
    @PostMapping(
            path = "{email}/friends/{friendEmail}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("#email == principal")
    ResponseEntity<Friendship> addFriend(
            @PathVariable("email") String email,
            @PathVariable("friendEmail") String friendEmail
    ) {
        try {
            if(users.get(friendEmail).isEmpty() || users.get(email).isEmpty()){ return ResponseEntity.notFound().build(); }
            if(users.getFriendship(email, friendEmail).isPresent()){ return ResponseEntity.status(409).body(users.getFriendship(email, friendEmail).get()); }

            Optional<Friendship> userAux = users.addFriend(email, friendEmail);

            if(userAux.isPresent()) {
                Link self = linkTo(methodOn(UserController.class).getFriendship(email, friendEmail)).withSelfRel();
                Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(Friendship.class));

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .body(userAux.get());
            }

            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Modify friendship
    @PatchMapping(
            path = "{email}/friends/{friendEmail}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#friendEmail == principal")
    ResponseEntity<Friendship> modifyFriendship(
            @PathVariable("email") String email,
            @PathVariable("friendEmail") String friendEmail
    ) {
        try {
            if(users.getFriendship(email, friendEmail).isEmpty()){ return ResponseEntity.notFound().build(); }

            Optional<Friendship> friendship = users.modifyFriendship(email, friendEmail);

            if(friendship.isPresent()) {
                Link self = linkTo(methodOn(UserController.class).getFriendship(email, friendEmail)).withSelfRel();
                Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(Friendship.class));
                Link user = linkTo(
                        methodOn(UserController.class).getUser(email)
                ).withRel(relationProvider.getItemResourceRelFor(User.class));
                Link friend = linkTo(
                        methodOn(UserController.class).getUser(friendEmail)
                ).withRel(relationProvider.getItemResourceRelFor(User.class));

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .header(HttpHeaders.LINK, user.toString())
                        .header(HttpHeaders.LINK, friend.toString())
                        .body(friendship.get());
            }

            return ResponseEntity.notFound().build();
        }catch (JsonPatchException e){
            return ResponseEntity.status(400).build();
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(422).build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Delete friendship
    @DeleteMapping(path = "{email}/friends/{friendEmail}")
    @PreAuthorize("#email == principal or #friendEmail == principal")
    ResponseEntity<Friendship> deleteFriend(@PathVariable("email") String email, @PathVariable("friendEmail") String friend) {
        try{
            if(users.getFriendship(email, friend).isEmpty()){ return ResponseEntity.notFound().build(); }
            users.deleteFriend(email, friend);
            Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(Friendship.class));
            return ResponseEntity.noContent().header(HttpHeaders.LINK, all.toString()).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
