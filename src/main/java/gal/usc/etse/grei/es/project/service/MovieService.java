package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.*;
import gal.usc.etse.grei.es.project.model.Date;
import gal.usc.etse.grei.es.project.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MovieService {
    private final MovieRepository movies;

    @Autowired
    public MovieService(MovieRepository movies) {
        this.movies = movies;
    }

    public Optional<Page<Movie>> get(int page, int size, Sort sort, String title, String keyword, String genre, String credits, String releaseDate) {
        Pageable request = PageRequest.of(page, size, sort);
        ArrayList<String> keywords = new ArrayList<>(); keywords.add(keyword);
        ArrayList<String> genres = new ArrayList<>(); genres.add(genre);
        ArrayList<Crew> crew = new ArrayList<>(); crew.add((Crew) new Crew().setName(credits));
        ArrayList<Cast> cast = new ArrayList<>(); cast.add((Cast) new Cast().setName(credits));
        Date date = new Date(releaseDate);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<Movie> filter = Example.of(new Movie().setTitle(title)
                .setKeywords(keywords).setGenres(genres).setCrew(crew).setCast(cast).setReleaseDate(date), matcher);
        Page<Movie> result = movies.findAll(filter, request);

        if(result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    //Get all
    public Optional<Page<Movie>> get(int page, int size, Sort sort) {
        Pageable request = PageRequest.of(page, size, sort);
        Page<Movie> result = movies.findAll(request);

        if(result.isEmpty())
            return Optional.empty();

        else return Optional.of(result);
    }

    //Create one
    public Optional<Movie> post(Movie movie) {
        return Optional.of(movies.save(movie));
    }

    //Update one
    public Optional<Movie> patch(Movie movie){
        Movie movieEdit = movies.findById(movie.getId()).get();
        movieEdit.updateMovie(movie);
        return Optional.of(this.movies.save(movieEdit));
    }

    //Get one
    public Optional<Movie> get(String id) {
        return movies.findById(id);
    }

    //Delete one
    public void delete(String id) {
        movies.deleteById(id);
    }
}
