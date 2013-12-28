package edu.unl.act.rma.firm.drought.index;

import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidStateException;


/**
 * @author Flora Jiang
 * @author Jesse Whidden
 * @author Ian Cottingham
 * 
 */
public class StandardizedPrecipitationIndex {

	/**
	 * @param args
	 */
	private float[][] precipitationData;
	private int startYear;
	private int annualPeriods; // this is weekly or monthly

	public StandardizedPrecipitationIndex(int startYear) {
		this.startYear = startYear;

	} // end SPIndexx

	public void setData(float[][] precipitationData) throws InvalidStateException { 
		this.precipitationData = precipitationData;
		this.annualPeriods = precipitationData[0].length;
		
		if ( annualPeriods != 12 && annualPeriods != 52 ) { 
			throw new InvalidStateException("The data must be organized into years with either 12 or 52 periods.  The provided period was "+annualPeriods);
		}
	}
	
	/**
	 * The main method of the SPI. This method can be executed multiple times on
	 * the same data set to generate differing step results.
	 * 
	 * @param steps
	 * @return a double array of floats
	 */
	public float[][] computeSpi(int steps) {
		int nyrsmax;

		// first get nyrsmax
		int calend = precipitationData.length + startYear - 1;
		nyrsmax = calend - startYear + 1; // Calculate years

		// spii,precA spiOut use nyrsmax instead NYRS
		float[] spii = new float[nyrsmax * annualPeriods];
		float[] precA = new float[nyrsmax * annualPeriods];
		float[][] spiOut = new float[nyrsmax][annualPeriods];
		for (int i = 0; i < nyrsmax * annualPeriods; i++)
			precA[i] = DataType.MISSING; // initialize array precA
		int nyrs = 0;
		int i = 0;
		for (i = 0; i < nyrsmax; i++)
			for (int im = 0; im < annualPeriods; im++)

				nyrs = nyrs + 1;

		// add adjust nyrs begin
		for (i = nyrs - 1; i > 0; i--) {
			if (!missing(precipitationData[i / annualPeriods][i % annualPeriods]))
				break;
		}
		nyrs = i + 1;
		// end adjust nyrs

		for (i = 0; i < nyrs; i++) {
			precA[i] = precipitationData[i / annualPeriods][i % annualPeriods];
		}

		spi_gamma(steps, nyrsmax, precA, spii); // compute SPI for one length

		int last = 0;
		// Skip leading MISSINGs
		for (i = 0; i < (nyrsmax * annualPeriods); i++)
			if (!missing(spii[i]))
				break;

		// Skip trailing MISSINGs
		for (last = (nyrsmax * annualPeriods - 1); last > 0; last--)
			if (!missing(spii[last]))
				break;
		last++;

		for (i = 0; i < last; i++) {
			spiOut[i / annualPeriods][i % annualPeriods] = spii[i];
		}

		return spiOut;
	} // end computeSpi

	/*************************************************************************
	 * 
	 * These functions compute the Standardized Precipitation Index using an
	 * incomplete gamma distribution function to estimate probabilities.
	 * 
	 * Useful references are:
	 * 
	 * _Numerical Recipes in C_ by Flannery, Teukolsky and Vetterling Cambridge
	 * University Press, ISBN 0-521-35465-x
	 * 
	 * _Handbook of Mathematical Functions_ by Abramowitz and Stegun Dover,
	 * Standard Book Number 486-61272-4
	 * 
	 * 
	 *************************************************************************/

	/* Calculate indices assuming incomplete gamma distribution. */
	private void spi_gamma(int nrun, /* input - run length */
	int nyrsmax, /* actual effective years */
	float[] pp, /* input - precA array */

	float[] index) /* output - index values */

	{
		int im, i, j, n, k;
		float[] temparr = new float[nyrsmax * annualPeriods];
		float[] aAA = new float[5]; // aAA[0] is the A in equation (3.8)
		float[] beta = new float[annualPeriods]; /* output - param */
		float[] alpha = new float[annualPeriods]; /* output - alpha param */
		float[] pzero = new float[annualPeriods]; /* output - prob of x = 0 */
		k = 0;

		/* The first nrun-1 index values will be missing. */
		for (j = 0; j < nrun - 1; j++)
			index[j] = DataType.MISSING;

		/*
		 * Sum nrun precip. values; store them in the appropriate index
		 * location. If any value is missing; set the sum to missing.
		 */
		for (j = nrun - 1; j < (nyrsmax * annualPeriods); j++) {
			index[j] = 0.0f; // here index[j] is used for the sum of precip
			for (i = 0; i < nrun; i++) {
				if (!missing(pp[j - i]))
					index[j] = index[j] + pp[j - i];
				else {
					index[j] = DataType.MISSING;
					break;
				}
			}
		}

		/*
		 * For nrun<12, the monthly distributions will be substantially
		 * different. So we need to compute gamma parameters for each month
		 * starting with the (nrun-1)th.
		 */
		for (i = 0; i < annualPeriods; i++) {
			k = 0; // used to deside the length of array temprr
			for (j = nrun + i - 1; j < (nyrsmax * annualPeriods); j += annualPeriods) {
				if (!missing(index[j])) {
					temparr[k] = index[j]; // in array temparr is the sum of
											// precip
					k++;
				}
			}
			n = k;
			im = (nrun + i - 1) % annualPeriods; /*
												 * im is the calendar month;
												 * 0=jan...
												 */

			/*
			 * Here's where we do the fitting of g(x). solve the parameters
			 * alpha and beta in equation (3.1)
			 */
			gamma_fit(temparr, n, im, aAA, beta, alpha, pzero);
		}

		/* Replace precip. sums stored in index with SPI's */
		for (j = nrun - 1; j < (nyrsmax * annualPeriods); j++) {
			im = j % annualPeriods;
			if (!missing(index[j])) {
				/*
				 * Get the probability the result index[j] is the probability
				 * H(x) in equation (3.12)
				 */
				index[j] = calcHx(beta[im], alpha[im], pzero[im], index[j]);

				/*
				 * Convert prob. to Z value. the result index[j] is the Z value
				 * which is the value of the SPI in equation (3.14) and (3.15)
				 */
				index[j] = calcZ_SPI(index[j]);
			}
		}

	} // end spi_gamma

	/****************************************************************************
	 * 
	 * input prob; return Z.
	 * 
	 * See Abromowitz and Stegun _Handbook of Mathematical Functions_, p. 933
	 * 
	 ****************************************************************************/
	private float calcZ_SPI(float prob1) {

		float c0 = 2.515517f;
		float c1 = 0.802853f;
		float c2 = 0.010328f;
		float d1 = 1.432788f;
		float d2 = 0.189269f;
		float d3 = 0.001308f;
		float t = 0.0f;
		float minus1;

		// see equation (3.14) (3.15) (3.16) (3.17)
		if (prob1 > 0.5f) {
			minus1 = 1.0f;
			prob1 = 1.0f - prob1;
		} else {
			minus1 = -1.0f;
		}

		if (prob1 < 0.0f) {
			return ((float) 0.0f);
		}
		if (prob1 == 0.0)
			return (9999.0f * minus1);

		t = (float) Math.sqrt(Math.log(1.0f / (prob1 * prob1)));

		return (minus1 * (t - ((((c2 * t) + c1) * t) + c0)
				/ ((((((d3 * t) + d2) * t) + d1) * t) + 1.0f)));
	} // end calcZ_SPI

	/***************************************************************************
	 * 
	 * Estimate incomplete gamma parameters.
	 * 
	 * Input: datarr - data array n - size of datarr
	 * 
	 * Output: aAA--A, beta--beta, alpha--alpha gamma parameters in equation
	 * (3.1) pzero - probability of zero.
	 * 
	 * Return: number of non zero items in datarr.
	 * 
	 ****************************************************************************/
	private int gamma_fit(float[] datarr, int n, int im, float[] aAA,
			float[] beta, float[] alpha, float[] pzero) {
		int i, nact;
		float sum = 0.0f;
		float sumlog = 0.0f;
		float mn = 0.0f;
		float aA = 0.0f;
		if (n <= 0) {
			return (0);
		}
		sum = 0.0f;
		sumlog = 0.0f;
		pzero[im] = 0.0f;
		nact = 0;

		/* compute sums */
		for (i = 0; i < n; i++) {
			if (datarr[i] > 0.0) {
				sum += datarr[i];
				sumlog += Math.log(datarr[i]);
				nact++;
			} else {
				pzero[im]++;
			}
		}

		pzero[im] = pzero[im] / n;
		if (nact != 0.0)
			mn = sum / nact; // mn is mean x--xtopbar

		if (nact == 1) /* Bogus data array but do something reasonable */
		{
			aAA[0] = 0.0f;
			alpha[im] = 1.0f;
			beta[im] = mn;
			return (nact);
		}
		if (pzero[im] == 1.0) /* They were all zeroes. */
		{
			aAA[0] = 0.0f;
			alpha[im] = 1.0f;
			beta[im] = mn;
			return (nact);
		}

		/* Use MLE */
		aA = (float) (Math.log(mn) - sumlog / nact); // see equation (3.8)
		aAA[0] = aA; // aAA[0] is A in equation (3.8)
		alpha[im] = (float) ((1.0f + Math.sqrt(1.0f + 4.0f * aA / 3.0f)) / (4.0f * aA)); // see
																							// equation
																							// (
																							// 3.6
																							// )
		beta[im] = mn / alpha[im]; // see equation (3.7)

		return (nact);
	} // end gamma_fit

	/**************************************************************************
	 * 
	 * Compute probability of a<=x using incomplete gamma parameters.
	 * 
	 * Input: beta , alpha - gamma parameters pzero - probability of zero. x -
	 * value.
	 * 
	 * Return: Probability a<=x. H(x) see equation (3.12)
	 * 
	 ****************************************************************************/

	private float calcHx(float beta, float alpha, float pzero, float x) {
		if (x <= 0.0)
			return (pzero);
		else
			return (pzero + (1.0f - pzero) * calcGxA(alpha, x / beta));
	} // end calcHx

	/*******************************************************************
	 * 
	 * Functions for the incomplete gamma functions P and Q
	 * 
	 * 1 /x -t a-1 G(x) = -------- | e t dt, a > 0 //see equation (3.11)
	 * Gamma(x)/ 0
	 * 
	 * see formula (3.11) Reference: Press, Flannery, Teukolsky, and Vetterling,
	 * _Numerical Recipes_, pp. 160-163
	 * 
	 * Thanks to kenny@cs.uiuc.edu
	 * 
	 *********************************************************************/

	/*
	 * Evaluate G(x) by its series representation. Also put lgamma(a) into gln.
	 */

	private float calcGx(float aa, float ax) {

		int maxiter = 100;
		float epsilon = 3.0e-7f;

		float gln; /* Holds log gamma (a), in case the */
		float ap, sum, del;
		int warn = 0;
		int n;

		gln = lgamma(aa);

		if (ax == 0.0) // here ax is t
			return 0.0f;

		ap = aa; // here aa is alpha in equation (3.11)
		sum = 1.0f / aa;
		del = sum;

		for (n = 0; n < maxiter; ++n) {
			sum += (del *= (ax / ++ap));
			if (Math.abs(del) < epsilon * Math.abs(sum)) {
				return (float) (sum * Math.exp(-ax + aa * Math.log(ax) - gln));
			}

		}
		if (warn++ < 20.) {
			// do nothing, legacy logging
		}

		return (float) (sum * Math.exp(-ax + aa * Math.log(ax) - gln));
	} // end calcGx

	// ------------------------------------------------------------------------
	/*
	 * Evaluate G(alpha,x) in its continued fraction representation. Once again,
	 * return gln = lgamma (a).
	 */

	private float calcGxE(float a, float x) {
		float g = 0.0f;
		float gold, a0, a1, b0, b1, fac, gln;
		float epsilon = 3.0e-07f;
		int maxiter = 100;
		int nwarn = 0;
		int n;

		gln = lgamma(a);
		gold = 0.0f;
		a0 = 1.0f;
		a1 = x; // here x is t see equation (3.11)
		b0 = 0.0f;
		b1 = 1.0f;
		fac = 1.0f;
		for (n = 1; n <= maxiter; ++n) {
			float an = n;
			float ana = an - a; // here a is alpha
			float anf;
			a0 = (a1 + a0 * ana) * fac;
			b0 = (b1 + b0 * ana) * fac;
			anf = an * fac;

			a1 = x * a0 + anf * a1;
			b1 = x * b0 + anf * b1;
			if (a1 != 0.0) {
				fac = 1.0f / a1;
				g = b1 * fac;
				if (Math.abs((g - gold) / g) < epsilon)
					return (float) (g * Math.exp(-x + a * Math.log(x) - gln));

				gold = g;
			}
		}
		if (nwarn++ < 20) {
			// do nothing, legacy logging

		}

		return (float) (g * Math.exp(-x + a * Math.log(x) - gln));
	} // end calcGxE

	// -----------------------------------------------------------------
	// /* Evaluate the incomplete gamma function G(x), choosing the most
	// appropriate representation. see equation (3.11) */

	private float calcGxA(float a, float x) {
		if (x < a + 1.0)
			return calcGx(a, x); // Compute G(x)
		else
			return 1.0f - calcGxE(a, x); // Compute G(x)
	} // end calcGxA

	// -------------------------------------------------------------------------
	/*
	 * Evaluate the incomplete gamma function Q(a,x), choosing the most
	 * appropriate representation.
	 */

	float gammaq(float a, float x) {
		if (x < a + 1.0)
			return 1.0f - calcGx(a, x);
		else
			return calcGxE(a, x);
	} // end gammaq
	// -------------------------------------------------------------------------
	//	
	//

	private float lgamma(float xx) {
		float[] cof = new float[6];
		float x = xx - 1.0f;
		float tmp = x + 5.5f;
		float ser = 1.0f;
		int j;
		cof[0] = 76.18009173f;
		cof[1] = -86.50532033f;
		cof[2] = 24.01409822f;
		cof[3] = -1.231739516f;
		cof[4] = 0.120858003e-2f;
		cof[5] = 0.536382e-5f;
		tmp = (float) (tmp - (x + 0.5f) * Math.log(tmp));
		for (j = 0; j < 6; j++) {
			x = x + 1.0f;
			ser = ser + cof[j] / x;
		}
		return (float) (-tmp + Math.log(2.50662827465f * ser));
	} // end lgamma

	private boolean missing(float val) { 
		return 	(val == DataType.MISSING || val == DataType.ERROR_RESULT || val == DataType.OUTSIDE_OF_RANGE || val == DataType.OUTSIDE_OF_REQUEST_RANGE);
	}
}
