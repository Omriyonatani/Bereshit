public class Engine {
    public int maxNewton;
    public int percentOn;
    private double burnRate;


    public Engine(int maxN, double burnRate) {
        this.maxNewton = maxN;
        this.burnRate = burnRate;
        this.percentOn = 0;
    }

    public void setPower(int percent) {
        this.percentOn = percent;
        if (percent > 100) {
            this.percentOn = 100;
        }
        if (percent < 0) {
            this.percentOn = 0;
        }
    }

    public double getPower() {
        return this.maxNewton*((double)this.percentOn/100);
    }

    public double BurnRate() {
        return this.burnRate*((double)this.percentOn/100);
    }
}
