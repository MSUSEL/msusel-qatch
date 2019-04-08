package miltos.diploma.unittests;

import miltos.diploma.toolkit.FxcopResultsImporter;
import miltos.diploma.toolkit.Issue;
import miltos.diploma.toolkit.IssueSet;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class FxcopResultsImporterTest {

    /**
     * This test should successfully find the FxCop XML file, parse the found issues in to Issue objects,
     * add the Issue objects to an IssueSet, and name the IssueSet according to the QM Property the findings belong to.
     */
    @Test
    public void testParseIssues() throws IOException, SAXException, ParserConfigurationException {
        String resultsPath = "src/test/resources/scanner-results/FxcopFindingsProperty01.xml";
        FxcopResultsImporter ri = new FxcopResultsImporter();

        IssueSet is = ri.parseIssues(resultsPath);
        Issue firstIssue = is.getIssues().firstElement();
        Issue lastIssue = is.getIssues().lastElement();

        Assert.assertEquals(is.getPropertyName(), "FxcopFindingsProperty01");
        Assert.assertEquals(is.getIssues().size(),8);

        Assert.assertEquals(firstIssue.getRuleName(), "IdentifiersShouldBeSpelledCorrectly");
        Assert.assertEquals(firstIssue.getRuleSetName(), "Microsoft.Naming");
        Assert.assertEquals(firstIssue.getPriority(), 3);

        Assert.assertEquals(lastIssue.getRuleName(), "ReviewUnusedParameters");
        Assert.assertEquals(lastIssue.getRuleSetName(), "Microsoft.Usage");
        Assert.assertEquals(lastIssue.getPriority(), 4);
    }
}
