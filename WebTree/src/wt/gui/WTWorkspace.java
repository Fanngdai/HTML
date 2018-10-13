package wt.gui;

import djf.AppTemplate;
import djf.components.AppDataComponent;
import djf.components.AppWorkspaceComponent;
import djf.ui.AppGUI;
import static djf.ui.AppGUI.CLASS_BORDERED_PANE;
import djf.ui.AppMessageDialogSingleton;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import properties_manager.PropertiesManager;
import wt.data.HTMLTagPrototype;
import wt.LanguageProperty;
import static wt.LanguageProperty.TAG_LOADING_ERROR_MESSAGE;
import static wt.LanguageProperty.TAG_LOADING_ERROR_TITLE;
import static wt.LanguageProperty.TEMP_PAGE_LOAD_ERROR_MESSAGE;
import static wt.LanguageProperty.TEMP_PAGE_LOAD_ERROR_TITLE;
import static wt.LanguageProperty.UPDATE_ERROR_MESSAGE;
import static wt.LanguageProperty.UPDATE_ERROR_TITLE;
import wt.WebTreeApp;
import static wt.css.WTStyle.CLASS_HEADING_LABEL;
import static wt.css.WTStyle.CLASS_PROMPT_LABEL;
import static wt.css.WTStyle.CLASS_PROMPT_TEXT_FIELD;
import wt.data.WTData;
import static wt.data.HTMLTagPrototype.TAG_HTML;
import wt.file.WTFiles;
import static wt.file.WTFiles.TEMP_PAGE;

import static wt.data.HTMLTagPrototype.TAG_BODY;
import static wt.data.HTMLTagPrototype.TAG_HEAD;
import static wt.data.HTMLTagPrototype.TAG_HTML;
import static wt.data.HTMLTagPrototype.TAG_LINK;
import static wt.data.HTMLTagPrototype.TAG_TITLE;

/**
 * This class serves as the workspace component for this application, providing
 * the user interface controls for editing work.
 *
 * @author Richard McKenna
 * @CoAuthor:	Fanng Dai
 * @version 1.0
 */
public class WTWorkspace extends AppWorkspaceComponent {

    // THESE CONSTANTS ARE FOR TYING THE PRESENTATION STYLE OF
    // THIS WTWorkspace'S COMPONENTS TO A STYLE SHEET THAT IT USES
    static final String CLASS_MAX_PANE = "max_pane";
    static final String CLASS_TAG_BUTTON = "tag_button";
    static final String EMPTY_TEXT = "";
    static final int BUTTON_TAG_WIDTH = 75;

    // HERE'S THE APP
    private final AppTemplate app;

    // IT KNOWS THE GUI IT IS PLACED INSIDE
    private AppGUI gui;

    // THIS HANDLES INTERACTIONS WITH PAGE EDITING CONTROLS
    private WTController pageEditController;

    // WE'LL PUT THE WORKSPACE INSIDE A SPLIT PANE
    private SplitPane workspaceSplitPane;

    // THESE ARE THE BUTTONS FOR ADDING AND REMOVING COMPONENTS
    private BorderPane leftPane;
    private Pane tagToolbar;
    // Will store the VBox which has 11 buttons each
    private HBox tagToolbarH;
    private ScrollPane tagToolbarScrollPane;
    private Button removeButton;
    private ArrayList<Button> tagButtons;
    private HashMap<String, HTMLTagPrototype> tags;

    // THIS IS THE TREE REPRESENTING THE DOM
    private TreeView<HTMLTagPrototype> htmlTree;
    private TreeItem<HTMLTagPrototype> htmlRoot;
    private ScrollPane treeScrollPane;

    // AND FOR EDITING A TAG
    private GridPane tagEditorPane;
    private ScrollPane tagEditorScrollPane;
    private Label tagEditorLabel;
    private ArrayList<Label> tagPropertyLabels;
    private ArrayList<TextField> tagPropertyTextFields;

    // THIS WILL CONTAIN BOTH THE TREE AND THE TREE EDITOR
    private VBox editVBox;

    // THIS IS WHERE WE CAN VIEW THE WEB PAGE OR DIRECTLY EDIT THE CSS
    private TabPane rightPane;
    private WebView htmlView;
    private WebEngine htmlEngine;
    private TextArea cssEditor;
    
    private Button cutButton;
    private Button copyButton;
    private Button pasteButton;
    
    /**
     * Constructor for initializing the workspace, note that this constructor
     * will fully setup the workspace user interface for use.
     *
     * @param initApp The application this workspace is part of.
     *
     * @throws IOException Thrown should there be an error loading application
     * data for setting up the user interface.
     */
    public WTWorkspace(AppTemplate initApp) {
        // KEEP THIS FOR LATER
        app = initApp;

        // WE'LL NEED THIS TO GET LANGUAGE PROPERTIES FOR OUR UI
//        PropertiesManager props = PropertiesManager.getPropertiesManager();
        
        // LAYOUT THE APP
        initLayout();
        
        // HOOK UP THE CONTROLLERS
        initControllers();
        
        // AND INIT THE STYLE FOR THE WORKSPACE
        initStyle();        
    }
    
    private void initLayout() {
	// THIS WILL PROVIDE US WITH OUR CUSTOM UI SETTINGS AND TEXT
	PropertiesManager propsSingleton = PropertiesManager.getPropertiesManager();

	// FIRST THE LEFT HALF OF THE SPLIT PANE
	leftPane = new BorderPane();

	// THIS IS THE LEFT TOOLBAR
//        tagToolbar = app.getGUI().getFileToolbar();
	tagToolbar = new FlowPane(Orientation.VERTICAL);
	tagToolbarScrollPane = new ScrollPane(tagToolbar);
	tagToolbarScrollPane.setFitToHeight(true);
	tagButtons = new ArrayList<>();
	tags = new HashMap<>();

	// FIRST THE REMOVE BUTTON
//        AppGUI gui = app.getGUI();
        gui = app.getGUI();
//                        gui.initChildButton(tagToolbar, TAG_HTML, TAG_HTML, workspaceActivated)
	removeButton = gui.initChildButton(tagToolbar, LanguageProperty.REMOVE_ELEMENT_ICON.toString(), LanguageProperty.REMOVE_ELEMENT_TOOLTIP.toString(), true);
	removeButton.setMaxWidth(BUTTON_TAG_WIDTH);
	removeButton.setMinWidth(BUTTON_TAG_WIDTH);
	removeButton.setPrefWidth(BUTTON_TAG_WIDTH);

	// LOAD ALL THE HTML TAG TYPES
	WTFiles fileManager = (WTFiles) app.getFileComponent();
	WTData dataManager = (WTData) app.getDataComponent();
        
	// AND NOW MAKE THE TREE
	htmlTree = new TreeView<>();
	treeScrollPane = new ScrollPane(htmlTree);

	// NOW RESET THE TREE
	HTMLTagPrototype htmlTag = new HTMLTagPrototype(TAG_HTML, true);
	htmlRoot = new TreeItem<>(htmlTag);
	htmlTree.setRoot(htmlRoot);
	dataManager.setHTMLRoot(htmlRoot);
	dataManager.resetData();

        // Set up the VBox to store buttons
        VBox tagToolbar1 = new VBox();
        tagToolbar1.setSpacing(10);
        tagToolbar1.getChildren().add(removeButton);
        VBox tagToolbar2 = new VBox();
        tagToolbar2.setSpacing(10);

	// AND NOW USE THE LOADED TAG TYPES TO ADD BUTTONS
	for (HTMLTagPrototype tag : dataManager.getTags()) {
	    // MAKE THE BUTTON
	    Button tagButton = new Button(tag.getTagName());
	    tagButton.setDisable(true);
	    tagButtons.add(tagButton);
	    tagButton.setMaxWidth(BUTTON_TAG_WIDTH);
	    tagButton.setMinWidth(BUTTON_TAG_WIDTH);
	    tagButton.setPrefWidth(BUTTON_TAG_WIDTH);
//	    tagToolbar.getChildren().add(tagButton);
            if(tagButtons.size() < 11)
                tagToolbar1.getChildren().add(tagButton);
            else
                tagToolbar2.getChildren().add(tagButton);
	}

        // Add VBox with buttons to HBox
        tagToolbarH = new HBox();
        tagToolbarH.setSpacing(20);
//        tagToolbarH.setPadding(new Insets(20,20,20,20));
        tagToolbarH.setAlignment(Pos.CENTER);
        tagToolbarH.getChildren().addAll(tagToolbar1, tagToolbar2);
        tagToolbar.getChildren().add(tagToolbarH);
        
	// AND NOW THE REGION FOR EDITING TAG PROPERTIES
	tagEditorPane = new GridPane();
	tagEditorScrollPane = new ScrollPane(tagEditorPane);
	tagEditorLabel = new Label("Tag Editor");
	tagPropertyLabels = new ArrayList<>();
	tagPropertyTextFields = new ArrayList<>();

	// PUT THEM IN THE LEFT
	leftPane.setLeft(tagToolbarScrollPane);
	leftPane.setCenter(treeScrollPane);
	leftPane.setBottom(tagEditorScrollPane);

	// NOW FOR THE RIGHT
	rightPane = new TabPane();
	htmlView = new WebView();
	htmlEngine = htmlView.getEngine();
	cssEditor = new TextArea();

	// PUT BOTH ITEMS IN THE TAB PANE
	Tab htmlTab = new Tab();
	htmlTab.setClosable(false);
	htmlTab.setText("HTML");
	htmlTab.setContent(htmlView);

	// NOW FOR THE CSS
	Tab cssTab = new Tab();
	cssTab.setClosable(false);
	cssTab.setText("CSS");
	cssTab.setContent(cssEditor);
	rightPane.getTabs().add(htmlTab);
	rightPane.getTabs().add(cssTab);

	// AND NOW PUT IT IN THE WORKSPACE
	workspaceSplitPane = new SplitPane();
	workspaceSplitPane.getItems().add(leftPane);
	workspaceSplitPane.getItems().add(rightPane);

	// AND FINALLY, LET'S MAKE THE SPLIT PANE THE WORKSPACE
	workspace = new BorderPane();
        // Sets the BorderPane to center of the window
        ((BorderPane) workspace).setCenter(workspaceSplitPane);
//	workspace.getChildren().add(workspaceSplitPane);

        // NOTE THAT WE HAVE NOT PUT THE WORKSPACE INTO THE WINDOW,
	// THAT WILL BE DONE WHEN THE USER EITHER CREATES A NEW
	// COURSE OR LOADS AN EXISTING ONE FOR EDITING
	workspaceActivated = false;

	// MAKE SURE THE FILE MANAGER HAS THE ROOT AND THEN
	// EXPORT THE SITE TO THE temp DIRECTORY. THEN, LOAD
	// IT INTO THE WEB ENGINE
	dataManager.setHTMLRoot(htmlRoot);
        try {
            fileManager.exportData(dataManager, TEMP_PAGE);
            loadTempPage();
        }
        catch(IOException ioe) {
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
            PropertiesManager props = PropertiesManager.getPropertiesManager();
            dialog.show(props.getProperty(TAG_LOADING_ERROR_TITLE),
                        props.getProperty(TAG_LOADING_ERROR_MESSAGE));
        }
    }
    
    private boolean cssSelected = false;
    private boolean pasteCss = true;
    boolean pasteNode = false;
    
    private void initControllers() {
	// THIS WILL MANAGE ALL EDITING EVENTS
	pageEditController = new WTController((WebTreeApp) app);
        pasteNode = pageEditController.getPasteNode();
        
        gui = app.getGUI();
        cutButton = gui.getCut();
        copyButton = gui.getCopy();
        pasteButton = gui.getPaste();
        
	removeButton.setOnAction(e -> {
	    pageEditController.handleRemoveElementRequest();
	});

        // The tree, the pane between buttons and tags
	htmlTree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
	htmlTree.getSelectionModel().selectedItemProperty().addListener(e -> {
            cssSelected = false;
            pasteButton.setDisable(pageEditController.getPasteNode());
            AppDataComponent data = app.getDataComponent();
	    reloadWorkspace(data);
	    refreshTagButtons();
            cutButton.setDisable(false);
            copyButton.setDisable(false);
            String tagName = htmlTree.getSelectionModel().selectedItemProperty().getValue().getValue().getTagName();
            // enable removeButton after selecting a tag unless it is default tags
            if(tagName.equals(TAG_BODY) || tagName.equals(TAG_HEAD) || tagName.equals(TAG_HTML)
                    || tagName.equals(TAG_LINK) || tagName.equals(TAG_TITLE)){
                removeButton.setDisable(true);
            }
            else
                removeButton.setDisable(false);
	});
        
        // SETUP THE RESPONSE TO CSS EDITING
	cssEditor.textProperty().addListener(e -> {
	    pageEditController.handleCSSEditing(cssEditor.getText());
	});
        
        // Putting actions to the cut, copy and paste buttons
         cutButton.setOnAction(e ->{
             if(cssSelected){
                pasteCss = false;
                cssEditor.cut();
                pasteButton.setDisable(pasteCss);
             }
             else{
//                pageEditController.handleCopyRequest();
//                pageEditController.handleRemoveElementRequest();
                pasteNode = false;
                pageEditController.handleCutRequest();
                pasteButton.setDisable(pageEditController.getPasteNode());
                // Must copy before paste
             }
        });
        copyButton.setOnAction(e ->{
            if(cssSelected){
                pasteCss = false;
                cssEditor.copy();
                pasteButton.setDisable(pasteCss);
             }
            else{
                pasteNode = false;
                pageEditController.handleCopyRequest();
                // Must copy before paste
                pasteButton.setDisable(pageEditController.getPasteNode());
                
            }
        });
        pasteButton.setOnAction(e ->{
            if(cssSelected){
                 cssEditor.paste();
             }
            else
                pageEditController.handlePasteRequest();
        });
        
        // WE KNOW WE ONLY PUT BUTTONS IN THIS TOOLBAR
        for (int i = 0; i < tagToolbarH.getChildren().size(); i++) {
            VBox tagToolbarVBox = (VBox) tagToolbarH.getChildren().get(i);
            for(int j = 0; j < tagToolbarVBox.getChildren().size(); j++){
                Button tagButton = (Button)tagToolbarVBox.getChildren().get(j);
                WTData data = (WTData)app.getDataComponent();
                // INIT ITS EVENT HANDLER
                tagButton.setOnAction(e -> {
                    // Remove button 
                    if(tagButton.equals(removeButton)){
                        pageEditController.handleRemoveElementRequest();
                    }
                    // Add a node
                    else{
                        String tagName = tagButton.getText();
                        HTMLTagPrototype clickedTag = data.getTag(tagName);
                        pageEditController.handleAddElementRequest(clickedTag);
                    }
                });
            }
        }
        
        cssEditor.focusedProperty().addListener( e -> {
            cssSelected = true;
            cutButton.setDisable(false);
            copyButton.setDisable(false);
            pasteButton.setDisable(pasteCss);
        });
        
        treeScrollPane.focusedProperty().addListener( e -> {
            cssSelected = false;
            cutButton.setDisable(false);
            copyButton.setDisable(false);
            pasteButton.setDisable(pageEditController.getPasteNode());
        });
    }

    /**
     * Accessor method for getting the html engine, which is tied to the page
     * display.
     *
     * @return The html engine for the Web view component.
     */
    public WebEngine getHTMLEngine() {
	return htmlEngine;
    }

    /**
     * Accessor method for getting the html tree, which contains all the tags
     * for the page being edited.
     *
     * @return The html tree being edited.
     */
    public TreeView<HTMLTagPrototype> getHTMLTree() {
	return htmlTree;
    }

    /**
     * Accessor method for getting the root node of the html tree. Through that
     * node one can access the full DOM.
     *
     * @return The root node of the html tree currently being edited.
     */
    public TreeItem<HTMLTagPrototype> getHTMLRoot() {
	return htmlRoot;
    }

    /**
     * Mutator method for setting the root node for the html tree.
     *
     * @param initRoot The node to use to set the root.
     */
    public void setHTMLRoot(TreeItem<HTMLTagPrototype> initRoot) {
	htmlTree.setRoot(initRoot);
	htmlRoot = initRoot;
    }

    /**
     * This function specifies the CSS style classes for all the UI components
     * known at the time the workspace is initially constructed. Note that the
     * tag editor controls are added and removed dynamicaly as the application
     * runs so they will have their style setup separately.
     */
    private void initStyle() {
	// NOTE THAT EACH CLASS SHOULD CORRESPOND TO
	// A STYLE CLASS SPECIFIED IN THIS APPLICATION'S
	// CSS FILE
	tagToolbar.getStyleClass().add(CLASS_BORDERED_PANE);
	removeButton.getStyleClass().add(CLASS_TAG_BUTTON);
	for (Button b : tagButtons) {
	    b.getStyleClass().add(CLASS_TAG_BUTTON);
	}
	leftPane.getStyleClass().add(CLASS_MAX_PANE);
	treeScrollPane.getStyleClass().add(CLASS_MAX_PANE);
	tagEditorLabel.getStyleClass().add(CLASS_HEADING_LABEL);
        
        // Used for making the tagToolbar button circular.
//        for(Node button: tagToolbar.getChildren()){
//            button.getStyleClass().add(CLASS_TAG_BUTTON);
////            button.getStyleClass().add(CLASS_TAG_BUTTON);
//        }
    }
    
    public void resetWorkspace() {
        
    }

    /**
     * This function reloads all the controls for editing tag attributes into
     * the workspace.
     */
    @Override
    public void reloadWorkspace(AppDataComponent dataComponent) {
	try {
	    // WE DON'T WANT TO RESPOND TO EVENTS FORCED BY
	    // OUR INITIALIZATION SELECTIONS
	    pageEditController.enable(false);

	    // FIRST CLEAR OUT THE OLD STUFF
	    tagPropertyLabels.clear();
	    tagPropertyTextFields.clear();
	    tagEditorPane.getChildren().clear();

	    // ADD THE LABEL
	    tagEditorPane.add(tagEditorLabel, 0, 0, 2, 1);

	    // THEN LOAD IN ALL THE NEW STUFF
	    TreeItem<HTMLTagPrototype> selectedItem = htmlTree.getSelectionModel().getSelectedItem();
	    if (selectedItem != null) {
		HTMLTagPrototype selectedTag = selectedItem.getValue();
		HashMap<String, String> attributes = selectedTag.getAttributes();
		Collection<String> keys = attributes.keySet();
		int row = 1;
		for (String attributeName : keys) {
		    String attributeValue = selectedTag.getAttribute(attributeName);
		    Label attributeLabel = new Label(attributeName + ": ");
		    attributeLabel.getStyleClass().add(CLASS_PROMPT_LABEL);
		    TextField attributeTextField = new TextField(attributeValue);
		    attributeTextField.getStyleClass().add(CLASS_PROMPT_TEXT_FIELD);
		    tagEditorPane.add(attributeLabel, 0, row);
		    tagEditorPane.add(attributeTextField, 1, row);
		    attributeTextField.textProperty().addListener(e -> {
			// UPDATE THE TEMP SITE AS WE TYPE ATTRIBUTE VALUES
			pageEditController.handleAttributeUpdate(selectedTag, attributeName, attributeTextField.getText());
		    });
		    row++;
		}
	    }

	    // LOAD THE CSS
	    WTData dataManager = (WTData) app.getDataComponent();
	    cssEditor.setText(dataManager.getCSSText());
            
	    // THEN FORCE THE CHANGES TO THE TEMP HTML PAGE
	    WTFiles fileManager = (WTFiles) app.getFileComponent();
	    fileManager.exportData(dataManager, TEMP_PAGE);
           
	    // AND REFRESH THE BROWSER
	    htmlEngine.reload();
            
	    // WE DON'T WANT TO RESPOND TO EVENTS FORCED BY
	    // OUR INITIALIZATION SELECTIONS
	    pageEditController.enable(true);
	} catch (Exception e) {
	    AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
	    PropertiesManager props = PropertiesManager.getPropertiesManager();
	    dialog.show(props.getProperty(UPDATE_ERROR_TITLE), props.getProperty(UPDATE_ERROR_MESSAGE));
	}
    }

    /**
     * This function loads the temp page into the web view.
     */
    public void loadTempPage() {
	String urlPath = TEMP_PAGE;
	File webPageFile = new File(urlPath);
	try {
	    URL pageURL = webPageFile.toURI().toURL();
	    String pagePath = pageURL.toString();
	    htmlEngine.load(pagePath);
	} catch (MalformedURLException murle) {
	    PropertiesManager props = PropertiesManager.getPropertiesManager();
	    AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
	    dialog.show(props.getProperty(TEMP_PAGE_LOAD_ERROR_TITLE), props.getProperty(TEMP_PAGE_LOAD_ERROR_MESSAGE));
	}
    }
    
    public void refreshTagButtons() {
	TreeItem<HTMLTagPrototype> selectedItem = htmlTree.getSelectionModel().getSelectedItem();
	WTData dataManager = (WTData)app.getDataComponent();
	if (selectedItem != null) {
	    HTMLTagPrototype selectedTag = selectedItem.getValue();
	    for (int i = 0; i < tagButtons.size(); i++) {
		Button tB = tagButtons.get(i);
		HTMLTagPrototype testTag = dataManager.getTag(tB.getText());
		tB.setDisable(!testTag.isLegalParent(selectedTag.getTagName()));
	    }
	}
    }
}