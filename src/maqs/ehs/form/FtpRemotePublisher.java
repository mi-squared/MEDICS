package maqs.ehs.form;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;

import maqs.ehs.util.AppProperties;
import com.sigilent.business.util.StringUtils;

public class FtpRemotePublisher implements RemotePublisher {
    
    public boolean publishRemote( File file, ResultCollector resultCollector, ServerInfo targetServerInfo, String destinationDir ) {

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
            }
        } catch ( IOException e ) {
            resultCollector.addResult( new ProcessingResult( ResultType.FATAL, file.getName(),
                    "Failed to connect to server '" + hostName + "'." ) );
            return false;
        }

        if ( !loginSuccessful ) {
            resultCollector.addResult( new ProcessingResult( ResultType.FATAL, file.getName(),
                    "Failed to authenticate user '" + username + "'." ) );
            return false;
        }

        try {
            if ( AppProperties.isFtpExportDropUserFolder() ) {
                // create a dedicated folder based on username
                client.mkd( AppProperties.getUsername() );
                client.changeWorkingDirectory( AppProperties.getUsername() );
            }
            if ( !StringUtils.isEmpty( destinationDir ) ) {
                client.mkd( "incoming" );
                client.changeWorkingDirectory( "incoming" );
            }
            client.setFileType( FTP.BINARY_FILE_TYPE );
            client.enterLocalPassiveMode();
            InputStream input = new FileInputStream( file.getAbsolutePath() );
            client.storeFile( file.getName(), input );
            input.close();
        } catch ( IOException e ) {
            resultCollector.addResult( new ProcessingResult( ResultType.FATAL, file.getName(),
                    "Failed to upload file '" + file.getAbsolutePath() + "'." + "'." ) );
            return false;
        }

        boolean found = false;
        try {
            String[] fileNames = client.listNames();
            if ( fileNames != null ) {
                for ( String remoteFile : fileNames ) {
                    if ( remoteFile.equalsIgnoreCase( file.getName() ) ) {
                        found = true;
                        break;
                    }
                }
            }
        } catch ( IOException e ) {
            resultCollector.addResult( new ProcessingResult( ResultType.FATAL, file.getName(),
                    "Failed to verify upload for file '" + file.getAbsolutePath() + "'; encountered exception." ) );
            return false;
        }

        if ( !found ) {
            resultCollector.addResult( new ProcessingResult( ResultType.FATAL, file.getName(),
                    "Failed to verify upload for file '" + file.getAbsolutePath() + "'." ) );
        }

        try {
            client.logout();
            client.disconnect();
        } catch ( Exception e ) {
            return false;
        }
        resultCollector.addResult( new ProcessingResult( ResultType.INFO, file.getName(), "Successfully uploaded " + file.getName() + "." ) );

        return true;
    }

    public boolean isConnectionAvailable( ServerInfo targetServerInfo ) {
        // ping
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
}
