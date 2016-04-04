package maqs.ehs.util;

import com.sigilent.business.util.StringUtils;

import java.io.File;

import maqs.ehs.BuildInfo;
import maqs.ehs.form.ServerInfo;

public class AppProperties {

    public enum ReportExportMethod {
        FTP,
        MAIL;

        public boolean isFtp() {
            return this == FTP;
        }
        public boolean isMail() {
            return this == MAIL;
        }
    }

    private static String outgoingDirPath;
    private static String gpgPath;
    private static String backupDirPath;
    private static String resourceDirPath;
    private static String getAppTitle;
    private static String username;
    private static String serverPublicKeyId;
    private static String versionNumber;
    private static String buildNumber;
    private static String splashImage;
    private static String iconImage;
    private static String rowLength;
    private static String saveBlankRequiredField;
    private static String maxSearches;

    private static String ftpKeyServer;
    private static String ftpKeyServerLoginTimeoutSec;

    private static String ftpExportDrop;
    private static String ftpExportDropLoginTimeoutSec;
    private static String ftpExportDropUserFolder;

    private static String ftpImport;
    private static String ftpImportLoginTimeoutSec;

    private static String mailExportDrop;
    private static String mailExportDropUser;

    private static String pgpKeyUploaded;
    private static String pgpKeyUploadedReminder;
    private static String saveRequiresRecordnameFields;
    private static String remoteExportEnabled;
    private static String remoteImportEnabled;
    private static String remoteExportMethod;

    private static ServerInfo ftpKeyServerInfo;
    private static ServerInfo exportDropInfo;
    private static ServerInfo importInfo;

    /**
     * supported modes:
     * disabled
     * zip
     * concat
     */
    private static String batchExportMode;

    private static String passPhrase;
    private static final String WORK_DIR = "work";
    private static final String INCOMING_DIR = "incoming";
    private static final String TEMP_DIR = "temp";
    private static final String BATH_DIR = "batch";
    private static final String IMAGE_DIR = "image";
    private static final String DOC_DIR = "docs";

    static final String CONFIG_INI_PATH = "conf/config.ini";
    private static String GPG_BIN_DIR = "bin/gpg";
    private static final String TEST_DIR = "encrypttest";
    private static final String KEYSTORE_DIR = "keystoredir";
    private static final String DEFAULT_ROWLENGTH = "300";
    private static final String DEFAULT_MAX_SEARCHES = "30";
    static {
        IniWrapper ini = new IniWrapper( CONFIG_INI_PATH );

        // config
        outgoingDirPath = ini.getSectionKeyValue( "config", "outgoingdir" );
        gpgPath = ini.getSectionKeyValue( "config", "gpgpath" );
        backupDirPath = ini.getSectionKeyValue( "config", "backupdir" );
        resourceDirPath = ini.getSectionKeyValue( "config", "resourcedir" );
        getAppTitle = ini.getSectionKeyValue( "config", "apptitle" );
        username = ini.getSectionKeyValue( "config", "username" );
        serverPublicKeyId = ini.getSectionKeyValue( "config", "serverpublickeyid" );
        versionNumber = BuildInfo.VERSION_NUMBER;
        buildNumber = BuildInfo.BUILD_NUMBER;
        splashImage = ini.getSectionKeyValue( "config", "splashimage" );
        iconImage = ini.getSectionKeyValue( "config", "iconimage" );
        rowLength = ini.getSectionKeyValue( "config", "rowlength" );
        batchExportMode = ini.getSectionKeyValue( "config", "batchexportmode" );
        saveBlankRequiredField = ini.getSectionKeyValue( "config", "saveblankrequiredfield" );
        saveRequiresRecordnameFields = ini.getSectionKeyValue( "config", "saverequiresrecordnamefields" );
        remoteExportEnabled = ini.getSectionKeyValue( "config", "remoteexportenabled" );
        remoteImportEnabled = ini.getSectionKeyValue( "config", "remoteimportenabled" );
        remoteExportMethod= ini.getSectionKeyValue( "config", "remoteexportmethod" );
        maxSearches = ini.getSectionKeyValue( "config", "maxsearches" );

        // ftp keyserver
        ftpKeyServer = ini.getSectionKeyValue( "ftp-keyserver", "ftp-keyserver" );
        ftpKeyServerLoginTimeoutSec = ini.getSectionKeyValue( "ftp-keyserver", "ftp-keyserver-login-timeout-seconds" );

        // ftp exportdrop
        ftpExportDrop = ini.getSectionKeyValue( "ftp-exportdrop", "ftp-exportdrop" );
        ftpExportDropLoginTimeoutSec = ini.getSectionKeyValue( "ftp-exportdrop", "ftp-exportdrop-login-timeout-seconds" );
        ftpExportDropUserFolder = ini.getSectionKeyValue( "ftp-exportdrop", "ftp-exportdrop-userfolder" );

        // ftp import
        ftpImport = ini.getSectionKeyValue( "ftp-import", "ftp-import" );
        ftpImportLoginTimeoutSec = ini.getSectionKeyValue( "ftp-import", "ftp-import-login-timeout-seconds" );

        // mail exportdrop
        mailExportDrop = ini.getSectionKeyValue( "mail-exportdrop", "mail-exportdrop" );
        mailExportDropUser = ini.getSectionKeyValue( "mail-exportdrop", "mail-exportdrop-user" );

        // state
        pgpKeyUploaded = ini.getSectionKeyValue( "state", "userpublickeyuploaded" );
        pgpKeyUploadedReminder = ini.getSectionKeyValue( "state", "remindpublickeyupload" );
    }

    public static String getWorkDirPath() {
        return WORK_DIR;
    }

    public static String getDocDirPath() {
        return DOC_DIR;
    }

    public static String getImageDirPath() {
        return IMAGE_DIR;
    }

    public static String getTempDirPath() {
        return TEMP_DIR;
    }

    public static String getOutgoingDirPath() {
        return outgoingDirPath;
    }

    public static String getIncomingDirPath() {
        return INCOMING_DIR;
    }

    public static boolean isBackupDirConfigured() {
        return !StringUtils.isEmpty( backupDirPath )
                && new File( backupDirPath ).exists();
    }

    public static String getBackupDirPath() {
        return backupDirPath;
    }

    public static String getResourceDirPath() {
        return resourceDirPath;
    }

    public static String getGpgPath() {
        if ( StringUtils.isEmpty( gpgPath ) ) {
            return FileSystemManager.isWindows() ? GPG_BIN_DIR : "/usr/bin";
        } else {
            return gpgPath;
        }
    }

    public static String getBatchPath() {
        return BATH_DIR;
    }

    public static String getAppTitle() {
        return getAppTitle;
    }

    public static String getAppTitleFull() {
        return getAppTitle + " v" + AppProperties.getVersionNumber();
    }

    public static void setOutgoingDirPath( String newDir ) {
        IniWrapper ini = new IniWrapper( CONFIG_INI_PATH );
        ini.updateSectionKeyValue( "config", "outgoingdir", newDir );
        ini.writeIni();
        outgoingDirPath = newDir;
    }

    public static void setBackupDirPath( String newDir ) {
        IniWrapper ini = new IniWrapper( CONFIG_INI_PATH );
        ini.updateSectionKeyValue( "config", "backupdir", newDir );
        ini.writeIni();
        backupDirPath = newDir;
    }

    public static String getPassPhrase() {
        return passPhrase;
    }

    public static void setPassPhrase( String passPhrase ) {
        AppProperties.passPhrase = passPhrase;
    }


    public static String getGetAppTitle() {
        return getAppTitle;
    }

    public static void setGetAppTitle( String getAppTitle ) {
        AppProperties.getAppTitle = getAppTitle;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername( String _username ) {
        IniWrapper ini = new IniWrapper( CONFIG_INI_PATH );
        ini.updateSectionKeyValue( "config", "username", _username );
        ini.writeIni();
        username = _username;
    }

    public static String getServerPublicKeyId() {
        return serverPublicKeyId;
    }

    public static void setServerPublicKeyId( String _serverPublicKeyId ) {
        IniWrapper ini = new IniWrapper( CONFIG_INI_PATH );
        ini.updateSectionKeyValue( "config", "serverpublickeyid", _serverPublicKeyId );
        ini.writeIni();
        serverPublicKeyId = _serverPublicKeyId;
    }

    public static String getTestDir() {
        return TEST_DIR;
    }

    public static String getKeyStoreDir() {
        return KEYSTORE_DIR;
    }

    public static String getVersionNumber() {
        return versionNumber;
    }

    public static String getBuildNumber() {
        return buildNumber;
    }

    public static String getSplashImagePath() {
        return AppProperties.getImageDirPath() + FileSystemManager.getSlash() + splashImage;
    }

    public static String getIconImagePath() {
        return AppProperties.getImageDirPath() + FileSystemManager.getSlash() + iconImage;
    }

    public static String getSeachImagePath() {
        return AppProperties.getImageDirPath() + FileSystemManager.getSlash() + "search.png";
    }

    public static String getLocalPublicKeyFilePath() {
        return FileSystemManager.getGpgKeyStoreDir() + FileSystemManager.getSlash() + "pub_key_" + getUsername() + ".txt";
    }

    public static int getRowLength() {
        if ( StringUtils.isEmpty( rowLength ) ) {
            rowLength = DEFAULT_ROWLENGTH;
        }
        return Integer.parseInt( rowLength );
    }

    public static int getMaxSearches() {
        if ( StringUtils.isEmpty( maxSearches ) ) {
            maxSearches = DEFAULT_MAX_SEARCHES;
        }
        return Integer.parseInt( maxSearches );
    }

    public static void setRowLength( int rowLength ) {
        AppProperties.rowLength = String.valueOf( rowLength );
    }

    public static String getBatchExportMode() {
        return batchExportMode;
    }

    public static boolean isBatchExportEnabled() {
        return !StringUtils.isEmpty( batchExportMode ) && !"disabled".equals( batchExportMode.toLowerCase() );
    }

    public static boolean isPgpKeyUploaded() {
        return !StringUtils.isEmpty( pgpKeyUploaded ) && "true".equals( pgpKeyUploaded.toLowerCase() );
    }

    public static void setPgpKeyUploaded() {
        IniWrapper ini = new IniWrapper( CONFIG_INI_PATH );
        ini.updateSectionKeyValue( "state", "userpublickeyuploaded", "true" );
        ini.writeIni();
        pgpKeyUploaded = "true";
    }

    public static boolean isPgpKeyUploadedReminder() {
        return !StringUtils.isEmpty( pgpKeyUploadedReminder ) && "true".equals( pgpKeyUploadedReminder.toLowerCase() );
    }

    public static void setPgpKeyUploadedReminder() {
        IniWrapper ini = new IniWrapper( CONFIG_INI_PATH );
        ini.updateSectionKeyValue( "state", "remindpublickeyupload", "false" );
        ini.writeIni();
        pgpKeyUploadedReminder = "false";
    }

    public static boolean isBatchExportModeConcat() {
        return isBatchExportEnabled() && "concat".equals( batchExportMode.toLowerCase() );
    }

    public static boolean isBatchExportModeZip() {
        return isBatchExportEnabled() && "zip".equals( batchExportMode.toLowerCase() );
    }

    public static boolean isBatchExportModeDefault() {
        return isBatchExportEnabled() && "default".equals( batchExportMode.toLowerCase() );
    }

    public static boolean isSaveRequiredBlankFieldEnabled() {
        return saveBlankRequiredField != null && "true".equals( saveBlankRequiredField.toLowerCase() );
    }

    public static ServerInfo getFtpKeyServerInfo() {
        if ( ftpKeyServerInfo == null ) {
            ftpKeyServerInfo = new ServerInfo();
            ftpKeyServerInfo.setHost( ftpKeyServer );
            ftpKeyServerInfo.setUsername( getUsername() );
            ftpKeyServerInfo.setPassword( getPassPhrase() );
            ftpKeyServerInfo.setPort( 21 );

            int timeout = 2;
            {
                if ( !StringUtils.isEmpty( ftpKeyServerLoginTimeoutSec  ) ) {
                    try {
                        timeout = Integer.parseInt( ftpKeyServerLoginTimeoutSec );
                    } catch ( NumberFormatException e ) {
                        //
                    }
                }
            }
            ftpKeyServerInfo.setTimeoutSeconds( timeout );
        }
        return ftpKeyServerInfo;
    }

    public static ServerInfo getExportDropInfo() {

        if ( exportDropInfo == null ) {

            if ( getRemoteExportMethod().isFtp() ) {
                exportDropInfo = new ServerInfo();
                exportDropInfo.setHost( ftpExportDrop );
                exportDropInfo.setUsername( getUsername() );
                exportDropInfo.setPassword( getPassPhrase() );
                exportDropInfo.setPort( 21 );

                int timeout = 2;
                {
                    if ( !StringUtils.isEmpty( ftpExportDropLoginTimeoutSec  ) ) {
                        try {
                            timeout = Integer.parseInt( ftpExportDropLoginTimeoutSec );
                        } catch ( NumberFormatException e ) {
                            //
                        }
                    }
                }
                exportDropInfo.setTimeoutSeconds( timeout );
            }

            if ( getRemoteExportMethod().isMail() ) {
                exportDropInfo = new ServerInfo();
                exportDropInfo.setHost( mailExportDrop );
                exportDropInfo.setUsername( mailExportDropUser );
            }

        }
        return exportDropInfo;
    }

    public static ServerInfo getImportInfo() {

        if ( importInfo == null ) {

            importInfo = new ServerInfo();
            importInfo.setHost( ftpImport );
            importInfo.setUsername( getUsername() );
            importInfo.setPassword( getPassPhrase() );
            importInfo.setPort( 21 );

            int timeout = 2;
            {
                if ( !StringUtils.isEmpty( ftpImportLoginTimeoutSec  ) ) {
                    try {
                        timeout = Integer.parseInt( ftpImportLoginTimeoutSec );
                    } catch ( NumberFormatException e ) {
                        //
                    }
                }
            }
            importInfo.setTimeoutSeconds( timeout );
        }
        return importInfo;
    }

    public static boolean isSaveRequiresRecordnameFields() {
        return saveRequiresRecordnameFields != null && "true".equals( saveRequiresRecordnameFields.toLowerCase() );
    }

    public static boolean isRemoteExportEnabled() {
        return remoteExportEnabled != null && "true".equals( remoteExportEnabled.toLowerCase() );
    }

    public static boolean isRemoteImportEnabled() {
        return remoteImportEnabled != null && "true".equals( remoteImportEnabled.toLowerCase() );
    }

    public static boolean isFtpExportDropUserFolder() {
        return ftpExportDropUserFolder != null && "true".equals( ftpExportDropUserFolder.toLowerCase() );
    }

    public static ReportExportMethod getRemoteExportMethod() {
        if ( "mail".equals( remoteExportMethod )) {
            return ReportExportMethod.MAIL;
        }
        if ( "ftp".equals( remoteExportMethod )) {
            return ReportExportMethod.FTP;
        }
        return null;
    }
}
