package gal.usc.etse.grei.es.project.controller;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.*;
import gal.usc.etse.grei.es.project.service.AssessmentService;
import gal.usc.etse.grei.es.project.service.MovieService;
import gal.usc.etse.grei.es.project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@Tag(name = "Movie API", description = "Movie related operations")
@RequestMapping("movies")
@SecurityRequirement(name = "JWT")
public class MovieController {
    private final MovieService movies;
    private final LinkRelationProvider relationProvider;
    private final AssessmentService assessments;
    private final UserService users;

    @Autowired
    public MovieController(MovieService movies, LinkRelationProvider relationProvider, AssessmentService assessments, UserService users) {
        this.movies = movies;
        this.relationProvider = relationProvider;
        this.assessments = assessments;
        this.users = users;
    }

    //Get all movies
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    @Operation(
            operationId = "getAllMovies",
            summary = "Get all Movies",
            description = "Get all movies that conform to optional filters"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The movies list",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request",
                    content = @Content(schema = @Schema(implementation = Void.class))
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
            ),
    })
    ResponseEntity<MappingJacksonValue> getAllMovies(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @RequestParam(name = "title", defaultValue = "") String title,
            @RequestParam(name = "keywords", required = false) List<String> keywords,
            @RequestParam(name = "genres", required = false) List<String> genres,
            @RequestParam(name = "crew", required = false) List<Crew> crew,
            @RequestParam(name = "cast", required = false) List<Cast> cast,
            @RequestParam(name = "producers", required = false) List<Producer> producers,
            @RequestParam(name = "releaseDate", required = false) Date releaseDate
    ) {
        try{
            List<Sort.Order> criteria = sort.stream().map(string -> {
                if(string.startsWith("+")){
                    return Sort.Order.asc(string.substring(1));
                } else if (string.startsWith("-")) {
                    return Sort.Order.desc(string.substring(1));
                } else return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            Optional<Page<Film>> result = movies.getAll(page, size, Sort.by(criteria), title, keywords, genres, crew, cast, producers, releaseDate);
            SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.filterOutAllExcept("id","title","overview","genres","releaseDate","resources");
            FilterProvider filterProvider = new SimpleFilterProvider().addFilter("movieFilter", filter);

            if(result.isPresent()) {
                Page<Film> data = result.get();
                Pageable metadata = data.getPageable();

                Link self = linkTo(
                        methodOn(MovieController.class).getAllMovies(page, size, sort, title, keywords, genres, crew, cast, producers, releaseDate)
                ).withSelfRel();
                Link first = linkTo(
                        methodOn(MovieController.class).getAllMovies(metadata.first().getPageNumber(), size, sort, title, keywords, genres, crew, cast, producers, releaseDate)
                ).withRel(IanaLinkRelations.FIRST);
                Link last = linkTo(
                        methodOn(MovieController.class).getAllMovies(data.getTotalPages() - 1, size, sort, title, keywords, genres, crew, cast, producers, releaseDate)
                ).withRel(IanaLinkRelations.LAST);
                Link next = linkTo(
                        methodOn(MovieController.class).getAllMovies(metadata.next().getPageNumber(), size, sort, title, keywords, genres, crew, cast, producers, releaseDate)
                ).withRel(IanaLinkRelations.NEXT);
                Link previous = linkTo(
                        methodOn(MovieController.class).getAllMovies(metadata.previousOrFirst().getPageNumber(), size, sort, title, keywords, genres, crew, cast, producers, releaseDate)
                ).withRel(IanaLinkRelations.PREVIOUS);

                Link one = linkTo(
                        methodOn(MovieController.class).getMovie(null)
                ).withRel(relationProvider.getItemResourceRelFor(Film.class));
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

    //Get one movie
    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    @Operation(
            operationId = "getMovie",
            summary = "Get a single movie details",
            description = "Get the details for a given movie. Everyone can access the movie repository"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The movie details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Movie not found",
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
    ResponseEntity<MappingJacksonValue> getMovie(@PathVariable("id") String id){
        try{
            Optional<Film> movie = movies.get(id);
            SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.serializeAll();
            FilterProvider filterProvider = new SimpleFilterProvider().addFilter("movieFilter", filter);

            if(movie.isPresent()) {
                Link self = linkTo(methodOn(MovieController.class).getMovie(id)).withSelfRel();
                Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Film.class));
                MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(movie.get());
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

    //Create movie
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            operationId = "createMovie",
            summary = "Create a new Movie",
            description = "Creates a new movie based on the object received"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The created movie",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request, the item has to be a valid Movie object",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict with existing Movie",
                    content = @Content(schema = @Schema(implementation = Void.class))
            )
    })
    ResponseEntity<MappingJacksonValue> createMovie(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Movie to be created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            )
            @RequestBody @Valid Film film) {
        try {
            SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.serializeAll();
            FilterProvider filterProvider = new SimpleFilterProvider().addFilter("movieFilter", filter);
            if(movies.get(film.getId()).isPresent()){
                Link self = linkTo(methodOn(MovieController.class).getMovie(film.getId())).withSelfRel();
                Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Film.class));
                MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(movies.get(film.getId()).get());
                mappingJacksonValue.setFilters(filterProvider);
                return ResponseEntity.status(409)
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .body(mappingJacksonValue);
            }

            Optional<Film> movie = movies.createMovie(film);

            if(movie.isPresent()) {
                Link self = linkTo(methodOn(MovieController.class).getMovie(movie.get().getId())).withSelfRel();
                Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Film.class));
                MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(movie.get());
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

    //Delete one movie
    @DeleteMapping(
            path = "{id}"
    )
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            operationId = "deleteUser",
            summary = "Deletes an user",
            description = "Deletes the user data from the database"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User deleted",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = Void.class))
            )
    })
    ResponseEntity<Object> deleteMovie(@PathVariable("id") String id) {
        try{
            if(movies.get(id).isEmpty()){return ResponseEntity.notFound().build();}
            movies.delete(id);
            Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Film.class));
            return ResponseEntity.noContent().header(HttpHeaders.LINK, all.toString()).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    //Update movie
    @PutMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            operationId = "updateMovie",
            summary = "Replaces a movie",
            description = "Replace the movie data with the new data provided"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The replaced movie",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
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
    ResponseEntity<MappingJacksonValue> updateMovie(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Film to be updated",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
            )
            @RequestBody @Valid Film film) {
        try {
            if(movies.get(film.getId()).isEmpty()){return ResponseEntity.notFound().build();}

            Optional<Film> movie = movies.updateMovie(film);
            SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.serializeAll();
            FilterProvider filterProvider = new SimpleFilterProvider().addFilter("movieFilter", filter);

            if(movie.isPresent()) {
                Link self = linkTo(methodOn(MovieController.class).getMovie(movie.get().getId())).withSelfRel();
                Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Film.class));
                MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(movie.get());
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

    //Modify movie
    @PatchMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            operationId = "modifyFilm",
            summary = "Modifies a movie",
            description = "Modify the movie data following JSONPatch standards"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The replaced user",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Film.class)
                    )
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
    ResponseEntity<MappingJacksonValue> modifyFilm(
            @PathVariable("id") String id,
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
            if(movies.get(id).isEmpty()){ return ResponseEntity.notFound().build(); }
            if(updates.isEmpty() || updates.stream().filter(stringObjectMap -> stringObjectMap.values().contains("/id")).count() > 0){ return ResponseEntity.status(422).build(); }

            Optional<Film> movie = movies.modifyMovie(id, updates);
            SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.serializeAll();
            FilterProvider filterProvider = new SimpleFilterProvider().addFilter("movieFilter", filter);

            if(movie.isPresent()) {
                Link self = linkTo(methodOn(MovieController.class).getMovie(movie.get().getId())).withSelfRel();
                Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Film.class));
                MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(movie.get());
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


    //--------------ASSESSMENTS-------------


    //Get movie's assessments
    @GetMapping(
            path = "{id}/assessments",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    @Operation(
            operationId = "getAllAssessments",
            summary = "Get all Assessments",
            description = "Get all assessments that conform to optional filters"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The assessments list",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)
                    )
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
    ResponseEntity<Page<Assessment>> getAllAssessments(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @PathVariable("id") String id
    ) {
        try{
            if(movies.get(id).isEmpty()){return ResponseEntity.notFound().build();}
            List<Sort.Order> criteria = sort.stream().map(string -> {
                if(string.startsWith("+")){
                    return Sort.Order.asc(string.substring(1));
                } else if (string.startsWith("-")) {
                    return Sort.Order.desc(string.substring(1));
                } else return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            Optional<Page<Assessment>> result = movies.getAssessments(page, size, Sort.by(criteria), id);

            if(result.isPresent()) {
                Page<Assessment> data = result.get();
                Pageable metadata = data.getPageable();

                Link first = linkTo(
                        methodOn(MovieController.class).getAllAssessments(metadata.first().getPageNumber(), size, sort, id)
                ).withRel(IanaLinkRelations.FIRST);
                Link last = linkTo(
                        methodOn(MovieController.class).getAllAssessments(data.getTotalPages() - 1, size, sort, id)
                ).withRel(IanaLinkRelations.LAST);
                Link next = linkTo(
                        methodOn(MovieController.class).getAllAssessments(metadata.next().getPageNumber(), size, sort, id)
                ).withRel(IanaLinkRelations.NEXT);
                Link previous = linkTo(
                        methodOn(MovieController.class).getAllAssessments(metadata.previousOrFirst().getPageNumber(), size, sort, id)
                ).withRel(IanaLinkRelations.PREVIOUS);

                Link movie = linkTo(
                        methodOn(MovieController.class).getMovie(id)
                ).withRel(relationProvider.getItemResourceRelFor(Film.class));

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, movie.toString())
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

    //Create Assessment
    @PostMapping(
            path = "{id}/assessments",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    @Operation(
            operationId = "postAssessment",
            summary = "Create a new Assessment",
            description = "Creates a new assessment based on the object received"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The created assessment",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request, the item has to be a valid Assessment object",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict with existing Assessment",
                    content = @Content(schema = @Schema(implementation = Void.class))
            )
    })
    ResponseEntity<Assessment> postAssessment(
            @PathVariable("id") String id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Assessment to be created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Assessment.class)
                    )
            )
            @RequestBody @Valid Assessment assessment
    ) {
        try {
            boolean error = false;
            if(assessments.get(assessment.getId()).isPresent() || !assessment.getMovie().getId().equals(id)){return ResponseEntity.status(409).body(assessments.get(assessment.getId()).get());}
            if(users.get(assessment.getUser().getEmail()).isEmpty()){
                error = true;
                assessment.setUser(null);
            }
            if(movies.get(assessment.getMovie().getId()).isEmpty()){
                error = true;
                assessment.setMovie(null);
            }
            if(error){ResponseEntity.status(422).body(assessment);}

            Optional<Assessment> result = assessments.post(assessment);

            if(result.isPresent()) {

                Link movie = linkTo(
                        methodOn(MovieController.class).getMovie(assessment.getMovie().getId())
                ).withRel(relationProvider.getItemResourceRelFor(Film.class));
                Link movieAssessments = linkTo(
                        methodOn(MovieController.class).getAllAssessments(0, 20, null, assessment.getMovie().getId())
                ).withRel(relationProvider.getItemResourceRelFor(Assessment.class));

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, movie.toString())
                        .header(HttpHeaders.LINK, movieAssessments.toString())
                        .body(result.get());
            }

            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
}
