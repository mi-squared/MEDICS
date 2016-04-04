package maqs.ehs.patient;

import com.sigilent.business.util.StringUtils;
import com.sigilent.business.util.xml.XMLWrapper;
import maqs.ehs.util.AppProperties;
import maqs.ehs.util.CommonUtil;
import maqs.ehs.util.FileSystemManager;
import maqs.ehs.util.IniWrapper;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class FieldManager {

    private static final String FIELD_DEFS_LOCATION = "conf/field_defs.ini";
    private static final String FIELD_DEFAUILTS_LOCATION = "conf/field_defaults.ini";
    private static final String SPECIAL_FIELDS_LOCATION = "conf/special_fields.ini";

    private static Map<String, KeyedResource> keyedResourceMap = new HashMap<String, KeyedResource>();

    private static IniWrapper fieldDefsIniWrapper;
    private static IniWrapper fieldDefaultsIniWrapper;

    public static Collection<Field> getFields() {
        Map<String, Field> fieldDefinitions = getFieldDefinitions();
        fillFieldCustomConfigurations( fieldDefinitions );
        return fieldDefinitions.values();
    }

    private static void fillFieldCustomConfigurations( Map<String, Field> definitions ) {
        if ( fieldDefaultsIniWrapper == null ) {
            fieldDefaultsIniWrapper = new IniWrapper( FIELD_DEFAUILTS_LOCATION );
        }

        for ( Object o : fieldDefaultsIniWrapper.getSectionKeys() ) {
            String fieldId = ( String ) o;
            String defaultValue = fieldDefaultsIniWrapper.getSectionKeyValue( fieldId, "defaultvalue" );
            if ( defaultValue == null ) {
                defaultValue = "";
            }
            Field field = definitions.get( fieldId );
            field.setDefaultValue( defaultValue );

            String required = fieldDefaultsIniWrapper.getSectionKeyValue( fieldId, "required" );
            if ( !StringUtils.isEmpty( required ) ) {
                field.setRequired( "TRUE".equalsIgnoreCase( required ) );
            }
        }
    }

    private static Map<String, Field> getFieldDefinitions() {
        Map<String, Field> fields = new LinkedHashMap<String, Field>();

        if ( fieldDefsIniWrapper == null ) {
            fieldDefsIniWrapper = new IniWrapper( FIELD_DEFS_LOCATION );
        }

        for ( Object o : fieldDefsIniWrapper.getSectionKeys() ) {
            String fieldId = ( String ) o;

            Field field = new Field();
            field.setId( fieldId );
            field.setLabel( fieldDefsIniWrapper.getSectionKeyValue( fieldId, "label" ) );
            field.setFieldType( Field.FieldType.get( fieldDefsIniWrapper.getSectionKeyValue( fieldId, "type" ) ) );
            field.setDefaultValue( fieldDefsIniWrapper.getSectionKeyValue( fieldId, "defaultvalue" ) );
            field.setTextStyle( fieldDefsIniWrapper.getSectionKeyValue( fieldId, "textstyle" ) );

            if ( field.getFieldType().isTextArea() ) {
                String textRowsSectionKeyValue = fieldDefsIniWrapper.getSectionKeyValue( fieldId, "rowsize" );
                if ( !StringUtils.isEmpty( textRowsSectionKeyValue ) ) {
                    field.setTextRows( Integer.parseInt( textRowsSectionKeyValue ) );
                } else {
                    field.setTextRows( 5 );
                }
            } else {
                field.setTextRows( 1 );
            }

            field.setXpath( fieldDefsIniWrapper.getSectionKeyValue( fieldId, "xpath" ) );
            field.setTabId( fieldDefsIniWrapper.getSectionKeyValue( fieldId, "tabid" ) );

            List<String> options = new ArrayList<String>();
            String optionsString = fieldDefsIniWrapper.getSectionKeyValue( fieldId, "valuelist" );
            String resourceName = fieldDefsIniWrapper.getSectionKeyValue( fieldId, "resourcename" );
            if ( !StringUtils.isEmpty( resourceName ) ) {
                appendResourceValues( options, resourceName );
                field.setResourceId( resourceName );
            }

            if ( !StringUtils.isEmpty( optionsString ) ) {
                String[] strings = optionsString.split( "," );
                options.addAll( Arrays.asList( strings ) );
            }

            field.setValueList( options );

            String mask = fieldDefsIniWrapper.getSectionKeyValue( fieldId, "mask" );
            if ( !StringUtils.isEmpty( mask ) ) {
                mask = mask.replace( '$', '#' );
                field.setMask( mask );
            }

            String linkedFieldId = fieldDefsIniWrapper.getSectionKeyValue( fieldId, "linkedfield" );
            if ( !StringUtils.isEmpty( linkedFieldId ) ) {
                field.setLinkedFieldId( linkedFieldId );
            }

            String required = fieldDefsIniWrapper.getSectionKeyValue( fieldId, "required" );
            field.setRequired( !StringUtils.isEmpty( required ) && "TRUE".equalsIgnoreCase( required ) );

            fields.put( fieldId, field );

            String nonUserEditable = fieldDefsIniWrapper.getSectionKeyValue( fieldId, "nonusereditable" );
            field.setNonUserEditable( !StringUtils.isEmpty( nonUserEditable ) && "TRUE".equalsIgnoreCase( nonUserEditable ) );

        }
        return fields;
    }

    private static void appendResourceValues( List<String> options, String resourceName ) {
        String resourcePath = AppProperties.getResourceDirPath() + FileSystemManager.getSlash() + resourceName;
        File resource = new File( resourcePath );
        if ( !resource.exists() ) {
            return;
        }
        // else
        try {
            String line;
            BufferedReader in = new BufferedReader( new FileReader( resource ) );
            while ( ( line = in.readLine() ) != null ) {
                options.add( line );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public static void fillValues( Collection<Field> fields, XMLWrapper wrapper ) {
        for ( Field field : fields ) {
            FieldSvc svc = FieldSvc.get( field );
            svc.setValue( wrapper );
        }
    }

    public static List<Field> getFieldsForTab( Collection<Field> fields, Tab tab ) {
        List<Field> tabFields = new ArrayList<Field>();
        for ( Field field : fields ) {
            if ( field.getTabId().equals( tab.getId() ) ) {
                tabFields.add( field );
            }
        }
        return tabFields;
    }

    static String getSpecialFieldDefinition( String specialFieldId ) {
        IniWrapper iniWrapper = new IniWrapper( SPECIAL_FIELDS_LOCATION );
        return iniWrapper.getSectionKeyValue( specialFieldId, "definition" );
    }

    static boolean isSpecialFieldBlankFree( String specialFieldId ) {
        IniWrapper iniWrapper = new IniWrapper( SPECIAL_FIELDS_LOCATION );
        String sectionKeyValue = iniWrapper.getSectionKeyValue( specialFieldId, "blankfree" );
        return !StringUtils.isEmpty( sectionKeyValue ) && "true".equals( sectionKeyValue.toLowerCase() );
    }

    public static List<String> getSpecialFieldComponents( String specialFieldId ) {
        String definition = getSpecialFieldDefinition( specialFieldId );

        List<String> keys = new ArrayList<String>();
        String curKey = "";
        boolean collecting = false;
        for ( int i = 0; i < definition.length(); i++ ) {
            char c = definition.charAt( i );
            if ( c == '[' ) {
                collecting = true;
                curKey = "";
            }

            if ( collecting && c != '[' && c != ']' ) {
                curKey += c;
            }

            if ( c == ']' ) {
                keys.add( curKey );
                collecting = false;
            }
        }
        return keys;
    }

    public static String getSpecialFieldValue( Map<String, Field> fielCollection, String specialFieldId ) {

        List<String> keys = getSpecialFieldComponents( specialFieldId );

        String definition = getSpecialFieldDefinition( specialFieldId );

        for ( String key : keys ) {
            Field field = fielCollection.get( key );
            if ( field == null ) {
                continue;
            }
            String replacement = field.getValue();
            replacement = StringUtils.isEmpty( replacement ) ? "empty" : replacement;
            definition = definition.replaceAll( "\\[" + key + "\\]", replacement );
        }

        if ( isSpecialFieldBlankFree( specialFieldId ) ) {
            definition = CommonUtil.sanitizeRecord( definition );
        }

        return definition;
    }

    public static KeyedResource getKeyedResource( String resourceName ) {
        KeyedResource resource = keyedResourceMap.get( resourceName );
        if ( resource == null ) {
            resource = new TabbedKeyedResource();
            resource.setSourceName( resourceName );
            resource.load();
            keyedResourceMap.put( resourceName, resource );
        }
        return resource;
    }


    private static boolean doit( Document document, String input, Set<String> backMap ) {

        StringTokenizer tokenized = new StringTokenizer( input, "/" );
        String cumulative = "";
        Element element;
        Element root = document.hasRootElement() ? document.getRootElement() : null;

        while ( tokenized.hasMoreTokens() ) {
            String nextToken = tokenized.nextToken();
            if ( root == null ) {
                root = new Element( nextToken );
                document.setRootElement( root );
            }

            element = getNode( document, cumulative + ( StringUtils.isEmpty( cumulative ) ? "" : "/" ) + nextToken );
            if ( element == null ) {
                Element parent = getNode( document, cumulative );
                if ( parent == null ) {
                    System.out.println( "Parent should not have been null: " + input );
                    backMap.add( input );
                    return false;
                }
                parent.addContent( new Element( cleanup( nextToken ) ) );
            }
            // else do nothing
            cumulative += ( StringUtils.isEmpty( cumulative ) ? "" : "/" ) + nextToken;
        }
        return true;
    }

    private static String cleanup( String input ) {
        if ( input.contains( "[" ) ) {
            String str[] = input.split( "\\[" );
            return str[0];
        }
        return input;
    }

    private static Element getNode( Document document, String input ) {
        try {
            XPath xPath = XPath.newInstance( input );
            List list = xPath.selectNodes( document );
            if ( list.isEmpty() ) {
                return null;
            }
            return ( Element ) list.get( 0 );
        } catch ( JDOMException e ) {
            throw new RuntimeException( e );
        }
    }

    public static XMLWrapper getBlankRecordXml() {
        Set<String> backMap = new HashSet<String>();
        Document document = new Document();
        for ( Field field : FieldManager.getFields() ) {
            String xpath = field.getXpath();
            if ( !StringUtils.isEmpty( xpath ) ) {
                doit( document, xpath, backMap );
            }
        }

        int attempts = 0;
        while ( !backMap.isEmpty() ) {
            for ( String item : backMap ) {
                boolean result = doit( document, item, backMap );
                if ( result ) {
                    backMap.remove( item );
                }
            }
            attempts++;
            if ( attempts > 50 ) {
                throw new RuntimeException( "Failed to build XML structure from fields!" );
            }
        }

        return new XMLWrapper( document );
    }

    public static void main( String s[] ) {
        XMLWrapper x = getBlankRecordXml();
        System.out.println( x.toString() );
    }
}
