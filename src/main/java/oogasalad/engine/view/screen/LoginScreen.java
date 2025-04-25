package oogasalad.engine.view.screen;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import oogasalad.ResourceManager;
import oogasalad.ResourceManagerAPI;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.Display;
import oogasalad.engine.view.ViewState;
import oogasalad.engine.view.factory.ButtonActionFactory;
import oogasalad.exceptions.UserDataParseException;
import oogasalad.userData.SessionManager;
import oogasalad.userData.UserDataApiDefault;
import oogasalad.userData.records.UserData;

public class LoginScreen extends Display {

  private static final Logger LOG = LogManager.getLogger();
  private static final ResourceManagerAPI resourceManager = ResourceManager.getInstance();

  private final ViewState viewState;
  private final String stylesheet;
  private final ButtonActionFactory actionFactory;
  private final UserDataApiDefault userDataApi;
  private final SessionManager sessionManager;
  private Label statusMessage;

  public LoginScreen(ViewState viewState) {
    this.viewState = viewState;
    this.stylesheet = Objects.requireNonNull(
        getClass().getResource(resourceManager.getConfig("engine/view/loginScreen", "login.stylesheet"))).toExternalForm();
    this.actionFactory = new ButtonActionFactory(viewState);
    this.userDataApi = new UserDataApiDefault();
    this.sessionManager = new SessionManager();
    
    // Ensure the user data directory exists
    try {
      File userDataDir = new File("data/userData");
      if (!userDataDir.exists()) {
        userDataDir.mkdirs();
      }
    } catch (Exception e) {
      LOG.error("Failed to create user data directory", e);
    }
    
    initializeLoginScreen();
  }

  private void initializeLoginScreen() {
    // Root pane
    BorderPane root = new BorderPane();
    root.setPadding(new Insets(Double.parseDouble(resourceManager.getConfig("engine/view/loginScreen", "login.root.padding"))));
    root.getStyleClass().add(resourceManager.getConfig("engine/view/loginScreen", "login.root.style"));

    // Top: Back button
    Button back = new Button(resourceManager.getConfig("engine/view/loginScreen", "login.back.button.text"));
    back.setId("backToSplash");
    back.setOnAction(e -> actionFactory.getAction("backToSplash").run());

    HBox topBar = new HBox(back);
    topBar.setAlignment(Pos.CENTER_LEFT);
    topBar.setPadding(new Insets(0, 0, 20, 0));
    root.setTop(topBar);

    // Center: Login/Signup tabs
    TabPane tabPane = new TabPane();
    tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    tabPane.getStyleClass().add(resourceManager.getConfig("engine/view/loginScreen", "login.tab.pane.style"));
    
    Tab loginTab = new Tab(resourceManager.getConfig("engine/view/loginScreen", "login.tab.text"));
    loginTab.setContent(createLoginForm());
    
    Tab signupTab = new Tab(resourceManager.getConfig("engine/view/loginScreen", "signup.tab.text"));
    signupTab.setContent(createSignupForm());
    
    tabPane.getTabs().addAll(loginTab, signupTab);
    
    // Status message area
    statusMessage = new Label();
    statusMessage.getStyleClass().add(resourceManager.getConfig("engine/view/loginScreen", "login.status.message.style"));
    
    VBox centerBox = new VBox(20, tabPane, statusMessage);
    centerBox.setAlignment(Pos.CENTER);
    root.setCenter(centerBox);

    // Attach
    this.getChildren().add(root);
    this.getStylesheets().add(stylesheet);
  }
  
  private VBox createLoginForm() {
    VBox loginBox = new VBox(15);
    loginBox.setAlignment(Pos.CENTER);
    loginBox.setPadding(new Insets(25));
    loginBox.getStyleClass().add(resourceManager.getConfig("engine/view/loginScreen", "login.form.style"));
    
    Label usernameLabel = new Label(resourceManager.getConfig("engine/view/loginScreen", "login.username.label"));
    TextField usernameField = new TextField();
    usernameField.setPromptText(resourceManager.getConfig("engine/view/loginScreen", "login.username.prompt"));
    
    Label passwordLabel = new Label(resourceManager.getConfig("engine/view/loginScreen", "login.password.label"));
    PasswordField passwordField = new PasswordField();
    passwordField.setPromptText(resourceManager.getConfig("engine/view/loginScreen", "login.password.prompt"));
    
    // Check if we have saved credentials and pre-fill the fields
    if (sessionManager.hasActiveSession()) {
      usernameField.setText(sessionManager.getSavedUsername());
      passwordField.setText(sessionManager.getSavedPassword());
    }
    
    // Add "Remember Me" checkbox
    CheckBox rememberMeCheckbox = new CheckBox(resourceManager.getConfig("engine/view/loginScreen", "login.remember.me.text"));
    rememberMeCheckbox.setSelected(sessionManager.hasActiveSession());
    
    Button loginButton = new Button(resourceManager.getConfig("engine/view/loginScreen", "login.button.text"));
    loginButton.getStyleClass().add(resourceManager.getConfig("engine/view/loginScreen", "login.button.style"));
    loginButton.setOnAction(e -> handleLogin(
        usernameField.getText(), 
        passwordField.getText(), 
        rememberMeCheckbox.isSelected()
    ));
    
    loginBox.getChildren().addAll(
        usernameLabel, usernameField, 
        passwordLabel, passwordField,
        rememberMeCheckbox,
        loginButton
    );
    
    return loginBox;
  }
  
  private VBox createSignupForm() {
    VBox signupBox = new VBox(15);
    signupBox.setAlignment(Pos.CENTER);
    signupBox.setPadding(new Insets(25));
    signupBox.getStyleClass().add(resourceManager.getConfig("engine/view/loginScreen", "signup.form.style"));
    
    Label usernameLabel = new Label(resourceManager.getConfig("engine/view/loginScreen", "signup.username.label"));
    TextField usernameField = new TextField();
    usernameField.setPromptText(resourceManager.getConfig("engine/view/loginScreen", "signup.username.prompt"));
    
    Label displayNameLabel = new Label(resourceManager.getConfig("engine/view/loginScreen", "signup.displayName.label"));
    TextField displayNameField = new TextField();
    displayNameField.setPromptText(resourceManager.getConfig("engine/view/loginScreen", "signup.displayName.prompt"));
    
    Label emailLabel = new Label(resourceManager.getConfig("engine/view/loginScreen", "signup.email.label"));
    TextField emailField = new TextField();
    emailField.setPromptText(resourceManager.getConfig("engine/view/loginScreen", "signup.email.prompt"));
    
    Label passwordLabel = new Label(resourceManager.getConfig("engine/view/loginScreen", "signup.password.label"));
    PasswordField passwordField = new PasswordField();
    passwordField.setPromptText(resourceManager.getConfig("engine/view/loginScreen", "signup.password.prompt"));
    
    Label confirmPasswordLabel = new Label(resourceManager.getConfig("engine/view/loginScreen", "signup.confirmPassword.label"));
    PasswordField confirmPasswordField = new PasswordField();
    confirmPasswordField.setPromptText(resourceManager.getConfig("engine/view/loginScreen", "signup.confirmPassword.prompt"));
    
    Button signupButton = new Button(resourceManager.getConfig("engine/view/loginScreen", "signup.button.text"));
    signupButton.getStyleClass().add(resourceManager.getConfig("engine/view/loginScreen", "signup.button.style"));
    signupButton.setOnAction(e -> handleSignup(
        usernameField.getText(),
        displayNameField.getText(),
        emailField.getText(),
        passwordField.getText(),
        confirmPasswordField.getText()
    ));
    
    signupBox.getChildren().addAll(
        usernameLabel, usernameField,
        displayNameLabel, displayNameField,
        emailLabel, emailField,
        passwordLabel, passwordField,
        confirmPasswordLabel, confirmPasswordField,
        signupButton
    );
    
    return signupBox;
  }
  
  private void handleLogin(String username, String password, boolean rememberMe) {
    if (username.isEmpty() || password.isEmpty()) {
      showError("Username and password cannot be empty");
      return;
    }
    
    try {
      UserData userData = userDataApi.parseUserData(username, password);
      actionFactory.navigateToUserProfile(userData).run();
      if (rememberMe) {
        sessionManager.saveSession(username, password);
      }
    } catch (UserDataParseException e) {
      showError("Invalid username or password");
    } catch (Exception e) {
      LOG.error("Login error", e);
      showError("Error logging in: " + e.getMessage());
    }
  }
  
  private void handleSignup(String username, String displayName, String email, 
                            String password, String confirmPassword) {
    // Validate inputs
    if (username.isEmpty() || displayName.isEmpty() || email.isEmpty() || 
        password.isEmpty() || confirmPassword.isEmpty()) {
      showError("All fields are required");
      return;
    }
    
    if (!password.equals(confirmPassword)) {
      showError("Passwords do not match");
      return;
    }
    
    // Check if username already exists
    File userFile = new File("data/userData/" + username + ".xml");
    if (userFile.exists()) {
      showError("Username already exists");
      return;
    }
    
    try {
      // Create default user image file in the userData directory
      File defaultImage = new File("data/userData/" + username + "-avatar.txt");
      
      // Create the parent directory if it doesn't exist
      File parentDir = defaultImage.getParentFile();
      if (!parentDir.exists()) {
        parentDir.mkdirs();
      }
      
      // Create a placeholder file for the avatar
      if (!defaultImage.exists()) {
        try {
          defaultImage.createNewFile();
          // Write a placeholder message
          java.io.FileWriter writer = new java.io.FileWriter(defaultImage);
          writer.write("Avatar placeholder for user: " + username);
          writer.close();
          
          LOG.info("Created default avatar for user: " + username);
        } catch (Exception e) {
          LOG.warn("Could not create default avatar file", e);
        }
      }
      
      // Create new user data
      UserData newUser = new UserData(
          username,
          displayName,
          email,
          password,
          "en", // Default language
          "New user", // Default bio
          defaultImage,
          null, // No avatar sprite
          new ArrayList<>() // Empty game data list
      );
      
      // Write to file
      userDataApi.writeNewUserData(newUser);
      
      // Show success and switch to profile screen
      showSuccess("Account created successfully!");
      
      // Login with the new account (just use the newly created user data directly)
      actionFactory.navigateToUserProfile(newUser).run();
      
    } catch (Exception e) {
      LOG.error("Error creating user", e);
      showError("Error creating account: " + e.getMessage());
    }
  }
  
  private void showError(String message) {
    statusMessage.setText(message);
    statusMessage.setTextFill(Color.RED);
  }
  
  private void showSuccess(String message) {
    statusMessage.setText(message);
    statusMessage.setTextFill(Color.GREEN);
  }

  @Override
  public void removeGameObjectImage(ImmutableGameObject gameObject) {
    // Not applicable for login screen
  }

  @Override
  public void addGameObjectImage(ImmutableGameObject gameObject) {

  }

  @Override
  public void renderPlayerStats(ImmutableGameObject player) {
    // Not applicable for login screen
  }
} 