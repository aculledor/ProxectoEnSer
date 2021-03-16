package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.repository.AssessmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
    public Optional<Assessment> get(String id) {
        return assessments.findById(id);
    }

    //Create one
    public Optional<Assessment> post(Assessment assessment) {
        return Optional.of(assessments.save(assessment));
    }

    //Update one
    public Optional<Assessment> patch(Assessment asses){
        Assessment assesEdit = assessments.findById(asses.getId()).get();
        assesEdit.updateAssessment(asses);
        return Optional.of(this.assessments.save(assesEdit));
    }

    //Delete one
    public void delete(String id) {
        assessments.deleteById(id);
    }
}
