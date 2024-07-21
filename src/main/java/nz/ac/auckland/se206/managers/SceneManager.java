package nz.ac.auckland.se206.managers;

import java.util.HashMap;
import javafx.scene.Parent;

/** Manager class for the current view being displayed in the scene on the stage. */
public class SceneManager {

  /** The different views available. */
  public enum SceneUi {
    START,
    CHAT,
    RIDDLE,
    CLOSED_AIRLOCK,
    OPEN_AIRLOCK,
    CONTROL_ROOM,
    BEDROOM,
    FAILED,
    SUCCESS,
    ACT_A,
    ACT_B,
    GAME
  }

  private static HashMap<SceneUi, String> sceneStringMap = new HashMap<>();
  private static HashMap<SceneUi, Parent> sceneParentMap = new HashMap<>();

  /**
   * Setter method for adding to the scene (stored as a string) hashmap.
   *
   * @param sceneUi the key for the value being added to the hashmap
   * @param root the value associated with the key in the hashmap
   */
  public static void addStringSceneUi(SceneUi sceneUi, String root) {
    sceneStringMap.put(sceneUi, root);
  }

  /**
   * Getter method for the scene (stored as a string) hashmap. Returns the associated fxml file path
   * as a String, so that it can be loaded.
   *
   * @param sceneUi the view to be selected in the hashmap
   * @return the fxml filename of the desired view
   */
  public static String getStringSceneUi(SceneUi sceneUi) {
    return sceneStringMap.get(sceneUi);
  }

  /**
   * Setter method for adding to the scene (stored as a Parent node) hashmap.
   *
   * @param sceneUi the key for the value being added to the hashmap
   * @param root the value associated with the key in the hashmap
   */
  public static void addParentSceneUi(SceneUi sceneUi, Parent root) {
    sceneParentMap.put(sceneUi, root);
  }

  /**
   * Getter method for the scene (stored as a Parent node) hashmap. Returns the associated Parent
   * node so that the stage can switch to it as the root node.
   *
   * @param sceneUi the view to be selected in the hashmap
   * @return the Parent node of the desired view
   */
  public static Parent getParentSceneUi(SceneUi sceneUi) {
    return sceneParentMap.get(sceneUi);
  }

  /**
   * Clears the sceneParentMap hashmap to reset the views for if the game needs to start from the
   * beginning.
   */
  public static void clearSceneParentMap() {
    sceneParentMap.clear();
  }
}
