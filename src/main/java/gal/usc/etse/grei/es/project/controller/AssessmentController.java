package gal.usc.etse.grei.es.project.controller;

import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.AssessmentService;
import gal.usc.etse.grei.es.project.service.MovieService;
import gal.usc.etse.grei.es.project.service.UserService;
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
@RequestMapping("assessments")
public class AssessmentController {
    private final AssessmentService assessments;
    private final MovieService movies;
    private final UserService users;

    @Autowired
    public AssessmentController(AssessmentService assessments, MovieService movies, UserService users) {
        this.assessments = assessments;
        this.movies = movies;
        this.users = users;
    }

    //Get all
    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Page<Assessment>> get(
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

        return ResponseEntity.of(assessments.get(page, size, Sort.by(criteria)));
    }

    //Get one
    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Assessment> get(@PathVariable("id") String id) {
        return ResponseEntity.of(assessments.get(id));
    }

    //Create user
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Assessment> post(
            @RequestBody @Valid Assessment assessment
    ) {
        try {
            if(assessments.get(assessment.getId()).isPresent())
                return ResponseEntity.status(409).build();
            User auxUser = users.get(assessment.getUser().getEmail()).get();
            Film auxFilm = movies.get(assessment.getMovie().getId()).get();
            if(!auxUser.getName().equals(assessment.getUser().getName()) || !auxFilm.getTitle().equals(assessment.getMovie().getTitle()))
                return ResponseEntity.badRequest().build();
            return ResponseEntity.of(assessments.post(assessment));
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Modify assessment
    @PatchMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Assessment> patch(@RequestBody @Valid Assessment asses) {
        try {
            if(assessments.get(asses.getId()).isEmpty()){
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.of(assessments.patch(asses));
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }



    //Delete one
    @DeleteMapping(
            path = "{id}"
    )
    ResponseEntity<Object> delete(@PathVariable("id") String id) {
        try{
            assessments.delete(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
