package org.firstinspires.ftc.teamcode.drive.opmode.pp;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.VuMarkInstanceId;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.teamcode.drive.Cone;
import org.firstinspires.ftc.teamcode.drive.intakeslide.IntakeSlideSubsystemAuto;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.drive.opmode.pp.autos.AutoHigh;
import org.firstinspires.ftc.teamcode.drive.opmode.pp.autos.AutoHighMedium;
import org.firstinspires.ftc.teamcode.drive.opmode.pp.autos.AutoMedium;
import org.firstinspires.ftc.teamcode.drive.opmode.pp.autos.LeftHigh;
import org.firstinspires.ftc.teamcode.drive.opmode.pp.autos.LeftTEST;
import org.firstinspires.ftc.teamcode.drive.opmode.pp.autos.RightHigh;

import java.util.ArrayList;
import java.util.List;

@Autonomous(name="0 - All Autos", group="Linear Opmode")
public class AutoInterface extends LinearOpMode {

    private enum Side {
        SELECTING,
        LEFT,
        RIGHT
    }
    private enum Junctions {
        SELECTING,
        HIGH_MEDIUM,
        DOUBLE_MEDIUM,
        MEDIUM_HIGH,
        HIGH,
        LEFT_TEST
    }

    // Selection variables
    private boolean sideSelected = false;
    private boolean junctionsSelected = false;
    private boolean testSelected = false;
    public int startSide = 1;

    // Positions
    public Pose2d LeftConeStack = new Pose2d(-38,-12, Math.toRadians(0));
    public Pose2d RightConeStack = new Pose2d(38,-12, Math.toRadians(180));


    public Pose2d Start = new Pose2d(-34, -62, Math.toRadians(90));
    public Pose2d Low = new Pose2d(-42, -12, Math.toRadians(90));
    public Pose2d Medium = new Pose2d(-24, -12, Math.toRadians(90));
    public Pose2d High = new Pose2d(-24, -12, Math.toRadians(-90));
    public Pose2d BottomHigh = new Pose2d(0, -12,  Math.toRadians(90));

    // Vuforia Variables
    int label = 0;
    int parkDistance = 1;

    private ElapsedTime runtime = new ElapsedTime();

    public static final String TAG = "Vuforia VuMark Sample";

    /**
     * {@link #vuforia} is the variable we will use to store our instance of the Vuforia
     * localization engine.
     */
    VuforiaLocalizer vuforia;
    private static final String VUFORIA_KEY =
            "AYP9k3r/////AAABmbsKp4S4+0RSpbJyMlJGbNQJWbthdpl1gIp8CO+DnDwIDkzifNXPuUMawrPbYmKwDfWtSi+PAKLOcvbHmHZxsTM24Sd32QsBy/RarvDqfIJgEIVDiUXpTlOvKCqFNCS5FGivU6Tz3C5FIhf5N/KapHhETsd2ExGtCtsZSE7QQw5SCjynKE+JvP/DnjZ8eBk6PYlS/TUdvQmonUSTkPwPCEXcL3HVO9Mw+QjvYT0eA93l7yn2NssK+37MjpJBn7kzME8FUmurwynqPJA5Ido5l/iafDl53Hndd+vl0H5ooXY0qVE1mc8HUK5lYoVXMygBDqa9Grkghg8bD791U09C20SnuKdwFCWH0Ic6zZUkeH9o";

    @Override
    public void runOpMode() throws InterruptedException  {

        Side side;
        Junctions junctions;

        side = Side.SELECTING;
        junctions = Junctions.SELECTING;

        // initialize stuffs
        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);
        IntakeSlideSubsystemAuto intakeSlide = new IntakeSlideSubsystemAuto();
        intakeSlide.init(hardwareMap);
        Cone cone = new Cone();
        cone.init(drive, intakeSlide, hardwareMap, this);
        
        // initialize autos
        AutoHighMedium auto1 = new AutoHighMedium();
//        AutoHigh auto2 = new AutoHigh();
        AutoMedium auto3 = new AutoMedium();

        // older autos (for backup)
        RightHigh rightHigh = new RightHigh();
        LeftHigh leftHigh = new LeftHigh();

        // testing programs
        LeftTEST leftTest = new LeftTEST();


        // Vuforia
        initVuforia();
        /**
         * Load the data set containing the VuMarks for Relic Recovery. There's only one trackable
         * in this data set: all three of the VuMarks in the game were created from this one template,
         * but differ in their instance id information.
         * @see VuMarkInstanceId
         */
        VuforiaTrackables targets1 = this.vuforia.loadTrackablesFromAsset("PowerPlay1");
        VuforiaTrackables targets2 = this.vuforia.loadTrackablesFromAsset("PowerPlay2");
        VuforiaTrackables targets3 = this.vuforia.loadTrackablesFromAsset("PowerPlay3");

        // For convenience, gather together all the trackable objects in one easily-iterable collection */
        List<VuforiaTrackable> allTrackables = new ArrayList<VuforiaTrackable>();
        allTrackables.addAll(targets1);
        allTrackables.addAll(targets2);
        allTrackables.addAll(targets3);

        // VuforiaTrackable relicTemplate = relicTrackables.get(0);
        // relicTemplate.setName("relicVuMarkTemplate"); // can help in debugging; otherwise not necessary


        while (!isStopRequested() && !sideSelected) {
            switch (side) {
                case SELECTING:
                    if (gamepad1.left_bumper) {
                        side = Side.LEFT;
                        // reverse X coordinate of Pose2Ds
                        startSide = 1;
                        sideSelected = true;
                    } else if (gamepad1.right_bumper) {
                        side = Side.RIGHT;
                        // keep X coordinate of Pose2Ds the same
                        startSide = -1;
                        sideSelected = true;
                    }
            }
            telemetry.addData("To select LEFT:", "Gamepad Left Bumper");
            telemetry.addData("To select RIGHT:", "Gamepad Right Bumper");
            telemetry.addData("Selected Side:", side.name());
            telemetry.update();
        }
        while (!isStopRequested() && !junctionsSelected) {
            switch (junctions) {
                case SELECTING:
                    if (gamepad1.a) {
                        junctions = Junctions.MEDIUM_HIGH;
                        auto1.init(drive, intakeSlide, cone, this, startSide);
                        junctionsSelected = true;
                    } else if (gamepad1.x) {
                        junctions = Junctions.DOUBLE_MEDIUM;
                        auto3.init(drive, intakeSlide, cone, this, startSide);
                        junctionsSelected = true;
                    } else if (gamepad1.y) {
                        junctions = Junctions.HIGH_MEDIUM;
                        auto1.init(drive, intakeSlide, cone, this, startSide);
                        junctionsSelected = true;
                    } else if (gamepad1.b) {
                        junctions = Junctions.HIGH;
                        leftHigh.init(drive, intakeSlide, cone, this);
                        rightHigh.init(drive, intakeSlide, cone, this);
                        junctionsSelected = true;
                    } else if (gamepad1.dpad_up) {
                        junctions = Junctions.LEFT_TEST;
                        leftTest.init(drive, intakeSlide, cone, this, startSide);
                        auto3.init(drive, intakeSlide, cone, this, startSide);
                        junctionsSelected = true;
                    }
            }
            telemetry.addData("Selected Side:", side.name());
            telemetry.addData("To select HIGH MEDIUM (Not recommended):", "Gamepad Y");
            telemetry.addData("To select CYCLE MEDIUM:", "Gamepad X");
            telemetry.addData("To select MEDIUM HIGH:", "Gamepad A");
            telemetry.addData("To select HIGH:", "Gamepad B");
            telemetry.addData("Run Test:", "DPAD UP");
            telemetry.update();
        }
        // flip or keep x coordinates (doesn't work)
//        initSide();
         /*
            read: Trajectories Overview
            https://learnroadrunner.com/trajectories.html#trajectories-vs-paths

            benefit of sequencer
            https://learnroadrunner.com/trajectory-sequence.html#overview

            reminder:
            Keep this in mind as the turn function will go counter-clockwise.
            all angles are in radian
            each tile is 24 inches (2 feet) ref: https://learnroadrunner.com/trajectories.html#coordinate-system
            always call drive.setPoseEstimate(startPose) to orient the drive ;

         */

        //Vufrofia
        targets1.activate();  // octopus
        targets2.activate(); // triangle
        targets3.activate(); // traffic

        boolean targetVisible = false;
        String targetName = "NOT FOUND";

        runtime.reset();
        // not needed if using robot centric coordinate system
        if (side == Side.RIGHT) {
            while (!isStopRequested() && !opModeIsActive()) {
                if (!targetVisible) {
                    for (VuforiaTrackable trackable : allTrackables) {
                        if ( ((VuforiaTrackableDefaultListener) trackable.getListener()).isVisible()){
                            targetVisible = true;
                            targetName = trackable.getName();
                            if (targetName == "PowerPlay2") {
                                label = 1;
//                                parkDistance = 1;
//                                parkDistance = 12;
                                parkDistance = -24;
                            } else if (targetName == "PowerPlay1") {
                                label = 2;
//                                parkDistance = 24;
//                                parkDistance = 36;
                                parkDistance = 0;
                            } else if (targetName == "PowerPlay3") {
                                label = 3;
//                                parkDistance = 48;
//                                parkDistance = 60;
                                parkDistance = 24;
                            }
                            break;
                        }
                    }
                }
                intakeSlide.setIntakePosition(IntakeSlideSubsystemAuto.IntakeState.IN);
                telemetry.addData("Visible Target", targetName);
                telemetry.addData("Lable #", label);
                telemetry.addData("Parking:", parkDistance);
                telemetry.addData("Selected Side:", side.name());
                telemetry.addData("Selected Auto:", junctions.name());
                telemetry.update();
            }
        } else {
            while (!isStopRequested() && !opModeIsActive()) {
                if (!targetVisible) {
                    for (VuforiaTrackable trackable : allTrackables) {
                        if ( ((VuforiaTrackableDefaultListener) trackable.getListener()).isVisible()){
                            targetVisible = true;
                            targetName = trackable.getName();
                            if (targetName == "PowerPlay2") {
                                label = 1;
//                                parkDistance = 48;
//                                parkDistance = 60;
                                parkDistance = -24;
                            } else if (targetName == "PowerPlay1") {
                                label = 2;
//                                parkDistance = 24;
//                                parkDistance = 36;
                                parkDistance = 0;
                            } else if (targetName == "PowerPlay3") {
                                label = 3;
//                                parkDistance = 1;
//                                parkDistance = 12;
                                parkDistance = 24;
                            }
                            break;
                        }
                    }
                }
                intakeSlide.setIntakePosition(IntakeSlideSubsystemAuto.IntakeState.IN);
                telemetry.addData("Visible Target", targetName);
                telemetry.addData("Zone #", label);
                telemetry.addData("Park distance:", parkDistance);
                telemetry.addData("Selected Side:", side.name());
                telemetry.addData("Selected Auto:", junctions.name());
                telemetry.update();
            }
        }

        intakeSlide.setIntakePosition(IntakeSlideSubsystemAuto.IntakeState.IN);

        // this telemetry will not be seen because of the loop above
        telemetry.addData("Selected Side:", side.name());
        telemetry.addData("Auto selected:", junctions.name());
        telemetry.addData("Zone #", label);
        telemetry.addData(">", "Turn of timer for testing!");
        telemetry.update();
        waitForStart();

        if(isStopRequested()) return;
        runtime.reset();
        // all paths are on the right side by default
        // remember to set poseEstimate in each class
        switch (junctions) {
            case MEDIUM_HIGH:
                auto1.followPath2(parkDistance);
                break;
            case HIGH_MEDIUM:
                auto1.followPath3(parkDistance);
                break;
            case DOUBLE_MEDIUM:
                auto3.followPath2(parkDistance);
                break;
            case HIGH:
                // follow the old high junction and park autos
                switch (side) {
                    case LEFT:
                        leftHigh.followPath(parkDistance);
                        break;
                    case RIGHT:
                        rightHigh.followPath(parkDistance);
                        break;
                }
                break;
            case LEFT_TEST:
                // runs test programs
                while (!testSelected && opModeIsActive()) {
                    if (gamepad1.a) {
                        leftTest.followPath(gamepad1);
                        testSelected = true;
                    }
                    else if (gamepad1.b) {
                        leftTest.followPath2();
                        testSelected = true;
                    }
                    else if (gamepad1.x) {
                        leftTest.runOtherTests(gamepad1);
                        testSelected = true;
                    }
                    telemetry.addData("Run strafe test:", "Gamepad A");
                    telemetry.addData("Run other tests:", "Gamepad X");
                    telemetry.addData("Auto:", "Left Test");
                    telemetry.update();
                }
                break;
        }
        telemetry.update();
        cone.stickDrive.stopCamera();
    }

    /**
     * Initialize the Vuforia localization engine.
     */
    private void initVuforia() {
        /*
         * Configure Vuforia by creating a Parameter object, and passing it to the Vuforia engine.
         */
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = VUFORIA_KEY;
        parameters.cameraName = hardwareMap.get(WebcamName.class, "Webcam 1");

        //  Instantiate the Vuforia engine
        vuforia = ClassFactory.getInstance().createVuforia(parameters);
    }

//    private void initSide() {
//        Start = new Pose2d(34*startSide, -62, Math.toRadians(90));
//        Low = new Pose2d(42*startSide, -12, Math.toRadians(90));
//        Medium = new Pose2d(24*startSide, -12, Math.toRadians(90));
//        High = new Pose2d(24*startSide, -12, Math.toRadians(-90));
//        BottomHigh = new Pose2d(0, -12,  Math.toRadians(90));
//    }
}
