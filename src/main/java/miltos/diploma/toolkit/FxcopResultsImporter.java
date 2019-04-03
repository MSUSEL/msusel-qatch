package miltos.diploma.toolkit;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

/**
 * This class is responsible for importing the issues found by FxCop 15.0
 * in to a data structure.
 */
public class FxcopResultsImporter {

    /**
     * Parse issues in the Fxcop XML file that correspond to a property
     * into an IssueSet object
     *
     * @param path
     *      the directory location of the XML file
     * @return
     *      object representing all found violations of the current property
     */
    public IssueSet parseIssues(String path) throws ParserConfigurationException, IOException, SAXException {

        File scanResults = new File(path);
        String propertyName = scanResults.getName().substring(0, scanResults.getName().length()-4);
        IssueSet issues = new IssueSet(propertyName);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder loader = factory.newDocumentBuilder();
        Document document = loader.parse("C:\\Users\\davidrice3\\Repository\\MSUSEL\\msusel-qatch\\src\\test\\resources\\scanner-results\\FxcopFindingsProperty01.xml");
        DocumentTraversal trav = (DocumentTraversal) document;

        NodeIterator it = trav.createNodeIterator(document.getDocumentElement(), NodeFilter.SHOW_ALL, null, true);

        for (Node node = it.nextNode(); node != null; node = it.nextNode()) {
            if (node.getNodeName().equals("Message")) {
                NamedNodeMap attributes = node.getAttributes();
                System.out.println(attributes.item(3).toString());
            }
        }
        throw new RuntimeException("TODO complete writing method");

    }
}
