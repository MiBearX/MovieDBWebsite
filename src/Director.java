import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class Director {
    String dirId;
    String dirstart;
    String dirname;
    String coverage;
    List<Film> films = new ArrayList<>();

    @Override
    public String toString() {
        return "Director{" +
                "dirId='" + dirId + '\'' +
                ", dirstart='" + dirstart + '\'' +
                ", dirname='" + dirname + '\'' +
                ", coverage='" + coverage + '\'' +
                ", films=" + films +
                '}';
    }
}

class Film {
    String fid;
    String title;
    String year;
    String genre;
    List<Actor> stars = new ArrayList<>();
    HashMap<String, String> actorBirthYears;
    String director;

    // Getters and setters
    public String getFid() { return fid; }
    public void setFid(String fid) { this.fid = fid; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public List<Actor> getStars() {
        return stars;
    }

    public void setStars(List<Actor> stars) {
        this.stars = stars;
    }

    @Override
    public String toString() {
        return "Film{" +
                "fid='" + fid + '\'' +
                ", title='" + title + '\'' +
                ", year='" + year + '\'' +
                ", genre='" + genre + '\'' +
                ", stars=" + stars +
                ", actorBirthYears=" + actorBirthYears +
                '}';
    }
}

class Actor {
    String stageName;
    String dob; // Date of Birth

    public Actor(String stageName, String dob) {
        this.stageName = stageName;
        this.dob = dob;
    }

    public String getStageName() {
        return stageName;
    }

    public String getDob() {
        return dob;
    }

    @Override
    public String toString() {
        return "Actor{" +
                "stageName='" + stageName + '\'' +
                ", dob='" + dob + '\'' +
                '}';
    }
}