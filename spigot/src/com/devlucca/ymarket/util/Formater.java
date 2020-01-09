package com.devlucca.ymarket.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class Formater
{
	
	
	public static void main(String[] args) {
		System.out.println(formatGerman(12345.10));
	}
	
	
    private static String formatValue(final double value) {
        final boolean isWholeNumber = value == Math.round(value);
        final DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.GERMANY);
        formatSymbols.setDecimalSeparator(',');
        final String pattern = isWholeNumber ? "###,###.###" : "###,##0.00";
        final DecimalFormat df = new DecimalFormat(pattern, formatSymbols);
        return df.format(value);
    }
    
    public static String formatGerman(double amount) {
        amount = getMoneyRounded(amount);
        String suffix = " ";
        if (amount > 0.0 && amount < 1.0) {
            if (amount == 0.01) {
                suffix = new StringBuilder(String.valueOf(suffix)).toString();
            }
            else if (amount < 1.0) {
                suffix = new StringBuilder(String.valueOf(suffix)).toString();
            }
            amount *= 100.0;
        }
        else if (amount == 1.0) {
            suffix = new StringBuilder(String.valueOf(suffix)).toString();
        }
        else {
            suffix = new StringBuilder(String.valueOf(suffix)).toString();
        }
        if (suffix.equalsIgnoreCase(" ")) {
            suffix = "";
        }
        return formatValue(amount);
    }
    
    public static double getMoneyRounded(final double amount) {
        final DecimalFormat twoDForm = new DecimalFormat("#.##");
        String formattedAmount = twoDForm.format(amount);
        formattedAmount = formattedAmount.replace(",", ".");
        return Double.valueOf(formattedAmount);
    }
	
    public static String formatar(final double n) {
        final NumberFormat instance = NumberFormat.getInstance(Locale.ENGLISH);
        instance.setMaximumFractionDigits(2);
        instance.setMinimumFractionDigits(0);
        return instance.format(n);
    }
	
    public static String format(final double n) {
        if (n < 1000.0) {
        	return formatar(n);
        }
        if (n < 1000000.0) {
            return String.valueOf(formatar(n / 1000.0)) + "K";
        }
        if (n < 1.0E9) {
            return String.valueOf(formatar(n / 1000000.0)) + "M";
        }
        if (n < 1.0E12) {
            return String.valueOf(formatar(n / 1.0E9)) + "B";
        }
        if (n < 1.0E15) {
            return String.valueOf(formatar(n / 1.0E12)) + "T";
        }
        if (n < 1.0E18) {
            return String.valueOf(formatar(n / 1.0E15)) + "Q";
        }
        if (n < 1.0E21) {
            return String.valueOf(formatar(n / 1.0E18)) + "QT";
        }
        return String.valueOf((long)n);
    }
    
    public static String deformat(String value) {
    	if (value.contains(","))
    		value = value.replace(",", ".");
    	String lastAlphabet = null;
    	if (!isProperNumber(value))
    		lastAlphabet = value.replaceAll("[^A-Za-z]+", "");
    	else
    		lastAlphabet = value.replaceAll("[^a-zA-Z]*$", "").replaceAll(".(?!$)", "");
        long multiplier = 1L;
        switch (lastAlphabet.toLowerCase()) {
            case "k":
                multiplier = 1_000L;
                break;
            case "m":
                multiplier = 1_000_000L;
                break;
            case "b":
                multiplier = 1_000_000_000L;
                break;
            case "t":
                multiplier = 1_000_000_000_000L;
                break;
            case "q":
                multiplier = 1_000_000_000_000_000L;
                break;
            case "qt":
                multiplier = 1_000_000_000_000_000_000L;
                break;
            default:
                break;
        }

        String[] values = value.split(lastAlphabet);
        if (multiplier == 1) {
        	return values[0];
        } else {
        	
            BigDecimal valueMultiplier = new BigDecimal(Double.parseDouble(values[0]));
            BigDecimal valueAdder;
            try {
                valueAdder = new BigDecimal(Long.parseLong(values[1]));
            } catch (ArrayIndexOutOfBoundsException ex) {
                valueAdder = new BigDecimal(0);
            }
            BigDecimal total = valueMultiplier.multiply(new BigDecimal(multiplier));
            total.add(valueAdder);
            return String.valueOf(total);
            //System.out.printf("%.0f\n", total);
        }
    }
    
    static boolean isProperNumber(String value) {
        value = value.replaceAll("\\s+", "");
        String count = value.replaceAll("[.0-9]+", "");
        return count.length() < 2;
    }
}
