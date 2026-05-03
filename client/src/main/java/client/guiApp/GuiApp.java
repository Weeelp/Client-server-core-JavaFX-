package client.guiApp;

import java.util.LinkedList;

import client.network.NetworkWorker;
import common.model.movie.Movie;

public interface GuiApp {

    void startNetworkWorker();

    NetworkWorker getWorker();

    void updateStatus(boolean isOnline);

    void updateTableData(LinkedList<Movie> newList);

    void showMainWindow(LinkedList<Movie> movies);

    void runOnUIThread(Runnable action);

    void showError(String message);
}

