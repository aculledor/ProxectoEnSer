package gal.usc.etse.grei.es.project.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(
        name = "Collection",
        description = "An auxiliary storage class for the Resources class"
)
public class Collection {
    @Id
    @NotBlank(message = "The name field can not be empty")
    @Schema(required = true, example = "resources")
    private String name;
    private List<Resource> resources = new ArrayList<>();

    public Collection() {
    }

    public Collection(String name, List<Resource> resources) {
        this.name = name;
        this.resources = resources;
    }

    public String getName() {
        return name;
    }

    public Collection setName(String name) {
        this.name = name;
        return this;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public Collection setResources(List<Resource> resources) {
        this.resources = resources;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Collection that = (Collection) o;
        return Objects.equals(name, that.name) && Objects.equals(resources, that.resources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, resources);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Collection.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("resources=" + resources)
                .toString();
    }
}
