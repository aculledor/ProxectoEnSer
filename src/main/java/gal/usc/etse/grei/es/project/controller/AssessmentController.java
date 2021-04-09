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
    ResponseEntity<Page<Assessment>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "") List<String> sort
    ) {
        try{
            List<Sort.Order> criteria = sort.stream().map(string -> {
                if(string.startsWith("+")){
                    return Sort.Order.asc(string.substring(1));
                } else if (string.startsWith("-")) {
                    return Sort.Order.desc(string.substring(1));
                } else return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            return ResponseEntity.of(assessments.get(page, size, Sort.by(criteria)));
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Get one
    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Assessment> getAssessment(@PathVariable("id") String id) {
        try{
            return ResponseEntity.of(assessments.get(id));
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

    //Create Assessment
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Assessment> post(
            @RequestBody @Valid Assessment assessment
    ) {
        try {
            boolean error = false;
            if(assessments.get(assessment.getId()).isPresent()){return ResponseEntity.status(409).body(assessments.get(assessment.getId()).get());}
            if(users.get(assessment.getUser().getEmail()).isEmpty()){
                error = true;
                assessment.setUser(null);
            }
            if(movies.get(assessment.getMovie().getId()).isEmpty()){
                error = true;
                assessment.setMovie(null);
            }
            if(error){ResponseEntity.status(422).body(assessment);}
            return ResponseEntity.of(assessments.post(assessment));
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
            if(assessments.get(id).isEmpty()){return ResponseEntity.notFound().build();}
            assessments.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    //Update assessment
    @PutMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Assessment> updateAssessment(@RequestBody @Valid Assessment assessment) {
        try {
            if(assessments.get(assessment.getId()).isEmpty()){return ResponseEntity.notFound().build();}
            return ResponseEntity.of(assessments.updateAssessment(assessment));
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }
}
