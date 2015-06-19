package muffinc.frog.userinterface;

import com.thoughtworks.xstream.XStream;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import muffinc.frog.object.FrogImg;
import muffinc.frog.object.Human;
import muffinc.frog.eigenface.TrainingEngine;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.bytedeco.javacpp.opencv_core;

import java.io.*;

public class Main extends Application {

    XStream xStream = new XStream();

    TrainingEngine engine = null;

    MainController mainController = null;

    Stage primaryStage;

    ProgressBar progressBar = null;


    @Override
    public void start(Stage primaryStage) throws Exception{

        this.primaryStage = primaryStage;

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/uixml/main.fxml"));

        BorderPane root = loader.load();

        mainController = loader.getController();
        mainController.setMain(this);


        primaryStage.setTitle("FROG测试");
        primaryStage.setScene(new Scene(root));

        //TODO add a popup progress bar
        //TODO Not working
        Stage progressStage = initProgressBar();

        primaryStage.show();

        engine = new TrainingEngine();
        progressBar.setProgress(0.5);

        loadHumans();

        progressBar.setProgress(1);
        progressStage.close();
    }

    private Stage initProgressBar() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/uixml/progressBar.fxml"));
        AnchorPane anchorPane = loader.load();
        Stage newStage = new Stage();
        newStage.initModality(Modality.WINDOW_MODAL);
        newStage.initOwner(primaryStage);
        ProgressBarController progressBarController = loader.getController();
        progressBar = progressBarController.progressBar;
        progressBar.setProgress(0);

        newStage.setScene(new Scene(anchorPane));

        newStage.show();
        return newStage;
    }

    @Override
    public void stop() throws Exception {

        File[] files = engine.humanFactory.frogImgTable.keySet().toArray(new File[1]);

        String filesxml = xStream.toXML(files);


        File filesXML = new File("/Users/Meth/Documents/FROG/src/main/resources/appxml/" + "filesXML.xml");
        if (!filesXML.exists()) {
            filesXML.createNewFile();
        }

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filesXML.getAbsoluteFile()));

        bufferedWriter.write(filesxml);
        bufferedWriter.close();

//        FileWriter fileWriter = new FileWriter(engineXML.getAbsoluteFile());
//        fileWriter.write("adsfasdfasfd\nadsfasdfas");
//        fileWriter.close();
    }

    public static void main(String[] args) {
        launch(args);
    }


//    public ObservableList<PeopleGem> getPeopleGemObservableList() {
//        return peopleGemObservableList;
//    }



    //TODO does not handle empty input right
    public void showAddPeopleDialogue() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/uixml/addPeopleDialogue.fxml"));

            AnchorPane anchorPane = loader.load();

            Stage dialogueStage = new Stage();
            dialogueStage.setTitle("Add People");
            dialogueStage.initModality(Modality.WINDOW_MODAL);
            dialogueStage.initOwner(primaryStage);
            Scene scene = new Scene(anchorPane);
            dialogueStage.setScene(scene);

            AddPeopleDialogueController controller = loader.getController();
            controller.setDialogStage(dialogueStage);

            dialogueStage.showAndWait();

            newHuman(controller.name);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Human newHuman(String name) {

        Human human = engine.humanFactory.newHuman(name);
        mainController.updateHumanObservableList();

        return human;
    }

    private void loadHumans() {
        File file = new File(TrainingEngine.HUMAN_DIRECTORY);

        for (File humanFile : file.listFiles(((FileFilter) new WildcardFileFilter("H_*")))) {
            if (humanFile.listFiles().length == 0) {
                System.out.println(humanFile.getName().substring(2) + " is not a valid Human profile.");
            } else {
                Human human = engine.humanFactory.newHuman(humanFile.getName().substring(2));

                for (File picFile : humanFile.listFiles()) {

                    FrogImg frogImg = mainController.addNewImg(picFile, false).getFrogImg();
                    frogImg.detect();

                    for (opencv_core.CvRect cvRect : frogImg.getCvRects()) {

                        human.linkWithImgCvRect(frogImg, cvRect);

                    }

                }
            }
        }
        mainController.updateHumanObservableList();
    }
}
