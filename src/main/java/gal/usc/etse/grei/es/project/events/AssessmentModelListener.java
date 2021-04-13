package gal.usc.etse.grei.es.project.events;

import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.service.SequenceGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
public class AssessmentModelListener extends AbstractMongoEventListener<Assessment> {

    private SequenceGeneratorService sequenceGenerator;

    @Autowired
    public AssessmentModelListener(SequenceGeneratorService sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Assessment> event) {
        if (event.getSource().getId() < 1) {
            event.getSource().setId(sequenceGenerator.generateSequence(Assessment.SEQUENCE_NAME));
        }
    }
}
