package oogasalad.engine.view.screen;

import java.io.File;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import oogasalad.ResourceManager;
import oogasalad.ResourceManagerAPI;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.Display;
import oogasalad.engine.view.ViewState;
import oogasalad.engine.view.factory.ButtonActionFactory;
import oogasalad.userData.records.UserData;
import oogasalad.userData.records.UserGameData;
import oogasalad.userData.records.UserLevelData;

public class UserDataScreen extends Display {

  private static final Logger LOG = LogManager.getLogger();
  private static final ResourceManagerAPI resourceManager = ResourceManager.getInstance();

  private final UserData user;
  private final ViewState viewState;
  private final String stylesheet;
  private final ButtonActionFactory actionFactory;

  public UserDataScreen(ViewState viewState, UserData user) {
    this.user          = user;
    this.viewState     = viewState;
    this.stylesheet    = Objects.requireNonNull(
        getClass().getResource(resourceManager.getConfig("engine/view/userDataScene", "userData.stylesheet"))).toExternalForm();
    this.actionFactory = new ButtonActionFactory(viewState);
    initializeUserDataScreen();
  }

  private void initializeUserDataScreen() {
    // Root pane
    BorderPane root = new BorderPane();
    root.setPadding(new Insets(
        Double.parseDouble(resourceManager.getConfig("engine/view/userDataScene", "userData.root.padding"))
    ));
    root.getStyleClass().add(resourceManager.getConfig("engine/view/userDataScene", "userData.root.style"));

    // Top: Back button via ButtonActionFactory
    String backId = resourceManager.getConfig("engine/view/userDataScene", "userData.back.button.id");
    Button back = new Button(resourceManager.getConfig("engine/view/userDataScene", "userData.back.button.text"));
    back.setId(backId);
    back.setId("backToSplash");
    back.setOnAction(e -> actionFactory.getAction("backToSplash").run());

    HBox topBar = new HBox(back);
    topBar.setAlignment(Pos.CENTER_LEFT);
    topBar.setPadding(new Insets(0, 0, 10, 0));
    root.setTop(topBar);

    // Left: Profile image
    VBox leftBox = new VBox(15);
    leftBox.setAlignment(Pos.TOP_CENTER);
    leftBox.getStyleClass().add(resourceManager.getConfig("engine/view/userDataScene", "userData.left.box.style"));
    File imgFile = user.userImage();
    if (imgFile != null && imgFile.exists()) {
      ImageView iv = new ImageView(new Image(imgFile.toURI().toString()));
      iv.setFitWidth(150);
      iv.setFitHeight(150);
      iv.setPreserveRatio(true);
      leftBox.getChildren().add(iv);
    }
    
    // Add Edit Profile button
    Button editProfileButton = new Button("Edit Profile");
    editProfileButton.getStyleClass().add("edit-profile-button");
    editProfileButton.setOnAction(e -> navigateToProfileEditScreen());
    leftBox.getChildren().add(editProfileButton);
    
    // Add Logout button
    Button logoutButton = new Button("Logout");
    logoutButton.getStyleClass().add("logout-button");
    logoutButton.setOnAction(e -> actionFactory.getAction("logoutButton").run());
    leftBox.getChildren().add(logoutButton);

    // Right: Info & Stats
    VBox rightBox = new VBox(10);
    rightBox.setAlignment(Pos.TOP_LEFT);
    rightBox.getStyleClass().add(resourceManager.getConfig("engine/view/userDataScene", "userData.right.box.style"));

    // Basic Info grid
    GridPane infoGrid = new GridPane();
    infoGrid.setVgap(8);
    infoGrid.setHgap(10);
    infoGrid.getStyleClass().add(resourceManager.getConfig("engine/view/userDataScene", "userData.info.grid.style"));

    Label l1 = new Label("Username:");      
    l1.getStyleClass().add(resourceManager.getConfig("engine/view/userDataScene", "userData.label.style"));
    Label v1 = new Label(user.username());
    Label l2 = new Label("Display Name:");  
    l2.getStyleClass().add(resourceManager.getConfig("engine/view/userDataScene", "userData.label.style"));
    Label v2 = new Label(user.displayName());
    Label l3 = new Label("Email:");         
    l3.getStyleClass().add(resourceManager.getConfig("engine/view/userDataScene", "userData.label.style"));
    Label v3 = new Label(user.email());
    Label l4 = new Label("Language:");      
    l4.getStyleClass().add(resourceManager.getConfig("engine/view/userDataScene", "userData.label.style"));
    Label v4 = new Label(user.language());
    Label l5 = new Label("Bio:");           
    l5.getStyleClass().add(resourceManager.getConfig("engine/view/userDataScene", "userData.label.style"));
    Label v5 = new Label(user.bio());       
    v5.setWrapText(true);

    infoGrid.addRow(0, l1, v1);
    infoGrid.addRow(1, l2, v2);
    infoGrid.addRow(2, l3, v3);
    infoGrid.addRow(3, l4, v4);
    infoGrid.addRow(4, l5, v5);

    rightBox.getChildren().add(infoGrid);

    // Games Accordion
    Accordion gamesAcc = new Accordion();
    gamesAcc.getStyleClass().add(resourceManager.getConfig("engine/view/userDataScene", "userData.game.accordion.style"));
    for (UserGameData g : user.userGameData()) {
      VBox gameBox = new VBox(5);
      gameBox.setPadding(new Insets(10));

      // Highest stats
      GridPane highGrid = new GridPane();
      highGrid.setVgap(4);
      highGrid.setHgap(8);
      int row = 0;
      for (var entry : g.playerHighestGameStatMap().entrySet()) {
        highGrid.add(new Label(entry.getKey() + ":"), 0, row);
        highGrid.add(new Label(entry.getValue().toString()), 1, row++);
      }
      gameBox.getChildren().add(new Label("Highest Stats:"));
      gameBox.getChildren().add(highGrid);

      // Levels Accordion
      Accordion levelsAcc = new Accordion();
      levelsAcc.getStyleClass().add(resourceManager.getConfig("engine/view/userDataScene", "userData.level.accordion.style"));
      for (UserLevelData lvl : g.playerLevelStatMap().values()) {
        VBox lvlBox = new VBox(4);
        lvlBox.setPadding(new Insets(5));
        lvlBox.getChildren().add(new Label("Last Played: " + lvl.lastPlayed()));

        GridPane lvlGrid = new GridPane();
        lvlGrid.setVgap(3);
        lvlGrid.setHgap(6);
        int lrow = 0;
        for (var e2 : lvl.levelHighestStatMap().entrySet()) {
          lvlGrid.add(new Label(e2.getKey() + ":"), 0, lrow);
          lvlGrid.add(new Label(e2.getValue()), 1, lrow++);
        }
        lvlBox.getChildren().add(new Label("Level Stats:"));
        lvlBox.getChildren().add(lvlGrid);

        TitledPane lvlPane = new TitledPane(lvl.levelName(), lvlBox);
        levelsAcc.getPanes().add(lvlPane);
      }
      gameBox.getChildren().add(new Label("Levels:"));
      gameBox.getChildren().add(levelsAcc);

      TitledPane gamePane = new TitledPane(
          g.gameName() + " (Last Played: " + g.lastPlayed() + ")", gameBox);
      gamesAcc.getPanes().add(gamePane);
    }
    rightBox.getChildren().add(new Label("Games:"));
    rightBox.getChildren().add(gamesAcc);

    ScrollPane scroll = new ScrollPane(rightBox);
    scroll.setFitToWidth(true);

    // Centering both image & stats
    HBox centerBox = new HBox(30, leftBox, scroll);
    centerBox.setAlignment(Pos.CENTER);
    centerBox.getStyleClass().add(resourceManager.getConfig("engine/view/userDataScene", "userData.center.container.style"));
    root.setCenter(centerBox);

    // Attach
    this.getChildren().add(root);
    this.getStylesheets().add(stylesheet);
  }

  @Override public void removeGameObjectImage(ImmutableGameObject gameObject) { }

  @Override
  public void addGameObjectImage(ImmutableGameObject gameObject) {

  }

  @Override public void renderPlayerStats(ImmutableGameObject player)      { }
  
  /**
   * Navigates to the profile edit screen.
   */
  private void navigateToProfileEditScreen() {
    try {
      actionFactory.navigateToProfileEdit(user).run();
    } catch (Exception e) {
      LOG.error("Failed to navigate to profile edit screen", e);
    }
  }
}
