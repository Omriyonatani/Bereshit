public class PID {
    private double p, i, d;
    private double max_i, min_i, integral, last_error;
    private boolean first_run;
    public PID(double p, double i, double d, double max_i, double min_i) {
        this.p=p;
        this.i=i;
        this.d=d;
        this.max_i=max_i;
        this.min_i=min_i;
        this.last_error=0;
        this.first_run=true;
    }

    public double update(double error, double dt) {
        if (first_run) {
            last_error = error;
            first_run = false;
        }
        integral+=i*error*dt;
        double diff = (error-last_error)/dt;
        double const_integral = constrain(integral,max_i,min_i);
        double control_out = p*error + const_integral + d*diff;
        last_error = error;
        return control_out;
    }

    public static double constrain(double val, double max, double min) {
        if (val > max) {
            val = max;
        }
        if (val < min) {
            val = min;
        }
        return val;
    }
}
