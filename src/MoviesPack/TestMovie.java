package MoviesPack;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import MoviesPack.MovieEnums.*;


public class TestMovie {
    Connection conn;
    public TestMovie() {
        conn = dbConnection.getConnection();
	}

    public List<Movie> readMovieData() throws IOException {
        List <Movie> movies = new ArrayList<Movie>();
        
        
        Path path = Paths.get("src/MoviesPack/movies.txt");
        List<String> lines = Files.readAllLines(path);
		for(String line: lines){
		   try{ 
                String[] splitText = line.split(",");
                Integer movieId;
                String movieName;
                Category movieType;
                Language language;
                Date releaseDate;
                List<String> casting;
                Double rating;
                Double totalBusinessDone;
                if (splitText.length != 8) {
                    try {
                        throw new Exception("Check for number of fields provided");
                    } catch (Exception e) {
                        
                        e.printStackTrace();
                    }
                }
                movieId = Integer.parseInt(splitText[0]);
                movieName = splitText[1];
                movieType = parseMovieType(splitText[2]);
                language = parseLanguage(splitText[3]);
                releaseDate = parseDate(splitText[4]);
                casting = parseCasting(splitText[5]);
                rating = Double.parseDouble(splitText[6]);
                totalBusinessDone = Double.parseDouble(splitText[7]);
                movies.add(new Movie(movieId, movieName, movieType, language, releaseDate, casting, rating, totalBusinessDone));
            }
            catch (Exception e){
                System.out.println(e);
            }
		}
        return movies;
    }

    private Date parseDate(String dateToParse) throws ParseException {
        java.util.Date date1=new SimpleDateFormat("dd/MM/yyyy").parse(dateToParse);  
        Date sqlDate = new Date(date1.getTime());
        return sqlDate;
        
    }
    
    private List<String> parseCasting(String casting) {
		List <String> castList = new ArrayList<> ();
		String[] castingTokens = casting.split(":");
		
		for (String token : castingTokens) {
			castList.add(token);
		}
		
		return castList;
	}

    private String deParseCasting(List <String> casting) {
		String castStr = String.join(":", casting);
		return castStr;
	} 


    private Language parseLanguage(String language) {
		Language languageToReturn = null;
		
		for (Language lang : Language.values()) {
			if (lang.getLanguage().equals(language) || lang.name().equals(language)) {
				languageToReturn = lang;
			}
		}
		
		return languageToReturn;
	}

    private Category parseMovieType(String movieType) {
		String movieTypeToCmp = movieType.toUpperCase();
		Category catMovieType = null;
		
		for (Category cat : Category.values()) {
			if (cat.name().equals(movieTypeToCmp)) {
				catMovieType = cat;
				break;
			}
		}
		
		return catMovieType;
	}
    
    
    public void populate(List <Movie> movies) {
		
		PreparedStatement prepStmt = null;
		
		try {
			int rowsInserted = 0;
			String query = "insert into movieDetails values (?,?,?,?,?,?,?,?)";
			prepStmt = conn.prepareStatement(query);
			
			for(Movie movie : movies) {
				prepStmt.setInt(1, movie.getMovieId());
                prepStmt.setString(2, movie.getMovieName());
                prepStmt.setString(3, movie.getMovieType().name());
                prepStmt.setString(4, movie.getLanguage().name());
                prepStmt.setDate(5, movie.getReleaseDate());
                prepStmt.setString(6, deParseCasting(movie.getCasting()));
                prepStmt.setDouble(7, movie.getRating());
                prepStmt.setDouble(8, movie.getTotalBusinessDone());
				
				rowsInserted += prepStmt.executeUpdate();
			}
			
			System.out.println("Inserted " + rowsInserted + " movies in the database.");
									
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    public Movie addMovie(String movie, List<Movie> movies) {
		Movie mm;
        try{ 
            String[] splitText = movie.split(",");
            Integer movieId;
            String movieName;
            Category movieType;
            Language language;
            Date releaseDate;
            List<String> casting;
            Double rating;
            Double totalBusinessDone;
            if (splitText.length != 8) {
                try {
                    throw new Exception("Check for number of fields provided");
                } catch (Exception e) {
                    
                    e.printStackTrace();
                }
            }
            movieId = Integer.parseInt(splitText[0]);
            movieName = splitText[1];
            movieType = parseMovieType(splitText[2]);
            language = parseLanguage(splitText[3]);
            releaseDate = parseDate(splitText[4]);
            casting = parseCasting(splitText[5]);
            rating = Double.parseDouble(splitText[6]);
            totalBusinessDone = Double.parseDouble(splitText[7]);
            System.out.println("Adding movie - " + movieName);
            mm= new Movie(movieId, movieName, movieType, language, releaseDate, casting, rating, totalBusinessDone);
            movies.add(mm);
			return mm;
        }
        catch (Exception e){
            System.out.println(e);
        }
		return new Movie(null, movie, null, null, null, null, null, null);
       
	}
	

    public void serializeMovies(List <Movie> movies, String fileName) {
		File file = new File(fileName);
		
		try (FileOutputStream fos = new FileOutputStream(file);
				ObjectOutputStream out = new ObjectOutputStream(fos)){
			
			out.writeObject(movies);
			
			System.out.println("Serialized movies list");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public List<Movie> deserializeMovies(String fileName) {
		File file = new File(fileName);
		List<Movie> movies = new ArrayList<>();
		
		try(FileInputStream fis = new FileInputStream(file);
				ObjectInputStream in = new ObjectInputStream(fis)) {
			
			movies = (ArrayList<Movie>) in.readObject();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return movies;
	}
	
    private List<Movie> convertResultSetToMovieList(ResultSet rs) {
		List <Movie> movies = new ArrayList<> ();
		
		Integer movieId;
		String movieName;
		Category movieType;
		Language language;
		Date releaseDate;
		List<String> casting;
		Double rating;
		Double totalBusinessDone;
		
		try {
			while(rs.next()) {
				movieId = rs.getInt(1);
				movieName = rs.getString(2);
				movieType = parseMovieType(rs.getString(3));
				language = parseLanguage(rs.getString(4));
				releaseDate = rs.getDate(5);
				casting = parseCasting(rs.getString(6));
				rating = rs.getDouble(7);
				totalBusinessDone = rs.getDouble(8);
				
				Movie movie = new Movie(movieId, movieName, movieType, language, releaseDate, casting, rating, totalBusinessDone);
				movies.add(movie);
			}
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
		
		return movies;
	}

    List <Movie> getMoviesReleasedInYear(int year) {
		List <Movie> movies = new ArrayList<>();
		String sql = "select * from movieDetails where to_char(releaseDate, 'yyyy') = ?";
		
		try {
			PreparedStatement prep = conn.prepareStatement(sql);
			
			prep.setInt(1, year);
			ResultSet rs = prep.executeQuery();
			
			movies = convertResultSetToMovieList(rs);
			
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
		
		return movies;
	}

    public List<Movie> getMoviesByActor(String ... actorNames) {
		List <Movie> movies = new ArrayList<>();
		int actorNameCount = actorNames.length;
		
		if (actorNameCount > 0) {
			String sql = "select * from movieDetails where casting like ?";
			
			for (int i = 1; i < actorNameCount; i++) {
				sql += " or casting like ?";
			}
			
			try {
				PreparedStatement prep = conn.prepareStatement(sql);
				
				for(int i = 0; i < actorNameCount; i++) {
					prep.setString(i+1, "%" + actorNames[i] + "%");
				}
				
				ResultSet rs = prep.executeQuery();
				movies = convertResultSetToMovieList(rs);
				
			} catch (SQLException e) {
				
				e.printStackTrace();
			}
		}
		
		return movies;
	}

    public void updateRatings(Movie movie, double rating, List<Movie> movies) {
		for(Movie curMovie : movies) {
			if (movie.equals(curMovie)) {
				curMovie.setRating(rating);
			}
		}
	}
	
	public void updateBusiness(Movie movie, double amount, List<Movie> movies) {
		for (Movie curMovie : movies) {
			if (movie.equals(curMovie)) {
				curMovie.setTotalBusinessDone(amount);
			}
		}
	}

    public void printMovieList(List <Movie> movies) {
		for (Movie movie : movies) {
			System.out.println(movie);
		}
		System.out.println();
	}
    
    public Map<Language, LinkedHashSet<Movie>> businessDone (double amount) {
		Map <Language, LinkedHashSet<Movie>> langToMovie = new LinkedHashMap<> ();
		
		String sql = "select * from movieDetails where totalbusinessdone > ? order by totalbusinessdone desc";
		try {
			PreparedStatement prep = conn.prepareStatement(sql);
			prep.setDouble(1, amount);
			ResultSet rs = prep.executeQuery();
			
			List<Movie> movies = convertResultSetToMovieList(rs);
			
			for (Movie movie : movies) {
				Language curLang = movie.getLanguage();
				
				if (langToMovie.containsKey(curLang)) {
					langToMovie.get(curLang).add(movie);
				} else {
					LinkedHashSet<Movie> curSet = new LinkedHashSet<>();
					curSet.add(movie);
					langToMovie.put(movie.getLanguage(), curSet);
				}
				
			}
		} catch (SQLException e) {
			
			e.printStackTrace();
		}
		
		return langToMovie;
	}
	
	public void displayLangToMovieMap(Map<Language, LinkedHashSet<Movie>> langToMovie) {
		for (Language lang : langToMovie.keySet()) {
			System.out.println(lang + " = ");
			for (Movie movie : langToMovie.get(lang)) {
				System.out.println(movie);
			}
			System.out.println("-");
		}
	}
    
    public static void main(String[] args) throws IOException {
		TestMovie md = new TestMovie();
		
		List <Movie> movies = md.readMovieData();
		
		
		System.out.println("Movie List:");
		
		System.out.println(movies);
		
		System.out.println("Populating movies into database.");
		md.populate(movies);
        
		
		
		System.out.println("Add new movie in the list");
		Scanner sc= new Scanner(System.in);
		System.out.println("Enter a Movie detail");
        String dummyMovie= sc.nextLine();
        Movie mm= md.addMovie(dummyMovie, movies);
		System.out.println();
		md.printMovieList(movies);
		
		String serialFileName = "serialFile";
		System.out.println("Serialize movie data to - " + serialFileName);
		md.serializeMovies(movies, serialFileName);
		
		System.out.println("Deserialize movie data from - " + serialFileName);
		List <Movie> deserialMovies = md.deserializeMovies(serialFileName);
		md.printMovieList(deserialMovies);
		
		int year = 2012;
		System.out.println("Find movies released in entered year - " + year);
		List <Movie> moviesByReleaseYear = md.getMoviesReleasedInYear(year);
		md.printMovieList(moviesByReleaseYear);
		
		String [] actors = new String [] {"Amir", "Kareena", "Lion"};
		System.out.println("List of movies by actors - " + String.join(" - ", actors));
		List<Movie> moviesByActor = md.getMoviesByActor(actors);
		md.printMovieList(moviesByActor);
		
		Double updatedRating = 4.4;
		System.out.println("Update rating of " + mm.getMovieName() + " to " + updatedRating);
		md.updateRatings(mm, updatedRating, movies);
		md.printMovieList(movies);
		
		Double updatedBusiness = 245.3;
		System.out.println("Updating total business done of " + mm.getMovieName() + " to " + updatedBusiness);
		md.updateBusiness(mm, updatedBusiness, movies);
		md.printMovieList(movies);
		
		Double businessThreshold = 24d;
		System.out.println("Display movies by language where business done is greater than - " + businessThreshold);
		Map<Language, LinkedHashSet<Movie>> langToMovie = md.businessDone(businessThreshold);
		md.displayLangToMovieMap(langToMovie);
	}

}
