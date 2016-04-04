package maqs.ehs.form;

import javax.swing.*;
import java.awt.*;

interface MedicsBlockFormRow {
    public JPanel getAsJPanel();

    public void setValueComponent( Component component, int textRows );

    public void setScrolling( boolean scrolling );
}