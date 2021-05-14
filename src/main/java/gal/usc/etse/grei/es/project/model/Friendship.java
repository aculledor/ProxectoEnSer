package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.Objects;
import java.util.StringJoiner;

@Document(collection = "friendship")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "Friendship",
        description = "A complete friendship between users representation with an auto increment id"
)
public class Friendship {
    @Transient
    public static final String SEQUENCE_NAME = "friendship_sequence";
    @Id
    public long id;
    @NotBlank(message = "The friend field can not be empty")
    @Schema(required = true)
    private String user;
    @NotBlank(message = "The friend field can not be empty")
    @Schema(required = true, example = "test@test.com")
    private String friend;
    private String userPicture;
    @Schema(example = "false")
    private Boolean confirmed;
    private Date since;

    public Friendship() {
    }

    public Friendship(String user, String friend, String userPicture, Boolean confirmed, Date since) {
        this.user = user;
        this.friend = friend;
        this.userPicture = userPicture;
        this.confirmed = confirmed;
        this.since = since;
    }

    public long getId() {
        return id;
    }
    public String getUser() {
        return user;
    }
    public String getFriend() {
        return friend;
    }
    public String getUserPicture() {
        return userPicture;
    }
    public Boolean getConfirmed() {
        return confirmed;
    }
    public Date getSince() {
        return since;
    }

    public Friendship setId(long id) {
        this.id = id;
        return this;
    }
    public Friendship setUser(String user) {
        this.user = user;
        return this;
    }
    public Friendship setFriend(String friend) {
        this.friend = friend;
        return this;
    }
    public Friendship setUserPicture(String userPicture) {
        this.userPicture = userPicture;
        return this;
    }
    public Friendship setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
        return this;
    }
    public Friendship setSince(Date since) {
        this.since = since;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Friendship that = (Friendship) o;

        if (id != that.id) return false;
        if (!user.equals(that.user)) return false;
        if (!friend.equals(that.friend)) return false;
        if (userPicture != null ? !userPicture.equals(that.userPicture) : that.userPicture != null) return false;
        if (!confirmed.equals(that.confirmed)) return false;
        return since.equals(that.since);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + user.hashCode();
        result = 31 * result + friend.hashCode();
        result = 31 * result + (userPicture != null ? userPicture.hashCode() : 0);
        result = 31 * result + confirmed.hashCode();
        result = 31 * result + since.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Friendship{" +
                "id=" + id +
                ", user='" + user + '\'' +
                ", friend='" + friend + '\'' +
                ", userPicture='" + userPicture + '\'' +
                ", confirmed=" + confirmed +
                ", since=" + since +
                '}';
    }
}
