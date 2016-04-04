package maqs.ehs.patient;

import com.sigilent.business.util.StringUtils;
import com.sigilent.business.util.xml.XMLWrapper;
import com.sigilent.business.util.xml.XMLWrapperException;
import maqs.ehs.form.*;
import maqs.ehs.util.AppProperties;
import maqs.ehs.util.SecurityUtil;
import org.jdom.Element;

import javax.swing.*;
import java.util.*;

public class PatientRecord {

    private String recordName;
    private Map<String, Field> fields = new LinkedHashMap<String, Field>();
    private Status status;
    private static final String RECORD_NAME_FIELD_ID = "recordname";

    public static enum Status {
        NEW,
        EDITED;

        public boolean isNew() {
            return this == NEW;
        }

        public boolean isEdited() {
            return this == EDITED;
        }
    }

    private String computeRecordName( Map<String, Field> fields ) {
        String raw = FieldManager.getSpecialFieldValue( fields, RECORD_NAME_FIELD_ID );
        raw = raw.replace( "\"", "" );
        return raw;
    }

    protected void setRecordName( String recordName ) {
        this.recordName = recordName;
    }

    public void load() {
        XMLWrapper xml = loadXmlWrapper( recordName );
        Collection<Field> fieldList = FieldManager.getFields();
        Map<String, Field> map = new LinkedHashMap<String, Field>();
        for ( Field field : fieldList ) {
            map.put( field.getId(), field );
        }
        fields = map;
        FieldManager.fillValues( map.values(), xml );
        status = Status.EDITED;
    }

    private XMLWrapper loadXmlWrapper( String fileName ) {
        StringBuffer data = SecurityUtil.getDecryptedLocalData( fileName );
        XMLWrapper xml;
        try {
            xml = new XMLWrapper( data );
        } catch ( XMLWrapperException e ) {
            throw new RuntimeException( "Record " + fileName + " could not be parsed." );
        }
        return xml;
    }

    protected boolean save( JPanel displayPane, ResultCollector resultCollector ) {

        XMLWrapper xml;
        FormHelper.updateFieldsFromPanel( displayPane, getFields() );

        collectResults( resultCollector );

        if ( resultCollector.hasResultsWithHigherSeverityThan( ResultType.WARNING ) ) {
            return false;
        }

        String computedName = computeRecordName( fields );
        if ( status.isNew() || !computedName.equals( recordName ) ) {
            recordName = computedName;
            xml = handleNew( recordName );
        } else {
            xml = update();
        }

        FormHelper.updateXmlWrapperFromFields( getFields(), xml );
        writeToFile( recordName, xml );

        status = Status.EDITED;

        return true;
    }

    public void collectResults( ResultCollector results ) {

        List<String> mandatoryNonBlankFields = new ArrayList<String>();

        if ( AppProperties.isSaveRequiresRecordnameFields() ) {
            mandatoryNonBlankFields.addAll( FieldManager.getSpecialFieldComponents( RECORD_NAME_FIELD_ID ) );
        }

        for ( Field field : getFields() ) {
            if ( field.isRequired() && StringUtils.isEmpty( field.getValue() ) ) {

                if ( !mandatoryNonBlankFields.isEmpty() ) {
                    if ( mandatoryNonBlankFields.contains( field.getId() ) ) {
                        ProcessingResult result = new ProcessingResult(
                                ResultType.FATAL,
                                field.getId(),
                                "Field '" + field.getLabel() + "' is blank."
                        );
                        results.addResult( result );
                        continue;
                    }
                }

                if ( !AppProperties.isSaveRequiredBlankFieldEnabled() ) {
                    ProcessingResult result = new ProcessingResult(
                            ResultType.FATAL,
                            field.getId(),
                            "Field '" + field.getLabel() + "' is blank."
                    );
                    results.addResult( result );
                } else {
                    ProcessingResult result = new ProcessingResult(
                            ResultType.WARNING,
                            field.getId(),
                            "Field '" + field.getLabel() + "' is blank."
                    );
                    results.addResult( result );
                }
            }
        }
    }

    public boolean hasErrors() {
        ResultCollector results = new DefaultResultCollector();
        collectResults( results );
        return results.hasResultsWithHigherSeverityThan( ResultType.INFO );
    }

    private void writeToFile( String recordName, XMLWrapper xml ) {
        // write
        SecurityUtil.localEncryptData( xml.toString(), recordName );
    }

    private XMLWrapper handleNew( String recordName ) {
        try {
            XMLWrapper xml = FieldManager.getBlankRecordXml();
            writeToFile( recordName, xml );
            return xml;
        } catch ( Exception e ) {
            throw new RuntimeException( "Could not create blank patient!" );
        }
    }

    public String toString() {
        XMLWrapper xmlWrapper = update();
        return xmlWrapper.toString();
    }

    private XMLWrapper update() {
        XMLWrapper xml = loadXmlWrapper( recordName );

        for ( Field field : getFields() ) {

            String xpath = field.getXpath();
            if ( StringUtils.isEmpty( xpath ) ) {
                continue;
            }

            Element node = null;
            try {
                node = xml.getSingleNode( xpath );
            } catch ( XMLWrapperException e ) {
                System.out.println( "No node found at: " + xpath );
            }

            if ( node != null ) {
                node.setText( field.getValue() );
            }
        }
        return xml;
    }

    public void clear() {
        for ( Field field : getFields() ) {
            field.setValue( "" );
        }
    }

    public Collection<Field> getFields() {
        return fields.values();
    }

    protected void setFields( Map<String, Field> fields ) {
        this.fields = fields;
    }

    public Status getStatus() {
        return status;
    }

    protected void setStatus( Status status ) {
        this.status = status;
    }

    public String getRecordName() {
        return recordName;
    }

    protected void save( ImportedLine line, Map<Integer, String> fieldPositionMap ) {
        int position = 0;
        for ( Object argObj : line.getArgs() ) {
            String arg = ( String ) argObj;
            String fieldId = fieldPositionMap.get( position );
            Field field = fields.get( fieldId );
            field.setValue( arg );
            position++;
        }

        recordName = computeRecordName( fields );
        XMLWrapper xml = handleNew( recordName );
        updateXmlWrapperFromFieldList( getFields(), xml );
        PatientManager.delete( recordName );
        writeToFile( recordName, xml );
        status = Status.EDITED;
    }

    public void updateXmlWrapperFromFieldList( Collection<Field> list, XMLWrapper xmlWrapper ) {

        for ( Field field : list ) {

            String xpath = field.getXpath();
            if ( StringUtils.isEmpty( xpath ) ) {
                continue;
            }

            Element node = null;
            try {
                node = xmlWrapper.getSingleNode( xpath );
            } catch ( XMLWrapperException e ) {
                e.printStackTrace();
            }

            // get new value; by default, its the old value
            String mostRecentValue = field.getValue();

            if ( node != null ) {
                node.setText( mostRecentValue );
            }

        }
    }

}
