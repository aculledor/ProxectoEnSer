package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.*;
import gal.usc.etse.grei.es.project.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("movies")
public class MovieController {
    private final MovieService movies;

    @Autowired
    public MovieController(MovieService movies) {
        this.movies = movies;
    }

//    //Get all movies
//    @GetMapping(
//            produces = MediaType.APPLICATION_JSON_VALUE
//    ) ResponseEntity<Page<Movie>> get(
//            @RequestParam(name = "page", defaultValue = "0") int page,
//            @RequestParam(name = "size", defaultValue = "20") int size,
//            @RequestParam(name = "sort", defaultValue = "") List<String> sort
//    ) {
//        List<Sort.Order> criteria = sort.stream().map(string -> {
//            if(string.startsWith("+")){
//                return Sort.Order.asc(string.substring(1));
//            } else if (string.startsWith("-")) {
//                return Sort.Order.desc(string.substring(1));
//            } else return null;
//        })
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//
//        return ResponseEntity.of(movies.get(page, size, Sort.by(criteria)));
//    }

    //Get all movies
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    ) ResponseEntity<Page<Film>> get(
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
        List<Sort.Order> criteria = sort.stream().map(string -> {
            if(string.startsWith("+")){
                return Sort.Order.asc(string.substring(1));
            } else if (string.startsWith("-")) {
                return Sort.Order.desc(string.substring(1));
            } else return null;
        })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        return ResponseEntity.of(movies.get(page, size, Sort.by(criteria), title, keywords, genres, crew, cast, producers, releaseDate));


    }

    //Get one movie
    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Film> get(@PathVariable("id") String id) {
        return ResponseEntity.of(movies.get(id));
    }

    //Get movie's assessments
    @GetMapping(
            path = "{id}/assessments",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Page<Assessment>> get(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
            @PathVariable("id") String id,
            @RequestBody @Valid Assessment assessment
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
        return ResponseEntity.of(movies.getAssessments(page, size, Sort.by(criteria), id));
    }

    //Create movie
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Film> post(@RequestBody @Valid Film film) {
        try {
            if(movies.get(film.getId()).isPresent()){
                return ResponseEntity.status(409).build();
            }
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.of(movies.post(film));
    }

    //Modify movie
    @PatchMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Film> patch(@RequestBody @Valid Film film) {
        try {
            if(movies.get(film.getId()).isEmpty()){
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.of(movies.patch(film));
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Delete one movie
    @DeleteMapping(
            path = "{id}"
    )
    ResponseEntity<Object> delete(@PathVariable("id") String id) {
        try{
            movies.delete(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
