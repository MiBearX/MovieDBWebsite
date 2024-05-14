public class Movie {
    private String id;
    private String title;
    private int year;
    private String directorId;

    // Constructor, getters, and setters
    public Movie() {}

    public Movie(String id, String title, int year, String directorId) {
        this.id = id;
        this.title = title;
        this.year = year;
        this.directorId = directorId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getDirectorId() {
        return directorId;
    }

    public void setDirectorId(String directorId) {
        this.directorId = directorId;
    }

    @Override
    public String toString() {
        return "Movie{" +                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", year=" + year +
                ", directorId='" + directorId + '\'' +
                '}';
    }
}