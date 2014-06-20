package com.izforge.izpack.panels.treepacks;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.izforge.izpack.api.adaptator.IXMLElement;
import com.izforge.izpack.api.data.LocaleDatabase;
import com.izforge.izpack.api.data.Pack;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Locales;
import com.izforge.izpack.api.resource.Messages;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.api.rules.RulesEngine;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.debugger.Debugger;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;
import com.izforge.izpack.installer.util.PackHelper;
import com.izforge.izpack.installer.web.WebAccessor;
import com.izforge.izpack.panels.imgpacks.ImgPacksPanelAutomationHelper;
import com.izforge.izpack.panels.packs.PacksModel;
import com.izforge.izpack.panels.packs.PacksModelGUI;
import com.izforge.izpack.panels.packs.PacksPanelInterface;
import com.izforge.izpack.util.IoHelper;
import com.izforge.izpack.util.file.FileUtils;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public class TreePacksPanel extends IzPanel implements PacksPanelInterface
{
    private static final long serialVersionUID = 5684716698930628262L;
    private static final transient Logger logger = Logger.getLogger(TreePacksPanel.class.getName());

    protected JLabel spaceLabel;
    protected JLabel freeSpaceLabel;

    protected JTextArea descriptionArea;
    protected JTextArea dependencyArea;

    protected JTree packsTree;
    protected PacksModelGUI packsModel;
    protected JScrollPane tableScroller;

    private Messages messages;
    private static final String LANG_FILE_NAME = "packsLang.xml";

    protected long bytes = 0;
    protected long freeBytes = 0;

    private Map<String, Pack> namesToPacks;
    private Map<Pack, Integer> packsToRowNumbers;
    private Map<String, List<String>> treeData;

    private CheckTreeController checkTreeController;
    private HashMap<String, CheckBoxNode> idToCheckBoxNode = new HashMap<String, CheckBoxNode>();

    /**
     * The constructor.
     *
     * @param panel       the panel meta-data
     * @param parent      the parent window
     * @param installData the installation data
     * @param resources   the resources
     * @param locales     the supported locales
     * @param rules       the rules
     */
    public TreePacksPanel(Panel panel, InstallerFrame parent, GUIInstallData installData, Resources resources,
                          Locales locales, RulesEngine rules)
    {
        super(panel, parent, installData, resources);

        messages = getAvailableStrings(locales);
        packsModel = new PacksModelGUI(this, installData, rules);
        namesToPacks = packsModel.getNameToPack();
        packsToRowNumbers = packsModel.getPacksToRowNumbers();
        treeData = createTreeData();

        createNormalLayout();
    }

    /**
     * The Implementation of this method should create the layout for the current class.
     */
    protected void createNormalLayout()
    {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        createLabel("PacksPanel.info", "preferences", null, null);
        add(Box.createRigidArea(new Dimension(0, 3)));
        createLabel("PacksPanel.tip", "tip", null, null);
        add(Box.createRigidArea(new Dimension(0, 5)));

        tableScroller = new JScrollPane();
        tableScroller.setColumnHeaderView(null);
        tableScroller.setColumnHeader(null);
        packsTree = createPacksTree(300, tableScroller, null, null);

        if (packsModel.dependenciesExist())
        {
            dependencyArea = createTextArea("PacksPanel.dependencyList", null, null, null);
        }
        descriptionArea = createTextArea("PacksPanel.description", null, null, null);
        spaceLabel = createPanelWithLabel("PacksPanel.space", null, null);
        if (IoHelper.supported("getFreeSpace"))
        {
            add(Box.createRigidArea(new Dimension(0, 3)));
            freeSpaceLabel = createPanelWithLabel("PacksPanel.freespace", null, null);
        }
    }

    /**
     * Layout helper method:<br>
     * Creates an label with a message given by msgId and an icon given by the iconId. If layout and
     * constraints are not null, the label will be added to layout with the given constraints. The
     * label will be added to this object.
     *
     * @param msgId       identifier for the IzPack langpack
     * @param iconId      identifier for the IzPack icons
     * @param layout      layout to be used
     * @param constraints constraints to be used
     * @return the created label
     */
    protected JLabel createLabel(String msgId, String iconId, GridBagLayout layout,
                                 GridBagConstraints constraints)
    {
        JLabel label = LabelFactory.create(getString(msgId), parent.getIcons()
                .get(iconId), TRAILING);
        if (layout != null && constraints != null)
        {
            layout.addLayoutComponent(label, constraints);
        }
        add(label);
        return (label);
    }

    /**
     * Creates a panel containing a anonymous label on the left with the message for the given msgId
     * and a label on the right side with initial no text. The right label will be returned. If
     * layout and constraints are not null, the label will be added to layout with the given
     * constraints. The panel will be added to this object.
     *
     * @param msgId       identifier for the IzPack langpack
     * @param layout      layout to be used
     * @param constraints constraints to be used
     * @return the created (right) label
     */
    protected JLabel createPanelWithLabel(String msgId, GridBagLayout layout, GridBagConstraints constraints)
    {
        JPanel panel = new JPanel();
        JLabel label = new JLabel();
        panel.setAlignmentX(LEFT_ALIGNMENT);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(LabelFactory.create(getString(msgId)));
        panel.add(Box.createHorizontalGlue());
        panel.add(label);
        if (layout != null && constraints != null)
        {
            layout.addLayoutComponent(panel, constraints);
        }
        add(panel);
        return (label);
    }

    /**
     * Creates a text area with standard settings and the title given by the msgId. If scroller is
     * not null, the create text area will be added to the scroller and the scroller to this object,
     * else the text area will be added directly to this object. If layout and constraints are not
     * null, the text area or scroller will be added to layout with the given constraints. The text
     * area will be returned.
     *
     * @param msgId       identifier for the IzPack langpack
     * @param scroller    the scroller to be used
     * @param layout      layout to be used
     * @param constraints constraints to be used
     * @return the created text area
     */
    protected JTextArea createTextArea(String msgId, JScrollPane scroller, GridBagLayout layout,
                                       GridBagConstraints constraints)
    {
        JTextArea area = new JTextArea();
        area.setAlignmentX(LEFT_ALIGNMENT);
        area.setCaretPosition(0);
        area.setEditable(false);
        area.setEditable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(BorderFactory.createTitledBorder(getString(msgId)));
        area.setFont(getControlTextFont());

        if (layout != null && constraints != null)
        {
            if (scroller != null)
            {
                layout.addLayoutComponent(scroller, constraints);
            }
            else
            {
                layout.addLayoutComponent(area, constraints);
            }
        }
        if (scroller != null)
        {
            scroller.setViewportView(area);
            add(scroller);
        }
        else
        {
            add(area);
        }
        return (area);

    }

    /**
     * FIXME Creates the JTree component and calls all initialization tasks
     *
     * @param width
     * @param scroller
     * @param layout
     * @param constraints
     * @return
     */
    protected JTree createPacksTree(int width, JScrollPane scroller, GridBagLayout layout,
                                    GridBagConstraints constraints)
    {
        JTree tree = new JTree(populateTreePacks(null));
        packsTree = tree;
        tree.setCellRenderer(new CheckBoxNodeRenderer(this));
        tree.setEditable(false);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        checkTreeController = new CheckTreeController(this);
        tree.addMouseListener(checkTreeController);
        tree.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        tree.setBackground(Color.white);
        tree.setToggleClickCount(0);

        scroller.setViewportView(tree);
        scroller.setAlignmentX(LEFT_ALIGNMENT);
        scroller.getViewport().setBackground(Color.white);
        scroller.setPreferredSize(new Dimension(width, (this.installData.guiPrefs.height / 3 + 30)));

        if (layout != null && constraints != null)
        {
            layout.addLayoutComponent(scroller, constraints);
        }
        add(scroller);
        return (tree);
    }

    @Override
    public Messages getMessages()
    {
        return messages;
    }

    @Override
    public LocaleDatabase getLangpack()
    {
        return (LocaleDatabase) messages;
    }

    @Override
    public long getBytes()
    {
        return (bytes);
    }

    @Override
    public void setBytes(long bytes)
    {
        this.bytes = bytes;
    }

    @Override
    public void showSpaceRequired()
    {
        if (spaceLabel != null)
        {
            spaceLabel.setText(Pack.toByteUnitsString(bytes));
        }
    }

    @Override
    public void showFreeSpace()
    {
        if (IoHelper.supported("getFreeSpace") && freeSpaceLabel != null)
        {
            String msg = null;
            freeBytes = IoHelper.getFreeSpace(IoHelper.existingParent(
                    new File(this.installData.getInstallPath())).getAbsolutePath());
            if (freeBytes < 0)
            {
                msg = getString("PacksPanel.notAscertainable");
            }
            else
            {
                msg = Pack.toByteUnitsString(freeBytes);
            }
            freeSpaceLabel.setText(msg);
        }
    }

    @Override
    public Debugger getDebugger()
    {
        return null;
    }

    /**
     * Indicates wether the panel has been validated or not.
     *
     * @return true if the needed space is less than the free space, else false
     */
    @Override
    public boolean isValidated()
    {
        refreshPacksToInstall(); //TODO: Replace the use within PacksModel instead
        if (IoHelper.supported("getFreeSpace") && freeBytes >= 0 && freeBytes <= bytes)
        {
            JOptionPane.showMessageDialog(this, getString("PacksPanel.notEnoughSpace"), getString("installer.error"),
                                          JOptionPane.ERROR_MESSAGE);
            return (false);
        }
        return (true);
    }

    @Override
    public void createInstallationRecord(IXMLElement panelRoot)
    {
        new ImgPacksPanelAutomationHelper().createInstallationRecord(this.installData, panelRoot);
    }


    /**
     * This method tries to resolve the localized name of the given pack. If this is not possible,
     * the name given in the installation description file in ELEMENT <pack> will be used.
     *
     * @param pack for which the name should be resolved
     * @return localized name of the pack
     */
    private String getI18NPackName(Pack pack)
    {
        return PackHelper.getPackName(pack, messages);
    }

    public String getI18NPackName(String name)
    {
        Pack pack = namesToPacks.get(name);
        if (pack == null)
        {
            return name;
        }
        return getI18NPackName(pack);
    }

    private void refreshPacksToInstall()
    {
        this.installData.getSelectedPacks().clear();
        CheckBoxNode rootCheckBoxNode = (CheckBoxNode) getTree().getModel().getRoot();
        Enumeration<CheckBoxNode> cbNodes = rootCheckBoxNode.depthFirstEnumeration();
        while (cbNodes.hasMoreElements())
        {
            CheckBoxNode checkBox = cbNodes.nextElement();
            if (checkBox.isSelected() || checkBox.isPartial())
            {
                this.installData.getSelectedPacks().add(checkBox.getPack());
            }
        }
    }

    /**
     * Synchronize the view with the PacksModel data.
     */
    public void updateViewFromModel()
    {
        TreeModel model = this.packsTree.getModel();
        CheckBoxNode root = (CheckBoxNode) model.getRoot();

        syncCheckboxesWithModel(root);
        showSpaceRequired();
        showFreeSpace();
        setBytes(root.getTotalSize());
        checkTreeController.initTotalSize(root, true);
    }

    /**
     * Return the row where the pack is represented
     *
     * @param pack
     * @return the row where the pack is represented
     */
    private int getRowIndex(Pack pack)
    {
        Integer rowNumber = packsToRowNumbers.get(pack);
        if (rowNumber == null)
        {
            return -1;
        }
        return rowNumber;
    }

    /**
     * Helper function for updateViewFromModel() - runs the recursion
     * Update our checkboxes based on the packs model.
     *
     * @param rootNode
     */
    private void syncCheckboxesWithModel(CheckBoxNode rootNode)
    {
        Enumeration<CheckBoxNode> e = rootNode.children();
        while (e.hasMoreElements())
        {
            CheckBoxNode node = e.nextElement();
            String nodeText = node.getId();
            Object nodePack = namesToPacks.get(nodeText);

            int childRowIndex = getRowIndex((Pack) nodePack);
            if (childRowIndex >= 0)
            {
                Integer state = (Integer) packsModel.getValueAt(childRowIndex, 0);
                node.setEnabled(state >= 0);
                if (state == packsModel.PARTIAL_SELECTED)
                {
                    node.setPartial(true);
                }
                else
                {
                    node.setPartial(false);
                }
                if (state == packsModel.REQUIRED_SELECTED
                        || state == packsModel.SELECTED)
                {
                    node.setSelected(true);
                }
                else
                {
                    node.setSelected(false);
                }
            }

            syncCheckboxesWithModel(node);
        }
    }

    /**
     * Updates a value for pack in PacksModel with installDataGUI from a checkbox node
     *
     * @param checkbox This is the checkbox node which contains model values
     */
    public void setModelValue(CheckBoxNode checkbox)
    {
        String name = checkbox.getId();
        Pack pack = namesToPacks.get(name);
        int row = getRowIndex(pack);
        packsModel.toggleValueAt(row);
    }

    /**
     * Initialize tree model structures
     */
    private Map<String, List<String>> createTreeData()
    {
        Map<String, List<String>> treeData = new HashMap<String, List<String>>();

        for (Pack pack : getAvailableShowablePacks())
        {
            if (pack.getParent() != null)
            {
                List<String> kids = null;
                if (treeData.containsKey(pack.getParent()))
                {
                    kids = treeData.get(pack.getParent());
                }
                else
                {
                    kids = new ArrayList<String>();
                }
                kids.add(pack.getName());
                treeData.put(pack.getParent(), kids);
            }
        }

        return treeData;
    }

    /**
     * Shows and updates the description text in the panel
     *
     * @param id
     */
    public void setDescription(String id)
    {
        if (descriptionArea != null)
        {
            Pack pack = namesToPacks.get(id);
            String desc = PackHelper.getPackDescription(pack, messages);
            desc = installData.getVariables().replace(desc);
            descriptionArea.setText(desc);
        }
    }

    /**
     * Shows and updates the dependencies text in the panel
     *
     * @param id
     */
    public void setDependencies(String id)
    {
        if (dependencyArea != null)
        {
            Pack pack = namesToPacks.get(id);
            java.util.List<String> dep = pack.getDependencies();
            String list = "";
            if (dep != null)
            {
                list += (messages == null) ? "Dependencies: " : messages.get("PacksPanel.dependencies");
            }
            for (int j = 0; dep != null && j < dep.size(); j++)
            {
                String name = dep.get(j);
                list += getI18NPackName(namesToPacks.get(name));
                if (j != dep.size() - 1)
                {
                    list += ", ";
                }
            }

            // add the list of the packs to be excluded
            String excludeslist = (messages == null) ? "Excludes: " : messages.get("PacksPanel.excludes");
            int numexcludes = 0;
            int i = getRowIndex(pack);
            if (pack.getExcludeGroup() != null)
            {
                for (int q = 0; q < getAvailableShowablePacks().size(); q++)
                {
                    Pack otherpack = getAvailableShowablePacks().get(q);
                    String exgroup = otherpack.getExcludeGroup();
                    if (exgroup != null)
                    {
                        if (q != i && pack.getExcludeGroup().equals(exgroup))
                        {

                            excludeslist += getI18NPackName(otherpack) + ", ";
                            numexcludes++;
                        }
                    }
                }
            }
            // concatenate
            if (dep != null)
            {
                excludeslist = "    " + excludeslist;
            }
            if (numexcludes > 0)
            {
                list += excludeslist;
            }
            if (list.endsWith(", "))
            {
                list = list.substring(0, list.length() - 2);
            }

            // and display the result
            dependencyArea.setText(list);
        }
    }

    /**
     * Gives a CheckBoxNode instance from the id
     *
     * @param id
     * @return
     */
    public CheckBoxNode getCbnById(String id)
    {
        return this.idToCheckBoxNode.get(id);
    }

    /**
     * Reads the available packs and creates the JTree structure based on
     * the parent definitions.
     *
     * @param parent
     * @return
     */
    private TreeNode populateTreePacks(String parent)
    {
        if (parent == null) // the root node
        {
            List<TreeNode> rootNodes = new ArrayList<TreeNode>();
            for (Pack pack : getAvailableShowablePacks())
            {
                if (pack.getParent() == null)
                {
                    rootNodes.add(populateTreePacks(pack.getName()));
                }
            }
            TreeNode treeNode = new CheckBoxNode("Root", "Root", rootNodes.toArray(), true);
            return treeNode;
        }
        else
        {
            List<TreeNode> links = new ArrayList<TreeNode>();
            List<String> kids = treeData.get(parent);
            Pack pack = namesToPacks.get(parent);
            String translated = getI18NPackName(parent);

            if (kids != null)
            {
                for (String kidId : kids)
                {
                    links.add(populateTreePacks(kidId));
                }

                CheckBoxNode checkBoxNode = new CheckBoxNode(parent, translated, links.toArray(), true);
                idToCheckBoxNode.put(checkBoxNode.getId(), checkBoxNode);
                checkBoxNode.setPack(pack);
                checkBoxNode.setTotalSize(pack.getSize());
                return checkBoxNode;
            }
            else
            {
                CheckBoxNode checkBoxNode = new CheckBoxNode(parent, translated, true);
                idToCheckBoxNode.put(checkBoxNode.getId(), checkBoxNode);
                checkBoxNode.setPack(pack);
                checkBoxNode.setTotalSize(pack.getSize());
                return checkBoxNode;
            }
        }
    }

    /**
     * Called when the panel becomes active. If a derived class implements this method also, it is
     * recommended to call this method with the super operator first.
     */
    @Override
    public void panelActivate()
    {
        updateViewFromModel();

    }

    /*
    * (non-Javadoc)
    *
    * @see com.izforge.izpack.installer.IzPanel#getSummaryBody()
    */
    @Override
    public String getSummaryBody()
    {
        StringBuffer retval = new StringBuffer(256);
        boolean first = true;
        for (Pack pack : this.installData.getSelectedPacks())
        {
            if (!first)
            {
                retval.append("<br>");
            }
            first = false;
            retval.append(getI18NPackName(pack));
        }
        return retval.toString();
    }


    public JTree getTree()
    {
        return packsTree;
    }

    private List<Pack> getAvailableShowablePacks()
    {
        return packsModel.getVisiblePacks();
    }

    private Messages getAvailableStrings(Locales locales)
    {
        Messages messages = installData.getMessages();
        try
        {
            String webDir = installData.getInfo().getWebDirURL();

            boolean fallback = true;
            if (webDir != null)
            {
                InputStream langPackStream = null;
                try
                {
                    java.net.URL url = new java.net.URL(
                            webDir + "/langpacks/" + LANG_FILE_NAME + installData.getLocaleISO3());
                    langPackStream = new WebAccessor(null).openInputStream(url);
                    messages = new LocaleDatabase(langPackStream, messages, locales);
                    fallback = false;
                }
                catch (Exception e)
                {
                    // just ignore this. we use the fallback below
                }
                finally
                {
                    FileUtils.close(langPackStream);
                }
            }
            if (fallback)
            {
                messages = messages.newMessages(LANG_FILE_NAME);
            }
        }
        catch (Throwable t)
        {
            logger.log(Level.WARNING, t.toString(), t);
        }
        return messages;
    }
}


/**
 * Controller class which handles the mouse clicks on checkbox nodes. Also
 * contains utility methods to update the sizes and the states of the nodes.
 *
 * @author <a href="vralev@redhat.com">Vladimir Ralev</a>
 * @version $Revision: 1.1 $
 */
class CheckTreeController extends MouseAdapter
{
    JTree tree;
    TreePacksPanel treePacksPanel;
    int checkWidth = new JCheckBox().getPreferredSize().width;

    public CheckTreeController(TreePacksPanel treePacksPanel)
    {
        this.tree = treePacksPanel.getTree();
        this.treePacksPanel = treePacksPanel;
    }

    @Override
    public void mouseReleased(MouseEvent me)
    {
        CheckBoxNode selectedNode = handleClick(me);
        if (selectedNode == null)
        {
            return;
        }

        treePacksPanel.setModelValue(selectedNode);
        treePacksPanel.updateViewFromModel();



        tree.treeDidChange();
    }

    /**
     * Handle a click event on the JTree.
     * Update the descriptions and dependencies/excludes if a checkbox or checkbox's text was clicked.
     * If a checkbox not clicked return null, otherwise return the checkbox that was clicked.
     * @param mouseEvent
     * @return {@code CheckBoxNode} if clicked otherwise {@code null}
     */
    private CheckBoxNode handleClick(MouseEvent mouseEvent)
    {
        // True when expanding/contracting a group (clicking +/- button)
        // True when clicking on whitespace within the TreePacksPanel area
        TreePath path = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());
        if (path == null)
        {
            return null;
        }

        // If a checkbox or the checkbox's text was clicked update any descriptions
        // Also update any dependencies and/or excludes
        CheckBoxNode selectedNode = (CheckBoxNode) path.getLastPathComponent();
        treePacksPanel.setDescription(selectedNode.getId());
        treePacksPanel.setDependencies(selectedNode.getId());

        // True is the a checkbox was not clicked
        if ((mouseEvent.getX() > tree.getPathBounds(path).x + checkWidth) || selectedNode.getPack().isRequired())
        {
            return null;
        }

        return selectedNode;
    }

    /**
     * Recursivley initialize the amount of space each pack would take.
     *
     * @param rootNode node to start initializing space, generally would use rootNode
     * @param markChanged
     * @return
     */
    public static long initTotalSize(CheckBoxNode rootNode, boolean markChanged)
    {
        if (rootNode.isLeaf())
        {
            return rootNode.getPack().getSize();
        }

        Enumeration<CheckBoxNode> e = rootNode.children();
        Pack nodePack = rootNode.getPack();
        long bytes = 0;
        if (nodePack != null)
        {
            bytes = nodePack.getSize();
        }
        while (e.hasMoreElements())
        {
            CheckBoxNode checkBoxNode = e.nextElement();
            long size = initTotalSize(checkBoxNode, markChanged);
            if (checkBoxNode.isSelected() || checkBoxNode.isPartial())
            {
                bytes += size;
            }
        }
        if (markChanged)
        {
            long old = rootNode.getTotalSize();
            if (old != bytes)
            {
                rootNode.setTotalSizeChanged(true);
            }
            else
            {
                rootNode.setTotalSizeChanged(false);
            }
        }
        rootNode.setTotalSize(bytes);
        return bytes;
    }
}
