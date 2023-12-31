package org.firstinspires.ftc.teamcode.drive.opmode.pp.autos.old;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.VuMarkInstanceId;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.teamcode.drive.Cone;
import org.firstinspires.ftc.teamcode.drive.intakeslide.IntakeSlideSubsystemAuto;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.trajectorysequence.TrajectorySequence;

import java.util.ArrayList;
import java.util.List;

@Disabled
@Autonomous(name="4_RIGHT(HIGH, MEDIUM)", group="Linear Opmode")
public class RightHighMedium extends LinearOpMode {



    private ElapsedTime runtime = new ElapsedTime();

    public static final String TAG = "Vuforia VuMark Sample";
    OpenGLMatrix lastLocation = null;

    /**
     * {@link #vuforia} is the variable we will use to store our instance of the Vuforia
     * localization engine.
     */
    VuforiaLocalizer vuforia;
    private static final String VUFORIA_KEY =
            "AYP9k3r/////AAABmbsKp4S4+0RSpbJyMlJGbNQJWbthdpl1gIp8CO+DnDwIDkzifNXPuUMawrPbYmKwDfWtSi+PAKLOcvbHmHZxsTM24Sd32QsBy/RarvDqfIJgEIVDiUXpTlOvKCqFNCS5FGivU6Tz3C5FIhf5N/KapHhETsd2ExGtCtsZSE7QQw5SCjynKE+JvP/DnjZ8eBk6PYlS/TUdvQmonUSTkPwPCEXcL3HVO9Mw+QjvYT0eA93l7yn2NssK+37MjpJBn7kzME8FUmurwynqPJA5Ido5l/iafDl53Hndd+vl0H5ooXY0qVE1mc8HUK5lYoVXMygBDqa9Grkghg8bD791U09C20SnuKdwFCWH0Ic6zZUkeH9o";

    @Override
    public void runOpMode() throws InterruptedException  {

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

        boolean coneThere = false;

        int label = 0;
        int parkDistance = 1;

        SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);

        // cone
        IntakeSlideSubsystemAuto intakeSlide = new IntakeSlideSubsystemAuto();
        intakeSlide.init(hardwareMap);
        Cone cone = new Cone();
        cone.init(drive, intakeSlide, hardwareMap, this);

//        // intake
//        IntakeSlideSubsystem2 intakeSlide2 = new IntakeSlideSubsystem2();
//        intakeSlide2.init(hardwareMap);

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

        // we assume A2/F5 is starting point, the robot back is facing the wall
        Pose2d startPose = new Pose2d(0, 0, 0);

        drive.setPoseEstimate(startPose);

        // run to bottom high junction
        TrajectorySequence trajSeq = drive.trajectorySequenceBuilder(startPose)
                .forward(2)
                .strafeLeft(24)
                .forward(48)
                .strafeLeft(8)
                .addTemporalMarker(() -> {
                    intakeSlide.liftState = IntakeSlideSubsystemAuto.LiftState.PICKUP2;
                    intakeSlide.run();
                })
                .waitSeconds(0.5)
                .resetConstraints()
                .build();


        //Vufrofia
        targets1.activate();  // octopus
        targets2.activate(); // triangle
        targets3.activate(); // traffic

        boolean targetVisible = false;
        String targetName = "NOT FOUND";

        runtime.reset();
        while (!isStopRequested() && !opModeIsActive()) {
            if (!targetVisible) {
                for (VuforiaTrackable trackable : allTrackables) {
                    if ( ((VuforiaTrackableDefaultListener) trackable.getListener()).isVisible()){
                        targetVisible = true;
                        targetName = trackable.getName();
                        if (targetName == "PowerPlay2") {
                            label = 1;
                            parkDistance = 1;
                        } else if (targetName == "PowerPlay1") {
                            label = 2;
                            parkDistance = 24;
                        } else if (targetName == "PowerPlay3") {
                            label = 3;
                            parkDistance = 42;
                        }
                        break;
                    }
                }
            }
            intakeSlide.setIntakePosition(IntakeSlideSubsystemAuto.IntakeState.IN);
            telemetry.addData("Visible Target", targetName);
            telemetry.addData("Lable #", label);
            telemetry.update();
        }
        intakeSlide.setIntakePosition(IntakeSlideSubsystemAuto.IntakeState.IN);

        telemetry.addData("Check to see if camera is aligned?", "Can it detect well?");
        telemetry.addData(">", "Press Play to start");
        telemetry.update();
        waitForStart();

        if(isStopRequested()) return;
        runtime.reset();

        drive.followTrajectorySequence(trajSeq);
        // Put align code here? [import Cone.java and call a function to drop off cone]
        cone.dropOffCone(-0.20, IntakeSlideSubsystemAuto.LiftState.HIGH, false);
        Pose2d afterAdjPose = drive.getPoseEstimate();
        // go to ready position
        TrajectorySequence trajSeq2 = drive.trajectorySequenceBuilder(afterAdjPose)
                .strafeRight(9.75)
                .turn(Math.toRadians(90))
                .back(24)
                .strafeRight(4)
                .build();
        TrajectorySequence rotateTo = drive.trajectorySequenceBuilder(trajSeq2.end())
                .forward(20)
                .strafeLeft(10)
                .build();
        TrajectorySequence rotateBack = drive.trajectorySequenceBuilder(rotateTo.end())
                .strafeRight(9.75)
                .back(20)
                .build();


        drive.followTrajectorySequence(trajSeq2);

        for (int i = 0; i < 1; i++) {
            cone.pickUpCone();
            drive.followTrajectorySequence(rotateTo);
            cone.dropOffCone(-0.2, IntakeSlideSubsystemAuto.LiftState.MEDIUM, coneThere);
            //drive.followTrajectorySequence(rotateBack);
            coneThere = true;
        }

        TrajectorySequence park = drive.trajectorySequenceBuilder(rotateTo.end())
                .strafeRight(7.5)
                .back(parkDistance)
                .turn(Math.toRadians(-90))
                .back(5)
                .build();
        drive.followTrajectorySequence(park);

        // the last thing auto should do is move slide back to rest
        moveSlide(intakeSlide, intakeSlide.targetPositionRest, 30);
        telemetry.update();
    }


    /**
     *  @param slides
     * @param position
     * @param timeoutS
     */
    public void moveSlide(IntakeSlideSubsystemAuto slides, int position, double timeoutS){

        ElapsedTime runtime = new ElapsedTime();


        // NOTE all while loop in op mode should check for
        // opModeIsActive
        while ( opModeIsActive() &&
                (runtime.seconds() < timeoutS)) {

            // run and  keep the position until timeout
            slides.runToPosition(position);

            // Display it for the driver.
            telemetry.addData("Slide to",  " %7d", position);
            telemetry.update();
        }
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
}
// Old Code
//    TrajectorySequence trajSeq = drive.trajectorySequenceBuilder(startPose)
//                .setTurnConstraint(DriveConstants.MAX_ANG_VEL_MEDIUM, DriveConstants.MAX_ANG_ACCE_MEDIUM)
//                .setConstraints(SampleMecanumDrive.VEL_CONSTRAINT ,SampleMecanumDrive.ACCEL_CONSTRAINT) // max speed
//                .addTemporalMarker(() -> {
//                    // intake code goes here:
//                    intakeSlide2.setIntakePower(IntakeSlide.IntakeState.IN);
//                })
//                .waitSeconds(2)
//                .addTemporalMarker(() -> {
//                    // intake code goes here:
//                    intakeSlide2.setIntakePower(IntakeSlide.IntakeState.STOP);
//                })
//                .waitSeconds(.5)
//                .forward(1)
//                .strafeRight(24)
//                .forward(49)
//                .strafeRight(11) // to align with junction //changed 1/7/2023
//                .addTemporalMarker(() -> {
//                    intakeSlide2.runToPosition(intakeSlide2.targetPositionHigh);
//                })
//                .waitSeconds(3)
//                .back(7.5)
//                .waitSeconds(2)
//                .addTemporalMarker(() -> {
//                    // intake code goes here:
//                    intakeSlide2.setIntakePower(IntakeSlide.IntakeState.OUT);
//                })
//                .waitSeconds(2)
//                .addTemporalMarker(() -> {
//                    // intake code goes here:
//                    intakeSlide2.setIntakePower(IntakeSlide.IntakeState.STOP);
//                })
//                .waitSeconds(.5)
//                .forward(7)
//                .addTemporalMarker(() -> {
//                    // intake code goes here:
//                    // moveSlide(intakeSlide2, intakeSlide2.targetPositionHigh, 4);
//                    intakeSlide2.runToPosition(intakeSlide2.targetPositionRest);
//                })
//                .waitSeconds(3)
//                .strafeLeft(9.75)
//                .back(24)
//                .strafeLeft(parkDistance)
//                .resetConstraints()
//                .build();