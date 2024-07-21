package nz.ac.auckland.se206;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import nz.ac.auckland.se206.managers.SceneManager.SceneUi;

/** Represents the state of the game. */
public class GameState {

  /** Represents the objects that the riddle is about. */
  public enum RiddleObjects {
    BOX,
    BLANKET,
    CHAIR,
    STORAGE,
    MUG
  }

  /** Represents the next task to be completed. */
  public enum NextTask {
    RIDDLE,
    BEDROOM,
    PAPER,
    WIRES,
    CONTROL_CONSOLE,
    ESCAPE,
    SUCCESS,
    FAIL
  }

  /** Indicates whether the riddle has been solved in the current game. */
  public static AtomicBoolean isRiddleSolved = new AtomicBoolean(false);

  /** Indicates which object the riddle is about. */
  public static RiddleObjects riddleObject;

  /** Indicates the next task to be completed. */
  public static NextTask nextTask = NextTask.RIDDLE;

  /** Indicates the random number that is used to open the airlock. */
  public static int lockCode;

  /** Indicates whether the airlock is open. */
  public static boolean isAirlockOpen;

  /** Indicates whether the sound is on. */
  public static AtomicBoolean isTextToSpeechOn = new AtomicBoolean(true);

  /** Indicates whether the music is on. */
  public static AtomicBoolean isMusicOn = new AtomicBoolean(true);

  /** Indicates whether the sound effect is on. */
  public static AtomicBoolean isSoundEffectOn = new AtomicBoolean(true);

  /**
   * Indicates the current number of hints remaining difficulty level selected. If there are {@code
   * < 0} hints remaining, the user has infinite hints (i.e. EASY difficulty).
   */
  public static IntegerProperty numHintsRemaining = new SimpleIntegerProperty(-1);

  /** Indicates the current time left using time limit selected. */
  public static int timeLimit;

  /**
   * Indicates the current room the player is in. Useful for transporting player to that room after
   * finished in the chat.
   */
  public static SceneUi currentScene = SceneUi.CONTROL_ROOM;

  /** Indicates the selection of wire colours. */
  private static ArrayList<String> wires = initialiseWires();

  /** Shuffle the colours for start (left column). */
  public static ArrayList<String> startWires = shuffleWires(wires);

  /** Shuffle the colours for end (right column). */
  public static ArrayList<String> endWires = shuffleWires(wires);

  /** Indicates whether the wire game has been solved in the current game. */
  public static BooleanProperty wireGameSolvedProperty = new SimpleBooleanProperty(false);

  /** Resets the state of the game. Useful for restarting. */
  public static void resetGame() {
    // reset all variables
    isRiddleSolved.set(false);
    riddleObject = getRandomObject();
    nextTask = NextTask.RIDDLE;
    lockCode = generateLockCode();
    isAirlockOpen = false;
    numHintsRemaining.set(-1);
    timeLimit = 120;
    startWires = shuffleWires(wires);
    endWires = shuffleWires(wires);
    wireGameSolvedProperty.set(false);
  }

  /**
   * Initialise the wires for the wire game.
   *
   * @return the list of wires
   */
  private static ArrayList<String> initialiseWires() {
    ArrayList<String> wires = new ArrayList<String>();
    wires.add("red");
    wires.add("blue");
    wires.add("green");
    wires.add("yellow");
    return wires;
  }

  /**
   * Shuffles the wires for the wire game.
   *
   * @return the shuffled wires
   */
  private static ArrayList<String> shuffleWires(ArrayList<String> list) {
    ArrayList<String> newList = new ArrayList<String>(list);
    Random random = new Random();
    int size = newList.size();
    for (int i = size - 1; i > 0; i--) {
      // swap wires to ensure randomness
      int j = random.nextInt(i + 1);
      Collections.swap(newList, i, j);
    }
    return newList;
  }

  /**
   * Generates a random number between range [min, max).
   *
   * <p>https://www.baeldung.com/java-generating-random-numbers-in-range
   *
   * @param min minimum value random number can be (inclusive)
   * @param max maximum value random number can be (exclusive)
   * @return the random integer
   */
  public static int getRandomNumber(int min, int max) {
    return (int) ((Math.random() * (max - min)) + min);
  }

  /**
   * Chooses one of the objects at random.
   *
   * @return a random riddle object
   */
  private static RiddleObjects getRandomObject() {
    int random = getRandomNumber(0, RiddleObjects.values().length);
    return RiddleObjects.values()[random];
  }

  /**
   * Generates a random 4-digit lock code.
   *
   * @return the random lock code
   */
  private static int generateLockCode() {
    lockCode = getRandomNumber(1000, 10000);
    return lockCode;
  }
}
