package Fabflix;

import java.util.ArrayList;

public class Movie {
	private String title;
	private String id;
	private String year;
	private String director;
	private ArrayList<String> genres;
	private ArrayList<String> stars;
	
	public Movie(String title, String id, String year, String director) {
		this.title = title;
		this.id = id;
		this.year = year;
		this.director = director;
		this.genres = new ArrayList<String>();
		this.stars = new ArrayList<String>();
	}
	
	public void addGenre(String genre) {
		genres.add(genre);
	}
	
	public void addStar(String star) {
		stars.add(star);
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getId() {
		return id;
	}
	
	public String getYear() {
		return year;
	}
	
	public String getDirector() {
		return director;
	}
	
	public ArrayList<String> getGenres() {
		return genres;
	}
	
	public ArrayList<String> getStars() {
		return stars;
	}
}