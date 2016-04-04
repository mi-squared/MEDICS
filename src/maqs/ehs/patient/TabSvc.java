package maqs.ehs.patient;

import maqs.ehs.util.IniWrapper;

import java.util.ArrayList;
import java.util.List;

import com.sigilent.business.util.StringUtils;

public class TabSvc {

    private static final String TAB_DEFS_LOCATION = "conf/tabs_defs.ini";

    private static List<Tab> tabs;

    private static void initTabs() {

        tabs = new ArrayList<Tab>();

        IniWrapper iniWrapper = new IniWrapper( TAB_DEFS_LOCATION );

        for ( Object o : iniWrapper.getSectionKeys() ) {
            String tabId = ( String ) o;

            Tab tab = new Tab();
            tab.setId( tabId );
            tab.setTitle( iniWrapper.getSectionKeyValue( tabId, "title" ) );

            String maxColumnRows = iniWrapper.getSectionKeyValue( tabId, "maxrowspercolumn" );
            if ( !StringUtils.isEmpty( maxColumnRows ) ) {
                tab.setMaxRowsPerColumn( Integer.parseInt( maxColumnRows ) );
            } else {
                tab.setMaxRowsPerColumn( 24 );
            }

            tabs.add( tab );
        }
    }

    public static List<Tab> getTabs() {
        if ( tabs == null ) {
            initTabs();
        }
        return tabs;
    }

}
