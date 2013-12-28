package edu.unl.act.rma.firm.drought.index;
import java.util.ArrayList;
import java.util.List;

import edu.unl.act.rma.firm.core.DataType;
import edu.unl.act.rma.firm.core.InvalidStateException;
import edu.unl.act.rma.firm.core.LogManager;
import edu.unl.act.rma.firm.core.Logger;
import edu.unl.act.rma.firm.core.Loggers;
import edu.unl.act.rma.firm.drought.NsmCompleteDTO;
import edu.unl.act.rma.firm.drought.NsmSummaryDTO;
import edu.unl.act.rma.firm.drought.SoilMoistureRegime;
import edu.unl.act.rma.firm.drought.SoilMoistureRegimeSubdivision;
import edu.unl.act.rma.firm.drought.SoilTemperatureRegime;


/**
 * 
 *
 */
public class NewhallSimulationModel {
	/**
	 * Logging static boject
	 */
	private static Logger LOG = LogManager.getLogger(Loggers.COMPONENT_LOG,
			NewhallSimulationModel.class);

	private int startYear;
	private int endYear;
	public NewhallSimulationModel(int startYear, int endYear){
		this.startYear = startYear;
		this.endYear=endYear;
	}
    
	
	private float[][] precipitationData;
	private float[][] temperatureData;
	private float lat;
	private float awc;
	double slotsum_old=0,slotsum_new=0;
	float LP, HP;
	int[] SMC=new int[360];  //  Soil moisture calendar
	int[] SMC_temp=new int[15];
	
	public void setData(float[][] precipitationData, float[][] temperatureData,float lat, float awc) throws InvalidStateException {
		this.precipitationData = precipitationData;
		this.temperatureData = temperatureData;
		this.lat = lat;
		this.awc = awc;
		int periodlen= precipitationData[0].length;
		if ( periodlen != 12 ) { 
			throw new InvalidStateException("The data must be organized into years with 12 period. The provided period was "+ periodlen);
		}
	}

	public NsmCompleteDTO computeCompleteNsm(){
		
		NsmCompleteDTO dto = computeComplete(temperatureData, 
				precipitationData, 
				startYear,
				endYear,
				lat,
				true, 
				awc);
		
		return dto;
	}
	
	public NsmSummaryDTO computeSummaryNsm(){
		
		NsmSummaryDTO dto = computeSummary(temperatureData, 
				precipitationData, 
				startYear,
				endYear,
				lat,
				true, 
				awc);
		
		return dto;
	}
	
		

	/**
	 * Runs the Newhall Simulation model on the given input data for single station.
	 */
	private NsmCompleteDTO computeComplete(
			float[][] montemp, float[][] monprec, int startYear, int endYear, float lat, boolean isNorthHemisphere, float awc){
		
		// Holds all year's calculation results
		
		NsmCompleteDTO dto = new NsmCompleteDTO();
		// Holds Soil regimes (SMR, SMRS, STR, along with other info)
		SoilRegimeSummary regimeSummary;
		// Holds single year's calculation
		NsmCompleteDTO.DataSet dataset;
		// Slots for water content modeling
		float slot[] = new float[64];
		// Soil moisture calendar
		int[] SMC = new int[360]; 
		// Soil temperature
		float[] soil_T;  
		// Counter for Subdivisions of soil moisture regime
		int[] STCs= new int[16]; for(int i = 0; i < 16; i++) STCs[i] = 0;
		// Counter for Soil moisture regime
		int[] STC= new int[6]; for(int i = 0; i < 6; i++) STC[i] = 0;
		// Potential evapotranspiration
		float[] monthly_PET;

		int end = endYear-startYear+1;
		float prec[][]= new float[end][12];
		float temp[][]= new float[end][12];
		awc=awc*25.4f;

		// Run simulation for all input years
		for(int i = 0; i < end; i++ ){
			
			// count the number of months with MISSING, ERROR, and OUT_OF_RANGE values
			int missing_months = 0;
			int error_months = 0;
			int oor_months = 0;
			int orn_months=0;
			int non_months=0;
						
			for (int j=0; j<12; j++)
			{
				if (monprec[i][j] == DataType.MISSING | montemp[i][j] == DataType.MISSING )
				{
					missing_months++;
				}
				if (monprec[i][j] == DataType.ERROR_RESULT | montemp[i][j] == DataType.ERROR_RESULT)
				{
					error_months++;
				}
				if (monprec[i][j] == DataType.OUTSIDE_OF_RANGE | montemp[i][j] == DataType.OUTSIDE_OF_RANGE )
				{
					oor_months++;
				}
				if (monprec[i][j] == DataType.OUTSIDE_OF_REQUEST_RANGE | montemp[i][j] == DataType.OUTSIDE_OF_REQUEST_RANGE )
				{
					orn_months++;
				}
				if (monprec[i][j] == DataType.NONEXISTANT | montemp[i][j] == DataType.NONEXISTANT )
				{
					non_months++;
				}
			}
			int total_bad_months = missing_months + error_months + oor_months+orn_months+non_months;
			
			for(int j=0; j<12; j++){
				prec[i][j]=(float) (monprec[i][j]* (25.4f));
			}
			for(int j=0; j<12; j++){
				temp[i][j]=(montemp[i][j]-32.0f)*5.0f/9.0f;
			}
			for (int cl=0;cl<64;cl++){
				slot[cl]=0;
			}
			
			// Calculate potential evapotranspiration
			monthly_PET = calculateEvapotranspiration(temp[i], isNorthHemisphere, lat);
			
			do{
				slotsum_old=slotsum_new;
				slotsum_new=0;

				for ( int j=0;j<12;j++){
					LP=prec[i][j]/2.0f;
					HP=prec[i][j]/2.0f;
					fillslot_LP(slot, monthly_PET[j],LP,awc/64.0f);
					fillslot_HP(slot,HP,awc/64.0f);
					fillslot_LP(slot, monthly_PET[j],LP,awc/64.0f);
				}

				for (int p=0;p<64;p++)
					slotsum_new=slotsum_new+slot[p];
			
			}while (Math.abs(slotsum_old-slotsum_new)>(0.01*slotsum_old));
		
			for (int j=0;j<12;j++)
			{
				//calculate the light and heavy precipitation
				LP=prec[i][j]/2.0f;
				HP=prec[i][j]/2.0f;
				//fill slots with light precipitation for the first half month
				fillslot_LP(slot,monthly_PET[j],LP,awc/64.0f,SMC_temp);
				//record the soil moisture condition for that period
				for (int k=0;k<15;k++)
					SMC[j*30+k]=SMC_temp[k];
				//fill slots with heavy precipitation at the end of 15th day
				fillslot_HP(slot,HP,awc/64.0f);
				//fill slots with light precipitation for the second half month
				fillslot_LP(slot,monthly_PET[j],LP,awc/64.0f,SMC_temp);
				//record the soil moisture condition for that period
				for (int k=0;k<15;k++)
					SMC[j*30+k+15]=SMC_temp[k];
			}
			
			// Calculate the soil temperature for the whole year
			soil_T = getSoilTemp(temp[i],isNorthHemisphere);

			// Determine the soil moisture and temperature calendar (update counters, biolgical window)
			regimeSummary = determine_SMR(soil_T, SMC, prec[i], monthly_PET,isNorthHemisphere, STCs, STC);
			
			// Add Calculated data to dataset
			dataset = new NsmCompleteDTO.DataSet();
			dataset.setMonthlyEvapotranspirations(wrap(monthly_PET));
			dataset.setMonthlyPrecipitations(wrap(prec[i]));
			dataset.setMonthlyTemperatures(wrap(temp[i]));

			dataset.setSoilMoistureCalendar(wrap(SMC));
			dataset.setSoilMoistureSlants(wrap(slot));
			dataset.setSoilTemperatureCalendar(wrap(soil_T));

			dataset.setTemperatureRegime(SoilTemperatureRegime.UNDEFINED);
			dataset.setMoistureRegimeSubdivision( SoilMoistureRegimeSubdivision.UNDEFINED);
						
			if(total_bad_months<=2){ // threshold of bad months is 2
				dataset.setConsecutiveDryDaysAfterSummerSolstice(regimeSummary.getConsecutiveDryDaysAfterSummerSolstice());
				dataset.setConsecutiveMoistDaysAfterWinterSolstice(regimeSummary.getConsecutiveMoistDaysAfterWinterSolstice());
				dataset.setMostConsecutiveMoistDays(regimeSummary.getMostConsecutiveMoistDays());
				dataset.setMostConsecutiveMoistDaysAbove8(regimeSummary.getMostConsecutiveMoistDaysAbove8());
				dataset.setTemperatureRegime(regimeSummary.getSTR());
				dataset.setMoistureRegimeSubdivision(regimeSummary.getSMRS());
				dataset.setTotalDryDays(regimeSummary.getTotalDryDays());
				dataset.setTotalDryDaysAbove5(regimeSummary.getTotalDryDaysAbove5());
				dataset.setTotalMediumDays(regimeSummary.getTotalMediumDays());
				dataset.setTotalMediumDaysAbove5(regimeSummary.getTotalMediumDaysAbove5());
				dataset.setTotalMoistDays(regimeSummary.getTotalMoistDays());
				dataset.setTotalMoistDaysAbove5(regimeSummary.getTotalMoistDaysAbove5());
			}
			dto.add(startYear+i, dataset);
		}
		return dto;
	}

	/**
	 * Runs the Newhall Simulation model on the given input data for single station.
	 */
	private NsmSummaryDTO computeSummary(
			float[][] montemp, float[][] monprec, int startYear, int endYear, float lat, boolean isNorthHemisphere, float awc){
		// Holds complete summary
		NsmSummaryDTO dto = new NsmSummaryDTO();
		// Hold summary statistics
		NsmSummary summary = new NsmSummary();
		// Holds Soil regimes (SMR, SMRS, STR, along with other info)
		SoilRegimeSummary regimeSummary;
		// Slots for water content modeling
		float slot[] = new float[64];
		// Soil moisture calendar
			// Soil temperature
		float[] soil_T;   
		// Counter for Subdivisions of soil moisture regime
		int[] STCs= new int[16]; for(int i = 0; i < 16; i++) STCs[i] = 0;
		// Counter for Soil moisture regime
		int[] STC= new int[6]; for(int i = 0; i < 6; i++) STC[i] = 0;
		// Potential evapotranspiration
		float[] monthly_PET;

		int end = endYear-startYear+1;

		// Run simulation for all input years
		awc=awc*25.4f;
		float prec[][]= new float[end][12];
		float temp[][]= new float[end][12];
		for(int i = 0; i < end; i++ ){
			// Calculate potential evapotranspiration
			// count the number of months with MISSING, ERROR, and OUT_OF_RANGE values
			int missing_months = 0;
			int error_months = 0;
			int oor_months = 0;
			int orn_months=0;
			int non_months=0;
		
			for (int j=0; j<12; j++)
			{
				if (monprec[i][j] == DataType.MISSING | montemp[i][j] == DataType.MISSING )
				{
					missing_months++;
				}
				if (monprec[i][j] == DataType.ERROR_RESULT | montemp[i][j] == DataType.ERROR_RESULT)
				{
					error_months++;
				}
				if (monprec[i][j] == DataType.OUTSIDE_OF_RANGE | montemp[i][j] == DataType.OUTSIDE_OF_RANGE )
				{
					oor_months++;
				}
				if (monprec[i][j] == DataType.OUTSIDE_OF_REQUEST_RANGE | montemp[i][j] == DataType.OUTSIDE_OF_REQUEST_RANGE )
				{
					orn_months++;
				}
				if (monprec[i][j] == DataType.NONEXISTANT | montemp[i][j] == DataType.NONEXISTANT )
				{
					non_months++;
				}
			}
			int total_bad_months = missing_months + error_months + oor_months+orn_months+non_months;
			
			for(int j=0; j<12; j++){
				prec[i][j]=(float) (monprec[i][j]* (25.4f));
			}
			for(int j=0; j<12; j++){
				temp[i][j]=(montemp[i][j]-32.0f)*5.0f/9.0f;
			}
			for (int cl=0;cl<64;cl++){
				slot[cl]=0;
			}
			
			// Calculate potential evapotranspiration
			monthly_PET = calculateEvapotranspiration(temp[i], isNorthHemisphere, lat);
			
			do{
				slotsum_old=slotsum_new;
				slotsum_new=0;

				for ( int j=0;j<12;j++){
					LP=prec[i][j]/2.0f;
					HP=prec[i][j]/2.0f;
					fillslot_LP(slot, monthly_PET[j],LP,awc/64.0f);
					fillslot_HP(slot,HP,awc/64.0f);
					fillslot_LP(slot, monthly_PET[j],LP,awc/64.0f);
				}

				for (int p=0;p<64;p++)
					slotsum_new=slotsum_new+slot[p];
			
			}while (Math.abs(slotsum_old-slotsum_new)>(0.01*slotsum_old));
		
			for (int j=0;j<12;j++)
			{
				//calculate the light and heavy precipitation
				LP=prec[i][j]/2.0f;
				HP=prec[i][j]/2.0f;
				//fill slots with light precipitation for the first half month
				fillslot_LP(slot,monthly_PET[j],LP,awc/64.0f,SMC_temp);
				//record the soil moisture condition for that period
				for (int k=0;k<15;k++)
					SMC[j*30+k]=SMC_temp[k];
				//fill slots with heavy precipitation at the end of 15th day
				fillslot_HP(slot,HP,awc/64.0f);
				//fill slots with light precipitation for the second half month
				fillslot_LP(slot,monthly_PET[j],LP,awc/64.0f,SMC_temp);
				//record the soil moisture condition for that period
				for (int k=0;k<15;k++)
					SMC[j*30+k+15]=SMC_temp[k];
			}
			
			// Calculate the soil temperature for the whole year
			soil_T = getSoilTemp(temp[i],isNorthHemisphere);

			// Determine the soil moisture and temperature calendar (update counters, biolgical window)
			regimeSummary = determine_SMR(soil_T, SMC, prec[i], monthly_PET,isNorthHemisphere, STCs, STC);

			if (total_bad_months >2) 
			{
				summary.updateTotalPrecipitation(prec[i],total_bad_months, monprec[i]);
				summary.updateTotalEvapotranspiration(monthly_PET,total_bad_months);
				regimeSummary.setSMRS(SoilMoistureRegimeSubdivision.UNDEFINED);
				summary.updateTotalAwb(0);
				summary.updateTotalMsd(0);
				summary.updateTotalDryDays(0);
				summary.updateTotalMediumDays(0);
				summary.updateTotalMoistDays(0);
				summary.updateTotalBio8(0);
				summary.incTotalYears();
			}
			else{
			// Update statics
				summary.updateTotalPrecipitation(prec[i],total_bad_months,monprec[i]);
				summary.updateTotalEvapotranspiration(monthly_PET,total_bad_months);
				summary.updateTotalAwb(summary.getLastPrecipitation()-summary.getLastEvapotranspiration());
				summary.updateTotalMsd(prec[i][5]+prec[i][6]+prec[i][7]-monthly_PET[5]-monthly_PET[6]-monthly_PET[7]);
				summary.updateTotalDryDays(regimeSummary.getTotalDryDaysAbove5());
				summary.updateTotalMediumDays(regimeSummary.getTotalMediumDaysAbove5());
				summary.updateTotalMoistDays(regimeSummary.getTotalMoistDaysAbove5());
				summary.updateTotalBio8(regimeSummary.getMostConsecutiveMoistDaysAbove8());
				summary.incTotalYears();
				// count good years
				summary.incGoodYears();
			}
				
				// Add year summary
			dto.add(startYear+i, new NsmSummaryDTO.DataSet(summary.getLastPrecipitation(),
					summary.getLastEvapotranspiration(),
					summary.getLastAwb(),
					summary.getLastMsd(),
					summary.getLastDryDays(),
					summary.getLastMediumDays(),
					summary.getLastMoistDays(),
					summary.getLastBio8(),
					regimeSummary.getSMRS()));
		}
		// Average of all years
		dto.setPrecipitationAvg(summary.getAvgPrecipitation());
		dto.setEvapotranspirationAvg(summary.getAvgEvapotranspiration());
		dto.setAwbAvg(summary.getAvgAwb()); 
		dto.setMsdAvg(summary.getAvgMsd());
		dto.setDryDaysAvg((int)summary.getAvgDryDays());
		dto.setMediumDaysAvg((int)summary.getAvgMediumDays());
		dto.setMoistDaysAvg((int)summary.getAvgMoistDays());
		dto.setBio8Avg((int)summary.getAvgBio8());

		// Add soil moisture regime subdivision summary
		int count;
		for (SoilMoistureRegimeSubdivision regime : SoilMoistureRegimeSubdivision.values()) {
			count = STCs[regime.getIndex()];
			dto.addRegimeSubdivisionSummary(new NsmSummaryDTO.RegimeSubdivisionSummary(count, (count * 100.0f) / (float) summary.getTotalYears(), regime));
		}

		// Add soil moisture regime summary
		for (SoilMoistureRegime regime : SoilMoistureRegime.values()) {
			count = STC[regime.getIndex()];
			dto.addRegimeSummary(new NsmSummaryDTO.RegimeSummary(count, (count * 100.0f) / (float) summary.getTotalYears(), regime));
		}

		return dto;
	}


	/**
	 * Initialize soil moisture slots. <br />
	 * Do loop does a test run to reach the state that the soil moisture 
	 * condition no longer changes over time and that condition will be 
	 * used as the initial condition
	 * 
	 * @param monthly_evapo calculated monthly potential evapotranspiration
	 * @param year's average monthly precipiation
	 * @param awc average water holding capacity
	 * @return initialize soil moisture slant array
	 */
	
	/**
	 * Fill the slot with light precipitation
	 * 
	 * @param slots  soil moisture slants
	 * @param PET  month's evapotranspiration
	 * @param precip  calculated light precipitation
	 * @param WHC  maximum water holding capacity
	 * 
	 * @return updated slots array (parameter updated)
	 */
	private void fillslot_LP(float slots[],float PET,float precip, float WHC){
		float diff, net, residue, dp_amount;
		int i;
		//	calculate the difference between the precipitation and evapotranspiration
		if ((WHC*64)<100.0)
			for (i=0;i<64;i++)
				NsmConstants.DP_S[i]=NsmConstants.DP_S_SANDY[i];

		diff=precip-PET;
		if (diff==0)
			return;
		else if (diff<0)
			net=diff*(-1f)/2.0f;
		else 
			net=diff/2.0f;

		//	if precipitation exceeds the PET, involks the accretion process.  
		// Otherwise, involks the depletion process
		if (diff>0){
			i=0;
			while (i<64){
				if(net<=0) break;
				//if slot is zero, just the fill the slot
				if (slots[i]==0){
					residue=net-WHC;
					if (residue>=0){
						slots[i]=WHC;
						net=net-WHC;
					} else {
						slots[i]=net;
						net=0;
					}
				} else if ((slots[i]>0)&&(slots[i]<WHC)) { //if slot is not zero, fill the difference
					residue=WHC-slots[i];
					if (net>=residue){
						slots[i]=WHC;
						net=net-residue;
					} else {
						slots[i]=slots[i]+net;
						net=0;
					}
				}
				i++;
			}
		} else {
			i=0;
			while (i<64){
				if(net<=0) break;
				//deplete the slot
				if (slots[NsmConstants.DP_S[i]-1]!=0){
					dp_amount=slots[NsmConstants.DP_S[i]-1]*NsmConstants.DP_R[i];
					net=net-dp_amount;
					if (net>=0)
						slots[NsmConstants.DP_S[i]-1]=0.0f;
					else
						slots[NsmConstants.DP_S[i]-1]=slots[NsmConstants.DP_S[i]-1]*Math.abs(net)/dp_amount;
				}
				i++;
			}
		}

	}

	/**
	 * Fill the slot with light precipitation and record the moisture condition.
	 * 
	 * @param slots  soil moisture slants
	 * @param PET  monthly evapotranspiration
	 * @param precip  light precipitation
	 * @param WHC  maximum water holding capacity
	 * @param SMR_r  soil moisture classification for half month
	 * 
	 * @return updated slots array (parameter updated)
	 */
	private void fillslot_LP(float slots[],float PET,float precip, float WHC, int SMC_r[]){
		//	NPE, RPEX, RPEX1, RPEX2 store the amount of moisture actually used to accreted or depleted slots
		float diff, net, residue, dp_amount, RPEX=0,RPEX1=0, RPEX2=0;

		int i,SMD1,SMD2,SMD3, Mid, End;
		SMD3=0;
		if ((WHC*64)<100.0)
			for (i=0;i<64;i++)
				NsmConstants.DP_S[i]=NsmConstants.DP_S_SANDY[i];

		diff=precip-PET;
		if (diff==0){
			SMD1=moisture_CON(slots[8],slots[16],slots[24]);
			for (i=0;i<15;i++)
				SMC_r[i]=SMD1;
			return;
		} else if (diff<0)
			net=diff*(-1)/2.0f;
		else 
			net=diff/2.0f;
		SMD1=moisture_CON(slots[8],slots[16],slots[24]);
		SMD2=SMD1;
		if (diff>0){
			i=0;
			while (i<64){
				if(net<=0) break;
				if (slots[i]==0){
					residue=net-WHC;
					if (residue>=0){
						slots[i]=WHC;
						net=net-WHC;
						RPEX=RPEX+WHC;
					} else {
						slots[i]=net;
						net=0;
						RPEX=RPEX+net;
					}
				} else if ((slots[i]>0)&&(slots[i]<WHC)){
					residue=WHC-slots[i];
					if (net>=residue)
					{
						slots[i]=WHC;
						net=net-residue;
						RPEX=RPEX+residue;
					} else {
						slots[i]=slots[i]+net;
						net=0;
						RPEX=RPEX+net;
					}
				}

				SMD3=moisture_CON(slots[8],slots[16],slots[24]);
				if (SMD2!=SMD3){
					if (RPEX1==0)
						RPEX1=RPEX;
					else
						RPEX2=RPEX;
					RPEX=0;
					SMD2=SMD3;
				}
				i++;
			}
		} else {
			i=0;
			while (i<64){
				if(net<=0) break;
				if (slots[NsmConstants.DP_S[i]-1]!=0){
					dp_amount=slots[NsmConstants.DP_S[i]-1]*NsmConstants.DP_R[i];
					net=net-dp_amount;
					if (net>=0){
						slots[NsmConstants.DP_S[i]-1]=0.0f;
						RPEX=RPEX+dp_amount;
					} else {
						slots[NsmConstants.DP_S[i]-1]=slots[NsmConstants.DP_S[i]-1]*Math.abs(net)/dp_amount;
						RPEX=RPEX+Math.abs(net);
					}
				}
				SMD3=moisture_CON(slots[8],slots[16],slots[24]);
				if (SMD2!=SMD3){
					if (RPEX1==0)
						RPEX1=RPEX;
					else
						RPEX2=RPEX;
					RPEX=0;
					SMD2=SMD3;
				}
				i++;
			}
		}

		//	determine the soil moisture classification from the soil moisture conditions
		if (Math.abs(SMD3-SMD1)==2){
			Mid=(int)(15.0*RPEX1/Math.abs(diff/2.0));
			End=(int)(15.0*RPEX2/Math.abs(diff/2.0));
			for (i=0;i<Mid;i++)
				SMC_r[i]=SMD1;
			for (i=Mid;i<Mid+End;i++)
				SMC_r[i]=2;
			for (i=End;i<15;i++)
				SMC_r[i]=SMD3;
		} else if (Math.abs(SMD3-SMD1)==1){
			Mid=(int)(15.0*RPEX1/Math.abs(diff/2.0));
			for (i=0;i<Mid;i++)
				SMC_r[i]=SMD1;
			for (i=Mid;i<15;i++)
				SMC_r[i]=SMD3;
		} else {
			for (i=0;i<15;i++)
				SMC_r[i]=SMD1;
		}
	}

	/**
	 * Fill the slot with heavy precipitation. <br />
	 * Note: The process is basically the same as fill slots with the light precipitation.
	 * 
	 * @param slots  soil moisture slants
	 * @param precip  heavy precipitation
	 * @param WHC  maximum water holding capacity
	 * 
	 * @return updated slots array (parameter updated)
	 */
	private void fillslot_HP(float slots[],float precip,float WHC)
	{
		float residue;
		int i=0;

		while (i<64){
			if(precip<=0) break;
			if (slots[i]==0){
				residue=precip-WHC;
				if (residue>=0){
					slots[i]=WHC;
					precip=precip-WHC;
				} else {
					slots[i]=precip;
					precip=0;
				}
			} else if ((slots[i]>0)&&(slots[i]<WHC)){
				residue=WHC-slots[i];
				if (precip>=residue){
					slots[i]=WHC;
					precip=precip-residue;
				} else {
					slots[i]=slots[i]+precip;
					precip=0;
				}
			}
			i++;
		}
	}

	/**
	 * Determine the soil moisture condition
	 * 
	 * @param a  slots[8]
	 * @param b  slots[16] 
	 * @param c  slots[24]
	 * 
	 * @return the soil moisture condition
	 */
	private int moisture_CON(double a, double b, double c)

	{
		if ((a==0.0)&&(b==0.0)&&(c==0.0))
			return 1;
		else if ((a!=0.0)&&(b!=0.0)&&(c!=0.0))
			return 3;
		else
			return 2;
	}

	/**
	 * Calculate the soil temperature for one year
	 * 
	 * @param monthlyTemps - average monthly temperature
	 * @param isNorthHemisphere - true if data for North Hemisphere, false else
	 * @return year's soil temperature (for each day)
	 */
	private float[] getSoilTemp(float monthlyTemps[], boolean isNorthHemisphere)
	{
		float year_AVG=0, A;
		//CC: average temperature adjustment
		//CD: amplitude adjustment
		//lag1, lag2: phase lag adjustment
		float lag1, lag2, w;
		float[] soil_t_temp=new float[360];
		float[] soil_t=new float[360];
		int i;
		float summer_AVG,winter_AVG;

		lag1=lag2=NsmConstants.AST_PHASE_SHIFT;

		for (i=0;i<12;i++)
			year_AVG+=monthlyTemps[i];

		year_AVG=year_AVG/12.0f+NsmConstants.AST_DIFF;

		summer_AVG=(monthlyTemps[5]+monthlyTemps[6]+monthlyTemps[7])/3.0f;

		winter_AVG=(monthlyTemps[0]+monthlyTemps[1]+monthlyTemps[11])/3.0f;

		//		A=fabs(T[6]-T[0])/2.0*CD;

		A=Math.abs(summer_AVG-winter_AVG)/2.0f*NsmConstants.AST_AMPLITUDE_CHANGE;

		w=2.0f*3.1415926f/360.0f;

		for(i=0;i<360;i++)
			soil_t_temp[i]=0;

		for(i=0;i<360;i++)
		{
			if (isNorthHemisphere)
			{
				if ((i>=90)&&(i<270)) 
					soil_t_temp[i]=year_AVG+A*(float)Math.sin(w*(i+lag2));
				else
					soil_t_temp[i]=year_AVG+A*(float)Math.sin(w*(i+lag1));
			}
			else
			{
				if ((i>=90)&&(i<270)) 
					soil_t_temp[i]=year_AVG+A*(float)Math.cos(w*(i+lag2));
				soil_t_temp[i]=year_AVG+A*(float)Math.cos(w*(i+lag1));
			}
		}

		for (i=0;i<134;i++)
			soil_t[i]=soil_t_temp[i+226];

		for (i=134;i<360;i++)
			soil_t[i]=soil_t_temp[i-134];

		return soil_t;
	}

	/**
	 * Calculates the potential evapotranspiration for a 
	 * single year using the THORNTHWAITE EVAPOTRANSPIRATION Method
	 * 
	 * @param monthlyTemps - average monthly temperature
	 * @param lat - latitude
	 * @return calculated evapotranspiration for year
	 */
	private float[] calculateEvapotranspiration(float[] monthlyTemps, boolean isNorthHemisphere, float lat){
		float SWI, A;
		int KI, KL, NROW, i;
		int KK=0;
		float[] APE = new float[12];
		float[] MWI=new float[12];
		float[] UPE=new float[12];
		float[][] FN=new float[31][12];
		float[][] FS=new float[13][12];

		float CF;

		int lFs1=NsmConstants.FS1.length;
		for(i=0;i<lFs1;i++)
			FS[(i/12)][i%12]=NsmConstants.FS1[i];
		int lFn1=NsmConstants.FN1.length;
		for(i=0;i<lFn1;i++)
			FN[(i/12)][i%12]=NsmConstants.FN1[i];
		//	   UPE  UNADJUSTED POT.EVAP.
		//	   APE  ADJUSTED EVAPOTRANSP.
		//	   MWI  MONTHLY HEAT INDEX
		for (i=0;i<12;i++){
			UPE[i]=0;
			APE[i]=0;
			MWI[i]=0;
		}
		//	  CALCULATE HEAT INDEX
		A=0;
		SWI=0;

		for (i=0;i<12;i++){
			if (monthlyTemps[i]>0) 
				MWI[i]=(float)(Math.pow(monthlyTemps[i]/5.0,1.514));
		}

		for (i=0;i<12;i++)
			SWI=SWI+MWI[i];
		A=(0.000000675f*(float)Math.pow(SWI,3))-(0.0000771f*(float)Math.pow(SWI,2))+(0.01792f*SWI)+0.49239f;


		//	 CALCULATE UNADJ.POT.EVAP.
		for (i=0;i<12;i++){
			if (monthlyTemps[i]>=38)
				UPE[i]=185;
			else if ((monthlyTemps[i]<38)&&(monthlyTemps[i]>=26.5)){
				for (KI=0;KI<24;KI++){
					KL=KI+1;
					KK=KI;
					if ((monthlyTemps[i]>=NsmConstants.ZT[KI])&&(monthlyTemps[i]<NsmConstants.ZT[KL]))
						break;
				}

				UPE[i]=NsmConstants.ZPE[KK];
			}
			else if ((monthlyTemps[i]>0)&&(monthlyTemps[i]<=26.5))
				UPE[i]=16f*(float)Math.pow(10*monthlyTemps[i]/SWI,A);
		}

		if (isNorthHemisphere){ //	  ADJUST FOR NORTHERN HEMISPHERE
			NROW=0;
			for (i=0;i<31;i++)
			{
				if (lat < NsmConstants.RN[i]) 
					break;
				NROW++;
			}

			for (i=0;i<12;i++)
			{
				if (UPE[i]>=0)
					APE[i]=UPE[i]*FN[NROW-1][i];
			}
		} else { //	   ADJUST FOR SOUTH HEMISPHERE
			NROW=0;
			for (i=0;i<13;i++){
				if (lat<NsmConstants.RS[i])
					break;
				NROW++;
			}

			if (NROW==0){
				for(i=0;i<12;i++)
				{
					if (UPE[i]>0)
					{
						CF=(FS[0][i]-FN[0][i])*(lat*60.0f)/300f;
						CF=CF+FN[0][i];
						APE[i]=UPE[i]*CF;
					}
				}
			} else {
				for(i=0;i<12;i++){
					if (NROW>=12)
						CF=FS[12][i];
					else {	
						CF=((FS[NROW][i]-FS[NROW-1][i])*(lat-NsmConstants.RS[NROW-1])/((NsmConstants.RS[NROW]-NsmConstants.RS[NROW-1])*60.0f));
						CF=FS[NROW-1][i]+CF;
					}

					APE[i]=UPE[i]*CF;

				}
			}
		}

		return APE;
	}

	/**
	 * Determine the moisture and temperature regime. Update the
	 * SMR and SMRS counters based determined regime. Biological
	 * Window set as well.
	 * 
	 * @param soil_t - soil temperature
	 * @param smc_t - soil moisture calendar
	 * @param precip - average monthly precipitation
	 * @param PET - monthly evapotranspiration
	 * @param isNorthHemisphere - true if data for North Hemisphere, false else
	 * @param BW - biological window
	 * @param SMRS - Subdivisions of soil moisture regime counter
	 * @param SMR - Soil moisture regime counter
	 * 
	 * @return updated SMRS & SMR counters, and SoilRegimeSummary
	 */
	private SoilRegimeSummary determine_SMR(
			float soil_t[], int smc_t[],float precip[],float PET[], boolean isNorthHemisphere, 
			int SMRS_Counter[], int SMR_Counter[])
	{
		int one=0,two=0,three=0,four=0,five=0,six=0,seven=0,eight=0,nine=0;
		int i;
		//		t5:number of days that soil temperature is above 5 degree C
		//		t8:number of days that soil temperature is above 8 degree C
		int t5=0,t8=0;
		//		dd:number of days that MCS is dry;
		//		dm:number of days that MCS is moist;
		//		db:number of days that MCS is partly dry and partly moist;
		int dd=0,dm=0,db=0;
		//		tm8:number of consecutive days that soil temperature is over 8 C and MCS is partly moist or moist for 90;
		//		td:number of days that MCS is dry when soil temperature is more over 5 C
		int td=0,tb=0,tm=0,tm8=0,tmy=0,iso=0; 
		//		st_sum:mean soil temperature for one year
		//		winter_avg: sum of soil temperature for the winter
		//		summer_avg: sum of soil temperature for the summer
		//		sd:number of consecutive days that MCS is dry within four month following the summer solstice
		//		snd:number of consecutive days that MCS is not completely dry within four month following the summer solstice
		//		wm:number of consecutive days that MCS is moist within four month following the winter solstice
		//		comd:number of consecutive days that MCS is completely dry or partly dry in one year
		float st_annual_mean=0,winter_avg=0,summer_avg=0,swdiff;
		int sd=0,snd=0,wm=0,comd=0,max;
		SoilMoistureRegimeSubdivision SMRS_t = SoilMoistureRegimeSubdivision.UNDEFINED;
		SoilMoistureRegime SMR_t = SoilMoistureRegime.UNDEFINED;
		SoilTemperatureRegime STR_t = SoilTemperatureRegime.UNDEFINED;
		// Biological window
		int BW[] = new int[10];
		// Holds all summary statistics 
		SoilRegimeSummary summary = new SoilRegimeSummary();

		for (i=0;i<360;i++){
			if (smc_t[i]==1)
				dd++;
			else if (smc_t[i]==2)
				db++;
			else if (smc_t[i]==3)
				dm++;
		}

		BW[0]=dd;
		BW[1]=db;
		BW[2]=dm;

		for (i=0;i<360;i++){
			if (soil_t[i]>5)
				t5++;
			if (soil_t[i]>8)
				t8++;
		}

		//		Condition No 1. 	
		for (i=0;i<360;i++){
			if ((smc_t[i]==1)&&(soil_t[i]>5))
				td++;
			if ((smc_t[i]==2)&&(soil_t[i]>5))
				tb++;
			if ((smc_t[i]==3)&&(soil_t[i]>5))
				tm++;
		}

		BW[3]=td;
		BW[4]=tb;
		BW[5]=tm;

		if (td>=(t5/2))
			one=1;

		//		Condition	No 2. 
		max=0;
		for (i=0;i<360;i++){
			if ((soil_t[i]>8)&&(smc_t[i]!=1))
				tm8++;
			else{
				if (tm8>max)
					max=tm8;
				tm8=0;
			}
		}
		if (tm8<max)
			tm8=max;

		max=0;
		for (i=0;i<360;i++){
			if (smc_t[i]!=1)
				tmy++;
			else{
				if (tmy>max)
					max=tmy;
				tmy=0;
			}
		}
		if (tmy<max)
			tmy=max;

		BW[6]=tmy;
		BW[7]=tm8;

		if (tm8>=90)
			two=1;

		//		Condition	No 3. 
		for (i=0;i<360;i++)
			st_annual_mean+=soil_t[i];
		st_annual_mean=st_annual_mean/360.0f;

		if (st_annual_mean<22)
			three=1;

		//		Condition	No 4.
		for (i=150;i<240;i++)
			summer_avg+=soil_t[i];
		for (i=0;i<60;i++)
			winter_avg+=soil_t[i];
		for (i=330;i<360;i++)
			winter_avg+=soil_t[i];

		summer_avg=summer_avg/90.0f;
		winter_avg=winter_avg/90.0f;
		swdiff=Math.abs(summer_avg-winter_avg);

		if (swdiff>=5)
			four=1;
		else
			iso=1;

		//		Condition	No. 5 for north hemisphere and 6 for south hemisphere.
		max=0;
		for (i=180;i<300;i++){
			if (isNorthHemisphere){
				if (smc_t[i]==1)
					sd++;
				else {
					if (sd>max)
						max=sd;
					sd=0;
				}

			} else {
				if (smc_t[i]==3)
					wm++;
				else {
					if (wm>max)
						max=wm;
					wm=0;
				}
			}
		}

		if (isNorthHemisphere) {
			if (sd<max)
				sd=max;
			BW[8]=sd;
			if (sd>=45)
				five=1;
		} else {
			if (wm<max)
				wm=max;
			BW[9]=wm;
			if (wm>=45)
				six=1;
		}

		//		Condition No.6 for north and 5 for south
		max=0;
		for (i=0;i<120;i++){
			if (isNorthHemisphere){
				if (smc_t[i]==3)
					wm++;
				else {
					if (wm>max)
						max=wm;
					wm=0;
				}
			}
			else {
				if (smc_t[i]==1)
					sd++;
				else {
					if (sd>max)
						max=sd;
					sd=0;
				}
			}
		}

		if (isNorthHemisphere){	
			if (wm<max)
				wm=max;
			BW[9]=wm;
			if (wm>=45)
				six=1;
		} else {
			if (sd<max)
				sd=max;
			BW[8]=sd;
			if (sd>=45)
				five=1;
		}

		//		Condition No.7
		max=0;
		for (i=0;i<360;i++){
			if ((smc_t[i]==1)||(smc_t[i]==2))
				comd++;
			else {
				if (comd>max)
					max=comd;
				comd=0;
			}
		}
		if (comd<max)
			comd=max;
		if (comd>=90)
			seven=1;

		//		Condition No.8
		if (st_annual_mean>=8)
			eight=1;

		//		Condition No.9
		for (i=0;i<12;i++)
			if ((precip[i]-PET[i])<=0)
				break;
		if (i==12)
			nine=1;

		//		determining soil moisture regime
		if (one==1 && two!=1){
			SMR_t = SoilMoistureRegime.ARIDIC;
			for (i=0;i<360;i++)
				if (smc_t[i]!=1)
					break;
			if (i==360)
				SMRS_t= SoilMoistureRegimeSubdivision.EXTREME_ARIDIC;
			else if (tm8<=45)
				SMRS_t= SoilMoistureRegimeSubdivision.TYPIC_ARIDIC;
			else
				SMRS_t= SoilMoistureRegimeSubdivision.WEAK_ARIDIC;
		}
		else if(three==1 && four==1 && five==1 && six==1){
			SMR_t= SoilMoistureRegime.XERIC;
			if (sd>90)
				SMRS_t= SoilMoistureRegimeSubdivision.DRY_XERIC;
			else
				SMRS_t= SoilMoistureRegimeSubdivision.TYPIC_XERIC;
		}
		else if(seven==1 && eight==1){
			SMR_t= SoilMoistureRegime.USTIC;
			max=0;
			for (i=180;i<300;i++){
				if (smc_t[i]!=1)
					snd++;
				else
				{
					if (snd>max)
						max=snd;
					snd=0;
				}
			}
			if (snd<max)
				snd=max;

			if (iso==1){
				if (tm8<180)
					SMRS_t= SoilMoistureRegimeSubdivision.ARIDIC_TROPUSTIC;
				else if ((tm8>=180)&&(tm8<270))
					SMRS_t= SoilMoistureRegimeSubdivision.TYPIC_TROPUSTIC;
				else if (tm8>=270)
					SMRS_t= SoilMoistureRegimeSubdivision.UDIC_TROPISTIC; //TODO changed from UDIC_TROPUSTIC (non-existent)
			} else {
				if ((sd>45)&&(wm>45))
					SMRS_t= SoilMoistureRegimeSubdivision.XERIC_TEMPUSTIC;
				else if ((wm>45)&&(snd>45))
					SMRS_t= SoilMoistureRegimeSubdivision.WET_TEMPUSTIC;
				else
					SMRS_t= SoilMoistureRegimeSubdivision.TYPIC_TEMPUSTIC;
			}

		}
		else if(seven==1 && eight!=1){
			SMR_t = SoilMoistureRegime.UNDEFINED;
			SMRS_t = SoilMoistureRegimeSubdivision.UNDEFINED;
		}
		else if(seven!=1 && nine==1){
			SMR_t = SoilMoistureRegime.PERUDIC;
			SMRS_t= SoilMoistureRegimeSubdivision.PERUDIC;
		}
		else if(seven!=1 && nine!=1){
			SMR_t = SoilMoistureRegime.UDIC;
			if (comd<30)
				SMRS_t = SoilMoistureRegimeSubdivision.TYPIC_UDIC;
			else if (iso==1)
				SMRS_t = SoilMoistureRegimeSubdivision.DRY_TROPUDIC;
			else
				SMRS_t = SoilMoistureRegimeSubdivision.DRY_TEMPUDIC;
		}

		// Determine Soil Temperature Regime
		if (st_annual_mean<0)
			STR_t=SoilTemperatureRegime.PERGELIC;

		if ((st_annual_mean>=0)&&(st_annual_mean<8))
			STR_t=SoilTemperatureRegime.CRYIC;

		if (st_annual_mean<8)
			if (iso==1)
				STR_t=SoilTemperatureRegime.ISOFRIGID;
			else
				STR_t=SoilTemperatureRegime.FRIGID;

		if ((st_annual_mean>=8)&&(st_annual_mean<15))
			if (iso==1)
				STR_t=SoilTemperatureRegime.ISOMESIC;
			else
				STR_t=SoilTemperatureRegime.MESIC;

		if ((st_annual_mean>=15)&&(st_annual_mean<22))
			if (iso==1)
				STR_t=SoilTemperatureRegime.ISOTHERMIC;
			else
				STR_t=SoilTemperatureRegime.THERMIC;

		if (st_annual_mean>=22)
			if (iso==1)
				STR_t=SoilTemperatureRegime.ISOHYPERTHERMIC;
			else
				STR_t=SoilTemperatureRegime.HYPERTHERMIC;

		// Update counters
		SMR_Counter[SMR_t.getIndex()]++;
		SMRS_Counter[SMRS_t.getIndex()]++;

		// Update summary
		summary.setSMR(SMR_t);
		summary.setSMRS(SMRS_t);
		summary.setSTR(STR_t);

		summary.setBiologicalWindow(BW);

		return summary;

	}	//end determine_SMR

	/**
	 * Wraps a float array as a list.
	 * 
	 * @param array The array to be wrapped
	 * @return The wrapped array
	 */
	private static List<Float> wrap(float[] array) {
		ArrayList<Float> ret = new ArrayList<Float>();
		for (float value : array) {
			ret.add(new Float(value));
		}
		return ret;
	}

	/**
	 * Wraps an integer array as a list.
	 * 
	 * @param array The array to be wrapped
	 * @return The wrapped array
	 */
	private static List<Integer> wrap(int[] array) {
		ArrayList<Integer> ret = new ArrayList<Integer>();
		for (int value : array) {
			ret.add(new Integer(value));
		}
		return ret;
	}


	/**
	 * Static class to hold Newhall Simulation Model constants. <br />
	 * This includes:
	 * <ul>
	 * <li> Soil Types
	 * <li> Thornthwaite Evapotranspiration 
	 * <li> Accretion Sequence
	 * <li> Depletion Sequence for Sandy Soils
	 * <li> Depletion Sequence
	 * </ul>
	 */
	private static class NsmConstants {

		// NSM Constants
		public static final float AST_DIFF = 2.0f;
		public static final float AST_AMPLITUDE_CHANGE = 0.988f;
		public static final float AST_PHASE_SHIFT = 12.5f;


		// THORNTHWAITE EVAPOTRANSPIRATION CONSTANTS
		public static final float[] ZPE={135.0f,139.5f,143.7f,147.8f,151.7f,155.4f,158.9f,162.1f,165.2f,
			168.0f,170.7f,173.1f,175.3f,177.2f,179.0f,180.5f,181.8f,182.9f,183.7f,184.3f,184.7f,184.9f,185.0f,185.0f};

		public static final float[] ZT={26.5f,27.f,27.5f,28.f,28.5f,29.f,29.5f,30.f,30.5f,31.f,31.5f,
			32.f,32.5f,33.f,33.5f,34.f,34.5f,35.f,35.5f,36.f,36.5f,37.f,37.5f,38.0f};

		public static final float[] FN1={1.04f,0.94f,1.04f,1.01f,1.04f,1.01f,1.04f,1.04f,1.01f,1.04f,1.01f,1.04f,
			1.02f,0.93f,1.03f,1.02f,1.06f,1.03f,1.06f,1.05f,1.01f,1.03f,0.99f,1.02f,
			1.00f,0.91f,1.03f,1.03f,1.08f,1.06f,1.08f,1.07f,1.02f,1.02f,0.98f,0.99f,
			0.97f,0.91f,1.03f,1.04f,1.11f,1.08f,1.12f,1.08f,1.02f,1.01f,0.95f,0.97f,
			0.95f,0.90f,1.03f,1.05f,1.13f,1.11f,1.14f,1.11f,1.02f,1.00f,0.93f,0.94f,
			0.93f,0.89f,1.03f,1.06f,1.15f,1.14f,1.17f,1.12f,1.02f,0.99f,0.91f,0.91f,
			0.92f,0.88f,1.03f,1.06f,1.15f,1.15f,1.17f,1.12f,1.02f,0.99f,0.91f,0.91f,
			0.92f,0.88f,1.03f,1.07f,1.16f,1.15f,1.18f,1.13f,1.02f,0.99f,0.90f,0.90f,
			0.91f,0.88f,1.03f,1.07f,1.16f,1.16f,1.18f,1.13f,1.02f,0.98f,0.90f,0.90f,
			0.91f,0.87f,1.03f,1.07f,1.17f,1.16f,1.19f,1.13f,1.03f,0.98f,0.90f,0.89f,
			0.90f,0.87f,1.03f,1.08f,1.18f,1.17f,1.20f,1.14f,1.03f,0.98f,0.89f,0.88f,
			0.90f,0.87f,1.03f,1.08f,1.18f,1.18f,1.20f,1.14f,1.03f,0.98f,0.89f,0.88f,
			0.89f,0.86f,1.03f,1.08f,1.19f,1.19f,1.21f,1.15f,1.03f,0.98f,0.88f,0.87f,
			0.88f,0.86f,1.03f,1.09f,1.19f,1.20f,1.22f,1.15f,1.03f,0.97f,0.88f,0.86f,
			0.88f,0.85f,1.03f,1.09f,1.20f,1.20f,1.22f,1.16f,1.03f,0.97f,0.87f,0.86f,
			0.87f,0.85f,1.03f,1.09f,1.21f,1.21f,1.23f,1.16f,1.03f,0.97f,0.86f,0.85f,
			0.87f,0.85f,1.03f,1.10f,1.21f,1.22f,1.24f,1.16f,1.03f,0.97f,0.86f,0.84f,
			0.86f,0.84f,1.03f,1.10f,1.22f,1.23f,1.25f,1.17f,1.03f,0.97f,0.85f,0.83f,
			0.85f,0.84f,1.03f,1.10f,1.23f,1.24f,1.25f,1.17f,1.04f,0.96f,0.84f,0.83f,
			0.85f,0.84f,1.03f,1.11f,1.23f,1.24f,1.26f,1.18f,1.04f,0.96f,0.84f,0.82f,
			0.84f,0.83f,1.03f,1.11f,1.24f,1.25f,1.27f,1.18f,1.04f,0.96f,0.83f,0.81f,
			0.83f,0.83f,1.03f,1.11f,1.25f,1.26f,1.27f,1.19f,1.04f,0.96f,0.82f,0.80f,
			0.82f,0.83f,1.03f,1.12f,1.26f,1.27f,1.28f,1.19f,1.04f,0.95f,0.82f,0.79f,
			0.81f,0.82f,1.02f,1.12f,1.26f,1.28f,1.29f,1.20f,1.04f,0.95f,0.81f,0.77f,
			0.81f,0.82f,1.02f,1.13f,1.27f,1.29f,1.30f,1.20f,1.04f,0.95f,0.80f,0.76f,
			0.80f,0.81f,1.02f,1.13f,1.28f,1.29f,1.31f,1.21f,1.04f,0.94f,0.79f,0.75f,
			0.79f,0.81f,1.02f,1.13f,1.29f,1.31f,1.32f,1.22f,1.04f,0.94f,0.79f,0.74f,
			0.77f,0.80f,1.02f,1.14f,1.30f,1.32f,1.33f,1.22f,1.04f,0.93f,0.78f,0.73f,
			0.76f,0.80f,1.02f,1.14f,1.31f,1.33f,1.34f,1.23f,1.05f,0.93f,0.77f,0.72f,
			0.75f,0.79f,1.02f,1.14f,1.32f,1.34f,1.35f,1.24f,1.05f,0.93f,0.76f,0.71f,
			0.74f,0.78f,1.02f,1.15f,1.33f,1.36f,1.37f,1.25f,1.06f,0.92f,0.76f,0.70f};

		public static final float RN[]={0.f,5.f,10.f,15.f,20.f,25.f,26.f,27.f,28.f,29.f,30.f,31.f,32.f,33.f,
			34.f,35.f,36.f,37.f,38.f,39.f,40.f,41.f,42.f,43.f,44.f,45.f,46.f,47.f,48.f,49.f,
			50.f};

		public static final float FS1[]={1.06f,0.95f,1.04f,1.00f,1.02f,0.99f,1.02f,1.03f,1.00f,1.05f,1.03f,1.06f,
			1.08f,0.97f,1.05f,0.99f,1.01f,0.96f,1.00f,1.01f,1.00f,1.06f,1.05f,1.10f,
			1.12f,0.98f,1.05f,0.98f,0.98f,0.94f,0.97f,1.00f,1.00f,1.07f,1.07f,1.12f,
			1.14f,1.00f,1.05f,0.97f,0.96f,0.91f,0.95f,0.99f,1.00f,1.08f,1.09f,1.15f,
			1.17f,1.01f,1.05f,0.96f,0.94f,0.88f,0.93f,0.98f,1.00f,1.10f,1.11f,1.18f,
			1.20f,1.03f,1.06f,0.95f,0.92f,0.85f,0.90f,0.96f,1.00f,1.12f,1.14f,1.21f,
			1.23f,1.04f,1.06f,0.94f,0.89f,0.82f,0.87f,0.94f,1.00f,1.13f,1.17f,1.25f,
			1.27f,1.06f,1.07f,0.93f,0.86f,0.78f,0.84f,0.92f,1.00f,1.15f,1.20f,1.29f,
			1.28f,1.07f,1.07f,0.92f,0.85f,0.76f,0.82f,0.92f,1.00f,1.16f,1.22f,1.31f,
			1.30f,1.08f,1.07f,0.92f,0.83f,0.74f,0.81f,0.91f,0.99f,1.17f,1.23f,1.33f,
			1.32f,1.10f,1.07f,0.91f,0.82f,0.72f,0.79f,0.90f,0.99f,1.17f,1.25f,1.35f,
			1.34f,1.11f,1.08f,0.90f,0.80f,0.70f,0.76f,0.89f,0.99f,1.18f,1.27f,1.37f,
			1.37f,1.12f,1.08f,0.89f,0.77f,0.67f,0.74f,0.88f,0.99f,1.19f,1.29f,1.41f};
		public static final float RS[]={5.f,10.f,15.f,20.f,25.f,30.f,35.f,40.f,42.f,44.f,46.f,48.f,50f};


		// Light Precipitation Constants
		/**	accretion sequence */
		public static final int DP_S[]={ 8, 7,16, 6,15,24, 5,14,
			23,32, 4,13,22,31,40, 3,12,
			21,30,39,48,2,11,20,29,38,47,56,1,10,19,28,37,46,55,64,9,18,
			27,36,45,54,63,17,26,35,44,53,62,25,34,43,52,61,33,42,51,60,
			41,50,59,49,58,57};
		/**	depletion sequence for sandy soils */
		public static final int DP_S_SANDY[]={8,7,6,5,4,3,2,1,16,15,14,13,12,11,10,9,24,23,22,21,20,19,18,17,
			32,31,30,29,28,27,26,25,40,39,38,37,36,35,34,33,48,47,46,45,44,43,42,41,
			56,55,54,53,52,51,50,49,64,63,62,61,60,59,58,57};
		/**	depletion sequence */
		public static final float DP_R[]={1.0f,1.0f,1.0f,1.0f,1.02f,1.03f,1.05f,1.07f,1.09f,1.11f,1.13f,1.15f,
			1.17f,1.19f,1.21f,1.23f,1.26f,1.28f,1.31f,1.34f,1.37f,1.40f,1.43f,1.46f,
			1.49f,1.53f,1.57f,1.61f,1.65f,1.69f,1.74f,1.78f,1.84f,1.89f,1.95f,2.01f,
			2.07f,2.14f,2.22f,2.30f,2.38f,2.47f,2.57f,2.68f,2.80f,2.93f,3.07f,3.22f,
			3.39f,3.58f,3.80f,4.03f,4.31f,4.62f,4.98f,5.0f,5.0f,5.0f,5.0f,5.0f,5.0f,5.0f,5.0f,5.0f};
	}

	/**
	 * Holds summary information of all calculated
	 * NSM data.
	 *
	 */
	private class NsmSummary{
		private float totalPrec;
		private float totalPet;
		private float totalAwb;
		private float totalMsd;
		private int totalDryDays;
		private int totalMediumDays;
		private int totalMoistDays;
		private int totalBio8;
		private int totalYears;
		private int goodYears;

		private float lastPrec;
		private float lastPet;
		private float lastAwb;
		private float lastMsd;
		private int lastDryDays;
		private int lastMediumDays;
		private int lastMoistDays;
		private int lastBio8;

		public NsmSummary(){
			totalPrec = 0.0f;
			totalPet = 0.0f;
			totalAwb = 0.0f;
			totalMsd = 0.0f;
			totalDryDays = 0;
			totalMediumDays = 0;
			totalMoistDays = 0;
			totalBio8 = 0;
			totalYears = 0;
			goodYears=0;

			lastPrec = 0.0f;
			lastPet = 0.0f;
			lastAwb = 0.0f;
			lastMsd = 0.0f;
			lastDryDays = 0;
			lastMediumDays = 0;
			lastMoistDays = 0;
			lastBio8 = 0;
		}

		public void updateTotalPrecipitation(float[] prec, int badmonth, float[] monprec){
			lastPrec =  getPrecipYearTotal(prec,badmonth,monprec);
			totalPrec += lastPrec;
			
		}
		
		public float getTotalPrecipitaion(){
			return totalPrec;
		}
		
		public float getTotalEvapotranspiration(){
			return totalPet;
		}
		public void updateTotalEvapotranspiration(float[] pet, int badmonth){
			lastPet = getYearTotal(pet,badmonth);
			totalPet += lastPet;
		}
		public void updateTotalAwb(float awb){
			lastAwb = awb;
			totalAwb += awb;
		}
		public void updateTotalMsd(float msd){
			lastMsd = msd;
			totalMsd += msd;
		}
		public void updateTotalDryDays(int dryDays){
			lastDryDays = dryDays;
			totalDryDays += dryDays;
		}
		public void updateTotalMediumDays(int mediumDays){
			lastMediumDays = mediumDays;
			totalMediumDays += mediumDays;
		}
		public void updateTotalMoistDays(int moistDays){
			lastMoistDays = moistDays;
			totalMoistDays += moistDays;
		}
		public void updateTotalBio8(int bio8){
			lastBio8 = bio8;
			totalBio8 += bio8;
		}
		public void incTotalYears(){
			totalYears++;
		}
		
		public void incGoodYears(){
			goodYears++;
		}

		public float getAvgPrecipitation(){
			if(totalYears == 0){
				LOG.warn("Divide by zero when calculating average precipitation");
				return 0.0f;
			}
			//totalYears++;
			return totalPrec/((float)goodYears);
		}
		public float getAvgEvapotranspiration(){
			if(totalYears == 0){
				LOG.warn("Divide by zero when calculating average potential evapotranspiration");
				return 0.0f;
			}
			return totalPet/((float)goodYears);
		}
		public float getAvgAwb(){
			if(totalYears == 0){
				LOG.warn("Divide by zero when calculating average awb");
				return 0.0f;
			}
			return totalAwb/((float)goodYears);
		}
		public float getAvgMsd(){
			if(totalYears == 0){
				LOG.warn("Divide by zero when calculating average msd");
				return 0.0f;
			}
			return totalMsd/((float)goodYears);
		}
		public float getAvgDryDays(){
			if(totalYears == 0){
				LOG.warn("Divide by zero when calculating average dry days");
				return 0;
			}
			return ((float)totalDryDays)/((float)goodYears);
		}
		public float getAvgMediumDays(){
			if(totalYears == 0){
				LOG.warn("Divide by zero when calculating average medium days");
				return 0;
			}
			return ((float)totalMediumDays)/((float)goodYears);
		}
		public float getAvgMoistDays(){
			if(totalYears == 0){
				LOG.warn("Divide by zero when calculating average moist days");
				return 0;
			}
			return ((float)totalMoistDays)/((float)goodYears);
		}
		public float getAvgBio8(){
			if(totalYears == 0){
				LOG.warn("Divide by zero when calculating average bio8");
				return 0;
			}
			return ((float)totalBio8)/((float)goodYears);
		}

		public float getLastPrecipitation() {
			return lastPrec;
		}
		public float getLastEvapotranspiration() {
			return lastPet;
		}
		public float getLastAwb() {
			return lastAwb;
		}
		public float getLastMsd() {
			return lastMsd;
		}
		public int getLastDryDays() {
			return lastDryDays;
		}
		public int getLastMediumDays() {
			return lastMediumDays;
		}
		public int getLastMoistDays() {
			return lastMoistDays;
		}
		public int getLastBio8() {
			return lastBio8;
		}

		public int getTotalYears(){
			return totalYears;
		}

		private float getPrecipYearTotal(float[] monthly, int badmonths,float[] monprec){
			float tmp = 0.0f;
		//	int bad_months=0;
			for(int i = 0; i < monthly.length; i++){
				if (monprec[i] != DataType.MISSING && monprec[i] != DataType.ERROR_RESULT && monprec[i] != DataType.OUTSIDE_OF_RANGE&&
						monprec[i] != DataType.OUTSIDE_OF_REQUEST_RANGE&&monprec[i] != DataType.NONEXISTANT){
					tmp+= monthly[i];
				}
				
			}
			if(badmonths>2){
				return 0.0f;
			}
			return tmp;
		}
		
		private float getYearTotal(float[] monthly, int badmonths){
			float tmp = 0.0f;
		//	int bad_months=0;
			for(int i = 0; i < monthly.length; i++){
				if (monthly[i] != DataType.MISSING && monthly[i] != DataType.ERROR_RESULT && monthly[i] != DataType.OUTSIDE_OF_RANGE&&
						monthly[i] != DataType.OUTSIDE_OF_REQUEST_RANGE&&monthly[i] != DataType.NONEXISTANT){
					tmp+= monthly[i];
				}
				
			}
			if(badmonths>2){
				return 0.0f;
			}
			return tmp;
		}

	}

	/**
	 * Holds Soil Moisture regimes and returns biological window meanings
	 * 
	 * Biological Window element meaning (1-indexed):
	 * <ol>
	 * <li> number of days that MCS is dry
	 * <li> number of days that MCS is medium
	 * <li> number of days that MCS is moist
	 * <li> number of days that MCS is dry when soil temperature is over 5 C
	 * <li> number of days that MCS is medium when soil temperature is over 5 C
	 * <li> number of days that MCS is moist when soil temperature is over 5 C
	 * <li> number of consecutive days MCS is partly moist or moist for 90
	 * <li> number of consecutive days that soil temperature is over 8 C 
	 * and MCS is partly moist or moist for 90
	 * <li> number of consecutive days that MCS is not completely 
	 * dry within four month following the summer solstice
	 * <li> number of consecutive days that MCS is moist within 
	 * four month following the winter solstice
	 * </ol>
	 *
	 */
	private class SoilRegimeSummary {

		private SoilTemperatureRegime STR;
		private SoilMoistureRegime SMR;
		private SoilMoistureRegimeSubdivision SMRS;

		// Biological window
		int[] bw; 


		public SoilRegimeSummary(){
			SMR = SoilMoistureRegime.UNDEFINED;
			SMRS = SoilMoistureRegimeSubdivision.UNDEFINED;
			STR = SoilTemperatureRegime.UNDEFINED;

			bw = null;
		}

		/**
		 * Get number of consecutive days that MCS is not completely 
		 * dry within four month following the summer solstice
		 */
		public int getConsecutiveDryDaysAfterSummerSolstice() {
			if(bw != null)
				return bw[8];
			else
				return -1;
		}

		/**
		 * Get number of consecutive days that MCS is moist within 
		 * four month following the winter solstice
		 */
		public int getConsecutiveMoistDaysAfterWinterSolstice() {
			if(bw != null)
				return bw[9];
			else
				return -1;
		}

		/**
		 * Get number of consecutive days MCS is partly 
		 * moist or moist for 90;
		 */
		public int getMostConsecutiveMoistDays() {
			if(bw != null)
				return bw[6];
			else
				return -1;
		}

		/**
		 * Get number of consecutive days that soil temperature is over 
		 * 8 C and MCS is partly moist or moist for 90;
		 */
		public int getMostConsecutiveMoistDaysAbove8() {
			if(bw != null)
				return bw[7];
			else
				return -1;
		}

		/**
		 * Get number of days that MCS is dry when soil 
		 * temperature is over 5 C
		 */
		public int getTotalDryDaysAbove5() {
			if(bw != null)
				return bw[3];
			else
				return -1;
		}

		/**
		 * Get number of days that MCS is medium when soil 
		 * temperature is over 5 C
		 */
		public int getTotalMediumDaysAbove5() {
			if(bw != null)
				return bw[4];
			else
				return -1;
		}

		/**
		 * Get number of days that MCS is moist when soil 
		 * temperature is over 5 C
		 */
		public int getTotalMoistDaysAbove5() {
			if(bw != null)
				return bw[5];
			else
				return -1;
		}

		/**
		 * Get number of days that MCS is dry
		 */
		public int getTotalDryDays() {
			if(bw != null)
				return bw[0];
			else
				return -1;
		}

		/**
		 * Get number of days that MCS is medium
		 */
		public int getTotalMediumDays() {
			if(bw != null)
				return bw[1];
			else
				return -1;
		}

		/**
		 * Get number of days that MCS is moist
		 */
		public int getTotalMoistDays() {
			if(bw != null)
				return bw[2];
			else
				return -1;
		}

		/**
		 * Set biological window <br />
		 * Biological Window element meaning (1-indexed):
		 * <ol>
		 * <li> number of days that MCS is dry
		 * <li> number of days that MCS is medium
		 * <li> number of days that MCS is moist
		 * <li> number of days that MCS is dry when soil temperature is over 5 C
		 * <li> number of days that MCS is medium when soil temperature is over 5 C
		 * <li> number of days that MCS is moist when soil temperature is over 5 C
		 * <li> number of consecutive days MCS is partly moist or moist for 90
		 * <li> number of consecutive days that soil temperature is over 8 C 
		 * and MCS is partly moist or moist for 90
		 * <li> number of consecutive days that MCS is not completely 
		 * dry within four month following the summer solstice
		 * <li> number of consecutive days that MCS is moist within 
		 * four month following the winter solstice
		 * </ol>
		 */
		public void setBiologicalWindow(int[] biologicalWindow) {
			this.bw = biologicalWindow;
		}

		/**
		 * Get soil temperature regime
		 */
		public SoilTemperatureRegime getSTR() {
			return STR;
		}

		public void setSTR(SoilTemperatureRegime str) {
			STR = str;
		}

		/**
		 * Get soil moisture regime
		 */
		public SoilMoistureRegime getSMR() {
			return SMR;
		}

		public void setSMR(SoilMoistureRegime smr) {
			SMR = smr;
		}

		/**
		 * Get soil moisture regime subdivision
		 */
		public SoilMoistureRegimeSubdivision getSMRS() {
			return SMRS;
		}

		public void setSMRS(SoilMoistureRegimeSubdivision smrs) {
			SMRS = smrs;
		}

	}
}
