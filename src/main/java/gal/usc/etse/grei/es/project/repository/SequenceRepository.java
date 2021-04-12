package gal.usc.etse.grei.es.project.repository;


import gal.usc.etse.grei.es.project.model.DatabaseSequence;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SequenceRepository extends MongoRepository<DatabaseSequence, String> {}
