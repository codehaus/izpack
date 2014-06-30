package com.izforge.izpack.panels.userinput.gui.custom;

import com.izforge.izpack.panels.userinput.gui.Component;
import com.izforge.izpack.panels.userinput.gui.GUIField;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class CustomInputRows extends JPanel
{
    private final List<GUIField> fields;

    private int numberOfRows = 1;

    public CustomInputRows(List<GUIField> fields)
    {
        super(new GridLayout(0, 1), true);
        this.fields = fields;
        addRow();
    }

    /**
     * Add an additional row of fields defined by the user.
     */
    public void addRow()
    {
        for (GUIField field : fields)
        {
            for( Component component : field.getComponents())
            {
                try
                {
                    JComponent jComponent = (JComponent) cloneJComponent(component.getComponent());
                    if (!(jComponent instanceof JLabel))
                    {
                        this.add(jComponent);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }

        numberOfRows++;
        revalidate();
        repaint();
    }

    /**
     * Remove last added row of fields defined by the user.
     */
    public void removeRow()
    {
        if (numberOfRows <= 1)
        {
            return;
        }
        this.remove(this.getComponentCount() - 1);

        numberOfRows--;
        revalidate();
        repaint();
    }

    /**
     * Make a deep clone of a JComponent
     *
     * @param oldObj
     * @return
     * @throws Exception
     */
    static public JComponent cloneJComponent(JComponent oldObj) throws Exception
    {
        ObjectOutputStream objectOutputStream = null;
        ObjectInputStream objectInputStream = null;

        try
        {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(oldObj);
            objectOutputStream.flush();

            ByteArrayInputStream byteArrayInputStream =   new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            return (JComponent) objectInputStream.readObject();
        }
        catch (Exception e)
        {
            System.out.println("Failed to clone JComponent" + e);
            throw (e);
        }
        finally
        {
            objectOutputStream.close();
            objectInputStream.close();
        }
    }
}
