package maqs.ehs.patient;

import com.sigilent.business.util.StringUtils;
import maqs.ehs.util.SecurityUtil;
import maqs.ehs.form.RemotePublisher;
import maqs.ehs.form.RemoteImporter;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImportManger {

    private static final String ERROR_INCORRECT_NUMBER_OF_COLUMNS = "Incorrect number of columns";

    private static Map<Integer, ColumnValidator> columnValidatorMap;
    private static Map<Integer, String> fieldPositionMap;

    static void processImportLine( ImportedLine line ) {
        try {
            persistImportLine( line );
            line.getResults().incrementSuccessCtr();
        } catch ( Exception e ) {
            line.getErrors().add( "Cannot import patient: " + e );
        }
    }

    static void processErrors( ImportedLine line ) throws Exception {
        List errors = line.getErrors();
        if ( !errors.isEmpty() && !StringUtils.isEmpty( line.getRawLine() ) ) {
            line.getResults().incrementFailureCtr();
        }
    }

    static void processImportedLine( ImportedLine line ) throws Exception {
        List args = line.getArgs();

        // exact argument number check
        if ( args.size() != getMaxColumns() ) {
            line.getErrors().add( ERROR_INCORRECT_NUMBER_OF_COLUMNS + "; expected " + getMaxColumns() + ", found " + args.size() );
            processErrors( line );
            return;
        }

        int columnPos = 0;
        for ( Iterator itr = line.getArgs().iterator(); itr.hasNext(); ) {
            String nextArg = ( String ) itr.next();
            ColumnValidator columnValidor = columnValidatorMap.get( columnPos );
            if ( !StringUtils.isEmpty( columnValidor.getRegex() ) ) {
                Pattern pattern = Pattern.compile( columnValidor.getRegex() );
                Matcher matcher = pattern.matcher( nextArg );
                if ( !matcher.matches() ) {
                    line.getErrors().add( columnValidor.getDescription() );
                }
            }
            columnPos++;
        }

        if ( !line.hasErrors() ) {
            processImportLine( line );
        }

        processErrors( line );
    }

    private static int getMaxColumns() {
        return columnValidatorMap.size();
    }

    public static ImportResults processImportFile( File encryptedFile ) {

        ImportResults importResults = new ImportResults();

        /// Decrypt the input file

        File decryptedFile = SecurityUtil.decryptImportFile( encryptedFile );
        if ( decryptedFile == null || !decryptedFile.exists() ) {
            importResults.getGlobalImportError().append( "File failed to import using local private key and passphrase." );
            return importResults;
        }

        ////

        ImportedLine importedLine;
        BufferedReader bReader = null;
        try {
            if ( columnValidatorMap == null || fieldPositionMap == null ) {
                prepMaps();
            }

            bReader = new BufferedReader( new InputStreamReader( new FileInputStream( decryptedFile ) ) );
            String nextLine = bReader.readLine();
            int position = 1;
            while ( nextLine != null ) {
                List<String> lineArgs = new ArrayList<String>();
                if ( !StringUtils.isEmpty( nextLine ) ) {
                    String[] elem = nextLine.split( "\t" );
                    for ( String anElem : elem ) {
                        anElem = stripQuotes( anElem );
                        lineArgs.add( anElem );
                    }
                }
                importedLine = new ImportedLine( importResults );
                importedLine.setArgs( lineArgs );
                importedLine.setRawLine( nextLine );
                importedLine.setLineRowNumber( position );
                processImportedLine( importedLine );
                importResults.getImportedLines().add( importedLine );
                nextLine = bReader.readLine();
                position++;
            }

        } catch ( Exception e ) {
            throw new RuntimeException( e );
        } finally {
            if ( bReader != null ) {
                try {
                    bReader.close();
                } catch ( IOException e ) {
                    throw new RuntimeException( e );
                }
            }

            if ( decryptedFile.exists() ) {
                if ( !decryptedFile.delete() ) {
                    decryptedFile.deleteOnExit();   
                }
            }
        }

        return importResults;
    }

    private static String stripQuotes( String anElem ) {
        if ( StringUtils.isEmpty( anElem ) ) {
            return anElem;
        }

        if ( anElem.endsWith( "\"" ) && anElem.startsWith( "\"" ) ) {
            anElem = anElem.substring( 1, anElem.length() - 1 );
        }

        return anElem;
    }

    private static void prepMaps() {
        columnValidatorMap = new HashMap<Integer, ColumnValidator>();
        fieldPositionMap = new HashMap<Integer, String>();
        int i = 0;
        for ( PositionFieldMapping mapping : ImportMappingSvc.getAllPositionFieldMappings() ) {
            columnValidatorMap.put( i, mapping.getColumnValidator() );
            fieldPositionMap.put( i, mapping.getFieldId() );
            i++;
        }
    }

    static void persistImportLine( ImportedLine line ) throws Exception {
        PatientRecord record = PatientManager.createNew();
        PatientManager.save( record, line, fieldPositionMap );
    }

    public static RemoteImporter getRemoteImporter() {
        return new FtpRemoteImporter();
    }
}
