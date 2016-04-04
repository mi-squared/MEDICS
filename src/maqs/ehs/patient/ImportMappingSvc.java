package maqs.ehs.patient;

import maqs.ehs.util.IniWrapper;

import java.util.*;

public class ImportMappingSvc {

    private static Map<Integer, PositionFieldMapping> importMap;
    private static final String IMPORT_MAPPING_DEFS_LOCATION = "conf/import_defs.ini";

    public static PositionFieldMapping getPositionFieldMapping( Integer position ) {
        if ( importMap == null ) {
            fillImportMap();
        }

        return importMap.get( position );
    }

    private static void fillImportMap() {
        importMap = new LinkedHashMap<Integer, PositionFieldMapping>();

        IniWrapper iniWrapper = new IniWrapper( IMPORT_MAPPING_DEFS_LOCATION );

        List positions = iniWrapper.getSectionKeys();
        for ( Object key : positions ) {
            String positionStr = ( String ) key;

            String fieldId = iniWrapper.getSectionKeyValue( positionStr, "fieldid" );
            String regex = iniWrapper.getSectionKeyValue( positionStr, "regex" );
            String errordescription = iniWrapper.getSectionKeyValue( positionStr, "errordescription" );

            PositionFieldMapping mapping = new PositionFieldMapping();
            mapping.setFieldId( fieldId );
            mapping.setColumnValidator( new ColumnValidator( regex, errordescription ) );

            importMap.put( Integer.valueOf( positionStr ), mapping );
        }
    }

    public static Collection<PositionFieldMapping> getAllPositionFieldMappings() {
        if ( importMap == null ) {
            fillImportMap();
        }
        return importMap.values();
    }

}
