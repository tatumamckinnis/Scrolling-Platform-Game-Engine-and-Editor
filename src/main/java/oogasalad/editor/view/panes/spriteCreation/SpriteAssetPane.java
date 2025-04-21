package oogasalad.editor.view.panes.spriteCreation;

import java.nio.file.Path;
import java.nio.file.Paths;
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
import oogasalad.editor.model.data.SpriteSheetLibrary;
import oogasalad.editor.model.data.SpriteTemplateMap;
import oogasalad.editor.model.data.object.sprite.FrameData;
import oogasalad.editor.model.data.object.sprite.SpriteTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * UI that lives inside the “Sprites” tab. Implements the buttons for the spritesheet editor and the
 * sprite creation.
 * TODO: Remove duplicate instantiation and checking of sprites
 *
 * @author Jacob You
 */
public class SpriteAssetPane extends BorderPane {

  private static final Logger LOG = LogManager.getLogger(SpriteAssetPane.class);

  private static final double BUTTON_SPACING = 5;
  private static final double ICON_SIZE = 64;
  private static final double GAP = 6;

  private final EditorController editorController;
  private final SpriteTemplateMap spriteTemplateMap;
  private final TilePane gallery = new TilePane();

  public SpriteAssetPane(EditorController editorController, Window ownerWindow) {
    this.editorController = editorController;
    this.spriteTemplateMap = editorController.getEditorDataAPI().getSpriteTemplateMap();

    Button importButton = new Button("Import Sheet");
    Button newSpriteButton = new Button("New Sprite");

    importButton.getStyleClass().add("small-button");
    newSpriteButton.getStyleClass().add("small-button");

    importButton.setOnAction(e ->
        new ProcessSpriteSheetComponent(editorController, ownerWindow).show()
    );

    newSpriteButton.setOnAction(e -> {
      SpriteSheetLibrary library =
          editorController.getEditorDataAPI().getSpriteLibrary();

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
    header.setPadding(new Insets(6));
    setTop(header);

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
    for (SpriteTemplate template : spriteTemplateMap.getSpriteMap().values()) {
      gallery.getChildren().add(createThumbnail(template));
    }
    LOG.info("Refreshed gallery");
  }

  private Node createThumbnail(SpriteTemplate template) {
    String fileName = template.getSpriteFile();
    String gameName = editorController.getEditorDataAPI().getGameName();
    Path imageFile = Paths.get("data","graphicsData", gameName, fileName);

    Image sheet = new Image(imageFile.toUri().toString());

    FrameData baseFrame = template.getBaseFrame();
    ImageView imageView = new ImageView(sheet);
    imageView.setViewport(
        new Rectangle2D(baseFrame.x(), baseFrame.y(), baseFrame.width(), baseFrame.height()));
    imageView.setFitWidth(ICON_SIZE);
    imageView.setFitHeight(ICON_SIZE);
    imageView.setPreserveRatio(true);
    imageView.setSmooth(true);

    Label name = new Label(template.getName());
    name.setMaxWidth(ICON_SIZE);
    name.setWrapText(true);
    name.setAlignment(Pos.CENTER);

    VBox cell = new VBox(4, imageView, name);
    cell.setAlignment(Pos.CENTER);
    cell.setOnMouseClicked(evt -> {
      if (evt.getClickCount() == 2) {
        var dialog = new SpriteTemplateComponent(
            editorController,
            getScene().getWindow(),
            editorController.getEditorDataAPI().getSpriteLibrary(),
            template
        );
        dialog.showAndWait();
        SpriteTemplate updated = dialog.getResult();
        if (updated != null) {
          editorController.getEditorDataAPI().addSpriteTemplate(updated);
          LOG.info("Added new sprite template: {}", updated.getName());
          refreshGallery();
        }
      }
    });
    LOG.info("Created thumbnail");
    return cell;
  }
}
