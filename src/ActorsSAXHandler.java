
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class ActorsSAXHandler extends DefaultHandler {
    private HashMap<String, Actor> actorsMap;
    private StringBuilder data = new StringBuilder();
    private List<String> skippedEntries = new ArrayList<>();
    private String currentStageName;
    private String currentDOB;

    public ActorsSAXHandler(HashMap<String, Actor> actorsMap) {
        this.actorsMap = actorsMap;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        data.setLength(0);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        data.append(new String(ch, start, length));
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName.toLowerCase()) {
            case "stagename":
                currentStageName = data.toString().trim();
                break;
            case "dob":
                currentDOB = data.toString().trim();
                break;
            case "actor":
                if (currentStageName == null || currentStageName.isEmpty()) {
                    skippedEntries.add("Skipped actor entry due to missing 'stagename'.");
                } else if (currentDOB == null || currentDOB.isEmpty()) {
                    skippedEntries.add("Skipped actor '" + currentStageName + "' due to missing 'dob'.");
                } else if (actorsMap.containsKey(currentStageName)) {
                    skippedEntries.add("Skipped actor '" + currentStageName + "' because they are already in the map.");
                } else {
                    actorsMap.put(currentStageName, new Actor(currentStageName, currentDOB));
                }
                currentStageName = null;
                currentDOB = null;
                break;
        }
    }

    public List<String> getSkippedEntries() {
        return skippedEntries;
    }
}
