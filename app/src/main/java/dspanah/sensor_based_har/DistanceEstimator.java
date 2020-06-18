package dspanah.sensor_based_har;

public class DistanceEstimator {

    public int getStepLengthX(int activity)
    {
        if (activity == 0) //run
        {
            return 75;
        }
        else if (activity == 2) //walk
        {
            return 43;
        }
        return 0;
    }
    public int getStepLengthY(int activity)
    {
        if (activity == 0) //run
        {
            return 87;
        }
        else if (activity == 2) //walk
        {
            return 50;
        }
        return 0;
    }
}
