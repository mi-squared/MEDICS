package maqs.ehs.form;

import maqs.ehs.util.AppProperties;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import com.sigilent.business.util.StringUtils;

public class MedicsDialog extends JDialog {
    private JPanel contentPane;
    private JPanel contentPanel;

    private String topTextContent;
    private String scrollableTextContent;

    private CallBackPerformer callBackPerformer;
    private PatientAction action;
    private JButton buttonOK;
    private JButton buttonCancel;
    private boolean showCancel;
    private String dialogTitle;

    public MedicsDialog() {
    }

    public void create() {
        contentPane = new JPanel( new GridLayout( 1, 1 ) );
        contentPane.setOpaque( false );

        setContentPane( contentPane );
        setModal( true );
        setBackground( Color.WHITE );

        contentPanel = new JPanel( new BorderLayout( 0, 0 ) );
        contentPanel.setOpaque( false );
        contentPane.add( contentPanel );

        setTitle( AppProperties.getAppTitleFull() + " - " + dialogTitle );

        // buttons
        JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT, 10, 10 ) );
        buttonOK = new JButton( "Ok" );
        buttonPanel.add( buttonOK );
        buttonCancel = new JButton( "Cancel" );
        buttonPanel.add( buttonCancel );
        buttonPanel.setOpaque( false );

        contentPanel.add( buttonPanel, BorderLayout.SOUTH );

        JPanel topTextPanel = new JPanel();
        topTextPanel.setLayout( new BoxLayout( topTextPanel, BoxLayout.LINE_AXIS ) );
        topTextPanel.setOpaque( false );

        JPanel topTextPanelInterior = new JPanel();
        topTextPanelInterior.setLayout( new BoxLayout( topTextPanelInterior, BoxLayout.PAGE_AXIS ) );
        topTextPanelInterior.setOpaque( false );

        JTextArea topText = new JTextArea();
        topText.setAutoscrolls( false );
        topText.setEditable( false );
        topText.setLineWrap( true );
        topText.setOpaque( false );
        topText.setText( topTextContent );
        topText.setWrapStyleWord( true );
        topTextPanelInterior.add( Box.createRigidArea( new Dimension( 1, 20 ) ) );
        topTextPanelInterior.add( topText );
        topTextPanelInterior.setAlignmentY( Component.TOP_ALIGNMENT );

        topTextPanel.add( Box.createRigidArea( new Dimension( 10, 1 ) ) );
        topTextPanel.add( topTextPanelInterior );
        topTextPanel.add( Box.createRigidArea( new Dimension( 10, 1 ) ) );

        contentPanel.add( topTextPanel, BorderLayout.NORTH );

        if ( StringUtils.isEmpty( scrollableTextContent ) ) {

            setSize( 350, 180 );

        } else {

            // main
            JPanel mainArea = new JPanel();
            mainArea.setLayout( new BoxLayout( mainArea, BoxLayout.PAGE_AXIS ) );
            mainArea.setOpaque( false );

            JTextArea bottomText = new JTextArea();
            bottomText.setAutoscrolls( false );
            bottomText.setEditable( false );
            bottomText.setLineWrap( true );
            bottomText.setOpaque( true );
            bottomText.setText( scrollableTextContent );
            bottomText.setWrapStyleWord( true );

            JPanel bottomTextPanel = new JPanel();
            bottomTextPanel.setLayout( new BoxLayout( bottomTextPanel, BoxLayout.LINE_AXIS ) );
            bottomTextPanel.setOpaque( false );

            JPanel bottomTextPanelInterior = new JPanel();
            bottomTextPanelInterior.setLayout( new BoxLayout( bottomTextPanelInterior, BoxLayout.PAGE_AXIS ) );
            bottomTextPanelInterior.setOpaque( false );
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setAutoscrolls( true );
            scrollPane.setOpaque( false );
            scrollPane.setVerticalScrollBarPolicy( 20 );
            scrollPane.setViewportView( bottomText );
            bottomTextPanelInterior.add( Box.createRigidArea( new Dimension( 5, 15 ) ) );
            bottomTextPanelInterior.add( scrollPane );
            bottomTextPanelInterior.setAlignmentY( Component.TOP_ALIGNMENT );

            bottomTextPanel.add( Box.createRigidArea( new Dimension( 10, 1 ) ) );
            bottomTextPanel.add( bottomTextPanelInterior );
            bottomTextPanel.add( Box.createRigidArea( new Dimension( 10, 1 ) ) );

            mainArea.add( bottomTextPanel );

            contentPanel.add( mainArea, BorderLayout.CENTER );

            setSize( 380, 350 );
        }

        // Center the dialog
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = ( screenSize.width - getWidth() ) / 2;
        int y = ( screenSize.height - getHeight() ) / 2;
        setLocation( x, y );


        buttonOK.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                onOK();
            }
        } );

        if ( showCancel ) {
            buttonCancel.addActionListener( new ActionListener() {
                public void actionPerformed( ActionEvent e ) {
                    onCancel();
                }
            } );
        } else {
            buttonCancel.setVisible( false );
        }

// call onCancel() when cross is clicked
        setDefaultCloseOperation( DO_NOTHING_ON_CLOSE );
        addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                onCancel();
            }
        } );

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );


        getRootPane().setDefaultButton( buttonOK );

        setVisible( true );

    }

    public String getTopTextContent() {
        return topTextContent;
    }

    public void setTopTextContent( String topTextContent ) {
        this.topTextContent = topTextContent;
    }

    public String getScrollableTextContent() {
        return scrollableTextContent;
    }

    public void setScrollableTextContent( String scrollableTextContent ) {
        this.scrollableTextContent = scrollableTextContent;
    }

    public CallBackPerformer getCallBackPerformer() {
        return callBackPerformer;
    }

    public void setCallBackPerformer( CallBackPerformer callBackPerformer ) {
        this.callBackPerformer = callBackPerformer;
    }

    private void onOK() {
        if ( callBackPerformer != null ) {
            callBackPerformer.performCallback( action );
        }
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public PatientAction getAction() {
        return action;
    }

    public void setAction( PatientAction action ) {
        this.action = action;
    }

    public boolean isShowCancel() {
        return showCancel;
    }

    public void setShowCancel( boolean showCancel ) {
        this.showCancel = showCancel;
    }

    public static void createOkCancelInstance( String _dialogTitle, String _dialogText, PatientAction _action,
                                               CallBackPerformer _callBackPerformer ) {
        createInstance( _dialogTitle, _dialogText, null, _action, _callBackPerformer, true );
    }

    public static void createScrollableOkCancelInstance( String _dialogTitle, String _dialogText, String scrollableText, 
                                                         PatientAction _action, CallBackPerformer _callBackPerformer ) {
        createInstance( _dialogTitle, _dialogText, scrollableText, _action, _callBackPerformer, true );
    }

    public static void createNotificationInstance( String _dialogTitle, String _dialogText ) {
        createScrollableDetailNotificationInstance( _dialogTitle, _dialogText, null );
    }

    public static void createScrollableDetailNotificationInstance( String _dialogTitle, String _topText, String _scrollableText ) {
        createInstance( _dialogTitle, _topText, _scrollableText, null, null, false );
    }

    static void createInstance( String _dialogTitle, String _topText, String _scrollableText, PatientAction _action,
                                CallBackPerformer _callBackPerformer,
                                boolean showCancel ) {

        MedicsDialog dialog = new MedicsDialog();
        dialog.setTopTextContent( _topText );
        dialog.setScrollableTextContent( _scrollableText );
        dialog.setCallBackPerformer( _callBackPerformer );
        dialog.setAction( _action );
        dialog.setShowCancel( showCancel );
        dialog.setDialogTitle( _dialogTitle );
        dialog.create();
    }

    public String getDialogTitle() {
        return dialogTitle;
    }

    public void setDialogTitle( String dialogTitle ) {
        this.dialogTitle = dialogTitle;
    }

    public static void main( String s[] ) {
        MedicsDialog dialog = new MedicsDialog();
        dialog.setTopTextContent( "This is the top text content!This is the top text content!This is the top text content!\nThis is the top text content!\nThis is the top text content!" );
//        dialog.setTopTextContent( "This is the top text content!" );
//        dialog.setScrollableTextContent( "This is the bottom text content!This is the top text content!This is the top text content!\nThis is the top text content!\nThis is the top text content!" );
        dialog.setShowCancel( true );
        dialog.create();

    }

}
