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

    //Get all with options
    public Optional<Page<Film>> getAll(int page, int size, Sort sort, String title, List<String> keywords, List<String> genres, List<Crew> crew, List<Cast> cast, List<Producer> producers, Date releaseDate) {
        Pageable request = PageRequest.of(page, size, sort);

        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreCase().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

        Example<Film> filter = Example.of(new Film().setTitle(title).setKeywords(keywords).setGenres(genres).setCrew(crew).setCast(cast).setProducers(producers).setReleaseDate(releaseDate), matcher);

        Page<Film> result = movies.findAll(filter, request);

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
                .setMovie(new Film().setId(id)),matcher);
        Page<Assessment> result = assessments.findAll(filter, request);

        if(result.isEmpty())
            return Optional.empty();
        return Optional.of(result);
    }

    //Create one
    public Optional<Film> createMovie(Film film) {
        return Optional.of(movies.save(film));
    }

    //Update one
    public Optional<Film> updateMovie(Film film){
        Film filmEdit = movies.findById(film.getId()).get();
        filmEdit.updateMovie(film);
        return Optional.of(this.movies.save(filmEdit));
    }

    //Get one
    public Optional<Film> get(String id) {
        return movies.findById(id);
    }

    //Delete one
    public void delete(String id) {
        movies.deleteById(id);
    }
}
