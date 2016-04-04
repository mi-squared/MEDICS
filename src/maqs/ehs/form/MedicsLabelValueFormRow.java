package maqs.ehs.form;

import javax.swing.*;
import java.awt.*;

interface MedicsLabelValueFormRow {

    public JPanel getAsJPanel();

    public void setLabelComponent( Component component );

    public void setValueComponent( Component component, int rows );

    public void setScrolling( boolean scrolling );

    public void setRequired( boolean required );
}
