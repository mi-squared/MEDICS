package maqs.ehs.patient;

import maqs.ehs.form.*;
import maqs.ehs.util.AppProperties;
import maqs.ehs.util.FileSystemManager;
import maqs.ehs.util.SecurityUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ExportManager {

    public static int doBatchExport() {
        File workDir = new File( AppProperties.getWorkDirPath() );
        File[] files = workDir.listFiles();

        if ( files.length < 1 ) {
            return 0;
        }

        if ( AppProperties.isBatchExportModeDefault() ) {
            return doDefaultBatchExport( files );
        } else if ( AppProperties.isBatchExportModeConcat() ) {
            return doConcatBatchExport( files );
        } else if ( AppProperties.isBatchExportModeZip() ) {
            return doZipBatchExport( files );
        } else {
            throw new RuntimeException( "Unknown batch export mode!" );
        }
    }

    public static boolean exportAllowed( Collection<String> recordsWithErrors ) {
        File workDir = new File( AppProperties.getWorkDirPath() );
        File[] files = workDir.listFiles();

        for ( File file : files ) {
            String recordName = file.getName();
            if ( !PatientManager.isComplete( recordName ) ) {
                recordsWithErrors.add( recordName );
            }
        }

        return recordsWithErrors.size() <= 0;
    }

    static int doDefaultBatchExport( File[] files ) {
        int exportedRecordCount = 0;
        for ( File file : files ) {
            String recordName = file.getName();
            PatientManager.export( recordName );
            exportedRecordCount++;
        }
        return exportedRecordCount;
    }

    static int doConcatBatchExport( File[] files ) {
        int exportedRecordCount = 0;

        StringBuffer concatenatedData = new StringBuffer();
        List<String> exportedRecords = new ArrayList<String>();
        for ( File file : files ) {
            String recordName = file.getName();
            StringBuffer decryptedData = SecurityUtil.getDecryptedLocalData( recordName );
            concatenatedData.append( decryptedData );
            exportedRecords.add( recordName );
            exportedRecordCount++;
        }

        String batchFileName = getBatchName();
        exportEncryptedData( concatenatedData.toString(), batchFileName );

        // delete local records and backups if configured
        for ( String exportedRecord : exportedRecords ) {
            PatientManager.delete( exportedRecord );
            if ( AppProperties.isBackupDirConfigured() ) {
                String recordBackupFileFullPath = FileSystemManager.getLocalRecordBackupFileFullPath( exportedRecord );
                new File( recordBackupFileFullPath ).delete();
            }
        }

        return exportedRecordCount;
    }

    private static String getBatchName() {
        SimpleDateFormat sdf = new SimpleDateFormat( "MM-dd-yyyy-hh-mm-ss" );
        return "batch-" + sdf.format( new Date() );
    }

    static int doZipBatchExport( File[] files ) {
        int exportedRecordCount = 0;

        // These are the files to include in the ZIP file
        byte[] buf = new byte[1024];

        try {
            // Create the ZIP file
            String outFilename = getBatchName() + ".zip";
            File fullZipPath = new File( AppProperties.getOutgoingDirPath(), outFilename );
            ZipOutputStream out = new ZipOutputStream( new FileOutputStream( fullZipPath ) );

            // Compress the files
            for ( File file : files ) {

                exportRecordToOutgoingFolder( file.getName() );
                String exportedFilePath = FileSystemManager.createExportFileFullPath( file.getName() );
                File exportedFile = new File( exportedFilePath );
                if ( !exportedFile.exists() ) {
                    continue;
                }

                out.putNextEntry( new ZipEntry( exportedFile.getName() ) );

                // Transfer bytes from the file to the ZIP file
                FileInputStream in = new FileInputStream( exportedFile );
                int len;
                while ( ( len = in.read( buf ) ) > 0 ) {
                    out.write( buf, 0, len );
                }

                // Complete the entry
                out.closeEntry();
                in.close();
                exportedRecordCount++;

                // delete local record
                PatientManager.delete( file.getName() );

                // remove the exported file, since its been included in the zip
                exportedFile.delete();
            }

            // Complete the ZIP file
            out.close();
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }

        return exportedRecordCount;
    }

    public static boolean exportRecord( String recordName ) {
        boolean localRecordExportSuccess = ExportManager.exportRecordToOutgoingFolder( recordName );
        if ( localRecordExportSuccess ) {
            // only remove the local record if local record export is a success
            PatientManager.delete( recordName );
        }
        return localRecordExportSuccess;
    }

    static boolean exportRecordToOutgoingFolder( String recordName ) {
        StringBuffer recordData = SecurityUtil.getDecryptedLocalData( recordName );
        return exportEncryptedData( recordData.toString(), recordName );
    }

    public static boolean exportTestRecordLocally( String recordName ) {
        String exportEncryptedCopyPath = FileSystemManager.createTestExportFileFullPath( recordName );
        return SecurityUtil.encryptData( "test data", AppProperties.getServerPublicKeyId(), exportEncryptedCopyPath );
    }

    static boolean exportEncryptedData( String recordData, String localRecordName ) {
        String exportEncryptedCopyPath = FileSystemManager.createExportFileFullPath( localRecordName );
        boolean success = SecurityUtil.encryptData( recordData, AppProperties.getServerPublicKeyId(), exportEncryptedCopyPath );
        if ( AppProperties.isBackupDirConfigured() ) {
            String recordBackupFileFullPath = FileSystemManager.getLocalRecordBackupFileFullPath( localRecordName );
            File file = new File( recordBackupFileFullPath );
            FileSystemManager.deleteFile( file );
            success = !file.exists();
        }
        return success;
    }

    /**
     * Only returns true if all records correctly export
     *
     * @param exportErrors
     * @return
     */
    public static int publishExportedRecords( ResultCollector exportErrors, boolean removeFileOnSuccess ) {
        File[] exportedRecords = ( new File( AppProperties.getOutgoingDirPath() ) ).listFiles(
                new FileFilter() {
                    public boolean accept( File pathname ) {
                        return pathname.isFile();
                    }
                }
        );
        int successCount = 0;

        for ( File exportedRecord : exportedRecords ) {
            // for each exported record, export
            ResultCollector collector = new DefaultResultCollector();
            publishFile( exportedRecord, collector );

            if ( collector.hasResultsWithHigherSeverityThan( ResultType.INFO ) ) {
                for ( ProcessingResult result : collector.getResultsWithHigherSeverityThan( ResultType.INFO ) ) {
                    exportErrors.addResult( result );
                }
            } else {
                successCount++;
                for ( ProcessingResult result : collector.getResultsOfSeverity( ResultType.INFO ) ) {
                    exportErrors.addResult( result );
                }
            }
        }

        return successCount;
    }

    public static boolean publishFile( File file, ResultCollector exportErrors ) {
        // for each exported record, export
        RemotePublisher remotePublisher = getRemotePublisher();
        if ( remotePublisher == null ) {
            exportErrors.addResult( new ProcessingResult( ResultType.FATAL, "", "Invalid setup; please contact your administrator." ) );
            return false;
        }

        remotePublisher.publishRemote( file, exportErrors, AppProperties.getExportDropInfo(), "incoming" );
        boolean success = !exportErrors.hasResultsWithHigherSeverityThan( ResultType.INFO );

        // remove outgoing export file on successful publish
        if ( success ) {
            file.delete();
        }
        return success;
    }

    public static RemotePublisher getRemotePublisher() {
        if ( AppProperties.getRemoteExportMethod().isFtp() ) {
            return new FtpRemotePublisher();
        }
        if ( AppProperties.getRemoteExportMethod().isMail() ) {
            return new EmailRemotePublisherImpl();
        }
        return null;
    }

    public static void main( String s[] ) {
        System.out.println( publishExportedRecords( new DefaultResultCollector(), true ) );
    }
}
