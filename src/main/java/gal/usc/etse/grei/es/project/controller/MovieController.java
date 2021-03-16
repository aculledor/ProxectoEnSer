package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.Movie;
import gal.usc.etse.grei.es.project.model.User;
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

    //Get all movies
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    ) ResponseEntity<Page<Movie>> get(
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

        return ResponseEntity.of(movies.get(page, size, Sort.by(criteria)));
    }

//    //Get all movies
//    @GetMapping(
//            produces = MediaType.APPLICATION_JSON_VALUE
//    ) ResponseEntity<Page<Movie>> get(
//            @RequestParam(name = "page", defaultValue = "0") int page,
//            @RequestParam(name = "size", defaultValue = "20") int size,
//            @RequestParam(name = "sort", defaultValue = "") List<String> sort,
//            @RequestParam(name = "title", defaultValue = "") String title,
//            @RequestParam(name = "keyword", defaultValue = "") String keyword,
//            @RequestParam(name = "genre", defaultValue = "") String genre,
//            @RequestParam(name = "credits", defaultValue = "") String credits,
//            @RequestParam(name = "date", defaultValue = "") String releaseDate
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
//        return ResponseEntity.of(movies.get(page, size, Sort.by(criteria), title, keyword, genre, credits, releaseDate));
//    }

    //Get one movie
    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Movie> get(@PathVariable("id") String id) {
        return ResponseEntity.of(movies.get(id));
    }

    //Create movie
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Movie> post(@RequestBody @Valid Movie movie) {
        try {
            if(movies.get(movie.getId()).isPresent()){
                return ResponseEntity.status(409).build();
            }
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.of(movies.post(movie));
    }

    //Modify movie
    @PatchMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Movie> patch(@RequestBody @Valid Movie movie) {
        try {
            if(movies.get(movie.getId()).isEmpty()){
                return ResponseEntity.notFound().build();
            }
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.of(movies.patch(movie));
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
