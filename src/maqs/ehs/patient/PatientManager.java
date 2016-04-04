package maqs.ehs.patient;

import com.sigilent.business.util.StringUtils;
import maqs.ehs.util.SecurityUtil;
import maqs.ehs.util.IniWrapper;
import maqs.ehs.util.AppProperties;
import maqs.ehs.form.ResultCollector;
import maqs.ehs.form.ResultType;

import javax.swing.*;
import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.FileFilter;


public class PatientManager {
    private static IniWrapper exportStatusCache;

    private static final String EXPORT_STATUS_LOCATION = "resource/export_status.ini";

    public static PatientRecord read( String recordName ) {
        PatientRecord record = new PatientRecord();
        record.setRecordName( recordName );
        record.load();
        updateExportStatusCache( recordName, record.hasErrors() );
        return record;
    }

    public static void updateExportStatusCache( String recordName, boolean hasError ) {
        exportStatusCache.updateSectionKeyValue( recordName, "has_export_error", "" + hasError );
        exportStatusCache.writeIni();
        cleanupDeadCachRecords();
    }

    public static boolean isComplete( String recordName ) {
        String exportStatus = exportStatusCache.getSectionKeyValue( recordName, "has_export_error" );
        if ( exportStatus == null ) {
            PatientRecord record = read( recordName );
            boolean hasErrors = record.hasErrors();
            updateExportStatusCache( recordName, hasErrors );
            exportStatus = exportStatusCache.getSectionKeyValue( recordName, "has_export_error" );
        }
        return !StringUtils.isEmpty( exportStatus ) && "FALSE".equalsIgnoreCase( exportStatus );
    }

    public static boolean export( String recordName ) {
        return ExportManager.exportRecord( recordName );
    }

    public static boolean isAllRecordsExportable() {
        if ( exportStatusCache.isEmpty() ) {
            return false;
        }

        for ( Object record : exportStatusCache.getSectionKeys() ) {
            String status = exportStatusCache.getSectionKeyValue( ( String ) record, "has_export_error" );
            if ( status != null && "true".equals( status ) ) {
                return false;
            }
        }
        return true;
    }

    public static int getExportableRecordsCount(){
        int count = 0;
        for ( Object record : exportStatusCache.getSectionKeys() ) {
            String status = exportStatusCache.getSectionKeyValue( ( String ) record, "has_export_error" );
            if ( status != null && "false".equals( status ) ) {
                count++;
            }
        }
        return count;
    }

    public static PatientRecord createNew() {
        PatientRecord record = new PatientRecord();
        Collection<Field> fieldList = FieldManager.getFields();

        Map<String, Field> map = new LinkedHashMap<String, Field>();

        for ( Field field : fieldList ) {
            String defaulValue = field.getDefaultValue();
            if ( !StringUtils.isEmpty( defaulValue ) ) {
                field.setValue( defaulValue );
            }
            map.put( field.getId(), field );
        }
        record.setFields( map );
        record.setStatus( PatientRecord.Status.NEW );
        return record;
    }

    public static boolean save( PatientRecord record, JPanel displayPane, ResultCollector resultCollector ) {
        boolean success = record.save( displayPane, resultCollector );

        // update cache
        updateExportStatusCache( record.getRecordName(),
                resultCollector.hasResultsWithHigherSeverityThan( ResultType.INFO ));

        return success;
    }

    public static void save( PatientRecord record, ImportedLine line, Map<Integer, String> fieldPositionMap ) {
        record.save( line, fieldPositionMap );

        // update cache
        updateExportStatusCache( record.getRecordName(), record.hasErrors() );
    }

    public static void delete( String recordName ) {
        SecurityUtil.deleteLocalRecord( recordName );

        // update cache
        updateExportStatusCache( recordName, false );
    }

    public static void init() {
        // load export status record
        File exportErrors = new File( EXPORT_STATUS_LOCATION );
        if ( !exportErrors.exists() ) {
            // initialize the export cache if does not yet exist
            try {
                exportErrors.createNewFile();
            } catch ( IOException e ) {
                throw new RuntimeException( "Failed to create export status file at " + EXPORT_STATUS_LOCATION );
            }

            exportStatusCache = new IniWrapper(exportErrors.getAbsolutePath() );

            // populate export status file
            File workDir = new File( AppProperties.getWorkDirPath() );
            File[] files = workDir.listFiles();
            for ( File file : files ) {
                String recordName = file.getName();
                PatientRecord record = read( recordName );
                if ( record != null ) {
                    exportStatusCache.updateSectionKeyValue( recordName, "has_export_error", "" + record.hasErrors()  );
                }
            }
            exportStatusCache.writeIni();
        } else {
            // load the export cache if exists, clean up
            exportStatusCache = new IniWrapper(exportErrors.getAbsolutePath() );
            cleanupDeadCachRecords();
        }
    }

    private static void cleanupDeadCachRecords() {
        File[] currentRecords = new File( AppProperties.getWorkDirPath() ).listFiles( new FileFilter() {
            public boolean accept( File pathname ) {
                return pathname.isFile();
            }
        });
        List<String> currentRecordList = new ArrayList<String>();
        for ( File record: currentRecords ) {
            currentRecordList.add( record.getName() );
        }
        for ( Object cachedRecord : exportStatusCache.getSectionKeys() ) {
            if ( !currentRecordList.contains( cachedRecord ) ) {
                exportStatusCache.removeSection( (String) cachedRecord );
            }
        }
        exportStatusCache.writeIni();
    }
}
