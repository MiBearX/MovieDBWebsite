import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class CastsSAXHandler extends DefaultHandler {
    private HashMap<String, Film> filmMap; // Map to store films by their FID
    private HashMap<String, Actor> actorMap; // Map to store actors by their stage name
    private StringBuilder data = new StringBuilder();
    private List<String> skippedEntries = new ArrayList<>();
    private String currentFid; // Currently processed FID
    private Film currentFilm; // Currently processed Film object

    public CastsSAXHandler(HashMap<String, Film> filmMap, HashMap<String, Actor> actorMap) {
        this.filmMap = filmMap;
        this.actorMap = actorMap;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        data.setLength(0); // Clear the character accumulator
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        data.append(new String(ch, start, length)); // Collect the characters within a tag
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String trimmedData = data.toString().trim(); // Trim any excess whitespace from the collected data
        switch(qName.toLowerCase()) {
            case "f":
                currentFilm = filmMap.get(trimmedData);
                if (currentFilm == null) {
                    skippedEntries.add("Film with FID '" + trimmedData + "' not found in the film map. Unable to assign actors to non-existent film.");
                }
                currentFid = trimmedData; // Store the current FID for potential further use
                break;
            case "a":
                if (currentFilm != null && actorMap.containsKey(trimmedData)) {
                    currentFilm.getStars().add(actorMap.get(trimmedData));
                } else if (currentFilm == null) {
                    skippedEntries.add("Skipped casting actor '" + trimmedData + "' because the film context was lost or never set.");
                } else if (!actorMap.containsKey(trimmedData)) {
                    skippedEntries.add("Actor '" + trimmedData + "' does not exist in actor map, thus cannot be assigned to film '" + currentFid + "'.");
                }
                break;
        }
        data.setLength(0); // Clear the buffer after processing the element
    }

    public List<String> getSkippedEntries() {
        return skippedEntries; // Provide access to the skipped entries
    }
}