package ch.so.agi.gretl.steps;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.testcontainers.containers.PostgreSQLContainer;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.gradle.testkit.runner.TaskOutcome.*;

public class SqlExecutorTaskTest {
    private static final String DB_NAME = "foo";
    private static final String USER = "bar";
    private static final String PWD = "baz";

    private File initFile;
    private String classpathString;

    @Rule 
    public final TemporaryFolder testProjectDir = new TemporaryFolder();
    

    @Rule
    public PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:9.6.5")
	    .withDatabaseName(DB_NAME)
	    .withUsername(USER)
	    .withPassword(PWD);

    @Before
    public void setup() throws IOException {
        initFile = testProjectDir.newFile("init.gradle");
        
        	List<String> pluginClasspathList = Files.readAllLines(Paths.get("build/resources/integrationTest/plugin-classpath.txt"), Charset.forName("UTF-8"));
        	for (int i=0; i<pluginClasspathList.size(); i++) {
        		String p = pluginClasspathList.get(i);
        		pluginClasspathList.set(i, "'" + p + "'");
        	}        	
        	
        	classpathString = String.join(",", pluginClasspathList);
    }
    
    @Test
    public void testSimple() throws SQLException, InterruptedException {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:postgresql://"
            + postgres.getContainerIpAddress()
            + ":" + postgres.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT)
            + "/" + DB_NAME);
        hikariConfig.setUsername(USER);
        hikariConfig.setPassword(PWD);

        System.err.println("**********");
        System.err.println(postgres.getJdbcUrl());
        
        //Thread.sleep(50000);
        
        HikariDataSource ds = new HikariDataSource(hikariConfig);
        Statement statement = ds.getConnection().createStatement();
        statement.execute("SELECT 1");
        ResultSet resultSet = statement.getResultSet();

        System.err.println("**********");

        resultSet.next();
        int resultSetInt = resultSet.getInt(1);
        System.err.println("**********");

        assertEquals("A basic SELECT query succeeds", 1, resultSetInt);
    }
    
    @Ignore
    @Test
    public void testHelloWorldTask() throws IOException {
    	            
        String initFileContent = "allprojects {\n" + 
        		"    buildscript {\n" + 
        		"        dependencies {\n" + 
        		"            classpath files("+ classpathString +")\n" + 
        		"        }\n" + 
        		"        ext {\n" + 
        		"            //dbUrl = System.env.dbUrl\n" + 
        		"            //dbUser = System.env.dbUser\n" + 
        		"            //dbPass = System.env.dbPass\n" + 
        		"            dbUrl = \"jdbc:postgresql://geodb-dev.cgjofbdf5rqg.eu-central-1.rds.amazonaws.com:5432/xanadu2\"\n" + 
        		"            dbUser = \"xxxxx\"\n" + 
        		"            dbPass = \"yyyyyy\"\n" + 
        		"        }\n" + 
        		"    }\n" + 
        		"}";
                
        writeFile(initFile, initFileContent);

        GradleRunner runner = GradleRunner.create()
            .withProjectDir(new File("src/integrationTest/resources/"))
            .withArguments("--init-script", initFile.getAbsolutePath(), "copyGemeindegrenzen")
            .withDebug(true);
            
        BuildResult result = runner.build();
        
        assertTrue(result.getOutput().contains("copyGemeindegrenzen: Transfered all Transfersets"));
        assertEquals(result.task(":copyGemeindegrenzen").getOutcome(), SUCCESS);
    }

    private void writeFile(File destination, String content) throws IOException {
        BufferedWriter output = null;
        try {
            output = new BufferedWriter(new FileWriter(destination));
            output.write(content);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }
}

