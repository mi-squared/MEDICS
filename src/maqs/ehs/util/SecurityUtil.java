package maqs.ehs.util;

import maqs.ehs.patient.ExportManager;

import java.io.File;
import java.util.Date;

public class SecurityUtil {

    public static void deleteLocalRecord( String recordName ) {
        File file = new File( FileSystemManager.getLocalRecordFileFullPath( recordName ) );
        if ( file.exists() ) {
            file.delete();
        }
    }

    public static boolean localEncryptData( String recordData, String localRecordName ) {
        String localEncryptedCopyPath = FileSystemManager.getLocalRecordFileFullPath( localRecordName );
        boolean success = encryptData( recordData, AppProperties.getUsername(), localEncryptedCopyPath );
        if ( AppProperties.isBackupDirConfigured() ) {
            encryptData( recordData, AppProperties.getUsername(),
                    FileSystemManager.getLocalRecordBackupFileFullPath( localRecordName ) );
        }
        return success;
    }

    public static boolean localEncryptTestData( String localRecordName ) {
        String localEncryptedCopyPath = FileSystemManager.createTestExportFileFullPath( localRecordName );
        return encryptData( "test data", AppProperties.getUsername(), localEncryptedCopyPath );
    }

    public static boolean encryptData( String recordData, String gpgUser, String exportFilePath ) {

        // 1. create a temporary file
        String inputTmpFilePath = FileSystemManager.createTempFileFullPath();
        File tmpFile = FileSystemManager.createFileForData( inputTmpFilePath, recordData );

        // 2. create export file
        File encryptedFile = new File( exportFilePath );
        if ( encryptedFile.exists() ) {
            encryptedFile.delete();
        }

        FileSystemManager.executeEncrypt( gpgUser, inputTmpFilePath, exportFilePath );

        if ( !encryptedFile.exists() ) {
            throw new RuntimeException( "Could not write new record at " + exportFilePath
                    + "; failed to create the encrypted file!" );
        }

        // 3. delete temporary file
        tmpFile.delete();

        return true;
    }

    public static File decryptImportFile( File encryptedImportFile ) {
        String decryptedImportFilePath = FileSystemManager.createTempFileFullPath();
        File decryptedFile = new File( decryptedImportFilePath );
        if ( decryptedFile.exists() ) {
            decryptedFile.delete();
        }

        FileSystemManager.executeDecrypt( AppProperties.getUsername(), encryptedImportFile.getAbsolutePath(),
                decryptedImportFilePath, AppProperties.getPassPhrase() );

        if ( decryptedFile.exists() ) {
            return decryptedFile;
        } else {
            return null;
        }
    }

    public static StringBuffer getDecryptedLocalData( String localRecordName ) {
        String localEncryptedCopyPath = FileSystemManager.getLocalRecordFileFullPath( localRecordName );
        return getDecryptedData( localEncryptedCopyPath );
    }

    public static StringBuffer getDecryptedTestData( String testRecordName ) {
        String encryptedTestRecordPath = FileSystemManager.createTestExportFileFullPath( testRecordName );
        return getDecryptedData( encryptedTestRecordPath );
    }

    private static StringBuffer getDecryptedData( String encryptedFilePath ) {
        File localEncryptedCopyFile = new File( encryptedFilePath );
        if ( !localEncryptedCopyFile.exists() ) {
            throw new RuntimeException( "Could not find locally encrypted file to decrypt: "
                    + localEncryptedCopyFile.getAbsolutePath() );
        }

        StringBuffer recordData = new StringBuffer();
        // 1. create a temporary file
        String tmpFilePath = FileSystemManager.createTempFileFullPath();
        File tmpFile = new File( tmpFilePath );

        FileSystemManager.executeDecrypt( AppProperties.getUsername(), encryptedFilePath,
                tmpFilePath, AppProperties.getPassPhrase() );

        // 2. suck in temporary file contents
        FileSystemManager.fillStringBufferFromFile( tmpFile, recordData );

        // 3. delete temporary file
        tmpFile.delete();

        return recordData;
    }

    public static boolean importPublicKey( File selectedFile ) {
        return FileSystemManager.executeImportKey( selectedFile.getAbsolutePath() );
    }

    public static boolean deletePublicKey( String keyId ) {
        return FileSystemManager.executeDeletePublicKey( keyId );
    }

    public static void reinitKeystoreDir() {
        FileSystemManager.reinitKeystoreDir();
    }

    public static boolean signPublicKey( String passphrase, String keyId ) {
        return FileSystemManager.executeSignKey( passphrase, keyId );
    }

    public static void createLocalKeyPair( String username, String passphrase ) {
        FileSystemManager.createLocalKeyPair( username, passphrase );
    }

    public static boolean testExportToServer() {
        // 1. encrypt test file
        String testRecName = "T_" + new Date().getTime();
        try {
            ExportManager.exportTestRecordLocally( testRecName );
        } catch ( Exception e ) {
            return false;
        }

        File exportedTestRec = new File( FileSystemManager.createTestExportFileFullPath( testRecName ) );
        boolean success = exportedTestRec.exists();
        exportedTestRec.delete();

        return success;
    }

    public static boolean testLocalEncrypt() {
        // 1. encrypt test file
        String testRecName = "T_" + new Date().getTime();
        try {
            localEncryptTestData( testRecName );
        } catch ( Exception e ) {
            return false;
        }

        File encryptedTestRec = new File( FileSystemManager.createTestExportFileFullPath( testRecName ) );
        boolean success = encryptedTestRec.exists();
        encryptedTestRec.delete();

        return success;
    }

    public static boolean testLocalDecrypt() {
        // 1. encrypt test file
        String testRecName = "T_" + new Date().getTime();
        try {
            localEncryptTestData( testRecName );
        } catch ( Exception e ) {
            return false;
        }

        File encryptedTestRec = new File( FileSystemManager.createTestExportFileFullPath( testRecName ) );
        boolean success = encryptedTestRec.exists();
        if ( !success ) {
            encryptedTestRec.delete();
            return false;
        }

        // 2. decrypt test file
        StringBuffer data = getDecryptedTestData( testRecName );
        encryptedTestRec.delete();
        return data != null && data.length() > 0;
    }

    public static void exportLocalPublicKey( String userName ) {
        FileSystemManager.exportLocalUserPublicKey( userName, AppProperties.getLocalPublicKeyFilePath() );
    }

}
