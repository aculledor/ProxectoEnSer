package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.User;
import gal.usc.etse.grei.es.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class UserService {
    private final UserRepository users;

    @Autowired
    public UserService(UserRepository users) {
        this.users = users;
    }

    public Optional<Page<User>> get(int page, int size, Sort sort) {
        Pageable request = PageRequest.of(page, size, sort);
        Page<User> result = users.findAll(request);

        if(result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    public Optional<User> get(String email) {
        return users.findById(email);
    }

    public Optional<User> post(User user) {
        return Optional.of(users.save(user));
    }

    public Optional<User> patch(String mail, User friend){
        User userEdit = users.findById(mail).get();
        userEdit.addFriend(friend);
        return Optional.of(this.users.save(userEdit));
    }

    public Optional<User> patch(User user){
        User userEdit = users.findById(user.getEmail()).get();
        userEdit.updateUser(user);
        return Optional.of(this.users.save(userEdit));
    }

    public void delete(String email) {
        users.deleteById(email);
    }

    public Optional<User> delete(String email, String friend) {
        Optional<User> userEdit = users.findById(email);

        if(userEdit.isEmpty())
            return Optional.empty();

        return Optional.of(this.users.save(userEdit.get().removeFriend(friend)));
    }
}
