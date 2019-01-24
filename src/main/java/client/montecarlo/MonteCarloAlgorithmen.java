package client.montecarlo;

import client.localization.IMonteEventListener;
import client.montecarlo.ParticleGenerator.IParticleGenerator;
import client.montecarlo.Resample.IResampler;
import client.montecarlo.Resample.RouletteWheelResampler;
import client.montecarlo.Weight.AllDistanceWeightCalculator;
import client.montecarlo.Weight.IWeightCalculator;

import java.util.List;
import java.util.Random;

public class MonteCarloAlgorithmen {

    private static double REUSE_GRADE = 1d;

    private IMoveController roboter;
    private List<IMoveController> partikels;

    private IResampler resampler;
    private IWeightCalculator calculator;
    private IParticleGenerator generator;

    private SensorDataSet latestRoboterDataSet;

    private boolean isRunning = false;

    public MonteCarloAlgorithmen(IMoveController roboter, IParticleGenerator generator, IResampler resampler) {
        this.roboter = roboter;
        this.resampler = resampler;
        this.generator = generator;
        this.calculator = new AllDistanceWeightCalculator();
    }

    public MonteCarloAlgorithmen(IMoveController roboter, IParticleGenerator generator) {
        this(roboter, generator, new RouletteWheelResampler());
    }

    public IResampler getResampler() {
        return resampler;
    }

    public void setResampler(IResampler resampler) {
        this.resampler = resampler;
    }

    public IWeightCalculator getCalculator() {
        return calculator;
    }

    public void setCalculator(IWeightCalculator calculator) {
        this.calculator = calculator;
    }

    public IParticleGenerator getGenerator() {
        return generator;
    }

    public void setGenerator(IParticleGenerator generator) {
        this.generator = generator;
    }

    public List<IMoveController> run (List<IMoveController> partikels) throws ActionException{
        this.isRunning = true;
        this.partikels = partikels;

        if (latestRoboterDataSet == null) {
            System.out.println("Init roboter dataset");
            this.latestRoboterDataSet = roboter.getSensorDataSet();
        }

        moveCommand();
        calculateWeights();
        resamplePartikels();

        this.isRunning = false;
        return this.partikels;
    }
    public void runAsync (List<IMoveController> partikels, IMonteEventListener listner) throws ActionException{
        List<IMoveController> newPartikels = run(partikels);
        listner.onMonteDone(newPartikels);
    }

    private void moveCommand() throws ActionException{
        Random random = new Random();
        double commandNumber = random.nextDouble();

        /*if ( commandNumber < 0.07){

            turnLeft(180);
        }
        else{*/
            moveForward((int) (commandNumber * 20.0));
        //}



        /*int angle;

        if ( commandNumber >= 0.0 && commandNumber <= 0.25){
            turnLeft();
        }
        if ( commandNumber > 0.25 && commandNumber <= 0.5){
            turnRight();
        }
        if ( commandNumber > 0.5 && commandNumber <= 1){
            moveForward();
        }*/
        /*switch (commandNumber){
            //case forward
            case 0:
                moveForward();
                break;
            //turn left
            case 1:
                turnLeft();
                break;
            //turn right
            case 2:
                turnRight();
                break;
        }*/
    }

    private void resamplePartikels(){

        List<IMoveController> result = resampler.resample(this.partikels, REUSE_GRADE, generator);
        this.partikels = result;

    }

    //move controlles
    private void moveForward(int distance) throws ActionException{
        System.out.println("forward");

        //if at end turn around
        if(latestRoboterDataSet.getDistanceFront() < 25 )
        {
            System.out.println("getDistanceFront() < 25 (d: " + latestRoboterDataSet.getDistanceFront() + ", befehl: " + distance + ")");
            turnLeft(180);
            return;
        }
        else if((latestRoboterDataSet.getDistanceFront()-25) < distance)
        {
            System.out.println("getDistanceFront() - 25 < distance (d: " + latestRoboterDataSet.getDistanceFront() + ", befehl: " + distance + ")");
            distance = (int)Math.ceil(latestRoboterDataSet.getDistanceFront()) - 25;


            if (distance < 5) {
                System.out.println("distance < 5 (d: " + distance + ")");
                turnLeft(180);
                return;
            }
        }

        //move
        roboter.moveForward(distance);
        for (IMoveController partikel: partikels) {
            partikel.moveForward(distance);
        }
    }

    private void turnLeft(int angle) throws ActionException {
        System.out.println("left");

        roboter.turnLeft(angle);
        for (IMoveController partikel: partikels) {
            partikel.turnLeft(angle);
        }
    }

    private void turnRight(int angle) throws ActionException {
        System.out.println("right");

        roboter.turnRight(angle);
        for (IMoveController particle: partikels) {
            particle.turnRight(angle);
        }
    }

    private void calculateWeights() throws ActionException {
        latestRoboterDataSet = roboter.getSensorDataSet();
        for (IMoveController particle: this.partikels) {
            particle.setBelief(calculator.calculateWeight(latestRoboterDataSet, particle));
        }
    }

    public IMoveController getUsedRobot() {
        return roboter;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
