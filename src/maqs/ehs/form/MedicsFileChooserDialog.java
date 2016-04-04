package maqs.ehs.form;

import maqs.ehs.util.AppProperties;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.File;

public class MedicsFileChooserDialog extends JDialog {
    private JPanel contentPane;
    private JFileChooser fileChooser;
    private JPanel fileChooserPanel;
    private String title;

    private ActionListener fileChooserActionListener;
    private int fileSelectionMode;
    private File initialDirectory;

    public MedicsFileChooserDialog() {
        super();

        contentPane = new JPanel( new GridLayout( 1, 1 ) );
        fileChooserPanel = new JPanel( new GridLayout( 1, 1 ) );
        contentPane.add( fileChooserPanel );
        fileChooser = new JFileChooser();
        fileChooserPanel.add( fileChooser );

        setContentPane( contentPane );
        setModal( true );
    }

    public void init() {

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName() );
        } catch ( Exception e ) {
        }

        setContentPane( contentPane );
        setModal( true );

        // call onCancel() when cross is clicked
        setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
        addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                dispose();
            }
        } );

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                dispose();
            }
        }, KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );

        fileChooser.setCurrentDirectory( initialDirectory );
        fileChooser.setFileSelectionMode( fileSelectionMode );
        fileChooser.addActionListener( fileChooserActionListener );

        // Center the dialog
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = ( screenSize.width - getWidth() ) / 4;
        int y = ( screenSize.height - getHeight() ) / 4;
        setLocation( x, y );

        setTitle( AppProperties.getAppTitleFull() + " - " +  title );

        pack();
        setVisible( true );
    }

    public static void createFileChooser(
            String chooserTitle,
            ActionListener actionListener,
            int fileSelectionMode,
            File initialDirectory
    ) {
        MedicsFileChooserDialog chooser = new MedicsFileChooserDialog();
        chooser.title = chooserTitle;
        chooser.fileChooserActionListener = actionListener;
        chooser.fileSelectionMode = fileSelectionMode;
        chooser.initialDirectory = initialDirectory;
        chooser.init();
    }

    public static void main( String[] args ) {
        MedicsFileChooserDialog dialog = new MedicsFileChooserDialog();
        dialog.pack();
        dialog.setVisible( true );
        System.exit( 0 );
    }

    public File getSelectedFile() {
        return fileChooser.getSelectedFile();
    }

}
