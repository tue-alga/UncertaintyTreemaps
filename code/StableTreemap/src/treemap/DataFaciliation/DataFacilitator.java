package treemap.DataFaciliation;

import treemap.dataStructure.DataMap;

/**
 *
 * @author Max Sondag
 */
public interface DataFacilitator {

    public DataMap getData(int time);

    public String getDataIdentifier();

    public String getExperimentName();

    public String getParamaterDescription();

    public DataFacilitator reinitializeWithSeed(int seed);

    public boolean hasMaxTime();

    public int getMaxTime();

    public int getLastTime();

    public void useSdAsValue();

  
}
