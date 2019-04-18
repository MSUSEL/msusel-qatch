package miltos.diploma.toolkit;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public interface FindingsResultsImporter {
    IssueSet parse(String path) throws ParserConfigurationException, IOException, SAXException;
}
