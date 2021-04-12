package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Document(collection = "users")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class User {
    @Id
    @NotBlank(message = "The email field can not be empty")
    @Email
    private String email;
    private String name;
    private String country;
    private String picture;
    private Date birthday;
    @NotBlank(message = "The password field can not be empty")
    private String password;
    private List<String> roles;

    public User() {}

    public User(String email, String name, String country, String picture, Date birthday, String password, List<String> roles) {
        this.email = email;
        this.name = name;
        this.country = country;
        this.picture = picture;
        this.birthday = birthday;
        this.password = password;
        this.roles = roles;
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
    public String getPassword() {
        return password;
    }
    public List<String> getRoles() {
        return roles;
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
    public User setPassword(String password) {
        this.password = password;
        return this;
    }
    public User setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public User updateUser(User user){
        this.name = user.name;
        this.country = user.country;
        this.picture = user.picture;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (!email.equals(user.email)) return false;
        if (name != null ? !name.equals(user.name) : user.name != null) return false;
        if (country != null ? !country.equals(user.country) : user.country != null) return false;
        if (picture != null ? !picture.equals(user.picture) : user.picture != null) return false;
        if (birthday != null ? !birthday.equals(user.birthday) : user.birthday != null) return false;
        if (!password.equals(user.password)) return false;
        return roles.equals(user.roles);
    }

    @Override
    public int hashCode() {
        int result = email.hashCode();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        result = 31 * result + (picture != null ? picture.hashCode() : 0);
        result = 31 * result + (birthday != null ? birthday.hashCode() : 0);
        result = 31 * result + password.hashCode();
        result = 31 * result + roles.hashCode();
        return result;
    }
}
