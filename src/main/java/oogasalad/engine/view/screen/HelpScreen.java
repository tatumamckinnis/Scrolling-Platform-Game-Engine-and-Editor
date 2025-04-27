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
 *
 * @author Alana Zinkin
 */
public class HelpScreen {

  private static final ResourceManagerAPI resourceManager = ResourceManager.getInstance();
  private static final String help = "help";
  private static final String listTagOpen = "<li>";
  private static final String listTagClose = "</li>";
  private static final String headerTwoOpen = "<h2>";
  private static final String headerTwoClose = "</h2>";
  private static final String paragraphOpen = "<p>";
  private static final String paragraphClose = "</p>";

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

  private String buildHelpHTML() {
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
    html.append(headerTwoOpen).append(resourceManager.getText(help, "help.build.header"))
        .append(headerTwoClose);
    html.append(paragraphOpen).append(resourceManager.getText(help, "help.build.steps")).append(paragraphClose);
    html.append(headerTwoOpen).append(resourceManager.getText(help, "help.tips.header")).append(headerTwoClose);
    html.append(paragraphOpen).append(resourceManager.getText(help, "help.tips.list")).append(paragraphClose);
  }

  private static void buildHTMLButtons(StringBuilder html, ResourceManagerAPI resourceManager) {
    html.append("<ul>");
    html.append(listTagOpen).append(resourceManager.getText(help, "help.menu.selectGameType"))
        .append(listTagClose);
    html.append(listTagOpen).append(resourceManager.getText(help, "help.menu.selectLevel"))
        .append(listTagClose);
    html.append(listTagOpen).append(resourceManager.getText(help, "help.menu.startEngine"))
        .append(listTagClose);
    html.append(listTagOpen).append(resourceManager.getText(help, "help.menu.startEditor"))
        .append(listTagClose);
    html.append(listTagOpen).append(resourceManager.getText(help, "help.menu.help")).append(listTagClose);
    html.append(listTagOpen).append(resourceManager.getText(help, "help.menu.playAnother"))
        .append(listTagClose);
    html.append(listTagOpen).append(resourceManager.getText(help, "help.menu.selectLanguage"))
        .append(listTagClose);
    html.append("</ul>");
  }

  private static void buildHTMLTitle(StringBuilder html, ResourceManagerAPI resourceManager) {
    html.append("<h1>").append(resourceManager.getText(help, "help.title")).append("</h1>");
    html.append(paragraphOpen).append(resourceManager.getText(help, "help.intro")).append(paragraphClose);
    html.append(headerTwoOpen).append(resourceManager.getText(help, "help.menu.header")).append(headerTwoClose);
  }

}
