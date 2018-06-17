package zvi.Controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import zvi.ImageProcessing.MatrixSegmentation;
import zvi.ImageProcessing.ThresholdSegmentation;
import zvi.Main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {

    @FXML
    public MenuItem fileOpen;

    @FXML
    public ImageView loadedImageView, segmentedImageView;

    @FXML
    public AreaChart histogramChart;

    @FXML
    public ChoiceBox segmentationMethod;

    @FXML
    public AnchorPane manualThresholdOptions, matrixOptions, automaticThresholdOptions, manualThresholdField;

    @FXML
    public CheckBox filterOption, normalizeOption;

    @FXML
    public TextField manualThresholdValue, thresholdVicinity, thresholdDistance, matrixSegments;

    @FXML
    public Button createHistogram, matrixSegmentationBtn, manualSegmentationBtn;

    @FXML
    public ToggleGroup neighbours;

    @FXML
    public RadioButton fourNeighbours, eightNeighbours;

    @FXML
    public Label manualThresholdError, detectedThresholds, manualThresholds, reportDialog;

    @FXML
    public ListView manualThresholdList, automaticThresholdList;

    private BufferedImage loadedImage;
    private ThresholdSegmentation thresholdSegmentation;
    private MatrixSegmentation matrixSegmentation;
    private ObservableList<Integer> manualObservableList = FXCollections.observableArrayList();
    private ObservableList<Integer> automaticObservableList = FXCollections.observableArrayList();

    @FXML
    protected void initialize() {
        segmentationMethod.setItems(FXCollections.observableArrayList("Automatické Prahování", "Ruční prahování", "Přebarvování", "Rozplavování"));
        segmentationMethod.getSelectionModel().select(0);

        fourNeighbours.setUserData("four");
        eightNeighbours.setUserData("eight");


        //-- Events

        loadedImageView.setOnMouseClicked(event -> {
            if (loadedImage != null) {
                BufferedImage bufferedImage = new BufferedImage((int) loadedImageView.getImage().getWidth(), (int) loadedImageView.getImage().getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                openImageWindow("original", loadedImageView.getImage(), loadedImage);

            }
        });

        segmentedImageView.setOnMouseClicked(event -> {
            if (matrixSegmentation != null || thresholdSegmentation != null) {
                BufferedImage bufferedImage = new BufferedImage((int) segmentedImageView.getImage().getWidth(), (int) segmentedImageView.getImage().getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                openImageWindow("segmented", segmentedImageView.getImage(), bufferedImage);
            }
        });

        segmentationMethod.getSelectionModel().selectedIndexProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        drawMethodOptions(newValue.intValue());
                    }
                }
        );


        manualObservableList.addListener(new ListChangeListener() {

            @Override
            public void onChanged(ListChangeListener.Change change) {
                if (manualObservableList.size() > 0) {
                    manualSegmentationBtn.setDisable(false);
                } else {
                    manualSegmentationBtn.setDisable(true);
                }
            }
        });
    }

    private void drawMethodOptions(int selectedMethod) {
        automaticThresholdOptions.setVisible(false);
        manualThresholdOptions.setVisible(false);
        matrixOptions.setVisible(false);
        switch (selectedMethod) {
            case 0:
                automaticThresholdOptions.setVisible(true);
                break;
            case 1:
                manualThresholdOptions.setVisible(true);
                break;
            case 2:
                matrixOptions.setVisible(true);
                break;
            case 3:
                break;
        }
    }

    public void fileChooser() {
        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilterBMP = new FileChooser.ExtensionFilter("Soubory BMP (*.bmp)", "*.BMP");
        FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("Soubory JPG (*.jpg)", "*.JPG");
        FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("Soubory PNG (*.png)", "*.PNG");
        fileChooser.getExtensionFilters().addAll(extFilterBMP, extFilterJPG, extFilterPNG);

        fileChooser.setTitle("Otevřít soubor");
        File loadedFile = fileChooser.showOpenDialog(Main.parentWindow);

        try {
            if (loadedFile != null) {
                loadedImage = ImageIO.read(loadedFile);

                Image image = SwingFXUtils.toFXImage(loadedImage, null);
                loadedImageView.setImage(image);
                System.out.println("created new image handler from " + loadedImage.toString());
                System.out.println(image);
            }
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.INFO, "Chyba při načítání souboru.", ex);
        }
    }

    private void openImageWindow(String name, Image image, BufferedImage bufferedImage) {
        try {
            File f = new File(name + "Image.jpg");
            ImageIO.write(SwingFXUtils.fromFXImage(image, bufferedImage), "png", f);
            Desktop dt = Desktop.getDesktop();
            dt.open(f);
        } catch (IOException e) {
            //empty try catch lul
        }
    }

    public void saveSegmentedFile() {
        if (segmentedImageView.getImage() != null) {
            FileChooser fileChooser = new FileChooser();

            FileChooser.ExtensionFilter extFilterBMP = new FileChooser.ExtensionFilter("Soubory BMP (*.bmp)", "*.BMP");
            FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter("Soubory JPG (*.jpg)", "*.JPG *.JPEG");
            FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter("Soubory PNG (*.png)", "*.PNG");
            fileChooser.getExtensionFilters().addAll(extFilterBMP, extFilterJPG, extFilterPNG);

            fileChooser.setTitle("Uložit segmentovaný obraz");
            File saveFile = fileChooser.showSaveDialog(Main.parentWindow);
            if (saveFile != null) {
                try {
                    ImageIO.write(SwingFXUtils.fromFXImage(segmentedImageView.getImage(),
                            null), "png", saveFile);
                } catch (IOException ex) {

                }
            }
        }
    }

    public void createHistogram() {
        thresholdSegmentation = new ThresholdSegmentation(loadedImage, filterOption.isSelected());
        drawHistogramChart(thresholdSegmentation.getImageHistogram());
        segmentedImageView.setImage(null);
        histogramChart.setVisible(true);
    }

    private void drawHistogramChart(int[] histogramValues) {


        final XYChart.Series<String, Number> histogramSeries = new XYChart.Series<String, Number>();
        histogramChart.getData().clear();
        for (int i = 0; i < histogramValues.length; i++) {
            histogramSeries.getData().add(new XYChart.Data<String, Number>(Integer.toString(i), histogramValues[i]));
        }
        histogramChart.setData(FXCollections.observableArrayList(histogramSeries));
    }

    public void matrixSegmentation() {
        segmentedImageView.setImage(null);
        histogramChart.setVisible(false);
        boolean useAllNeighbours = true;

        if (neighbours.getSelectedToggle() != null) {
            if (neighbours.getSelectedToggle().getUserData().toString().equals("four")) {
                useAllNeighbours = false;
            }
        }


        int segments = 3;
        try {
            if (Integer.parseInt(matrixSegments.getText()) >= 2) {
                segments = Integer.parseInt(matrixSegments.getText());
            } else {
                System.out.println("Neplatný parametr. Bude použito základní nastavení na 3 segmenty.");
                reportDialog.setText("Neplatný parametr. Bude použito základní nastavení na 3 segmenty.");
            }
        } catch (Exception e) {

        }

        matrixSegmentation = new MatrixSegmentation(loadedImage, useAllNeighbours, segments);
        Image image = SwingFXUtils.toFXImage(matrixSegmentation.getSegmentedImage(), null);
        segmentedImageView.setImage(image);
    }

    @FXML
    public void manualSegmentation() {
        if (manualThresholdValue.getText() != null && !manualThresholdValue.getText().isEmpty()) {
//            thresholdSegmentation.setThreshold(Integer.parseInt(manualThresholdValue.getText()));
            Image image = SwingFXUtils.toFXImage(thresholdSegmentation.segmentation(), null);
            segmentedImageView.setImage(image);
        } else {
            reportDialog.setText("Nebyl zadán práh segmentace");
        }

    }

    @FXML
    public void addManualThreshold() {
        thresholdSegmentation.addThreshold(Integer.parseInt(manualThresholdValue.getText()));
//        manualThresholds.setText(thresholdSegmentation.getThresholds().toString());
        manualObservableList.setAll(thresholdSegmentation.getThresholds());
        manualThresholdList.setItems(manualObservableList);
    }

    @FXML
    public void removeManualThreshold() {
        thresholdSegmentation.removeThreshold(Integer.parseInt(manualThresholdValue.getText()));
        manualObservableList.setAll(thresholdSegmentation.getThresholds());
        manualThresholdList.setItems(manualObservableList);
    }

    public void automaticSegmentation() {
        int segments = Integer.parseInt(thresholdDistance.getText());  //ne thresholddistance ale počet segmentů
        int maxVicinity = Integer.parseInt(thresholdVicinity.getText());
        int distance = 256 / segments;
        ArrayList<Integer> t;
        boolean smallerVicinity = false;
        do {
            //System.out.println("Hledání prahů s vzdáleností " + distance);
            t = thresholdSegmentation.findThresholds(maxVicinity, distance);

            distance = distance - 10;

            if (distance < 10) {
                if(!smallerVicinity) {
                    maxVicinity = maxVicinity / 2;
                    distance = 256 / segments;
                    smallerVicinity = true;
                }
                else{
                    break;
                }
            }
        } while (t.size() != segments - 1);
        if(t.size() != segments - 1){
            System.out.println("Pro zadaný počet segmentů se nepodařilo nalézt prahy. Nalezený počet segmentů: " + t.size());
            reportDialog.setText("Pro zadaný počet segmentů se nepodařilo nalézt prahy Nalezený počet segmentů: " + t.size());
        }
        automaticObservableList.setAll(thresholdSegmentation.getThresholds());
        automaticThresholdList.setItems(automaticObservableList);

        Image image = SwingFXUtils.toFXImage(thresholdSegmentation.segmentation(), null);
        segmentedImageView.setImage(image);
    }
}
