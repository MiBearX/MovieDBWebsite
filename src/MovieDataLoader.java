import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class MovieDataLoader {

    private static HashMap<String, Film> filmMap = new HashMap<>();
    private static HashMap<String, Actor> actorMap = new HashMap<>();
    private static Set<String> allSkippedEntries = new HashSet<>();

    public static HashMap<String, Object> loadMovies() throws Exception {
        MainFilmsSAXHandler mainHandler = new MainFilmsSAXHandler(filmMap);
        ActorsSAXHandler actorsHandler = new ActorsSAXHandler(actorMap);
        CastsSAXHandler castsHandler = new CastsSAXHandler(filmMap, actorMap);

        // Setup and parse the main_example.xml
        XMLReader xmlReader1 = XMLReaderFactory.createXMLReader();
        xmlReader1.setContentHandler(mainHandler);
        xmlReader1.parse("/Users/michaelxiong/Documents/CS122B/cs122b-project1-api-example/mains243.xml");

        // Setup and parse the example.xml for actors
        XMLReader xmlReader2 = XMLReaderFactory.createXMLReader();
        xmlReader2.setContentHandler(actorsHandler);
        xmlReader2.parse("/Users/michaelxiong/Documents/CS122B/cs122b-project1-api-example/actors63.xml");

        // Setup and parse the casts_example.xml
        XMLReader xmlReader3 = XMLReaderFactory.createXMLReader();
        xmlReader3.setContentHandler(castsHandler);
        xmlReader3.parse("/Users/michaelxiong/Documents/CS122B/cs122b-project1-api-example/casts124.xml");

        // Aggregate skipped entries
        allSkippedEntries.addAll(mainHandler.getSkippedFilms());
        allSkippedEntries.addAll(actorsHandler.getSkippedEntries());
        allSkippedEntries.addAll(castsHandler.getSkippedEntries());

        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("films", filmMap);
        resultMap.put("actors", actorMap);
        resultMap.put("skippedEntries", new ArrayList<>(allSkippedEntries)); // Convert set back to list to maintain API consistency
        return resultMap;
    }

    // Method to return the list of all skipped entries
    public static List<String> getSkippedEntries() {
        return new ArrayList<>(allSkippedEntries); // Ensure it returns a list for external use
    }

    // Method to return the filmMap
    public static HashMap<String, Film> getFilmMap() {
        return filmMap;
    }
}