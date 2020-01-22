package treemap.DataFaciliation.Generators;

import treemap.DataFaciliation.DataFacilitator;
import treemap.dataStructure.DataMap;

/**
 *
 * @author Max Sondag
 */
public abstract class DataGenerator implements DataFacilitator {

    DataMap currentData;
    int seed;
    int iteration;

    @Override
    public String getDataIdentifier() {
        return ("GeneratorClass: " + this.getClass().getCanonicalName()
                + "seed: " + seed
                + "iteration: " + iteration);
    }

    @Override
    public String getExperimentName() {
        return "none";
    }

    public boolean hasMaxTime() {
        return false;
    }

    public int getMaxTime() {
        return Integer.MAX_VALUE;
    }
}
