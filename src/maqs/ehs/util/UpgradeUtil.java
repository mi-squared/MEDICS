package maqs.ehs.util;

import java.io.File;
import java.util.LinkedHashMap;

public class UpgradeUtil {

    /**
     * Copy these directories / files:
     * bin/*.*
     * classes/*.*
     * lib/*.*
     * resource/*.*
     * .sh
     * .bat
     * @param target
     * @param source
     */
    public static void upgradeInstallationDir( File target, File source ) {

        // 1.
        upgradeCoreArtefacts( source, target );

        // 2.
        upgradeCustomizedArtefacts( source, target );
    }

    private static void upgradeCustomizedArtefacts( File source, File target ) {
        // Only config.ini changes are supported at this time
        IniWrapper targetIni = new IniWrapper( new File( target, AppProperties.CONFIG_INI_PATH ).getAbsolutePath() );
        IniWrapper srcIni = new IniWrapper( new File( source, AppProperties.CONFIG_INI_PATH ).getAbsolutePath() );

        for ( Object o : srcIni.getSectionKeys() ) {
            String srcSectionKey = ( String ) o;

            for ( Object o1 : srcIni.getSectionKeyValueSet( srcSectionKey ).keySet() ) {
                String srcValueKey = ( String ) o1;
                String srcValue = srcIni.getSectionKeyValue( srcSectionKey, srcValueKey );

                LinkedHashMap targetSection = targetIni.getSectionKeyValueSet( srcSectionKey );
                if ( targetSection == null ) {
                    targetIni.addSection( srcSectionKey );
                    targetSection = targetIni.getSectionKeyValueSet( srcSectionKey );
                }

                if ( !targetSection.keySet().contains( srcValueKey ) ) {
                    targetIni.updateSectionKeyValue( srcSectionKey, srcValueKey, srcValue );
                }
            }
        }

        targetIni.writeIni();
    }

    /**
     * Merges missing keys from target deployment from source deployment, which assumes it is the later one
     *
     * @param source
     * @param target
     */
    private static void upgradeCoreArtefacts( File source, File target ) {
        String batchFileToUse;
        if ( FileSystemManager.isLinux() ) {
            batchFileToUse = AppProperties.getBatchPath() + FileSystemManager.getSlash() + "upgrade_core.sh";
        } else if ( FileSystemManager.isWindows() ) {
            batchFileToUse = AppProperties.getBatchPath() + FileSystemManager.getSlash() + "upgrade_core.bat";
        } else {
            throw new RuntimeException( "System is not linux or windows. Cannot upgrade." );
        }

        StringBuffer scriptSb = new StringBuffer();
        scriptSb.append( batchFileToUse );

        FileSystemManager.executeShellCmd(
                scriptSb.append( " " )
                        .append( source.getAbsolutePath() )
                        .append( " " )
                        .append( target.getAbsolutePath() ) );
    }

}
