package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    //@Query("SELECT r.email, r.name, r.country, r.picture, r.birthday, r.roles  FROM Users r where r.name = :name")
    //Optional<User> findById(@Param("email") String email);
}
