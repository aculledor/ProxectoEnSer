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

    public Optional<Page<Assessment>> get(int page, int size, Sort sort) {
        Pageable request = PageRequest.of(page, size, sort);
        Page<Assessment> result = assessments.findAll(request);

        if(result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    public Optional<Assessment> get(String id) {
        return assessments.findById(id);
    }


    public void delete(String id) {
        assessments.deleteById(id);
    }
}
