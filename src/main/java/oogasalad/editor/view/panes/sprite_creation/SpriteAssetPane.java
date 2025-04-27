package oogasalad.editor.view.panes.sprite_creation;

import java.util.stream.Collectors;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import oogasalad.editor.controller.EditorController;
import oogasalad.editor.model.data.SpriteSheetAtlas;
import oogasalad.editor.model.data.SpriteSheetLibrary;
import oogasalad.editor.model.data.SpriteTemplateMap;
import oogasalad.editor.model.data.object.sprite.AnimationData;
import oogasalad.editor.model.data.object.sprite.FrameData;
import oogasalad.editor.model.data.object.sprite.SpriteTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * UI that lives inside the “Sprites” tab. Implements the buttons for sprite‑sheet import and
 * sprite‑template creation/editing. Thumbnails use the absolute PNG path provided by
 * {@link SpriteSheetAtlas#getImageFile()} and the header buttons are made larger for easier clicks.
 */
public class SpriteAssetPane extends BorderPane {

  private static final Logger LOG = LogManager.getLogger(SpriteAssetPane.class);

  private static final double BUTTON_SPACING = 8;
  private static final double BUTTON_WIDTH   = 140;
  private static final double BUTTON_HEIGHT  = 32;
  private static final double ICON_SIZE      = 64;
  private static final double GAP           = 6;

  private final EditorController editorController;
  private final SpriteTemplateMap spriteTemplateMap;
  private final TilePane gallery = new TilePane();

  public SpriteAssetPane(EditorController editorController, Window ownerWindow) {
    this.editorController = editorController;
    this.spriteTemplateMap = editorController.getEditorDataAPI().getSpriteTemplateMap();

    // Header buttons
    Button importButton    = new Button("Import Sheet");
    Button newSpriteButton = new Button("New Sprite");

    importButton.getStyleClass().add("primary-button");
    newSpriteButton.getStyleClass().add("primary-button");
    importButton.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
    newSpriteButton.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);

    importButton.setOnAction(e ->
        new ProcessSpriteSheetComponent(editorController, ownerWindow).show()
    );

    newSpriteButton.setOnAction(e -> {
      SpriteSheetLibrary library = editorController.getEditorDataAPI().getSpriteLibrary();
      SpriteTemplateComponent dialog =
          new SpriteTemplateComponent(editorController, ownerWindow, library);
      dialog.showAndWait();
      SpriteTemplate result = dialog.getResult();
      if (result != null) {
        editorController.getEditorDataAPI().addSpriteTemplate(result);
        LOG.info("Added new sprite template: {}", result.getName());
        refreshGallery();
      }
    });

    HBox header = new HBox(BUTTON_SPACING, importButton, newSpriteButton);
    header.setPadding(new Insets(10));
    header.setAlignment(Pos.CENTER_LEFT);
    setTop(header);

    // Gallery area
    gallery.setHgap(GAP);
    gallery.setVgap(GAP);
    gallery.setPadding(new Insets(GAP));
    gallery.setPrefColumns(4);
    gallery.setTileAlignment(Pos.CENTER);

    ScrollPane scroller = new ScrollPane(gallery);
    scroller.setFitToWidth(true);
    scroller.setFitToHeight(true);
    setCenter(scroller);

    refreshGallery();
    LOG.info("Initialized SpriteAssetPane");
  }

  private void refreshGallery() {
    gallery.getChildren().clear();
    spriteTemplateMap.getSpriteMap().values()
        .forEach(template -> gallery.getChildren().add(createThumbnail(template)));
    LOG.info("Refreshed gallery");
  }

  private Node createThumbnail(SpriteTemplate template) {
    SpriteSheetLibrary library = editorController.getEditorDataAPI().getSpriteLibrary();
    String atlasFile = template.getAtlasFile();
    String atlasId   = atlasFile.contains(".") ? atlasFile.substring(0, atlasFile.lastIndexOf('.')) : atlasFile;
    SpriteSheetAtlas atlas = library.getAtlas(atlasId);

    if (atlas == null) {
      LOG.warn("Atlas '{}' not found for template '{}'", atlasId, template.getName());
      return new Label(template.getName());
    }

    Image sheet = new Image(atlas.getImageFile().toURI().toString());
    FrameData baseFrame = template.getBaseFrame();

    ImageView imageView = new ImageView(sheet);
    imageView.setViewport(new Rectangle2D(baseFrame.x(), baseFrame.y(), baseFrame.width(), baseFrame.height()));
    imageView.setFitWidth(ICON_SIZE);
    imageView.setFitHeight(ICON_SIZE);
    imageView.setPreserveRatio(true);
    imageView.setSmooth(true);

    Label nameLabel = new Label(template.getName());
    nameLabel.setMaxWidth(ICON_SIZE);
    nameLabel.setWrapText(true);
    nameLabel.setAlignment(Pos.CENTER);

    VBox cell = new VBox(4, imageView, nameLabel);
    cell.setAlignment(Pos.CENTER);
    cell.setOnMouseClicked(evt -> {
      if (evt.getClickCount() == 2) {
        SpriteTemplateComponent dialog = new SpriteTemplateComponent(
            editorController,
            getScene().getWindow(),
            library,
            template);
        dialog.showAndWait();
        SpriteTemplate updated = dialog.getResult();
        if (updated != null) {
          editorController.getEditorDataAPI().addSpriteTemplate(updated);
          LOG.info("Updated sprite template: {}", updated.getName());
          refreshGallery();
        }
      }
    });

    return cell;
  }
}