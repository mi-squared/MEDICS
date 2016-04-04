package maqs.ehs.form;

import maqs.ehs.patient.Field;
import maqs.ehs.patient.FieldSvc;
import maqs.ehs.util.AppProperties;
import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultFormManager implements MedicsFormManager {

    private static final int FULL_ROW_WIDTH = AppProperties.getRowLength();
    private static final int LABEL_COLUMN_WIDTH = ( int ) ( FULL_ROW_WIDTH * 0.30 );
    private static final int VALUE_COLUMN_WIDTH = ( int ) ( FULL_ROW_WIDTH * ( 1 - 0.30 ) );
    private static final int SINGLE_ROW_HEIGHT = 22;

    private static final Font DEFAULT_LABEL_FONT = new Font( "Arial", Font.PLAIN, 10 );
    private static final Font REQUIRED_LABEL_FONT = new Font( "Arial", Font.ITALIC, 10 );
    private static final Font HEADER_LABEL_FONT = new Font( "Arial", Font.BOLD, 13 );
    private static final Font REQUIRED_ASTERIX_FONT = new Font( "Arial", Font.BOLD, 13 );
    private static final Color REQUIRED_FIELD_WARNING = Color.CYAN;
    private static final Color REQUIRED_FIELD_ERROR = Color.PINK;

    private static List<MedicsFormRow> FORM_ROW_TYPES;
    private Component parentContainer;
    private ResultCollector resultCollector;

    public DefaultFormManager( Component parentContainer ) {
        this.parentContainer = parentContainer;
    }

    {
        FORM_ROW_TYPES = new ArrayList<MedicsFormRow>();
        FORM_ROW_TYPES.add( new TextFormField() );
        FORM_ROW_TYPES.add( new BlockTextAreaFormRow() );
        FORM_ROW_TYPES.add( new LabeledTextAreaFormRow() );
        FORM_ROW_TYPES.add( new PureTextFormRow() );
        FORM_ROW_TYPES.add( new SpacerFormRow() );
        FORM_ROW_TYPES.add( new DropDownFormField() );
        FORM_ROW_TYPES.add( new RadioFormField() );
        FORM_ROW_TYPES.add( new LinkedTextFormField() );
        FORM_ROW_TYPES.add( new DateFormField() );
    }

    public MedicsFormManager getInstance( JPanel parentContainer ) {
        return new DefaultFormManager( parentContainer );
    }

    public Component getParentContainer() {
        return parentContainer;
    }

    private static Dimension columnSpacer = new Dimension( 20, SINGLE_ROW_HEIGHT );
    private static Dimension rowSpacer = new Dimension( 2, 2 );
    private static Dimension singleColumnRowDimensions = new Dimension( FULL_ROW_WIDTH, SINGLE_ROW_HEIGHT );
    private static Dimension valueColumnDimensions = new Dimension( VALUE_COLUMN_WIDTH, SINGLE_ROW_HEIGHT );
    private static Dimension labelColumnDimensions = new Dimension( LABEL_COLUMN_WIDTH, SINGLE_ROW_HEIGHT );

    public void populatePanel( MedicsForm medicsForm, int maxRowsPerColumn, List<Field> list, ResultCollector resultCollector ) {
        this.resultCollector = resultCollector;
        int totalRows = list.size();
        int numColumns = Math.round( totalRows / maxRowsPerColumn );
        int mod = totalRows % maxRowsPerColumn;
        if ( mod > 0 ) {
            numColumns++;
        }
        JPanel displayPane = medicsForm.getFormPanel();

        // set number of columns
        BoxLayout boxLayout = new BoxLayout( displayPane, BoxLayout.LINE_AXIS );
        displayPane.setLayout( boxLayout );

        displayPane.add( Box.createRigidArea( new Dimension( 8, 1 ) ) );

        Map<Integer, JPanel> columnPanelHash = new HashMap<Integer, JPanel>();
        for ( int col = 0; col < numColumns; col++ ) {
            // create each columnNumber panel
            JPanel columnPanel = new JPanel();
            columnPanel.setLayout( new BoxLayout( columnPanel, BoxLayout.PAGE_AXIS ) );
            columnPanel.setAlignmentY( Component.TOP_ALIGNMENT );
            columnPanelHash.put( col, columnPanel );
            displayPane.add( columnPanel );
            displayPane.add( Box.createRigidArea( columnSpacer ) );
        }

        // assign fields to the right panel
        int overallFieldNumber = 0;
        int columnNumber = -1;
        JPanel columnPanel = null;
        for ( Field field : list ) {

            if ( overallFieldNumber % maxRowsPerColumn == 0 ) {
                columnNumber++;
                columnPanel = columnPanelHash.get( columnNumber );
            }

            addField( field, columnPanel );
            columnPanel.add( Box.createRigidArea( rowSpacer ) );
            overallFieldNumber++;
        }
    }

    private void addField( Field field, JPanel columnPanel ) {
        FieldSvc svc = FieldSvc.get( field );
        MedicsFormRow formRow = getFormRow( svc );

        // attach row
        JPanel panel = formRow.getAsJPanel();

        if ( resultCollector.hasResultFor( ResultType.FATAL, field.getId() ) ) {
            renderAsError( panel.getComponents(), REQUIRED_FIELD_ERROR );
        } else if ( resultCollector.hasResultFor( ResultType.WARNING, field.getId() ) ) {
            renderAsError( panel.getComponents(), REQUIRED_FIELD_WARNING );
        }
        columnPanel.add( panel );
    }

    private void renderAsError( Component[] components, Color renderColor ) {
        for ( Component component : components ) {
            if ( component instanceof JPanel ) {
                JPanel jPanel = ( JPanel ) component;
                renderAsError( jPanel.getComponents(), renderColor );
            } else if ( component instanceof JScrollPane ) {
                JScrollPane scrollPane = ( JScrollPane ) component;
                renderAsError( scrollPane.getViewport().getComponents(), renderColor );
            } else if ( component instanceof JTabbedPane ) {
                JTabbedPane tabbedPane = ( JTabbedPane ) component;
                renderAsError( tabbedPane.getComponents(), renderColor );
            } else if ( component instanceof JFormattedTextField ) {
                component.setBackground( renderColor );
            } else if ( component instanceof JTextField ) {
                component.setBackground( renderColor );
            } else if ( component instanceof JTextArea ) {
                component.setBackground( renderColor );
            } else if ( component instanceof JXDatePicker ) {
                ( ( JXDatePicker ) component ).getEditor().setBackground( renderColor );
            } else if ( component instanceof JLabel ) {
                JLabel label = ( JLabel ) component;
                label.setText( "<html><font color='red'>" + label.getText() + "</font></html>" );
            } else if ( component instanceof JComboBox ) {
                component.setBackground( renderColor );
            }
        }
    }

    private MedicsFormRow getFormRow( FieldSvc svc ) {
        for ( MedicsFormRow formRowType : FORM_ROW_TYPES ) {
            MedicsFormRow newFormRow = formRowType.create( this, svc );
            if ( newFormRow != null ) {
                return newFormRow;
            }
        }

        throw new RuntimeException( "Unrecognized fieldtype: " + svc.getField().getFieldType() );
    }

    private JPanel createFormRow() {
        JPanel row = new JPanel();
        row.setLayout( new FlowLayout( FlowLayout.LEFT ) );
        return row;
    }

    public MedicsBlockFormRow createBlockFormRow() {
        return new DefaultManagerBlockFormRow();
    }

    public Font getDefaultLabelFont() {
        return DEFAULT_LABEL_FONT;
    }

    public Font getRequiredLabelFont() {
        return REQUIRED_LABEL_FONT;
    }

    public Font getRequiredAsterixFont() {
        return REQUIRED_ASTERIX_FONT;
    }

    public Font getTextStyle( Field field ) {
        if ( "header".equals( field.getTextStyle() ) ) {
            return HEADER_LABEL_FONT;
        }

        return DEFAULT_LABEL_FONT;
    }

    public Dimension getSingleColumnRowDimensions() {
        return singleColumnRowDimensions;
    }

    public Dimension getValueColumnDimensions() {
        return valueColumnDimensions;
    }

    public JPanel createSpacerFormRow() {
        JPanel row = createFormRow();
        row.setMaximumSize( getSingleColumnRowDimensions() );
        row.setMinimumSize( getSingleColumnRowDimensions() );
        return row;
    }

    public MedicsLabelValueFormRow createLabelValueFormRow() {
        return new DefaultManagerLabelValueFormRow();
    }

    class DefaultManagerLabelValueFormRow implements MedicsLabelValueFormRow {

        private JPanel rowPanel;
        private Component valueComponent;
        private int rows;
        private Component labelComponent;
        private boolean scrolling;
        private boolean required;

        public JPanel getAsJPanel() {
            assemble();
            return rowPanel;
        }

        private void assemble() {
            // create row
            rowPanel = createFormRow();
            rowPanel.setLayout( new BoxLayout( rowPanel, BoxLayout.LINE_AXIS ) );

            // create label column
            addLabelColumnToRow();

            // create column
            addValueColumnToRow();
        }

        private void addLabelColumnToRow() {
            JPanel labelPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
            labelPanel.setMinimumSize( labelColumnDimensions );
            labelPanel.setMaximumSize( labelColumnDimensions );

//            labelPanel.setBorder(BorderFactory.createCompoundBorder(
//                               BorderFactory.createLineBorder(Color.red),
//                               labelPanel.getBorder()));


            JPanel panel = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
            panel.setSize( LABEL_COLUMN_WIDTH, SINGLE_ROW_HEIGHT );
            if ( required ) {
                final JLabel requiredAsterixLbl = new JLabel();
                requiredAsterixLbl.setText( "*" );
                requiredAsterixLbl.setFont( REQUIRED_ASTERIX_FONT );
                requiredAsterixLbl.setFocusable( false );
                panel.add( requiredAsterixLbl );
            }
            panel.add( labelComponent );
            panel.setAlignmentY( Component.TOP_ALIGNMENT );

            labelPanel.add( panel );
            // Add label column to row
            rowPanel.add( labelPanel );
        }

        private void addValueColumnToRow() {
            JPanel valuePanel = new JPanel( new GridLayout( 1, 1 ) );
            valuePanel.setMinimumSize( new Dimension( VALUE_COLUMN_WIDTH, SINGLE_ROW_HEIGHT * rows ) );
            valuePanel.setMaximumSize( new Dimension( VALUE_COLUMN_WIDTH, SINGLE_ROW_HEIGHT * rows ) );

//            valuePanel.setBorder(BorderFactory.createCompoundBorder(
//                               BorderFactory.createLineBorder(Color.red),
//                               valuePanel.getBorder()));

            if ( scrolling ) {
                JScrollPane scrollPane1 = new JScrollPane();
                valuePanel.add( scrollPane1 );
                scrollPane1.setViewportView( valueComponent );
            } else {
                valuePanel.add( valueComponent );
            }

            // Add column to row
            rowPanel.add( valuePanel );
        }


        public void setLabelComponent( Component labelComponent ) {
            this.labelComponent = labelComponent;
        }

        public void setValueComponent( Component valueComponent, int rows ) {
            this.valueComponent = valueComponent;
            this.rows = rows;
        }

        public void setScrolling( boolean scrolling ) {
            this.scrolling = scrolling;
        }

        public void setRequired( boolean required ) {
            this.required = required;
        }

    }

    class DefaultManagerBlockFormRow implements MedicsBlockFormRow {
        private boolean scrolling;
        private Component valueComponent;
        private int textRows;
        private JPanel rowPanel;

        public JPanel getAsJPanel() {
            assemble();
            return rowPanel;
        }

        private void assemble() {
            // create row
            rowPanel = createFormRow();
            rowPanel.setLayout( new BoxLayout( rowPanel, BoxLayout.LINE_AXIS ) );

            attachValueColumnToRow();

//            rowPanel.setBorder(BorderFactory.createCompoundBorder(
//                               BorderFactory.createLineBorder(Color.blue),
//                               rowPanel.getBorder()));

        }

        private void attachValueColumnToRow() {
            JPanel valuePanel = new JPanel();
            valuePanel.setLayout( new GridLayout( 1, 1 ) );
            valuePanel.setMaximumSize( new Dimension( FULL_ROW_WIDTH, SINGLE_ROW_HEIGHT * textRows ) );
            valuePanel.setMinimumSize( new Dimension( FULL_ROW_WIDTH, SINGLE_ROW_HEIGHT * textRows ) );

            if ( scrolling ) {
                JScrollPane scrollPane1 = new JScrollPane();
                valuePanel.add( scrollPane1 );
                scrollPane1.setViewportView( valueComponent );
            } else {
                valuePanel.add( valueComponent );
            }
            rowPanel.add( valuePanel );
        }

        public void setValueComponent( Component valueComponent, int textRows ) {
            this.valueComponent = valueComponent;
            this.textRows = textRows;
        }

        public void setScrolling( boolean scrolling ) {
            this.scrolling = scrolling;
        }
    }

}