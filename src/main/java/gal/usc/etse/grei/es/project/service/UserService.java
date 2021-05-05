package gal.usc.etse.grei.es.project.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.Friendship;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.AssessmentRepository;
import gal.usc.etse.grei.es.project.repository.FriendshipRepository;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {
    private final UserRepository users;
    private final AssessmentRepository assessments;
    private final FriendshipRepository friendships;
    private final PasswordEncoder encoder;

    @Autowired
    public UserService(UserRepository users, AssessmentRepository assessments, FriendshipRepository friendships, PasswordEncoder encoder) {
        this.users = users;
        this.assessments = assessments;
        this.friendships = friendships;
        this.encoder = encoder;
    }

    //Get all
    public Optional<Page<User>> getAll(int page, int size, Sort sort, String email, String name) {
        Pageable request = PageRequest.of(page, size, sort);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<User> filter = Example.of(new User().setEmail(email).setName(name), matcher);

        Page<User> result = users.findAll(filter, request);

        if(result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    //Get assessments
    public Optional<Page<Assessment>> getAssessments(int page, int size, Sort sort, String email) {
        Pageable request = PageRequest.of(page, size, sort);
        Page<Assessment> result = assessments.findAllByUserEmail(request, email);

        if(result.isEmpty())
            return Optional.empty();
        return Optional.of(result);
    }

    //Get one
    public Optional<User> get(String email) {
        return users.findById(email);
    }

    //Create one
    public Optional<User> post(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return Optional.of(users.insert(user));
    }

    //Update one
    public Optional<User> updateUser(User user){
        User userEdit = users.findById(user.getEmail()).get();
        userEdit.updateUser(user);
        return Optional.of(this.users.save(userEdit));
    }

    //Modify one
    public Optional<User> modifyUser(String email,  List<Map<String, Object>> updates) throws JsonPatchException {
        User userEdit = users.findById(email).get();
        PatchUtils aux = new PatchUtils(new ObjectMapper());
        return Optional.of(this.users.save(aux.patch(userEdit,updates)));
    }

    //Delete one
    public void deleteUser(String email) {
        users.deleteById(email);
    }

    //Get friends
    public Optional<Page<Friendship>> getFriendships(int page, int size, Sort sort, String user){
        Pageable request = PageRequest.of(page, size, sort);
        Page<Friendship> result = friendships.findByUserOrFriend(user, user, request);

        if(result.isEmpty())
            return Optional.empty();
        return Optional.of(result);
    }

    //Get friendship
    public Optional<Friendship> getFriendship(String email, String friend){
        Optional<Friendship> aux = friendships.findByUserAndFriend(email, friend);
        if(aux.isEmpty()){aux = friendships.findByUserAndFriend(friend, email);}
        return aux;
    }

    //Are friends
    public Boolean areFriends(String email, String friend) {
        Optional<Friendship> aux = friendships.findByUserAndFriend(email, friend);
        if(aux.isEmpty()){aux = friendships.findByUserAndFriend(friend, email);}
        if(aux.isEmpty()){return false;}
        return true;
    }

    //Add friend
    public Optional<Friendship> addFriend(String email, String friend){
        Friendship friendship = new Friendship().setUser(email).setFriend(friend).setConfirmed(false);
        return Optional.of(this.friendships.insert(friendship));
    }

    //Delete friendship
    public void deleteFriend(String email, String friend) {
        this.friendships.delete(this.getFriendship(email, friend).get());
    }

    //Modify friendship
    public Optional<Friendship> modifyFriendship(String email, String friend) throws JsonPatchException {
        Friendship userFriendship = this.friendships.findByUserAndFriend(email, friend).get().setConfirmed(true).setSince(new Date());
        return Optional.of(this.friendships.save(userFriendship));
    }
}
