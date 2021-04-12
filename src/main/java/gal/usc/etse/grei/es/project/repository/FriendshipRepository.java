package gal.usc.etse.grei.es.project.repository;

import gal.usc.etse.grei.es.project.model.Friendship;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FriendshipRepository extends MongoRepository<Friendship, String> {

    public Optional<Friendship> findByUserAndFriend(String user, String friend);

    public Page<Friendship> findByUserOrFriend(String user, String friend, Pageable request);

}
