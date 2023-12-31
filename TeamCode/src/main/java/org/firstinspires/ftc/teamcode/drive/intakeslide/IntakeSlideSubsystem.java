package org.firstinspires.ftc.teamcode.drive.intakeslide;

import androidx.annotation.NonNull;

import com.arcrobotics.ftclib.gamepad.GamepadEx;
import com.arcrobotics.ftclib.gamepad.GamepadKeys;
import com.arcrobotics.ftclib.gamepad.TriggerReader;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.drive.intakeslide.IntakeSlide;

/**
 * The IntakeSlideSubsystem class is a subsystem module that control
 * the elevator and roller intake used in PowerPlay
 *
 * @author  MY, AN, SL
 * @version 1.0
 * @since   2022-10-20
 */
public class IntakeSlideSubsystem extends IntakeSlide {

    // THESE VARIABLES ARE USED BY THIS IMPLEMENTATION OF DRIVE CONTROL
    // Declare OpMode members.

    // Variable to detect on press and on release
    private int i = 0;

    // Variable to auto spin in intake
    private boolean autoIn = false;

    /**
     *
     * @param hardwareMap from teleop
     */
    @Override
    public void init(@NonNull HardwareMap hardwareMap) {

        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone).
        slides = hardwareMap.get(DcMotor.class, "slides");
        slides.setDirection(DcMotorSimple.Direction.REVERSE);
        slides.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        slides.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        slides.setMode(DcMotor.RunMode.RUN_USING_ENCODER);


        intake = hardwareMap.get(CRServo.class, "intake");

        currentCaption = "Lift Status";
        currentStatus = "Initialized";
        currentTarget = 0;
        currentPower = 0;

        intakeState = IntakeState.STOP;
        liftState = LiftState.REST;


    }

    public int getDpadPressed() { return i; }
    @Override
    public void run(GamepadEx gamepad1, GamepadEx gamepad2) {
        TriggerReader rtReader1 = new TriggerReader(gamepad1, GamepadKeys.Trigger.RIGHT_TRIGGER);
        switch (liftState) {
            case REST:
                // stops intake when slides hit rest
                if (slides.getCurrentPosition() == currentTarget) {
                    autoIn = false;
                }
                if (gamepad1.wasJustPressed(GamepadKeys.Button.RIGHT_BUMPER)) {
                    // code here
                    currentTarget = targetPositionLow;
                    liftState = LiftState.LOW;
                } else if (rtReader1.isDown()) {
                    // code here
                    autoIn = false;
                    currentTarget = targetPositionPickup;
                    liftState = LiftState.PICKUP;
                } else if (gamepad1.wasJustPressed(GamepadKeys.Button.LEFT_BUMPER)) {
                    autoIn = false;
                } else if (gamepad1.wasJustPressed(GamepadKeys.Button.DPAD_UP)) {
                    i++;
                }
                setSlidePower();
                //if (gamepad1.x || gamepad1.y) {
                //   liftState = LiftState.MANUAL;
                //}
                break;
            case PICKUP:
                if (!rtReader1.isDown()) {
                    currentTarget = targetPositionRest;
                    liftState = LiftState.REST;
                    autoIn = true;
                } else if (gamepad1.wasJustPressed(GamepadKeys.Button.RIGHT_BUMPER)) {
                    currentTarget = targetPositionLow;
                    liftState = LiftState.LOW;
                } else if (gamepad1.wasJustPressed(GamepadKeys.Button.LEFT_BUMPER)) {
                    currentTarget = targetPositionRest;
                    liftState = LiftState.REST;
                    autoIn = false;
                } else if (slides.getCurrentPosition() < targetPositionMedium) {
                    // add position to pick up from stack all the way to LOW
                    currentTarget += 2;
                } else {
                    setSlidePower();
                }
                break;
            case LOW:
                if (gamepad1.wasJustPressed(GamepadKeys.Button.RIGHT_BUMPER)) {
                    // code here
                    currentTarget = targetPositionMedium;
                    liftState = LiftState.MEDIUM;
                } else if (gamepad1.wasJustPressed(GamepadKeys.Button.LEFT_BUMPER)) {
                    // code here
                    currentTarget = targetPositionRest;
                    liftState = LiftState.REST;
                } else {
                    setSlidePower();
                }
                //if (gamepad1.x || gamepad1.y) {
                //  liftState = LiftState.MANUAL;
                //}
                break;
            case MEDIUM:
                if (gamepad1.wasJustPressed(GamepadKeys.Button.RIGHT_BUMPER)) {
                    // code here
                    currentTarget = targetPositionHigh;
                    liftState = LiftState.HIGH;
                } else if (gamepad1.wasJustPressed(GamepadKeys.Button.LEFT_BUMPER)) {
                    // code here
                    currentTarget = targetPositionRest;
                    liftState = LiftState.REST;
                } else {
                    setSlidePower();
                }
                //if (gamepad1.x || gamepad1.y) {
                //   liftState = LiftState.MANUAL;
                //}
                break;
            case HIGH:
                // DO SOMETHING TO MAKE DRIVING SLOWER WHILE THE CASE IS HIGH FOR BETTER CONTROL
                if (gamepad1.wasJustPressed(GamepadKeys.Button.LEFT_BUMPER)) {
                    // code here
                    currentTarget = targetPositionRest;
                    liftState = LiftState.REST;
                } else {
                    setSlidePower();
                }
                if (rtReader1.isDown()) {
                    autoIn = true;
                }
                //if (gamepad1.x || gamepad1.y) {
                //   liftState = LiftState.MANUAL;
                //}
                break;

                /*
            case MANUAL:
                // Backup Controls

                // Viper Slides
                if (gamepad1.y && !gamepad1.x) {
                    currentTarget += 1;
                } else if (gamepad1.x && !gamepad1.y) {
                    currentTarget -= 1;
                }

                if ((gamepad1.wasJustPressed(GamepadKeys.Button.RIGHT_BUMPER)) && (currentPosition < 300)) {
                    currentTarget = targetPositionLow;
                    liftState = LiftState.LOW;
                    setSlidePower();
                } else if ((gamepad1.wasJustPressed(GamepadKeys.Button.RIGHT_BUMPER)) && (currentPosition >= 300 && currentPosition < 350)) {
                    currentTarget = targetPositionMedium;
                    liftState = LiftState.MEDIUM;
                    setSlidePower();
                } else if ((gamepad1.wasJustPressed(GamepadKeys.Button.RIGHT_BUMPER)) && (currentPosition >= 350 && currentPosition < 2500)) {
                    currentTarget = targetPositionHigh;
                    liftState = LiftState.HIGH;
                    setSlidePower();
                }
                if (gamepad1.wasJustPressed(GamepadKeys.Button.LEFT_BUMPER)) {
                    // code here
                    currentTarget = targetPositionRest;
                    liftState = LiftState.REST;
                    setSlidePower();
                }
                break;
                 */
        }
        // update gamepad state
        gamepad1.readButtons();
        runToPosition(currentTarget, currentPower);
        runIntake(gamepad1);
        //runIntake();
    }


    /**
     * start or stop intake, depending upon the current state
     *
     *  steady state and transient state
     *  https://resources.pcb.cadence.com/blog/2020-steady-state-vs-transient-state-in-system-design-and-stability-analysis
     */
    @Override
    public void runIntake(GamepadEx controller){
        TriggerReader rtReader = new TriggerReader(controller, GamepadKeys.Trigger.LEFT_TRIGGER);
        switch (intakeState) {
            case STOP:
                setIntakePower(IntakeState.STOP);   // intake.setPower(0);
                if (rtReader.isDown() || controller.isDown(GamepadKeys.Button.B)) {
                    intakeState = IntakeState.OUT;
                }
                if (controller.isDown(GamepadKeys.Button.A) || autoIn) {
                    intakeState = IntakeState.IN;
                }
                break;
            case IN:
                if (!controller.getButton(GamepadKeys.Button.A) && !autoIn) {
                    intakeState = IntakeState.STOP;
                } else if (rtReader.isDown() || controller.getButton(GamepadKeys.Button.B)) {
                    autoIn = false;
                    intakeState = IntakeState.STOP;
                }
                setIntakePower(IntakeState.IN);   // intake.setPower(-1);
                break;
            case OUT:
                if (!rtReader.isDown() || !controller.getButton(GamepadKeys.Button.B)) {
                    intakeState = IntakeState.STOP;
                }
                setIntakePower(IntakeState.OUT);   //  intake.setPower(1);
                break;
        }
    }
//    private boolean onPress(boolean ButtonState, String ButtonName) {
//        switch (ButtonName) {
//            case "RT":
//                 // ButtonState = true when right trigger is pressed i.e. trigger > 0
//                 // ButtonState = false when trigger is not pressed i.e. trigger = 0
//                if (ButtonState && !pressedLastIterationRT) {
//                    return true;
//                }
//                pressedLastIterationRT = ButtonState;
//                break;
//            case "RB":
//                if (ButtonState && !pressedLastIterationRB) {
//                    return true;
//                }
//                break;
//            case "LB":
//                if (ButtonState && !pressedLastIterationLB) {
//                    return true;
//                }
//                break;
//            case "DU":
//                if (ButtonState && !pressedLastIterationDU) {
//                    return true;
//                }
//                break;
//        }
//        return false;
//    }
//    private boolean onRelease(boolean ButtonState, String ButtonName) {
//        switch (ButtonName) {
//            case "RT":
//                if (!ButtonState && pressedLastIterationRT) {
//                    return true;
//                }
//                pressedLastIterationRT = ButtonState;
//                break;
//            case "RB":
//                if (!ButtonState && pressedLastIterationRB) {
//                    return true;
//                }
//                break;
//            case "LB":
//                if (!ButtonState && pressedLastIterationLB) {
//                    return true;
//                }
//                break;
//            case "DU":
//                if (!ButtonState && pressedLastIterationDU) {
//                    return true;
//                }
//                break;
//        }
//        return false;
//    }
}