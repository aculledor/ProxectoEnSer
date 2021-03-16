package gal.usc.etse.grei.es.project.service;

import gal.usc.etse.grei.es.project.model.*;
import gal.usc.etse.grei.es.project.model.Date;
import gal.usc.etse.grei.es.project.repository.AssessmentRepository;
import gal.usc.etse.grei.es.project.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MovieService {
    private final MovieRepository movies;
    private final AssessmentRepository assessments;

    @Autowired
    public MovieService(MovieRepository movies, AssessmentRepository assessments) {
        this.movies = movies;
        this.assessments = assessments;
    }

    public Optional<Page<Movie>> get(int page, int size, Sort sort, String title, List<String> keywords, List<String> genres, List<Crew> crew, List<Cast> cast, List<Producer> producers, Date releaseDate) {
        Pageable request = PageRequest.of(page, size, sort);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Movie> filter = Example.of(new Movie().setTitle(title).setKeywords(keywords).setGenres(genres).setCrew(crew).setCast(cast).setProducers(producers).setReleaseDate(releaseDate), matcher);

        //Non funciona e non sei porqué
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

    //Get assessments
    public Optional<Page<Assessment>> getAssessments(int page, int size, Sort sort, String id) {
        Pageable request = PageRequest.of(page, size, sort);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<Assessment> filter = Example.of(new Assessment()
                .setMovie(new Movie().setId(id)),matcher);
        Page<Assessment> result = assessments.findAll(filter, request);

        if(result.isEmpty())
            return Optional.empty();
        return Optional.of(result);
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
