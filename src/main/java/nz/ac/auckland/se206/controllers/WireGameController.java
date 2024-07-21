package nz.ac.auckland.se206.controllers;

import java.io.IOException;
import java.net.URISyntaxException;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import nz.ac.auckland.se206.App;
import nz.ac.auckland.se206.GameState;
import nz.ac.auckland.se206.GameState.NextTask;
import nz.ac.auckland.se206.managers.SceneManager.SceneUi;

/** The controller class for the wire game view. */
public class WireGameController extends Controller {

  @FXML private Pane rootPane;

  // Starting Wires
  @FXML private Line startLine1; // red, MuxSel
  @FXML private Line startLine2; // blue, RegIn
  @FXML private Line startLine3; // green, RegOut
  @FXML private Line startLine4; // yellow, ALUop

  // Ending Wires
  @FXML private Line endLine1;
  @FXML private Line endLine2;
  @FXML private Line endLine3;
  @FXML private Line endLine4;

  // Labels for each wire
  @FXML private Label label1;
  @FXML private Label label2;
  @FXML private Label label3;
  @FXML private Label label4;
  @FXML private Label label5;
  @FXML private Label label6;
  @FXML private Label label7;
  @FXML private Label label8;

  @FXML private Label statusLabel;

  private boolean isConnecting = false;
  private Line selectedWire;

  /**
   * Initializes the controller class. Ensures the mouse can be tracked and gets the randomised
   * positions of each colour for the wires.
   */
  @FXML
  private void initialize() {
    // Arrange the startends in the order of startWires
    setUpWires(startLine1, "red", true);
    setUpWires(startLine2, "blue", true);
    setUpWires(startLine3, "green", true);
    setUpWires(startLine4, "yellow", true);
    setUpWires(endLine1, "red", false);
    setUpWires(endLine2, "blue", false);
    setUpWires(endLine3, "green", false);
    setUpWires(endLine4, "yellow", false);

    // Add mouse event handlers for all wires
    addMouseHandlers(startLine1);
    addMouseHandlers(startLine2);
    addMouseHandlers(startLine3);
    addMouseHandlers(startLine4);

    // Set up labels
    label1.setText(convertText(GameState.startWires.get(0)));
    label2.setText(convertText(GameState.startWires.get(1)));
    label3.setText(convertText(GameState.startWires.get(2)));
    label4.setText(convertText(GameState.startWires.get(3)));
    label5.setText(convertText(GameState.endWires.get(0)));
    label6.setText(convertText(GameState.endWires.get(1)));
    label7.setText(convertText(GameState.endWires.get(2)));
    label8.setText(convertText(GameState.endWires.get(3)));

    statusLabel.setText("OFFLINE");
    statusLabel.setTextFill(Color.RED);

    rootPane.setOnMouseReleased(event -> endConnecting(event));
  }

  /**
   * Helper method to convert the specified colour to a component of the fuse box.
   *
   * @param colour the colour to convert
   * @return a string of the fuse box component
   */
  private String convertText(String colour) {
    switch (colour) {
      case "red":
        return "MuxSel";
      case "blue":
        return "RegIn";
      case "green":
        return "RegOut";
      case "yellow":
        return "ALUop";
      default:
        return ""; // only four wires, so four colours
    }
  }

  /**
   * Sets up the wires for the wire game.
   *
   * @param line the line to set up
   * @param colour the colour of the wire
   * @param side the side of the wire (left or right)
   */
  private void setUpWires(Line line, String colour, boolean side) {
    // left = true, right = false
    double startXl = 240; // starting position of the left column
    double startXr = 530; // starting position of the right column
    double startY = 117; // starting position of the first row
    double endY = startY; // ending position is the same height as the starting position
    double gap = 66.4; // gap between wires
    double length = 20; // length of the wires
    double startOffset = gap * (GameState.startWires.indexOf(colour));
    // There is a weird issue where the left is lower, this is a hacky fix
    double endOffset = gap * (GameState.endWires.indexOf(colour)) - 2;
    if (side) {
      // left column, all starting wires
      line.setStartX(startXl);
      line.setStartY(startY + startOffset);
      line.setEndX(startXl + length);
      line.setEndY(endY + startOffset);
    } else {
      // right column, all ending Wires
      line.setStartX(startXr);
      line.setStartY(startY + endOffset);
      line.setEndX(startXr + length);
      line.setEndY(endY + endOffset);
    }
  }

  /**
   * Adds mouse event handlers for dragging.
   *
   * @param wire the wire to add mouse event handlers to
   */
  private void addMouseHandlers(Line wire) {
    wire.setOnMousePressed(e -> selectWire(wire));
    wire.setOnMouseDragged(this::updateConnectingLine);
  }

  /**
   * Helper method to select the specified wire.
   *
   * @param wire the wire to select
   */
  private void selectWire(Line wire) {
    selectedWire = wire;
    isConnecting = true;
  }

  /**
   * Ends the connecting of the wire. If the wires are all connected in the correct place, the
   * player will be notified and the wire game will close.
   *
   * @param event the mouse event
   */
  private void endConnecting(MouseEvent event) {
    if (isConnecting) {
      isConnecting = false;
      selectedWire = null;
      if (areAllWiresConnected()) {
        if (GameState.isSoundEffectOn.get()) {
          try {
            Media sound = new Media(App.class.getResource("/sounds/spark.mp3").toURI().toString());
            player = new MediaPlayer(sound);
            player.play();
          } catch (URISyntaxException e) {
            // sound effect file cannot be found
            e.printStackTrace();
          }
        }

        // show successful completion of mini game to player
        statusLabel.setText("ONLINE");
        statusLabel.setTextFill(Color.LIMEGREEN);

        GameState.wireGameSolvedProperty.set(true);
        if (GameState.nextTask == NextTask.WIRES) {
          GameState.nextTask = NextTask.CONTROL_CONSOLE;
        }

        // exit wire game view and go back to airlock
        Task<Void> endTask =
            new Task<Void>() {
              @Override
              protected Void call() throws Exception {
                Thread.sleep(1000);
                App.setRoot(SceneUi.CLOSED_AIRLOCK);
                return null;
              }
            };
        Thread endThread = new Thread(endTask);
        endThread.setDaemon(true);
        endThread.start();
      }
    }
  }

  /**
   * Checks if all wires are connected.
   *
   * @return true if all wires are connected, false otherwise
   */
  private boolean areAllWiresConnected() {
    return isWireConnected(startLine1, endLine1)
        && isWireConnected(startLine2, endLine2)
        && isWireConnected(startLine3, endLine3)
        && isWireConnected(startLine4, endLine4);
  }

  /**
   * Checks if the wire is connected.
   *
   * @param startWire the starting wire
   * @param endWire the ending wire
   * @return true if the wire is connected, false otherwise
   */
  private boolean isWireConnected(Line startWire, Line endWire) {
    return startWire.getBoundsInParent().intersects(endWire.getBoundsInParent());
  }

  /**
   * Helper method to update the connecting line using the current mouse position.
   *
   * @param event the mouse event
   */
  private void updateConnectingLine(MouseEvent event) {
    if (isConnecting && selectedWire != null) {
      double mouseX = event.getX();
      double mouseY = event.getY();

      selectedWire.setEndX(mouseX);
      selectedWire.setEndY(mouseY);
    }
  }

  /**
   * Handles the click action on the back button to return to the airlock.
   *
   * @throws IOException if "src/main/resources/fxml/closedAirlock.fxml" is not found
   */
  @FXML
  private void clickGoBack() throws IOException {
    App.setRoot(SceneUi.CLOSED_AIRLOCK);
  }
}
