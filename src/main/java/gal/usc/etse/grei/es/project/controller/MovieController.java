package gal.usc.etse.grei.es.project.controller;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.*;
import gal.usc.etse.grei.es.project.service.MovieService;
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
@RequestMapping("movies")
public class MovieController {
    private final MovieService movies;
    private final LinkRelationProvider relationProvider;

    @Autowired
    public MovieController(MovieService movies, LinkRelationProvider relationProvider) {
        this.movies = movies;
        this.relationProvider = relationProvider;
    }

    //Get all movies
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    ) ResponseEntity<Page<Film>> getAll(
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

            if(result.isPresent()) {
                Page<Film> data = result.get();
                Pageable metadata = data.getPageable();

                Link self = linkTo(
                        methodOn(MovieController.class).getAll(page, size, sort, title, keywords, genres, crew, cast, producers, releaseDate)
                ).withSelfRel();
                Link first = linkTo(
                        methodOn(MovieController.class).getAll(metadata.first().getPageNumber(), size, sort, title, keywords, genres, crew, cast, producers, releaseDate)
                ).withRel(IanaLinkRelations.FIRST);
                Link last = linkTo(
                        methodOn(MovieController.class).getAll(data.getTotalPages() - 1, size, sort, title, keywords, genres, crew, cast, producers, releaseDate)
                ).withRel(IanaLinkRelations.LAST);
                Link next = linkTo(
                        methodOn(MovieController.class).getAll(metadata.next().getPageNumber(), size, sort, title, keywords, genres, crew, cast, producers, releaseDate)
                ).withRel(IanaLinkRelations.NEXT);
                Link previous = linkTo(
                        methodOn(MovieController.class).getAll(metadata.previousOrFirst().getPageNumber(), size, sort, title, keywords, genres, crew, cast, producers, releaseDate)
                ).withRel(IanaLinkRelations.PREVIOUS);

                Link one = linkTo(
                        methodOn(MovieController.class).getMovie(null)
                ).withRel(relationProvider.getItemResourceRelFor(Film.class));

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, first.toString())
                        .header(HttpHeaders.LINK, next.toString())
                        .header(HttpHeaders.LINK, previous.toString())
                        .header(HttpHeaders.LINK, last.toString())
                        .header(HttpHeaders.LINK, one.toString())
                        .body(result.get());
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
    ResponseEntity<Film> getMovie(@PathVariable("id") String id){
        try{
            Optional<Film> movie = movies.get(id);

            if(movie.isPresent()) {
                Link self = linkTo(methodOn(MovieController.class).getMovie(id)).withSelfRel();
                Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Film.class));

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .body(movie.get());
            }

            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Get movie's assessments
    @GetMapping(
            path = "{id}/assessments",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Page<Assessment>> getAssessments(
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
                        methodOn(MovieController.class).getAssessments(metadata.first().getPageNumber(), size, sort, id)
                ).withRel(IanaLinkRelations.FIRST);
                Link last = linkTo(
                        methodOn(MovieController.class).getAssessments(data.getTotalPages() - 1, size, sort, id)
                ).withRel(IanaLinkRelations.LAST);
                Link next = linkTo(
                        methodOn(MovieController.class).getAssessments(metadata.next().getPageNumber(), size, sort, id)
                ).withRel(IanaLinkRelations.NEXT);
                Link previous = linkTo(
                        methodOn(MovieController.class).getAssessments(metadata.previousOrFirst().getPageNumber(), size, sort, id)
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

    //Create movie
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Film> createMovie(@RequestBody @Valid Film film) {
        try {
            if(movies.get(film.getId()).isPresent()){
                Link self = linkTo(methodOn(MovieController.class).getMovie(film.getId())).withSelfRel();
                Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Film.class));
                return ResponseEntity.status(409)
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .body(movies.get(film.getId()).get());
            }

            Optional<Film> movie = movies.createMovie(film);

            if(movie.isPresent()) {
                Link self = linkTo(methodOn(MovieController.class).getMovie(movie.get().getId())).withSelfRel();
                Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Film.class));

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .body(movie.get());
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
    ResponseEntity<Film> updateMovie(@RequestBody @Valid Film film) {
        try {
            if(movies.get(film.getId()).isEmpty()){return ResponseEntity.notFound().build();}

            Optional<Film> movie = movies.updateMovie(film);

            if(movie.isPresent()) {
                Link self = linkTo(methodOn(MovieController.class).getMovie(movie.get().getId())).withSelfRel();
                Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Film.class));

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .body(movie.get());
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
    ResponseEntity<Film> modifyUser(
            @PathVariable("id") String id,
            @RequestBody List<Map<String, Object>> updates
    ) {
        try {
            if(movies.get(id).isEmpty()){ return ResponseEntity.notFound().build(); }
            if(updates.isEmpty() || updates.stream().filter(stringObjectMap -> stringObjectMap.values().contains("/id")).count() > 0){ return ResponseEntity.status(422).build(); }

            Optional<Film> movie = movies.modifyMovie(id, updates);

            if(movie.isPresent()) {
                Link self = linkTo(methodOn(MovieController.class).getMovie(movie.get().getId())).withSelfRel();
                Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Film.class));

                return ResponseEntity.ok()
                        .header(HttpHeaders.LINK, self.toString())
                        .header(HttpHeaders.LINK, all.toString())
                        .body(movie.get());
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
}
