package zvi.Controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.*;
import zvi.ImageProcessing.MatrixSegmentation;
import zvi.ImageProcessing.ThresholdSegmentation;
import zvi.ImageProcessing.ImageHandler;
import zvi.Main;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
    public AnchorPane thresholdOptions, matrixOptions;

    @FXML
    public CheckBox automaticThreshold, filterOption;

    @FXML
    public TextField manualThresholdCount;

    @FXML
    public Button thresholdSegmentationBtn, matrixSegmentationBtn;

    @FXML
    public ToggleGroup neighbours;

    @FXML
    public RadioButton fourNeighbours, eightNeighbours;

    private ImageHandler loadedImage;
    private ThresholdSegmentation thresholdSegmentation;
    private MatrixSegmentation matrixSegmentation;

    @FXML
    protected void initialize() {
        segmentationMethod.setItems(FXCollections.observableArrayList("Prahování", "Matice sousednosti", "Rozplavování"));
        segmentationMethod.getSelectionModel().select(0);
        automaticThreshold.setSelected(true);
        manualThresholdCount.setDisable(true);

        loadedImageView.setOnMouseClicked(event -> {
            if (loadedImage != null) {
                BufferedImage bufferedImage = new BufferedImage((int) loadedImageView.getImage().getWidth(), (int) loadedImageView.getImage().getHeight(),
                        BufferedImage.TYPE_INT_RGB);
                openImageWindow("original", loadedImageView.getImage(), loadedImage.getImage());

            }
        });

        fourNeighbours.setUserData(0);
        eightNeighbours.setUserData(1);

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
//        histogramChart.getXAxis().setAutoRanging(false);
//        ((NumberAxis)histogramChart.getXAxis()).setLowerBound(0);
//        ((NumberAxis)histogramChart.getXAxis()).setUpperBound(255);
    }

    private void drawMethodOptions(int selectedMethod) {
        thresholdOptions.setVisible(false);
        matrixOptions.setVisible(false);
        switch (selectedMethod) {
            case 0:
                thresholdOptions.setVisible(true);
                break;
            case 1:
                matrixOptions.setVisible(true);
                break;
            case 2:
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
                BufferedImage bufferedImage = ImageIO.read(loadedFile);
                loadedImage = new ImageHandler(bufferedImage);

                Image image = SwingFXUtils.toFXImage(loadedImage.getGrayScaleImage(), null);
                loadedImageView.setImage(image);
            }
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.INFO, "Chyba při načítání souboru.", ex);
        }
    }

    private void openImageWindow(String name, Image image, BufferedImage bufferedImage) {
        try {
//                openImageWindow(thresholdSegmentation.segmentedImage.getImage());
            File f = new File(name + "Image.jpg");
            ImageIO.write(SwingFXUtils.fromFXImage(image, bufferedImage), "png", f);
            Desktop dt = Desktop.getDesktop();
            dt.open(f);
        } catch (IOException e) {

        }
        ;
    }

    private void openHistogramWindow() {

    }

    public void autoSegmentation() {
        thresholdSegmentation = new ThresholdSegmentation(loadedImage, filterOption.isSelected());
        drawHistogramChart(thresholdSegmentation.getImageHistogram());
        histogramChart.setVisible(true);
        int threshold = thresholdSegmentation.findThreshold(100);


//        loadedImage.AutoThresholdSegmentation();
    }

    private void drawHistogramChart(int[] histogramValues) {


        final XYChart.Series<String, Number> histogramSeries = new XYChart.Series<String, Number>();
        histogramChart.getData().clear();
        for (int i = 0; i < histogramValues.length; i++) {
            histogramSeries.getData().add(new XYChart.Data<String, Number>(Integer.toString(i), histogramValues[i]));
        }
        histogramChart.setData(FXCollections.observableArrayList(histogramSeries));
//        histogramChart.getXAxis().invalidateRange(histogramSeries.getData());
        createCursorGraphCoordsMonitor(histogramChart);
    }

    private void createCursorGraphCoordsMonitor(AreaChart<Number, Number> chart) {
        final Axis<Number> xAxis = chart.getXAxis();
        final Axis<Number> yAxis = chart.getYAxis();

        final Node chartBackground = chart.lookup(".chart-plot-background");
        for (Node n : chartBackground.getParent().getChildrenUnmodifiable()) {
            if (n != chartBackground && n != xAxis && n != yAxis) {
                n.setMouseTransparent(true);
            }
        }

//        xAxis.setOnMouseEntered(event -> );

        chartBackground.setOnMouseClicked(event -> {

            thresholdSegmentation.addThreshold((int) event.getX());
        });
    }

    public void matrixSegmentation() {
        histogramChart.setVisible(false);
        boolean useAllNeighbours = false;
        matrixSegmentation = new MatrixSegmentation(loadedImage, useAllNeighbours);
        Image image = SwingFXUtils.toFXImage(matrixSegmentation.segmentedImage.getImage(), null);
        segmentedImageView.setImage(image);
    }

//    private void chartRefresh() {
//
//        series.getData().clear();
//        if (level < datas.length) {
//
//            for (int i = 0; i < datas[level].length; i++) {
//                Data<Number, Number> data = new Data<Number, Number>(i, datas[level][i]);
//                data.setNode(new Circle(3, Color.RED));
//                series.getData().add(data);
//            }
//        }
//        level++;
//
//        chart.getData().clear();
//        chart.getData().add(series);
//        series.getNode().setStyle("-fx-stroke:blue;-fx-stroke-width:1");
//
//        // reDrawShapes(series);
//    }

    @FXML
    public void manual() {
        Image image = SwingFXUtils.toFXImage(thresholdSegmentation.segmentation(), null);
        segmentedImageView.setImage(image);
    }
}
