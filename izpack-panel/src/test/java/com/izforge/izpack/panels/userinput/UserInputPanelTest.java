/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.panels.userinput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.swing.JCheckBox;

import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.FrameFixture;
import org.fest.swing.fixture.JComboBoxFixture;
import org.fest.swing.fixture.JRadioButtonFixture;
import org.fest.swing.fixture.JTextComponentFixture;
import org.fest.swing.timing.Timeout;
import org.junit.Ignore;
import org.junit.Test;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.InstallData;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.data.DynamicVariableImpl;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.gui.IconsDatabase;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.data.UninstallDataWriter;
import com.izforge.izpack.panels.simplefinish.SimpleFinishPanel;
import com.izforge.izpack.panels.test.AbstractPanelTest;
import com.izforge.izpack.panels.test.TestGUIPanelContainer;
import com.izforge.izpack.panels.userinput.field.KeyValue;
import com.izforge.izpack.test.Container;


/**
 * Tests the {@link UserInputPanel}.
 *
 * @author Tim Anderson
 */
@Container(TestGUIPanelContainer.class)
public class UserInputPanelTest extends AbstractPanelTest
{

    /**
     * Constructs an {@code UserInputPanelTest}.
     *
     * @param container           the test container
     * @param installData         the installation data
     * @param resourceManager     the resource manager
     * @param factory             the panel factory
     * @param rules               the rules
     * @param icons               the icons
     * @param uninstallDataWriter the uninstallation data writer
     */
    public UserInputPanelTest(TestGUIPanelContainer container, GUIInstallData installData,
                              ResourceManager resourceManager, ObjectFactory factory, RulesEngine rules,
                              IconsDatabase icons, UninstallDataWriter uninstallDataWriter)
    {
        super(container, installData, resourceManager, factory, rules, icons, uninstallDataWriter);
    }

    /**
     * Tests rule fields.
     *
     * @throws Exception for any error
     */
    @Test
    public void testRuleField() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/rule/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/rule/");
        InstallData installData = getInstallData();

        // show the panel
        FrameFixture frame = showUserInputPanel();

        JTextComponentFixture rule1 = frame.textBox("rule1.1");
        assertEquals("80", rule1.text());

        assertNull(installData.getVariable("rule1"));

        rule1.setText("12345");

        // attempt to navigate to the next panel
        checkNavigateNext(frame);

        assertEquals("12345", installData.getVariable("rule1"));
    }

    /**
     * Tests text fields.
     *
     * @throws Exception for any error
     */
    @Test
    public void testTextField() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/text/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/text/");
        InstallData installData = getInstallData();

        installData.setVariable("text3value", "text3 default value");

        // show the panel
        FrameFixture frame = showUserInputPanel();

        JTextComponentFixture text1 = frame.textBox("text1");
        assertEquals("", text1.text());

        JTextComponentFixture text2 = frame.textBox("text2");
        assertEquals("text2 value", text2.text());

        JTextComponentFixture text3 = frame.textBox("text3");
        assertEquals("text3 default value", text3.text());

        assertNull(installData.getVariable("text1"));
        assertNull(installData.getVariable("text2"));
        assertNull(installData.getVariable("text3"));

        text1.setText("text1 value");

        // attempt to navigate to the next panel
        checkNavigateNext(frame);

        assertEquals("text1 value", installData.getVariable("text1"));
        assertEquals("text2 value", installData.getVariable("text2"));
        assertEquals("text3 default value", installData.getVariable("text3"));
    }

    /**
     * Tests combo fields.
     *
     * @throws Exception for any error
     */
    @Test
    public void testComboField() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/combo/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/combo/");

        InstallData installData = getInstallData();

        installData.setVariable("combo2", "value3"); // should select the 3rd value

        // show the panel
        FrameFixture frame = showUserInputPanel();

        // for combo1, the initial selection is determined by the 'set' attribute
        JComboBoxFixture combo1 = frame.comboBox("combo1");
        KeyValue item1 = (KeyValue) combo1.component().getSelectedItem();
        assertEquals("value2", item1.getKey());

        // for combo2, the initial selection is determined by the "combo2" variable
        JComboBoxFixture combo2 = frame.comboBox("combo2");
        KeyValue item2 = (KeyValue) combo2.component().getSelectedItem();
        assertEquals("value3", item2.getKey());

        // for combo3, there is no initial selection
        JComboBoxFixture combo3 = frame.comboBox("combo3");
        assertNull(combo3.component().getSelectedItem());

        // for combo4, the initial selection is determined by the 'set' attribute
        JComboBoxFixture combo4 = frame.comboBox("combo4");
        KeyValue item4 = (KeyValue) combo4.component().getSelectedItem();
        assertEquals("value4", item4.getKey());

        combo1.component().setSelectedIndex(0);
        combo2.clearSelection();
        combo3.component().setSelectedIndex(1);

        checkNavigateNext(frame);

        assertEquals("value1", installData.getVariable("combo1"));
        assertNull(installData.getVariable("combo2"));
        assertEquals("value2", installData.getVariable("combo3"));
        assertEquals("value4", installData.getVariable("combo4"));
    }

    /**
     * Tests radio fields.
     *
     * @throws Exception for any error
     */
    @Test
    public void testRadioField() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/radio/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/radio/");

        InstallData installData = getInstallData();

        // show the panel
        FrameFixture frame = showUserInputPanel();

        // for radioA, the initial selection is determined by the 'set' attribute
        JRadioButtonFixture radioA1 = frame.radioButton("radioA.1");
        JRadioButtonFixture radioA2 = frame.radioButton("radioA.2");
        JRadioButtonFixture radioA3 = frame.radioButton("radioA.3");
        assertFalse(radioA1.component().isSelected());
        assertTrue(radioA2.component().isSelected());
        assertFalse(radioA3.component().isSelected());

        // for radioB, there is no initial selection
        JRadioButtonFixture radioB1 = frame.radioButton("radioB.1");
        JRadioButtonFixture radioB2 = frame.radioButton("radioB.2");
        JRadioButtonFixture radioB3 = frame.radioButton("radioB.3");
        assertFalse(radioB1.component().isSelected());
        assertFalse(radioB2.component().isSelected());
        assertFalse(radioB3.component().isSelected());

        // for radioC, the initial selection is determined by the 'set' attribute
        JRadioButtonFixture radioC1 = frame.radioButton("radioC.1");
        JRadioButtonFixture radioC2 = frame.radioButton("radioC.2");
        JRadioButtonFixture radioC3 = frame.radioButton("radioC.3");
        assertFalse(radioC1.component().isSelected());
        assertFalse(radioC2.component().isSelected());
        assertTrue(radioC3.component().isSelected());

        // select the first value of C
        radioC1.check();

        checkNavigateNext(frame);

        assertEquals("value2", installData.getVariable("radioA"));
        assertNull(installData.getVariable("radioB"));
        assertEquals("valueQ", installData.getVariable("radioC"));
    }

    /**
     * Tests password fields.
     *
     * @throws Exception for any error
     */
    @Test
    public void testPassword() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/password/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/password/");

        InstallData installData = getInstallData();

        // show the panel
        FrameFixture frame = showUserInputPanel();

        // for passwordA, the initial value is determined by the 'set' attribute
        JTextComponentFixture passwordA1 = frame.textBox("passwordA.1");
        JTextComponentFixture passwordA2 = frame.textBox("passwordA.2");
        assertEquals("ab1234", passwordA1.component().getText());
        assertEquals("ab1234", passwordA2.component().getText());

        // passwordB has no initial value
        JTextComponentFixture passwordB = frame.textBox("passwordB.1");
        assertEquals("", passwordB.component().getText());

        // for password C, the initial value is determined by the 'set' attribute
        JTextComponentFixture passwordC = frame.textBox("passwordC.1");
        assertEquals("qwerty", passwordC.component().getText());

        // update passwordC
        passwordC.setText("xyz");

        assertTrue(getPanels().getView().panelValidated());

        // test password validation
        passwordA2.setText("foo");

        frame.button(GuiId.BUTTON_NEXT.id).click();
        DialogFixture dialog = frame.dialog(Timeout.timeout(10000));
        assertEquals("Passwords must match", dialog.label("OptionPane.label").text());
        dialog.button().click();
        passwordA2.setText("ab1234");

        // move to the next panel and verify the variables have updated
        checkNavigateNext(frame);

        assertEquals("ab1234", installData.getVariable("passwordA"));
        assertEquals("", installData.getVariable("passwordB"));
        assertEquals("xyz", installData.getVariable("passwordC"));
    }

    /**
     * Tests check fields.
     *
     * @throws Exception for any error
     */
    @Test
    public void testCheck() throws Exception
    {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/check/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/check/");

        InstallData installData = getInstallData();

        installData.setVariable("check5", "check5set");
        installData.setVariable("check6", "check6unset");

        // show the panel
        FrameFixture frame = showUserInputPanel();

        checkCheckBox("check1", true, frame);
        checkCheckBox("check2", false, frame);
        checkCheckBox("check3", true, frame);
        checkCheckBox("check4", false, frame);
        checkCheckBox("check5", true, frame);
        JCheckBox check6 = checkCheckBox("check6", false, frame);

        check6.setSelected(true);

        // move to the next panel and verify the variables have updated
        checkNavigateNext(frame);

        assertEquals("", installData.getVariable("check1"));
        assertEquals("", installData.getVariable("check2"));

        assertEquals("check3set", installData.getVariable("check3"));
        assertEquals("check4unset", installData.getVariable("check4"));
        assertEquals("check5set", installData.getVariable("check5"));
        assertEquals("check6set", installData.getVariable("check6"));
    }

    /**
     * Tests search fields.
     *
     * @throws Exception for any error
     */
    @Ignore
    @Test
    public void testSearch() throws Exception {
        // Set the base path in order to pick up com/izforge/izpack/panels/userinput/search/userInputSpec.xml
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/search/");

        InstallData installData = getInstallData();

        // show the panel
        FrameFixture frame = showUserInputPanel();

        JComboBoxFixture search1 = frame.comboBox("search1");
        assertEquals(-1, search1.component().getSelectedIndex());
        search1.selectItem(0);
        assertEquals(0, search1.component().getSelectedIndex());

        Thread.sleep(10000000);
        // move to the next panel and verify the variables have updated
        checkNavigateNext(frame);

        assertEquals("$USER_HOME", installData.getVariable("search1"));
    }

    /**
     * Verifies that dynamic variables are refreshed when the panel is validated.
     *
     * @throws Exception for any error
     */
    @Test
    public void testRefreshDynamicVariables() throws Exception
    {
        getResourceManager().setResourceBasePath("/com/izforge/izpack/panels/userinput/refresh/");
        InstallData installData = getInstallData();

        // create a variable to be used in userInputSpec.xml to set a default value for the address field
        installData.setVariable("defaultAddress", "localhost");

        // create a dynamic variable that will be updated with the value of the address field
        installData.getVariables().add(new DynamicVariableImpl("dynamicMasterAddress", "${address}"));

        // show the panel
        FrameFixture fixture = showUserInputPanel();

        JTextComponentFixture address = fixture.textBox();
        assertEquals("localhost", address.text());

        // address variable won't be defined until the field is set
        assertNull(installData.getVariable("address"));

        assertEquals("${address}", installData.getVariable("dynamicMasterAddress"));
        address.setText("myhost");
        assertNull(installData.getVariable("address"));

        assertEquals("${address}", installData.getVariable("dynamicMasterAddress"));

        // verify that "address" is updated when the panel is validated, but "dynamicMasterAddress" isn't
        assertTrue(getPanels().getView().panelValidated());
        assertEquals("myhost", installData.getVariable("address"));
        assertEquals("${address}", installData.getVariable("dynamicMasterAddress"));
        checkNavigateNext(fixture);


        // navigation triggers a variable refresh. Make sure dynamicMasterAddress has updated
        assertEquals("myhost", installData.getVariable("dynamicMasterAddress"));
    }

    private JCheckBox checkCheckBox(String name, boolean expected, FrameFixture frame)
    {
        JCheckBox check = frame.checkBox(name).component();
        assertEquals(expected, check.isSelected());
        return check;
    }

    private void checkNavigateNext(FrameFixture fixture) throws InterruptedException
    {
        // attempt to navigate to the next panel
        fixture.button(GuiId.BUTTON_NEXT.id).click();
        Thread.sleep(2000);
        assertTrue(getPanels().getView() instanceof SimpleFinishPanel);
    }

    /**
     * Shows the user input panel.
     *
     * @return the frame fixture
     * @throws InterruptedException if interrupted waiting for the frame to display
     */
    private FrameFixture showUserInputPanel() throws InterruptedException
    {
        FrameFixture fixture = show(UserInputPanel.class, SimpleFinishPanel.class);
        Thread.sleep(2000);
        assertTrue(getPanels().getView() instanceof UserInputPanel);
        return fixture;
    }

}
