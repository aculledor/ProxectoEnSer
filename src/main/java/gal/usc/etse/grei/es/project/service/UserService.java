package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class UserService {
    private final UserRepository users;

    @Autowired
    public UserService(UserRepository users) {
        this.users = users;
    }

    //Get all
    public Optional<Page<User>> get(int page, int size, Sort sort, String email, String name) {
        Pageable request = PageRequest.of(page, size, sort);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<User> filter = Example.of(new User().setEmail(email).setName(name), matcher);
        Page<User> result = users.findAll(filter, request);

        if(result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
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
    public Optional<User> patch(User user){
        User userEdit = users.findById(user.getEmail()).get();
        userEdit.updateUser(user);
        return Optional.of(this.users.save(userEdit));
    }

    //Add friend
    public Optional<User> patch(String mail, User friend){
        User userEdit = users.findById(mail).get();
        userEdit.addFriend(friend);
        return Optional.of(this.users.save(userEdit));
    }

    //Delete one
    public void delete(String email) {
        users.deleteById(email);
    }

    //Delete friend
    public Optional<User> delete(String email, String friend) {
        Optional<User> userEdit = users.findById(email);

        if(userEdit.isEmpty())
            return Optional.empty();

        return Optional.of(this.users.save(userEdit.get().removeFriend(friend)));
    }
}
