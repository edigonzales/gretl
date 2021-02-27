package ch.so.agi.gretl.tasks.impl;

import java.io.File;
import java.sql.SQLException;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;

import ch.ehi.basics.logging.EhiLogger;
import ch.ehi.ili2db.base.Ili2db;
import ch.ehi.ili2db.base.Ili2dbException;
import ch.ehi.ili2db.gui.Config;
import ch.ehi.ili2h2gis.H2gisMain;
import ch.so.agi.gretl.api.Connector;
import ch.so.agi.gretl.logging.GretlLogger;
import ch.so.agi.gretl.logging.LogEnvironment;
import ch.so.agi.gretl.util.TaskUtil;
import groovy.lang.Range;

public abstract class Ili2h2gisAbstractTask extends DefaultTask {
    protected GretlLogger log;

    @InputFile
    public Object dbfile;
    @Input
    @Optional
    public String dbschema = null;
    @Input
    @Optional
    public String proxy = null;
    @Input
    @Optional
    public Integer proxyPort = null;
    @Input
    @Optional
    public String modeldir = null;
    @Input
    @Optional
    public String models = null;
    @Input
    @Optional
    public Object dataset = null;
    @Input
    @Optional
    public String baskets = null;
    @Input
    @Optional
    public String topics = null;
    @Input
    @Optional
    public boolean importTid = false;
    @Input
    @Optional
    public boolean importBid = false;
    @InputFile
    @Optional
    public File preScript = null;
    @InputFile
    @Optional
    public File postScript = null;
    @Input
    @Optional
    public boolean deleteData = false;
    @OutputFile
    @Optional
    public Object logFile = null;
    @Input
    @Optional
    public boolean trace = false;
    @InputFile
    @Optional
    public File validConfigFile = null;
    @Input
    @Optional
    public boolean disableValidation = false;
    @Input
    @Optional
    public boolean disableAreaValidation = false;
    @Input
    @Optional
    public boolean forceTypeValidation = false;
    @Input
    @Optional
    public boolean strokeArcs = false;
    @Input
    @Optional
    public boolean skipPolygonBuilding = false;
    @Input
    @Optional
    public boolean skipGeometryErrors = false;
    @Input
    @Optional
    public boolean iligml20 = false;
    @Input
    @Optional
    public boolean disableRounding = false;  
    @Input
    @Optional
    public boolean failOnException = true;
    @Input
    @Optional
    public Range<Integer> datasetSubstring = null;

    protected void run(int function, Config settings) {
        log = LogEnvironment.getLogger(Ili2pgAbstractTask.class);

        if (dbfile == null) {
            throw new IllegalArgumentException("dbfile must not be null");
        }
        
        settings.setFunction(function);

        if (proxy != null) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_HOST, proxy);
        }
        if (proxyPort != null) {
            settings.setValue(ch.interlis.ili2c.gui.UserSettings.HTTP_PROXY_PORT, proxyPort.toString());
        }

        if (dbschema != null) {
            settings.setDbschema(dbschema);
        }
        if (modeldir != null) {
            settings.setModeldir(modeldir);
        }
        if (models != null) {
            settings.setModels(models);
        }
        if (baskets != null) {
            settings.setBaskets(baskets);
        }
        if (topics != null) {
            settings.setTopics(topics);
        }
        if (importTid) {
            settings.setImportTid(true);
        }
        if (importBid) {
            settings.setImportBid(true);
        }
        if (preScript != null) {
            settings.setPreScript(this.getProject().file(preScript).getPath());
        }
        if (postScript != null) {
            settings.setPostScript(this.getProject().file(postScript).getPath());
        }
        if (deleteData) {
            settings.setDeleteMode(Config.DELETE_DATA);
        }
        if(function!=Config.FC_IMPORT && function!=Config.FC_UPDATE && function!=Config.FC_REPLACE) {
            if (logFile != null) {
                settings.setLogfile(this.getProject().file(logFile).getPath());
            }
        }
        if (trace) {
            EhiLogger.getInstance().setTraceFilter(false);
        }
        if (validConfigFile != null) {
            settings.setValidConfigFile(this.getProject().file(validConfigFile).getPath());
        }
        if (disableValidation) {
            settings.setValidation(false);
        }
        if (disableAreaValidation) {
            settings.setDisableAreaValidation(true);
        }
        if (forceTypeValidation) {
            settings.setOnlyMultiplicityReduction(true);
        }
        if (strokeArcs) {
            Config.setStrokeArcs(settings, Config.STROKE_ARCS_ENABLE);
        }
        if (skipPolygonBuilding) {
            Ili2db.setSkipPolygonBuilding(settings);
        }
        if (skipGeometryErrors) {
            settings.setSkipGeometryErrors(true);
        }
        if (iligml20) {
            settings.setTransferFileFormat(Config.ILIGML20);
        }
        if (disableRounding) {
            settings.setDisableRounding(true);;
        }        

        try {
            String dbFileName = this.getProject().file(dbfile).getAbsolutePath();
            settings.setDbfile(dbFileName);
            settings.setDburl("jdbc:h2:" + settings.getDbfile());

            Ili2db.readSettingsFromDb(settings);
            Ili2db.run(settings, null);
        } catch (Exception e) {
            log.error("failed to run ili2h2gis", e);

            GradleException ge = TaskUtil.toGradleException(e);
            throw ge;
        }        
    }

    protected Config createConfig() {
        Config settings = new Config();
        new H2gisMain().initConfig(settings);
        return settings;
    }

}
