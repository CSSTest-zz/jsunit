package net.jsunit;

import junit.framework.TestCase;
import junit.framework.TestResult;

import net.jsunit.configuration.ConfigurationSource;
import net.jsunit.model.ResultType;

public class InvalidRemoteMachinesDistributedTestTest extends TestCase {

  protected ConfigurationSource invalidRemoteMachinesFarmSource() {
        return new StubConfigurationSource() {
            public String remoteMachineURLs() {
                return "http://invalid_machine1:8080, http://invalid_machine2:8080";
            }
        };
    }

    protected ConfigurationSource serverSource() {
        return new StubConfigurationSource();
    }

    public void testUnresponsive() {
      DistributedTest test = new DistributedTest(serverSource(), invalidRemoteMachinesFarmSource());
      TestResult testResult = test.run();
      assertFalse(testResult.wasSuccessful());
      assertEquals(
        ResultType.UNRESPONSIVE,
        test.getDistributedTestRunManager().getDistributedTestRunResult().getResultType()
      );
    }

}