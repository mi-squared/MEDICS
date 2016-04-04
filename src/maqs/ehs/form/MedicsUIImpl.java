package maqs.ehs.form;

import maqs.ehs.patient.*;
import maqs.ehs.util.AppProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import com.sigilent.business.util.StringUtils;

public class MedicsUIImpl extends JFrame implements MedicsUI {
    private JPanel contentPane;
    private JPanel cardPanel;
    private JPanel defaultPanel;
    private JPanel patientListPanel;
    private JPanel patientPanel;

    private JTabbedPane tabbedPane;
    private JLabel statusLabel;
    private PatientRecord patient;
    private JLabel patientListText;
    private JPanel patientListPanelList;

    // menu items
    private JMenuItem savePtAction;
    private JMenuItem printAction;
    private JMenuItem newPtAction;
    private JMenuItem editPtAction;
    private JMenuItem quitAction;
    private JMenuItem importAction;
    private JMenuItem uploadPublicKey;
    private JMenuItem publishExportedRecords;
    private JMenuItem outgoingDirAction;
    private JMenuItem backupDirAction;
    private JMenuItem batchExportAction;

    private boolean isAlive;

    private MenuActionListener actionListener = new MenuActionListener( this );
    private boolean hasRemoteExportConnection;
    private boolean hasRemoteImportConnection;
    private ConnectionChecker checker;
    private JButton remoteImport;

    public MedicsUIImpl() {
        LoginDialog login = LoginDialog.createInstance( this );
    }

    public void init() {
        initSystem();

        initUI();

        if ( AppProperties.isRemoteExportEnabled() || AppProperties.isRemoteImportEnabled()  ) {
            checker = new ConnectionChecker();
            checker.start();
        }
    }

    private void initUI() {

        contentPane = new JPanel( new GridLayout( 1, 1 ) );
        contentPane.setInheritsPopupMenu( false );
        this.add( contentPane );
        setBackground( Color.WHITE );

        cardPanel = new JPanel( new CardLayout() );
        contentPane.add( cardPanel );

        // default panel
        initDefaultPanel();

        // patient list panel
        initPatientListPanel();

        // edit patient panel
        initPatientPanel();

        initWindowEvents();

        initMenu();

        setVisible( true );

        activateHomePane();

        setContentPane( cardPanel );

        setTitle( AppProperties.getAppTitleFull() );

        setMenuItemVisibility();

        setIconImage(  new ImageIcon( AppProperties.getIconImagePath() ).getImage() );
    }

    private void initWindowEvents() {
        setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
        addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                shutdown();
                dispose();
            }
        } );

        this.addWindowStateListener( new WindowStateListener() {
            public void windowStateChanged( WindowEvent e ) {
                if ( e.getNewState() != e.getOldState() && e.getNewState() == 0 ) {
                    e.getWindow().pack();
                }
            }
        } );

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName() );
        } catch ( Exception e ) {
        }
    }

    private void shutdown() {
        isAlive = false;
        if ( checker != null ) {
            checker.interrupt();
        }
    }

    private void initPatientPanel() {
        patientPanel = new JPanel( new BorderLayout( 0, 0 ) );

        tabbedPane = new JTabbedPane();
        tabbedPane.setEnabled( true );
        tabbedPane.setFont( new Font( "Arial", tabbedPane.getFont().getStyle(), 10 ) );
        tabbedPane.setTabLayoutPolicy( 0 );
        tabbedPane.setTabPlacement( 1 );

        patientPanel.add( tabbedPane, BorderLayout.CENTER );

        statusLabel = new JLabel();
        statusLabel.setText( "Status Panel" );
        statusLabel.setVerticalAlignment( 0 );
        statusLabel.setAlignmentX( Component.LEFT_ALIGNMENT );

        patientPanel.add( statusLabel, BorderLayout.SOUTH );

        cardPanel.add( "patient", patientPanel );
    }

    private void initPatientListPanel() {
        patientListPanel = new JPanel( new BorderLayout( 0, 0 ) );

        patientListText = new JLabel();
        patientListText.setText( "Please click on the 'Edit' button adjacent to the patient record to begin editing." );
        patientListPanel.add( patientListText, BorderLayout.NORTH );
        final JScrollPane scrollPane1 = new JScrollPane();
        patientListPanel.add( scrollPane1, BorderLayout.CENTER );
        patientListPanelList = new JPanel( new BoxLayout( patientListPanelList, BoxLayout.PAGE_AXIS ) );
        patientListPanelList.setLayout( new FlowLayout( FlowLayout.CENTER, 5, 5 ) );
        scrollPane1.setViewportView( patientListPanelList );
        cardPanel.add( "patientlist", patientListPanel );
    }

    private void initDefaultPanel() {
        defaultPanel = new JPanel();
        defaultPanel.setLayout( new BorderLayout( 0, 0 ) );

        File splashImage = new File( AppProperties.getSplashImagePath() );
        if ( splashImage.exists() ) {
            ImageIcon image = new ImageIcon( splashImage.getAbsolutePath() );
            JLabel imageLbl = new JLabel( image );
            imageLbl.setBounds( 0, 0, image.getIconWidth(), image.getIconHeight() );
            defaultPanel.setBackground( Color.WHITE );
            defaultPanel.add( imageLbl, BorderLayout.CENTER );
        }

        cardPanel.add( "default", defaultPanel );
    }

    private void initSystem() {
        PatientManager.init();
        // clear out old temp files
        File tempDir = new File( AppProperties.getTempDirPath() );
        for ( File tmp : tempDir.listFiles() ) {
            tmp.delete();
        }

        File incomingDir = new File ( AppProperties.getIncomingDirPath() );
        if ( !incomingDir.exists() ) {
            incomingDir.mkdir();
        }

        if ( AppProperties.isRemoteExportEnabled() ) {
            PgpKeyUploadHelper.handlePgpUpload();

            if ( !new File( AppProperties.getOutgoingDirPath() ).exists() ) {
                MedicsDialog.createNotificationInstance( "Invalid Outgoing Directory",
                        StringUtils.isEmpty( AppProperties.getOutgoingDirPath() ) ?
                                "You have not configured an outgoing directory. You must select on to continue." :
                                "The current outgoing directory '" + AppProperties.getOutgoingDirPath() + "' is invalid. You must change it to continue."
                );
                FileChooserFactory.createOutgoingDirectoryChooser();
                if ( !new File( AppProperties.getOutgoingDirPath() ).exists() ) {
                    MedicsDialog.createNotificationInstance( "Invalid Outgoing Directory", AppProperties.getAppTitle() + " cannot be started until you" +
                            " specify a valid outgoing directory. The program will quit now, but you will be prompted again on restart."
                    );
                    System.exit( 0 );
                }
            }
        }

        isAlive = true;
    }

    private void initMenu() {
        //Create the menu bar.
        JMenuBar menuBar = new JMenuBar();
        this.setJMenuBar( menuBar );

        // Records
        JMenu ptMenu = new JMenu( "Records" );
        ptMenu.setMnemonic( KeyEvent.VK_P );
        menuBar.add( ptMenu );

        newPtAction = new JMenuItem( "New Record", KeyEvent.VK_N );
        newPtAction.setAccelerator( KeyStroke.getKeyStroke(
                KeyEvent.VK_N, ActionEvent.CTRL_MASK ) );
        newPtAction.addActionListener( actionListener );
        ptMenu.add( newPtAction );

        editPtAction = new JMenuItem( "Edit Record", KeyEvent.VK_E );
        editPtAction.setAccelerator( KeyStroke.getKeyStroke(
                KeyEvent.VK_E, ActionEvent.CTRL_MASK ) );
        editPtAction.addActionListener( actionListener );
        ptMenu.add( editPtAction );

        ptMenu.addSeparator();

        savePtAction = new JMenuItem( "Save Record", KeyEvent.VK_S );
        savePtAction.setAccelerator( KeyStroke.getKeyStroke(
                KeyEvent.VK_S, ActionEvent.CTRL_MASK ) );
        savePtAction.addActionListener( actionListener );
        savePtAction.setEnabled( false );
        ptMenu.add( savePtAction );

        ptMenu.addSeparator();

        printAction = new JMenuItem( "Print Record", KeyEvent.VK_P );
        printAction.setAccelerator( KeyStroke.getKeyStroke(
                KeyEvent.VK_P, ActionEvent.CTRL_MASK ) );
        printAction.setEnabled( false );
        printAction.addActionListener( actionListener );

        ptMenu.add( printAction );

        ptMenu.addSeparator();

        quitAction = new JMenuItem( "Quit", KeyEvent.VK_Q );
        quitAction.setAccelerator( KeyStroke.getKeyStroke(
                KeyEvent.VK_Q, ActionEvent.CTRL_MASK ) );
        quitAction.addActionListener( actionListener );

        ptMenu.add( quitAction );

        // Tools
        JMenu toolsMenu = new JMenu( "Tools" );
        toolsMenu.setMnemonic( KeyEvent.CTRL_MASK );
        menuBar.add( toolsMenu );

        importAction = new JMenuItem( "Import Records" );
        importAction.addActionListener( actionListener );
        toolsMenu.add( importAction );

        if ( AppProperties.isBatchExportEnabled() ) {
            batchExportAction = new JMenuItem( "Batch Export Records" );
            batchExportAction.addActionListener( actionListener );
            toolsMenu.add( batchExportAction );
        }

        if ( AppProperties.isRemoteExportEnabled() ) {
            toolsMenu.addSeparator();
        }

        uploadPublicKey = new JMenuItem( "Upload Public Key" );
        uploadPublicKey.addActionListener( actionListener );
        toolsMenu.add( uploadPublicKey );

        publishExportedRecords = new JMenuItem( "Publish Exported Records" );
        publishExportedRecords.addActionListener( actionListener );
        toolsMenu.add( publishExportedRecords );

        toolsMenu.addSeparator();

        outgoingDirAction = new JMenuItem( "Change Outgoing Directory" );
        outgoingDirAction.addActionListener( actionListener );
        toolsMenu.add( outgoingDirAction );

        backupDirAction = new JMenuItem( "Change Backup Directory" );
        backupDirAction.addActionListener( actionListener );
        toolsMenu.add( backupDirAction );

        // About
        createHelpMenu( menuBar );
        JLabel spacer = new JLabel( "     " );
        spacer.setSize( 500, 1 );
        menuBar.add( spacer );

        // Invisible tab navigation shortcut
        JMenuItem nextTabAction = new JMenuItem( "", KeyEvent.VK_T );
        nextTabAction.setAccelerator( KeyStroke.getKeyStroke(
                KeyEvent.VK_T, ActionEvent.CTRL_MASK ) );
        nextTabAction.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                if ( isWorkingWithPatient() ) {
                    int totalTabs = tabbedPane.getTabCount();
                    int nextTab = tabbedPane.getSelectedIndex() + 1;
                    if ( nextTab >= totalTabs ) {
                        nextTab = 0;
                    }
                    tabbedPane.setSelectedIndex( nextTab );
                }
            }
        } );
        toolsMenu.add( nextTabAction );
        nextTabAction.setVisible( false );

        JMenuItem previousTabAction = new JMenuItem( "", KeyEvent.VK_T );
        previousTabAction.setAccelerator( KeyStroke.getKeyStroke(
                KeyEvent.VK_T, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK ) );
        previousTabAction.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                if ( isWorkingWithPatient() ) {
                    int totalTabs = tabbedPane.getTabCount();
                    int nextTab = tabbedPane.getSelectedIndex() - 1;
                    if ( nextTab < 0 ) {
                        nextTab = totalTabs - 1;
                    }
                    tabbedPane.setSelectedIndex( nextTab );
                }
            }
        } );
        toolsMenu.add( previousTabAction );
        previousTabAction.setEnabled( false );

        remoteImport = new JButton( "Remote Import" );
        remoteImport.addActionListener( actionListener );
        remoteImport.setEnabled( false );
        menuBar.add( remoteImport );
    }

    private void createHelpMenu( JMenuBar menuBar ) {
        JMenu aboutMenu = new JMenu( "Help" );
        aboutMenu.setMnemonic( KeyEvent.CTRL_MASK );
        menuBar.add( aboutMenu );

        JMenuItem aboutAction = new JMenuItem( "About " + AppProperties.getGetAppTitle(), KeyEvent.VK_A );
        aboutAction.setAccelerator( KeyStroke.getKeyStroke(
                KeyEvent.VK_A, ActionEvent.CTRL_MASK ) );
        aboutAction.addActionListener( actionListener );
        aboutMenu.add( aboutAction );

        // construct help file links
        File helpFiles = new File( AppProperties.getDocDirPath() );
        if ( !helpFiles.exists() || helpFiles.listFiles().length < 1 ) {
            return;
        }

        boolean addedSeparator = false;
        for ( File helpFile : helpFiles.listFiles() ) {
            if ( !helpFile.getName().toLowerCase().endsWith( ".pdf" ) ) {
                continue;
            }
            if ( !addedSeparator ) {
                aboutMenu.addSeparator();
                addedSeparator = true;
            }
            JMenuItem helpFileAction = new JMenuItem( helpFile.getName() );
            helpFileAction.setName( "HelpFile" );
            helpFileAction.addActionListener( actionListener );
            aboutMenu.add( helpFileAction );
        }
    }

    public void activateHomePane() {
        activateCard( "default" );
    }

    private void activateCard( String cardName ) {
        CardLayout layoutManager = ( CardLayout ) cardPanel.getLayout();
        layoutManager.show( cardPanel, cardName );
        setMenuItemVisibility();
    }

    public void listPatients() {
        setMenuItemVisibility();

        File outgoingDir = new File( AppProperties.getOutgoingDirPath() );

        if ( !outgoingDir.exists() || !outgoingDir.isDirectory() ) {
            MedicsDialog.createNotificationInstance( "Invalid Outgoing Directory", "The current outgoing directory '" + outgoingDir.getAbsolutePath()
                    + "' is invalid. You must change it to continue." );
            FileChooserFactory.createOutgoingDirectoryChooser();
            return;
        }


        activateHomePane();
        patientListPanelList.removeAll();
        activateCard( "patientlist" );

        File workDir = new File( AppProperties.getWorkDirPath() );
        File[] files = workDir.listFiles();

        if ( files.length > 0 ) {
            patientListText.setText( "Click the 'Edit' button adjacent to a record to begin editing. Error-free records can be exported via the 'Export' button." );
        } else {
            patientListText.setText( "There are no records in the database." );
        }

        FormHelper.populatePatientList( this, patientListPanelList, files );
    }

    public void editPatient( String recordName ) {
        String fileLoadErrorString = null;
        try {
            patient = PatientManager.read( recordName );
        } catch ( Exception e ) {
            fileLoadErrorString = e.getMessage();
        }
        if ( fileLoadErrorString == null ) {
            activateCard( "patient" );
            statusLabel.setText( "Working with record '" + recordName + "'" );
            renderPatient( false );
        } else {
            //
            MedicsDialog.createNotificationInstance( "Edit Patient Error", "Could not open record: " + fileLoadErrorString );
        }
    }

    public void printRecord() {
        PdfApi pdfApi = new PdfApi();
        pdfApi.setTargetRecord( patient );
        try {
            File file = pdfApi.work();
            pdfApi.setDeleteOnView( true );
            MedicsPdfViewer dialog = new MedicsPdfViewer();
            dialog.setCallback( pdfApi );
            dialog.init( file );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    public void maximizeView() {
        setExtendedState( getExtendedState() | Frame.MAXIMIZED_BOTH );
    }

    public boolean isWorkingWithPatient() {
        return patient != null;
    }

    public void clearPatient() {
        patient = null;
    }

    public void createNewPatient() {
        File outgoingDir = new File( AppProperties.getOutgoingDirPath() );

        if ( !outgoingDir.exists() || !outgoingDir.isDirectory() ) {
            MedicsDialog.createNotificationInstance( "Invalid Outgoing Directory", "The current outgoing directory '" + outgoingDir.getAbsolutePath()
                    + "' is invalid. You must change it to continue." );
            FileChooserFactory.createOutgoingDirectoryChooser();
        } else {
            statusLabel.setText( "New Record" );
            activateCard( "patient" );
            patient = PatientManager.createNew();
            renderPatient( true );
        }
    }

    public void quit() {
        shutdown();
        dispose();
    }

    public void savePatient() {
        ResultCollector result = new DefaultResultCollector();
        PatientManager.save( patient, cardPanel, result );

        if ( result.hasResultsWithHigherSeverityThan( ResultType.WARNING ) ) {

            StringBuffer errorMsg = new StringBuffer();
            for ( ProcessingResult error : result.getResultsWithHigherSeverityThan( ResultType.INFO ) ) {
                errorMsg.append( "(" );
                errorMsg.append( error.getResultType().getDescription() );
                errorMsg.append( ") " );
                errorMsg.append( error.getDetail() );
                errorMsg.append( "\n" );
            }

            MedicsDialog.createScrollableDetailNotificationInstance(
                    "Save Patient Errors",
                    "The record could not be saved, due to the following errors:",
                    errorMsg.toString() );

            statusLabel.setText( "Error saving patient record." );
        } else {

            StringBuffer errorMsg = new StringBuffer();
            List<ProcessingResult> resultsWithHigherSeverityThanInfo = result.getResultsWithHigherSeverityThan( ResultType.INFO );
            if ( !resultsWithHigherSeverityThanInfo.isEmpty() ) {
                for ( ProcessingResult error : resultsWithHigherSeverityThanInfo ) {
                    errorMsg.append( "(" );
                    errorMsg.append( error.getResultType().getDescription() );
                    errorMsg.append( ") " );
                    errorMsg.append( error.getDetail() );
                    errorMsg.append( "\n" );
                }

                MedicsDialog.createScrollableDetailNotificationInstance(
                        "Save Patient Warnings",
                        "The record was saved, but you must correct the following errors prior to record export:",
                        errorMsg.toString() );

            }
            Date date = new Date();
            DateFormat format = new SimpleDateFormat();
            statusLabel.setText( "Record '" + patient.getRecordName() + "' last saved on " + format.format( date ) );
        }

        renderPatient( false );
    }

    private void renderPatient( boolean isNew ) {
        setMenuItemVisibility();

        tabbedPane.removeAll();
        Collection<Field> list = patient.getFields();
        ResultCollector collector = new DefaultResultCollector();
        if ( !isNew ) {
            // only check for errors if the patient has been edited (i.e. not new)
            patient.collectResults( collector );
        }
        FormHelper.populateTabbedPane( tabbedPane, list, collector );
    }

    private void setMenuItemVisibility() {

        if ( isWorkingWithPatient() ) {
            savePtAction.setEnabled( true );
            printAction.setEnabled( true );
        } else {
            savePtAction.setEnabled( false );
            printAction.setEnabled( false );
        }

        if ( AppProperties.isRemoteExportEnabled() ) {
            uploadPublicKey.setEnabled( !AppProperties.isPgpKeyUploaded() );
        }  else {
            uploadPublicKey.setVisible( false );
        }

        updatePublishStatus();

        boolean isAllRecordsExportable = PatientManager.isAllRecordsExportable();
        batchExportAction.setEnabled( isAllRecordsExportable );
    }

    private void updatePublishStatus() {
        if ( AppProperties.isRemoteExportEnabled() ) {
            boolean hasExportedRecords = new File( AppProperties.getOutgoingDirPath() ).listFiles(
                    new FileFilter() {
                        public boolean accept( File pathname ) {
                            return pathname.isFile();
                        }
                    }
            ).length > 0;
            publishExportedRecords.setEnabled( hasRemoteExportConnection && hasExportedRecords && AppProperties.isRemoteExportEnabled() );
        } else {
            publishExportedRecords.setVisible( false );
            publishExportedRecords.setEnabled( false );
        }
    }

    private void updateImportStatus() {
        if ( AppProperties.isRemoteImportEnabled() ) {
            remoteImport.setEnabled( hasRemoteImportConnection );
        } else {
            remoteImport.setVisible( false );
        }
    }

    class ConnectionChecker extends Thread {
        private static final int PING_DELAY = 15000;
        private static final int IMPORT_CHECK_DELAY = 0;

        private long lastImportStatusCheck = 0;
        public void run() {
            while ( isAlive ) {
                try {
                    handlePublishStatus();

                    if ( lastImportStatusCheck == 0
                     || (System.currentTimeMillis() - lastImportStatusCheck) > IMPORT_CHECK_DELAY ) {
                        handleImportStatus();
                        lastImportStatusCheck = System.currentTimeMillis();
                    }
                    
                    // update every 15 seconds
                    Thread.sleep( PING_DELAY );
                } catch ( Exception e ) {
                    e.printStackTrace(  );
                }
            }
        }

        private void handleImportStatus() {
            RemoteImporter remotePublisher = ImportManger.getRemoteImporter();
            hasRemoteImportConnection = remotePublisher != null
                    && remotePublisher.isConnectionAvailable( AppProperties.getImportInfo() );
            updateImportStatus();
        }

        private void handlePublishStatus() {
            RemotePublisher remotePublisher = ExportManager.getRemotePublisher();
            hasRemoteExportConnection = remotePublisher != null
                    && remotePublisher.isConnectionAvailable( AppProperties.getExportDropInfo() );

            updatePublishStatus();
        }
    }

    public static void main( String s[] ) {
        MedicsUI theFrame = new MedicsUIImpl();
    }

}
