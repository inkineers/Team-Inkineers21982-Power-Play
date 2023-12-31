package org.firstinspires.ftc.teamcode.drive.opmode.pp;

import com.acmerobotics.roadrunner.geometry.Pose2d;

public class JunctionPoses {
//    // left side junctions
//    public Pose2d BottomHigh = new Pose2d(0, -12,  Math.toRadians(90));
//    public Pose2d LeftHigh = new Pose2d(-24, -12, Math.toRadians(-90));
//    public Pose2d LeftMedium = new Pose2d(-24, -12, Math.toRadians(90));
//    // right side junctions
//    public Pose2d RightHigh = new Pose2d(24, -12, Math.toRadians(-90));
//    public Pose2d RightMedium = new Pose2d(24, -12, Math.toRadians(90));

    // misc
    public Pose2d LeftConeStack = new Pose2d(-54,-12, Math.toRadians(0));
    public Pose2d RightConeStack = new Pose2d(54,-12, Math.toRadians(180));
//    public Pose2d LeftStart = new Pose2d(-34, -62, Math.toRadians(90));
//    public Pose2d RightStart = new Pose2d(34, -62, Math.toRadians(90));

    // single right side quadrant
    public Pose2d Start = new Pose2d(34, -62, Math.toRadians(90));
    public Pose2d Medium = new Pose2d(24, -12, Math.toRadians(90));
    public Pose2d High = new Pose2d(24, -12, Math.toRadians(-90));
    public Pose2d BottomHigh = new Pose2d(0, -12,  Math.toRadians(90));
}
