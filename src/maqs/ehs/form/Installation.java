package maqs.ehs.form;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.sigilent.business.util.StringUtils;
import maqs.ehs.util.AppProperties;
import maqs.ehs.util.FileSystemManager;
import maqs.ehs.util.SecurityUtil;
import maqs.ehs.util.UpgradeUtil;
import maqs.ehs.form.FileChooserFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class Installation extends JDialog {
    private JPanel contentPane;
    private JButton buttonNext;
    private JButton buttonCancel;
    private JPanel displayPanel;
    private JPanel fieldsCard;
    private JTextField username;
    private JTextField password;
    private JTextField publicKeyId;
    private JPanel done;

    private boolean isUpgrade = false;

    public Installation() {
        setTitle( "MEDICS Installation" );

        // check upgrade flag
        String isUpgradeOption = System.getProperty( "is.upgrade" );
        if ( !StringUtils.isEmpty( isUpgradeOption ) && "true".equals( isUpgradeOption ) ) {
            setTitle( "MEDICS Upgrade" );
            isUpgrade = true;
        }

        setContentPane( contentPane );
        setModal( true );
        getRootPane().setDefaultButton( buttonNext );

        buttonNext.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                onNext();
            }
        } );

        buttonCancel.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                onCancel();
            }
        } );

        // call onNext() when cross is clicked
        setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
        addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                onCancel();
            }
        } );

        // call onNext() on ESCAPE
        contentPane.registerKeyboardAction( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                onNext();
            }
        }, KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName() );
        } catch ( Exception e ) {
        }

        if ( isUpgrade ) {
            doUpgrade();
        } else {
            CardLayout layout = ( CardLayout ) displayPanel.getLayout();
            layout.show( displayPanel, "fieldsCard" );
        }
    }

    private void doUpgrade() {
        File selectedFile = new File( "../" );
        File thisDir = new File( "." );
        UpgradeUtil.upgradeInstallationDir( selectedFile, thisDir );
        completedUpgrade();
    }

    private void onCancel() {
        dispose();
    }

    private void onNext() {

        // Validation
        if ( StringUtils.isEmpty( username.getText() )
                || StringUtils.isEmpty( password.getText() )
                || StringUtils.isEmpty( publicKeyId.getText() ) ) {
            MedicsDialog.createNotificationInstance( "Setup Error", "Some fields are empty. Please supply data for all fields." );
            return;
        }

        // Set local username
        AppProperties.setUsername( username.getText() );

        // Set local passphrase
        AppProperties.setPassPhrase( password.getText() );

        // Set import key id
        AppProperties.setServerPublicKeyId( publicKeyId.getText() );

        // Reinit keystore dir
        SecurityUtil.reinitKeystoreDir();

        // Setup directories
        setupCleanDirectories();

        // Create local keypair
        SecurityUtil.createLocalKeyPair( username.getText(), password.getText() );

        // Export the local public key
        SecurityUtil.exportLocalPublicKey( username.getText() );

        // Import key
        MedicsDialog.createNotificationInstance( AppProperties.getAppTitle() + " Installation", "Please select the Public Key that matches the SERVER Public Key ID." );
        FileChooserFactory.createImportPublicKeyChooser( this );

        // Prompt for backup directory
        MedicsDialog.createNotificationInstance( AppProperties.getAppTitle() + " Installation", "Please choose a backup directory." );
        FileChooserFactory.createBackupDirectoryChooser();
    }

    private void setupCleanDirectories() {
        File tempDir = new File( AppProperties.getTempDirPath() );
        if ( tempDir.exists() ) {
            FileSystemManager.deleteDir( tempDir );
        }
        tempDir.mkdir();

        File workDir = new File( AppProperties.getWorkDirPath() );
        if ( workDir.exists() ) {
            FileSystemManager.deleteDir( workDir );
        }
        workDir.mkdir();

        File incomingDir = new File( AppProperties.getIncomingDirPath() );
        if ( incomingDir.exists() ) {
            FileSystemManager.deleteDir( incomingDir );
        }
        incomingDir.mkdir();

        File outgoingDir = new File( AppProperties.getOutgoingDirPath() );
        if ( !outgoingDir.exists() ) {
            outgoingDir.mkdir();
        }

        File testDir = new File( AppProperties.getTestDir() );
        if ( testDir.exists() ) {
            FileSystemManager.deleteDir( testDir );
        }
        testDir.mkdir();
    }

    public static void main( String[] args ) {
        Installation dialog = new Installation();
        dialog.pack();
        dialog.setVisible( true );
        System.exit( 0 );
    }

    public void importedPublicKey() {
        if ( runTests() ) {
            createDonePanel();
        }
    }

    public void completedUpgrade() {
        createDonePanel();
    }

    private void createDonePanel() {
        CardLayout layout = ( CardLayout ) displayPanel.getLayout();
        layout.show( displayPanel, "done" );
        buttonNext.setEnabled( false );
        buttonNext.setVisible( false );
        buttonCancel.setText( "Done" );
    }

    private boolean runTests() {
        // Run public key encrypt test
        if ( !SecurityUtil.testExportToServer() ) {
            MedicsDialog.createNotificationInstance( "Setup Error", "Failed export using supplied public key! Please make sure the key you supplied " +
                    "matches the one for the public key you've imported." );
            return false;
        }

        // Run private key encrypt test
        if ( !SecurityUtil.testLocalEncrypt() ) {
            MedicsDialog.createNotificationInstance( "Setup Error", "Failed to perform local encrypt. Please call your administrator." );
            return false;
        }

        // Run private key decrypt test
        if ( !SecurityUtil.testLocalDecrypt() ) {
            MedicsDialog.createNotificationInstance( "Setup Error", "Failed to perform local decrypt. Please call your administrator." );
            return false;
        }

        return true;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout( new GridLayoutManager( 2, 1, new Insets( 10, 10, 10, 10 ), -1, -1 ) );
        final JPanel panel1 = new JPanel();
        panel1.setLayout( new GridLayoutManager( 1, 2, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        contentPane.add( panel1, new GridConstraints( 1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false ) );
        final Spacer spacer1 = new Spacer();
        panel1.add( spacer1, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false ) );
        final JPanel panel2 = new JPanel();
        panel2.setLayout( new GridLayoutManager( 1, 2, new Insets( 0, 0, 0, 0 ), -1, -1, true, false ) );
        panel1.add( panel2, new GridConstraints( 0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false ) );
        buttonNext = new JButton();
        buttonNext.setEnabled( true );
        buttonNext.setText( "Next" );
        panel2.add( buttonNext, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false ) );
        buttonCancel = new JButton();
        buttonCancel.setText( "Cancel" );
        panel2.add( buttonCancel, new GridConstraints( 0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false ) );
        displayPanel = new JPanel();
        displayPanel.setLayout( new CardLayout( 0, 0 ) );
        contentPane.add( displayPanel, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false ) );
        done = new JPanel();
        done.setLayout( new GridLayoutManager( 2, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        displayPanel.add( done, "done" );
        final JLabel label1 = new JLabel();
        label1.setText( "Congratulations! You've successfully installed MEDICS on this computer!" );
        done.add( label1, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false ) );
        final Spacer spacer2 = new Spacer();
        done.add( spacer2, new GridConstraints( 1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false ) );
        fieldsCard = new JPanel();
        fieldsCard.setLayout( new GridLayoutManager( 5, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        fieldsCard.setVisible( true );
        displayPanel.add( fieldsCard, "fieldsCard" );
        final JPanel panel3 = new JPanel();
        panel3.setLayout( new GridLayoutManager( 1, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        fieldsCard.add( panel3, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false ) );
        final JLabel label2 = new JLabel();
        label2.setFont( new Font( "Arial Black", label2.getFont().getStyle(), label2.getFont().getSize() ) );
        label2.setText( "  MEDICS Install" );
        panel3.add( label2, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false ) );
        final JPanel panel4 = new JPanel();
        panel4.setLayout( new GridLayoutManager( 1, 2, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        fieldsCard.add( panel4, new GridConstraints( 1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false ) );
        final JPanel panel5 = new JPanel();
        panel5.setLayout( new GridLayoutManager( 1, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        panel4.add( panel5, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension( 130, -1 ), null, null, 0, false ) );
        final JLabel label3 = new JLabel();
        label3.setHorizontalAlignment( 4 );
        label3.setHorizontalTextPosition( 4 );
        label3.setText( "Username:" );
        panel5.add( label3, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension( 48, 14 ), null, 0, false ) );
        final JPanel panel6 = new JPanel();
        panel6.setLayout( new GridLayoutManager( 1, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        panel4.add( panel6, new GridConstraints( 0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, new Dimension( 200, -1 ), 0, false ) );
        username = new JTextField();
        username.setText( "" );
        panel6.add( username, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension( 373, 20 ), null, 0, false ) );
        final JPanel panel7 = new JPanel();
        panel7.setLayout( new GridLayoutManager( 1, 2, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        fieldsCard.add( panel7, new GridConstraints( 2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false ) );
        final JPanel panel8 = new JPanel();
        panel8.setLayout( new GridLayoutManager( 1, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        panel7.add( panel8, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension( 130, -1 ), null, null, 0, false ) );
        final JLabel label4 = new JLabel();
        label4.setHorizontalAlignment( 4 );
        label4.setHorizontalTextPosition( 2 );
        label4.setText( "Password:" );
        panel8.add( label4, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension( 48, 14 ), null, 0, false ) );
        final JPanel panel9 = new JPanel();
        panel9.setLayout( new GridLayoutManager( 1, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        panel7.add( panel9, new GridConstraints( 0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, new Dimension( 200, -1 ), 0, false ) );
        password = new JTextField();
        panel9.add( password, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension( 373, 20 ), null, 0, false ) );
        final JPanel panel10 = new JPanel();
        panel10.setLayout( new GridLayoutManager( 1, 2, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        fieldsCard.add( panel10, new GridConstraints( 3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false ) );
        final JPanel panel11 = new JPanel();
        panel11.setLayout( new GridLayoutManager( 1, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        panel10.add( panel11, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, new Dimension( 130, -1 ), null, null, 0, false ) );
        final JLabel label5 = new JLabel();
        label5.setHorizontalAlignment( 4 );
        label5.setHorizontalTextPosition( 4 );
        label5.setText( "Server Public Key Id:" );
        panel11.add( label5, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension( 48, 14 ), null, 0, false ) );
        final JPanel panel12 = new JPanel();
        panel12.setLayout( new GridLayoutManager( 1, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        panel10.add( panel12, new GridConstraints( 0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, new Dimension( 200, -1 ), 0, false ) );
        publicKeyId = new JTextField();
        publicKeyId.setText( "" );
        panel12.add( publicKeyId, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension( 373, 20 ), null, 0, false ) );
        final Spacer spacer3 = new Spacer();
        fieldsCard.add( spacer3, new GridConstraints( 4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false ) );
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
