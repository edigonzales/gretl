package ch.so.agi.gretl.jobs;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.PostgisContainerProvider;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import ch.so.agi.gretl.util.GradleVariable;
import ch.so.agi.gretl.util.IntegrationTestUtil;
import ch.so.agi.gretl.util.IntegrationTestUtilSql;

import static org.junit.Assert.*;

public class Ili2pgValidateDatasetTest {
    static String WAIT_PATTERN = ".*database system is ready to accept connections.*\\s";
    private final GradleVariable[] gradleVariables = {GradleVariable.newGradleProperty(IntegrationTestUtilSql.VARNAME_PG_CON_URI, postgres.getJdbcUrl())};

    @ClassRule
    public static PostgreSQLContainer postgres = 
        (PostgreSQLContainer) new PostgisContainerProvider()
        .newInstance().withDatabaseName("gretl")
        .withUsername(IntegrationTestUtilSql.PG_CON_DDLUSER)
        .withPassword(IntegrationTestUtilSql.PG_CON_DDLPASS)
        .withInitScript("init_postgresql.sql")
        .waitingFor(Wait.forLogMessage(WAIT_PATTERN, 2));

    @Test
    public void validateSingleDataset_Ok() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Ili2pgValidateSingleDataset");
        IntegrationTestUtil.getGradleRunner(projectDirectory, "validate", gradleVariables).build();

        String logFileContent = new String(Files.readAllBytes(Paths.get(projectDirectory + "/validation.log")));
        assertTrue(logFileContent.contains("Info: ...validate done"));        
    }
    
    @Test
    public void validateMultipleDataset_Ok() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Ili2pgValidateMultipleDatasets");
        IntegrationTestUtil.getGradleRunner(projectDirectory, "validate", gradleVariables).build();

        String logFileContent = new String(Files.readAllBytes(Paths.get(projectDirectory + "/validation.log")));
        assertTrue(logFileContent.contains("Info: ...validate done"));        
    }
    
    @Test
    public void validateData_Fail() throws Exception {
        File projectDirectory = new File(System.getProperty("user.dir") + "/src/integrationTest/jobs/Ili2pgValidateMultipleDatasets");

        assertThrows(Exception.class, () -> {
            IntegrationTestUtil.getGradleRunner(projectDirectory, "validate").build();
        });

        String logFileContent = new String(Files.readAllBytes(Paths.get(projectDirectory + "/validation.log")));

        assertTrue(logFileContent.contains("Error: ...validate failed"));
    }
}
