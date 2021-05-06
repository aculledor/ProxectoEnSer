package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.StringJoiner;

@Document(collection = "assessments")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "Assessment",
        description = "A complete assessment representation with an auto increment id"
)
public class Assessment {
    @Transient
    public static final String SEQUENCE_NAME = "assessment_sequence";
    @Id
    private long id;
    @NotNull(message = "The rating can not be null")
    @Schema(required = true, minimum = "1", maximum = "10", example = "3")
    private Integer rating;
    @NotNull(message = "The rating can not be null")
    private String user;
    @NotNull(message = "The rating can not be null")
    private String movie;
    @Schema(example = "I like it")
    private String comment;

    public Assessment() { }
    public Assessment(Integer rating, String user, String movie, String comment) {
        this.id = id;
        this.rating = rating;
        this.user = user;
        this.movie = movie;
        this.comment = comment;
    }

    public long getId() {
        return id;
    }
    public Integer getRating() {
        return rating;
    }
    public String getUser() {
        return user;
    }
    public String getMovie() {
        return movie;
    }
    public String getComment() {
        return comment;
    }

    public Assessment setId(long id) {
        this.id = id;
        return this;
    }
    public Assessment setRating(Integer rating) {
        this.rating = rating;
        return this;
    }
    public Assessment setUser(String user) {
        this.user = user;
        return this;
    }
    public Assessment setMovie(String film) {
        this.movie = film;
        return this;
    }
    public Assessment setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public Assessment updateAssessment(Assessment asses){
        this.rating = asses.rating;
        this.user = asses.user;
        this.movie = asses.movie;
        this.comment = asses.comment;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Assessment that = (Assessment) o;
        return Objects.equals(id, that.id) && Objects.equals(rating, that.rating) && Objects.equals(user, that.user) && Objects.equals(movie, that.movie) && Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rating, user, movie, comment);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Assessment.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("rating=" + rating)
                .add("user=" + user)
                .add("movie=" + movie)
                .add("comment='" + comment + "'")
                .toString();
    }
}
