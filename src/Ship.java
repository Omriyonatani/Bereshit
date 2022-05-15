public class Ship {
    private Engine main;
    private Engine topLeft, topRight, bottomLeft, bottomRight, leftTop, leftBottom, rightTop, rightBottom;

    private double pitch;//,roll,yaw; //0-360
    private double angularSpeed;
    private double horizontalSpeed, verticalSpeed;
    private double accelerationVertical, accelerationHorizontal;//X, accelerationY, accelerationZ;

    private double fuelKG;
    private double shipWeightKG;
    private double weightKG;
    private double altitude;//, distance;

    private State state;


    public Ship() {
        this.main = new Engine(430,0.15);
        this.topLeft = new Engine(25,0.009);
        this.topRight = new Engine(25,0.009);
        this.bottomLeft = new Engine(25,0.009);
        this.bottomRight = new Engine(25,0.009);
        this.leftTop = new Engine(25,0.009);
        this.leftBottom = new Engine(25,0.009);
        this.rightTop = new Engine(25,0.009);
        this.rightBottom = new Engine(25,0.009);
        this.pitch = 53;//this.roll = this.yaw = 0;
        this.angularSpeed = 0;
        this.horizontalSpeed = 1700;
        this.verticalSpeed = 43;
        double ang_rad = Math.toRadians(this.pitch);
        this.accelerationVertical = Math.sin(ang_rad)*1.2;
        this.accelerationHorizontal = Math.cos(ang_rad)*1.2;
        this.fuelKG = 216;
        this.shipWeightKG = 165;
        this.weightKG = this.fuelKG+shipWeightKG;
        this.altitude = 30000;
        this.state = State.orientation;
        //this.distance = 181055;
    }
    public void updateWeight(double dt) {
        double totalBurnRate = 0;
        totalBurnRate += this.main.BurnRate();
        totalBurnRate += this.topLeft.BurnRate();
        totalBurnRate += this.topRight.BurnRate();
        totalBurnRate += this.bottomLeft.BurnRate();
        totalBurnRate += this.bottomRight.BurnRate();
        totalBurnRate += this.leftTop.BurnRate();
        totalBurnRate += this.leftBottom.BurnRate();
        totalBurnRate += this.rightTop.BurnRate();
        totalBurnRate += this.rightBottom.BurnRate();
        double changeOfFuel = dt * totalBurnRate;
        this.fuelKG -=changeOfFuel;
        this.weightKG = this.shipWeightKG + this.fuelKG;
    }
    public void updateAngularSpeed(double dt) {
        double angularSpeedChange = 0;
        angularSpeedChange += (double)this.topLeft.percentOn/(2*100);
        angularSpeedChange -= (double)this.topRight.percentOn/(2*100);
        angularSpeedChange += (double)this.bottomLeft.percentOn/(2*100);
        angularSpeedChange -= (double)this.bottomRight.percentOn/(2*100);
        angularSpeedChange += (double)this.leftTop.percentOn/100;
        angularSpeedChange += (double)this.leftBottom.percentOn/100;
        angularSpeedChange -= (double)this.rightTop.percentOn/100;
        angularSpeedChange -= (double)this.rightBottom.percentOn/100;
        this.angularSpeed += angularSpeedChange;
        this.pitch += angularSpeed*dt;
        while (this.pitch < 0) {
            this.pitch += 360;
        }
        this.pitch%=360;
    }
    public void updateAcceleration() {
        double power = 0;
        power += this.main.getPower();
        power += this.topLeft.getPower();
        power += this.topRight.getPower();
        power += this.bottomLeft.getPower();
        power += this.bottomRight.getPower();
        power += this.leftTop.getPower();
        power += this.leftBottom.getPower();
        power += this.rightTop.getPower();
        power += this.rightBottom.getPower();
        //double weightInMoon = Moon.inMoonRange(altitude) ? weightKG*Moon.MoonGravity : 0;
        double weightInMoon = weightKG*Moon.MoonGravity;
        //https://www.grc.nasa.gov/www/k-12/VirtualAero/BottleRocket/airplane/rktpow.html
        double ang_rad = Math.toRadians(this.pitch);
        //System.out.println("ido: " + (power*Math.cos(ang_rad) - weightInMoon) / weightKG + ", boaz: " + (((power*Math.cos(ang_rad)/* - weightInMoon*/) / weightKG) - Moon.getAcc(horizontalSpeed)));
        accelerationVertical = (power*Math.cos(ang_rad) - weightInMoon) / weightKG;
        //accelerationVertical -= Moon.getAcc(horizontalSpeed);
        accelerationHorizontal = (power*Math.sin(ang_rad)) / weightKG;
    }



    public boolean update(double time, double dt) {
        //System.out.printf("%.2d\t %.2lf\t %.2lf\t %.2lf\t %.2lf\t %.2lf\t %.2lf\t %.2lf\t %.2lf\t %.2lf",time, verticalSpeed,horizontalSpeed,altitude,pitch,weightKG,accelerationVertical,accelerationHorizontal,fuelKG);
        System.out.println(time+"\t"+verticalSpeed+"\t"+horizontalSpeed+"\t"+altitude+"\t"+pitch+"\t"+weightKG+"\t"+accelerationVertical + "\t" + accelerationHorizontal +"\t"+fuelKG+",,,");
        updateAngularSpeed(dt);
        updateWeight(dt);
        if (fuelKG > 0) {
            updateAcceleration();
        }else {
            return false;
        }


        //double ang_rad = Math.toRadians(this.pitch);
        //double h_acc = Math.sin(ang_rad)*acceleration;
        //double v_accUp = Math.cos(ang_rad)*acceleration;
        //double v_accDown = Moon.getAcc(this.horizontalSpeed);

        //v_accUp -=v_accDown;
        //System.out.println("vertical acc: " + v_accUp);
        horizontalSpeed +=-1*accelerationHorizontal*dt;
        verticalSpeed +=-1*accelerationVertical*dt;
        altitude -= dt*verticalSpeed;
        if (altitude < 25000) {
            this.state = State.braking;
        }
        return true;
    }

    public boolean pushRequest(double degrees, double up) {
        if ((this.angularSpeed >= 3 && degrees < 0) || (this.angularSpeed <= -3 && degrees >= 0)) {
            return false;
        }
        if (degrees < 0) {
            pushRight(-1*degrees, up);
        }else {
            pushLeft(degrees, up);
        }
        return true;
    }

    public void pushLeft(double degrees, double up) {
        double maxDegrees = 3;
        if (degrees > maxDegrees) {
            degrees = maxDegrees;
        }
        double ratio = degrees/maxDegrees;
        int percent = (int)(100*ratio);
        int oldPercent = 0;
        if (up > 0) {
            double missing = 100 - percent;
            if (up > missing) {
                oldPercent = 100-percent;
                percent = 100;
            } else {
                percent += up;
                oldPercent = (int)up;
            }
        }
        topRight.setPower(percent);
        bottomRight.setPower(percent);
        rightTop.setPower(percent);
        rightBottom.setPower(percent);
        topLeft.setPower(oldPercent);
        bottomLeft.setPower(oldPercent);
        leftTop.setPower(oldPercent);
        leftBottom.setPower(oldPercent);
    }

    public void pushRight(double degrees, double up) {
        double maxDegrees = 3;
        if (degrees > maxDegrees) {
            degrees = maxDegrees;
        }
        double ratio = degrees/maxDegrees;
        int percent = (int)(100*ratio);
        int oldPercent = 0;
        if (up > 0) {
            double missing = 100 - percent;
            if (up > missing) {
                oldPercent = 100-percent;
                percent = 100;
            } else {
                percent += up;
                oldPercent = (int)up;
            }
        }
        topLeft.setPower(percent);
        bottomLeft.setPower(percent);
        leftTop.setPower(percent);
        leftBottom.setPower(percent);
        topRight.setPower(oldPercent);
        bottomRight.setPower(oldPercent);
        rightTop.setPower(oldPercent);
        rightBottom.setPower(oldPercent);
    }
    public State getState() {
        return this.state;
    }

    public double getAngularSpeed() {
        return this.angularSpeed;
    }

    public double getMaxEnginePower() {
        return  this.getMain().maxNewton + this.topLeft.maxNewton + this.topRight.maxNewton + this.bottomLeft.maxNewton + this.bottomRight.maxNewton + this.leftTop.maxNewton + this.leftBottom.maxNewton + this.rightTop.maxNewton + this.rightBottom.maxNewton;
    }

    public double getAccelerationVertical() {
        return accelerationVertical;
    }

    public void setAccelerationVertical(double accelerationVertical) {
        this.accelerationVertical = accelerationVertical;
    }

    public double getAccelerationHorizontal() {
        return accelerationHorizontal;
    }

    public void setAccelerationHorizontal(double accelerationHorizontal) {
        this.accelerationHorizontal = accelerationHorizontal;
    }

//    public double getAccelerationX() {
//        return accelerationX;
//    }
//
//    public void setAccelerationX(double accelerationX) {
//        this.accelerationX = accelerationX;
//    }
//
//    public double getAccelerationY() {
//        return accelerationY;
//    }
//
//    public void setAccelerationY(double accelerationY) {
//        this.accelerationY = accelerationY;
//    }
//
//    public double getAccelerationZ() {
//        return accelerationZ;
//    }
//
//    public void setAccelerationZ(double accelerationZ) {
//        this.accelerationZ = accelerationZ;
//    }

    public double getFuelKG() {
        return fuelKG;
    }

    public void setFuelKG(double fuelKG) {
        this.fuelKG = fuelKG;
    }

    public double getWeightKG() {
        return weightKG;
    }

    public void setWeightKG(double weightKG) {
        this.weightKG = weightKG;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

//    public double getDistance() {
//        return distance;
//    }
//
//    public void setDistance(double distance) {
//        this.distance = distance;
//    }


    public Engine getMain() {
        return main;
    }

    public void setMain(Engine main) {
        this.main = main;
    }

    public Engine getTopLeft() {
        return topLeft;
    }

    public void setTopLeft(Engine topLeft) {
        this.topLeft = topLeft;
    }

    public Engine getTopRight() {
        return topRight;
    }

    public void setTopRight(Engine topRight) {
        this.topRight = topRight;
    }

    public Engine getBottomLeft() {
        return bottomLeft;
    }

    public void setBottomLeft(Engine bottomLeft) {
        this.bottomLeft = bottomLeft;
    }

    public Engine getBottomRight() {
        return bottomRight;
    }

    public void setBottomRight(Engine bottomRight) {
        this.bottomRight = bottomRight;
    }

    public Engine getLeftTop() {
        return leftTop;
    }

    public void setLeftTop(Engine leftTop) {
        this.leftTop = leftTop;
    }

    public Engine getLeftBottom() {
        return leftBottom;
    }

    public void setLeftBottom(Engine leftBottom) {
        this.leftBottom = leftBottom;
    }

    public Engine getRightTop() {
        return rightTop;
    }

    public void setRightTop(Engine rightTop) {
        this.rightTop = rightTop;
    }

    public Engine getRightBottom() {
        return rightBottom;
    }

    public void setRightBottom(Engine rightBottom) {
        this.rightBottom = rightBottom;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

//    public double getRoll() {
//        return roll;
//    }
//
//    public void setRoll(double roll) {
//        this.roll = roll;
//    }
//
//    public double getYaw() {
//        return yaw;
//    }
//
//    public void setYaw(double yaw) {
//        this.yaw = yaw;
//    }

    public double getHorizontalSpeed() {
        return horizontalSpeed;
    }

    public void setHorizontalSpeed(double horizontalSpeed) {
        this.horizontalSpeed = horizontalSpeed;
    }

    public double getVerticalSpeed() {
        return verticalSpeed;
    }

    public void setVerticalSpeed(double verticalSpeed) {
        this.verticalSpeed = verticalSpeed;
    }

//    public double getAcceleration() {
//        return accelerationX;
//    }
//
//    public void setAcceleration(double acceleration) {
//        this.accelerationX = acceleration;
//    }

    public void enginesOff() {
        this.getMain().setPower(0);
        this.getTopLeft().setPower(0);
        this.getTopRight().setPower(0);
        this.getBottomLeft().setPower(0);
        this.getBottomRight().setPower(0);
        this.getLeftTop().setPower(0);
        this.getLeftBottom().setPower(0);
        this.getRightTop().setPower(0);
        this.getRightBottom().setPower(0);
    }
}