package miltos.diploma.unittests;

import miltos.diploma.evaluation.Project;
import miltos.diploma.qualitymodel.Property;
import miltos.diploma.toolkit.FxcopAggregator;
import miltos.diploma.toolkit.Issue;
import miltos.diploma.toolkit.IssueSet;
import org.junit.Assert;
import org.junit.Test;

import java.util.Vector;

public class FxcopAggregatorTest {

    /**
     * This test should take in a hard-coded Project object with hard-coded IssueSet, Properties, etc.
     * and ensure the aggregation at the Issue level works as expected.
     */
    @Test
    public void testAggregate() {
        Issue i1_11 = new Issue(1);
        Issue i1_21 = new Issue(2);
        Issue i1_22 = new Issue(2);
        Issue i1_31 = new Issue(3);
        Issue i1_32 = new Issue(3);
        Issue i1_33 = new Issue(3);

        Issue i2_11 = new Issue(5);
        Issue i2_21 = new Issue(4);
        Issue i2_31 = new Issue(3);

        Vector<Issue> issues01 = new Vector<Issue>() {{
           add(i1_11); add(i1_21); add(i1_22); add(i1_31); add(i1_32); add(i1_33);
        }};
        Vector<Issue> issues02 = new Vector<Issue>() {{
            add(i2_11); add(i2_21);  add(i2_31);
        }};
        IssueSet is01 = new IssueSet("PropertyName01", issues01);
        IssueSet is02 = new IssueSet("PropertyName02", issues02);

        Property p01 = new Property("PropertyName01");
        Property p02 = new Property("PropertyName02");

        Project p = new Project();
        p.addProperty(p01);
        p.addProperty(p02);
        p.addIssueSet(is01);
        p.addIssueSet(is02);

        FxcopAggregator aggregator = new FxcopAggregator();
        aggregator.aggregate(p);

        Assert.assertEquals(6, p.getProperties().get("PropertyName01").getMeasure().getValue(), 0.001);
        Assert.assertEquals(3, p.getProperties().get("PropertyName02").getMeasure().getValue(), 0.001);
    }
}
