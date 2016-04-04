package maqs.ehs.form;

import maqs.ehs.patient.*;
import maqs.ehs.util.AppProperties;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Iterator;

public class MenuActionListener implements ActionListener, CallBackPerformer {

    private MedicsUI parentFrame;

    public MenuActionListener( MedicsUI _parentFrame ) {
        parentFrame = _parentFrame;
    }

    public void actionPerformed( ActionEvent e ) {

        if ( e.getSource() instanceof JMenuItem ) {
            JMenuItem item = ( JMenuItem ) e.getSource();
            if ( "HelpFile".equals( item.getName() ) ) {
                String fileName = e.getActionCommand();
                File filePath = new File( AppProperties.getDocDirPath() + "/" + fileName );
                MedicsPdfViewer viewer = new MedicsPdfViewer();
                viewer.init( filePath );
            }
        }

        if ( "Quit".equals( e.getActionCommand() ) ) {
            if ( parentFrame.isWorkingWithPatient() ) {
                MedicsDialog.createOkCancelInstance( e.getActionCommand(), "Are you sure you want to quit? " +
                        "Any unsaved data will be lost.", PatientAction.QUIT, this );
            } else {
                MedicsDialog.createOkCancelInstance( e.getActionCommand(), "Are you sure you want to quit?",
                        PatientAction.QUIT, this );
            }
        }

        // Records -------------------

        if ( "New Record".equals( e.getActionCommand() ) ) {
            if ( parentFrame.isWorkingWithPatient() ) {
                MedicsDialog.createOkCancelInstance( e.getActionCommand(), "Are you sure you want to leave this record? All unsaved data will be lost.",
                        PatientAction.NEW, this );
            } else {
                handleNewRecord();
            }
        }

        if ( "Edit Record".equals( e.getActionCommand() ) ) {
            if ( parentFrame.isWorkingWithPatient() ) {
                MedicsDialog.createOkCancelInstance( e.getActionCommand(), "Are you sure you want to leave this record? All unsaved data will be lost.",
                        PatientAction.EDIT, this );
            } else {
                handleEditRecord();
            }
        }

        if ( "Save Record".equals( e.getActionCommand() ) ) {
            MedicsDialog.createOkCancelInstance( e.getActionCommand(), "Are you sure? Any existing file " +
                    "with the same name will be overwritten.", PatientAction.SAVE, this );
        }

        if ( "Print Record".equals( e.getActionCommand() ) ) {
            parentFrame.printRecord();
        }

        // Tools -------------------

        if ( "Import Records".equals( e.getActionCommand() ) ) {
            if ( parentFrame.isWorkingWithPatient() ) {
                MedicsDialog.createOkCancelInstance( e.getActionCommand(), "Are you sure you want to leave this record? All unsaved data " +
                        "will be lost.",
                        PatientAction.IMPORT, this );
            } else {
                if ( handleImport() ) return;
            }
        }

        if ( "Batch Export Records".equals( e.getActionCommand() ) ) {
            handleBatchRecordExport( e.getActionCommand() );
        }

        if ( "Change Backup Directory".equals( e.getActionCommand() ) ) {
            FileChooserFactory.createBackupDirectoryChooser();
        }

        if ( "Change Outgoing Directory".equals( e.getActionCommand() ) ) {
            FileChooserFactory.createOutgoingDirectoryChooser();
        }

        if ( "Upload Public Key".equals( e.getActionCommand() ) ) {
            handlePublicKeyUpload();
        }

        if ( "Publish Exported Records".equals( e.getActionCommand() ) ) {
            handlePublishExportedRecords( e.getActionCommand() );
        }

        String aboutItem = "About " + AppProperties.getGetAppTitle();
        if ( aboutItem.equals( e.getActionCommand() ) ) {
            MedicsDialog.createNotificationInstance( e.getActionCommand(), "\n" + AppProperties.getGetAppTitle() + ", Version "
                    + AppProperties.getVersionNumber() + "\nBuild " + AppProperties.getBuildNumber() );
        }

        if ( "Remote Import".equals( e.getActionCommand() ) ) {
            doRemoteImport();
        }
    }

    private void doRemoteImport() {
        RemoteImporter remoteImporter = ImportManger.getRemoteImporter();
        ResultCollector errorCollector = new DefaultResultCollector();
        List<File> files = remoteImporter.importRemote( errorCollector, AppProperties.getImportInfo() );

        List<String> successful = new ArrayList<String>();

        if ( files == null ) {
            MedicsDialog.createNotificationInstance( "Import Remote Files", "No files were imported." );
            return;
        }
        for ( File file : files ) {

            ImportResults importResults = ImportManger.processImportFile( file );
            if ( !importResults.hasErrors() ) {
                MedicsDialog.createNotificationInstance( "Import Remote File", "Successfully imported remote file '" + file.getName() + "'" );
                remoteImporter.deleteRemoteFile( file.getName(), AppProperties.getImportInfo() );
            } else {
                StringBuffer errorPanelText = new StringBuffer();
                String topText = "Remote file '" + file.getName() + "' has failed to completely process. \n\nImport Errors:";
                errorPanelText.append( importResults.getGlobalImportError() );
                for ( ImportedLine line : importResults.getImportedLines() ) {
                    if ( line.getErrors().isEmpty() ) {
                        continue;
                    }
                    int lineRowNumber = line.getLineRowNumber();
                    errorPanelText.append( "Line " );
                    errorPanelText.append( lineRowNumber );
                    errorPanelText.append( ": " );
                    for ( Iterator itr = line.getErrors().iterator(); itr.hasNext(); ) {
                        String error = ( String ) itr.next();
                        errorPanelText.append( String.valueOf( error ) );
                        if ( itr.hasNext() ) {
                            errorPanelText.append( "; " );
                        }
                    }
                    errorPanelText.append( "\r\n" );
                }

                MedicsDialog.createScrollableDetailNotificationInstance( "Import Remote File Errors", topText, errorPanelText.toString() );
            }
        }

    }

    private void handlePublishExportedRecords( String actionCommand ) {
        int exportedFileCount = new File( AppProperties.getOutgoingDirPath() ).listFiles(
                new FileFilter() {
                    public boolean accept( File pathname ) {
                        return pathname.isFile();
                    }
                }
        ).length;

        MedicsDialog.createOkCancelInstance( actionCommand, "There are " + exportedFileCount + " exported record(s)" +
                " available for publishing. Are you sure you want to publish these records? " +
                "Doing so will remove these records from your outgoing folder.",
                PatientAction.PUBLISH_EXPORTED, this );
    }

    private void handlePublicKeyUpload() {
        List<String> errorList = new ArrayList<String>();
        boolean success = PgpKeyUploadHelper.doPgpUpload( errorList );
        String text;
        if ( success ) {
            text = "Successfully uploaded your public key.";
        } else {
            text = "Failed to successfully upload your public key. Errors:\n ";
            for ( String error : errorList ) {
                text += "\n" + error;
            }
        }
        MedicsDialog.createNotificationInstance( AppProperties.getAppTitleFull(), text );

        if ( success ) {
            AppProperties.setPgpKeyUploaded();
        }
    }

    private void handleBatchRecordExport( String actionCommand ) {
        Collection<String> recordsWithErrors = new ArrayList<String>();

        if ( !ExportManager.exportAllowed( recordsWithErrors ) ) {
            // errors were found, do not permit export

            StringBuffer errorMsg = new StringBuffer();
            for ( String error : recordsWithErrors ) {
                errorMsg.append( error );
                errorMsg.append( "\n" );
            }

            String topText = "You may not perform a batch record export if there are some records with errors. Please export " +
                    "record individually, or edit and repair the following records with errors prior to export:";

            MedicsDialog.createScrollableDetailNotificationInstance( "Export Errors", topText, errorMsg.toString() );
        } else {
            // no errors were found, permit export
            if ( parentFrame.isWorkingWithPatient() ) {
                MedicsDialog.createOkCancelInstance( actionCommand, "To export records, you must leave this current record. Any unsaved data " +
                        "will be lost. You will also be unable to edit exported records.",
                        PatientAction.BATCH_EXPORT, this );
            } else {
                MedicsDialog.createOkCancelInstance( actionCommand, "There are " + PatientManager.getExportableRecordsCount() + " exportable record(s). " +
                        "Are you sure you want to export all records? You will be unable to edit these records.",
                        PatientAction.BATCH_EXPORT, this );
            }
        }
    }

    private void handleEditRecord() {
        parentFrame.clearPatient();
        parentFrame.listPatients();
    }

    private void handleNewRecord() {
        parentFrame.createNewPatient();
    }

    private boolean handleImport() {
        File outgoingDir = new File( AppProperties.getOutgoingDirPath() );
        if ( !outgoingDir.exists() || !outgoingDir.isDirectory() ) {
            MedicsDialog.createNotificationInstance( AppProperties.getAppTitle(), "The current outgoing directory '"
                    + outgoingDir.getAbsolutePath() + "' is invalid. You must change it to continue." );
            FileChooserFactory.createOutgoingDirectoryChooser();
            return true;
        }

        parentFrame.clearPatient();
        FileChooserFactory.createImportRecordsChooser( parentFrame );
        return false;
    }

    private boolean handleBatchExport() {
        int exportedRecordCount = ExportManager.doBatchExport();
        MedicsDialog.createNotificationInstance( "Batch Export", "Batch exported " + exportedRecordCount + " records." );

        if ( AppProperties.isRemoteExportEnabled() ) {
            RemotePublisher publisher = new FtpRemotePublisher();
            boolean isReachable = publisher.isConnectionAvailable(AppProperties.getExportDropInfo() );
            if ( isReachable ) {
                File[] unpublishedExportedRecords = new File( AppProperties.getOutgoingDirPath() ).listFiles( 
                new FileFilter(){
                    public boolean accept( File pathname ) {
                        return pathname.isFile();
                    }
                } );
                if ( unpublishedExportedRecords.length > 0 ) {
                    String list = "";
                    for ( File unpublishedRecord : unpublishedExportedRecords ) {
                        list += unpublishedRecord.getName() + "\n";                        
                    }
                    MedicsDialog.createScrollableOkCancelInstance( "Publish Exported Records", "An internet connection was detected, and there are some " +
                            "unpublished records. Would you like to publish them remotely?", list,
                            PatientAction.PUBLISH_EXPORTED, this );
                }
            }
        }

        parentFrame.activateHomePane();
        return true;
    }

    public void performCallback( PatientAction action ) {
        if ( action.isSave() ) {
            handleSavePatient();
        }
        if ( action.isNew() ) {
            handleNewRecord();
        }
        if ( action.isQuit() ) {
            handleQuit();
        }
        if ( action.isImport() ) {
            handleImport();
        }
        if ( action.isEdit() ) {
            handleEditRecord();
        }
        if ( action.isBatchExport() ) {
            handleBatchExport();
        }
        if ( action.isPublishExported() ) {
            handlePublishExported();
        }
    }

    private void handlePublishExported() {
        ResultCollector collector = new DefaultResultCollector();
        int publishedCount = ExportManager.publishExportedRecords( collector, true );
        if ( collector.hasResultsWithHigherSeverityThan( ResultType.WARNING ) ) {
            String explanationText;
            String detailText = "";
            for ( ProcessingResult result : collector.getResultsWithHigherSeverityThan( ResultType.WARNING ) ) {
                detailText += ( result.getDetail() + "\n" );
            }
            if ( publishedCount > 0 ) {
                explanationText = "Though " + publishedCount + " records were published, some records were not successfully published. See details below: ";
            } else {
                explanationText = "No records were successfully publshed. See details below: ";
            }
            MedicsDialog.createScrollableDetailNotificationInstance( "Published Records", explanationText, detailText );
        } else {
            String detailText = "";
            for ( ProcessingResult result : collector.getResultsOfSeverity( ResultType.INFO ) ) {
                detailText += ( result.getDetail() + "\n" );
            }
            MedicsDialog.createScrollableDetailNotificationInstance( "Published Records", "Published " + publishedCount + " exported records: ", detailText );
        }
        parentFrame.activateHomePane();
    }

    private void handleQuit() {
        parentFrame.quit();
    }

    private void handleSavePatient() {
        parentFrame.savePatient();
    }
}
