package wt.data;

import djf.AppTemplate;
import djf.components.AppDataComponent;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.scene.control.TreeItem;
import static wt.data.HTMLTagPrototype.ATT_HREF;
import static wt.data.HTMLTagPrototype.ATT_REL;
import static wt.data.HTMLTagPrototype.ATT_TYPE;
import static wt.data.HTMLTagPrototype.HREF_HOME;
import static wt.data.HTMLTagPrototype.REL_STYLESHEET;
import static wt.data.HTMLTagPrototype.TAG_BODY;
import static wt.data.HTMLTagPrototype.TAG_HEAD;
import static wt.data.HTMLTagPrototype.TAG_LINK;
import static wt.data.HTMLTagPrototype.TAG_TITLE;
import static wt.data.HTMLTagPrototype.TYPE_TEXT_CSS;
import wt.file.WTFiles;
//import wt.gui.WTWorkspace;

/**
 * This class serves as the data management component for this application.
 *
 * @author Richard McKenna
 * @CoAuthor:	Fanng Dai
 * @version 1.0
 */
public class WTData implements AppDataComponent {

    // THIS FILE HAS THE LIST OF TAGS OUR APPLICATION WILL USE
    static final String TAG_TYPES_FILE_PATH = "data/tags.json";

    // THESE ARE ALL THE AVAILABLE TAGS FROM WHICH WE WILL CLONE
    private final ArrayList<HTMLTagPrototype> tags;
    
    // THIS PROVIDES A NICE EASY TO LOOKUP DICTIONARY FOR THE TAGS
    // FOR WHEN WE HAVE TO CLONE THEM
    private final HashMap<String, HTMLTagPrototype> hashTags;

    // THIS IS THE ROOT OF THE TREE, FROM WHICH WE CAN
    // ACCESS THE ENTIRE TREE
    private TreeItem<HTMLTagPrototype> htmlRoot;
    
    // THE FULL CONTENTS OF THE CSS FILE
    private String cssText;

    // THIS IS A SHARED REFERENCE TO THE APPLICATION
    private final AppTemplate app;

    /**
     * THis constructor creates the data manager and sets up the tags to be used.
     *
     * @param initApp The application within which this data manager is serving.
     */
    public WTData(AppTemplate initApp) {
	// KEEP THE APP FOR LATER
	app = initApp;

	// WE'LL STORE THE TAGS HERE
	tags = new ArrayList<>();
	hashTags = new HashMap<>();

	// NOW LOAD ALL THE TAGS WE'LL USE
	WTFiles fileManager = (WTFiles) app.getFileComponent();
	fileManager.loadHTMLTags(this, TAG_TYPES_FILE_PATH);
    }
    
    /**
     * Accessor method for getting the CSS text.
     * 
     * @return The contents of the CSS file for the page.
     */
    public String getCSSText() {
	return cssText;
    }
    
    /**
     * Mutator method for setting css text.
     * 
     * @param initCSSText The text to set for the css text.
     */
    public void setCSSText(String initCSSText) {
	cssText = initCSSText;
    }

    /**
     * Accessor method for getting the tree's root node.
     *
     * @return The root of the tree.
     */
    public TreeItem<HTMLTagPrototype> getHTMLRoot() {
	return htmlRoot;
    }

    /**
     * Mutator method for setting the tree's root node.
     *
     * @param initHTMLRoot The value to set as the root of the tree.
     */
    public void setHTMLRoot(TreeItem<HTMLTagPrototype> initHTMLRoot) {
	htmlRoot = initHTMLRoot;
    }

    /**
     * Accessor method for getting a tag.
     *
     * @param tagName The name of the tag to return.
     *
     * @return The HTMLTagPrototype object that has tagName as its name.
     */
    public HTMLTagPrototype getTag(String tagName) {
	return hashTags.get(tagName);
    }

    /**
     * This method adds the tag argument to the set of tags.
     *
     * @param tag A tag representing an HTML element.
     */
    public void addTag(HTMLTagPrototype tag) {
	tags.add(tag);
	hashTags.put(tag.getTagName(), tag);
    }
    /**
     * Accessor method for getting all the tags.
     *
     * @return A list containing all the tags used by this data manager.
     */
    public ArrayList<HTMLTagPrototype> getTags() {
	return tags;
    }

    /**
     * This function clears out the HTML tree and reloads it with the minimal
     * tags, like html, head, and body such that the user can begin editing a
     * page.
     */
    @Override
    public void resetData() {
	// LET'S BUILD OUR START TAGS
	HTMLTagPrototype headTag = new HTMLTagPrototype(TAG_HEAD, true);
	HTMLTagPrototype titleTag = new HTMLTagPrototype(TAG_TITLE, true);
	HTMLTagPrototype linkTag = new HTMLTagPrototype(TAG_LINK, false);
	linkTag.addAttribute(ATT_REL, REL_STYLESHEET);
	linkTag.addAttribute(ATT_TYPE, TYPE_TEXT_CSS);
	linkTag.addAttribute(ATT_HREF, HREF_HOME);
	HTMLTagPrototype bodyTag = new HTMLTagPrototype(TAG_BODY, true);
        
	// NOW MAKE THE NODES
	TreeItem<HTMLTagPrototype> headItem = new TreeItem<>(headTag);
	TreeItem<HTMLTagPrototype> titleItem = new TreeItem<>(titleTag);
	TreeItem<HTMLTagPrototype> linkItem = new TreeItem<>(linkTag);
	TreeItem<HTMLTagPrototype> bodyItem = new TreeItem<>(bodyTag);

	// FIRST CLEAR OUT ANY OLD STUFF
	htmlRoot.getChildren().clear();

	// AND ARRANGE THEM IN THE TREE
	htmlRoot.getChildren().add(headItem);
	headItem.getChildren().add(titleItem);
	headItem.getChildren().add(linkItem);
	htmlRoot.getChildren().add(bodyItem);
	
	// AND FINALLY CLEAR THE CSS
	cssText = "";
        
        // Disable the copy, cut and paste buttons
        app.getGUI().getCopy().setDisable(true);
        app.getGUI().getCut().setDisable(true);
        app.getGUI().getPaste().setDisable(true);
    }
}
