package maqs.ehs.util;

import com.sigilent.business.util.StringUtils;

import java.io.*;
import java.util.*;

public class IniWrapper {

    Map sections = new LinkedHashMap();
    String inputfile = "";

    public IniWrapper( String inputfile ) {
        this.inputfile = inputfile;
        populate();
    }

    public IniWrapper() {
    }

    public void setIniFile( String inputfile ) {
        this.inputfile = inputfile;
    }

    public String getIniFileName() {
        return this.inputfile;
    }

    public void populate() {
        File f = new File( inputfile );

        try {
            DataInputStream dis = new DataInputStream( new BufferedInputStream( new FileInputStream( f ) ) );
            String line = "";
            String section = "";
            while ( ( line = dis.readLine() ) != null ) {
                if ( line.startsWith( "[" ) && line.endsWith( "]" ) ) {
                    section = line.substring( line.indexOf( "[" ) + 1, line.lastIndexOf( "]" ) );
                    sections.put( section, new LinkedHashMap() );
                    continue;
                }

                String currentpair = "";
                if ( line.lastIndexOf( "#" ) > -1 ) {
                    currentpair = line.substring( line.lastIndexOf( "#" ), line.length() );
                } else {
                    currentpair = line;
                }

                if ( currentpair.indexOf( "=" ) == -1 ) {
                    continue;
                }

                LinkedHashMap currentSectionHashtable = ( LinkedHashMap ) this.sections.get( section );
                String key = currentpair.substring( 0, currentpair.indexOf( "=" ) );
                String value = currentpair.substring( currentpair.indexOf( "=" ) + 1, currentpair.length() );


                if ( !key.equals( "" ) ) {
                    currentSectionHashtable.put( key, value );
                    this.sections.remove( section );
                    this.sections.put( section, currentSectionHashtable );
                }
            }
            dis.close();
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    public List getSectionKeys() {
        List sectionkeys = new ArrayList();
        Iterator e = this.sections.keySet().iterator();

        try {
            while ( e.hasNext() ) {
                sectionkeys.add( e.next() );
            }
        } catch ( Exception err ) {
            return null;
        }
        return sectionkeys;
    }

    public String getSectionKeyValue( String section, String key ) {
        String value = "";
        if ( hasSectionKey( section, key ) ) {
            value = ( String ) ( ( LinkedHashMap ) ( sections.get( section ) ) ).get( key );
        }

        return value;
    }

    public boolean hasSection( String section ) {
        if ( !sections.containsKey( section ) ) return false;

        return true;
    }

    public boolean hasSectionKey( String section, String key ) {
        if ( !hasSection( section ) ) {
            return false;
        } else {
            if ( !( ( LinkedHashMap ) ( sections.get( section ) ) ).containsKey( key ) ) return false;
        }
        return true;
    }

    public boolean hasSectionKeyValue( String section, String key, String value ) {
        if ( !hasSectionKey( section, key ) ) {
            return false;
        } else {
            if ( !( ( ( LinkedHashMap ) ( sections.get( section ) ) ).get( key ) ).equals( value ) ) {
                return false;
            }

        }
        return true;
    }

    public boolean updateSectionKeyValue( String section, String key, String value ) {
        if ( sections.containsKey( section ) ) {
            if ( ( ( LinkedHashMap ) ( sections.get( section ) ) ).containsKey( key ) ) {
                ( ( LinkedHashMap ) ( sections.get( section ) ) ).remove( key );
                ( ( LinkedHashMap ) ( sections.get( section ) ) ).put( key, value );
            } else {
                ( ( LinkedHashMap ) ( sections.get( section ) ) ).put( key, value );
            }
        } else {
            LinkedHashMap newsectionhash = new LinkedHashMap();
            newsectionhash.put( key, StringUtils.noNull( value ) );
            sections.put( section, newsectionhash );
        }
        return true;
    }

    public boolean removeSectionKey( String section, String key ) {
        if ( hasSectionKey( section, key ) ) {
            ( ( LinkedHashMap ) ( sections.get( section ) ) ).remove( key );
        } else {
            return false;
        }
        return true;
    }

    public LinkedHashMap getSectionKeyValueSet( String section ) {
        return ( LinkedHashMap ) this.sections.get( section );
    }

    public boolean writeIni( String filename ) {
        Iterator sectionkeys = this.sections.keySet().iterator();
        File f = new File( filename );

        try {
            FileWriter fw = new FileWriter( f );

            while ( sectionkeys.hasNext() ) {
                String sectionlabel = ( String ) sectionkeys.next();
                fw.write( "[" + sectionlabel + "]\r\n" );

                LinkedHashMap sectionpairs =  (LinkedHashMap) sections.get( sectionlabel );

                for ( Object currentpairkeyObj : sectionpairs.keySet() ) {
                    String currentpairkey = ( String ) currentpairkeyObj;
                    String currentpairvalue = ( String ) sectionpairs.get( currentpairkey );
                    fw.write( currentpairkey + "=" + currentpairvalue + "\r\n" );
                }
            }
            fw.close();
        } catch ( Exception err ) {
            return false;
        }


        return true;
    }

    public void removeSection( String key ) {
        sections.remove( key );
    }

    public boolean writeIni() {
        return writeIni( inputfile );
    }

    public boolean isEmpty() {
        return sections.isEmpty();
    }

    public void addSection( String sectionKey ) {
        LinkedHashMap newsectionhash = new LinkedHashMap();
        sections.put( sectionKey, newsectionhash );
    }
}