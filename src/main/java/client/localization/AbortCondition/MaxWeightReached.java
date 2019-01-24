package client.localization.AbortCondition;

import client.localization.Particle;
import client.montecarlo.IMoveController;

import java.util.List;

public class MaxWeightReached implements IAbortConditionChecker {

    public final static double ABORT_WEIGHT = 0.98;

    @Override
    public boolean abort(List<IMoveController> particles, IMoveController robot) {
        for (IMoveController p : particles){
            if ( p.getBelief() > ABORT_WEIGHT ){
                return true;
            }
        }
        return false;
    }
}