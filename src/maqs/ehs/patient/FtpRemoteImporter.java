package maqs.ehs.patient;

import maqs.ehs.form.RemoteImporter;
import maqs.ehs.form.ResultCollector;
import maqs.ehs.form.ServerInfo;
import maqs.ehs.util.AppProperties;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * todo javadoc
 */
public class FtpRemoteImporter implements RemoteImporter {

    public List<File> importRemote( ResultCollector resultCollector, ServerInfo targetServerInfo ) {
        List<File> importFiles = new ArrayList<File>();

        boolean isReachable = ping( targetServerInfo );
        if ( !isReachable ) {
            return null;
        }

        FTPClient client = login( targetServerInfo );
        if ( client == null ) {
            return null;
        }

        try {
            client.setFileType( FTP.BINARY_FILE_TYPE );
            client.changeWorkingDirectory( "outgoing" );
            String[] fileNames = client.listNames();

            for ( String fileName : fileNames ) {
                try {
                    File local = captureFile( client, fileName );
                    if ( local != null ) {
                        importFiles.add( local );
                    }
                } catch ( IOException e ) {
                    // todo use an eror collector
                    e.printStackTrace();
                }
            }

        } catch ( IOException e ) {
            // error collector
        } finally {
            try {
                client.logout();
                client.disconnect();
            } catch ( Exception ex ) {
                // error collector
            }
        }

        return importFiles;
    }

    private File captureFile( FTPClient client, String fileName ) throws IOException {
        String incomingFile = AppProperties.getIncomingDirPath() + "/" + fileName;
        FileOutputStream fos = new FileOutputStream( incomingFile );
        client.retrieveFile( fileName, fos );
        fos.close();
        File importFile = new File( incomingFile );
        if ( importFile.exists() ) {
            return importFile;
        }
        return null;
    }

    public boolean isConnectionAvailable( ServerInfo targetServerInfo ) {
        // ping
        boolean isReachable = ping( targetServerInfo );

        if ( !isReachable ) {
            return false;
        }

        FTPClient client = login( targetServerInfo );
        if ( client == null ) {
            return false;
        }

        boolean hasRemoteFiles = false;
        try {
            client.changeWorkingDirectory( "outgoing" );
            String[] fileNames = client.listNames();
            hasRemoteFiles = fileNames != null && fileNames.length > 0;
        } catch ( IOException e ) {
            hasRemoteFiles = false;
        } finally {
            try {
                client.logout();
                client.disconnect();
            } catch ( Exception ex ) {
                //
            }
        }

        return hasRemoteFiles;
    }

    private boolean ping( ServerInfo targetServerInfo ) {
        boolean isReachable = false;
        try {
            FTPClient client = new FTPClient();
            client.connect( targetServerInfo.getHost() );
            if ( client.isConnected() ) {
                isReachable = true;
                client.disconnect();
            }
        } catch ( UnknownHostException e ) {
            System.out.println( "Failed to connect to " + targetServerInfo.getHost() );
        } catch ( Exception e ) {
            System.out.println( "Failed to connect to " + targetServerInfo.getHost() );
        }
        return isReachable;
    }

    private FTPClient login( ServerInfo targetServerInfo ) {
        // if reachable, get import file list
        String username = targetServerInfo.getUsername();
        String hostName = targetServerInfo.getHost();
        String password = targetServerInfo.getPassword();
        int timeOut = targetServerInfo.getTimeoutSeconds();

        FTPClient client = new FTPClient();
        boolean loginSuccessful;

        try {
            client.connect( hostName );
            client.setDefaultTimeout( timeOut * 1000 );
            loginSuccessful = client.login( username, password );
            if ( !loginSuccessful ) {
                client.disconnect();
            } else {
                return client;
            }
        } catch ( IOException e ) {
            return null;
        }
        return null;
    }

    public boolean deleteRemoteFile( String fileName, ServerInfo targetServerInfo ) {

        boolean isReachable = ping( targetServerInfo );
        if ( !isReachable ) {
            return false;
        }

        boolean success = false;
        FTPClient client = login( targetServerInfo );
        if ( client == null ) {
            return false;
        }
        try {
            client.changeWorkingDirectory( "outgoing" );
            client.deleteFile( fileName );
            success = true;
        } catch ( IOException e ) {
            success = false;
        } finally {
            try {
                client.logout();
                client.disconnect();
            } catch ( Exception ex ) {
                success = false;
            }
        }

        return success;
    }


}
