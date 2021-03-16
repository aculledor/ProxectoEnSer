package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@Document(collection = "users")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    @Id
    @NotBlank(message = "The email can not be empty")
    @Email
    private String email;
    @NotBlank(message = "The name can not be empty")
    private String name;
    private String country;
    private String picture;
    private Date birthday;
    private List<User> friends;

    public User() {}
    public User(String email, String name, String country, String picture, Date birthday, List<User> friends) {
        this.email = email;
        this.name = name;
        this.country = country;
        this.picture = picture;
        this.birthday = birthday;
        this.friends = friends;
    }

    public String getEmail() {
        return email;
    }
    public String getName() {
        return name;
    }
    public String getCountry() {
        return country;
    }
    public String getPicture() {
        return picture;
    }
    public Date getBirthday() {
        return birthday;
    }
    public List<User> getFriends() {
        return friends;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }
    public User setName(String name) {
        this.name = name;
        return this;
    }
    public User setCountry(String country) {
        this.country = country;
        return this;
    }
    public User setPicture(String picture) {
        this.picture = picture;
        return this;
    }
    public User setBirthday(Date birthday) {
        this.birthday = birthday;
        return this;
    }
    public User setFriends(List<User> friends) {
        this.friends = friends;
        return this;
    }

    public User addFriend(User friend){
        User aux = new User().setEmail(friend.email).setName(friend.name);
        this.friends.add(aux);
        return this;
    }

    public User updateUser(User user){
        this.name = user.name;
        this.country = user.country;
        this.picture = user.picture;
        this.friends = user.friends;
        return this;
    }

    public User removeFriend(String friendId){
        User aux = this.friends.stream().filter(user ->
                user.getEmail().equals(friendId)
        ).findFirst().get();

        this.friends.remove(aux);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email) && Objects.equals(name, user.name) && Objects.equals(country, user.country) && Objects.equals(birthday, user.birthday);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, name, country, picture, birthday, friends);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", User.class.getSimpleName() + "[", "]")
                .add("email='" + email + "'")
                .add("name='" + name + "'")
                .add("country='" + country + "'")
                .add("picture='" + picture + "'")
                .add("birthday=" + birthday)
                .add("friends=" + friends)
                .toString();
    }
}
