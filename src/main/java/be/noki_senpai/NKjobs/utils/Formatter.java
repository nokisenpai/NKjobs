package be.noki_senpai.NKjobs.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Formatter
{
	public static double format(double amount)
	{
		if(amount % 1 == 0)
		{
			return amount;
		}
		return Math.round(amount * 10) / 10.0;
	}

	// Format amount
	public static String formatMoney(double amount)
	{
		String pattern = "";
		if(amount % 1 == 0)
		{
			pattern = "###,###,###,##0";
		}
		else
		{
			pattern = "###,###,###,##0.00";
		}

		DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.FRENCH);
		formatSymbols.setDecimalSeparator(',');
		formatSymbols.setGroupingSeparator(' ');

		DecimalFormat decimalFormat = new DecimalFormat(pattern, formatSymbols);

		return decimalFormat.format(amount);
	}
}
