package UserControl;

import TreeMapGenerator.TreeMapGenerator;
import treemap.DataFaciliation.DataFacilitator;

/**
 *
 * @author Max Sondag
 */
public class Experiment {

    private TreeMapGenerator generator;
    private DataFacilitator df;
    private int maxTime;
    private SimulatorMaster simulator;

    /**
     * Simulates a treemap algorithm for timeSteps steps using df as the data
     * facialotor, generator as the algorithm
     *
     * @param df
     * @param generator
     * @param timeSteps
     */
    public Experiment(DataFacilitator df, TreeMapGenerator generator, int timeSteps, SimulatorMaster simulator) {
        this.df = df;
        this.generator = generator;
        this.maxTime = timeSteps;
        this.simulator = simulator;

        simulator.setDataFacilitator(df);
        simulator.setTreeMapGenerator(generator);

        maxTime = timeSteps;
        if (df.hasMaxTime()) {
            maxTime = df.getMaxTime();
        }
    }

    public void runExperiment() {

        for (int time = 0; time <= maxTime; time++) {

            String dataGeneratorParamaters = df.getParamaterDescription();
            String algorithmName = generator.getClass().getSimpleName();
            String algorithmParamaters = generator.getParamaterDescription();

            String commandIdentifier = "experiment;" + df.getExperimentName() + ";" + dataGeneratorParamaters + ";" + algorithmName + ";" + algorithmParamaters + ";time=" + time;
            generateTreeMap(time, commandIdentifier);
        }
    }

    private void generateTreeMap(int time, String commandIdentifier) {
        simulator.getTreeMap(time, false, "experimentnoStability");
    }
}
