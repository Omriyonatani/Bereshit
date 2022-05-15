import java.awt.desktop.SystemEventListener;
import java.util.concurrent.TimeUnit;

public class main {

    public static void main(String[] args) throws InterruptedException {
        Ship bereshit = new Ship();
        double time = 0;
        double dt = 1;
        double startSpeed = bereshit.getVerticalSpeed();
        System.out.println("time, vs, hs,  alt, ang,weight,acc,fuel");
        double startSpeedRatio = bereshit.getHorizontalSpeed()/bereshit.getVerticalSpeed();
        double hypotenuse = Math.sqrt(Math.pow(bereshit.getVerticalSpeed(),2) + Math.pow(bereshit.getHorizontalSpeed(),2));
        double startSpeedAngle = Math.toDegrees(Math.asin(bereshit.getHorizontalSpeed()/hypotenuse));

        double brakingAccelerationHypotenuse = Math.sqrt(Math.pow(1,2)+Math.pow(1+Moon.MoonGravity,2));
        double brakingAccelerationAngle = Math.toDegrees(Math.asin((1) / brakingAccelerationHypotenuse));

        double orientationVerticalSpeed = 35;
        double brakingVertialSpeed = 5;
        System.out.println(brakingAccelerationAngle);
        System.out.println(startSpeedAngle);
        //vertical pid
        PID altPID = new PID(1,1/2,1/2,100, 0);
        PID speedPID = new PID(30,0,1,10000,0);
        PID anglePID = new PID(0.05,0,0.5,10000,-10000);
        PID ratioPID = new PID(50,0,0,50000,-50000);
        PID accelPID = new PID(-100000,0,0.005,10000,-1000);
        PID horizPID = new PID(0.06,0,0,10000,-10000);
        //PID anglePID = new PID(1,1,-1,360,0);
        System.out.println("start angular speed: " + bereshit.getAngularSpeed());
        while (bereshit.getAltitude()>0) {
            bereshit.enginesOff();
            double speed = Math.sqrt(Math.pow(bereshit.getVerticalSpeed(),2) + Math.pow(bereshit.getHorizontalSpeed(),2));
            double altError = speed;
            double wantedVerticalSpeed = bereshit.getState() == State.orientation ? orientationVerticalSpeed: brakingVertialSpeed;
            double wantedAngle = bereshit.getState() == State.orientation ? 6 : 32;
            double pitchOffset = Math.abs(bereshit.getPitch()-45);
            double speedRatio = bereshit.getHorizontalSpeed() / bereshit.getVerticalSpeed();
            double error = 0;
            boolean ratioFlag = false;
            double pidOUT = altPID.update(bereshit.getAltitude(), dt);
            double speedOUT = speedPID.update(bereshit.getVerticalSpeed()-wantedVerticalSpeed, dt);
            double angleOUT = anglePID.update(bereshit.getPitch()-wantedAngle,dt);
            double ratioOUT = ratioPID.update(speedRatio-startSpeedRatio,dt);
            double accelOUT = accelPID.update(bereshit.getAccelerationVertical(),dt);
            double horizOUT = horizPID.update(bereshit.getHorizontalSpeed(),dt);
            if (bereshit.getPitch() > 180) {
                angleOUT *= -1;
            }
            bereshit.pushRequest(angleOUT,speedOUT);
            if (bereshit.getPitch() < 90 && bereshit.getPitch() > 0) {
                if (Math.abs(bereshit.getVerticalSpeed()-wantedVerticalSpeed) > 2.5) {
                    bereshit.getMain().setPower((int)speedOUT);
                    if (Math.abs(bereshit.getPitch()-wantedAngle) < 1) {
                        bereshit.getTopRight().setPower((int)speedOUT);
                        bereshit.getBottomRight().setPower((int)speedOUT);
                        bereshit.getRightTop().setPower((int)speedOUT);
                        bereshit.getRightBottom().setPower((int)speedOUT);
                        bereshit.getTopLeft().setPower((int)speedOUT);
                        bereshit.getBottomLeft().setPower((int)speedOUT);
                        bereshit.getLeftTop().setPower((int)speedOUT);
                        bereshit.getLeftBottom().setPower((int)speedOUT);
                    }

                }else {
                    bereshit.getMain().setPower((int)horizOUT);
                }
            }

            if (!bereshit.update(time, dt)) {
                System.out.println("out of fuel");
                return;
            }

            //System.out.println(".....: " + bereshit.getAngularSpeed() + ", altpid: " + out);//+ ", anglePID: "+ angleOUT);
            TimeUnit.MILLISECONDS.sleep(100);
            time+=dt;
        }
        if (bereshit.getVerticalSpeed() < 2.5 && bereshit.getHorizontalSpeed() < 2.5) {
            System.out.println("landed");
        }else{
            System.out.println("crashed, speed too high");
        }
    }
}
