/*
* Copyright (c) 2013, TeamCMPUT301F13T02
* All rights reserved.
* 
* Redistribution and use in source and binary forms, with or without modification,
* are permitted provided that the following conditions are met:
* 
* Redistributions of source code must retain the above copyright notice, this
* list of conditions and the following disclaimer.
* 
* Redistributions in binary form must reproduce the above copyright notice, this
* list of conditions and the following disclaimer in the documentation and/or
* other materials provided with the distribution.
* 
* Neither the name of the {organization} nor the names of its
* contributors may be used to endorse or promote products derived from
* this software without specific prior written permission.
* 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
* ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
* ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package ca.ualberta.CMPUT301F13T02.chooseyouradventure;

import java.util.ArrayList;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * The Activity in the application that is responsible for viewing and editing
 * a page within a story.  <br />
 * <br />
 * In this activity a reader can:
 * <ol>
 *     <li> Read the page </li>
 *     <li> Follow decisions at the bottom </li>
 *     <li> Comment on the page </li>
 * </ol>
 * In this activity an author can: 
 * <ol>
 *     <li> Edit the tiles on this page (add, edit, reorder, delete) </li>
 * </ol>
 * 
 * The ViewPageActivity is a view of the application.
 * 
 * TODO This activity will need to be able to display and edit Audio-, Video-, and Photo- Tiles
 */

public class ViewPageActivity extends Activity {
	
	private static final int RESULT_LOAD_IMAGE = 1;
	private final int TAKE_PHOTO = 2;
	private final int GRAB_PHOTO = 3;
	private final int ADD_PHOTO = 4;
	
	private final int EDIT_INDEX = 0;
	private final int SAVE_INDEX = 1;
	private final int HELP_INDEX = 2;
	
	private LinearLayout tilesLayout;
	private LinearLayout decisionsLayout;
	private LinearLayout commentsLayout;
	private LinearLayout fightingLayout;
	
	private FightView fightView = new FightView();
	private TileView tileView;
	private DecisionView decisionView;
	private StoryController storyController; 
	private PageController pageController; 
    private ControllerApp app;
    private Menu menu;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_page_activity);
    }

	/**
	 * Called when the Activity resumes
	 */
	@Override
	public void onResume() {
        super.onResume();
        
        app = (ControllerApp) this.getApplication();
        tileView = new TileView(app, this);
        decisionView = new DecisionView(app, this);
        storyController = app.getStoryController();
        pageController = app.getPageController();
        
        fightingLayout = (LinearLayout) findViewById(R.id.fightingLayout);
        tilesLayout = (LinearLayout) findViewById(R.id.tilesLayout);
        decisionsLayout = (LinearLayout) findViewById(R.id.decisionsLayout);
        commentsLayout = (LinearLayout) findViewById(R.id.commentsLayout);
        
        pageController.setActivity(this);
        
        
		
		
        update(pageController.getPage());
        
        /* Set up onClick listeners for buttons on screen, even if some aren't
         * shown at the time.
         */
        
        
		Button addTileButton = (Button) findViewById(R.id.addTile);
		addTileButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				addTileMenu();
			}
		});
		
		Button addDecisionButton = (Button) findViewById(R.id.addDecision);
		addDecisionButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				pageController.addDecision();
			}
		});
		
        TextView addComment = (TextView) findViewById(R.id.addComment);
        addComment.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View view) {
        		onCallComment();
        		
        	}
        });
        
        TextView pageEnding = (TextView) findViewById(R.id.pageEnding);
        pageEnding.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View view) {
        		onEditPageEnding(view);
        	}
        });
	}
	
	@Override
	public void onPause() {
		super.onPause();
		pageController.deleteActivity();
	}
	
	/**
	 * Create an options menu.
	 * 
	 * @param menu The menu to create
	 * @return Success
	 */
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		super.onCreateOptionsMenu(menu);
        makeMenu(menu);
        return true;
    }
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		app = (ControllerApp) getApplication();
		changeActionBarButtons();
		return true;
	}
	
    /**
     * Puts button for changing to edit mode in the action bar.
     * @param menu The Menu to make
     */
	public void makeMenu(Menu menu) {
	
		MenuItem editPage = menu.add(0, EDIT_INDEX, EDIT_INDEX, getString(R.string.edit));
		MenuItem savePage = menu.add(0, SAVE_INDEX, SAVE_INDEX, getString(R.string.done));
		MenuItem help = menu.add(0, HELP_INDEX, HELP_INDEX, getString(R.string.help));

		editPage.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		savePage.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		help.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

	}
	
	/**
	 * Callback for clicking an item in the menu.
	 * 
	 * @param item The item that was clicked
	 * @return Success
	 */
    public boolean onOptionsItemSelected(MenuItem item) 
    {

    	try {
			return menuItemClicked(item);
		} catch (HandlerException e) {
			e.printStackTrace();
		}
    	return true;

    }
	
	/**
	 * Handles what to do when an item of the action bar is pressed.
	 * @param item The clicked item
	 * @return
	 */
	private boolean menuItemClicked(MenuItem item) throws HandlerException {
		switch (item.getItemId()) {
		case EDIT_INDEX:

			final String myId = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
			final String storyID = storyController.getStory().getAuthor();
			if(myId.equals(storyID)){
				app.setEditing(true);
				pageController.reloadPage();
				changeActionBarButtons();
				setButtonVisibility();
			}

			break;

		case SAVE_INDEX:

			app.setEditing(false);
			storyController.saveStory();
			pageController.reloadPage();
			changeActionBarButtons();
			setButtonVisibility();

			break;

		case HELP_INDEX:

			AlertDialog dialog = null;

	        if (app.getEditing())
	        	dialog = HelpDialogFactory.create(R.string.edit_page_help, this);
	        else
	        	dialog = HelpDialogFactory.create(R.string.read_page_help, this);
	        
	        dialog.show();
	        
			break;
		}
		return true;
	} 
	
	/**
	 * Sets which buttons are visible in the action bar.
	 */
	public void changeActionBarButtons() {
		MenuItem editButton = menu.findItem(EDIT_INDEX);
		MenuItem saveButton = menu.findItem(SAVE_INDEX);
		
		final String myId = Secure.getString(
				getBaseContext().getContentResolver(), Secure.ANDROID_ID);
		Story story = storyController.getStory();
		final String storyID = story.getAuthor();
		if(myId.equals(storyID)){
			if (app.getEditing()) {
				saveButton.setVisible(true);
				editButton.setVisible(false);
			} else {
				saveButton.setVisible(false);
				editButton.setVisible(true);
			}
		} else {
			saveButton.setVisible(false);
			editButton.setVisible(false);
		}
	}
	
	/**
	 * Show the dialog that allows users to pick which type of tile they would 
	 * like to add.
	 */
	public void addTileMenu(){		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final AlertDialog.Builder photoSelector = 
				new AlertDialog.Builder(this);
		final String[] titles = { getString(R.string.textTile), getString(R.string.photoTile),
				                   getString(R.string.videoTile), getString(R.string.audioTile), getString(R.string.cancel) };   
		final String[] titlesPhoto = { getString(R.string.fromFile), getString(R.string.takePhoto), getString(R.string.cancel) };
        builder.setItems(titles, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
            	switch(item){
            	case(0):
            		//TODO fix this to be MVC and observer pattern
            		TextTile tile = new TextTile();
					pageController.getPage().addTile(tile);
					tileView.addTile(pageController.getPage().getTiles().size() - 1, tile, tilesLayout);   				
            		break;
            	case(1):
            		photoSelector.setItems(titlesPhoto, 
            				new DialogInterface.OnClickListener() {
            			 public void onClick(DialogInterface dialog, 
            					              int item) {
            	            	switch(item){
	            	            	case(0):
	            	            		getPhoto();
	            	            		
	            	            		
	            	            		break;
	            	            	case(1):
	            	            		takePhoto();
	            	            		break;
            	            	}
            	                }});
            	       	photoSelector.show();
            		
            		break;
            		
            		
            	case(2):
            		break;
            	case(3):
            		break;
            	}
                    
                }});
        builder.show();
    }
	
	/**
	 * Updates a page to show any changes that have been made. These
	 * changes can also include whether the page is in view mode or
	 * edit mode.
	 * @param page The current page
	 */
	public void grabPhoto(){
		Intent i = new Intent(
        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, GRAB_PHOTO);
	}
	
	public void getPhoto(){
		Intent i = new Intent(
        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
	}
	public void update(Page page) {
		
		setButtonVisibility();
		
		if (storyController.getStory().isUsesCombat() == true) {
			fightView.updateFightView(fightingLayout, app);
		}
		
		if (pageController.haveTilesChanged()) {
			tileView.updateTiles(page, tilesLayout);
		}
		
		if (pageController.haveDecisionsChanged()) {
			decisionView.updateDecisions(page, decisionsLayout);
		}
		
		if (pageController.haveCommentsChanged()) {
			updateComments(page);
		}
		
		if (pageController.hasEndingChanged()) {
			updateEnding(page);
		}
		
		pageController.finishedUpdating();
	}
	
	

	/**
	 * Handles removing or showing the proper buttons in both the action bar
	 * and the in the page.
	 */
	private void setButtonVisibility() {
		Button addTileButton = (Button) findViewById(R.id.addTile);
		Button addDecisionButton = (Button) findViewById(R.id.addDecision);
		
		final String myId = Secure.getString(
				getBaseContext().getContentResolver(), Secure.ANDROID_ID);
		final String storyID = storyController.getStory().getAuthor();
		if(myId.equals(storyID)){
		
			int visibility = 0;
		
			if (app.getEditing()) {
				visibility = View.VISIBLE;
			} else {
				visibility = View.GONE;
			}
				
			addTileButton.setVisibility(visibility);
			addDecisionButton.setVisibility(visibility);
		} else {
			addTileButton.setVisibility(View.GONE);
			addDecisionButton.setVisibility(View.GONE);
		}
	}
	
	
	
	
	
	

	/**
	 * Removes the comments from commentsLayout and repopulates it with the
	 * current comments.
	 * @param page
	 */
	private void updateComments(Page page) {
		commentsLayout.removeAllViews();
		
		//For each comment in the page, add it to commentsLayout
		ArrayList<Comment> comments = page.getComments();
		for (int i = 0; i < comments.size(); i++) {
			addComment(comments.get(i));
		}
	}
	
	/**
	 * Updates the pageEnding from the passed page object.
	 * @param page
	 */
	private void updateEnding(Page page) {
		TextView pageEnding = (TextView) findViewById(R.id.pageEnding);
		pageEnding.setText(page.getPageEnding());
	}
		
	
	
	
	
	/**
	 * Brings up a menu with options of what to do to the decision.
	 * @param view
	 */
	public void editTileMenu(final View view){
		final String[] titles = { getString(R.string.edit), getString(R.string.delete) };
		
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.story_options);
        builder.setItems(titles, new DialogInterface.OnClickListener() {
        	
            public void onClick(DialogInterface dialog, int item) {
            	int whichTile = tilesLayout.indexOfChild(view);
            	switch(item){
            	case(0):
            		onEditTile(view);
            		break;
            	case(1):
            		pageController.deleteTile(whichTile);
            		break;
            	}
            }
            
        });
        builder.show();
    }
	
	private void takePhoto() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, TAKE_PHOTO);
	}
	
	private void addPhoto() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(intent, ADD_PHOTO);
	}
	
	/**
	 * Displays a dialog for editing a tile.
	 * @param view
	 */
	private void onEditTile(View view) {
		final TextView textView = (TextView) view;
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	final EditText alertEdit = new EditText(this);
    	alertEdit.setText(textView.getText().toString());
    	builder.setView(alertEdit);
    	builder.setPositiveButton(getString(R.string.done), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	int whichTile = tilesLayout.indexOfChild(textView);
            	pageController.updateTile(alertEdit.getText().toString(), whichTile);
            }
        })
        .setNegativeButton(getString(R.string.done), null);

        builder.show();
	}
	
	
	
	/**
	 * Brings up a menu with options of what to do to the decision.
	 * @param view
	 */
	public void decisionMenu(final View view){
		final String[] titles;
		final String[] titlesBasic = { getString(R.string.editProperties), getString(R.string.delete), getString(R.string.cancel) };
		final String[] titlesCounter = { getString(R.string.editProperties), getString(R.string.delete),
				                          getString(R.string.transitionMessages), getString(R.string.cancel) };
		final String[] titlesFight = { getString(R.string.editProperties), getString(R.string.delete), getString(R.string.transitionMessages),
				                        getString(R.string.setConditionals), getString(R.string.cancel) };
		final boolean fighting = pageController.getPage().isFightingFrag();
		final boolean combat = storyController.getStory().isUsesCombat();
		if(fighting == true){
			titles = titlesFight;
		}
		else if(combat == true){
			titles = titlesCounter;
		}
		else{
			titles = titlesBasic;
		}
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.story_options);
        builder.setItems(titles, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
            	int whichDecision = decisionsLayout.indexOfChild(view);
            	switch(item){
            	case(0):
            		onEditDecision(view);
            		break;
            	case(1):
            		pageController.deleteDecision(whichDecision);
            		break;
            	case(2):
            		if(combat == true){
            			onEditMessages(view);
            		}
            		break;
            	case(3):
            		if(fighting == true){
            			onEditConditionals(view);
            		}
            		break;
            	}
            }
        });
        builder.show();
    }
	
	protected void onEditMessages(View view) {

		final int whichDecision = decisionsLayout.indexOfChild(view);
		final Decision decision = pageController.findDecisionByIndex(whichDecision);
		ArrayList<Page> pages = storyController.getPages();
		int toPagePosition = pageController.findArrayPosition(decision, pages);
		
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(getString(R.string.counterMessage));
    	
    	final LinearLayout layout = (LinearLayout) View.inflate(this, R.layout.edit_messages_dialog, null);
    	final EditText decisionTitle = (EditText) layout.findViewById(R.id.edit_messages_dialog_decision_edittext);
    	final EditText dMessage = (EditText) layout.findViewById(R.id.edit_messages_dialog_takeDamage_edittext);
    	final EditText hMessage = (EditText) layout.findViewById(R.id.edit_messages_dialog_giveDamage_edittext);
    	final EditText tMessage = (EditText) layout.findViewById(R.id.edit_messages_dialog_coin_edittext);
    	final Spinner pageSpinner = (Spinner) layout.findViewById(R.id.edit_messages_dialog_page_spinner);

    	ArrayList<String> pageStrings = app.getPageStrings(pages);
    	ArrayAdapter<String> pagesAdapter = new ArrayAdapter<String>(this, R.layout.list_item_base, pageStrings);
    	pageSpinner.setAdapter(pagesAdapter);
    	pageSpinner.setSelection(toPagePosition);
    	
    	decisionTitle.setText(decision.getText());
    	dMessage.setText("" + decision.getChoiceModifiers().getDamageMessage());
    	hMessage.setText("" + decision.getChoiceModifiers().getHitMessage());
    	tMessage.setText("" + decision.getChoiceModifiers().getTreasureMessage());
    	
    	builder.setView(layout);
    	builder.setPositiveButton(getString(R.string.done), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
        		Counters counter = decision.getChoiceModifiers();
        		counter.setMessages(dMessage.getText().toString(), tMessage.getText().toString(), hMessage.getText().toString());
        		app.updateDecisionFight(decisionTitle.getText().toString(), pageSpinner.getSelectedItemPosition(),whichDecision, counter);
            }
        })
        .setNegativeButton(getString(R.string.cancel), null);

        builder.show();
	}

	protected void onEditConditionals(View view) {
		final int whichDecision = decisionsLayout.indexOfChild(view);
		final Decision decision = pageController.findDecisionByIndex(whichDecision);
		ArrayList<Page> pages = storyController.getPages();
		int toPagePosition = pageController.findArrayPosition(decision, pages);
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(getString(R.string.setDecisionConditions));
    	
    	final LinearLayout layout = (LinearLayout) View.inflate(this, R.layout.edit_conditionals_dialog, null);
    	final EditText decisionText = (EditText) layout.findViewById(R.id.edit_conditionals_dialog_decision_edittext);
    	final EditText conditionText = (EditText) layout.findViewById(R.id.edit_conditionals_dialog_threshold_edittext);
    	final Spinner pageSpinner = (Spinner) layout.findViewById(R.id.edit_conditionals_dialog_page_spinner);
    	final Spinner condSpinner = (Spinner) layout.findViewById(R.id.edit_conditionals_dialog_type_spinner);
    	final Spinner signSpinner = (Spinner) layout.findViewById(R.id.edit_conditionals_dialog_op_spinner);
    	
    	conditionText.setText("" + decision.getChoiceModifiers().getThresholdValue());
    	decisionText.setText(decision.getText());
    	
    	ArrayList<String> pageStrings = app.getPageStrings(pages);
    	ArrayAdapter<String> pagesAdapter = new ArrayAdapter<String>(this, R.layout.list_item_base, pageStrings);
    	pageSpinner.setAdapter(pagesAdapter);
    	pageSpinner.setSelection(toPagePosition);
    	
    	
    	condSpinner.setSelection(decision.getChoiceModifiers().getThresholdType());
    	
    	
    	signSpinner.setSelection(decision.getChoiceModifiers().getThresholdSign());
    	
    	builder.setView(layout);
    	builder.setPositiveButton(getString(R.string.done), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
        		Counters counter = decision.getChoiceModifiers();
        		counter.setThresholds(signSpinner.getSelectedItemPosition(), condSpinner.getSelectedItemPosition(), conditionText.getText().toString());
        		app.updateDecisionFight(decisionText.getText().toString(), pageSpinner.getSelectedItemPosition(), whichDecision, counter);
            }
        })
        .setNegativeButton(getString(R.string.cancel), null);

        builder.show();
	}

	/**
	 * Changes the view so that the next page is showing.
	 * @param view
	 */
	protected void decisionClicked(View view) {
		Story story = storyController.getStory();
		Page page = pageController.getPage();
		int whichDecision = decisionsLayout.indexOfChild(view);
		if(story.isUsesCombat() == true){
			Decision decision = page.getDecisions().get(whichDecision);
			if(page.isFightingFrag() == true){
				story.getPlayerStats().invokeUpdateComplex(decision.getChoiceModifiers());
			}
			else{
				story.getPlayerStats().invokeUpdateSimple(decision.getChoiceModifiers());
			}
			
		}
		app.followDecision(whichDecision);

	}
	
	/**
	 * Brings up a dialog for editing the decision clicked.
	 * @param view
	 */
	private void onEditDecision(View view) {
		int whichDecision = decisionsLayout.indexOfChild(view);
		final Decision decision = pageController.getPage().getDecisions().get(whichDecision);
		final Story story = storyController.getStory();
		final Page page = pageController.getPage();
		UUID toPageId = decision.getPageID();
		ArrayList<Page> pages = story.getPages();
		int toPagePosition = -1;
		for (int i = 0; i < pages.size(); i++) {

			UUID comparePage = pages.get(i).getId();
			System.out.println("toPageID: " + toPageId + "\ncomparePage: " + comparePage + "\nPage: " + page + "\nDecision: " + decision.getPageID() + decision.getText());
			if (toPageId.equals(comparePage)) {
				toPagePosition = i;
				break;
			}
		}
		
		final TextView decisionView = (TextView) view;
		
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(getString(R.string.setTextandPage));
    	
    	final LinearLayout layout = (LinearLayout) View.inflate(this, R.layout.edit_decision_dialog, null);
    	final LinearLayout combatOptions = (LinearLayout) layout.findViewById(R.id.edit_decision_dialog_page_combatoptions);
    	final LinearLayout fightOptions = (LinearLayout) layout.findViewById(R.id.edit_decision_dialog_page_fightoptions);
    	final Spinner pageSpinner = (Spinner) layout.findViewById(R.id.edit_decision_dialog_page_spinner);
    	final EditText decisionText = (EditText) layout.findViewById(R.id.edit_decision_dialog_decision_edittext);
    	final EditText alertTreasure = (EditText) layout.findViewById(R.id.edit_decision_dialog_coin_edittext);
    	final EditText playerDamage = (EditText) layout.findViewById(R.id.edit_decision_dialog_playerDamage_edittext); 
    	final EditText enemyDamage = (EditText) layout.findViewById(R.id.edit_decision_dialog_enemyDamage_edittext); 
    	final SeekBar seekPlayer = (SeekBar) layout.findViewById(R.id.edit_decision_dialog_playerPerc); 
    	final SeekBar seekEnemy = (SeekBar) layout.findViewById(R.id.edit_decision_dialog_enemyPerc); 
    	decisionText.setText(decision.getText());
    	
    	ArrayList<String> pageStrings = app.getPageStrings(pages);
    	ArrayAdapter<String> pagesAdapter = new ArrayAdapter<String>(this, R.layout.list_item_base, pageStrings);

    	if(page.getDecisions().size() > 2){
    		pageStrings.add(getString(R.string.randomChoice));
    	}

    	pageSpinner.setAdapter(pagesAdapter);
    	pageSpinner.setSelection(toPagePosition);
    	
    	if(!story.isUsesCombat()) {
			combatOptions.setVisibility(View.GONE);
    	}
    	else {

        	alertTreasure.setText("" + decision.getChoiceModifiers().getTreasureStat());
        	playerDamage.setText("" + decision.getChoiceModifiers().getPlayerHpStat());

        	if(!page.isFightingFrag()) {
        		fightOptions.setVisibility(View.GONE);
        	}
        	else {
        		
            	seekPlayer.setProgress(decision.getChoiceModifiers().getPlayerHitPercent());
            	enemyDamage.setText("" + decision.getChoiceModifiers().getEnemyHpStat());
            	seekEnemy.setProgress(decision.getChoiceModifiers().getEnemyHitPercent());
        	}
    	}

    	builder.setView(layout);
    	builder.setPositiveButton(getString(R.string.done), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	Counters counter = decision.getChoiceModifiers();
        		int decisionNumber = decisionsLayout.indexOfChild(decisionView);
        		if(story.isUsesCombat() == true){
        			String treasure = alertTreasure.getText().toString();
        			String hp = playerDamage.getText().toString();
        			if(page.isFightingFrag() == false){      				
        				counter.setBasic(treasure, hp);
	        			app.updateDecisionFight(decisionText.getText().toString(), 
	                			pageSpinner.getSelectedItemPosition(), decisionNumber, counter);
        			}
	        		else{
	        			String ehp = enemyDamage.getText().toString();
	        			String hitP = "" + seekPlayer.getProgress();
	        			String hitE = "" + seekEnemy.getProgress();
	        			
	        			counter.setStats(treasure, hp, ehp, hitE, hitP);
	        			app.updateDecisionFight(decisionText.getText().toString(), 
	                			pageSpinner.getSelectedItemPosition(), decisionNumber, counter);
	        		}     			
        		}
        		else{
        		
            	app.updateDecision(decisionText.getText().toString(), 
            			pageSpinner.getSelectedItemPosition(), decisionNumber);
        		}
            }
        })
        .setNegativeButton(getString(R.string.cancel), null);

        builder.show();
	}
	
	/**
	 * Called to display a new comment at position i.
	 * @param comment
	 */
	public void addComment(Comment comment) {
		final LinearLayout layout = new LinearLayout(this);
    	layout.setOrientation(LinearLayout.VERTICAL);
    	
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(0, 5, 0, 0);
		TextView view = new TextView(this);
		view.setBackgroundColor(0xFFFFFFFF);
		view.setPadding(10, 5, 10, 5);
		view.setLayoutParams(lp);
		view.setText(comment.getTimestamp() + " - '" + comment.getText() + "'");
		layout.addView(view);
		
		if(comment.getAnnotation() != null){
			ImageView imageView = new ImageView(this);
			imageView.setImageBitmap(comment.getAnnotation().getImage());
			imageView.setBackgroundColor(0xFFFFFFFF);
			layout.addView(imageView);
		}
	    commentsLayout.addView(layout);
	}
	
	/**
	 * Called when the add comment button is clicked. It creates a dialog that
	 * allows the user to input text and then save the comment.
	 * @param view
	 */
	private void onCallComment(){
		final String[] titlesPhoto = { getString(R.string.noImage), getString(R.string.fromFile),
				                       getString(R.string.takePhoto) };
		final AlertDialog.Builder photoSelector = 
				new AlertDialog.Builder(this); 
		photoSelector.setTitle(getString(R.string.usePhotoComment));
		photoSelector.setItems(titlesPhoto, 
				new DialogInterface.OnClickListener() {
			 public void onClick(DialogInterface dialog, 
					              int item) {
	            	switch(item){
    	            	
    	            	
    	            	case(0):
    	            		onEditComment();
    	            		break;
    	            	case(1):
    	            		grabPhoto();	            		
    	            		break;
    	            	case(2):
    	            		addPhoto();            		
    	            		break;
	            	}
	            	
	                }
			 }
		
		);
		photoSelector.show();
	      
	}
	private void onEditComment() {

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(getString(R.string.whatToSay));
    	
    	final LinearLayout layout = new LinearLayout(this);
    	layout.setOrientation(LinearLayout.VERTICAL);
    	
    	final EditText alertEdit = new EditText(this);
    	layout.addView(alertEdit);
    	
    	final ImageView alertImage = new ImageView(this);
    	
    	final PhotoTile photoAdd = (PhotoTile) app.getTempSpace();
		app.setTempSpace(null);

		if(photoAdd != null)
			alertImage.setImageBitmap(photoAdd.getImage());

    	layout.addView(alertImage);
    	
		builder.setView(layout);
    	builder.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            	app.addComment(alertEdit.getText().toString(),photoAdd );
            }
        })
        .setNegativeButton(getString(R.string.cancel), null);
        builder.show();
	}
	
	/**
	 * Opens a dialog that allows the user to edit the pageEnding.
	 * @param view
	 */
	private void onEditPageEnding(View view) {
		if (app.getEditing()) {
			TextView textView = (TextView) view;
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	final EditText alertEdit = new EditText(this);
	    	alertEdit.setText(textView.getText().toString());
	    	builder.setView(alertEdit);
	    	builder.setPositiveButton(getString(R.string.done), new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	            	pageController.setEnding(alertEdit.getText().toString());
	            }
	        })
	        .setNegativeButton(getString(R.string.cancel), null);
	        builder.show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		AlertDialog.Builder successChecker = new AlertDialog.Builder(this);
		if (resultCode == RESULT_OK && null != data) {
			switch(requestCode) {
			case (RESULT_LOAD_IMAGE):
				pageController.addTile(loadImage(data));
				break;
			case (GRAB_PHOTO):
				app.setTempSpace(loadImage(data));
			onEditComment();
				break;
			case(TAKE_PHOTO):
				final Bitmap image = retrievePhoto(data);
				successChecker.setView(makeViewByPhoto(image));
				successChecker.setTitle(getString(R.string.retakeQuestion));
				successChecker.setPositiveButton(getString(R.string.save),
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						PhotoTile tile = new PhotoTile();
						tile.setContent(image);
						pageController.addTile(tile);
					}
				})
				.setNegativeButton(getString(R.string.retake), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						takePhoto();
					}
				});
				successChecker.show();
				break;
			case(ADD_PHOTO):
				final Bitmap image2 = retrievePhoto(data);
				successChecker.setView(makeViewByPhoto(image2));
				successChecker.setTitle(getString(R.string.retakeQuestion));
				successChecker.setPositiveButton(getString(R.string.save),
					new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						PhotoTile tile = new PhotoTile();
						tile.setContent(image2);
						app.setTempSpace(tile);
						onEditComment();
					}
				})
				.setNegativeButton(getString(R.string.retake), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						addPhoto();
					}
				});
				successChecker.show();
				break;
		}}
	}
	
	public PhotoTile loadImage(Intent data){
		Uri selectedImage = data.getData();
		String[] filePathColumn = { MediaStore.Images.Media.DATA };

		Cursor cursor = getContentResolver().query(selectedImage,
				filePathColumn, null, null, null);
		cursor.moveToFirst();

		int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		String picturePath = cursor.getString(columnIndex);
		cursor.close();       	
		Bitmap pickedPhoto = BitmapFactory.decodeFile(picturePath);
		PhotoTile newPhoto = new PhotoTile();
		newPhoto.setImageFile(pickedPhoto);	
		return newPhoto;
	}
	
	public Bitmap retrievePhoto(Intent data){
		Bundle bundle = data.getExtras();
		return  (Bitmap) bundle.get("data");	
	}
	public ImageView makeViewByPhoto(Bitmap image){
		ImageView pictureTaken = new ImageView(this);
		pictureTaken.setImageBitmap(image);
		return pictureTaken;
	}
	
}
