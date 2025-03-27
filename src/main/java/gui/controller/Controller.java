package gui.controller;

import gui.SceneNavigator;
import rest_client.RestClient;

import java.util.List;

/**
 * DO NOT CHANGE THE CONTENT OF THIS CLASS! <br>
 * <br>
 * Superclass for all explicit controllers.
 */
public abstract class Controller {

    protected RestClient restClient = RestClient.getRestClient();
    protected SceneNavigator sceneNavigator = SceneNavigator.getSceneNavigator();

    /**
     * Uses the provided list to initialize labels, buttons and other components
     * that are present in the layout which belongs to this controller. The
     * implementation in the generic <code>Controller</code> is empty and all
     * initializations must be done in the subclass (if needed).
     *
     * @param <T>  the data type of the items in the list
     * @param data the list containing the data
     */
    public <T> void initializeSceneData(List<T> data) {

    }

}
