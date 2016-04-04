package maqs.ehs.form;

import maqs.ehs.patient.PatientManager;
import maqs.ehs.patient.ExportManager;
import maqs.ehs.util.AppProperties;
import maqs.ehs.util.FileSystemManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class PtListListener implements ActionListener, CallBackPerformer {

    private String recordName;
    private MedicsUI parentForm;

    public void actionPerformed( ActionEvent e ) {

        if ( "Edit".equals( e.getActionCommand() ) ) {
            parentForm.editPatient( recordName );
        }

        if ( "Delete".equals( e.getActionCommand() ) ) {
            MedicsDialog.createOkCancelInstance( e.getActionCommand(), "Are you sure you want to delete this record?",
                    PatientAction.DELETE, this );
        }

        if ( "Export".equals( e.getActionCommand() ) ) {
            MedicsDialog.createOkCancelInstance( e.getActionCommand(), "Are you sure you want to export this record?",
                    PatientAction.EXPORT, this );
        }

    }

    public void performCallback( PatientAction action ) {
        
        // delete
        if ( action.isDelete() ) {
            PatientManager.delete( recordName );
            parentForm.listPatients();
        }

        // export
        if ( action.isExport() ) {
            boolean exportSuccessful = PatientManager.export( recordName );

            String exportedFilePath = FileSystemManager.createExportFileFullPath( recordName );
            File exportedFile = new File( exportedFilePath );
            exportSuccessful = exportSuccessful && exportedFile.exists();

            if ( exportSuccessful ) {

                MedicsDialog.createNotificationInstance( "Export", "Record '" + recordName + "' successfully exported." );

                if ( AppProperties.isRemoteExportEnabled() ) {
                    RemotePublisher publisher = new FtpRemotePublisher();
                    boolean isReachable = publisher.isConnectionAvailable(AppProperties.getExportDropInfo() );
                    if ( isReachable ) {
                        MedicsDialog.createOkCancelInstance( "Publish Exported Records", "An internet connection was detected. " +
                                "Would you like to publish this exported record?", PatientAction.PUBLISH_EXPORTED, this );
                    }
                }

            } else {
                MedicsDialog.createNotificationInstance( "Export Error", "Record '" + recordName + "' failed to be exported." );
            }

            parentForm.listPatients();
        }

        // publish
        if ( action.isPublishExported() ) {
            String exportedFilePath = FileSystemManager.createExportFileFullPath( recordName );
            File exportedFile = new File( exportedFilePath );
            ResultCollector collector = new DefaultResultCollector();
            ExportManager.publishFile( exportedFile, collector );
            if ( !collector.hasResultsWithHigherSeverityThan( ResultType.INFO ) ) {
                MedicsDialog.createNotificationInstance( "Publish Exported Records", "Successfully published exported record '" + exportedFilePath + "'" );
            } else {
                String errors = "";
                for ( ProcessingResult result : collector.getResultsWithHigherSeverityThan( ResultType.INFO ) ) {
                    errors += result.getDetail() + "\n";
                }
                MedicsDialog.createScrollableDetailNotificationInstance( "Publish Exported Records Error", "Failed to publish exported record '" + exportedFilePath + "' due to the " +
                        "following errors:" , errors );
            }
        }
    }

    public String getRecordName() {
        return recordName;
    }

    public void setRecordName( String recordName ) {
        this.recordName = recordName;
    }


    public MedicsUI getParentForm() {
        return parentForm;
    }

    public void setParentForm( MedicsUI parentForm ) {
        this.parentForm = parentForm;
    }
}
