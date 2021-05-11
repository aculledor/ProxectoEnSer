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
    @NotNull(message = "The movie title can not be null")
    private String movieTitle;
    @Schema(example = "I like it")
    private String comment;

    public Assessment() { }
    public Assessment(Integer rating, String user, String movie, String movieTitle, String comment) {
        this.id = id;
        this.rating = rating;
        this.user = user;
        this.movie = movie;
        this.movieTitle = movieTitle;
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
    public String getMovieTitle() {
        return movieTitle;
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
    public Assessment setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
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
        this.movieTitle = asses.movieTitle;
        this.comment = asses.comment;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Assessment that = (Assessment) o;

        if (id != that.id) return false;
        if (!rating.equals(that.rating)) return false;
        if (!user.equals(that.user)) return false;
        if (!movie.equals(that.movie)) return false;
        if (!movieTitle.equals(that.movieTitle)) return false;
        return comment != null ? comment.equals(that.comment) : that.comment == null;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + rating.hashCode();
        result = 31 * result + user.hashCode();
        result = 31 * result + movie.hashCode();
        result = 31 * result + movieTitle.hashCode();
        result = 31 * result + (comment != null ? comment.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Assessment{" +
                "id=" + id +
                ", rating=" + rating +
                ", user='" + user + '\'' +
                ", movie='" + movie + '\'' +
                ", movieTitle='" + movieTitle + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
