package gal.usc.etse.grei.es.project.events;

import gal.usc.etse.grei.es.project.model.Friendship;
import gal.usc.etse.grei.es.project.service.SequenceGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;

@Component
public class FriendshipModelListener  extends AbstractMongoEventListener<Friendship> {

    private SequenceGeneratorService sequenceGenerator;

    @Autowired
    public FriendshipModelListener(SequenceGeneratorService sequenceGenerator) {
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public void onBeforeConvert(BeforeConvertEvent<Friendship> event) {
        if (event.getSource().getId() < 1) {
            event.getSource().setId(sequenceGenerator.generateSequence(Friendship.SEQUENCE_NAME));
        }
    }
}
