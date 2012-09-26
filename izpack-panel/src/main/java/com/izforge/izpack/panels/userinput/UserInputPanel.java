package com.izforge.izpack.panels.userinput;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.data.binding.OsModel;
import com.izforge.izpack.api.exception.IzPackException;
import com.izforge.izpack.api.factory.ObjectFactory;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.core.rules.process.ExistsCondition;
import com.izforge.izpack.core.rules.process.ExistsCondition.ContentType;
import com.izforge.izpack.gui.ButtonFactory;
import com.izforge.izpack.gui.FlowLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.TwoColumnConstraints;
import com.izforge.izpack.gui.TwoColumnLayout;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.panels.userinput.field.Alignment;
import com.izforge.izpack.panels.userinput.field.ElementReader;
import com.izforge.izpack.panels.userinput.field.Field;
import com.izforge.izpack.panels.userinput.field.FieldFactory;
import com.izforge.izpack.panels.userinput.field.FieldValidator;
import com.izforge.izpack.panels.userinput.field.KeyValue;
import com.izforge.izpack.panels.userinput.field.UserInputPanelSpec;
import com.izforge.izpack.panels.userinput.field.check.CheckField;
import com.izforge.izpack.panels.userinput.field.combo.ComboField;
import com.izforge.izpack.panels.userinput.field.divider.Divider;
import com.izforge.izpack.panels.userinput.field.file.DirField;
import com.izforge.izpack.panels.userinput.field.file.FileField;
import com.izforge.izpack.panels.userinput.field.file.MultipleFileField;
import com.izforge.izpack.panels.userinput.field.password.PasswordField;
import com.izforge.izpack.panels.userinput.field.password.PasswordGroupField;
import com.izforge.izpack.panels.userinput.field.radio.RadioChoice;
import com.izforge.izpack.panels.userinput.field.radio.RadioField;
import com.izforge.izpack.panels.userinput.field.rule.RuleField;
import com.izforge.izpack.panels.userinput.field.search.SearchField;
import com.izforge.izpack.panels.userinput.field.space.Spacer;
import com.izforge.izpack.panels.userinput.field.statictext.StaticText;
import com.izforge.izpack.panels.userinput.field.text.TextField;
import com.izforge.izpack.panels.userinput.field.title.TitleField;
import com.izforge.izpack.panels.userinput.processorclient.RuleInputField;
import com.izforge.izpack.panels.userinput.processorclient.TextInputField;
import com.izforge.izpack.panels.userinput.validator.ValidatorContainer;
import com.izforge.izpack.util.HyperlinkHandler;
import com.izforge.izpack.util.OsConstraintHelper;
import com.izforge.izpack.util.OsVersion;
import com.izforge.izpack.util.PlatformModelMatcher;

/**
 * User input panel.
 *
 * @author Anthonin Bonnefoy
 */
public class UserInputPanel extends IzPanel implements ActionListener, ItemListener, FocusListener
{
    private static final Logger logger = Logger.getLogger(UserInputPanel.class.getName());

    private static final String FIELD_NODE_ID = "field";

    private static final String TOPBUFFER = "topBuffer";

    protected static final String ATTRIBUTE_CONDITIONID_NAME = "conditionid";

    protected static final String VARIABLE_NODE = "variable";

    protected static final String ATTRIBUTE_VARIABLE_NAME = "name";

    protected static final String ATTRIBUTE_VARIABLE_VALUE = "value";

    /**
     * If there is a possibility that some UI elements will not get added we can not allow to go
     * back to the PacksPanel, because the process of building the UI is not reversable. This
     * variable keeps track if any packs have been defined and will be used to make a decision for
     * locking the 'previous' button.
     */
    private boolean packsDefined = false;

    /**
     * The parsed result from reading the XML specification from the file
     */
    private IXMLElement spec;

    /**
     * used for temporary storage of references to password groups that have already been read in a
     * given read cycle.
     */
    private List<PasswordGroup> passwordGroupsRead = new ArrayList<PasswordGroup>();

    /**
     * Used to track search fields. Contains SearchField references.
     */
    private List<SearchInputField> searchFields = new ArrayList<SearchInputField>();

    /**
     * Holds all user inputs for use in automated installation
     */
    private List<KeyValue> entries = new ArrayList<KeyValue>();

    // Used for dynamic controls to skip content validation unless the user
    // really clicks "Next"
    private boolean validating = true;

    private boolean eventsActivated = false;

    private List<UIElement> elements = new ArrayList<UIElement>();

    private JPanel panel;
    private RulesEngine rules;

    /**
     * The factory for creating validators.
     */
    private final ObjectFactory factory;

    /**
     * The platform-model matcher.
     */
    private final PlatformModelMatcher matcher;

    private UserInputPanelSpec userInputModel;

    /*--------------------------------------------------------------------------*/
    // This method can be used to search for layout problems. If this class is
    // compiled with this method uncommented, the layout guides will be shown
    // on the panel, making it possible to see if all components are placed
    // correctly.
    /*--------------------------------------------------------------------------*/
    // public void paint (Graphics graphics)
    // {
    // super.paint (graphics);
    // layout.showRules ((Graphics2D)graphics, Color.red);
    // }
    /*--------------------------------------------------------------------------*/

    /**
     * Constructs an <code>UserInputPanel</code>.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent IzPack installer frame
     * @param installData the installation data
     * @param resources   the resources
     * @param rules       the rules engine
     * @param factory     factory
     * @param matcher     the platform-model matcher
     */
    public UserInputPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
                          RulesEngine rules, ObjectFactory factory, PlatformModelMatcher matcher)
    {
        super(panel, parent, installData, resources);

        this.rules = rules;
        this.factory = factory;
        this.matcher = matcher;
    }

    /**
     * Indicates wether the panel has been validated or not. The installer won't let the user go
     * further through the installation process until the panel is validated.
     *
     * @return a boolean stating whether the panel has been validated or not.
     */
    @Override
    public boolean isValidated()
    {
        return readInput();
    }

    /**
     * This method is called when the panel becomes active.
     */
    @Override
    public void panelActivate()
    {
        this.init();

        if (spec == null)
        {
            // TODO: translate
            emitError("User input specification could not be found.",
                      "The specification for the user input panel could not be found. Please contact the packager.");
            parent.skipPanel();
        }
        else
        {
            // update UI with current values of associated variables
            updateUIElements();

            ElementReader reader = new ElementReader(userInputModel.getConfig());
            List<String> forPacks = reader.getPacks(spec);
            List<String> forUnselectedPacks = reader.getUnselectedPacks(spec);
            List<OsModel> forOs = reader.getOsModels(spec);

            if (!itemRequiredFor(forPacks) || !itemRequiredForUnselected(forUnselectedPacks)
                    || !matcher.matchesCurrentPlatform(forOs))
            {
                parent.skipPanel();
            }
            else
            {
                buildUI();

                Dimension size = getMaximumSize();
                setSize(size.width, size.height);
                validate();
                if (packsDefined)
                {
                    parent.lockPrevButton();
                }
            }
        }
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Asks the panel to set its own XML installDataGUI that can be brought back for an automated installation
     * process. Use it as a blackbox if your panel needs to do something even in automated mode.
     *
     * @param panelRoot The XML root element of the panels blackbox tree.
     */
    /*--------------------------------------------------------------------------*/
    @Override
    public void makeXMLData(IXMLElement panelRoot)
    {
        Map<String, String> entryMap = new HashMap<String, String>();

        for (KeyValue pair : entries)
        {
            String key = pair.toString();
            entryMap.put(key, installData.getVariable(key));
        }

        new UserInputPanelAutomationHelper(entryMap).makeXMLData(installData, panelRoot);
    }

    private void createBuiltInVariableConditions(String variable)
    {
        if (variable != null)
        {
            ExistsCondition existsCondition = new ExistsCondition();
            existsCondition.setContentType(ContentType.VARIABLE);
            existsCondition.setContent(variable);
            existsCondition.setId("izpack.input." + variable);
            existsCondition.setInstallData(this.installData);
            rules.addCondition(existsCondition);
        }
    }

    private void init()
    {
        eventsActivated = false;
        TwoColumnLayout layout;
        super.removeAll();
        elements.clear();


        // ----------------------------------------------------
        // read the specifications
        // ----------------------------------------------------
        if (spec == null)
        {
            spec = readSpec();
        }

        // ----------------------------------------------------
        // Set the topBuffer from the attribute. topBuffer=0 is useful
        // if you don't want your panel to be moved up and down during
        // dynamic validation (showing and hiding components within the
        // same panel)
        // ----------------------------------------------------
        int topbuff = 25;
        try
        {
            topbuff = Integer.parseInt(spec.getAttribute(TOPBUFFER));
        }
        catch (Exception ignore)
        {
            // do nothing
        }
        finally
        {
            layout = new TwoColumnLayout(10, 5, 30, topbuff, TwoColumnLayout.LEFT);
        }
        setLayout(new BorderLayout());

        panel = new JPanel();
        panel.setLayout(layout);

        if (spec == null)
        {
            // return if we could not read the spec. further
            // processing will only lead to problems. In this
            // case we must skip the panel when it gets activated.
            return;
        }

        // refresh variables specified in spec
        updateVariables();

        // ----------------------------------------------------
        // process all field nodes. Each field node is analyzed
        // for its type, then an appropriate memeber function
        // is called that will create the correct UI elements.
        // ----------------------------------------------------
        List<IXMLElement> fields = spec.getChildrenNamed(FIELD_NODE_ID);

        FieldFactory factory = new FieldFactory();
        for (IXMLElement field : fields)
        {
            Field f = factory.create(field, userInputModel.getConfig(), installData, matcher);
            String variable = f.getVariable();
            if (variable != null)
            {
                // create automatic existence condition
                createBuiltInVariableConditions(variable);
            }

            String condition = f.getCondition();
            if (condition == null || rules.isConditionTrue(condition, installData))
            {
                if (f instanceof RuleField)
                {
                    addRuleField((RuleField) f);
                }
                else if (f instanceof TextField)
                {
                    addTextField((TextField) f);
                }
                else if (f instanceof ComboField)
                {
                    addComboBox((ComboField) f);
                }
                else if (f instanceof RadioField)
                {
                    addRadioButton((RadioField) f);
                }
                else if (f instanceof PasswordGroupField)
                {
                    addPasswordField((PasswordGroupField) f);
                }
                else if (f instanceof Spacer)
                {
                    addSpace((Spacer) f);
                }
                else if (f instanceof Divider)
                {
                    addDivider((Divider) f);
                }
                else if (f instanceof CheckField)
                {
                    addCheckBox((CheckField) f);
                }
                else if (f instanceof StaticText)
                {
                    addText((StaticText) f);
                }
                else if (f instanceof TitleField)
                {
                    addTitle((TitleField) f);
                }
                else if (f instanceof SearchField)
                {
                    addSearch((SearchField) f);
                }
                else if (f instanceof MultipleFileField)
                {
                    addMultipleFileField((MultipleFileField) f);
                }
                else if (f instanceof FileField)
                {
                    addFileField((FileField) f);
                }
                else if (f instanceof DirField)
                {
                    addDirectoryField((DirField) f);
                }
            }
        }
        eventsActivated = true;
    }

    private List<ValidatorContainer> analyzeValidator(Field field)
    {
        List<ValidatorContainer> result = Collections.emptyList();
        List<FieldValidator> validators = field.getValidators();
        if (!validators.isEmpty())
        {
            result = new ArrayList<ValidatorContainer>(validators.size());
            for (FieldValidator validator : validators)
            {
                ValidatorContainer container = new ValidatorContainer(validator.create(), validator.getMessage(),
                                                                      validator.getParameters());
                result.add(container);
            }
        }
        return result;
    }

    private void addDirectoryField(DirField model)
    {
        String set = model.getDefaultValue();
        int size = model.getSize();
        boolean allowEmptyValue = model.getAllowEmptyValue();
        boolean mustExist = model.getMustExist();
        boolean create = model.getCreate();

        List<ValidatorContainer> validatorConfig = analyzeValidator(model);

        addLabel(model);

        TwoColumnConstraints constraints2 = new TwoColumnConstraints(TwoColumnConstraints.EAST);

        FileInputField fileInput = new DirInputField(this, installData, true, set, size,
                                                     validatorConfig, mustExist, create);

        fileInput.setAllowEmptyInput(allowEmptyValue);

        UIElement element = new UIElement(UIElementType.DIRECTORY, model, fileInput, constraints2);
        elements.add(element);
    }

    private void addMultipleFileField(MultipleFileField model)
    {
        List<ValidatorContainer> validatorConfig = analyzeValidator(model);

        MultipleFileInputField fileInputField = new MultipleFileInputField(model, parent, installData,
                                                                           false, validatorConfig);

        TwoColumnConstraints constraints2 = new TwoColumnConstraints(TwoColumnConstraints.EAST);
        UIElement element = new UIElement(UIElementType.MULTIPLE_FILE, model, fileInputField, constraints2);
        elements.add(element);
    }

    private void addFileField(FileField model)
    {
        String set = model.getDefaultValue();
        int size = model.getSize();

        String filter = model.getFileExtension();
        String filterdesc = model.getFileExtensionDescription();

        boolean allowEmptyValue = model.getAllowEmptyValue();
        List<ValidatorContainer> validatorConfig = analyzeValidator(model);

        addLabel(model);

        FileInputField fileInputField = new FileInputField(this, installData, false, set, size,
                                                           validatorConfig, filter, filterdesc);
        fileInputField.setAllowEmptyInput(allowEmptyValue);

        TwoColumnConstraints constraints2 = new TwoColumnConstraints(TwoColumnConstraints.EAST);
        UIElement element = new UIElement(UIElementType.FILE, model, fileInputField, constraints2);
        elements.add(element);
    }

    protected void updateUIElements()
    {
        boolean updated = false;

        for (UIElement element : elements)
        {
            if (element.hasVariableAssignment())
            {
                String variable = element.getAssociatedVariable();
                String value = this.installData.getVariable(variable);

                logger.fine("variable=" + variable + ", value=" + value);
                if (element.getType() == UIElementType.RADIOBUTTON)
                {
                    // we have a radio field, which should be updated
                    JRadioButton choice = (JRadioButton) element.getComponent();
                    if (value == null)
                    {
                        continue;
                    }
                    if (value.equals(element.getTrueValue()))
                    {
                        choice.setSelected(true);
                    }
                    else
                    {
                        choice.setSelected(false);
                    }
                }
                else if (element.getType() == UIElementType.TEXT)
                {
                    // update TextField
                    TextInputField textf = (TextInputField) element.getComponent();

                    if (value == null)
                    {
                        value = textf.getText();
                    }
                    textf.setText(replaceVariables(value));
                }
                else if (element.getType() == UIElementType.PASSWORD)
                {
                    // update PasswordField
                    JTextComponent textf = (JTextComponent) element.getComponent();

                    if (value == null)
                    {
                        value = textf.getText();
                    }
                    textf.setText(replaceVariables(value));
                }
                else if (element.getType() == UIElementType.RULE)
                {

                    RuleInputField rulef = (RuleInputField) element.getComponent();
                    if (value == null)
                    {
                        value = rulef.getText();
                    }
                }
                else if (element.getType() == UIElementType.MULTIPLE_FILE)
                {
                    MultipleFileInputField multifile = (MultipleFileInputField) element
                            .getComponent();
                    if (value != null)
                    {
                        multifile.clearFiles();
                        if (multifile.isCreateMultipleVariables())
                        {
                            multifile.addFile(value);
                            // try to read more files
                            String basevariable = element.getAssociatedVariable();
                            int index = 1;

                            while (value != null)
                            {
                                StringBuilder builder = new StringBuilder(basevariable);
                                builder.append("_");
                                builder.append(index++);
                                value = installData.getVariable(builder.toString());
                                if (value != null)
                                {
                                    multifile.addFile(value);
                                }
                            }
                        }
                        else
                        {
                            // split file string
                            String[] files = value.split(";");
                            for (String file : files)
                            {
                                multifile.addFile(file);
                            }
                        }
                    }
                }
                else if (element.getType() == UIElementType.FILE)
                {
                    FileInputField fileInput = (FileInputField) element.getComponent();
                    if (value != null)
                    {
                        fileInput.setFile(value);
                    }
                }
                else if (element.getType() == UIElementType.DIRECTORY)
                {
                    FileInputField fileInput = (FileInputField) element.getComponent();
                    if (value != null)
                    {
                        fileInput.setFile(value);
                    }
                }
                updated = true;
            }
        }

        if (updated)
        {
            super.invalidate();
        }
    }

    /**
     * Builds the UI and makes it ready for display
     */
    /*--------------------------------------------------------------------------*/
    private void buildUI()
    {
        for (UIElement element : elements)
        {
            if (itemRequiredFor(element.getForPacks()) && matcher.matchesCurrentPlatform(element.getForOs()))
            {
                if (!element.isDisplayed())
                {
                    element.setDisplayed(true);
                    panel.add(element.getComponent(), element.getConstraints());
                }
            }
            else
            {
                if (element.isDisplayed())
                {
                    element.setDisplayed(false);
                    panel.remove(element.getComponent());
                }
            }
        }

        JScrollPane scroller = new JScrollPane(panel);
        Border emptyBorder = BorderFactory.createEmptyBorder();
        scroller.setBorder(emptyBorder);
        scroller.setViewportBorder(emptyBorder);
        scroller.getVerticalScrollBar().setBorder(emptyBorder);
        scroller.getHorizontalScrollBar().setBorder(emptyBorder);
        add(scroller, BorderLayout.CENTER);
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Reads the input installDataGUI from all UI elements and sets the associated variables.
     *
     * @return <code>true</code> if the operation is successdul, otherwise <code>false</code>.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readInput()
    {
        boolean success = true;

        passwordGroupsRead.clear();

        for (UIElement element : elements)
        {
            if (element.isDisplayed())
            {
                if (element.getType() == UIElementType.RULE)
                {
                    success = readRuleField(element);
                }
                else if (element.getType() == UIElementType.PASSWORD)
                {
                    success = readPasswordField(element);
                }
                else if (element.getType() == UIElementType.TEXT)
                {
                    success = readTextField(element);
                }
                else if (element.getType() == UIElementType.COMBOBOX)
                {
                    success = readComboBox(element);
                }
                else if (element.getType() == UIElementType.RADIOBUTTON)
                {
                    success = readRadioButton(element);
                }
                else if (element.getType() == UIElementType.CHECKBOX)
                {
                    success = readCheckBox(element);
                }
                else if (element.getType() == UIElementType.SEARCH)
                {
                    success = readSearch(element);
                }
                else if (element.getType() == UIElementType.MULTIPLE_FILE)
                {
                    success = readMultipleFileField(element);
                }
                else if (element.getType() == UIElementType.FILE)
                {
                    success = readFileField(element);
                }
                else if (element.getType() == UIElementType.DIRECTORY)
                {
                    success = readDirectoryField(element);
                }
                if (!success)
                {
                    return (false);
                }
            }
        }
        return (true);
    }

    private boolean readDirectoryField(UIElement field)
    {
        boolean result = false;
        try
        {
            FileInputField panel = (FileInputField) field.getComponent();
            result = panel.validateField();
            if (result)
            {
                String absolutePath = panel.getSelectedFile().getAbsolutePath();
                installData.setVariable(field.getAssociatedVariable(), absolutePath);
                entries.add(new KeyValue(field.getAssociatedVariable(), absolutePath));
            }
        }
        catch (Exception e)
        {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        return result;
    }

    private boolean readFileField(UIElement field)
    {
        boolean result = false;
        try
        {
            FileInputField input = (FileInputField) field.getComponent();
            result = input.validateField();
            if (result)
            {
                String selectFileName = input.getSelectedFile().getName().length() != 0 ? input
                        .getSelectedFile().getAbsolutePath() : "";
                this.installData.setVariable(field.getAssociatedVariable(), selectFileName);
                entries.add(new KeyValue(field.getAssociatedVariable(), selectFileName));
            }
        }
        catch (Exception e)
        {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        return result;
    }

    private boolean readMultipleFileField(UIElement field)
    {
        boolean result = false;
        try
        {
            MultipleFileInputField input = (MultipleFileInputField) field.getComponent();
            result = input.validateField();
            if (result)
            {
                List<String> files = input.getSelectedFiles();
                String variable = field.getAssociatedVariable();
                if (input.isCreateMultipleVariables())
                {
                    int index = 0;
                    for (String file : files)
                    {
                        StringBuilder indexedVariableName = new StringBuilder(variable);
                        if (index > 0)
                        {
                            indexedVariableName.append("_");
                            indexedVariableName.append(index);
                        }
                        index++;
                        installData.setVariable(indexedVariableName.toString(), file);
                        entries.add(new KeyValue(indexedVariableName.toString(), file));
                    }

                }
                else
                {
                    StringBuilder buffer = new StringBuilder();
                    for (String file : files)
                    {
                        buffer.append(file);
                        buffer.append(";");
                    }
                    installData.setVariable(variable, buffer.toString());
                    entries.add(new KeyValue(variable, buffer.toString()));
                }
            }
        }
        catch (Exception e)
        {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        return result;
    }

    /**
     * Reads the XML specification for the panel layout.
     *
     * @return the panel specification
     * @throws IzPackException for any problems in reading the specification
     */
    private IXMLElement readSpec()
    {
        userInputModel = new UserInputPanelSpec(getResources(), installData, factory);
        return userInputModel.getPanelSpec(getMetadata());
    }

    /**
     * Adds the title to the panel. There can only be one title, if multiple titles are defined, they
     * keep overwriting what has already be defined, so that the last definition is the one that
     * prevails.
     *
     * @param field the title field
     */
    private void addTitle(TitleField field)
    {
        String title = field.getLabel();
        float multiplier = field.getTitleSize();
        int justify = getAlignment(field.getAlignment());

        if (title != null)
        {
            JLabel label = null;
            ImageIcon icon;
            String iconName = field.getIconName(installData.getMessages());
            if (iconName != null)
            {
                try
                {
                    icon = parent.getIcons().get(iconName);
                    label = LabelFactory.create(title, icon, SwingConstants.TRAILING, true);
                }
                catch (Exception e)
                {
                    logger.log(Level.WARNING, "Icon " + iconName + " not found in icon list: " + e.getMessage(), e);
                }
            }
            if (label == null)
            {
                label = LabelFactory.create(title);
            }
            Font font = label.getFont();
            float size = font.getSize();
            int style = 0;

            if (field.isBold())
            {
                style += Font.BOLD;
            }
            if (field.isItalic())
            {
                style += Font.ITALIC;
            }

            font = font.deriveFont(style, (size * multiplier));
            label.setFont(font);
            label.setAlignmentX(0);

            TwoColumnConstraints constraints = new TwoColumnConstraints(TwoColumnConstraints.NORTH, justify);
            panel.add(label, constraints);
        }
    }

    /**
     * Adds a rule field to the list of UI elements.
     *
     * @param field the field
     */
    private void addRuleField(RuleField field)
    {
        RuleInputField component = new RuleInputField(field, getToolkit(), installData);
        int id = 1;
        for (JTextField input : component.getInputFields())
        {
            input.setName(field.getVariable() + "." + id);
            ++id;
        }

        addField(field, UIElementType.RULE, component);
    }

    /**
     * Reads the installDataGUI from the rule input field and sets the associated variable.
     *
     * @param field the object array that holds the details of the field.
     * @return <code>true</code> if there was no problem reading the installDataGUI or if there was an
     *         irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     *         dialog is popped up and <code>false</code> is returned.
     */
    private boolean readRuleField(UIElement field)
    {
        RuleInputField ruleField;
        String variable;
        String message;

        try
        {
            ruleField = (RuleInputField) field.getComponent();
            variable = field.getAssociatedVariable();
            message = field.getMessage();
        }
        catch (Throwable exception)
        {
            return (true);
        }
        if ((variable == null) || (ruleField == null))
        {
            return (true);
        }

        boolean success = !validating || ruleField.validateContents();
        if (!success)
        {
            showWarningMessageDialog(parent, message);
            return (false);
        }

        this.installData.setVariable(variable, ruleField.getText());
        entries.add(new KeyValue(variable, ruleField.getText()));
        return (true);
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Adds a text field to the list of UI elements.
     *
     * @param field the field
     */
    private void addTextField(TextField field)
    {
        TextInputField component = new TextInputField(field, installData);
        component.addFocusListener(this);

        addField(field, UIElementType.TEXT, component);
    }

    /**
     * Reads installDataGUI from the text field and sets the associated variable.
     *
     * @param field the object array that holds the details of the field.
     * @return <code>true</code> if there was no problem reading the installDataGUI or if there was an
     *         irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     *         dialog is popped up and <code>false</code> is returned.
     */
    private boolean readTextField(UIElement field)
    {
        TextInputField textField;
        String variable;
        String value;
        String message;

        try
        {
            textField = (TextInputField) field.getComponent();
            variable = field.getAssociatedVariable();
            message = field.getMessage();
            value = textField.getText();
        }
        catch (Throwable exception)
        {
            return (true);
        }
        if ((variable == null) || (value == null))
        {
            return (true);
        }

        // validate the input
        logger.fine("Validating text field");
        boolean success = textField.validateContents();
        if (!success)
        {
            logger.fine("Validation did not pass, message: " + message);
            if (message == null)
            {
                message = "Text entered did not pass validation.";
            }
            showWarningMessageDialog(parent, message);
            return (false);
        }
        logger.fine("Field validated");
        this.installData.setVariable(variable, value);
        entries.add(new KeyValue(variable, value));
        return (true);
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Adds a combo box to the list of UI elements. <br>
     * This is a complete example of a valid XML specification
     * <p/>
     * <p/>
     * <pre>
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     *      &lt;field type=&quot;combo&quot; variable=&quot;testVariable&quot;&gt;
     *        &lt;description text=&quot;Description for the combo box&quot; id=&quot;a key for translated text&quot;/&gt;
     *        &lt;spec text=&quot;label&quot; id=&quot;key for the label&quot;/&gt;
     *          &lt;choice text=&quot;choice 1&quot; id=&quot;&quot; value=&quot;combo box 1&quot;/&gt;
     *          &lt;choice text=&quot;choice 2&quot; id=&quot;&quot; value=&quot;combo box 2&quot; set=&quot;true&quot;/&gt;
     *          &lt;choice text=&quot;choice 3&quot; id=&quot;&quot; value=&quot;combo box 3&quot;/&gt;
     *          &lt;choice text=&quot;choice 4&quot; id=&quot;&quot; value=&quot;combo box 4&quot;/&gt;
     *        &lt;/spec&gt;
     *      &lt;/field&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * </pre>
     *
     * @param field the field
     */
    /*--------------------------------------------------------------------------*/
    private void addComboBox(ComboField field)
    {
        JComboBox comboBox = new JComboBox();
        comboBox.setName(field.getVariable());
        if (field.getRevalidate())
        {
            comboBox.addItemListener(this);
        }
        for (KeyValue choice : field.getChoices())
        {
            comboBox.addItem(choice);
        }
        comboBox.setSelectedIndex(field.getSelectedIndex());

        addField(field, UIElementType.COMBOBOX, comboBox);
    }


    /**
     * Reads the content of the combobox field and substitutes the associated variable.
     *
     * @param field the object array that holds the details of the field.
     * @return <code>true</code> if there was no problem reading the installDataGUI or if there was an
     *         irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     *         dialog is popped up and <code>false</code> is returned.
     */
    private boolean readComboBox(UIElement field)
    {
        String variable = field.getAssociatedVariable();
        String value;
        JComboBox comboBox = (JComboBox) field.getComponent();
        KeyValue selected = (KeyValue) comboBox.getSelectedItem();
        value = (selected != null) ? selected.getKey() : null;

        installData.setVariable(variable, value);
        entries.add(new KeyValue(variable, value));
        return true;
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Adds a radio button set to the list of UI elements. <br>
     * This is a complete example of a valid XML specification
     * <p/>
     * <p/>
     * <pre>
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     *      &lt;field type=&quot;radio&quot; variable=&quot;testVariable&quot;&gt;
     *        &lt;description text=&quot;Description for the radio buttons&quot; id=&quot;a key for translated text&quot;/&gt;
     *        &lt;spec text=&quot;label&quot; id=&quot;key for the label&quot;/&gt;
     *          &lt;choice text=&quot;radio 1&quot; id=&quot;&quot; value=&quot;&quot;/&gt;
     *          &lt;choice text=&quot;radio 2&quot; id=&quot;&quot; value=&quot;&quot; set=&quot;true&quot;/&gt;
     *          &lt;choice text=&quot;radio 3&quot; id=&quot;&quot; value=&quot;&quot;/&gt;
     *          &lt;choice text=&quot;radio 4&quot; id=&quot;&quot; value=&quot;&quot;/&gt;
     *          &lt;choice text=&quot;radio 5&quot; id=&quot;&quot; value=&quot;&quot;/&gt;
     *        &lt;/spec&gt;
     *      &lt;/field&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * </pre>
     *
     * @param field the field
     */
    private void addRadioButton(RadioField field)
    {
        String variable = field.getVariable();

        ButtonGroup group = new ButtonGroup();

        TwoColumnConstraints constraints = new TwoColumnConstraints(TwoColumnConstraints.BOTH);
        constraints.indent = true;
        constraints.stretch = true;

        addDescription(field);

        int id = 1;
        for (RadioChoice choice : field.getChoices())
        {
            JRadioButton button = new JRadioButton();
            button.setName(variable + "." + id);
            ++id;
            button.setText(choice.getValue());
            if (choice.getRevalidate())
            {
                button.addActionListener(this);
            }
            String value = choice.getKey();

            group.add(button);
            boolean selected = field.getSelectedIndex() == group.getButtonCount() - 1;

            // in order to properly initialize dependent controls we must set this variable now
            if (selected)
            {
                if (installData.getVariable(variable) == null)
                {
                    installData.setVariable(variable, value);
                }
                button.setSelected(true);
            }

            RadioButtonUIElement element = new RadioButtonUIElement(field, button, constraints, group, value);
            elements.add(element);
        }
    }

    /**
     * Reads the content of the radio button field and substitutes the associated variable.
     *
     * @param field the object array that holds the details of the field.
     * @return <code>true</code> if there was no problem reading the installDataGUI or if there was an
     *         irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     *         dialog is popped up and <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readRadioButton(UIElement field)
    {
        String variable;
        String value;
        JRadioButton button;

        try
        {
            button = (JRadioButton) field.getComponent();

            if (!button.isSelected())
            {
                return (true);
            }

            variable = field.getAssociatedVariable();
            value = field.getTrueValue();
        }
        catch (Throwable exception)
        {
            return (true);
        }

        installData.setVariable(variable, value);
        entries.add(new KeyValue(variable, value));
        return (true);
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Adds one or more password fields to the list of UI elements. <br>
     * This is a complete example of a valid XML specification
     * <p/>
     * <p/>
     * <pre>
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     *      &lt;field type=&quot;password&quot; variable=&quot;testVariable&quot;&gt;
     *        &lt;description align=&quot;left&quot; txt=&quot;Please enter your password&quot; id=&quot;a key for translated text&quot;/&gt;
     *        &lt;spec&gt;
     *          &lt;pwd txt=&quot;Password&quot; id=&quot;key for the label&quot; size=&quot;10&quot; set=&quot;&quot;/&gt;
     *          &lt;pwd txt=&quot;Retype password&quot; id=&quot;another key for the label&quot; size=&quot;10&quot; set=&quot;&quot;/&gt;
     *        &lt;/spec&gt;
     *        &lt;validator class=&quot;com.izforge.sample.PWDValidator&quot; txt=&quot;Both versions of the password must match&quot; id=&quot;key for the error text&quot;/&gt;
     *        &lt;processor class=&quot;com.izforge.sample.PWDEncryptor&quot;/&gt;
     *      &lt;/field&gt;
     * &lt;p/&gt;
     * </pre>
     * <p/>
     * Additionally, parameters and multiple validators can be used to provide separate validation
     * and error messages for each case.
     * <p/>
     * <pre>
     * &lt;p/&gt;
     *    &lt;field type=&quot;password&quot; align=&quot;left&quot; variable=&quot;keystore.password&quot;&gt;
     *      &lt;spec&gt;
     *        &lt;pwd txt=&quot;Keystore Password:&quot; size=&quot;25&quot; set=&quot;&quot;/&gt;
     *        &lt;pwd txt=&quot;Retype Password:&quot; size=&quot;25&quot; set=&quot;&quot;/&gt;
     *      &lt;/spec&gt;
     *      &lt;validator class=&quot;com.izforge.izpack.panels.userinput.validator.PasswordEqualityValidator&quot; txt=&quot;Both keystore passwords must match.&quot; id=&quot;key for the error text&quot;/&gt;
     *      &lt;validator class=&quot;com.izforge.izpack.panels.userinput.validator.PasswordKeystoreValidator&quot; txt=&quot;Could not validate keystore with password and alias provided.&quot; id=&quot;key for the error text&quot;&gt;
     *        &lt;param name=&quot;keystoreFile&quot; value=&quot;${existing.ssl.keystore}&quot;/&gt;
     *        &lt;param name=&quot;keystoreType&quot; value=&quot;JKS&quot;/&gt;
     *        &lt;param name=&quot;keystoreAlias&quot; value=&quot;${keystore.key.alias}&quot;/&gt;
     *      &lt;/validator&gt;
     *    &lt;/field&gt;
     * &lt;p/&gt;
     * </pre>
     *
     * @param passwordGroup the field
     */
    /*--------------------------------------------------------------------------*/
    private void addPasswordField(PasswordGroupField passwordGroup)
    {
        addDescription(passwordGroup);

        List<ValidatorContainer> validatorsList = analyzeValidator(passwordGroup);
        PasswordGroup group = new PasswordGroup(validatorsList, passwordGroup.getProcessor());

        int id = 1;
        for (PasswordField f : passwordGroup.getPasswordFields())
        {
            JPasswordField component = new JPasswordField(f.getSet(), f.getSize());
            component.setName(passwordGroup.getVariable() + "." + id++);
            component.setCaretPosition(0);

            addLabel(f.getLabel(), passwordGroup.getPacks(), passwordGroup.getOsModels());

            TwoColumnConstraints constraints = new TwoColumnConstraints(TwoColumnConstraints.EAST);
            PasswordUIElement element = new PasswordUIElement(passwordGroup, component, constraints, group);
            elements.add(element);
            group.addField(component);
        }
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Reads the content of the password field and substitutes the associated variable.
     *
     * @param field a password group that manages one or more passord fields.
     * @return <code>true</code> if there was no problem reading the installDataGUI or if there was an
     *         irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     *         dialog is popped up and <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readPasswordField(UIElement field)
    {
        PasswordUIElement pwdField = (PasswordUIElement) field;

        PasswordGroup group;
        String variable;

        try
        {
            group = pwdField.getPasswordGroup();
            variable = field.getAssociatedVariable();
            // Removed to support grabbing the message from multiple validators
            // message = (String) field[POS_MESSAGE];
        }
        catch (Throwable exception)
        {
            return (true);
        }
        if ((variable == null) || (passwordGroupsRead.contains(group)))
        {
            return (true);
        }
        int size = group.validatorSize();
        boolean success = !validating || size < 1;

        // Use each validator to validate contents
        if (!success)
        {
            // System.out.println("Found "+(size)+" validators");
            for (int i = 0; i < size; i++)
            {
                success = group.validateContents(i);
                if (!success)
                {
                    JOptionPane.showMessageDialog(parent, group.getValidatorMessage(i),
                                                  parent.getMessages().get("UserInputPanel.error.caption"),
                                                  JOptionPane.WARNING_MESSAGE);
                    break;
                }
            }
        }

        if (success)
        {
            installData.setVariable(variable, group.getPassword());
            entries.add(new KeyValue(variable, group.getPassword()));
        }
        return success;
    }

    /**
     * Adds a checkbox to the list of UI elements.
     *
     * @param field the field
     */
    private void addCheckBox(CheckField field)
    {
        String label = field.getLabel();
        JCheckBox checkbox = new JCheckBox(label);
        checkbox.setName(field.getVariable());

        if (field.getRevalidate())
        {
            checkbox.addActionListener(this);
        }
        checkbox.setSelected(field.getInitialSelection(installData));

        addDescription(field);

        TwoColumnConstraints constraints = new TwoColumnConstraints(TwoColumnConstraints.BOTH);
        constraints.stretch = true;
        constraints.indent = true;

        UIElement element = new UIElement(UIElementType.CHECKBOX, field, checkbox, constraints);
        element.setTrueValue(field.getTrueValue());
        element.setFalseValue(field.getFalseValue());
        elements.add(element);
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Reads the content of the checkbox field and substitutes the associated variable.
     *
     * @param field the object array that holds the details of the field.
     * @return <code>true</code> if there was no problem reading the installDataGUI or if there was an
     *         irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     *         dialog is popped up and <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readCheckBox(UIElement field)
    {
        String variable;
        String trueValue;
        String falseValue;
        JCheckBox box;

        try
        {
            box = (JCheckBox) field.getComponent();
            variable = field.getAssociatedVariable();
            trueValue = field.getTrueValue();
            if (trueValue == null)
            {
                trueValue = "";
            }

            falseValue = field.getFalseValue();
            if (falseValue == null)
            {
                falseValue = "";
            }
        }
        catch (Throwable e)
        {
            logger.log(Level.WARNING, "Failed: " + e.getMessage(), e);
            return (true);
        }

        if (box.isSelected())
        {
            logger.fine("Selected, setting " + variable + " to " + trueValue);
            installData.setVariable(variable, trueValue);
            entries.add(new KeyValue(variable, trueValue));
        }
        else
        {
            logger.fine("Not selected, setting " + variable + " to " + falseValue);
            installData.setVariable(variable, falseValue);
            entries.add(new KeyValue(variable, falseValue));
        }

        return (true);
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Adds a search field to the list of UI elements.
     * <p/>
     * This is a complete example of a valid XML specification
     * <p/>
     * <p/>
     * <pre>
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     *      &lt;field type=&quot;search&quot; variable=&quot;testVariable&quot;&gt;
     *        &lt;description text=&quot;Description for the search field&quot; id=&quot;a key for translated text&quot;/&gt;
     *        &lt;spec text=&quot;label&quot; id=&quot;key for the label&quot; filename=&quot;the_file_to_search&quot; result=&quot;directory&quot; /&gt; &lt;!-- values for result: directory, file --&gt;
     *          &lt;choice dir=&quot;directory1&quot; set=&quot;true&quot; /&gt; &lt;!-- default value --&gt;
     *          &lt;choice dir=&quot;dir2&quot; /&gt;
     *        &lt;/spec&gt;
     *      &lt;/field&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * &lt;p/&gt;
     * </pre>
     *
     * @param field the search field
     */
    private void addSearch(SearchField field)
    {
        String filename = field.getFilename();
        String checkFilename = field.getCheckFilename();
        JComboBox combo = new JComboBox();

        // allow the user to enter something
        combo.setEditable(true);
        combo.setName(field.getVariable());

        for (String choice : field.getChoices())
        {
            combo.addItem(choice);
        }
        combo.setSelectedIndex(field.getSelectedIndex());

        addDescription(field);
        addLabel(field);

        StringBuilder tooltip = new StringBuilder();

        if ((filename != null) && (filename.length() > 0))
        {
            tooltip.append(parent.getMessages().get("UserInputPanel.search.location", filename));
        }

        boolean showAutodetect = (checkFilename != null) && (checkFilename.length() > 0);
        if (showAutodetect)
        {
            tooltip.append(parent.getMessages().get("UserInputPanel.search.location.checkedfile", checkFilename));
        }

        if (tooltip.length() > 0)
        {
            combo.setToolTipText(tooltip.toString());
        }

        TwoColumnConstraints east = new TwoColumnConstraints(TwoColumnConstraints.EAST);
        UIElement searchElement = new UIElement(UIElementType.SEARCH, field, combo, east);
        elements.add(searchElement);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEADING));

        JButton autodetectButton = ButtonFactory.createButton(
                getString("UserInputPanel.search.autodetect"), installData.buttonsHColor);
        autodetectButton.setVisible(showAutodetect);

        autodetectButton.setToolTipText(getString("UserInputPanel.search.autodetect.tooltip"));

        buttonPanel.add(autodetectButton);

        JButton browseButton = ButtonFactory.createButton(getString("UserInputPanel.search.browse"),
                                                          installData.buttonsHColor);
        buttonPanel.add(browseButton);

        TwoColumnConstraints eastonlyconstraint = new TwoColumnConstraints(TwoColumnConstraints.EASTONLY);

        UIElement searchButton = new UIElement(UIElementType.SEARCHBUTTON, buttonPanel, field.getPacks(),
                                               field.getOsModels(), eastonlyconstraint);
        elements.add(searchButton);

        searchFields.add(new SearchInputField(field, parent, combo, autodetectButton, browseButton, installData));
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Reads the content of the search field and substitutes the associated variable.
     *
     * @param field the object array that holds the details of the field.
     * @return <code>true</code> if there was no problem reading the installDataGUI or if there was an
     *         irrecovarable problem. If there was a problem that can be corrected by the operator, an error
     *         dialog is popped up and <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    private boolean readSearch(UIElement field)
    {
        String variable;
        String value = null;
        JComboBox comboBox;

        try
        {
            variable = field.getAssociatedVariable();
            comboBox = (JComboBox) field.getComponent();
            for (SearchInputField sf : this.searchFields)
            {
                if (sf.belongsTo(comboBox))
                {
                    value = sf.getResult();
                    break;
                }
            }
        }
        catch (Throwable exception)
        {
            return (true);
        }
        if ((variable == null) || (value == null))
        {
            return (true);
        }

        this.installData.setVariable(variable, value);
        entries.add(new KeyValue(variable, value));
        return (true);
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Adds text to the list of UI elements
     *
     * @param field the static text field
     */
    private void addText(StaticText field)
    {
        addDescription(field);
    }

    /**
     * Adds a dummy field to the list of UI elements to act as spacer.
     *
     * @param spacer the spacer to add
     */
    private void addSpace(Spacer spacer)
    {
        JPanel panel = new JPanel();

        TwoColumnConstraints constraints = new TwoColumnConstraints(TwoColumnConstraints.BOTH);
        constraints.stretch = true;

        UIElement element = new UIElement(UIElementType.SPACE, spacer, panel, constraints);
        elements.add(element);
    }

    /**
     * Adds a dividing line to the list of UI elements act as separator.
     *
     * @param divider the divider
     */
    private void addDivider(Divider divider)
    {
        JPanel panel = new JPanel();
        Alignment alignment = divider.getAlignment();

        if (alignment != null && alignment == Alignment.TOP)
        {
            panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.gray));
        }
        else
        {
            panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        }

        TwoColumnConstraints constraints = new TwoColumnConstraints(TwoColumnConstraints.BOTH);
        constraints.stretch = true;

        UIElement element = new UIElement(UIElementType.DIVIDER, divider, panel, constraints);
        elements.add(element);
    }

    /**
     * Adds a field description to the list of UI elements.
     *
     * @param field the field
     */
    private void addDescription(Field field)
    {
        String description = field.getDescription();
        // if we have a description, add it to the UI elements
        if (description != null)
        {
            // String alignment = spec.getAttribute(ALIGNMENT);
            // FIX needed: where do we use this variable at all? i dont think so...
            // int justify = MultiLineLabel.LEFT;
            //
            // if (alignment != null)
            // {
            // if (alignment.equals(LEFT))
            // {
            // justify = MultiLineLabel.LEFT;
            // }
            // else if (alignment.equals(CENTER))
            // {
            // justify = MultiLineLabel.CENTER;
            // }
            // else if (alignment.equals(RIGHT))
            // {
            // justify = MultiLineLabel.RIGHT;
            // }
            // }

            TwoColumnConstraints constraints = new TwoColumnConstraints(TwoColumnConstraints.BOTH);
            constraints.stretch = true;

            JTextPane label = new JTextPane();

            // Not editable, but still selectable.
            label.setEditable(false);

            // If html tags are present enable html rendering, otherwise the JTextPane
            // looks exactly like MultiLineLabel.
            if (description.startsWith("<html>") && description.endsWith("</html>"))
            {
                label.setContentType("text/html");
                label.addHyperlinkListener(new HyperlinkHandler());
            }
            label.setText(description);

            // Background color and font to match the label's.
            label.setBackground(UIManager.getColor("label.background"));
            label.setMargin(new Insets(3, 0, 3, 0));
            // workaround to cut out layout problems
            label.getPreferredSize();
            // end of workaround.

            UIElement element = new UIElement(UIElementType.DESCRIPTION, field, label, constraints);
            elements.add(element);
        }
    }

    /**
     * Maps an {@code Alignment} to the {@link TwoColumnConstraints} constants.
     *
     * @param alignment the alignment to map
     * @return the corresponding int value
     * @see com.izforge.izpack.gui.TwoColumnConstraints
     */
    private int getAlignment(Alignment alignment)
    {
        int result = TwoColumnConstraints.RIGHT;
        if (alignment == Alignment.LEFT)
        {
            result = TwoColumnConstraints.LEFT;
        }
        else if (alignment == Alignment.CENTER)
        {
            result = TwoColumnConstraints.CENTER;
        }

        return (result);
    }

    /**
     * Verifies if an item is required for the operating system the installer executed. The
     * configuration for this feature is: <br/>
     * &lt;os family="unix"/&gt; <br>
     * <br>
     * <b>Note:</b><br>
     * If the list of the os is empty then <code>true</code> is always returnd.
     *
     * @param os The <code>Vector</code> of <code>String</code>s. containing the os names
     * @return <code>true</code> if the item is required for the os, otherwise returns
     *         <code>false</code>.
     */
    private boolean itemRequiredForOs(List<String> os)
    {
        if (os.size() == 0)
        {
            return true;
        }

        for (String family : os)
        {
            boolean match = false;

            if ("windows".equals(family))
            {
                match = OsVersion.IS_WINDOWS;
            }
            else if ("mac".equals(family))
            {
                match = OsVersion.IS_OSX;
            }
            else if ("unix".equals(family))
            {
                match = OsVersion.IS_UNIX;
            }
            if (match)
            {
                return true;
            }
        }
        return false;
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Verifies if an item is required for any of the packs listed. An item is required for a pack
     * in the list if that pack is actually selected for installation. <br>
     * <br>
     * <b>Note:</b><br>
     * If the list of selected packs is empty then <code>true</code> is always returnd. The same is
     * true if the <code>packs</code> list is empty.
     *
     * @param packs a <code>Vector</code> of <code>String</code>s. Each of the strings denotes a
     *              pack for which an item should be created if the pack is actually installed.
     * @return <code>true</code> if the item is required for at least one pack in the list,
     *         otherwise returns <code>false</code>.
     */
    /*--------------------------------------------------------------------------*/
    /*
     * $ @design
     *
     * The information about the installed packs comes from GUIInstallData.selectedPacks. This assumes
     * that this panel is presented to the user AFTER the PacksPanel.
     * --------------------------------------------------------------------------
     */
    private boolean itemRequiredFor(List<String> packs)
    {

        String selected;

        if (packs.size() == 0)
        {
            return (true);
        }

        // ----------------------------------------------------
        // We are getting to this point if any packs have been
        // specified. This means that there is a possibility
        // that some UI elements will not get added. This
        // means that we can not allow to go back to the
        // PacksPanel, because the process of building the
        // UI is not reversable.
        // ----------------------------------------------------
        // packsDefined = true;

        // ----------------------------------------------------
        // analyze if the any of the packs for which the item
        // is required have been selected for installation.
        // ----------------------------------------------------
        for (int i = 0; i < this.installData.getSelectedPacks().size(); i++)
        {
            selected = this.installData.getSelectedPacks().get(i).getName();

            for (String pack : packs)
            {
                if (selected.equals(pack))
                {
                    return (true);
                }
            }
        }

        return (false);
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Verifies if an item is required for any of the packs listed. An item is required for a pack
     * in the list if that pack is actually NOT selected for installation. <br>
     * <br>
     * <b>Note:</b><br>
     * If the list of selected packs is empty then <code>true</code> is always returnd. The same is
     * true if the <code>packs</code> list is empty.
     *
     * @param packs a <code>Vector</code> of <code>String</code>s. Each of the strings denotes a
     *              pack for which an item should be created if the pack is actually installed.
     * @return <code>true</code> if the item is required for at least one pack in the list,
     *         otherwise returns <code>false</code>.
     */
    /*--------------------------------------------------------------------------*/
    /*
     * $ @design
     *
     * The information about the installed packs comes from GUIInstallData.selectedPacks. This assumes
     * that this panel is presented to the user AFTER the PacksPanel.
     * --------------------------------------------------------------------------
     */
    private boolean itemRequiredForUnselected(List<String> packs)
    {

        String selected;

        if (packs.size() == 0)
        {
            return (true);
        }

        // ----------------------------------------------------
        // analyze if the any of the packs for which the item
        // is required have been selected for installation.
        // ----------------------------------------------------
        for (int i = 0; i < this.installData.getSelectedPacks().size(); i++)
        {
            selected = this.installData.getSelectedPacks().get(i).getName();

            for (String pack : packs)
            {
                if (selected.equals(pack))
                {
                    return (false);
                }
            }
        }

        return (true);
    }

    protected void updateVariables()
    {
        /**
         * Look if there are new variables defined
         */
        List<IXMLElement> variables = spec.getChildrenNamed(VARIABLE_NODE);

        for (IXMLElement variable : variables)
        {
            String vname = variable.getAttribute(ATTRIBUTE_VARIABLE_NAME);
            String vvalue = variable.getAttribute(ATTRIBUTE_VARIABLE_VALUE);

            if (vvalue == null)
            {
                // try to read value element
                if (variable.hasChildren())
                {
                    IXMLElement value = variable.getFirstChildNamed("value");
                    vvalue = value.getContent();
                }
            }

            String conditionid = variable.getAttribute(ATTRIBUTE_CONDITIONID_NAME);
            if (conditionid != null)
            {
                // check if condition for this variable is fulfilled
                if (!rules.isConditionTrue(conditionid, this.installData))
                {
                    continue;
                }
            }
            // are there any OS-Constraints?
            List<OsModel> osList = OsConstraintHelper.getOsList(variable);
            if (matcher.matchesCurrentPlatform(osList))
            {
                if (vname != null)
                {
                    if (vvalue != null)
                    {
                        // substitute variables in value field
                        vvalue = replaceVariables(vvalue);

                        // try to cut out circular references
                        installData.setVariable(vname, "");
                        vvalue = replaceVariables(vvalue);
                    }
                    // set variable
                    installData.setVariable(vname, vvalue);

                    // for save this variable to be used later by Automation Helper
                    entries.add(new KeyValue(vname, vvalue));
                }
            }
        }
    }

    // Repaint all controls and validate them agains the current variables

    @Override
    public void actionPerformed(ActionEvent e)
    {
        // validating = false;
        // readInput();
        // panelActivate();
        // validating = true;
        updateDialog();
    }

    /*--------------------------------------------------------------------------*/

    /**
     * Adds a field.
     * <p/>
     * This adds the field description (if any), the field label, and field component.
     *
     * @param field     the field
     * @param type      the component type
     * @param component the component
     */
    private void addField(Field field, UIElementType type, JComponent component)
    {
        addDescription(field);
        addLabel(field);
        addComponent(field, type, component);
    }

    /**
     * Adds the label for a field.
     *
     * @param field the field
     */
    private void addLabel(Field field)
    {
        addLabel(field.getLabel(), field.getPacks(), field.getOsModels());
    }

    /**
     * Adds a label.
     *
     * @param label the label
     * @param packs the selected packs that the label applies to
     * @param os    the operating systems that the label applies to
     */
    private void addLabel(String label, List<String> packs, List<OsModel> os)
    {
        TwoColumnConstraints constraints = new TwoColumnConstraints(TwoColumnConstraints.WEST);

        UIElement element = new UIElement();
        element.setType(UIElementType.LABEL);
        element.setConstraints(constraints);
        element.setComponent(new JLabel(label));
        element.setForPacks(packs);
        element.setForOs(os);
        elements.add(element);
    }

    /**
     * Adds a component.
     *
     * @param field     the field that the component is associated with
     * @param type      the component type
     * @param component the component
     */
    private void addComponent(Field field, UIElementType type, JComponent component)
    {
        TwoColumnConstraints constraints = new TwoColumnConstraints(TwoColumnConstraints.EAST);

        UIElement element = new UIElement(type, field, component, constraints, field.getValidator());
        elements.add(element);
    }

    /**
     * Show localized message dialog basing on given parameters.
     *
     * @param parentFrame The parent frame.
     * @param message     The message to print out in dialog box.
     * @param caption     The caption of dialog box.
     * @param messageType The message type (JOptionPane.*_MESSAGE)
     */
    private void showMessageDialog(InstallerFrame parentFrame, String message, String caption, int messageType)
    {
        String localizedMessage = getString(message);
        String localizedCaption = getString(caption);
        JOptionPane.showMessageDialog(parentFrame, localizedMessage, localizedCaption, messageType);
    }

    /**
     * Show localized warning message dialog basing on given parameters.
     *
     * @param parentFrame parent frame.
     * @param message     the message to print out in dialog box.
     */
    private void showWarningMessageDialog(InstallerFrame parentFrame, String message)
    {
        showMessageDialog(parentFrame, message, "UserInputPanel.error.caption", JOptionPane.WARNING_MESSAGE);
    }

    @Override
    public void itemStateChanged(ItemEvent arg0)
    {
        updateDialog();
    }

    private void updateDialog()
    {
        if (this.eventsActivated)
        {
            this.eventsActivated = false;
            if (isValidated())
            {
                // read input
                // and update elements
                // panelActivate();
                init();
                updateVariables();
                updateUIElements();
                buildUI();
                validate();
                repaint();
            }
            this.eventsActivated = true;
        }
    }

    @Override
    public void focusGained(FocusEvent e)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void focusLost(FocusEvent e)
    {
        updateDialog();
    }

    /**
     * Helper to replace variables in text.
     *
     * @param text the text to perform replacement on. May be {@code null}
     * @return the text with any variables replaced with their values
     */
    private String replaceVariables(String text)
    {
        return installData.getVariables().replace(text);
    }


}
