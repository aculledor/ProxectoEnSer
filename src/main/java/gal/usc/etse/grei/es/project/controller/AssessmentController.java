package gal.usc.etse.grei.es.project.controller;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.service.AssessmentService;
import gal.usc.etse.grei.es.project.service.MovieService;
import gal.usc.etse.grei.es.project.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@Tag(name = "Assessments API", description = "Assessments related operations")
@RequestMapping("assessments")
@SecurityRequirement(name = "JWT")
public class AssessmentController {
    private final AssessmentService assessments;

    @Autowired
    public AssessmentController(AssessmentService assessments) {
        this.assessments = assessments;
    }

    //Get one
    @GetMapping(
            path = "{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("isAuthenticated()")
    @Operation(
            operationId = "getAssessment",
            summary = "Get a single assessment",
            description = "Get the details for a given assessment by just knowing the id"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "The assessment details",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Assessment.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(schema = @Schema(implementation = Void.class))
            )
    })
    ResponseEntity<MappingJacksonValue> getAssessment(@PathVariable("id") long id) {
        try{
            Optional<Assessment> assessment = assessments.get(id);
            if(assessment.isPresent()) {
                Link self = linkTo(methodOn(AssessmentController.class).getAssessment(id)).withSelfRel();
                MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(assessment.get());
                mappingJacksonValue.setFilters(new SimpleFilterProvider().addFilter("userFilter", SimpleBeanPropertyFilter.filterOutAllExcept("email")).addFilter("movieFilter", SimpleBeanPropertyFilter.filterOutAllExcept("id", "title")));
                return ResponseEntity.ok().header(HttpHeaders.LINK, self.toString()).body(mappingJacksonValue);
            }
            return ResponseEntity.notFound().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().build();
        }
    }

}
