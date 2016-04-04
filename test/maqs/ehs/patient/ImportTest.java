package maqs.ehs.patient;

import java.io.File;

public class ImportTest {
    public static void main( String s[] ) {
        String path = "C:\\projects\\ehs\\test\\maqs\\ehs\\patient\\testimport.csv";
        File file = new File( path );
        ImportManger.processImportFile( file );
    }
}
