package maqs.ehs.form;

import com.sigilent.business.util.StringUtils;
import com.sigilent.business.util.xml.XMLWrapper;
import com.sigilent.business.util.xml.XMLWrapperException;
import maqs.ehs.MedicsContext;
import maqs.ehs.patient.*;
import maqs.ehs.util.AllowBlankFieldMaskFormatter;
import org.jdom.Element;
import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.*;
import java.util.List;

public class FormHelper {

    static void populateTabbedPane( JTabbedPane tabbedPane, Collection<Field> list, ResultCollector resultCollector) {

        List<Tab> tabs = TabSvc.getTabs();
        for ( Tab tab : tabs ) {

            final JPanel panel = new JPanel();

            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setViewportView( panel );

            List<Field> fieldsForTab = FieldManager.getFieldsForTab( list, tab );
            boolean hasRequiredFields = false;
            boolean hasErroredFields = false;
            for ( Field field : fieldsForTab ) {
                if ( field.isRequired() ) {
                    hasRequiredFields = true;
                }
                if (  resultCollector.hasResultFor( ResultType.FATAL, field.getId() )
                   || resultCollector.hasResultFor( ResultType.WARNING, field.getId() ) ) {
                    hasErroredFields = true;
                }
            }
            String tabTitle = tab.getTitle();
            if ( hasRequiredFields ) {
                if ( hasErroredFields ) {
                    tabTitle = "<html><font face='Arial' color='red' size='4'><b>*</b></font><font color='red'><i>" + tabTitle + "</i></font></html>";
                } else {
                    tabTitle = "<html><font face='Arial' size='4'><b>*</b></font><i>" + tabTitle + "</i></html>";
                }
            }

            tabbedPane.addTab( tabTitle, scrollPane );

            MedicsForm form = new MedicsFormImpl();
            form.setFormPanel( panel );

            MedicsContext.get().getFormManager( tabbedPane ).populatePanel( form, tab.getMaxRowsPerColumn(), fieldsForTab, resultCollector );
        }
    }

    static String searchForFormFieldValue( Component[] components, String fieldId ) {
        Component component = searchForFormField( components, fieldId );
        if ( component == null ) {
            return null;
        }
        if ( component instanceof JFormattedTextField ) {
            JFormattedTextField jFormattedTextField = ( JFormattedTextField ) component;
            AllowBlankFieldMaskFormatter formatter = ( AllowBlankFieldMaskFormatter ) jFormattedTextField.getFormatter();
            String representation = formatter.getBlankRepresentation();
            if ( representation != null && representation.equals( jFormattedTextField.getText() ) ) {
                return "";
            } else {
                return jFormattedTextField.getText();
            }
        }

        if ( component instanceof JTextField ) {
            return ( ( JTextField ) component ).getText();
        }

        if ( component instanceof JLabel ) {
            return ( ( JLabel ) component ).getText();
        }

        if ( component instanceof JTextArea ) {
            return ( ( JTextArea ) component ).getText();
        }

        if ( component instanceof JComboBox ) {
            return String.valueOf( ( ( JComboBox ) component ).getSelectedItem() );
        }

        if ( component instanceof JXDatePicker ) {
            Date date = ( ( JXDatePicker ) component ).getDate();
            if ( date == null ) {
                return null;
            } else {
                return String.valueOf( date );
            }
        }

        return null;
    }

    static Component searchForFormField( Component[] components, String fieldId ) {
        for ( Component component : components ) {
            if ( component instanceof JPanel ) {
                JPanel jPanel = ( JPanel ) component;
                Component field = searchForFormField( jPanel.getComponents(), fieldId );
                if ( field != null ) {
                    return field;
                }
            } else if ( component instanceof JScrollPane ) {
                JScrollPane scrollPane = ( JScrollPane ) component;
                Component field = searchForFormField( scrollPane.getViewport().getComponents(), fieldId );
                if ( field != null ) {
                    return field;
                }
            } else if ( component instanceof JTabbedPane ) {
                JTabbedPane tabbedPane = ( JTabbedPane ) component;
                Component field = searchForFormField( tabbedPane.getComponents(), fieldId );
                if ( field != null ) {
                    return field;
                }
            } else {
                String anObject = component.getName();
                if ( fieldId.equals( anObject ) ) {

                    if ( component instanceof JFormattedTextField ) {
                        return component;
                    }

                    if ( component instanceof JTextField ) {
                        return component;
                    }

                    if ( component instanceof JLabel ) {
                        return component;
                    }

                    if ( component instanceof JTextArea ) {
                        return component;
                    }

                    if ( component instanceof JComboBox ) {
                        return component;
                    }

                    if ( component instanceof JXDatePicker ) {
                        return component;
                    }

                }
            }
        }

        return null;
    }

    public static void updateXmlWrapperFromFields( Collection<Field> list, XMLWrapper xmlWrapper ) {
        for ( Field field : list ) {

            String xpath = field.getXpath();
            if ( StringUtils.isEmpty( xpath ) ) {
                continue;
            }

            Element node = null;
            try {
                node = xmlWrapper.getSingleNode( xpath );
            } catch ( XMLWrapperException e ) {
                System.out.println( "No node found at: " + xpath );
            } catch (Exception  e) {
                throw new RuntimeException( e );
            }

            if ( node != null ) {
                String fieldValue = field.getValue();
                node.setText( StringUtils.isEmpty( fieldValue ) ? null : fieldValue );
            }
        }
    }

    public static void updateFieldsFromPanel( JPanel displayPane, Collection<Field> list ) {
        for ( Field field : list ) {

            String xpath = field.getXpath();
            if ( StringUtils.isEmpty( xpath ) ) {
                continue;
            }

            String fieldId = field.getId();

            // get new value; by default, its the old value
            String mostRecentValue = searchForFormFieldValue( displayPane.getComponents(), fieldId );
            field.setValue( mostRecentValue );
        }
    }

    public static void populatePatientList( MedicsUI panel, JPanel patientsPanel, File[] files ) {

        patientsPanel.setLayout( new BoxLayout( patientsPanel , BoxLayout.PAGE_AXIS ) );

        for ( File file : files ) {
            String fileName = file.getName();

            JPanel row = new JPanel();
            row.setLayout( new BoxLayout( row, BoxLayout.LINE_AXIS) );
            row.setAlignmentX( Component.LEFT_ALIGNMENT );
            patientsPanel.add( row );

            PtListListener listener = new PtListListener();
            listener.setRecordName( fileName );
            listener.setParentForm( panel );

            populatePatientRow( row, listener );
        }

    }

    private static void populatePatientRow( JPanel row, PtListListener listener ) {
        // create column
        JPanel panel = new JPanel();
        panel.setLayout( new BoxLayout( panel, BoxLayout.LINE_AXIS ));

        JLabel label = new JLabel();
        String recordName = listener.getRecordName();
        label.setText( recordName );

        JButton editButton = new JButton();
        editButton.setText( "Edit" );
        editButton.addActionListener( listener );

        JButton deleteButton = new JButton();
        deleteButton.setText( "Delete" );
        deleteButton.addActionListener( listener );

        JButton exportButton = new JButton();
        exportButton.setText( "Export" );
        exportButton.addActionListener( listener );

        if ( !PatientManager.isComplete( recordName ) ) {
            exportButton.setEnabled( false );
        }

        panel.add( editButton );
        panel.add( deleteButton );
        panel.add( exportButton );
        panel.add( label );

        // Add column to row
        row.add( panel );
    }

    static void invertFocusTraversalBehaviour( JTextArea textArea ) {
        Set<AWTKeyStroke> forwardKeys = textArea.getFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS );
        Set<AWTKeyStroke> backwardKeys = textArea.getFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS );

        // check that we WANT to modify current focus traversal keystrokes
        if ( forwardKeys.size() != 1 || backwardKeys.size() != 1 ) return;
        final AWTKeyStroke fks = forwardKeys.iterator().next();
        final AWTKeyStroke bks = backwardKeys.iterator().next();
        final int fkm = fks.getModifiers();
        final int bkm = bks.getModifiers();
        final int ctrlMask = KeyEvent.CTRL_MASK + KeyEvent.CTRL_DOWN_MASK;
        final int ctrlShiftMask = KeyEvent.SHIFT_MASK + KeyEvent.SHIFT_DOWN_MASK + ctrlMask;
        if ( fks.getKeyCode() != KeyEvent.VK_TAB || ( fkm & ctrlMask ) == 0 || ( fkm & ctrlMask ) != fkm ) {    // not currently CTRL+TAB for forward focus traversal
            return;
        }
        if ( bks.getKeyCode() != KeyEvent.VK_TAB || ( bkm & ctrlShiftMask ) == 0 || ( bkm & ctrlShiftMask ) != bkm ) {    // not currently CTRL+SHIFT+TAB for backward focus traversal
            return;
        }

        // bind our new forward focus traversal keys
        Set<AWTKeyStroke> newForwardKeys = new HashSet<AWTKeyStroke>( 1 );
        newForwardKeys.add( AWTKeyStroke.getAWTKeyStroke( KeyEvent.VK_TAB, 0 ) );
        textArea.setFocusTraversalKeys(
                KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                Collections.unmodifiableSet( newForwardKeys )
        );
        // bind our new backward focus traversal keys
        Set<AWTKeyStroke> newBackwardKeys = new HashSet<AWTKeyStroke>( 1 );
        newBackwardKeys.add( AWTKeyStroke.getAWTKeyStroke( KeyEvent.VK_TAB, KeyEvent.SHIFT_MASK + KeyEvent.SHIFT_DOWN_MASK ) );
        textArea.setFocusTraversalKeys(
                KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                Collections.unmodifiableSet( newBackwardKeys )
        );

        // Now, it's still useful to be able to type TABs in some cases.
        // Using this technique assumes that it's rare however (if the user
        // is expected to want to type TAB often, consider leaving text area's
        // behaviour unchanged...).  Let's add some key bindings, inspired
        // from a popular behaviour in instant messaging applications...
        TextInserter.applyTabBinding( textArea );

        // we could do the same stuff for RETURN and CTRL+RETURN for activating
        // the root pane's default button: omitted here for brevity
    }


    private static class TextInserter extends AbstractAction {
        private JTextArea textArea;
        private String insertable;

        private TextInserter( JTextArea textArea, String insertable ) {
            this.textArea = textArea;
            this.insertable = insertable;
        }

        public static void applyTabBinding( JTextArea textArea ) {
            textArea.getInputMap( JComponent.WHEN_FOCUSED )
                    .put( KeyStroke.getKeyStroke( KeyEvent.VK_TAB, KeyEvent.CTRL_MASK + KeyEvent.CTRL_DOWN_MASK ), "tab" );
            textArea.getActionMap()
                    .put( "tab", new TextInserter( textArea, "\t" ) );
        }

        public void actionPerformed( ActionEvent evt ) {
            // could be improved to overtype selected range
            textArea.insert( insertable, textArea.getCaretPosition() );
        }
    }

}
