package ua.karazina.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static java.lang.Math.log;
import static java.lang.Math.pow;
import static java.lang.Math.random;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;


public class Controller {

    @FXML
    private TextField angleOfIncidenceField;
    @FXML
    private TextField layerThicknessField;
    @FXML
    private TextField statisticsField;

    @FXML
    private TextField meanFreePathField;
    @FXML
    private TextField absorptionProbField;
    @FXML
    private TextField scatterParameterField;

    @FXML
    private TextField transmissionRatioField;
    @FXML
    private TextField reflectionCoeffField;
    @FXML
    private TextField absorptionCoeffField;

    @FXML
    private TextField uncertaintyZeroField;
    @FXML
    private TextField uncertaintyOneField;
    @FXML
    private TextField uncertaintySecondField;

    private double tet0 = 0;
    private double cos0 = 1.0;

    private double h = 2;
    private double mu = 1;
    private double kr = 1;
    private double pa = 0.001;

    private double Ns = 10000;

    private double F = 0;
    private double B = 0;
    private double Ab = 0;

    @FXML
    private void onStartButtonClick(ActionEvent event) {
        tet0 = parseDouble(angleOfIncidenceField.getText(), "angleOfIncidenceField");
        h = parseDouble(layerThicknessField.getText(), "layerThicknessField");
        mu = parseDouble(meanFreePathField.getText(), "meanFreePathField");

        pa = parseDouble(absorptionProbField.getText(), "absorptionProbField");
        kr = parseDouble(scatterParameterField.getText(), "scatterParameterField");
        Ns = parseDouble(statisticsField.getText(), "statisticField");

        cos0 = countCos0();

        checkMu();
        pasRadian();
    }

    void pasRadian() {
        for (int i = 1; i <= Ns; i++) {
            double x = 0;
            double cs = cos0;
            while (true) {
                double g = random();
                double l = -log(g) / mu; //on slide number 3 Lec_11

                double x1 = countX1(x, cs, l);

                if (isCorrectX1(x1)) break;
                if (isX1biggerThanH(x1)) break;

                g = random();
                if (isPaBiggerThanG(g)) break;

                g = random();
                double Com = elevateG(g);
                g = random();

                double Sfi = countSfi(g);
                double cos1 = countCos1(cs, Com, Sfi);

                x = x1;
                cs = cos1;
            }
        }
        transmissionRatioField.setText(roundDouble(F / Ns, 3));
        reflectionCoeffField.setText(roundDouble(B / Ns, 3));
        absorptionCoeffField.setText(roundDouble(Ab / Ns, 3));

        double SF = getSquare(F);
        uncertaintyZeroField.setText(roundDouble(SF, 4));

        double SB = getSquare(B);
        uncertaintyOneField.setText(roundDouble(SB, 4));

        double SAb = getSquare(Ab);
        uncertaintySecondField.setText(roundDouble(SAb, 4));
    }

    private double countCos0() {
        return Math.cos(3.1415927 * tet0 / 180);
    }

    private void checkMu() {
        if (mu > 0) {
            mu = 1 / mu;
        } else {
            mu = 1;
            meanFreePathField.setText(String.valueOf(mu));
        }
        F = 0;
        B = 0;
        Ab = 0;
    }

    private double countX1(double x, double cs, double l) {
        return x + l * cs; //on slide number 4 Lec_11
    }

    private double getSquare(double n) {
        double number = n / Ns;
        return sqrt(number * (1 - number) / Ns);
    }

    private double countCos1(double cs, double Com, double Sfi) {
        return cs * Com - sqrt((1 - cs * cs) * (1 - Com * Com)) * Sfi;//on slide number 8 Lec_11
    }

    private double countSfi(double g) {
        return sin(2 * 3.1415927 * g);
    }

    private double elevateG(double g) {
        return pow(g, 1 / (kr + 1));//on slide number 7 Lec_11
    }

    private boolean isPaBiggerThanG(double g) {
        if (g < pa) {
            Ab++;
            return true;
        } //on slide number 6 Lec_11
        return false;
    }

    private boolean isX1biggerThanH(double x1) {
        if (x1 > h) {
            F++;
            return true;
        }
        return false;
    }

    private boolean isCorrectX1(double x1) {
        if (x1 < 0) {
            B++;
            return true;
        } //on slide number 5 Lec_11
        return false;
    }

    private String roundDouble(double value, int accuracy) {
        return BigDecimal.valueOf(value)
                .setScale(accuracy, RoundingMode.HALF_EVEN)
                .toString();
    }

    private double parseDouble(String value, String fieldName) {
        double d;
        try {
            d = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setContentText("Wrong value for field: " + fieldName);
            alert.show();
            throw new RuntimeException();
        }
        return d;
    }
}