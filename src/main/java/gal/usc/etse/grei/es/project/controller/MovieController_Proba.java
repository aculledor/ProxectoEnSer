package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.MovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("movies")
public class MovieController_Proba {
    private final MovieService movies;
    private final LinkRelationProvider relationProvider;

    @Autowired
    public MovieController_Proba(MovieService movies, LinkRelationProvider relationProvider) {
        this.movies = movies;
        this.relationProvider = relationProvider;
    }

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Page<Film>> get(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort
    ) {
        Optional<Page<Film>> result = movies.get(page, size, Sort.by((Sort.Order) sort));

        if(result.isPresent()) {
            Page<Film> data = result.get();
            Pageable metadata = data.getPageable();

            Link self = linkTo(
                    methodOn(MovieController_Proba.class).get(page, size, sort)
            ).withSelfRel();
            Link first = linkTo(
                    methodOn(MovieController_Proba.class).get(metadata.first().getPageNumber(), size, sort)
            ).withRel(IanaLinkRelations.FIRST);
            Link last = linkTo(
                    methodOn(MovieController_Proba.class).get(data.getTotalPages() - 1, size, sort)
            ).withRel(IanaLinkRelations.LAST);
            Link next = linkTo(
                    methodOn(MovieController_Proba.class).get(metadata.next().getPageNumber(), size, sort)
            ).withRel(IanaLinkRelations.NEXT);
            Link previous = linkTo(
                    methodOn(MovieController_Proba.class).get(metadata.previousOrFirst().getPageNumber(), size, sort)
            ).withRel(IanaLinkRelations.PREVIOUS);

            Link one = linkTo(
                    methodOn(MovieController.class).get(null)
            ).withRel(relationProvider.getItemResourceRelFor(Film.class));

            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, first.toString())
                    .header(HttpHeaders.LINK, last.toString())
                    .header(HttpHeaders.LINK, next.toString())
                    .header(HttpHeaders.LINK, previous.toString())
                    .header(HttpHeaders.LINK, one.toString())
                    .body(result.get());
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Film> get(@PathVariable("id") String id) {
        Optional<Film> movie = movies.get(id);

        if(movie.isPresent()) {
            Link self = linkTo(methodOn(MovieController.class).get(id)).withSelfRel();
            Link all = linkTo(MovieController.class).withRel(relationProvider.getCollectionResourceRelFor(Film.class));

            return ResponseEntity.ok()
                    .header(HttpHeaders.LINK, self.toString())
                    .header(HttpHeaders.LINK, all.toString())
                    .body(movie.get());
        }

        return ResponseEntity.notFound().build();
    }
}
