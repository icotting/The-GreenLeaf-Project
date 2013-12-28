package edu.unl.act.rma.firm.drought.index;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidStateException;

/*
 * @author Jesse Whidden
 * @author Ian Cottingham
 * 
 */
public class PalmerDroughtSeverityIndex {

	/**
	 * @param args
	 */
	private float[][] precipitationData;
	private float[][] temperatureData;
	private float[] temperatureAverage;
	private int startYear;
	private int annualPeriods; // this is weekly or monthly
	private float stationLatitude;
	private float soilWaterCapacity;
	//these variables keep track of what type of PDSI is being calculated.
	boolean Weekly;
	boolean Monthly;
	boolean SCMonthly;

	// The variables for storing the starting year of calculation and the
	// total number of years to calculate
	int startyear;
	int endyear;
	int totalyears;

	int period_length;        //set to 1 for monthly, otherwise, legth of period
	int num_of_periods;       //number of periods of period_length in a year.

	// The variables used as flags to the pdsi class
	// int bug;
	int output_mode;
	int verbose;
	int s_year;
	int e_year;
	int extra;
	boolean metric;
	//  int south;
	boolean nadss;
	// Various constants used in calculations
	private double TLA; // The negative tangent of latitude is used in calculating PE
	private double AWC; // The soils water capacity
	private double I;   // Thornthwaites heat index
	private double A;   // Thornthwaites exponent
	//private float tolerance; // The tolerance for various comparisons
	private boolean south;

	// The arrays used to read in the normal temp data, a year's worth of 
	// actual temp data, and a years worth of precipitation data
	private double TNorm[]; 
	private double T[];
	private double P[] ;

	// The CAFEC percipitation
	double Phat;
	// These variables are used in calculating the z index
	double d;     // Departure from normal for a period
	double D[]; // Sum of the absolute value of all d values by period
	double k[]; // Palmer's k' constant by period
	double K;     // The final K value for a period
	double Z;     // The z index for a period (Z=K*d)

	// These variables are used in calculation to store the current period's
	// potential and actual water balance variables as well as the soil
	// moisture levels
	private double ET;            // Actual evapotranspiration
	private double R;             // Actual soil recharge 
	private double L;             // Actual loss
	private double RO;            // Actual runoff 
	private double PE;            // Potential evapotranspiration
	private double PR;            // Potential soil recharge
	private double PL;            // Potential Loss
	private double PRO;           // Potential runoff
	private double Su;            // Underlying soil moisture
	private double Ss;            // Surface soil moisture

	double ETSum[] ;
	double RSum[] ;
	double LSum[] ;
	double ROSum[] ;
	double PESum[] ;
	double PRSum[] ;
	double PLSum[];
	double PROSum[] ;
	double PSum[];
	double DEPSum[];
	double DSSqr[];
	double Alpha[];
	double Beta[];
	double Gamma[];
	double Delta[] ;
	private double [][][] potential;
	private double[][] dvalue;
	private double bigtable[][][];

	private double DKSum;
	// These variables are used in calculating the PDSI from the Z
	// index.  They determine how much of an effect the z value has on 
	// the PDSI based on the climate of the region.  
	// They are calculated using CalcDurFact()
	private double wetm;
	private double wetb;
	private double drym;
	private double dryb;


	private  double SD;
	private  double SD2;

	//these two variables weight the climate characteristic in the 
	//calibration process
	private double dry_ratio; 
	private double wet_ratio;

	// The X variables are used in book keeping for the computation of
	// the pdsi
	private double X1;    // Wet index for a month/week
	private double X2;    // Dry index for a month/week
	private double X3;    // Index for an established wet or dry spell
	private double X;     // Current period's pdsi value before backtracking

	// These variables are used in calculating the probability of a wet
	// or dry spell ending
	private double Prob;  // Prob=V/Q*100
	private double V;     // Sumation of effective wetness/dryness
	private double Q;     // Z needed for an end plus last period's V


	// linked lists to store X values for backtracking when computing X
	LinkedList Xlist= new LinkedList();//final list of PDSI values
	LinkedList altX1= new LinkedList();//list of X1 values
	LinkedList altX2= new LinkedList();//list of X2 values

	// These linked lists store the Z, Prob, and 3 X values for
	// outputing the Z index, Hydro Palmer, and Weighted Palmer
	LinkedList XL1= new LinkedList();
	LinkedList XL2= new LinkedList();
	LinkedList XL3= new LinkedList();
	LinkedList ProbL= new LinkedList();
	LinkedList ZIND;
	LinkedList PeriodList;
	LinkedList YearList;

	double tolerance=0.00001;
	int bug=0;



	public PalmerDroughtSeverityIndex(int startYear) {
		this.startYear = startYear;
		verbose = 0;
		bug = 0;
		output_mode = 1;
		tolerance = 0.00001;
		metric =false;
		nadss = false;
		startyear = startYear;
		s_year = startYear;

	}

	public void setData(float[][] precipitationData, float[][] temperatureData, float[] temperatureAverage,float stationLatitude,float soilWaterCapacity) throws InvalidStateException { 
		this.precipitationData = precipitationData;
		this.annualPeriods = precipitationData[0].length;
		this.temperatureData = temperatureData;
		this.annualPeriods = temperatureData[0].length;
		this.temperatureAverage = temperatureAverage;
		this.annualPeriods = temperatureAverage.length;
		this.stationLatitude=stationLatitude;
		this.soilWaterCapacity=soilWaterCapacity;
		endyear = startyear+precipitationData.length-1;
		e_year = endyear;
		totalyears = endyear - startyear + 1;
		if ( annualPeriods != 12 && annualPeriods != 52 ) { 
			throw new InvalidStateException("The data must be organized into years with either 12 or 52 periods.  The provided period was "+annualPeriods);
		}
	}

	
	
	public float[][] weeklyZNDI(int steps) throws InvalidStateException{
		Weekly = true;
		Monthly = false; SCMonthly = false;
		period_length= steps;
		switch(period_length) {
		case 1:
			num_of_periods = 52;
			break;
		case 2:
			num_of_periods = 26;
			break;
		case 4:
			num_of_periods = 13;
			break;
		case 13:
			num_of_periods = 4;
			break;
		default:
			num_of_periods = 52;
		period_length = 1;
		break;
		}

		float[][] pdsi_value= new float[totalyears][num_of_periods*period_length];
		getParam(soilWaterCapacity,stationLatitude,temperatureAverage);
		// SumAll is called to compute the sums for the 8 water balance variables
		SumAll();
		DEPSum= new double[num_of_periods*period_length];
		DSSqr= new double[num_of_periods];
		for (int i=0;i<num_of_periods;i++) {
			DEPSum[i] = ETSum[i] + RSum[i] - PESum[i] + ROSum[i];
			DSSqr[i] = 0;
		}
		// CalcWBCoef is then called to calculate alpha, beta, gamma, and delta
		CalcWBCoef();
		// Next Calcd is called to calculate the weekly departures from normal
		calcd();
		// CalcK is called to compute the K values
		calcK();
		// CalcZ is called to compute the Z index
		CalcZ();
		//calculate the duration factors
		CalcDurFactClass dur= new CalcDurFactClass();
		//  CalcDurFact(wetm, wetb, 1); 
		//  CalcDurFact(drym, dryb, -1); 
		CalcDurFact(dur, 1); 
		wetm=dur.getMl();
		wetb= dur.getBl();
		dur= new CalcDurFactClass();
		CalcDurFact(dur, -1); 
		drym=dur.getMl();
		dryb= dur.getBl();

		//Calculate the PDSI values
		CalcX();
		//Calibrate the Index
		Calibrate();

		// Xlist pdsi value
		// PeriodList // period
		// YearList year
		Iterator i= YearList.descendingIterator();
		Iterator j= PeriodList.descendingIterator();
		Iterator k = ZIND.descendingIterator();
		int cnt=0;
		
		int lastyear=0;
		for(;i.hasNext();){
			int year= (Integer)i.next();
			if(year!=lastyear){
				cnt=0;
			}
			lastyear=year;
			int per= (Integer)j.next();
			
			Float val= new Float(String.valueOf(k.next()));
			for(int step=0; step<52/num_of_periods;step++){
				pdsi_value[year][cnt]=val;
				cnt++;
			}
		}
		return pdsi_value;
	}
	

	public float[][] weeklyPDSI(int steps) throws InvalidStateException{
		Weekly = true;
		Monthly = false; SCMonthly = false;
		period_length= steps;
		switch(period_length) {
		case 1:
			num_of_periods = 52;
			break;
		case 2:
			num_of_periods = 26;
			break;
		case 4:
			num_of_periods = 13;
			break;
		case 13:
			num_of_periods = 4;
			break;
		default:
			num_of_periods = 52;
		period_length = 1;
		break;
		}

		float[][] pdsi_value= new float[totalyears][num_of_periods*period_length];
		getParam(soilWaterCapacity,stationLatitude,temperatureAverage);
		// SumAll is called to compute the sums for the 8 water balance variables
		SumAll();
		DEPSum= new double[num_of_periods*period_length];
		DSSqr= new double[num_of_periods];
		for (int i=0;i<num_of_periods;i++) {
			DEPSum[i] = ETSum[i] + RSum[i] - PESum[i] + ROSum[i];
			DSSqr[i] = 0;
		}
		// CalcWBCoef is then called to calculate alpha, beta, gamma, and delta
		CalcWBCoef();
		// Next Calcd is called to calculate the weekly departures from normal
		calcd();
		// CalcK is called to compute the K values
		calcK();
		// CalcZ is called to compute the Z index
		CalcZ();
		//calculate the duration factors
		CalcDurFactClass dur= new CalcDurFactClass();
		//  CalcDurFact(wetm, wetb, 1); 
		//  CalcDurFact(drym, dryb, -1); 
		CalcDurFact(dur, 1); 
		wetm=dur.getMl();
		wetb= dur.getBl();
		dur= new CalcDurFactClass();
		CalcDurFact(dur, -1); 
		drym=dur.getMl();
		dryb= dur.getBl();

		//Calculate the PDSI values
		CalcX();
		//Calibrate the Index
		Calibrate();

		// Xlist pdsi value
		// PeriodList // period
		// YearList year
		Iterator i= YearList.descendingIterator();
		Iterator j= PeriodList.descendingIterator();
		Iterator k = Xlist.descendingIterator();
		int cnt=0;
		
		int lastyear=0;
		for(;i.hasNext();){
			int year= (Integer)i.next();
			if(year!=lastyear){
				cnt=0;
			}
			lastyear=year;
			int per= (Integer)j.next();
			
			Float val= new Float(String.valueOf(k.next()));
			for(int step=0; step<52/num_of_periods;step++){
				pdsi_value[year][cnt]=val;
				cnt++;
			}
		}
		
		return pdsi_value;
	}
	

	public float [][] scMonthlyZNDI() throws InvalidStateException{
		Monthly = false;
		Weekly = false; 
		SCMonthly = true;
		//preserve period_length and num_of_periods for multiple week PDSI's.

		period_length = 1;
		num_of_periods = 12;
		float pdsi_value[][]= new float[totalyears][num_of_periods];

		getParam(soilWaterCapacity,stationLatitude,temperatureAverage);
		SumAll();
		DEPSum= new double[num_of_periods*period_length];
		DSSqr= new double[num_of_periods];
		for (int i=0;i<num_of_periods;i++) {
			/* DEPSum will only include calibration interval data since the ET, R, PE, and RO 
			 ** sum variables only include data from the calibration interval.
			 */
			DEPSum[i] = ETSum[i] + RSum[i] - PESum[i] + ROSum[i];
			DSSqr[i] = 0;
		}
		// CalcWBCoef is then called to calculate alpha, beta, gamma, and delta
		/* These variables will only include calibration interval data since the other 
		 ** sum variables only include data from the calibration interval--set in SumALL().
		 */
		CalcWBCoef();
		// Next Calcd is called to calculate the monthly departures from normal
		calcd();
		// CalcK is called to compute the K values
		/* These variables will only include calibration interval data since the other 
		 ** sum variables only include data from the calibration interval--set in SumALL().
		 */
		calcK();
		// CalcZ is called to compute the Z index
		CalcZ();
		//calculate the duration factors
		CalcDurFactClass dur= new CalcDurFactClass();
		//  CalcDurFact(wetm, wetb, 1); 
		//  CalcDurFact(drym, dryb, -1); 
		CalcDurFact(dur, 1); 
		wetm=dur.getMl();
		wetb= dur.getBl();
		dur= new CalcDurFactClass();
		CalcDurFact(dur, -1); 
		drym=dur.getMl();
		dryb= dur.getBl();
		//Calculate the PDSI values
		CalcX();
		//Calibrate the Index
		Calibrate();
		Iterator i= YearList.iterator();
		Iterator j= PeriodList.iterator();
		Iterator k = ZIND.iterator();

		for(;i.hasNext();){
			int year= (Integer)i.next();
			int per= (Integer)j.next();
			Float val= new Float(String.valueOf(k.next()));
			pdsi_value[year][per]=val;
		}
		return pdsi_value;
	}


	public float [][] scMonthlyPDSI() throws InvalidStateException{
		Monthly = false;
		Weekly = false; 
		SCMonthly = true;
		//preserve period_length and num_of_periods for multiple week PDSI's.

		period_length = 1;
		num_of_periods = 12;
		float pdsi_value[][]= new float[totalyears][num_of_periods];

		getParam(soilWaterCapacity,stationLatitude,temperatureAverage);
		SumAll();
		DEPSum= new double[num_of_periods*period_length];
		DSSqr= new double[num_of_periods];
		for (int i=0;i<num_of_periods;i++) {
			/* DEPSum will only include calibration interval data since the ET, R, PE, and RO 
			 ** sum variables only include data from the calibration interval.
			 */
			DEPSum[i] = ETSum[i] + RSum[i] - PESum[i] + ROSum[i];
			DSSqr[i] = 0;
		}
		// CalcWBCoef is then called to calculate alpha, beta, gamma, and delta
		/* These variables will only include calibration interval data since the other 
		 ** sum variables only include data from the calibration interval--set in SumALL().
		 */
		CalcWBCoef();
		// Next Calcd is called to calculate the monthly departures from normal
		calcd();
		// CalcK is called to compute the K values
		/* These variables will only include calibration interval data since the other 
		 ** sum variables only include data from the calibration interval--set in SumALL().
		 */
		calcK();
		// CalcZ is called to compute the Z index
		CalcZ();
		//calculate the duration factors
		CalcDurFactClass dur= new CalcDurFactClass();
		//  CalcDurFact(wetm, wetb, 1); 
		//  CalcDurFact(drym, dryb, -1); 
		CalcDurFact(dur, 1); 
		wetm=dur.getMl();
		wetb= dur.getBl();
		dur= new CalcDurFactClass();
		CalcDurFact(dur, -1); 
		drym=dur.getMl();
		dryb= dur.getBl();
		//Calculate the PDSI values
		CalcX();
		//Calibrate the Index
		Calibrate();
		Iterator i= YearList.iterator();
		Iterator j= PeriodList.iterator();
		Iterator k = Xlist.iterator();

		for(;i.hasNext();){
			int year= (Integer)i.next();
			int per= (Integer)j.next();
			Float val= new Float(String.valueOf(k.next()));
			pdsi_value[year][per]=val;
		}
		return pdsi_value;
	}

	private void CalcX(){
		LinkedList tempZ,tempPer,tempYear;
		int year, per;
		while(!Xlist.isEmpty()){
			Xlist.removeFirst();
		}
		while(!XL1.isEmpty()){
			XL1.removeFirst();
		}
		while(!XL2.isEmpty()){
			XL2.removeFirst();
		}
		while(!XL3.isEmpty()){
			XL3.removeFirst();
		}
		while(!altX1.isEmpty()){
			altX1.removeFirst();
		}
		while(!altX2.isEmpty()){
			altX2.removeFirst();
		}
		while(!ProbL.isEmpty()){
			ProbL.removeFirst();
		}
		// Initializes the book keeping indices used in finding the PDSI
		Prob = 0.0;
		X1 = 0.0;
		X2 = 0.0;
		X3 = 0.0;
		X = 0.0;
		V = 0.0;
		Q = 0.0;
		tempZ= new LinkedList();
		tempPer= new LinkedList();
		tempYear= new LinkedList();
		for (Iterator i=ZIND.iterator(); i.hasNext();)
		{
			tempZ.add(i.next());
		}
		for (Iterator i=PeriodList.iterator(); i.hasNext();)
		{
			tempPer.add(i.next());
		}
		for (Iterator i=YearList.iterator(); i.hasNext();)
		{
			tempYear.add(i.next());
		}
		bigtable= new double [totalyears][num_of_periods][5];
		while(!tempZ.isEmpty()){
			Z = (Double)tempZ.removeLast();
			per = (Integer)tempPer.removeLast();
			year = (Integer)tempYear.removeLast();
			CalcOneX(bigtable,per,year);
		}

	}

	//-----------------------------------------------------------------------------
	// This function calculates X, X1, X2, and X3
	//
	// X1 = severity index of a wet spell that is becoming "established"
	// X2 = severity index of a dry spell that is becoming "established"
	// X3 = severity index of any spell that is already "established"
	//
	// newX is the name given to the pdsi value for the current week.
	// newX will be one of X1, X2 and X3 depending on what the current 
	// spell is, or if there is an established spell at all.
	//-----------------------------------------------------------------------------
	private void CalcOneX(double[][][] bigtable,int period_number, int year){
		double newV;    //These variables represent the values for 
		double newProb; //corresponding variables for the current period.
		double newX=0;    //They are kept seperate because many calculations
		double newX1 = 0;   //depend on last period's values.  
		double newX2=0;
		double newX3=0;
		double ZE;      //ZE is the Z value needed to end an established spell

		double m, b, c;

		int wd=0;        //wd is a sign changing flag.  It allows for use of the same
		//equations during both a wet or dry spell by adjusting the
		//appropriate signs.

		if(X3>=0){
			m = wetm;
			b = wetb;
		}
		else{
			m = drym;
			b = dryb;
		}
		c = 1 - (m / (m + b));
		ChooseNew cn= new ChooseNew();
		if(Z==DataType.MISSING){
			bigtable[year][period_number][0]=DataType.MISSING;
			bigtable[year][period_number][1]=DataType.MISSING;
			bigtable[year][period_number][2]=DataType.MISSING;
			bigtable[year][period_number][3]=DataType.MISSING;
			bigtable[year][period_number][4]=DataType.MISSING;
			Xlist.addFirst(DataType.MISSING);
			XL1.addFirst(DataType.MISSING);
			XL2.addFirst(DataType.MISSING);
			XL3.addFirst(DataType.MISSING);
			ProbL.addFirst(DataType.MISSING);
		}else if(Z==DataType.ERROR_RESULT){
			bigtable[year][period_number][0]=DataType.ERROR_RESULT;
			bigtable[year][period_number][1]=DataType.ERROR_RESULT;
			bigtable[year][period_number][2]=DataType.ERROR_RESULT;
			bigtable[year][period_number][3]=DataType.ERROR_RESULT;
			bigtable[year][period_number][4]=DataType.ERROR_RESULT;
			Xlist.addFirst(DataType.ERROR_RESULT);
			XL1.addFirst(DataType.ERROR_RESULT);
			XL2.addFirst(DataType.ERROR_RESULT);
			XL3.addFirst(DataType.ERROR_RESULT);
			ProbL.addFirst(DataType.ERROR_RESULT);
		}else if(Z==DataType.OUTSIDE_OF_RANGE){
			bigtable[year][period_number][0]=DataType.OUTSIDE_OF_RANGE;
			bigtable[year][period_number][1]=DataType.OUTSIDE_OF_RANGE;
			bigtable[year][period_number][2]=DataType.OUTSIDE_OF_RANGE;
			bigtable[year][period_number][3]=DataType.OUTSIDE_OF_RANGE;
			bigtable[year][period_number][4]=DataType.OUTSIDE_OF_RANGE;
			Xlist.addFirst(DataType.OUTSIDE_OF_RANGE);
			XL1.addFirst(DataType.OUTSIDE_OF_RANGE);
			XL2.addFirst(DataType.OUTSIDE_OF_RANGE);
			XL3.addFirst(DataType.OUTSIDE_OF_RANGE);
			ProbL.addFirst(DataType.OUTSIDE_OF_RANGE);
		}else if(Z==DataType.OUTSIDE_OF_REQUEST_RANGE){
			bigtable[year][period_number][0]=DataType.OUTSIDE_OF_REQUEST_RANGE;
			bigtable[year][period_number][1]=DataType.OUTSIDE_OF_REQUEST_RANGE;
			bigtable[year][period_number][2]=DataType.OUTSIDE_OF_REQUEST_RANGE;
			bigtable[year][period_number][3]=DataType.OUTSIDE_OF_REQUEST_RANGE;
			bigtable[year][period_number][4]=DataType.OUTSIDE_OF_REQUEST_RANGE;
			Xlist.addFirst(DataType.OUTSIDE_OF_REQUEST_RANGE);
			XL1.addFirst(DataType.OUTSIDE_OF_REQUEST_RANGE);
			XL2.addFirst(DataType.OUTSIDE_OF_REQUEST_RANGE);
			XL3.addFirst(DataType.OUTSIDE_OF_REQUEST_RANGE);
			ProbL.addFirst(DataType.OUTSIDE_OF_REQUEST_RANGE);
		}else if(Z==DataType.NONEXISTANT){
			bigtable[year][period_number][0]=DataType.NONEXISTANT;
			bigtable[year][period_number][1]=DataType.NONEXISTANT;
			bigtable[year][period_number][2]=DataType.NONEXISTANT;
			bigtable[year][period_number][3]=DataType.NONEXISTANT;
			bigtable[year][period_number][4]=DataType.NONEXISTANT;
			Xlist.addFirst(DataType.NONEXISTANT);
			XL1.addFirst(DataType.NONEXISTANT);
			XL2.addFirst(DataType.NONEXISTANT);
			XL3.addFirst(DataType.NONEXISTANT);
			ProbL.addFirst(DataType.NONEXISTANT);
		}else{
			if(X3>=0) wd=1;
			else wd=-1;
			// If X3 is 0 then there is no reason to calculate Q or ZE, V and Prob
			// are reset to 0;
			if(X3==0) {
				newX3=0;
				newV=0;
				newProb=0;
				cn.setNewX3(newX3);
				chooseX(cn, bug);
				newX1=cn.getNewX1();
				newX= cn.getNewX();
				newX2=cn.getNewX2();
				newX3=cn.getNewX3();
			}
			// Otherwise all calculations are needed.
			else {
				newX3 = (c * X3 + Z/(m+b));
				ZE = (m+b)*(wd*0.5 - c*X3);
				Q=ZE+V;  
				newV = Z - wd*(m*0.5) + wd*Math.min(wd*V+tolerance,0);

				if((wd*newV)>0) {
					newV=0;
					newProb=0;
					newX1=0;
					newX2=0;
					newX=newX3;
					while(!altX1.isEmpty())
						altX1.removeFirst();
					while(!altX2.isEmpty())
						altX2.removeFirst();
				}
				else {
					newProb=(newV/Q)*100;
					if(newProb>=100-tolerance) {
						newX3=0;
						newV=0;
						newProb=100;

					}
					cn.setNewX3(newX3);
					chooseX(cn, bug);
					newX1=cn.getNewX1();
					newX= cn.getNewX();
					newX2=cn.getNewX2();
					newX3=cn.getNewX3();
				}
			}


			bigtable[year][period_number][0]=Z;
			bigtable[year][period_number][1]=newProb;
			bigtable[year][period_number][2]=newX1;
			bigtable[year][period_number][3]=newX2;
			bigtable[year][period_number][4]=newX3;

			//update variables for next month:
			V = newV;
			Prob = newProb;
			X1 = newX1;
			X2 = newX2;
			X3 = newX3;

			//add newX to the list of pdsi values
			Xlist.addFirst(newX);
			XL1.addFirst(X1);
			XL2.addFirst(X2);
			XL3.addFirst(X3);
			ProbL.addFirst(Prob);
		}

	}
	private void Calibrate() {  
		LinkedList tempZ=new LinkedList();
		float cal_range;

		//calibrate using upper and lower 2% 
		cal_range = 4.0f;
		dry_ratio = (-cal_range / safe_percentile(0.02f,Xlist));
		wet_ratio = (cal_range / safe_percentile(0.98f,Xlist));

		//adjust the Z-index values
		while(!ZIND.isEmpty()){

			Z = (Double)ZIND.removeLast();
			if(Z != DataType.MISSING && Z != DataType.ERROR_RESULT && Z != DataType.OUTSIDE_OF_RANGE &&Z!=DataType.OUTSIDE_OF_REQUEST_RANGE&&Z!=DataType.NONEXISTANT){
				if(Z >= 0)
					Z =Z * wet_ratio;
				else
					Z = Z * dry_ratio;
			}
			tempZ.addFirst(Z);
		}
	
		for (Iterator i=tempZ.iterator(); i.hasNext();)
		{
			ZIND.add(i.next());
		}

		CalcX();
	}//end of calibrate()

	private void Backtrack(double X1, double X2){
		double num1, num2;
		num1=X1;
		Iterator set=null;
		int count=0;
		while (!altX1.isEmpty() && !altX2.isEmpty()) {
			if (num1>0) {
				num1=(Double)altX1.removeFirst();
				num2=(Double)altX2.removeFirst();
			}
			else {
				num1=(Double)altX2.removeFirst();
				num2=(Double)altX1.removeFirst();
			}
			if (-tolerance<=num1 && num1<=tolerance){
				num1=num2;
			}
			count=set_node1(set,num1, count);
		}
	} 

	private int set_node1(Iterator set,double x, int index){

		set=Xlist.iterator();
		Iterator i = set;
	
		for(int j =0; j<index; j++){
			i.next();
		}
		for (; i.hasNext();)
		{
			Double val= new Double(String.valueOf(i.next()));
			if((val!=DataType.MISSING)&&(val!=DataType.NONEXISTANT)&&(val!=DataType.ERROR_RESULT)&&(val!=DataType.OUTSIDE_OF_RANGE)&&(val!=DataType.OUTSIDE_OF_REQUEST_RANGE)){
				break;
			}
			index++;
		}
		Xlist.set(index, x);
		return ++index;
	}

	private void chooseX(ChooseNew cn, int bug){
		double m, b;
		double wetc, dryc;
		double newX, newX1, newX2, newX3=0.0;
		newX=cn.getNewX();
		newX1=cn.getNewX1();
		newX2=cn.getNewX2();
		newX3=cn.getNewX3();

		if(X3>=0){
			m = wetm;
			b = wetb;
		}
		else{
			m = drym;
			b = dryb;
		}

		wetc = 1 - (wetm / (wetm+wetb));
		dryc = 1 - (drym / (drym+wetb));

		newX1 = (wetc*X1 + Z/(wetm+wetb));
		if(newX1 < 0)
			newX1 = 0;
		newX2 = X2;

		if(bug==0){
			newX2 = (dryc*X2 + Z/(drym+dryb));
			if(newX2 > 0)
				newX2 = 0;
		}

		//if((newX1 >= 0.5)&&(newX3 == 0)){
		if((newX1-0.5 >=tolerance)&&(newX3 == 0)){
			Backtrack(newX1, newX2);
			newX = newX1;
			newX3 = newX1;
			newX1 = 0;
		}
		else{
			newX2 = (dryc*X2 + Z/(drym+dryb));
			if(newX2 > 0)
				newX2 = 0;

			//if((newX2 <= -0.5)&&(newX3 == 0)){
			if((newX2 <= -0.5+tolerance)&&(newX3 == 0)){
				Backtrack(newX2, newX1);
				newX = newX2;
				newX3 = newX2;
				newX2 = 0;
			}
			else if(newX3 == 0) {
				if(newX1 == 0){
					Backtrack(newX2, newX1);
					newX = newX2;
				}
				else if(newX2 == 0){
					Backtrack(newX1, newX2);
					newX = newX1;
				}
				else{
					altX1.addFirst(newX1);
					altX2.addFirst(newX2);
					newX = newX3;
				}
			}

			else{
				//store X1 and X2 in their linked lists for possible use later
				altX1.addFirst(newX1);
				altX2.addFirst(newX2);
				newX = newX3;
			}
		}
		cn.setNewX(newX);
		cn.setNewX1(newX1);
		cn.setNewX2(newX2);
		cn.setNewX3(newX3);
	}
	private void getParam(double AWC_param, double TLA_param, float[] temperatureAverage) {
		AWC = AWC_param;
		TLA = TLA_param;
		double lat;
		if(metric){
			AWC= AWC/25.2f;
		}
		double PI = 3.1415926535;
		//check for metric and invalid AWC values left out
		Ss = 1.0f;   //assume the top soil can hold 1 inch
		if(AWC < Ss) {
			//always assume the top layer of soil can 
			//hold at least the Ss value of 1 inch.
			AWC = Ss;
		}
		Su = AWC - Ss;
		if(Su < 0)
			Su = 0;
		//nadss=false;
		south= false;
		if(nadss){
			if(TLA > 0){
				south = true;
				TLA = -TLA;
			}
			else
				south = false;
		}
		else {
			lat = TLA;
			TLA =  -Math.tan(PI*lat/180);
			if(lat >= 0) {
				south = false;
			}
			else {
				south = true;
				TLA = -TLA;
			}
		}
		if(Weekly)
			I=calcWkThornI(temperatureAverage); 
		else if(Monthly || SCMonthly)
			I=calcMonThornI(temperatureAverage);

		A=calcThornA(I);
	}


	//-----------------------------------------------------------------------------
	// This function calculates the Thornthwaite heat index I.  This is done by 
	// reading in the weekly normal temperature from file.  Any above freezing 
	// temperatures are adjusted and then added to the index.  The equations have
	// been modified to handle temperature in degrees Fahrenheit
	//-----------------------------------------------------------------------------
	private double calcWkThornI(float t[]) {
		double I = 0;
		int i = 0;
		TNorm= new double[t.length];
		//float tNormI[] = new float[normal.length];
		//System.arraycopy(normal, 0, tNormI, 0, normal.length);
		//check for metric temps is left out
		for (i = 0; i < t.length; i++) {
			if(metric){
				TNorm[i]= t[i]*(9.0/5.0)+32;
			}else{
				TNorm[i]= t[i];
			}
			if (TNorm[i] > 32)
				I = (I + Math.pow((TNorm[i]-32)/9, 1.514));
		}
		return (I/52 * 12);
	}

	//-----------------------------------------------------------------------------
	//This function calculates the Thornthwaite heat index I for monthly PDSI's.
	//-----------------------------------------------------------------------------
	private double calcMonThornI(float t[]) {
		double I = 0;
		int i = 0;
		TNorm= new double[t.length];

		//check for metric temps is left out
		for (i = 0; i < 12; i++) {
			if(metric){
				TNorm[i]= t[i]*(9.0/5.0)+32;
			}else{
				TNorm[i]= t[i];
			}
			if (TNorm[i] >32)
				I = (I + Math.pow((TNorm[i]-32)/9, 1.514));
		}
		return I;
	}

	//-----------------------------------------------------------------------------
	// CalcThornA calculates the Thornthwaite exponent a based on the heat index I.
	//-----------------------------------------------------------------------------
	private double calcThornA(double I) {
		double A;
		A = (6.75*(Math.pow(I,3))/10000000 - 7.71*(Math.pow(I,2))/100000 + 0.0179*I + 0.49);
		return A;
	}

	private void calcPL() {
		if (Ss >= PE)
			PL = PE;
		else {
			PL = ((PE - Ss) * Su) / (AWC) + Ss;
			if(PL > PRO)  
				PL = PRO;
		}
	}

	private void calcActual(int period) {
		double R_surface = 0.0;   // recharge of the surface layer
		double R_under = 0.0;    // recharge of the underlying layer
		double surface_L = 0.0;   // loss from surface layer
		double under_L = 0.0;    // loss from underlying layer
		double new_Su, new_Ss;    // new soil moisture values

		if (P[period] >= PE) {
			ET = PE;
			L = 0.0f;

			if ((P[period] - PE) > (1.0f - Ss)) {
				R_surface = 1.0f - Ss;
				new_Ss = 1.0f;
				if((P[period] - PE - R_surface) < ((AWC - 1.0f) - Su)) {
					R_under = (P[period] - PE - R_surface);
					RO = 0.0f;
				}
				else {
					R_under = (AWC - 1.0f) - Su;
					RO = P[period] - PE - (R_surface + R_under);
				}
				new_Su = Su + R_under;
				R = R_surface + R_under;
			}
			else {
				R = P[period] - PE;
				new_Ss = Ss + R;
				new_Su = Su;
				RO = 0.0f;
			}
		}
		else {
			if(Ss > (PE - P[period])) {
				surface_L = PE - P[period];
				under_L = 0.0f;
				new_Ss = Ss - surface_L;
				new_Su = Su;
			}
			else {
				surface_L = Ss;
				under_L = (PE - P[period] - surface_L) * Su / AWC;
				if(Su < under_L)
					under_L = Su;
				new_Ss = 0.0f;
				new_Su = Su - under_L;
			}
			R = 0.0f;
			L = under_L + surface_L;
			RO = 0.0f;
			ET = P[period] + L;
		}
		Ss = new_Ss;
		Su = new_Su;
	}

	private void SumAll() throws InvalidStateException {
		ETSum = new double[annualPeriods];
		RSum = new double[annualPeriods];
		LSum = new double[annualPeriods];
		ROSum = new double[annualPeriods];
		PESum = new double[annualPeriods];
		PRSum = new double[annualPeriods];
		PLSum = new double[annualPeriods];
		PROSum = new double[annualPeriods];
		PSum = new double[annualPeriods];
		T = new double[annualPeriods];
		P= new double[annualPeriods];
		potential= new double[totalyears][annualPeriods][6]; // this is to hold the values of the potential file in the original c++ code

		double DEP=0.0;
		SD=0;
		SD2=0;
		for(int i = 0; i < annualPeriods; i++) {
			ETSum[i] = 0;
			RSum[i] = 0;
			LSum[i] = 0;
			ROSum[i] = 0;
			PSum[i] = 0;
			PESum[i] = 0;
			PRSum[i] = 0;
			PLSum[i] = 0;
			PROSum[i] = 0;
		}

		for (int i = 0; i < totalyears; i++) {
			getTemp(temperatureData[i],i,annualPeriods,i+startyear,T);
			getPrecip(precipitationData[i],i,annualPeriods,i+startyear,P);

			for (int per = 0; per < num_of_periods; per++) {

				if(P[per]<0||T[per]==DataType.MISSING){
					for(int index=0; index<6; index++){
						potential[i][per][index]=DataType.MISSING;
					}
				}else if(T[per]==DataType.ERROR_RESULT){
					for(int index=0; index<6; index++){
						potential[i][per][index]=DataType.ERROR_RESULT;
					}
				}else if(T[per]==DataType.OUTSIDE_OF_RANGE){
					for(int index=0; index<6; index++){
						potential[i][per][index]=DataType.OUTSIDE_OF_RANGE;
					}
				}else if(T[per]==DataType.MISSING){
					for(int index=0; index<6; index++){
						potential[i][per][index]=DataType.MISSING;
					}
				}else if(T[per]==DataType.OUTSIDE_OF_REQUEST_RANGE) {
					for(int index=0; index<6; index++){
						potential[i][per][index]=DataType.OUTSIDE_OF_REQUEST_RANGE;
					}
				}
				else{
					// calculate the Potential Evapotranspiration first
					// because it's needed in later calculations
					if(Weekly){
						calcWkPE(per,startYear);
					}else{
						calcMonPE(per, startYear);
					}


					calcPR();// calculate Potential Recharge, Potential Runoff,
					calcPRO();
					calcPL();
					calcActual(per);
					// Calculates some statistical variables for output 
					// to the screen in the most verbose mode (verbose > 1)
					if(Weekly){
						if (per > (17/period_length) && per < (35/period_length)) {
							DEP = DEP + P[per] + L - PE;
							if (per>(30/period_length) && per < (35/period_length)) {
								SD=SD+DEP;
								SD2=SD2+DEP*DEP;
								DEP=0;
							}
						}
					}
					else{
						if (per > 4 && per < 8) {
							DEP = DEP + P[per] + L - PE;
							if (per == 7) {
								SD=SD+DEP;
								SD2=SD2+DEP*DEP;
								DEP=0;
							}
						}
					}
					ETSum[per] += ET;
					RSum[per] += R;
					ROSum[per] += RO;
					LSum[per] += L;
					PSum[per] += P[per];
					PESum[per] += PE;
					PRSum[per] += PR;
					PROSum[per] += PRO;
					PLSum[per] += PL;
					potential[i][per][0]=P[per];
					potential[i][per][1]=PE;
					potential[i][per][2]=PR;
					potential[i][per][3]=PRO;
					potential[i][per][4]=PL;
					potential[i][per][5]=P[per]-PE;
				}
			}
		}
	}
	
	private void getTemp(float [] temperature, int index, int size, int year, double []A){
		float t2[], temp;
		t2= new float[size];
		int i, j, missing_weeks, error_weeks, oor_weeks, total_bad_weeks,oorr_weeks, nonex;

		for(i = 0; i < size; i++){
			A[i] = 0;
		}
		for(i = 0; i < size; i++) {
			t2[i] = temperature[i];
		}
		for(i = 0; i < num_of_periods; i++) {
			missing_weeks = 0;
			error_weeks = 0;
			oor_weeks = 0;
			total_bad_weeks = 0;
			oorr_weeks=0;
			nonex=0;
			temp = 0;
			for(j = 0; j < period_length; j++) {
				if(t2[i*period_length+j] == DataType.MISSING) {
					missing_weeks++;
					total_bad_weeks++;
				} else if (t2[i*period_length+j] ==DataType.ERROR_RESULT) {
					error_weeks++;
					total_bad_weeks++;
				} else if (t2[i*period_length+j] == DataType.OUTSIDE_OF_RANGE) {
					oor_weeks++;
					total_bad_weeks++;
				} else if (t2[i*period_length+j] == DataType.OUTSIDE_OF_REQUEST_RANGE) {
					oorr_weeks++;
					total_bad_weeks++;
				} else if (t2[i*period_length+j] == DataType.NONEXISTANT) {
					nonex++;
					total_bad_weeks++;
				} else {
					temp += t2[i*period_length+j];
				}
			}
			if (total_bad_weeks >= period_length) {
				A[i] = DataType.MISSING;
			} else if (missing_weeks >= period_length) {
				A[i] = DataType.MISSING;
			} else if (error_weeks >= period_length) {
				A[i] = DataType.ERROR_RESULT;
			} else if (oor_weeks >= period_length) {
				A[i] = DataType.OUTSIDE_OF_RANGE;
			} else if (nonex >= period_length) {
				A[i] = DataType.NONEXISTANT;
			}else if (oorr_weeks >= period_length) {
				A[i] = DataType.OUTSIDE_OF_REQUEST_RANGE;
			}else {
				A[i] = temp / (period_length - total_bad_weeks);
			}
		}
		if(metric){
			for(i = 0; i < num_of_periods; i++){
				if(A[i] != DataType.MISSING && A[i] != DataType.ERROR_RESULT && A[i] != DataType.OUTSIDE_OF_RANGE
						&& A[i] != DataType.OUTSIDE_OF_REQUEST_RANGE&& A[i] != DataType.NONEXISTANT)
					A[i] = A[i] * (9.0/5.0) + 32;
			}
		}
	}

	private void getPrecip(float [] precip, int index, int size, int year, double []A){
		float t2[], temp;
		t2= new float[size];
		int i, j, missing_weeks, error_weeks, oor_weeks, total_bad_weeks,oorr_weeks, nonex;;

		for(i = 0; i < size; i++){
			A[i] = 0;
		}
		for(i = 0; i < size; i++) {
			t2[i] = precip[i];
		}
		for(i = 0; i < num_of_periods; i++) {
			missing_weeks = 0;
			error_weeks = 0;
			oor_weeks = 0;
			total_bad_weeks = 0;
			oorr_weeks=0;
			nonex=0;
			temp = 0;
			for(j = 0; j < period_length; j++) {
				if(t2[i*period_length + j] == DataType.MISSING) {
					missing_weeks++;
					total_bad_weeks++;
				} else if (t2[i*period_length + j] ==DataType.ERROR_RESULT) {
					error_weeks++;
					total_bad_weeks++;
				} else if (t2[i*period_length + j] == DataType.OUTSIDE_OF_RANGE) {
					oor_weeks++;
					total_bad_weeks++;
				}else if (t2[i*period_length+j] == DataType.OUTSIDE_OF_REQUEST_RANGE) {
					oorr_weeks++;
					total_bad_weeks++;
				} else if (t2[i*period_length+j] == DataType.NONEXISTANT) {
					nonex++;
					total_bad_weeks++;
				} else {
					temp += t2[i*period_length + j];
				}
				if (total_bad_weeks >= period_length) {
					A[i] = DataType.MISSING;
				} else if (missing_weeks >= period_length) {
					A[i] = DataType.MISSING;
				} else if (error_weeks >= period_length) {
					A[i] = DataType.ERROR_RESULT;
				} else if (oor_weeks >= period_length) {
					A[i] = DataType.OUTSIDE_OF_RANGE;
				}  else if (nonex >= period_length) {
					A[i] = DataType.NONEXISTANT;
				}	else if (oorr_weeks >= period_length) {
					A[i] = DataType.OUTSIDE_OF_REQUEST_RANGE;
				}	else {
					A[i] = temp ;
				}
			}
		}
		if(metric){
			for(i = 0; i < num_of_periods; i++){
				if(A[i] != DataType.MISSING && A[i] != DataType.ERROR_RESULT && A[i] != DataType.OUTSIDE_OF_RANGE
						&& A[i] != DataType.OUTSIDE_OF_REQUEST_RANGE&& A[i] != DataType.NONEXISTANT)
					A[i] = A[i] /25.4;
			}
		}
	}
	private void calcPR(){
		PR= AWC-(Su+Ss);
	}

	private void calcPRO(){
		PRO=Ss+Su;
	}
	private void calcWkPE(int period, int year) {
		double Phi[] = new double[]{-.39796983,-.38279708,-.36154002,-.33468535,-.30281503,-.26657407,-.22664177,-.18370885,-.13846106,-.09156812,-.04368365,0.00456585,.05256951,.09973421,.14548036,.18923931,.23045239,.26857222,.30306721,.33342965,.35918741,.37991898,.39527047,.40497267,.40885593,.40686015,.3990384,.38555316,.36666599,.34272229,.31413363,.28136006,.24489444,.20524994,.16295153,.11853109,.0725259,.02547986,-.02205355,-.06950714,-.11629575,-.16181268,-.20542804,-.24649005,-.28433068,-.31827697,-.3476688,-.37188342,-.3903656,-.40266061,-.40844587,-.40755601};
		//Phi is a daylight constant based on what day of the week January 1st falls
		//on.  The method of calculating Phi was taken directly from palmcode.f, the
		//NCDC's weekly palmer program.
		//For this program however, Phi was calculated by first calculating the 
		//Phi values for a year with January 1st falling on each day of the week and 
		//then averaging those results.  This was found to have little to no effect
		//on the final results.
		double Dum, Dk;
		double adjusted_Phi[] = new double[52];
		double temp;
		int i, j;
		int offset;
		if (south == true)
			offset = 26;
		else
			offset = 0;

		//need to average Phi[] into an array of num_of_period elements
		//if calculated in southern hemisphere, this will also shift
		//the values by 6 months worth to make up for the opposite seasons.
		for(i=0; i<(52/period_length); i++){
			temp = 0;
			for(j=0;j<period_length;j++)
				temp += Phi[((i*period_length)+j+offset )%52];
			temp = temp/period_length;
			adjusted_Phi[i]=temp;
		}

		if(T[period] <= 32)
			PE = 0;
		else {
			Dum = adjusted_Phi[period]*TLA;
			Dk =  Math.atan(Math.sqrt(1-Dum*Dum)/Dum);
			if (Dk < 0)
				Dk += 3.141593;
			Dk = (Dk + .0157)/1.57;
			if (T[period] >= 80)
				PE =  (Math.sin(T[period]/57.3-.166)-.76)*Dk;
			else {
				Dum =  Math.log(T[period]-32);
				PE =  (Math.exp(-3.863233+A*1.715598-A*Math.log(I)+A*Dum))*Dk;
			}
		}
		PE = PE * period_length * 7;
	}

	private void calcMonPE(int month, int year){
		double Phi[] = new double[]{-.3865982,-.2316132,-.0378180,.1715539,.3458803,.4308320,.3916645,.2452467,.0535511,-.15583436,-.3340551,-.4310691}; 
		//these values of Phi[] come directly from the Fortran program.
		int Days[] = new int[]{31,28,31,30,31,30,31,31,30,31,30,31};
		double Dum, Dk;
		int offset;
		if (south == true)
			offset = 6;
		else
			offset = 0;

		if (T[month] <= 32)
			PE = 0;
		else {
			Dum = Phi[(month + offset)%12]*TLA;
			Dk = (float) Math.atan(Math.sqrt(1-Dum*Dum)/Dum);
			if (Dk < 0)
				Dk += 3.141593;
			Dk = (Dk + .0157f)/1.57f;
			if (T[month] >= 80)
				PE =  (Math.sin(T[month]/57.3-.166)-.76)*Dk;
			else {
				Dum =  Math.log(T[month]-32);
				PE = (Math.exp(-3.863233+A*1.715598-A*Math.log(I)+A*Dum))*Dk;
			}
		}

		if(month == 1){
			if(year%400 == 0)
				PE=PE*29;
			else if(year%4 == 0 && year%100 != 0)
				PE=PE*29;
			else
				PE=PE*28;
		}
		else
			PE=PE*Days[month];
	}

	private void CalcWBCoef() {
		Alpha = new double[num_of_periods];
		Beta = new double[num_of_periods];
		Gamma = new double[num_of_periods];
		Delta = new double[num_of_periods];
		int i;
		for (i = 0; i < num_of_periods; i++) {
			//calculate alpha:
			if(PESum[i] != 0.0)
				Alpha[i] = ETSum[i] / PESum[i];
			else if(ETSum[i] == 0.0)
				Alpha[i] = 1.0f;
			else
				Alpha[i] = 0.0f;

			//calculate beta:
			if(PRSum[i] != 0.0)
				Beta[i] = RSum[i] / PRSum[i];
			else if(RSum[i] == 0.0)
				Beta[i] = 1.0f;
			else
				Beta[i] = 0.0f;

			//calculate gamma:
			if(ROSum[i] > PROSum[i])
				Gamma[i] = 1.0f;
			else if(PROSum[i] != 0.0)
				Gamma[i] = ROSum[i] / PROSum[i];
			else if(ROSum[i] == 0.0)
				Gamma[i] = 1.0f;
			else
				Gamma[i] = 0.0f;

			//calculate delta:
			if(PLSum[i] != 0.0)
				Delta[i] = LSum[i] / PLSum[i];
			else 
				Delta[i] = 0.0f;

		}
	}

	//-----------------------------------------------------------------------------
	// This function uses previously calculated sums to find K, which is the 
	// weighting factor used in the Palmer Index calculation.
	//-----------------------------------------------------------------------------
	private void calcK() {
		double sums;        //used to calc k
		k = new double[num_of_periods];

		// Calculate k, which is K', or Palmer's second approximation of K

		for(int per = 0; per < num_of_periods; per++){
			if(PSum[per] + LSum[per] == 0)
				sums = 0;//prevent div by 0
			else
				sums = (PESum[per] + RSum[per] + ROSum[per]) / (PSum[per] + LSum[per]);

			if(D[per] == 0)
				k[per] = 0.5f;//prevent div by 0
			else
				k[per] = ((1.5) * Math.log10((sums + 2.8) / D[per]) + 0.5);
		}
	}

	private void calcd(){
		double p, PE,PR, PRO, PL; 
		double D_sum[]= new double[annualPeriods];
		double DSAct[]= new double[annualPeriods];
		double SPhat[]= new double[annualPeriods];
		dvalue= new double[totalyears][annualPeriods];
		D = new double[annualPeriods];
		// read the potential array
		for(int years =0; years<totalyears; years++){
			for(int per=0; per< num_of_periods; per++){
				
				p = potential[years][per][0];
				PE = potential[years][per][1];
				PR = potential[years][per][2];
				PRO = potential[years][per][3];
				PL = potential[years][per][4];
				if((p==DataType.MISSING)||(PE==DataType.MISSING)||(PR==DataType.MISSING)||(PRO==DataType.MISSING)||(PL==DataType.MISSING)){
					dvalue[years][per]=DataType.MISSING;
				} else if((p==DataType.NONEXISTANT)||(PE==DataType.NONEXISTANT)||(PR==DataType.NONEXISTANT)||(PRO==DataType.NONEXISTANT)||(PL==DataType.NONEXISTANT)){
					dvalue[years][per]=DataType.NONEXISTANT;
				} else if((p==DataType.OUTSIDE_OF_RANGE)||(PE==DataType.OUTSIDE_OF_RANGE)||(PR==DataType.OUTSIDE_OF_RANGE)||(PRO==DataType.OUTSIDE_OF_RANGE)||(PL==DataType.OUTSIDE_OF_RANGE)){
					dvalue[years][per]=DataType.OUTSIDE_OF_RANGE;
				} else if((p==DataType.OUTSIDE_OF_REQUEST_RANGE)||(PE==DataType.OUTSIDE_OF_REQUEST_RANGE)||(PR==DataType.OUTSIDE_OF_REQUEST_RANGE)||(PRO==DataType.OUTSIDE_OF_REQUEST_RANGE)||(PL==DataType.OUTSIDE_OF_REQUEST_RANGE)){
					dvalue[years][per]=DataType.OUTSIDE_OF_REQUEST_RANGE;
				} else if((p==DataType.ERROR_RESULT)||(PE==DataType.ERROR_RESULT)||(PR==DataType.ERROR_RESULT)||(PRO==DataType.ERROR_RESULT)||(PL==DataType.ERROR_RESULT)){
					dvalue[years][per]=DataType.ERROR_RESULT;
				}else{
					Phat=(Alpha[per]*PE)+(Beta[per]*PR)+(Gamma[per]*PRO)-(Delta[per]*PL);
					d=p - Phat;
					dvalue[years][per]=d;
					if(d < 0.0)
						D_sum[per] += -(d);
					else
						D_sum[per] += d;


					// The statistical values are updated
					DSAct[per] += d;
					DSSqr[per] += d*d;
					SPhat[per] += Phat;
				}
			}
		}
		for(int i=0; i<annualPeriods; i++){
			D[i]=D_sum[i]/totalyears;
		}

	}

	private void CalcZ(){
		int year, per;
		float dtemp;
		LinkedList tempZ,tempPer, tempyear;
		ZIND=  new LinkedList();
		PeriodList= new LinkedList();
		YearList= new LinkedList();


		DKSum = 0.0f; //sum of all D[i] and k[i]; used to calc K
		for(per=0; per<num_of_periods; per++){
			DKSum += D[per] * k[per];
		}
		for(int years =0; years<totalyears; years++){
			for( per=0; per< num_of_periods; per++){

				d=dvalue[years][per];
				K=k[per];
				// per= per/period_length;
				PeriodList.addFirst(per);
				YearList.addFirst(years);
				if(d==DataType.MISSING){
					Z=DataType.MISSING;
				}else if(d==DataType.ERROR_RESULT){
					Z= DataType.ERROR_RESULT;
				}else if(d==DataType.NONEXISTANT){
					Z= DataType.NONEXISTANT;
				}else if(d==DataType.OUTSIDE_OF_RANGE){
					Z= DataType.OUTSIDE_OF_RANGE;
				}else if(d==DataType.OUTSIDE_OF_REQUEST_RANGE){
					Z= DataType.OUTSIDE_OF_REQUEST_RANGE;
				}else{
					Z=d*K;
				}
				ZIND.addFirst(Z);
			}
		}
	}

	private void CalcDurFact(CalcDurFactClass dur, int sign){
		//calculates m and b, which are used to calculated X(i) 
		//based on the Z index.  These constants will determine the 
		//weight that the previous PDSI value and the current Z index 
		//will have on the current PDSI value.  This is done by finding 
		//several of the driest periods at this station and assuming that
		//those periods represents an extreme drought.  Then a linear 
		//regression is done to determine the relationship between length
		//of a dry (or wet) spell and the accumulated Z index during that
		//same period.  
		//
		//it appears that there needs to be a different weight given to 
		//negative and positive Z values, so the variable 'sign' will 
		//determine whether the driest or wettest periods are looked at.

		int num_list = 10;
		double sum[]= new double[10];
		int length[]= new int[10];
		int i;

		if(Weekly){
			if(period_length==1){
				length[0]=13;
				length[1]=26;
				length[2]=39;
				length[3]=52;
				length[4]=78;
				length[5]=104;
				length[6]=130;
				length[7]=156;
				length[8]=182;
				length[9]=208;
			}
			else if(period_length==2){
				length[0]=6;
				length[1]=13;
				length[2]=19;
				length[3]=26;
				length[4]=39;
				length[5]=52;
				length[6]=65;
				length[7]=78;
				length[8]=91;
				length[9]=104;
			}
			else if(period_length==4){
				length[0]=3;
				length[1]=6;
				length[2]=10;
				length[3]=13;
				length[4]=20;
				length[5]=26;
				length[6]=33;
				length[7]=39;
				length[8]=46;
				length[9]=52;
			}
			else if(period_length==13){
				length[0]=2;
				length[1]=3;
				length[2]=4;
				length[3]=5;
				length[4]=6;
				length[5]=8;
				length[6]=10;
				length[7]=12;
				length[8]=14;
				length[9]=16;
			}
		}
		else{
			length[0]=3;
			length[1]=6;
			length[2]=9;
			length[3]=12;
			length[4]=18;
			length[5]=24;
			length[6]=30;
			length[7]=36;
			length[8]=42;
			length[9]=48;
		}
		for(i=0; i< num_list;i++){
			sum[i]=get_Z_sum(length[i],sign);
		}
		LeastSquares(length, sum, num_list, sign,dur);
	
		//now divide m and b by 4 or -4 becuase that line represents
		//pdsi of either 4.0 or -4.0
		dur.setMl(dur.getMl()/(sign*4));
		dur.setBl(dur.getBl()/(sign*4));

	}

	private void LeastSquares(int [] x, double[] y, int n, int sign,CalcDurFactClass dur ){
		double sumX, sumX2, sumY, sumY2, sumXY;
		double SSX, SSY, SSXY;
		double xbar, ybar;

		double correlation = 0;
		double c_tol = 0.85;

		double max = 0;
		double max_diff = 0;
		int max_i = 0;

		double this_x, this_y;
		int i;
		double slope, intercept=0;

		sumX = 0; sumY = 0; sumX2 = 0; sumY2 = 0; sumXY = 0;
		for(i = 0; i < n; i++){
			this_x = x[i];
			this_y = y[i];

			sumX += this_x;
			sumY += this_y;
			sumX2 += this_x * this_x;
			sumY2 += this_y * this_y;
			sumXY += this_x * this_y;
		}

		xbar = sumX/n;
		ybar = sumY/n;

		SSX = sumX2 - (sumX * sumX)/n;
		SSY = sumY2 - (sumY * sumY)/n;
		SSXY = sumXY - (sumX * sumY)/n;

		correlation = SSXY / (Math.sqrt(SSX) * Math.sqrt(SSY));

		i = n - 1;
		while((sign*correlation) < c_tol && i > 3){
			//when the correlation is off, it appears better to 
			//take the earlier sums rather than the later ones.
			this_x = x[i];
			this_y = y[i];

			sumX -= this_x;
			sumY -= this_y;
			sumX2 -= this_x * this_x;
			sumY2 -= this_y * this_y;
			sumXY -= this_x * this_y;

			SSX = sumX2 - (sumX * sumX)/i;
			SSY = sumY2 - (sumY * sumY)/i;
			SSXY = sumXY - (sumX * sumY)/i;

			xbar = sumX/i;
			ybar = sumY/i;

			correlation = SSXY / (Math.sqrt(SSX) * Math.sqrt(SSY));
			i--;
		}
		slope = SSXY / SSX;
		n = i+1;
		for(i=0; i < n; i++){
			if(sign*(y[i] - slope * x[i]) > sign*max_diff){
				max_diff = y[i] - slope * x[i];
				max_i = i;
				max = y[i];
			}
		}
		intercept = max - slope*x[max_i];
		dur.setBl(intercept);
		dur.setMl(slope);
	}

	private double get_Z_sum(int length, int sign) {
		double sum, max_sum, z;
		LinkedList list_to_sum, list_of_sums = null;
		list_to_sum= new LinkedList();
		list_of_sums= new LinkedList();
		double highest_reasonable;
		double percentile = 0;
		float reasonable_tol = 1.25f;
		LinkedList tempZ = new LinkedList(); // copy list

		for (Iterator i=ZIND.iterator(); i.hasNext();)
		{
			tempZ.add(i.next());
		}
		sum = 0;

		//first fill the list to be summed
		for(int i = 0; i < length; i++){
			if(tempZ.isEmpty()){
				i = length;
			}
			else {
				z = (Double) (tempZ.removeLast());
				if(z != DataType.MISSING &&z != DataType.NONEXISTANT && z != DataType.ERROR_RESULT && z != DataType.OUTSIDE_OF_RANGE&& z != DataType.OUTSIDE_OF_REQUEST_RANGE){
					sum += z;
					list_to_sum.addFirst(z);
				}
				else{
					i--;
				}
			}
		}

		//now for each remaining Z value,
		//recalculate the sum based on last value in the
		//list to sum and the next Z value
		max_sum = sum;
		list_of_sums.addFirst(sum);
		while(!tempZ.isEmpty()){
			z = (Double)tempZ.removeLast();
			if(z != DataType.MISSING&&z != DataType.NONEXISTANT && z != DataType.ERROR_RESULT && z != DataType.OUTSIDE_OF_RANGE&& z != DataType.OUTSIDE_OF_REQUEST_RANGE){
				sum -= (Double)list_to_sum.removeLast();
				sum += z;
				list_to_sum.addFirst(z);
				list_of_sums.addFirst(sum);
			}
			if(sign * sum > sign * max_sum)
				max_sum = sum;
		}

		//highest reasonable is the highest (or lowest)
		//value that is not due to some freak anomaly in the
		//data.
		//"freak anomaly" is defined as a value that is either
		//   1) 25% higher than the 98th percentile
		//   2) 25% lower than the 2nd percentile
		//
		highest_reasonable = 0; 
		if(sign == 1)
			percentile = safe_percentile(.98f,list_of_sums);
		if(sign == -1)
			percentile = safe_percentile(.02f,list_of_sums);

		while(!list_of_sums.isEmpty()){
			sum = (Double)list_of_sums.removeLast();
			if(sign * sum > 0 ){
				if( (sum / percentile) < reasonable_tol ) {
					if(sign * sum > sign * highest_reasonable )
						highest_reasonable = sum;
				}
			}
		}

		if(sign == -1)
			return max_sum;
		else if(sign == 1)
			//return max_sum;
		return highest_reasonable;
		else
			return DataType.MISSING;
	}//end of get_Z_sum()

	//Linkedlist functions
	private double safe_percentile(float percentage,LinkedList list_to_sum){
		LinkedList safelist = new LinkedList();
		for (Iterator i=list_to_sum.iterator(); i.hasNext();)
		{	//double z = (Double)i.next();
			double z= new Double(String.valueOf(i.next()));
			if(z != DataType.MISSING &&z != DataType.NONEXISTANT&& z != DataType.ERROR_RESULT && z != DataType.OUTSIDE_OF_RANGE&& z != DataType.OUTSIDE_OF_REQUEST_RANGE){
				safelist.addFirst(z);
			}
		}
		return percentile(percentage, safelist.size(), safelist);
	}

	private double percentile(float percentage, int size,LinkedList list_to_sum){
		int k;
		//the argument may not be in correct demical 
		//representation of a percentage, that is,
		//it may be a whole number like 25 instead of .25
		if(percentage > 1)
			percentage = percentage / 100;
		k = (int)(percentage * size);
		return kthLargest(k, size,list_to_sum);

	}

	private double kthLargest(int k, int size,LinkedList list_to_sum){
		if(k < 1 || k > size)
			return DataType.MISSING;
		else if(k == 1)
			return minList(list_to_sum);
		else if(k == size) 
			return maxList(list_to_sum);
		else{
			LinkedList temp = new LinkedList();
			for (Iterator i=list_to_sum.iterator(); i.hasNext();)
			{
				temp.add(i.next());
			}
			Collections.sort(temp);
			return (Double)temp.get(k-1);

		}

	}

	private double minList(LinkedList list_to_sum){
		//Collections.sort(list_to_sum);
		LinkedList temp = new LinkedList();
		for (Iterator i=list_to_sum.iterator(); i.hasNext();)
		{
			temp.add(i.next());
		}
		Collections.sort(temp);
		return (Double) temp.getFirst();
	}
	private double maxList(LinkedList list_to_sum){
		//Collections.sort(list_to_sum);
		LinkedList temp = new LinkedList();
		for (Iterator i=list_to_sum.iterator(); i.hasNext();)
		{
			temp.add(i.next());
		}
		Collections.sort(temp);
		return (Double) temp.getLast();
	}
	class CalcDurFactClass{

		private double bl;
		public double getBl() {
			return bl;
		}
		public void setBl(double bl) {
			this.bl = bl;
		}
		public double getMl() {
			return ml;
		}
		public void setMl(double ml) {
			this.ml = ml;
		}
		private double ml;

	}

	class ChooseNew{
		double newX;
		public double getNewX() {
			return newX;
		}
		public void setNewX(double newX) {
			this.newX = newX;
		}
		public double getNewX1() {
			return newX1;
		}
		public void setNewX1(double newX1) {
			this.newX1 = newX1;
		}
		public double getNewX2() {
			return newX2;
		}
		public void setNewX2(double newX2) {
			this.newX2 = newX2;
		}
		public double getNewX3() {
			return newX3;
		}
		public void setNewX3(double newX3) {
			this.newX3 = newX3;
		}
		double newX1;
		double newX2;
		double newX3;

	}
}
