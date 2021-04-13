package gal.usc.etse.grei.es.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Film;
import gal.usc.etse.grei.es.project.repository.AssessmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AssessmentService {
    private final AssessmentRepository assessments;

    @Autowired
    public AssessmentService(AssessmentRepository assessments) {
        this.assessments = assessments;
    }

    //Get all
    public Optional<Page<Assessment>> get(int page, int size, Sort sort) {
        Pageable request = PageRequest.of(page, size, sort);
        Page<Assessment> result = assessments.findAll(request);

        if(result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    //Get one
    public Optional<Assessment> get(long id) {
        return assessments.findById(id+"");
    }

    //Create one
    public Optional<Assessment> post(Assessment assessment) {
        return Optional.of(assessments.insert(assessment));
    }

    //Update one
    public Optional<Assessment> updateAssessment(Assessment asses){
        Assessment assesEdit = assessments.findById(asses.getId()+"").get();
        assesEdit.updateAssessment(asses);
        return Optional.of(this.assessments.save(assesEdit));
    }

    //Modify one
    public Optional<Assessment> modifyAssessment(long id, List<Map<String, Object>> updates) throws JsonPatchException {
        Assessment assessmentEdit = assessments.findById(id+"").get();
        PatchUtils aux = new PatchUtils(new ObjectMapper());
        return Optional.of(this.assessments.save(aux.patch(assessmentEdit, updates)));
    }

    //Delete one
    public void delete(long id) {
        assessments.deleteById(id+"");
    }
}
