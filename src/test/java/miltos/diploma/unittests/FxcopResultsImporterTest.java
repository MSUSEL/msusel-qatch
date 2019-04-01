package miltos.diploma.unittests;

import miltos.diploma.toolkit.FxcopResultsImporter;
import miltos.diploma.toolkit.Issue;
import miltos.diploma.toolkit.IssueSet;
import org.junit.Assert;
import org.junit.Test;

public class FxcopResultsImporterTest {

    /**
     * This method should successfully find the FxCop XML file, parse the found issues in to Issue objects,
     * add the Issue objects to an IssueSet, and name the IssueSet according to the QM Property the findings belong to.
     */
    @Test
    public void testParseIssues() {
        String resultsPath = "src/test/resources/scanner-results/FxcopFindingsProperty01.xml";
        FxcopResultsImporter ri = new FxcopResultsImporter();
        IssueSet is = ri.parseIssues(resultsPath);
        Issue firstIssue = is.getIssues().firstElement();
        Issue lastIssue = is.getIssues().lastElement();

        Assert.assertEquals(is.getPropertyName(), "FxcopFindingsProperty01");
        Assert.assertEquals(is.getIssues().size(),6);
    }
}
