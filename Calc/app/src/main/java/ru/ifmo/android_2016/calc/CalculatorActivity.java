package ru.ifmo.android_2016.calc;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.ParseException;

public class CalculatorActivity extends AppCompatActivity {

    // Decimal format
    private DecimalFormat df = new DecimalFormat();
    private char decimalSeparator = df.getDecimalFormatSymbols().getDecimalSeparator();

    // Layout
    private static final int[] numberButtonIds = new int[]{
            R.id.d0,
            R.id.d1,
            R.id.d2,
            R.id.d3,
            R.id.d4,
            R.id.d5,
            R.id.d6,
            R.id.d7,
            R.id.d8,
            R.id.d9,
            R.id.point
    };
    private Button[] numbers = new Button[numberButtonIds.length];
    private Button add, sub, mul, div, clear, sign, percent, eqv;
    private TextView summary, result;
    private HorizontalScrollView summaryScrollview, resultScrollview;

    // Variables
    private boolean inputState = false;
    private String resultString = "0", summaryString = "";
    private int summaryOperator = 0;
    private double summaryNumber = 0; // left operand

    private void updateState() {
        String operator = (summaryOperator != 0 ? ((Button) findViewById(summaryOperator)).getText().toString() : "");
        summary.setText(String.format(getResources().getString(R.string.operator_separator), summaryString, operator));
        result.setText(resultString);
        summaryScrollview.post(new Runnable() {
            @Override
            public void run() {
                summaryScrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        });
        resultScrollview.post(new Runnable() {
            @Override
            public void run() {
                resultScrollview.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
            }
        });
    }

    private double parseDouble(String s) {
        double d = 0;
        try {
            d = df.parse(s).doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return d;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        df.setMinimumFractionDigits(0);
        df.setMinimumIntegerDigits(1);
        df.setGroupingUsed(false);
        df.setMaximumFractionDigits(64);

        add = (Button) findViewById(R.id.add);
        add.setOnClickListener(putOperator);
        sub = (Button) findViewById(R.id.sub);
        sub.setOnClickListener(putOperator);
        mul = (Button) findViewById(R.id.mul);
        mul.setOnClickListener(putOperator);
        div = (Button) findViewById(R.id.div);
        div.setOnClickListener(putOperator);

        clear = (Button) findViewById(R.id.clear);
        clear.setOnClickListener(deleteNumber);
        clear.setOnLongClickListener(clearInput);
        sign = (Button) findViewById(R.id.sign);
        sign.setOnClickListener(changeSign);
        percent = (Button) findViewById(R.id.percent);
        percent.setOnClickListener(putPercent);
        eqv = (Button) findViewById(R.id.eqv);
        eqv.setOnClickListener(calculate);

        for (int i = 0; i < numberButtonIds.length; i++) {
            numbers[i] = (Button) findViewById(numberButtonIds[i]);
            numbers[i].setOnClickListener(putNumber);
            if (numberButtonIds[i] == R.id.point) {
                numbers[i].setText(String.valueOf(decimalSeparator));
            }
        }

        summary = (TextView) findViewById(R.id.summary);
        result = (TextView) findViewById(R.id.result);

        summaryScrollview = (HorizontalScrollView) findViewById(R.id.summary_scrollview);
        resultScrollview = (HorizontalScrollView) findViewById(R.id.result_scrollview);
        updateState();
    }

    View.OnClickListener putNumber = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            char symbol = ((Button) v).getText().charAt(0);
            if (v.getId() == R.id.point && resultString.indexOf(decimalSeparator) > -1) {
                return; // Point was already used
            }
            if (inputState && resultString.length() >= 16) {
                return; // Too long number
            }
            if (v.getId() == R.id.d0 && resultString.equals("0")) {
                inputState = true;
                return; // No more 0s
            }
            if (Character.isDigit(symbol) || symbol == decimalSeparator) {
                if (!inputState) {
                    resultString = (v.getId() == R.id.point) ? "0" : "";
                    inputState = true;
                }
                resultString += symbol;
            }
            updateState();
        }
    };

    View.OnClickListener putOperator = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            double value = parseDouble(resultString);
            if (summaryString.isEmpty()) {
                summaryString = resultString;
                summaryNumber = value;
            } else if (inputState) {
                switch (summaryOperator) {
                    case R.id.add:
                        summaryNumber += value;
                        break;
                    case R.id.sub:
                        summaryNumber -= value;
                        break;
                    case R.id.mul:
                        summaryNumber *= value;
                        break;
                    case R.id.div:
                        summaryNumber /= value;
                        break;
                }
                summaryString += " " + ((Button) findViewById(summaryOperator)).getText() + " " + resultString;
                resultString = df.format(summaryNumber);
            }
            summaryOperator = v.getId();
            inputState = false;
            updateState();
        }
    };

    View.OnClickListener deleteNumber = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (inputState && resultString.length() > 1) {
                resultString = resultString.substring(0, resultString.length() - 1);
            } else {
                resultString = "0";
                inputState = false;
            }
            updateState();
        }
    };

    View.OnLongClickListener clearInput = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            summaryNumber = 0;
            inputState = false;
            resultString = "0";
            summaryString = "";
            summaryOperator = 0;
            updateState();
            return true;
        }
    };

    View.OnClickListener changeSign = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!resultString.equals("0")) {
                resultString = df.format(-parseDouble(resultString));
                updateState();
            }
        }
    };

    View.OnClickListener putPercent = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            double percentValue = parseDouble(resultString) * 0.01;
            if (summaryString.isEmpty()) {
                resultString = df.format(percentValue);
            } else {
                switch (summaryOperator) {
                    case R.id.add:
                        summaryNumber += summaryNumber * percentValue;
                        break;
                    case R.id.sub:
                        summaryNumber -= summaryNumber * percentValue;
                        break;
                    case R.id.mul:
                        summaryNumber *= summaryNumber * percentValue;
                        break;
                    case R.id.div:
                        summaryNumber /= summaryNumber * percentValue;
                        break;
                }
                summaryOperator = 0;
                summaryString = "";
                resultString = df.format(summaryNumber);
            }
            inputState = false;
            updateState();
        }
    };

    View.OnClickListener calculate = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (summaryString.isEmpty()) {
                return;
            } else {
                double value = parseDouble(resultString);
                switch (summaryOperator) {
                    case R.id.add:
                        summaryNumber += value;
                        break;
                    case R.id.sub:
                        summaryNumber -= value;
                        break;
                    case R.id.mul:
                        summaryNumber *= value;
                        break;
                    case R.id.div:
                        summaryNumber /= value;
                        break;
                }
                resultString = df.format(summaryNumber);
            }
            summaryOperator = 0;
            summaryString = "";
            inputState = false;
            updateState();
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("inputState", inputState);
        outState.putInt("summaryOperator", summaryOperator);
        outState.putString("resultString", resultString);
        outState.putString("summaryString", summaryString);
        outState.putDouble("summaryNumber", summaryNumber);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        inputState = savedInstanceState.getBoolean("inputState");
        summaryOperator = savedInstanceState.getInt("summaryOperator");
        resultString = savedInstanceState.getString("resultString");
        summaryString = savedInstanceState.getString("summaryString");
        summaryNumber = savedInstanceState.getDouble("summaryNumber");
        updateState();
    }
}
