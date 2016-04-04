package maqs.ehs.form;

import maqs.ehs.patient.ImportManger;
import maqs.ehs.patient.ImportResults;
import maqs.ehs.patient.ImportedLine;
import maqs.ehs.util.AppProperties;
import maqs.ehs.util.SecurityUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;

public class FileChooserFactory {

    public static void createOutgoingDirectoryChooser() {
        File outgoingDir = new File( AppProperties.getOutgoingDirPath() );
        MedicsFileChooserDialog.createFileChooser(
                "Change Outgoing Directory",
                new ActionListener() {
                    public void actionPerformed( ActionEvent e ) {
                        JFileChooser chooser = ( JFileChooser ) e.getSource();
                        MedicsFileChooserDialog chooserDialog = ( MedicsFileChooserDialog ) chooser.getRootPane().getParent();

                        if ( JFileChooser.APPROVE_SELECTION.equals( e.getActionCommand() ) ) {
                            File selectedFile = chooser.getSelectedFile();
                            AppProperties.setOutgoingDirPath( selectedFile.getAbsolutePath() );
                            chooserDialog.dispose();

                            MedicsDialog.createNotificationInstance( "Outgoing Directory",
                                    "You have set the outgoing directory to:\n '" + selectedFile.getAbsolutePath() + "'" );
                        }

                        if ( JFileChooser.CANCEL_SELECTION.equals( e.getActionCommand() ) ) {
                            chooserDialog.dispose();
                        }
                    }
                },
                JFileChooser.DIRECTORIES_ONLY,
                outgoingDir.exists() ? outgoingDir : null );
    }

    public static void createBackupDirectoryChooser() {
        File backupDir = new File( AppProperties.getBackupDirPath() );
        MedicsFileChooserDialog.createFileChooser(
                "Change Backup Directory",
                new ActionListener() {
                    public void actionPerformed( ActionEvent e ) {
                        JFileChooser chooser = ( JFileChooser ) e.getSource();
                        MedicsFileChooserDialog chooserDialog = ( MedicsFileChooserDialog ) chooser.getRootPane().getParent();

                        if ( JFileChooser.APPROVE_SELECTION.equals( e.getActionCommand() ) ) {
                            File selectedFile = chooser.getSelectedFile();
                            AppProperties.setBackupDirPath( selectedFile.getAbsolutePath() );
                            chooserDialog.dispose();

                            MedicsDialog.createNotificationInstance( "Backup Directory",
                                    "You have set the backup directory to:\n '" + selectedFile.getAbsolutePath() + "'" );
                        }

                        if ( JFileChooser.CANCEL_SELECTION.equals( e.getActionCommand() ) ) {
                            chooserDialog.dispose();
                        }
                    }
                },
                JFileChooser.DIRECTORIES_ONLY,
                backupDir.exists() ? backupDir : null );
    }

    public static void createImportRecordsChooser( final MedicsUI parentForm ) {
        File currentDirectory = new File( "./../" );
        MedicsFileChooserDialog.createFileChooser(
                "Select a File to Import",
                new ActionListener() {
                    public void actionPerformed( ActionEvent e ) {
                        JFileChooser chooser = ( JFileChooser ) e.getSource();
                        MedicsFileChooserDialog chooserDialog = ( MedicsFileChooserDialog ) chooser.getRootPane().getParent();

                        if ( JFileChooser.APPROVE_SELECTION.equals( e.getActionCommand() ) ) {
                            File selectedFile = chooser.getSelectedFile();
                            ImportResults importResults = ImportManger.processImportFile( selectedFile );
                            if ( !importResults.hasErrors() ) {
                                parentForm.listPatients();
                                chooserDialog.dispose();
                            } else {
                                StringBuffer errorPanelText = new StringBuffer();
                                String topText = "Some of the import lines failed to completely process. \n\nImport Errors:";
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

                                MedicsDialog.createScrollableDetailNotificationInstance( "Import Errors", topText, errorPanelText.toString() );
                                parentForm.listPatients();
                                chooserDialog.dispose();
                            }
                        }

                        if ( JFileChooser.CANCEL_SELECTION.equals( e.getActionCommand() ) ) {
                            chooserDialog.dispose();
                        }
                    }
                },
                JFileChooser.FILES_ONLY,
                currentDirectory );
    }


    public static void createImportPublicKeyChooser( final Installation parentForm ) {
        File currentDirectory = new File( "./../" );
        MedicsFileChooserDialog.createFileChooser(
                "Import Server Public Key",
                new ActionListener() {
                    public void actionPerformed( ActionEvent e ) {
                        JFileChooser chooser = ( JFileChooser ) e.getSource();
                        MedicsFileChooserDialog chooserDialog = ( MedicsFileChooserDialog ) chooser.getRootPane().getParent();

                        if ( JFileChooser.CANCEL_SELECTION.equals( e.getActionCommand() ) ) {
                            chooserDialog.dispose();
                        }

                        if ( JFileChooser.APPROVE_SELECTION.equals( e.getActionCommand() ) ) {
                            File selectedFile = chooser.getSelectedFile();

                            // 1.
                            if ( !SecurityUtil.importPublicKey( selectedFile ) ) {
                                MedicsDialog.createNotificationInstance( "Import Error", "The selected key failed to imported. Please select another to import." );
                                return;
                            }

                            // 2.
                            if ( !SecurityUtil.signPublicKey( AppProperties.getPassPhrase(), AppProperties.getServerPublicKeyId() ) ) {
                                MedicsDialog.createNotificationInstance( "Import Error", "Failed to sign server public key with local private key! " +
                                        "Call your administrator for assistance." );
                            }

                            parentForm.importedPublicKey();

                            chooserDialog.dispose();
                        }
                    }
                },
                JFileChooser.FILES_ONLY,
                currentDirectory );
    }


}
