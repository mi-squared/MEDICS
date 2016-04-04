package maqs.ehs.patient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

public abstract class KeyedResource {

    protected LinkedHashMap<String, KeyedResourceEntry> entries = new LinkedHashMap<String, KeyedResourceEntry>();
    private String sourceName;

    public abstract void load();

    public KeyedResourceEntry getResource( String key ) {
        return entries.get( key );
    }

    public List<KeyedResourceEntry> searchResourceByKey( String keyFragment, int maxItems ) {
        List<KeyedResourceEntry> found = new ArrayList<KeyedResourceEntry>();
        for ( String key : entries.keySet() ) {
            if ( found.size() >= maxItems ) {
                break;
            }
            if ( key.toUpperCase().contains( keyFragment.toUpperCase() ) ) {
                found.add( getResource( key ) );
            }
        }
        return found;
    }

    public List<KeyedResourceEntry> searchResourceByValue( String valueFragment, int maxItems ) {
        List<KeyedResourceEntry> found = new ArrayList<KeyedResourceEntry>();
        for ( KeyedResourceEntry resource : entries.values() ) {
            if ( found.size() >= maxItems ) {
                break;
            }
            if ( resource.getValue().toUpperCase().contains( valueFragment.toUpperCase() ) ) {
                found.add( resource );
            }
        }
        return found;
    }

    public List<KeyedResourceEntry> searchResource( String searchTerm, int maxItems ) {
        List<KeyedResourceEntry> found = new ArrayList<KeyedResourceEntry>();
        for ( KeyedResourceEntry resource : entries.values() ) {
            if ( found.size() >= maxItems ) {
                break;
            }
            if ( resource.getValue().toUpperCase().contains( searchTerm.toUpperCase() ) || resource.getKey().toUpperCase().contains( searchTerm.toUpperCase() ) ) {
                found.add( resource );
            }
        }
        return found;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName( String sourceName ) {
        this.sourceName = sourceName;
    }

    protected void addEntry( String key, String value) {
        KeyedResourceEntry entry = new KeyedResourceEntry();
        entry.setKey( key  );
        entry.setValue( value );
        entries.put( key, entry );
    }
}
