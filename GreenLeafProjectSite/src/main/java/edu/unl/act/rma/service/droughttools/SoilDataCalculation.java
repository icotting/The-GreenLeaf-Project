/* Created On: Feb 10, 2010 */
package edu.unl.act.rma.service.droughttools;

import org.joda.time.DateTime;

import edu.unl.act.rma.firm.core.DataType;

/**
 * @see edu.unl.act.rma.firm.drought.component.SoilsDataQuery
 * 
 * @author Laura Meerkatz
 * 
 */

public class SoilDataCalculation {
	private static final float[] PHI = { -0.3865982f, -0.2316132f, -0.0378180f,
			0.1715539f, 0.3458803f, 0.4308320f, 0.3916645f, 0.2452467f,
			0.0535511f, -0.15583436f, -0.3340551f, -0.4310691f };

	/*
	 * modified from edu.unl.act.rma.firm.drought.component.SoilsDataQuery to
	 * allow for custom data
	 */
	public static float getCurrentSoilMoisture(Station station, DateTime date) {

		/*
		 * the first step is to get the necessary data for the computation this
		 * data includes:
		 * 
		 * 1) the AWC values for the station 2) the latitude of the station 3)
		 * the normal temperature for the day being considered for the station
		 * 4) the precipitation on the day being considered 5) the high
		 * temperature on the day being considered
		 */

		int year_index = (date.getYear() - station.getStartYear());

		float lat, prec, wc = 0f;
		float awc = station.getAwc();
		lat = station.getLocation().getLatitude();
		float precip = extractValue(station.getPrecipitationData()[year_index], date);
		float high_temp = calculateAnnualAverage(station
				.getHighTemperatureData());
		float[] normal_temp_aves = station.getTemperatureAverage();

		float TLA, Ss, Su = 0f;
		int south = 0;

		prec = precip;
		float tave = high_temp;
		wc = awc;

		if (wc != DataType.MISSING) {
			TLA = (float) -Math.tan(lat);
		} else {
			throw new RuntimeException(
					"Cannot calculate CSM. AWC value is missing.");
		}

		Ss = 1.0f; // assume the top soil can hold 1in

		if (wc < Ss) {
			wc = Ss; // the soil should be able to hold at least the default Ss
						// value
		}

		Su = (float) wc - Ss;
		Su = (Su < 0) ? 0 : Su; // if Su is less than 0 then Su is set to 0

		/*
		 * I am pretty sure this calculation is for southern hemisphere points,
		 * which do not exist for the purposes of FIRM. This is a bit of logic
		 * from the original code
		 */
		// TODO: test this assumption, and remove is this is the case
		if (TLA > 0) {
			south = 1;
			TLA = -TLA;
		} else {
			south = 0;
		}

		float I = 0f;
		int count = 0;
		float[] d = normal_temp_aves;
		for (int i = 0; i < d.length; i++) {
			if (d[i] != DataType.MISSING) {
				count++;
			}
			I = (d[i] > 32) ? I + (float) Math.pow((d[i] - 32) / 9, 1.514) : I;
		}

		/* make sure it's an annual estimate */
		if (count > 0) {
			I = I / count * 12;
		}
		float A = (float) (6.75 * (Math.pow(I, 3)) / 10000000 - 7.71
				* (Math.pow(I, 2)) / 100000 + 0.0179 * I + 0.49);
		float Dum, Dk = 0;
		float PE;
		int offset = (south > 0) ? 6 : 0;

		if (tave <= 32) {
			PE = 0;
		} else {
			Dum = PHI[(date.getMonthOfYear() - 1 + offset) % 12] * TLA;
			Dk = (float) Math.atan(Math.sqrt(Math.abs(1 - Dum * Dum)) / Dum);

			if (Dk < 0) {
				Dk += Math.PI;
			}

			Dk = (float) (Dk + 0.0157f) / 1.57f;

			if (tave >= 80) {
				PE = (float) (Math.sin(tave / 57.3 - 0.166) - 0.76) * Dk;
			} else {
				Dum = (float) Math.log(tave - 32);
				PE = (float) (Math.exp(-3.863233 + A * 1.715598 - A
						* Math.log(I) + A * Dum))
						* Dk;
			}
		}
		PE *= date.monthOfYear().getMaximumValue();

		float PRO = (Ss + Su);
		float PL = 0.0f;

		if (Ss >= PE) {
			PL = PE;
		} else {
			PL = ((PE - Ss) * Su) / wc + Ss;
			// if PL > PRO then PL > water in the soil, which isn't possible, so
			// PL is set to the water in the soil
			PL = (PL > PRO) ? PRO : PL;
		}
		float R_surface, R_under, surface_L, under_L = 0.0f;
		float new_Su, new_Ss;

		if (prec >= PE) {

			if ((prec - PE) > (1 - Ss)) {
				/*
				 * The excess precip will recharge both layers. (Note -Ss is the
				 * amount of water needed to saturate the top layer of soil
				 * assuming it can only hold 1in. of water.
				 */
				R_surface = 1.0f - Ss;
				new_Ss = 1.0f;
				if ((prec - PE - R_surface) < ((wc - 1.0) - Su)) {
					/*
					 * The entire amount of precip can be absorbed by the soil
					 * (no runoff) and the underlying layer will receive what's
					 * left after the top layer Note: (AWC - 1.0) is the amount
					 * able to be stored in the lower layer.
					 */
					R_under = (prec - PE - R_surface);

				} else {
					/*
					 * the underlying layer is fully recharged and some runoff
					 * will occur
					 */
					R_under = (wc - 1.0f) - Su;

				}
				new_Su = (float) (Su + R_under);
			} else {
				/*
				 * there is only enough moisture to recharge some fo the top
				 * layer
				 */
				new_Ss = (float) (Ss + prec - PE);
				new_Su = Su;
			}
		} else {
			/*
			 * the evapotranspiration is greater than the precipitation. This
			 * means some moisture loos will occur from the soil.
			 */
			if (Ss > (PE - prec)) {
				/*
				 * the moisture from the top layer is enough to meet the
				 * remaining PE so only the top layer loses moisture.
				 */
				surface_L = PE - prec;
				under_L = 0.0f;
				new_Ss = (float) (Ss - surface_L);
				new_Su = Su;

			} else {
				/*
				 * The top layer is drained, so the underlying layer loses
				 * moisture also
				 */
				surface_L = Ss;
				under_L = (PE - prec - surface_L) * Su / wc;
				if (Su < under_L) {
					under_L = Su;
				}
				new_Ss = 0.0f;
				new_Su = (float) (Su - under_L);
			}

			Ss = new_Ss;
			Su = new_Su;

		}
		return Su + Ss;
	}



	public static float calculateAnnualAverage(float[][] data) {
		float total = 0.0f;
		int years = data.length + 1;
		for (int i = 0; i < data.length; i++) {
			int missing = 0;
			int count = 0;
			float year_total = 0.0f;
			for (int j = 0; j < data[i].length; j++) {
				float val = data[i][j];
				if (val != DataType.MISSING && val != DataType.ERROR_RESULT
						&& val != DataType.NONEXISTANT
						&& val != DataType.OUTSIDE_OF_RANGE
						&& val != DataType.OUTSIDE_OF_REQUEST_RANGE) {
					year_total += val;
					count++;
				} else {
					missing++;
				}
			}
			// if more than 20% of values are missing, exclude year from average
			if ((float) (missing / (missing + count)) > 0.2) {
				years--;
			} else {
				total += (float) (year_total / count);
			}
		}
		return total / years;
	}
	
	private static float extractValue(float[] data, DateTime date) {
		int index = date.getDayOfYear() - 1;
		// if date is not on a leap year and after 2-28, add 1 to account for missing value 
		if (date.getYear() % 4 > 0 && date.getDayOfYear() > 59) {
			index++;
		}
		return data[index];
	}
	
}
