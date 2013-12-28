package edu.unl.act.rma.firm.test.drought.index;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;

import junit.framework.TestCase;
import edu.unl.act.rma.firm.core.InvalidStateException;
import edu.unl.act.rma.firm.drought.NsmCompleteDTO;
import edu.unl.act.rma.firm.drought.NsmSummaryDTO;
import edu.unl.act.rma.firm.drought.index.NewhallSimulationModel;

public class NsmTest extends TestCase {

	private static HashMap<String, Object> ORACLE;
	
	static { 
		
		try {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(NsmTest.class.getClassLoader().getResource("DroughtTestOracle").getPath()));
			ORACLE = (HashMap<String, Object>)ois.readObject();
		} catch  ( Exception e ) { 
			RuntimeException re = new RuntimeException("could not load the data oracle");
			re.initCause(e);
			throw re;
		}
	}
		
	public void testNsmSummary() throws InvalidStateException{
		NewhallSimulationModel nsm = new NewhallSimulationModel(1893,2009);
		HashMap<String, float[][]> additional_inputs = (HashMap<String, float[][]>) ORACLE.get("PDSI_ADDITIONAL_INPUTS");
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("MONTHLY_PRECIP");
		HashMap<String, float[][]> temp_data = (HashMap<String, float[][]>) ORACLE.get("MONTHLY_TEMP");
		HashMap<String, Object> nsm_oracle_data = (HashMap<String, Object>) ORACLE.get("NSM_SUMMARY");
		for ( String str : nsm_oracle_data.keySet() ) {
			HashMap<String, HashMap<String, Object>> nsm_oracle = (HashMap<String, HashMap<String, Object>>)nsm_oracle_data.get(str);
			float[][] add=additional_inputs.get(str);
			nsm.setData(precip_data.get(str), temp_data.get(str), add[1][0], add[0][0]);
			NsmSummaryDTO dto = nsm.computeSummaryNsm();
			for(Integer i: dto.years()){
				NsmSummaryDTO.DataSet summary = dto.getData(i);
				assertEquals("Values do not match for Regime name for station " + str,nsm_oracle.get(String.valueOf(i)).get("Regime-Name"),summary.getRegime().getName());
				assertEquals("Values do not match for Awb for station " + str,Float.valueOf(nsm_oracle.get(String.valueOf(i)).get("Awb").toString()),summary.getAwb(), 0.01f);
				assertEquals("Values do not match for Bio8 for station " + str,Float.valueOf(nsm_oracle.get(String.valueOf(i)).get("Bio8").toString()),summary.getBio8(), 0.01f);
				assertEquals("Values do not match for Dry days for station " + str,Float.valueOf(nsm_oracle.get(String.valueOf(i)).get("DryDays").toString()),summary.getDryDays(), 0.01f);
				assertEquals("Values do not match for Evapotranspiration for station " + str,Float.valueOf(nsm_oracle.get(String.valueOf(i)).get("Evapotranspiration").toString()),summary.getEvapotranspiration(), 0.01f);
				assertEquals("Values do not match for Medium Days for station " + str,Float.valueOf(nsm_oracle.get(String.valueOf(i)).get("MediumDays").toString()),summary.getMediumDays(), 0.01f);
				assertEquals("Values do not match for Moist Days for station " + str,Float.valueOf(nsm_oracle.get(String.valueOf(i)).get("MoistDays").toString()),summary.getMoistDays(), 0.01f);
				assertEquals("Values do not match for Msd for station " + str,Float.valueOf(nsm_oracle.get(String.valueOf(i)).get("Msd").toString()),summary.getMsd(), 0.01f);
				assertEquals("Values do not match for Precipitation for station " + str,Float.valueOf(nsm_oracle.get(String.valueOf(i)).get("Precipitation").toString()),summary.getPrecipitation(), 0.01f);
				
				assertEquals("Values do not match for AWB_Average " + str,Float.valueOf(nsm_oracle.get(String.valueOf(i)).get("AWB_Average").toString()),dto.getAwbAvg(), 0.01f);
				assertEquals("Values do not match for Evapotranspiration_Average " + str,Float.valueOf(nsm_oracle.get(String.valueOf(i)).get("Evapotranspiration_Average").toString()),dto.getEvapotranspirationAvg(), 0.01f);
				assertEquals("Values do not match for Precip_Average " + str,Float.valueOf(nsm_oracle.get(String.valueOf(i)).get("Precip_Average").toString()),dto.getPrecipitationAvg(), 0.01f);
				assertEquals("Values do not match for MSD_Average" + str,Float.valueOf(nsm_oracle.get(String.valueOf(i)).get("MSD_Average").toString()),dto.getMsdAvg(), 0.01f);
				
				assertEquals("Values do not match for Bio8_Average " + str,Float.valueOf(nsm_oracle.get(String.valueOf(i)).get("Bio8_Average").toString()),dto.getBio8Avg(), 0.01f);
				assertEquals("Values do not match for DryDays_Average " + str,Float.valueOf(nsm_oracle.get(String.valueOf(i)).get("DryDays_Average").toString()),dto.getDryDaysAvg(), 0.01f);
				assertEquals("Values do not match for MediumDays_Average " + str,Float.valueOf(nsm_oracle.get(String.valueOf(i)).get("MediumDays_Average").toString()),dto.getMediumDaysAvg(), 0.01f);
				assertEquals("Values do not match for MoistDays_Average" + str,Float.valueOf(nsm_oracle.get(String.valueOf(i)).get("MoistDays_Average").toString()),dto.getMoistDaysAvg() , 0.01f);
				}
			}
	}
	
	public void testNsmComplete() throws Exception {
		NewhallSimulationModel nsm = new NewhallSimulationModel(1893,2009);
		HashMap<String, float[][]> additional_inputs = (HashMap<String, float[][]>) ORACLE.get("PDSI_ADDITIONAL_INPUTS");
		HashMap<String, float[][]> precip_data = (HashMap<String, float[][]>) ORACLE.get("MONTHLY_PRECIP");
		HashMap<String, float[][]> temp_data = (HashMap<String, float[][]>) ORACLE.get("MONTHLY_TEMP");
		HashMap<String, Object> nsm_oracle_data = (HashMap<String, Object>) ORACLE.get("NSM_COMPLETE");
		
		for ( String str : nsm_oracle_data.keySet() ) {
			HashMap<String, HashMap<String, Object>> nsm_oracle = (HashMap<String, HashMap<String, Object>>)nsm_oracle_data.get(str);
			float[][] add=additional_inputs.get(str);
			nsm.setData(precip_data.get(str), temp_data.get(str), add[1][0], add[0][0]);
			NsmCompleteDTO dto = nsm.computeCompleteNsm();
			for(Integer year: dto.years()){
				NsmCompleteDTO.DataSet complete = dto.getData(year);
				assertEquals("Values do not match for Moisture Regime name for station " + str,nsm_oracle.get(String.valueOf(year)).get("MoistureRegimeSubdivision-Name"),complete.getMoistureRegimeSubdivision().getName());
				assertEquals("Values do not match for  Temperature Regime name for station " + str,nsm_oracle.get(String.valueOf(year)).get("TemperatureRegime-Name"),complete.getTemperatureRegime().getName());
				assertEquals("Values do not match for Consecutive dry days after summer solstice for station " + str,Float.valueOf(nsm_oracle.get(String.valueOf(year)).get("ConsecutiveDryDaysAfterSummerSolstice").toString()),complete.getConsecutiveDryDaysAfterSummerSolstice(), 0.01f);
				assertEquals("Values do not match for Consecutive moist days after winter solstice for station " + str,Float.valueOf(nsm_oracle.get(String.valueOf(year)).get("ConsecutiveMoistDaysAfterWinterSolstice").toString()),complete.getConsecutiveMoistDaysAfterWinterSolstice(), 0.01f);
				assertEquals("Values do not match for Most consecutive moist days for station " + str,Float.valueOf(nsm_oracle.get(String.valueOf(year)).get("MostConsecutiveMoistDays").toString()),complete.getMostConsecutiveMoistDays(), 0.01f);
				assertEquals("Values do not match for Consecutive moist days above 8 for station " + str,Float.valueOf(nsm_oracle.get(String.valueOf(year)).get("MostConsecutiveMoistDaysAbove8").toString()),complete.getMostConsecutiveMoistDaysAbove8(), 0.01f);
				assertEquals("Values do not match for Total dry days for station " + str,Float.valueOf(nsm_oracle.get(String.valueOf(year)).get("TotalDryDays").toString()),complete.getTotalDryDays(), 0.01f);
				assertEquals("Values do not match for Total dry days above 5 for station " + str,Float.valueOf(nsm_oracle.get(String.valueOf(year)).get("TotalDryDaysAbove5").toString()),complete.getTotalDryDaysAbove5(), 0.01f);
				assertEquals("Values do not match for Total medium days for station " + str,Float.valueOf(nsm_oracle.get(String.valueOf(year)).get("TotalMediumDays").toString()),complete.getTotalMediumDays(), 0.01f);
				assertEquals("Values do not match for Total medium days above 5 for station " + str,Float.valueOf(nsm_oracle.get(String.valueOf(year)).get("TotalMediumDaysAbove5").toString()),complete.getTotalMediumDaysAbove5(), 0.01f);
				assertEquals("Values do not match for Total moist days for station " + str,Float.valueOf(nsm_oracle.get(String.valueOf(year)).get("TotalMoistDays").toString()),complete.getTotalMoistDays(), 0.01f);
				assertEquals("Values do not match for Total moist days above 5 for station " + str,Float.valueOf(nsm_oracle.get(String.valueOf(year)).get("TotalMoistDaysAbove5").toString()),complete.getTotalMoistDaysAbove5(), 0.01f);
				assertEquals("Values do not match for SoilMoistureCalendar size" + str,Float.valueOf(nsm_oracle.get(String.valueOf(year)).get("SoilMoistureCalendar").toString()),complete.getSoilMoistureCalendar().size(), 0.01f);
				assertEquals("Values do not match for SoilMoistureSlants size " + str,Float.valueOf(nsm_oracle.get(String.valueOf(year)).get("SoilMoistureSlants").toString()),complete.getSoilMoistureSlants().size(), 0.01f);
				assertEquals("Values do not match for SoilTemperatureCalendar size " + str,Float.valueOf(nsm_oracle.get(String.valueOf(year)).get("SoilTemperatureCalendar").toString()),complete.getSoilTemperatureCalendar().size(), 0.01f);
			}
		}
	}
}