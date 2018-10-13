package wt.gui;

import djf.components.AppDataComponent;
import static djf.settings.AppPropertyType.NEW_COMPLETED_MESSAGE;
import static djf.settings.AppPropertyType.NEW_COMPLETED_TITLE;
import djf.ui.AppMessageDialogSingleton;
import java.io.IOException;
import javafx.event.EventType;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.web.WebEngine;
import properties_manager.PropertiesManager;
import static wt.LanguageProperty.ADD_ELEMENT_ERROR_MESSAGE;
import static wt.LanguageProperty.ADD_ELEMENT_ERROR_TITLE;
import static wt.LanguageProperty.ATTRIBUTE_UPDATE_ERROR_MESSAGE;
import static wt.LanguageProperty.ATTRIBUTE_UPDATE_ERROR_TITLE;
import static wt.LanguageProperty.CSS_EXPORT_ERROR_MESSAGE;
import static wt.LanguageProperty.CSS_EXPORT_ERROR_TITLE;
import static wt.LanguageProperty.ILLEGAL_NODE_REMOVAL_ERROR_MESSAGE;
import static wt.LanguageProperty.ILLEGAL_NODE_REMOVAL_ERROR_TITLE;
import static wt.LanguageProperty.REMOVE_ELEMENT_ERROR_MESSAGE;
import static wt.LanguageProperty.REMOVE_ELEMENT_ERROR_TITLE;
import static wt.LanguageProperty.UNABLE_TO_PASTE;
import static wt.LanguageProperty.ILLEGAL_NODE_COPY_ERROR_MESSAGE;
import static wt.LanguageProperty.ILLEGAL_NODE_COPY_ERROR_TITLE;
import wt.WebTreeApp;
import wt.data.WTData;
import wt.data.HTMLTagPrototype;
import static wt.data.HTMLTagPrototype.TAG_BODY;
import static wt.data.HTMLTagPrototype.TAG_HEAD;
import static wt.data.HTMLTagPrototype.TAG_HTML;
import static wt.data.HTMLTagPrototype.TAG_LINK;
import static wt.data.HTMLTagPrototype.TAG_TITLE;
import wt.file.WTFiles;
import static wt.file.WTFiles.TEMP_HOME_CSS;
import static wt.file.WTFiles.TEMP_PAGE;

/**
 * This class provides event programmed responses to workspace interactions for
 * this application for things like adding elements, removing elements, and
 * editing them.
 *
 * @author Richard McKenna
 * @CoAuthor:	Fanng Dai
 * @version 1.0
 */
public class WTController {

    // HERE'S THE FULL APP, WHICH GIVES US ACCESS TO OTHER STUFF
    private final WebTreeApp app;

    // WE USE THIS TO MAKE SURE OUR PROGRAMMED UPDATES OF UI
    // VALUES DON'T THEMSELVES TRIGGER EVENTS
    private boolean enabled;

    // Store the node which was copied or cut
    private static TreeItem<HTMLTagPrototype> copyItem;
    
    // Homework 1 part 9
    private boolean pasteNode = true;

    
    public boolean getPasteNode(){
        return pasteNode;
    }
    
    /**
     * Constructor for initializing this object, it will keep the app for later.
     *
     * @param initApp The JavaFX application this controller is associated with.
     */
    public WTController(WebTreeApp initApp) {
	// KEEP IT FOR LATER
	app = initApp;
    }

    /**
     * This mutator method lets us enable or disable this controller.
     *
     * @param enableSetting If false, this controller will not respond to
     * workspace editing. If true, it will.
     */
    public void enable(boolean enableSetting) {
	enabled = enableSetting;
    }

    /**
     * This function responds live to the user typing changes into a text field
     * for updating element attributes. It will respond by updating the
     * appropriate data and then forcing an update of the temp site and its
     * display.
     *
     * @param selectedTag The element in the DOM (our tree) that's currently
     * selected and therefore is currently having its attribute updated.
     *
     * @param attributeName The name of the attribute for the element that is
     * currently being updated.
     *
     * @param attributeValue The new value for the attribute that is being
     * updated.
     */
    public void handleAttributeUpdate(HTMLTagPrototype selectedTag, String attributeName, String attributeValue) {
	if (enabled) {
	    try {
		// FIRST UPDATE THE ELEMENT'S DATA
		selectedTag.addAttribute(attributeName, attributeValue);

		// THEN FORCE THE CHANGES TO THE TEMP HTML PAGE
		WTFiles fileManager = (WTFiles) app.getFileComponent();
		fileManager.exportData(app.getDataComponent(), TEMP_PAGE);

		// AND FINALLY UPDATE THE WEB PAGE DISPLAY USING THE NEW VALUES
		WTWorkspace workspace = (WTWorkspace) app.getWorkspaceComponent();
		workspace.getHTMLEngine().reload();
                
                // Updated something - The tag Editor has been modified
                app.getGUI().getFileController().markAsEdited(app.getGUI());
	    } catch (IOException ioe) {
		// AN ERROR HAPPENED WRITING TO THE TEMP FILE, NOTIFY THE USER
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
		dialog.show(props.getProperty(ATTRIBUTE_UPDATE_ERROR_TITLE), props.getProperty(ATTRIBUTE_UPDATE_ERROR_MESSAGE));
                // Load new file homework 1
                app.getGUI().getFileController().handleNewRequest();
	    }
	}
    }

    /**
     * This function responds to when the user tries to add an element to the
     * tree being edited.
     *
     * @param element The element to add to the tree.
     */
    public void handleAddElementRequest(HTMLTagPrototype element) {
	if (enabled) {
	    WTWorkspace workspace = (WTWorkspace) app.getWorkspaceComponent();

	    // GET THE TREE TO SEE WHICH NODE IS CURRENTLY SELECTED
	    TreeView<HTMLTagPrototype> tree = workspace.getHTMLTree();
	    TreeItem<HTMLTagPrototype> selectedItem = tree.getSelectionModel().getSelectedItem();
	    HTMLTagPrototype selectedTag = selectedItem.getValue();

	    // MAKE A NEW HTMLTagPrototype AND PUT IT IN A NODE
	    HTMLTagPrototype newTag = element.clone();
	    TreeItem<HTMLTagPrototype> newNode = new TreeItem<>(newTag);

	    // ONLY ADD IT IF IT'S BEING ADDED TO A LEGAL NEIGHBOR
	    if (newTag.isLegalParent(selectedTag.getTagName())) {
		selectedItem.getChildren().add(newNode);

		// SELECT THE NEW NODE
		tree.getSelectionModel().select(newNode);
		selectedItem.setExpanded(true);

		// FORCE A RELOAD OF TAG EDITOR
                AppDataComponent data = app.getDataComponent();
		workspace.reloadWorkspace(data);

		workspace.refreshTagButtons();
		try {
		    WTFiles fileManager = (WTFiles) app.getFileComponent();
		    fileManager.exportData(data, TEMP_PAGE);
                    
                    // AND UPDATE THE PAGE
		    workspace.getHTMLEngine().reload();
                    
                    // Updated something - A node has been added
                    app.getGUI().getFileController().markAsEdited(app.getGUI());
		} catch (IOException ioe) {
		    // AN ERROR HAPPENED WRITING TO THE TEMP FILE, NOTIFY THE USER
		    PropertiesManager props = PropertiesManager.getPropertiesManager();
		    AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
		    dialog.show(props.getProperty(ADD_ELEMENT_ERROR_TITLE), props.getProperty(ADD_ELEMENT_ERROR_MESSAGE));
		}
	    }
	}
    }

    /**
     * This function responds to when the user requests to remove an element
     * from the tree. It responds by removing the currently selected node.
     */
    public void handleRemoveElementRequest() {
	PropertiesManager props = PropertiesManager.getPropertiesManager();

	if (enabled) {
	    WTWorkspace workspace = (WTWorkspace) app.getWorkspaceComponent();

	    // GET THE TREE TO SEE WHICH NODE IS CURRENTLY SELECTED
	    TreeView<HTMLTagPrototype> tree = workspace.getHTMLTree();
	    TreeItem<HTMLTagPrototype> selectedItem = tree.getSelectionModel().getSelectedItem();

	    // DON'T LET THE USER REMOVE THE HTML, HEAD,
	    // TITLE, LINK, OR BODY TAGS
	    HTMLTagPrototype selectedTag = selectedItem.getValue();
	    String tagName = selectedTag.getTagName();

	    // DON'T LET THE USER DELETE THESE ELEMENTS
	    if (tagName.equals(TAG_HTML)
		    || tagName.equals(TAG_HEAD)
		    || tagName.equals(TAG_TITLE)
		    || tagName.equals(TAG_LINK)
		    || tagName.equals(TAG_BODY)) {
		AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
		dialog.show(props.getProperty(ILLEGAL_NODE_REMOVAL_ERROR_MESSAGE), props.getProperty(ILLEGAL_NODE_REMOVAL_ERROR_TITLE));
	    } else {
		TreeItem<HTMLTagPrototype> parentNode = selectedItem.getParent();

		parentNode.getChildren().remove(selectedItem);
		tree.getSelectionModel().select(parentNode);

		// FORCE A RELOAD OF TAG EDITOR
                AppDataComponent data = app.getDataComponent();
		workspace.reloadWorkspace(data);
                workspace.refreshTagButtons();
		try {
		    // NOW FORCE THE CHANGES TO OUR TEMP FILE
		    WTFiles fileManager = (WTFiles) app.getFileComponent();
		    fileManager.exportData(app.getDataComponent(), TEMP_PAGE);

		    // AND UPDATE THE PAGE
		    workspace.getHTMLEngine().reload();
                    
                    // Updated something - A node has been removed
                    app.getGUI().getFileController().markAsEdited(app.getGUI());
		} catch (IOException ioe) {
		    AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
		    dialog.show(props.getProperty(REMOVE_ELEMENT_ERROR_TITLE), props.getProperty(REMOVE_ELEMENT_ERROR_MESSAGE));
		}
	    }
	}
    }
    
    // If you cannot copy, you shouldn't be able to paste.
    // Let user know once
    private boolean cutCopyOkay = false;
    /**
     * Copy a selected node and all of it's children.
     * 
     * Homework1
     */
    public void handleCopyRequest(){
        cutCopyOkay = false;
	if (enabled) {
	    WTWorkspace workspace = (WTWorkspace) app.getWorkspaceComponent();

	    // GET THE TREE TO SEE WHICH NODE IS CURRENTLY SELECTED
	    TreeView<HTMLTagPrototype> tree = workspace.getHTMLTree();
	    TreeItem<HTMLTagPrototype> selectedItem = tree.getSelectionModel().getSelectedItem();

	    // DON'T LET THE USER REMOVE THE HTML, HEAD,
	    // TITLE, LINK, OR BODY TAGS
	    HTMLTagPrototype selectedTag = selectedItem.getValue();
	    String tagName = selectedTag.getTagName();

	    // DON'T LET THE USER DELETE THESE ELEMENTS
	    if (tagName.equals(TAG_HTML)
		    || tagName.equals(TAG_HEAD)
		    || tagName.equals(TAG_TITLE)
		    || tagName.equals(TAG_LINK)
		    || tagName.equals(TAG_BODY)) {
		AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
                // Print error
                PropertiesManager props = PropertiesManager.getPropertiesManager();
		dialog.show(props.getProperty(ILLEGAL_NODE_COPY_ERROR_TITLE), props.getProperty(ILLEGAL_NODE_COPY_ERROR_MESSAGE));
	    }
            else{
                // The node which the user selected
                copyItem = tree.getSelectionModel().getSelectedItem();
                pasteNode = false;
                cutCopyOkay = true;
            }
	}
    }
    
//    // MAKE A NEW HTMLTagPrototype AND PUT IT IN A NODE
//	    HTMLTagPrototype newTag = element.clone();
//	    TreeItem<HTMLTagPrototype> newNode = new TreeItem<>(newTag);
//
//	    // ONLY ADD IT IF IT'S BEING ADDED TO A LEGAL NEIGHBOR
//	    if (newTag.isLegalParent(selectedTag.getTagName())) {
//		selectedItem.getChildren().add(newNode);
	
    /**
     * Paste the copied or cut item to the selected node.
     * Just paste the copyItem since it is a clone of the original.
     * 
     * Homework1
     */
    public void handlePasteRequest() {
        PropertiesManager props = PropertiesManager.getPropertiesManager();
	if (enabled) {
	    WTWorkspace workspace = (WTWorkspace) app.getWorkspaceComponent();

	    // GET THE TREE TO SEE WHICH NODE IS CURRENTLY SELECTED
	    TreeView<HTMLTagPrototype> tree = workspace.getHTMLTree();
	    TreeItem<HTMLTagPrototype> selectedItem = tree.getSelectionModel().getSelectedItem();
	    HTMLTagPrototype selectedTag = selectedItem.getValue();

            copyItem = cloneTreeItem(copyItem);
            
            try {
                // Make sure that the selected node is a legal parent
                if (copyItem.getValue().isLegalParent(selectedTag.getTagName())) {
                    selectedItem.getChildren().add(copyItem);
                }
                // If node is not an elgible parent, let the user know node cannot be pasted here
                else{
                    throw new IOException();
                }

            // FORCE A RELOAD OF TAG EDITOR
            AppDataComponent data = app.getDataComponent();
            workspace.reloadWorkspace(data);
            
		// NOW FORCE THE CHANGES TO OUR TEMP FILE
		WTFiles fileManager = (WTFiles) app.getFileComponent();
		fileManager.exportData(app.getDataComponent(), TEMP_PAGE);

		// AND UPDATE THE PAGE
		workspace.getHTMLEngine().reload();
                
                // Updated something - a tree has been added
                app.getGUI().getFileController().markAsEdited(app.getGUI());
            }
            catch (IOException ioe) {
		AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
		dialog.show(props.getProperty(UNABLE_TO_PASTE), props.getProperty(UNABLE_TO_PASTE));
            }
	}
    }
    
    /**
     * Handles the cut request which copies and remove node.
     * If the node cannot be pasted, it will be be able to copy thus,
     * let the user know.
     */
    public void handleCutRequest() {
        handleCopyRequest();
        if(cutCopyOkay){
            handleRemoveElementRequest();
        }
    }
    /**
     * Clone the whole TreeItem vector.
     * 
     * Homework1
     * @param element
     *  TreeItem to clone.
     * 
     * @return 
     *  A cloned version of the element.
     */
    public TreeItem<HTMLTagPrototype> cloneTreeItem(TreeItem<HTMLTagPrototype> element){
        HTMLTagPrototype newTag = element.getValue().clone();
        TreeItem<HTMLTagPrototype> temp = new TreeItem<>(newTag);
        for( int i=0; i<element.getChildren().size(); i++){
            temp.getChildren().add(cloneTreeItem(element.getChildren().get(i)));
        }
        return temp;
    }
    
    /**
     * This function provides a response to when the user changes the CSS
     * content. It responds but updating the data manager with the new CSS text,
     * and by exporting the CSS to the temp css file.
     *
     * @param cssContent The css content.
     *
     * @throws IOException Thrown should an error occur while writing out to the
     * CSS file.
     */
    public void handleCSSEditing(String cssContent) {
	if (enabled) {
	    try {
		// MAKE SURE THE DATA MANAGER GETS THE CSS TEXT
		WTData dataManager = (WTData) app.getDataComponent();
		dataManager.setCSSText(cssContent);
                
		// WRITE OUT THE TEXT TO THE CSS FILE
		WTFiles fileManager = (WTFiles) app.getFileComponent();
		fileManager.exportCSS(cssContent, TEMP_HOME_CSS);

		// REFRESH THE HTML VIEW VIA THE ENGINE
		WTWorkspace workspace = (WTWorkspace) app.getWorkspaceComponent();
		WebEngine htmlEngine = workspace.getHTMLEngine();
		htmlEngine.reload();
                
                // Updated something - something in the css editor has been modified
                app.getGUI().getFileController().markAsEdited(app.getGUI());
	    } catch (IOException ioe) {
		AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
		PropertiesManager props = PropertiesManager.getPropertiesManager();
		dialog.show(props.getProperty(CSS_EXPORT_ERROR_TITLE), props.getProperty(CSS_EXPORT_ERROR_MESSAGE));
                // Load new file homework 1
                app.getGUI().getFileController().handleNewRequest();
	    }
	}
    }
}
