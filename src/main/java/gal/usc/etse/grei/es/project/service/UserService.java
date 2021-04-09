package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.Assessment;
import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.AssessmentRepository;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class UserService {
    private final UserRepository users;
    private final AssessmentRepository assessments;

    @Autowired
    public UserService(UserRepository users, AssessmentRepository assessments) {
        this.users = users;
        this.assessments = assessments;
    }

    //Get all
    public Optional<Page<User>> get(int page, int size, Sort sort, String email, String name) {
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
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<Assessment> filter = Example.of(new Assessment()
                .setUser(new User().setEmail(email)),matcher);
        Page<Assessment> result = assessments.findAll(filter, request);

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
        return Optional.of(users.save(user));
    }

    //Update one
    public Optional<User> updateUser(User user){
        User userEdit = users.findById(user.getEmail()).get();
        userEdit.updateUser(user);
        return Optional.of(this.users.save(userEdit));
    }

    //Add friend
    public Optional<User> addFriend(String mail, User friend){
        User userEdit = users.findById(mail).get();
        userEdit.addFriend(friend);
        return Optional.of(this.users.save(userEdit));
    }

    //Delete one
    public void deleteUser(String email) {
        users.deleteById(email);
    }

    //Delete friend
    public Optional<User> deleteFriend(String email, String friend) {
        Optional<User> userEdit = users.findById(email);

        if(userEdit.isEmpty())
            return Optional.empty();

        return Optional.of(this.users.save(userEdit.get().removeFriend(friend)));
    }
}
