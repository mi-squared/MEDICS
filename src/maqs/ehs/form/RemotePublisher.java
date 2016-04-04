package maqs.ehs.form;

import java.io.File;

public interface RemotePublisher {

    public boolean publishRemote( File file, ResultCollector resultCollector, ServerInfo targetServerInfo, String destinationDir );

    public boolean isConnectionAvailable( ServerInfo targetServerInfo );

}
