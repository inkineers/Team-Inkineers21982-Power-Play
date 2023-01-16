package org.firstinspires.ftc.teamcode.cv;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.opencv.core.Rect;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvWebcam;

public class StickDriveMediator {

    public final int WIDTH = 640;
    public final int HEIGHT = 480;
    private OpenCvWebcam webcam;
    private StickObserverPipeline opencv = null;
    private LinearOpMode op;
    private SampleMecanumDrive drive;
    private DistanceSensor sensorRange;
    private DigitalChannel redLED;
    private DigitalChannel greenLED;

    private double LATERAL_ERROR_THRESHOLD = 0.1;
    private double DISTANCE_ERROR_THRESHOLD = 0.1;
    private double DISTANCE_JUNCTION = 13; // cm
    private double DISTANCE_JUNCTION_MAX = 20; // cm


    // Setter
    public void setDrive(SampleMecanumDrive drive) {
        this.drive = drive;
    }

    public StickDriveMediator(LinearOpMode p_op){
        //you can input  a hardwareMap instead of linearOpMode if you want
        op = p_op;
        //initialize webcam
        // note: assume that there is a second camera on top of intake

        int cameraMonitorViewId = op.hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", op.hardwareMap.appContext.getPackageName());
        /**
         * This is the only thing you need to do differently when using multiple cameras.
         * Instead of obtaining the camera monitor view and directly passing that to the
         * camera constructor, we invoke {@link OpenCvCameraFactory#splitLayoutForMultipleViewports(int, int, OpenCvCameraFactory.ViewportSplitMethod)}
         * on that view in order to split that view into multiple equal-sized child views,
         * and then pass those child views to the constructor.
         */
        int[] viewportContainerIds = OpenCvCameraFactory.getInstance()
                .splitLayoutForMultipleViewports(
                        cameraMonitorViewId, //The container we're splitting
                        2, //The number of sub-containers to create
                        OpenCvCameraFactory.ViewportSplitMethod.VERTICALLY); //Whether to split the container vertically or horizontally


        webcam = OpenCvCameraFactory.getInstance().createWebcam(op.hardwareMap.get(WebcamName.class, "Webcam 2"), viewportContainerIds[1]);
        // webcam = OpenCvCameraFactory.getInstance().createWebcam(op.hardwareMap.get(WebcamName.class, "Webcam 2"));

        // initialize distance sensor
        sensorRange = op.hardwareMap.get(DistanceSensor.class, "sensor_range");
        redLED = op.hardwareMap.get(DigitalChannel.class, "red");
        greenLED = op.hardwareMap.get(DigitalChannel.class, "green");

    }

    public void alignStick(double lateralAlignmentTime, double distanceAlignmentTime){
         ElapsedTime timer = new ElapsedTime();
         double LateralError = 1;
         double distanceError = 1;

         while (timer.seconds() <= lateralAlignmentTime && LateralError > LATERAL_ERROR_THRESHOLD ){
             LateralError = alignStickLateral(LATERAL_ERROR_THRESHOLD);
         }

         timer.reset();
        while (timer.seconds() <= distanceAlignmentTime && distanceError > DISTANCE_ERROR_THRESHOLD ){
            distanceError = alignStickDistance(DISTANCE_JUNCTION, DISTANCE_JUNCTION_MAX, DISTANCE_ERROR_THRESHOLD);
        }


    }

    /**
     *  approach the junction automtically ; assume that the left right lateral alignment is correct
     *  using the PID approach
     * @param targetDistance e.g. 13 cm
     * @return
     */
    public double alignStickDistance(double targetDistance, double maxRange, double thresHold){
        double error = 0;

        // assume the robot is close to the junction/stack but not crazy far
        // if the robot distance is really far, i.e. exceed max Range, don't do anything
        error = (sensorRange.getDistance(DistanceUnit.CM) - targetDistance) / targetDistance ;
        error = error > 1 ? 1: error; // cap as 1
        if (error > thresHold &&  sensorRange.getDistance(DistanceUnit.CM) < maxRange ) {

            double leftXControl = 0;
            double leftYControl = 1 * error; // test and see if it needs to reduce the speed or not
            double rightXControl = 0;

            drive.setWeightedDrivePower(
                    new Pose2d(
                            -leftYControl,
                            -leftXControl,
                            -rightXControl
                    )
            );
            drive.update();

            greenLED.setState(false);
            redLED.setState(true);
        } else {
            // stop
            drive.setWeightedDrivePower(
                    new Pose2d(
                            0,
                            0,
                            0
                    )
            );
            drive.update();

            greenLED.setState(true);
            redLED.setState(false);
        }


        return error;
    }

    /**
     *  assumption: observe stick has been called and it will continue to stream and update maxRect
     *  inspiration: Ultimate Goal align with ring using EasyOpenCV
     *  https://www.youtube.com/watch?v=_Hxn4fzfN7k
     * @return error from the observation
     */
    public double  alignStickLateral(double thresHold){

        double error = 0;

        // first observe stick location
        if (opencv != null ) {
            Rect stick = opencv.getStickRect();
            if (stick != null && stick.width > 0) {
                // stick midpoint - screen midpoint
                double stickMidpoint = stick.x + stick.width / 2;
                error = stickMidpoint - WIDTH / 2;
                error = (error / (WIDTH / 2));

                // using the principle of PID
                if (Math.abs(error) > thresHold) {
                    double leftXControl = error; // assume camera is mount at the back of robot
                    double leftYControl = 0;
                    double rightXControl = 0;

                    drive.setWeightedDrivePower(
                            new Pose2d(
                                    -leftYControl,
                                    -leftXControl,
                                    -rightXControl
                            )
                    );
                    drive.update();

                    greenLED.setState(false);
                    redLED.setState(true);
                } else {
                    // stop
                    drive.setWeightedDrivePower(
                            new Pose2d(
                                    0,
                                    0,
                                    0
                            )
                    );
                    drive.update();

                    greenLED.setState(true);
                    redLED.setState(false);
                }
            }
        }


        return error;

    }

    public void observeStick(){
        //create the pipeline
        opencv = new StickObserverPipeline();

        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
        {
            @Override
            public void onOpened()
            {
                /*
                 * Tell the webcam to start streaming images to us! Note that you must make sure
                 * the resolution you specify is supported by the camera. If it is not, an exception
                 * will be thrown.
                 *
                 * Keep in mind that the SDK's UVC driver (what OpenCvWebcam uses under the hood) only
                 * supports streaming from the webcam in the uncompressed YUV image format. This means
                 * that the maximum resolution you can stream at and still get up to 30FPS is 480p (640x480).
                 * Streaming at e.g. 720p will limit you to up to 10FPS and so on and so forth.
                 *
                 * Also, we specify the rotation that the webcam is used in. This is so that the image
                 * from the camera sensor can be rotated such that it is always displayed with the image upright.
                 * For a front facing camera, rotation is defined assuming the user is looking at the screen.
                 * For a rear facing camera or a webcam, rotation is defined assuming the camera is facing
                 * away from the user.
                 */
                webcam.setPipeline(opencv);
                //start streaming the camera
                webcam.startStreaming(WIDTH, HEIGHT, OpenCvCameraRotation.UPSIDE_DOWN);
                //if you are using dashboard, update dashboard camera view
                // dashboard : 192.168.43.1:8080/dash
                FtcDashboard.getInstance().startCameraStream(webcam, 5);

            }

            @Override
            public void onError(int errorCode)
            {
                /*
                 * This will be called if the camera could not be opened
                 */
            }
        });
    }

    //stop streaming
    public void stopCamera(){
        webcam.stopStreaming();
    }
}