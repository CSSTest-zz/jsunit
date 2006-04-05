package net.jsunit;

import net.jsunit.configuration.Configuration;
import net.jsunit.model.*;
import org.jdom.Document;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DistributedTestRunManager {

    private Logger logger = Logger.getLogger("net.jsunit");
    private RemoteServerHitter hitter;
    private Configuration localConfiguration;
    private String overrideURL;
    private DistributedTestRunResult distributedTestRunResult = new DistributedTestRunResult();
    private Browser singleRemoteBrowser;

    public static DistributedTestRunManager forConfigurationAndURL(RemoteServerHitter hitter, Configuration localConfiguration, String overrideURL) {
        return new DistributedTestRunManager(hitter, localConfiguration, overrideURL);
    }

    protected DistributedTestRunManager(RemoteServerHitter hitter, Configuration localConfiguration, String overrideURL) {
        this.hitter = hitter;
        this.localConfiguration = localConfiguration;
        this.overrideURL = overrideURL;
    }

    public void runTests() {
        List<Thread> threads = new ArrayList<Thread>();
        for (final URL baseURL : localConfiguration.getRemoteMachineURLs())
            threads.add(new Thread("Run JsUnit tests on " + baseURL) {
                public void run() {
                    runTestsOnRemoteMachine(baseURL);
                }
            });
        for (Thread thread : threads)
            thread.start();
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException("One of the test threads was interrupted.");
            }
        }
    }

    private void runTestsOnRemoteMachine(URL baseURL) {
        List<TestRunResult> results = new ArrayList<TestRunResult>();
        try {
            URL fullURL = buildURL(baseURL);
            logger.info("Requesting run on remote machine URL " + baseURL);
            Document documentFromRemoteMachine = hitter.hitURL(fullURL);
            logger.info("Received response from remote machine URL " + baseURL);
            if (isMultipleTestRunResultsResult(documentFromRemoteMachine)) {
                DistributedTestRunResult multiple = new DistributedTestRunResultBuilder(localConfiguration).build(documentFromRemoteMachine);
                results.addAll(multiple.getTestRunResults());
            } else {
                TestRunResult single = new TestRunResultBuilder(localConfiguration).build(documentFromRemoteMachine);
                results.add(single);
            }
        } catch (IOException e) {
            if (localConfiguration.shouldIgnoreUnresponsiveRemoteMachines())
                logger.info("Ignoring unresponsive machine " + baseURL.toString());
            else {
                logger.info("Remote machine URL is unresponsive: " + baseURL.toString());
                TestRunResult unresponsiveResult = new TestRunResult(baseURL);
                unresponsiveResult.setUnresponsive();
                results.add(unresponsiveResult);
            }
        }
        for (TestRunResult result : results) {
            result.setURL(baseURL);
            //noinspection SynchronizeOnNonFinalField
            synchronized (distributedTestRunResult) {
                distributedTestRunResult.addTestRunResult(result);
            }
        }
    }

    private boolean isMultipleTestRunResultsResult(Document document) {
        return document.getRootElement().getName().equals(DistributedTestRunResult.NAME);
    }

    private URL buildURL(URL url) throws UnsupportedEncodingException, MalformedURLException {
        String fullURLString = url.toString();
        fullURLString += "/runner";
        boolean hasFirstParameter = false;
        if (overrideURL != null) {
            fullURLString += "?url=" + URLEncoder.encode(overrideURL, "UTF-8");
            hasFirstParameter = true;
        } else if (localConfiguration.getTestURL() != null) {
            fullURLString += "?url=" + URLEncoder.encode(localConfiguration.getTestURL().toString(), "UTF-8");
            hasFirstParameter = true;
        }
        if (singleRemoteBrowser != null) {
            fullURLString += (hasFirstParameter ? "&" : "?");
            fullURLString += "browserId=" + singleRemoteBrowser.getId();
        }
        return new URL(fullURLString);
    }

    public DistributedTestRunResult getDistributedTestRunResult() {
        return distributedTestRunResult;
    }

    public String getOverrideURL() {
        return overrideURL;
    }

    public void setOverrideURL(String overrideURL) {
        this.overrideURL = overrideURL;
    }

    public void limitToBrowser(Browser remoteBrowser) {
        this.singleRemoteBrowser = remoteBrowser;
    }
}
