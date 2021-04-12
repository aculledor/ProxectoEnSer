package gal.usc.etse.grei.es.project.controller;

import com.github.fge.jsonpatch.JsonPatchException;
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
import java.util.Map;
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

            return ResponseEntity.of(movies.getAll(page, size, Sort.by(criteria), title, keywords, genres, crew, cast, producers, releaseDate));
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
            return ResponseEntity.of(movies.get(id));
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

            return ResponseEntity.of(movies.getAssessments(page, size, Sort.by(criteria), id));
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Create movie
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Film> createMovie(@RequestBody @Valid Film film) {
        try {
            if(movies.get(film.getId()).isPresent()){return ResponseEntity.status(409).body(movies.get(film.getId()).get());}
            return ResponseEntity.of(movies.createMovie(film));
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
            return ResponseEntity.noContent().build();
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
            return ResponseEntity.of(movies.updateMovie(film));
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
            return ResponseEntity.of(movies.modifyMovie(id, updates));
        }catch (JsonPatchException e){
            return ResponseEntity.status(400).build();
        }catch (IllegalArgumentException e){
            return ResponseEntity.status(422).build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
}
