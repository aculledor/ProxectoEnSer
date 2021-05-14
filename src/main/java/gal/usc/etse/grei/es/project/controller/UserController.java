package gal.usc.etse.grei.es.project.controller;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.configuration.SerializationConfiguration;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Friendship;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.AssessmentService;
import gal.usc.etse.grei.es.project.service.MovieService;
import gal.usc.etse.grei.es.project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User API", description = "User related operations")
@SecurityRequirement(name = "JWT")
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
    @Operation(
            operationId = "getAllUsers",
            summary = "Get all Users",
            description = "Get all users that conform to optional filters"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The users list",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    ),
                    headers = {
                            @Header(
                                    name = "Self",
                                    description = "HATEOAS Link to itself",
                                    schema = @Schema(title = "Self", type = "/users")
                            ),
                            @Header(
                                    name = "First Page",
                                    description = "HATEOAS Link to the first page",
                                    schema = @Schema(title = "First Page", type = "/users")
                            ),
                            @Header(
                                    name = "Last Page",
                                    description = "HATEOAS Link to the last page",
                                    schema = @Schema(title = "Last Page", type = "/users")
                            ),
                            @Header(
                                    name = "Next Page",
                                    description = "HATEOAS Link to the next page",
                                    schema = @Schema(title = "Next Page", type = "/users")
                            ),
                            @Header(
                                    name = "Previous Page",
                                    description = "HATEOAS Link to the previous page",
                                    schema = @Schema(title = "Previous Page", type = "/users")
                            ),
                            @Header(
                                    name = "One user",
                                    description = "HATEOAS Link to one user",
                                    schema = @Schema(title = "One movie", type = "/users/{email}")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found",
                    content = @Content(schema = @Schema(implementation = Void.class))
            )
    })
    ResponseEntity<MappingJacksonValue> getAllUsers(
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
                        methodOn(UserController.class).getAllUsers(page, size, sort, email, name)
                ).withSelfRel();
                Link first = linkTo(
                        methodOn(UserController.class).getAllUsers(metadata.first().getPageNumber(), size, sort, email, name)
                ).withRel(IanaLinkRelations.FIRST);
                Link last = linkTo(
                        methodOn(UserController.class).getAllUsers(data.getTotalPages() - 1, size, sort, email, name)
                ).withRel(IanaLinkRelations.LAST);
                Link next = linkTo(
                        methodOn(UserController.class).getAllUsers(metadata.next().getPageNumber(), size, sort, email, name)
                ).withRel(IanaLinkRelations.NEXT);
                Link previous = linkTo(
                        methodOn(UserController.class).getAllUsers(metadata.previousOrFirst().getPageNumber(), size, sort, email, name)
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
    @Operation(
            operationId = "getUser",
            summary = "Get a single user details",
            description = "Get the details for a given user. To see the user details " +
                    "you must be the requested user, his friend, or have admin permissions."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The user details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    ),
                    headers = {
                            @Header(
                                    name = "Self",
                                    description = "HATEOAS Link to itself",
                                    schema = @Schema(title = "Self", type = "/users/{email}")
                            ),
                            @Header(
                                    name = "All",
                                    description = "HATEOAS Link to all users",
                                    schema = @Schema(title = "All", type = "/users")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
    })
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
    @Operation(
            operationId = "postUser",
            summary = "Create a new Users",
            description = "Creates a new user based on the object received"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The created user",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    ),
                    headers = {
                            @Header(
                                    name = "Self",
                                    description = "HATEOAS Link to itself",
                                    schema = @Schema(title = "Self", type = "/users/{email}")
                            ),
                            @Header(
                                    name = "All",
                                    description = "HATEOAS Link to all users",
                                    schema = @Schema(title = "All", type = "/users")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request, the item has to be a valid User object",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict with existing User",
                    content = @Content(schema = @Schema(implementation = Void.class))
            )
    })
    ResponseEntity<MappingJacksonValue> postUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User to be created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            )
            @RequestBody @Valid User user
    ) {
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
    @Operation(
            operationId = "updateUser",
            summary = "Replaces an user",
            description = "Replace the user data with the new data provided"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The replaced user",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    ),
                    headers = {
                            @Header(
                                    name = "Self",
                                    description = "HATEOAS Link to itself",
                                    schema = @Schema(title = "Self", type = "/users/{email}")
                            ),
                            @Header(
                                    name = "All",
                                    description = "HATEOAS Link to all users",
                                    schema = @Schema(title = "All", type = "/users")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found",
                    content = @Content(schema = @Schema(implementation = Void.class))
            )
    })
    ResponseEntity<MappingJacksonValue> updateUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User to be created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            )
            @RequestBody @Valid User user
    ) {
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
            consumes = "application/json-patch+json",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#email == principal")
    @Operation(
            operationId = "modifyUser",
            summary = "Modifies an user",
            description = "Modify the user data following JSONPatch standards"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The replaced user",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    ),
                    headers = {
                            @Header(
                                    name = "Self",
                                    description = "HATEOAS Link to itself",
                                    schema = @Schema(title = "Self", type = "/users/{email}")
                            ),
                            @Header(
                                    name = "All",
                                    description = "HATEOAS Link to all users",
                                    schema = @Schema(title = "All", type = "/users")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Unprocessable Entity",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
    })
    ResponseEntity<MappingJacksonValue> modifyUser(
            @PathVariable("email") String email,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Modifications to be applied",
                    content = @Content(
                            mediaType = "application/json-patch+json",
                            examples = @ExampleObject(
                                    value = "[{\"op\": \"replace\", \"path\": \"/foo\", \"value\": \"boo\"}]"
                            )
                    )
            )
            @RequestBody List<Map<String, Object>> updates
    ) {
        try {
            if(users.get(email).isEmpty()){ return ResponseEntity.notFound().build(); }
            if(updates.isEmpty() || updates.stream().filter(stringObjectMap -> stringObjectMap.values().contains("/email")).count() > 0){ return ResponseEntity.status(422).build(); }

            Optional<User> userAux = users.modifyUser(email, updates);
            SimpleBeanPropertyFilter filterToSend = SimpleBeanPropertyFilter.serializeAllExcept("password");
            FilterProvider filterProviderToSend = new SimpleFilterProvider().addFilter("userFilter", filterToSend);

            if(userAux.isPresent()) {
                Link self = linkTo(methodOn(UserController.class).getUser(userAux.get().getEmail())).withSelfRel();
                Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(User.class));
                MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(userAux.get());
                mappingJacksonValue.setFilters(filterProviderToSend);

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
    @Operation(
            operationId = "deleteUser",
            summary = "Deletes an user",
            description = "Deletes the user data from the database"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User deleted",
                    content = @Content(schema = @Schema(implementation = Void.class)),
                    headers = {
                            @Header(
                                    name = "All",
                                    description = "HATEOAS Link to all users",
                                    schema = @Schema(title = "All", type = "/users")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = Void.class))
            )
    })
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
    @Operation(
            operationId = "getUserAssessments",
            summary = "Gets user's assessments",
            description = "Get the user's assessments if you are the user, a friend or an admin"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The user's assessment list",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    ),
                    headers = {
                            @Header(
                                    name = "First Page",
                                    description = "HATEOAS Link to the first page",
                                    schema = @Schema(title = "First Page", type = "/users/{email}/assessments")
                            ),
                            @Header(
                                    name = "Last Page",
                                    description = "HATEOAS Link to the last page",
                                    schema = @Schema(title = "Last Page", type = "/users/{email}/assessments")
                            ),
                            @Header(
                                    name = "Next Page",
                                    description = "HATEOAS Link to the next page",
                                    schema = @Schema(title = "Next Page", type = "/users/{email}/assessments")
                            ),
                            @Header(
                                    name = "Previous Page",
                                    description = "HATEOAS Link to the previous page",
                                    schema = @Schema(title = "Previous Page", type = "/users/{email}/assessments")
                            ),
                            @Header(
                                    name = "User",
                                    description = "HATEOAS Link to the user",
                                    schema = @Schema(title = "One user", type = "/users/{email}")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
    })
    ResponseEntity<Page<Assessment>> getUserAssessments(
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
                        methodOn(UserController.class).getUserAssessments(metadata.first().getPageNumber(), size, sort, email)
                ).withRel(IanaLinkRelations.FIRST);
                Link last = linkTo(
                        methodOn(UserController.class).getUserAssessments(data.getTotalPages() - 1, size, sort, email)
                ).withRel(IanaLinkRelations.LAST);
                Link next = linkTo(
                        methodOn(UserController.class).getUserAssessments(metadata.next().getPageNumber(), size, sort, email)
                ).withRel(IanaLinkRelations.NEXT);
                Link previous = linkTo(
                        methodOn(UserController.class).getUserAssessments(metadata.previousOrFirst().getPageNumber(), size, sort, email)
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
    @Operation(
            operationId = "modifyAssessment",
            summary = "Modifies an assessment",
            description = "Modify the assessment data following JSONPatch standards"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The replaced assessment",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Assessment.class)
                    ),
                    headers = {
                            @Header(
                                    name = "Self",
                                    description = "HATEOAS Link to itself",
                                    schema = @Schema(title = "Self", type = "/users/{email}/assessments/{id}")
                            ),
                            @Header(
                                    name = "User Assessments",
                                    description = "HATEOAS Link to the user's assessments",
                                    schema = @Schema(title = "User's Assessments", type = "/users/{email}/assessments/{id}")
                            ),
                            @Header(
                                    name = "Movie Assessments",
                                    description = "HATEOAS Link to the movie's assessments",
                                    schema = @Schema(title = "Movie's Assessments", type = "/movies/{id}/assessments/{id}")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Unprocessable Entity",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
    })
    ResponseEntity<Assessment> modifyAssessment(
            @PathVariable("id") long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Modifications to be applied",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = "[{\"op\": \"replace\", \"path\": \"/foo\", \"value\": \"boo\"}]"
                            )
                    )
            )
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
                        methodOn(MovieController.class).getAllAssessments(0, 20, null, assessment.get().getMovie())
                ).withRel(relationProvider.getItemResourceRelFor(Assessment.class));
                Link userAssessments = linkTo(
                        methodOn(UserController.class).getUserAssessments(0, 20, null, assessment.get().getUser())
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
    @Operation(
            operationId = "updateAssessment",
            summary = "Replaces an assessment",
            description = "Replace the assessment data with the new data provided"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The replaced assessment",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Assessment.class)
                    ),
                    headers = {
                            @Header(
                                    name = "Self",
                                    description = "HATEOAS Link to itself",
                                    schema = @Schema(title = "Self", type = "/users/{email}/assessments/{id}")
                            ),
                            @Header(
                                    name = "User Assessments",
                                    description = "HATEOAS Link to the user's assessments",
                                    schema = @Schema(title = "User's Assessments", type = "/users/{email}/assessments/{id}")
                            ),
                            @Header(
                                    name = "Movie Assessments",
                                    description = "HATEOAS Link to the movie's assessments",
                                    schema = @Schema(title = "Movie's Assessments", type = "/movies/{id}/assessments/{id}")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found",
                    content = @Content(schema = @Schema(implementation = Void.class))
            )
    })
    ResponseEntity<Assessment> updateAssessment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Assessment to be updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Assessment.class)
                    )
            )
            @RequestBody @Valid Assessment assessment
    ) {
        try {
            if(assessments.get(assessment.getId()).isEmpty()){return ResponseEntity.notFound().build();}
            Optional<Assessment> assessmentAux = assessments.updateAssessment(assessment);
            if (assessmentAux.isPresent()) {
                Link self = linkTo(methodOn(AssessmentController.class).getAssessment(assessmentAux.get().getId())).withSelfRel();
                Link movieAssessments = linkTo(
                        methodOn(MovieController.class).getAllAssessments(0, 20, null, assessmentAux.get().getMovie())
                ).withRel(relationProvider.getItemResourceRelFor(Assessment.class));
                Link userAssessments = linkTo(
                        methodOn(UserController.class).getUserAssessments(0, 20, null, assessmentAux.get().getUser())
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


    //Delete assessment
    @DeleteMapping(
            path = "{email}/assessments/{id}"
    )
    @PreAuthorize("hasRole('ADMIN') or #email == principal")
    @Operation(
            operationId = "deleteAssessment",
            summary = "Deletes an assessment",
            description = "Deletes the assessment data from the database"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Assessment deleted",
                    content = @Content(schema = @Schema(implementation = Void.class)),
                    headers = {
                            @Header(
                                    name = "Self",
                                    description = "HATEOAS Link to itself",
                                    schema = @Schema(title = "Self", type = "/users/{email}/assessments/{id}")
                            ),
                            @Header(
                                    name = "User Assessments",
                                    description = "HATEOAS Link to the user's assessments",
                                    schema = @Schema(title = "User's Assessments", type = "/users/{email}/assessments/{id}")
                            ),
                            @Header(
                                    name = "Movie Assessments",
                                    description = "HATEOAS Link to the movie's assessments",
                                    schema = @Schema(title = "Movie's Assessments", type = "/movies/{id}/assessments/{id}")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = Void.class))
            )
    })
    ResponseEntity<Object> deleteAssessment(@PathVariable("id") long id) {
        try{
            if(assessments.get(id).isEmpty()){return ResponseEntity.notFound().build();}
            Optional<Assessment> assessment = assessments.get(id);

            if (assessment.isPresent()) {
                assessments.delete(id);
                Link movieAssessments = linkTo(
                        methodOn(MovieController.class).getAllAssessments(0, 20, null, assessment.get().getMovie())
                ).withRel(relationProvider.getItemResourceRelFor(Assessment.class));
                Link userAssessments = linkTo(
                        methodOn(UserController.class).getUserAssessments(0, 20, null, assessment.get().getUser())
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
    @Operation(
            operationId = "getFriendships",
            summary = "Get user friendships",
            description = "Get every user's friendship and page it by the given parameters"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The user's friendship list",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    ),
                    headers = {
                            @Header(
                                    name = "Self",
                                    description = "HATEOAS Link to itself",
                                    schema = @Schema(title = "Self", type = "/users/{email}/friends")
                            ),
                            @Header(
                                    name = "First Page",
                                    description = "HATEOAS Link to the first page",
                                    schema = @Schema(title = "First Page", type = "/users/{email}/friends")
                            ),
                            @Header(
                                    name = "Last Page",
                                    description = "HATEOAS Link to the last page",
                                    schema = @Schema(title = "Last Page", type = "/users/{email}/friends")
                            ),
                            @Header(
                                    name = "Next Page",
                                    description = "HATEOAS Link to the next page",
                                    schema = @Schema(title = "Next Page", type = "/users/{email}/friends")
                            ),
                            @Header(
                                    name = "Previous Page",
                                    description = "HATEOAS Link to the previous page",
                                    schema = @Schema(title = "Previous Page", type = "/users/{email}/friends")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
    })
    ResponseEntity<Page<Friendship>> getFriendships(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @PathVariable(name = "email") String email
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
    @Operation(
            operationId = "getFriendship",
            summary = "Get friendship between users",
            description = "Get the friendship between users if you are any of them"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The user details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Friendship.class)
                    ),
                    headers = {
                            @Header(
                                    name = "Self",
                                    description = "HATEOAS Link to itself",
                                    schema = @Schema(title = "Self", type = "/users/{email}/friends/{friendEmail}")
                            ),
                            @Header(
                                    name = "All",
                                    description = "HATEOAS Link to all the user's friends",
                                    schema = @Schema(title = "First Page", type = "/users/{email}/friends")
                            ),
                            @Header(
                                    name = "User",
                                    description = "HATEOAS Link to the user",
                                    schema = @Schema(title = "Last Page", type = "/users/{email}")
                            ),
                            @Header(
                                    name = "Friend",
                                    description = "HATEOAS Link to user's friend",
                                    schema = @Schema(title = "Next Page", type = "/users/{email}")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Bad token",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
    })
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
    @Operation(
            operationId = "addFriend",
            summary = "Create a friend request",
            description = "Creates a friends request and saves it in the database"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The created request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Friendship.class)
                    ),
                    headers = {
                            @Header(
                                    name = "Self",
                                    description = "HATEOAS Link to itself",
                                    schema = @Schema(title = "Self", type = "/users/{email}/friends/{friendEmail}")
                            ),
                            @Header(
                                    name = "All",
                                    description = "HATEOAS Link to all the user's friends",
                                    schema = @Schema(title = "First Page", type = "/users/{email}/friends")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = Void.class))
            )
    })
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

    //Accept friendship
    @PatchMapping(
            path = "{email}/friends/{friendEmail}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("#friendEmail == principal")
    @Operation(
            operationId = "modifyFriendship",
            summary = "Accepts a friendship request",
            description = "Accept the friendship request from another user"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The replaced user",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Friendship.class)
                    ),
                    headers = {
                            @Header(
                                    name = "Self",
                                    description = "HATEOAS Link to itself",
                                    schema = @Schema(title = "Self", type = "/users/{email}/friends/{friendEmail}")
                            ),
                            @Header(
                                    name = "All",
                                    description = "HATEOAS Link to all the user's friends",
                                    schema = @Schema(title = "First Page", type = "/users/{email}/friends")
                            ),
                            @Header(
                                    name = "User",
                                    description = "HATEOAS Link to the user",
                                    schema = @Schema(title = "Last Page", type = "/users/{email}")
                            ),
                            @Header(
                                    name = "Friend",
                                    description = "HATEOAS Link to user's friend",
                                    schema = @Schema(title = "Next Page", type = "/users/{email}")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = Void.class))
            )
    })
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
    @Operation(
            operationId = "deleteFriend",
            summary = "Deletes an friendship",
            description = "Deletes the friendship data from the database"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Friendship deleted",
                    content = @Content(schema = @Schema(implementation = Void.class)),
                    headers = {
                            @Header(
                                    name = "All",
                                    description = "HATEOAS Link to all the user's friends",
                                    schema = @Schema(title = "First Page", type = "/users/{email}/friends")
                            )
                    }
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = Void.class))
            )
    })
    ResponseEntity<Friendship> deleteFriend(
            @PathVariable("email") String email,
            @PathVariable("friendEmail") String friendEmail) {
        try{
            if(users.getFriendship(email, friendEmail).isEmpty()){ return ResponseEntity.notFound().build(); }
            users.deleteFriend(email, friendEmail);
            Link all = linkTo(UserController.class).withRel(relationProvider.getCollectionResourceRelFor(Friendship.class));
            return ResponseEntity.noContent().header(HttpHeaders.LINK, all.toString()).build();
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
