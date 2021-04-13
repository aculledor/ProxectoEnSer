package gal.usc.etse.grei.es.project.controller;

import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.service.AssessmentService;
import gal.usc.etse.grei.es.project.service.MovieService;
import gal.usc.etse.grei.es.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkRelationProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("assessments")
public class AssessmentController {
    private final AssessmentService assessments;
    private final MovieService movies;
    private final UserService users;
    private final LinkRelationProvider relationProvider;

    @Autowired
    public AssessmentController(AssessmentService assessments, MovieService movies, UserService users, LinkRelationProvider relationProvider) {
        this.assessments = assessments;
        this.movies = movies;
        this.users = users;
        this.relationProvider = relationProvider;
    }

    //Get one
    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    ResponseEntity<Assessment> getAssessment(@PathVariable("id") long id) {
        try{
            Optional<Assessment> assessment = assessments.get(id);
            if(assessment.isPresent()) {
                Link self = linkTo(methodOn(AssessmentController.class).getAssessment(id)).withSelfRel();
                return ResponseEntity.ok().header(HttpHeaders.LINK, self.toString()).body(assessment.get());
            }
            return ResponseEntity.notFound().build();
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

            Optional<Assessment> result = assessments.post(assessment);

            if(result.isPresent()) {

                Link movie = linkTo(
                        methodOn(MovieController.class).getMovie(assessment.getMovie().getId())
                ).withRel(relationProvider.getItemResourceRelFor(Film.class));
                Link movieAssessments = linkTo(
                        methodOn(MovieController.class).getAssessments(0, 20, null, assessment.getMovie().getId())
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

    //Modify assessment
    @PatchMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
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

        //Delete one
    @DeleteMapping(
            path = "{id}"
    )
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

    //Update assessment
    @PutMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
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
}
