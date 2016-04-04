package maqs.ehs.util;

import com.sigilent.business.util.StringUtils;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class FileSystemManager {

    private static final String OP_SYS_WIN = "win";
    private static final String OP_SYS_LIN = "linux";
    public static String operatingSystem;

    static {
        operatingSystem = System.getProperty( "op.sys" );
        if ( StringUtils.isEmpty( operatingSystem ) ) {
            // default to windows operating system
            operatingSystem = OP_SYS_WIN;
        }
    }

    public static boolean isLinux() {
        return OP_SYS_LIN.equals( operatingSystem );
    }

    public static boolean isWindows() {
        return OP_SYS_WIN.equals( operatingSystem );
    }

    public static String renderArgument( String argument ) {
        return quote( argument );
    }

    public static String getSlash() {
        return isWindows() ? "\\" : "/";
    }

    static String createTempFileFullPath() {
        return getTempDirPath() + getSlash() + new Date().getTime() + ".tmp";
    }

    static String getTempDirPath() {
        return AppProperties.getTempDirPath();
    }

    static String quote( String str ) {
        return "\"" + str + "\"";
    }

    static String getResourceDirPath() {
        return AppProperties.getResourceDirPath();
    }

    public static String createExportFileFullPath( String recordName ) {
        return AppProperties.getOutgoingDirPath() + getSlash() + CommonUtil.sanitizeRecord( recordName ) + ".pgp";
    }

    static String createExportFileBackupFullPath( String recordName ) {
        return AppProperties.getBackupDirPath() + getSlash() + CommonUtil.sanitizeRecord( recordName ) + ".pgp";
    }

    public static String createTestExportFileFullPath( String recordName ) {
        return AppProperties.getTestDir() + getSlash() + CommonUtil.sanitizeRecord( recordName ) + ".pgp";
    }

    static String getLocalRecordFileFullPath( String recordName ) {
        return AppProperties.getWorkDirPath() + getSlash() + CommonUtil.sanitizeRecord( recordName );
    }

    public static String getLocalRecordBackupFileFullPath( String recordName ) {
        return AppProperties.getBackupDirPath() + getSlash() + CommonUtil.sanitizeRecord( recordName );
    }

    private static String getBatchFullPath() {
        File path = new File( AppProperties.getBatchPath() );
        return path.getAbsolutePath();
    }

    static String getGpgKeyStoreDir() {
        File path = new File( AppProperties.getKeyStoreDir() );
        return path.getAbsolutePath();
    }

    private static String getGpgFullPath() {
        File path = new File( AppProperties.getGpgPath() );
        return path.getAbsolutePath();
    }

    static boolean executeEncrypt( String user, String inputFilePath, String outputFilePath ) {
        StringBuffer cmd = new StringBuffer();
        buildGpgCmd( cmd );
        cmd.append( " " );

        cmd.append( "--batch -r " );
        cmd.append( renderArgument( user ) );
        cmd.append( " " );

        cmd.append( "-o " );
        cmd.append( renderArgument( outputFilePath ) );
        cmd.append( " " );

        cmd.append( "--encrypt " );
        cmd.append( renderArgument( inputFilePath ) );
        cmd.append( " " );

        return executeShellCmd( cmd );
    }

    static boolean executeDecrypt( String user, String inputFilePath,
                                   String outputFilePath, String passPhrase ) {
        StringBuffer cmd = new StringBuffer();
        buildGpgCmd( cmd );
        cmd.append( " " );

        cmd.append( "--batch -r " );
        cmd.append( renderArgument( user ) );
        cmd.append( " " );

        cmd.append( "-o " );
        cmd.append( renderArgument( outputFilePath ) );
        cmd.append( " " );

        cmd.append( "--passphrase " );
        cmd.append( renderArgument( passPhrase ) );
        cmd.append( " " );

        cmd.append( "--decrypt " );
        cmd.append( renderArgument( inputFilePath ) );
        cmd.append( " " );

        return executeShellCmd( cmd );
    }

    static boolean executeImportKey( String importKeyPath ) {
        StringBuffer cmd = new StringBuffer();
        buildGpgCmd( cmd );
        cmd.append( " " );
        cmd.append( "--batch --import " );

        cmd.append( renderArgument( importKeyPath ) );

        return executeShellCmd( cmd );
    }

    static boolean executeDeletePublicKey( String name ) {
        StringBuffer cmd = new StringBuffer();
        buildGpgCmd( cmd );
        cmd.append( " " );
        cmd.append( "--batch --yes --delete-key " );

        cmd.append( renderArgument( name ) );

        return executeShellCmd( cmd );
    }

    static boolean executeSignKey( String passphrase, String keyToSign ) {
        StringBuffer cmd = new StringBuffer();
        buildGpgCmd( cmd );
        cmd.append( " " );
        cmd.append( "--batch --yes --passphrase " );

        cmd.append( renderArgument( passphrase ) );

        cmd.append( " " );

        cmd.append( "--sign-key " );

        cmd.append( renderArgument( keyToSign ) );

        return executeShellCmd( cmd );
    }

    static void reinitKeystoreDir() {
        File keyStoreDir = new File( getGpgKeyStoreDir() );
        if ( keyStoreDir.isDirectory() && keyStoreDir.exists() ) {
            deleteDir( keyStoreDir );
        }

        keyStoreDir.mkdir();
    }

    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
    public static boolean deleteDir( File dir ) {
        if ( dir.isDirectory() ) {
            String[] children = dir.list();
            for ( int i = 0; i < children.length; i++ ) {
                boolean success = deleteDir( new File( dir, children[i] ) );
                if ( !success ) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    public static boolean deleteFile( File file ) {
        return file.delete();
    }

    static boolean executeShellCmd( StringBuffer batch ) {
        return executeShellCmd( batch, new ArrayList<String>(), new ArrayList<String>() );
    }

    static boolean executeShellCmd( StringBuffer batch, List<String> output, List<String> error ) {
        try {
            Runtime rt = Runtime.getRuntime();

            String theString = batch.toString();
            System.out.println( theString );

            if ( isLinux() ) {
                // if linux, build a wrapper around the file in a location (i.e. /tmp) which is executable
                // and is guaranteed not to have spaces
                File execFile = new File( "/tmp/exec.sh" );
                if ( execFile.exists() ) {
                    execFile.delete();
                }
                File file = createFileForData( "/tmp/exec.sh", theString );
                rt.exec( "chmod +x /tmp/exec.sh" ).waitFor();
                theString = file.getAbsolutePath();
            }

            Process proc = rt.exec( theString );
            // any error message?
            StreamGobbler errorGobbler = new StreamGobbler( proc.getErrorStream(), "ERROR", error );

            // any output?
            StreamGobbler outputGobbler = new StreamGobbler( proc.getInputStream(), "OUTPUT", output );

            // kick them off
            errorGobbler.start();
            outputGobbler.start();

            // any error???
            return proc.waitFor() < 1;
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    static boolean exportLocalUserPublicKey( String localUserName, String targetLocation ) {
        StringBuffer cmd = new StringBuffer();
        buildGpgCmd( cmd );
        cmd.append( " " );
        cmd.append( "-r " );
        cmd.append( renderArgument( localUserName ) );
        cmd.append( " " );
        cmd.append( "-o " );
        cmd.append( renderArgument( targetLocation ) );
        cmd.append( " " );
        cmd.append( "-a --export " );

        return executeShellCmd( cmd );
    }

    static boolean publicKeyExists( String name ) {
        StringBuffer cmd = new StringBuffer();
        buildGpgCmd( cmd );
        cmd.append( " " );
        cmd.append( "--list-keys " );
        cmd.append( renderArgument( name) );

        List<String> output = new ArrayList<String>();
        List<String> error = new ArrayList<String>();
        
        executeShellCmd( cmd, output, error );

        for ( String str : output ) {
            if ( str != null &&  str.contains( "public key not found" ) ) {
                return false;
            }
        }
        for ( String str : error ) {
            if ( str != null && str.contains( "public key not found" ) ) {
                return false;
            }
        }
        return true;
    }

    public static boolean createLocalKeyPair( String username, String passphrase ) {
        // 1.
        String pathname = getBatchFullPath() + "/create_local_keypair.txt";
        File keyPairScript = new File( pathname );

        if ( !keyPairScript.exists() ) {
            throw new RuntimeException( "Could not locate create local keypair script; "
                    + keyPairScript.getAbsolutePath() + " does not exist." );
        }
        StringBuffer scriptSb = new StringBuffer();
        fillStringBufferFromFile( keyPairScript, scriptSb );
        if ( scriptSb.length() < 1 ) {
            throw new RuntimeException( "Could not read " + keyPairScript.getAbsolutePath() );
        }

        String contents = scriptSb.toString();
        contents = contents.replaceAll( "\\^username\\^", username );
        contents = contents.replaceAll( "\\^passphrase\\^", passphrase );
        contents += "\r\n";

        // 2.
        File tmpFile = new File( createTempFileFullPath() );
        createFileForData( tmpFile.getAbsolutePath(), contents );

        // 3.
        StringBuffer cmd = new StringBuffer();
        buildGpgCmd( cmd );
        cmd.append( " " );
        cmd.append( "--batch --gen-key " );
        cmd.append( renderArgument( tmpFile.getAbsolutePath() ) );

        // 4.
        boolean success = executeShellCmd( cmd );
        tmpFile.delete();
        return success;
    }

    private static void buildGpgCmd( StringBuffer cmd ) {
        if ( isWindows() ) {
            cmd.append( "\"" );
        }
        cmd.append( getGpgFullPath() );
        cmd.append( getSlash() );
        cmd.append( "gpg" );
        if ( isWindows() ) {
            cmd.append( ".exe" );
        }
        if ( isWindows() ) {
            cmd.append( "\"" );
        }
        cmd.append( " --homedir " );
        cmd.append( renderArgument( getGpgKeyStoreDir() ) );
    }

    public static void fillStringBufferFromFile( File tmpFile, StringBuffer recordData ) {
        BufferedReader input = null;
        try {
            input = new BufferedReader( new FileReader( tmpFile ) );
            String line;
            while ( ( line = input.readLine() ) != null ) {
                recordData.append( line + "\n" );
            }
        }
        catch ( FileNotFoundException ex ) {
            ex.printStackTrace();
        }
        catch ( IOException ex ) {
            ex.printStackTrace();
        }
        finally {
            try {
                if ( input != null ) {
                    input.close();
                }
            }
            catch ( IOException ex ) {
                ex.printStackTrace();
            }
        }
    }

    static File createFileForData( String filePath, String recordData ) {
        File tmpFile = new File( filePath );
        if ( tmpFile.exists() ) {
            throw new RuntimeException( "File " + filePath + " already exists!" );
        }

        try {
            tmpFile.createNewFile();
            FileWriter fileWriter = new FileWriter( tmpFile );
            fileWriter.write( recordData );
            fileWriter.close();
        } catch ( IOException e ) {
            throw new RuntimeException( "Failed to create temp file " + filePath + ": " + e );
        }
        return tmpFile;
    }


    static class StreamGobbler extends Thread {
        InputStream is;
        String type;
        private List<String> output;
        private List<String> error;

        StreamGobbler( InputStream is, String type, List<String> output ) {
            this.is = is;
            this.type = type;
            this.output = output;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader( is );
                BufferedReader br = new BufferedReader( isr );
                String line = null;
                while ( ( line = br.readLine() ) != null ) {
                    output.add( line );
                }
            } catch ( IOException ioe ) {
                ioe.printStackTrace();
            }
        }
    }

    public static void main( String s[] ) {
        System.out.println( "Aron is cool".replaceAll( "\\s", "\\\\ ") );
    }
}



