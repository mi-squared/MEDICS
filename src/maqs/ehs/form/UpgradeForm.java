package maqs.ehs.form;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import maqs.ehs.util.UpgradeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

public class UpgradeForm extends JDialog {
    private JPanel contentPane;
    private JFileChooser upgradeForm;
    private Installation parentForm;

    public UpgradeForm() {
        setContentPane( contentPane );
        setModal( true );

        setContentPane( contentPane );
        setModal( true );

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                dispose();
            }
        }, KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                dispose();
            }
        }, KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );

        upgradeForm.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {

                if ( JFileChooser.CANCEL_SELECTION.equals( e.getActionCommand() ) ) {
                    dispose();
                }

                if ( JFileChooser.APPROVE_SELECTION.equals( e.getActionCommand() ) ) {

                    File selectedFile = upgradeForm.getSelectedFile();
                    File thisDir = new File( "." );
                    System.out.println( thisDir.isDirectory() + ": " + thisDir.getAbsolutePath() );

                    UpgradeUtil.upgradeInstallationDir( selectedFile, thisDir );

                    parentForm.completedUpgrade();

                    dispose();
                }

            }
        } );

    }

    public Installation getParentForm() {
        return parentForm;
    }

    public void setParentForm( Installation parentForm ) {
        this.parentForm = parentForm;
    }


    public static void main( String[] args ) {
        UpgradeForm dialog = new UpgradeForm();
        dialog.pack();
        dialog.setVisible( true );
        System.exit( 0 );
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
        panel1.setLayout( new GridLayoutManager( 1, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        contentPane.add( panel1, new GridConstraints( 1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false ) );
        upgradeForm = new JFileChooser();
        upgradeForm.setDialogTitle( "ddddddd" );
        upgradeForm.setEnabled( false );
        upgradeForm.setFileHidingEnabled( false );
        upgradeForm.setFileSelectionMode( 1 );
        panel1.add( upgradeForm, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false ) );
        final JPanel panel2 = new JPanel();
        panel2.setLayout( new GridLayoutManager( 1, 1, new Insets( 0, 0, 0, 0 ), -1, -1 ) );
        contentPane.add( panel2, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false ) );
        final JLabel label1 = new JLabel();
        label1.setFont( new Font( label1.getFont().getName(), Font.BOLD, 14 ) );
        label1.setText( "Please select the location of the MEDICS installation to upgrade" );
        panel2.add( label1, new GridConstraints( 0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false ) );
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
