import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class MainFilmsSAXHandler extends DefaultHandler {
    private HashMap<String, Film> filmMap;
    private List<String> skippedFilms;  // List to track reasons for skipped films
    private StringBuilder data = new StringBuilder();
    private Film currentFilm;
    private String currentDirector;  // Temporary storage for director's name

    public MainFilmsSAXHandler(HashMap<String, Film> filmMap) {
        this.filmMap = filmMap;
        this.skippedFilms = new ArrayList<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        data.setLength(0);  // Clear the data buffer
        if (qName.equalsIgnoreCase("film")) {
            currentFilm = new Film();  // Initialize a new Film object when <film> start tag is found
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String content = data.toString().trim();
        switch (qName.toLowerCase()) {
            case "film":
                if (currentFilm != null) {
                    if (currentFilm.getFid() == null || currentFilm.getTitle() == null || currentFilm.getDirector() == null) {
                        skippedFilms.add("Skipped film with ID " + (currentFilm.getFid() != null ? currentFilm.getFid() : "undefined") +
                                " due to missing mandatory fields. Title: " + (currentFilm.getTitle() != null ? currentFilm.getTitle() : "Not provided") +
                                ", Director: " + (currentFilm.getDirector() != null ? currentFilm.getDirector() : "Not provided") + ".");
                    } else {
                        filmMap.put(currentFilm.getFid(), currentFilm);  // Store the film object in the map if all fields are present
                    }
                }
                currentFilm = null;  // Reset currentFilm for the next entry
                break;
            case "fid":
                if (currentFilm != null) currentFilm.setFid(content);
                break;
            case "t":
                if (currentFilm != null) currentFilm.setTitle(content);
                break;
            case "year":
                if (currentFilm != null) currentFilm.setYear(content);
                break;
            case "dirn":
                currentDirector = content;
                if (currentFilm != null) currentFilm.setDirector(currentDirector);
                break;
            case "cat":
                if (currentFilm != null) currentFilm.setGenre(content);
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        data.append(ch, start, length);  // Append the characters to the buffer
    }

    public List<String> getSkippedFilms() {
        return skippedFilms;  // Getter for the list of skipped films and reasons
    }
}