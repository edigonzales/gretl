package ch.so.agi.gretl.jobs;

import ch.so.agi.gretl.util.IntegrationTestUtil;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Test;

import java.io.File;
import java.util.Objects;

import static org.junit.Assert.*;

public class IliValidatorTest {
    @Test
    public void validationOk() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/IliValidator");
        IntegrationTestUtil.executeTestRunner(projectDirectory, "validate");
    }
    @Test
    public void validationFileSetOk() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/IliValidatorFileSet");
        IntegrationTestUtil.executeTestRunner(projectDirectory, "validate");
    }

    @Test
    public void validationFail() {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/IliValidatorFail");

        assertThrows(AssertionError.class, () -> {
            IntegrationTestUtil.executeTestRunner(projectDirectory, "validate");
        });
    }
}
