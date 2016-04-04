package maqs.ehs.patient;

import com.sigilent.business.util.StringUtils;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

import maqs.ehs.util.AppProperties;

public class TabbedKeyedResource extends KeyedResource {
    public void load() {
        // pre
        if ( StringUtils.isEmpty( getSourceName() ) ) {
            throw new RuntimeException( "No resource provided" );
        }

        // pre
        File file = new File( AppProperties.getResourceDirPath() + "/" + getSourceName() );
        if ( !file.exists() || !file.isFile() ) {
            throw new RuntimeException( getSourceName() + " is not a valid resource file" );
        }

        try {
            String line;
            BufferedReader in = new BufferedReader( new FileReader( file ) );
            while ( ( line = in.readLine() ) != null ) {
                String[] parts = line.split( "\t" );
                String key = parts[0].trim();
                String value = parts[1].trim();
                addEntry( key, value );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public static void main( String[] args ) {
        System.out.println( FieldManager.getKeyedResource( "I9diagnosis-with-dots.txt" ).searchResource( "salmonella",1 ));
    }

}
