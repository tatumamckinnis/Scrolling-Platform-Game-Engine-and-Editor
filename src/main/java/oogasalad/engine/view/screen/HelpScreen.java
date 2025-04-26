package oogasalad.engine.view.screen;

import java.io.FileNotFoundException;
import static java.lang.Integer.parseInt;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import oogasalad.ResourceManager;
import oogasalad.ResourceManagerAPI;

/**
 * HelpScreen displays an HTML help view for each simulation
 */
public class HelpScreen {

  private static final ResourceManagerAPI resourceManager = ResourceManager.getInstance();
  private static final String help = "help";

  /**
   * opens up a help documentation window providing information about the program to assist users
   */
  public void showHelpWindow() throws FileNotFoundException {
    Stage helpStage = new Stage();
    helpStage.setTitle(resourceManager.getText("displayedText", "engine.help"));

    WebView webView = new WebView();
    String htmlContent = buildHelpHTML();
    webView.getEngine().loadContent(htmlContent);

    Scene scene = new Scene(webView,
        parseInt(resourceManager.getConfig("engine.view.help", "help.window.width")),
        parseInt(resourceManager.getConfig("engine.view.help", "help.window.height")));
    helpStage.setScene(scene);
    helpStage.show();
  }

  public String buildHelpHTML() {
    ResourceManagerAPI resourceManager = ResourceManager.getInstance();
    StringBuilder html = new StringBuilder();
    html.append("<html><head><title>").append(resourceManager.getText(help, "help.title"))
        .append("</title></head><body>");
    buildHTMLTitle(html, resourceManager);
    buildHTMLButtons(html, resourceManager);
    buildHTMLTipsSection(html, resourceManager);
    html.append("</body></html>");
    return html.toString();
  }

  private static void buildHTMLTipsSection(StringBuilder html, ResourceManagerAPI resourceManager) {
    html.append("<h2>").append(resourceManager.getText(help, "help.build.header"))
        .append("</h2>");
    html.append("<p>").append(resourceManager.getText(help, "help.build.steps")).append("</p>");
    html.append("<h2>").append(resourceManager.getText(help, "help.tips.header")).append("</h2>");
    html.append("<p>").append(resourceManager.getText(help, "help.tips.list")).append("</p>");
  }

  private static void buildHTMLButtons(StringBuilder html, ResourceManagerAPI resourceManager) {
    html.append("<ul>");
    html.append("<li>").append(resourceManager.getText(help, "help.menu.selectGameType"))
        .append("</li>");
    html.append("<li>").append(resourceManager.getText(help, "help.menu.selectLevel"))
        .append("</li>");
    html.append("<li>").append(resourceManager.getText(help, "help.menu.startEngine"))
        .append("</li>");
    html.append("<li>").append(resourceManager.getText(help, "help.menu.startEditor"))
        .append("</li>");
    html.append("<li>").append(resourceManager.getText(help, "help.menu.help")).append("</li>");
    html.append("<li>").append(resourceManager.getText(help, "help.menu.playAnother"))
        .append("</li>");
    html.append("<li>").append(resourceManager.getText(help, "help.menu.selectLanguage"))
        .append("</li>");
    html.append("</ul>");
  }

  private static void buildHTMLTitle(StringBuilder html, ResourceManagerAPI resourceManager) {
    html.append("<h1>").append(resourceManager.getText(help, "help.title")).append("</h1>");
    html.append("<p>").append(resourceManager.getText(help, "help.intro")).append("</p>");
    html.append("<h2>").append(resourceManager.getText(help, "help.menu.header")).append("</h2>");
  }

}
