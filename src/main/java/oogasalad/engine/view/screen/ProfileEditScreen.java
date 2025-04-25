package oogasalad.engine.view.screen;

import java.io.File;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import oogasalad.ResourceManager;
import oogasalad.ResourceManagerAPI;
import oogasalad.engine.model.object.ImmutableGameObject;
import oogasalad.engine.view.Display;
import oogasalad.engine.view.ViewState;
import oogasalad.engine.view.factory.ButtonActionFactory;
import oogasalad.userData.UserDataApiDefault;
import oogasalad.userData.records.UserData;

/**
 * Screen for editing user profile information.
 * Allows users to modify their display name, email, bio, password and avatar.
 */
public class ProfileEditScreen extends Display {

  private static final Logger LOG = LogManager.getLogger();
  private static final ResourceManagerAPI resourceManager = ResourceManager.getInstance();

  private final ViewState viewState;
  private final String stylesheet;
  private final ButtonActionFactory actionFactory;
  private final UserDataApiDefault userDataApi;
  private final UserData originalUserData;
  
  private TextField displayNameField;
  private TextField emailField;
  private TextArea bioField;
  private PasswordField passwordField;
  private PasswordField confirmPasswordField;
  private ImageView avatarView;
  private File selectedAvatarFile;
  private Label statusMessage;

  public ProfileEditScreen(ViewState viewState, UserData userData) {
    this.viewState = viewState;
    this.originalUserData = userData;
    this.stylesheet = Objects.requireNonNull(
        getClass().getResource(resourceManager.getConfig("engine/view/profileEditScreen", "profileEdit.stylesheet"))).toExternalForm();
    this.actionFactory = new ButtonActionFactory(viewState);
    this.userDataApi = new UserDataApiDefault();
    this.selectedAvatarFile = userData.userImage();
    
    initializeProfileEditScreen();
  }

  private void initializeProfileEditScreen() {
    // Root pane
    BorderPane root = new BorderPane();
    root.setPadding(new Insets(Double.parseDouble(resourceManager.getConfig("engine/view/profileEditScreen", "profileEdit.root.padding"))));
    root.getStyleClass().add(resourceManager.getConfig("engine/view/profileEditScreen", "profileEdit.root.style"));

    // Top: Back button and title
    Button backButton = new Button(resourceManager.getConfig("engine/view/profileEditScreen", "profileEdit.back.button.text"));
    backButton.setOnAction(e -> returnToProfile());
    
    Label titleLabel = new Label(resourceManager.getConfig("engine/view/profileEditScreen", "profileEdit.title.text"));
    titleLabel.getStyleClass().add(resourceManager.getConfig("engine/view/profileEditScreen", "profileEdit.title.style"));
    
    HBox topBar = new HBox(20, backButton, titleLabel);
    topBar.setAlignment(Pos.CENTER_LEFT);
    topBar.setPadding(new Insets(0, 0, 20, 0));
    root.setTop(topBar);

    // Center: Profile edit form
    VBox editForm = createProfileEditForm();
    
    // Bottom: Status message
    statusMessage = new Label();
    statusMessage.getStyleClass().add(resourceManager.getConfig("engine/view/profileEditScreen", "profileEdit.status.message.style"));
    
    VBox centerBox = new VBox(20, editForm, statusMessage);
    centerBox.setAlignment(Pos.TOP_CENTER);
    root.setCenter(centerBox);

    // Attach
    this.getChildren().add(root);
    this.getStylesheets().add(stylesheet);
  }
  
  private VBox createProfileEditForm() {
    VBox formContainer = new VBox(30);
    formContainer.setAlignment(Pos.TOP_CENTER);
    formContainer.setPadding(new Insets(10));
    
    // Avatar section
    VBox avatarSection = new VBox(10);
    avatarSection.setAlignment(Pos.CENTER);
    
    // Avatar preview
    avatarView = new ImageView();
    avatarView.setFitWidth(150);
    avatarView.setFitHeight(150);
    avatarView.setPreserveRatio(true);
    
    if (originalUserData.userImage() != null && originalUserData.userImage().exists()) {
      try {
        Image avatarImage = new Image(originalUserData.userImage().toURI().toString());
        avatarView.setImage(avatarImage);
      } catch (Exception e) {
        LOG.warn("Could not load avatar image", e);
      }
    }
    
    Button changeAvatarButton = new Button(resourceManager.getConfig("engine/view/profileEditScreen", "profileEdit.avatar.button.text"));
    changeAvatarButton.setOnAction(e -> chooseNewAvatar());
    
    avatarSection.getChildren().addAll(avatarView, changeAvatarButton);
    
    // User information form
    GridPane infoGrid = new GridPane();
    infoGrid.setVgap(15);
    infoGrid.setHgap(20);
    infoGrid.setAlignment(Pos.CENTER);
    
    // Display name
    Label displayNameLabel = new Label(resourceManager.getConfig("engine/view/profileEditScreen", "profileEdit.displayName.label"));
    displayNameField = new TextField(originalUserData.displayName());
    displayNameField.setPromptText(resourceManager.getConfig("engine/view/profileEditScreen", "profileEdit.displayName.prompt"));
    infoGrid.add(displayNameLabel, 0, 0);
    infoGrid.add(displayNameField, 1, 0);
    
    // Email
    Label emailLabel = new Label(resourceManager.getConfig("engine/view/profileEditScreen", "profileEdit.email.label"));
    emailField = new TextField(originalUserData.email());
    emailField.setPromptText(resourceManager.getConfig("engine/view/profileEditScreen", "profileEdit.email.prompt"));
    infoGrid.add(emailLabel, 0, 1);
    infoGrid.add(emailField, 1, 1);
    
    // Bio
    Label bioLabel = new Label(resourceManager.getConfig("engine/view/profileEditScreen", "profileEdit.bio.label"));
    bioField = new TextArea(originalUserData.bio());
    bioField.setPromptText(resourceManager.getConfig("engine/view/profileEditScreen", "profileEdit.bio.prompt"));
    bioField.setPrefRowCount(4);
    bioField.setWrapText(true);
    infoGrid.add(bioLabel, 0, 2);
    infoGrid.add(bioField, 1, 2);
    
    // New password (optional)
    Label passwordLabel = new Label(resourceManager.getConfig("engine/view/profileEditScreen", "profileEdit.password.label"));
    passwordField = new PasswordField();
    passwordField.setPromptText(resourceManager.getConfig("engine/view/profileEditScreen", "profileEdit.password.prompt"));
    infoGrid.add(passwordLabel, 0, 3);
    infoGrid.add(passwordField, 1, 3);
    
    // Confirm password
    Label confirmPasswordLabel = new Label(resourceManager.getConfig("engine/view/profileEditScreen", "profileEdit.confirmPassword.label"));
    confirmPasswordField = new PasswordField();
    confirmPasswordField.setPromptText(resourceManager.getConfig("engine/view/profileEditScreen", "profileEdit.confirmPassword.prompt"));
    infoGrid.add(confirmPasswordLabel, 0, 4);
    infoGrid.add(confirmPasswordField, 1, 4);
    
    // Save button
    Button saveButton = new Button(resourceManager.getConfig("engine/view/profileEditScreen", "profileEdit.save.button.text"));
    saveButton.getStyleClass().add(resourceManager.getConfig("engine/view/profileEditScreen", "profileEdit.save.button.style"));
    saveButton.setOnAction(e -> saveChanges());
    
    formContainer.getChildren().addAll(avatarSection, infoGrid, saveButton);
    return formContainer;
  }
  
  private void chooseNewAvatar() {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Select Avatar Image");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
    );
    
    // Use actionFactory to handle the file chooser dialog
    File selectedFile = actionFactory.openFileChooser(fileChooser);
    if (selectedFile != null) {
      try {
        // Update the preview
        Image image = new Image(selectedFile.toURI().toString());
        avatarView.setImage(image);
        
        // Save the reference for later
        selectedAvatarFile = selectedFile;
      } catch (Exception e) {
        LOG.error("Failed to load selected avatar", e);
        showError("Failed to load the selected image");
      }
    }
  }
  
  private void saveChanges() {
    // Validate inputs
    String displayName = displayNameField.getText().trim();
    String email = emailField.getText().trim();
    String bio = bioField.getText().trim();
    String password = passwordField.getText();
    String confirmPassword = confirmPasswordField.getText();
    
    if (displayName.isEmpty() || email.isEmpty()) {
      showError("Display name and email are required");
      return;
    }
    
    // Check if password is being changed
    String finalPassword = originalUserData.password();
    if (!password.isEmpty()) {
      if (!password.equals(confirmPassword)) {
        showError("Passwords do not match");
        return;
      }
      finalPassword = password;
    }
    
    try {
      // Copy the avatar file if needed
      File avatarFile = originalUserData.userImage();
      if (selectedAvatarFile != null && !selectedAvatarFile.equals(originalUserData.userImage())) {
        // Create a copy of the avatar in the user data directory
        File userDir = new File("data/userData");
        if (!userDir.exists()) {
          userDir.mkdirs();
        }
        
        avatarFile = new File(userDir, originalUserData.username() + "-avatar" + 
            getFileExtension(selectedAvatarFile.getName()));
        
        try {
          // Copy file
          java.nio.file.Files.copy(selectedAvatarFile.toPath(), avatarFile.toPath(), 
              java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
          LOG.error("Failed to copy avatar file", e);
          showError("Failed to save avatar image");
          return;
        }
      }
      
      // Create updated user data
      UserData updatedUserData = new UserData(
          originalUserData.username(),
          displayName,
          email,
          finalPassword,
          originalUserData.language(),
          bio,
          avatarFile,
          originalUserData.userAvatar(),
          originalUserData.userGameData()
      );
      
      // Save to file
      userDataApi.writeUserData(updatedUserData);
      
      showSuccess("Profile updated successfully!");
      
      // Return to profile view after a short delay
      new Thread(() -> {
        try {
          Thread.sleep(1000);
          javafx.application.Platform.runLater(this::returnToProfile);
        } catch (Exception e) {
          LOG.error("Error in delay thread", e);
        }
      }).start();
      
    } catch (Exception e) {
      LOG.error("Failed to save profile changes", e);
      showError("Error saving profile: " + e.getMessage());
    }
  }
  
  private void returnToProfile() {
    try {
      // Parse user data again to get updated version
      UserData refreshedData = userDataApi.parseUserData(
          originalUserData.username(), 
          passwordField.getText().isEmpty() ? originalUserData.password() : passwordField.getText()
      );
      
      // Return to profile screen
      actionFactory.navigateToUserProfile(refreshedData).run();
    } catch (Exception e) {
      LOG.error("Failed to return to profile", e);
    }
  }
  
  private String getFileExtension(String filename) {
    int i = filename.lastIndexOf('.');
    if (i > 0) {
      return filename.substring(i);
    }
    return ".png"; // Default extension
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
    // Not applicable for profile edit screen
  }

  @Override
  public void addGameObjectImage(ImmutableGameObject gameObject) {

  }

  @Override
  public void renderPlayerStats(ImmutableGameObject player) {
    // Not applicable for profile edit screen
  }
} 