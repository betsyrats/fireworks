
/**
 * Picture with AWT Shapes ASSIGNMENT
 *
 * PROJECT DESCRIPTION:
 * Create a Java program that creates a drawing
 * JPanel window made up of shapes from the AWT Graphics (or better) class
 * 
 * PROJECT:
 * - animation of fireworks!
 * - a lovely couple watching the fireworks
 * - I made this because I thought it was semi-close to the New Year
 * - and fireworks are pretty :)
 *
 * @Author Betty Wu, CS11
 * @Version 1.0, 11.18.25 to 12.01.25
 */

// packages imported
import java.awt.Graphics; // for drawing basic shapes
import java.awt.Graphics2D; // for more complex graphics
import java.awt.Color; // coloring shapes
import java.awt.GradientPaint; // color gradients in silhouettes and rim lights
import java.awt.BasicStroke; // changing the stroke width
import java.awt.geom.Path2D; // for drawing custom shapes
import java.awt.RenderingHints; // smoother graphics

import java.util.ArrayList; // for spark storage

import javax.swing.JFrame; // creates application window
import javax.swing.JPanel; // the panel/canvas
import javax.swing.Timer; // for animation timer


public class FireworkArt {
    // JFrame dimension constants
    final int PANEL_WIDTH = 900; // width of the drawing area
    final int PANEL_HEIGHT = 500; // height of the drawing area

    /** 
     * MyJPanel class:
     * - internal class with overrided paintComponent method to draw the graphics
     */
    class MyJPanel extends JPanel {
        // frame counter used to animate sparks that increases every 33ms
        private int frameCount = 0;

        // one spark list per firework so each firework has its own particles
        private ArrayList<Spark> greenSparks = new ArrayList<>();
        private ArrayList<Spark> pinkSparks = new ArrayList<>();
        private ArrayList<Spark> yellowSparks = new ArrayList<>();

        /** 
         * constructor for the MyJPanel class:
         * - starts a timer for the animation, repainting paintComponent
         */
        public MyJPanel() {
            super();

            // animation swing timer ~30 fps, lambda expression for action listener
            Timer timer = new Timer(33, e -> {
                frameCount++;
                repaint(); // triggers paintComponent
            });
            timer.start(); // start timer
        } // end of MyJPanel() constructor

        
        /**
         * Spark class:
         * - a single particle in a firework explosion.
         * - each spark stores position, velocity, color, lifespan, and trail history
         */
        private class Spark {
            // current position
            double x;
            double y;

            // velocity that adjusts position each frame
            double xVelocity;
            double yVelocity;

            // gravity pulling sparks downward each frame
            double gravity = 0.25;

            // lifespan of a spark defined in constructor
            double life;

            // spark color rgb
            int red;
            int green;
            int blue;

            // how long the trail of the spark is
            static final int trailLength = 20;

            // arrays holding previous positions where trailX[0] is oldest, trailX[trailLength-1] is newest
            double[] trailX = new double[trailLength];
            double[] trailY = new double[trailLength];

            /**
             * constructor of Spark class:
             * - creates a spark at (x,y) shooting outward with (xVelocity, yVelocity)
             * - initializes variables
             */
            Spark(double x, double y, double xVelocity, double yVelocity, int red, int green, int blue) {
                this.x = x;
                this.y = y;
                this.xVelocity = xVelocity;
                this.yVelocity = yVelocity;
                
                this.life = 120; // set lifespan to be 120; if decrement by 2, life = 60 frames

                this.red = red;
                this.green = green;
                this.blue = blue;

                // initialize the whole trail to the starting point
                for (int i = 0; i < trailLength; i++) {
                    trailX[i] = x;
                    trailY[i] = y;
                }
            } // end of Spark class constructor

            /**
             * trailUpdate():
             * - shifts trail positions down one index and stores current position at the end
             * - creates a trailing line behind the spark
             */
            void trailUpdate() {
                for (int i = 0; i < trailLength - 1; i++) {
                    trailX[i] = trailX[i+1];
                    trailY[i] = trailY[i+1];
                }
                trailX[trailLength-1] = x;
                trailY[trailLength-1] = y;
            } // end of trailUpdate() method

            /**
             * sparkUpdate():
             * - applies cool physics, moves spark, and fades it out!
             */
            void sparkUpdate() {
                trailUpdate(); // save current position into trail

                // sparks slow down slightly each frame like air resistance
                xVelocity*=0.985;
                yVelocity*=0.985;

                yVelocity+=gravity; // pulls sparks downward

                // move according to velocity
                x+=xVelocity;
                y+=yVelocity;

                life-=2; // lifespan decreases so spark fades out over time
            } // end of sparkUpdate() method

            /**
             * isDead():
             * - returns true when spark is done/"dead" and should be removed
             * - this is so we don't keep updating invisible sparks, reducing lag
             */
            boolean isDead() {
                return life <= 0; // returns true when the spark's life is over
            }
        } // end of Spark class

        
        /**
         * fireworkSparks():
         * - creates and animates sparks that explode in place
         *
         * params:
         * - x, y, width, height: the size and position constants of the firework
         * - red, green, blue: base firework color
         * - position: "left", "right", or "center" which affects where the burst center is
         */
        private void fireworkSparks(Graphics2D g2, int x, int y, int width, int height, int red, int green, int blue, String position) {
            // fixed offset frames so fireworks appear at different times
            final int greenOffset = 45;
            final int pinkOffset = 55;
            final int yellowOffset = 75;

            // number of frames per full explosion cycle
            final int cycleFrames = 80;

            // variables to be assigned based on the color
            double maxRadius; // controls how big the sparks can travel
            ArrayList<Spark> sparkList; // the list of sparks
            int offset; // different timing
            
            // choose spark list, how big the firework, and offset based on color
            if (green > red && green >= blue) {
                sparkList = greenSparks; // select the spark list
                offset = greenOffset; // select the offset
                maxRadius = width*0.5; // adjust the burst size

                // brighten the spark colors
                red = 180;
                green = 220;
                blue = 190;
            } // green firework
            else if (blue > green && red > green) {
                sparkList = pinkSparks;
                offset = pinkOffset;
                maxRadius = width*0.5;

                red = 240;
                green = 180;
                blue = 240;
            } // pink/purple firework
            else {
                sparkList = yellowSparks;
                offset = yellowOffset;
                maxRadius = width*0.4;

                red = 240;
                green = 220;
                blue = 130;
            } // yellow/orange firework

            // progress of one explosion cycle; (the frame + offset) % cycleFrames = 0-79
            double timePhase = (frameCount+offset) % cycleFrames;

            // find center of firework based on the position
            double centerX, centerY;
            if ("left".equals(position)) {
                centerX = x + width*0.35;
                centerY = y + height*0.35;
            } 
            else if ("right".equals(position)) {
                centerX = x + width*0.62;
                centerY = y + width*0.38;
            } 
            else {
                centerX = x + width*0.50;
                centerY = y + height*0.50;
            }

            /**
             * create a new firework explosion at the start of each cycle and only if old sparks are gone
             * - timePhase <= 2 in case of floating point error
             */
            if (timePhase<=2 && sparkList.isEmpty()) {
                // how many sparks relative to size, capping at 120 or else very laggy :(
                int sparkCount = Math.min((int)(35 + maxRadius/10), 120);

                // cool mathy physics for a more realistic spark path; use Math.random for more organic look
                for (int i = 0; i < sparkCount; i++) {
                    // direction of spark
                    double angle = Math.random()*2*Math.PI;
                    
                    // initial speed outward (scales with size)
                    double speed = (1 + Math.random()*1.5)*(maxRadius/55.0);

                    // vertical and horizontal velocities
                    double xVelocity = Math.cos(angle)*speed;
                    double yVelocity = Math.sin(angle)*speed;

                    // add the new spark to this firework's list
                    sparkList.add(new Spark(centerX, centerY, xVelocity, yVelocity, red, green, blue));
                }
            }

            /**
             * update and draw all sparks
             * - walk backwards so removing items doesn't skip anything
             */
            for (int i = sparkList.size()-1; i>=0; i--) {
                Spark spark = sparkList.get(i); // get one spark from the list
                spark.sparkUpdate(); // update the position
                
                // if a spark's lifespan is over, remove it
                if (spark.isDead()) {
                    sparkList.remove(i);
                    continue;
                }

                // draw tapered trail using stored positions
                int trailLength = Spark.trailLength;

                // loop through all the trail segments
                for (int j = 0; j<trailLength-1; j++) {
                    // how far along the trail goes from 0 (tail) to 1 (head)
                    double pos = j/(double)(trailLength-1);

                    // adjust the opacity so the tail fades out
                    float alpha = (float)(pos*spark.life/160.0);

                    // stroke width that changes to create a taper
                    float strokeWidth = (float)(0.5 + 5*pos);
                    
                    // adjust the coordinates of the line
                    int x1 = (int)spark.trailX[j];
                    int y1 = (int)spark.trailY[j];
                    int x2 = (int)spark.trailX[j+1];
                    int y2 = (int)spark.trailY[j+1];
                    
                    // set the thickness to strokeWidth
                    g2.setStroke(new BasicStroke(strokeWidth));
                    // uncomment this out for round sparks; laggier
                    //g2.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.setColor(new Color(255, 255, 255, (int)(alpha*255))); // set spark color to white
                    g2.drawLine(x1, y1, x2, y2); // drawing the bright spark
                    
                    // make the glow thickness bigger than the spark
                    g2.setStroke(new BasicStroke(strokeWidth*3));
                    // uncomment this out for round glow; laggier
                    //g2.setStroke(new BasicStroke(strokeWidth*3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.setColor(new Color(spark.red, spark.green, spark.blue, (int)(alpha*50))); // set glow color
                    g2.drawLine(x1, y1, x2, y2); // drawing a soft glow
                }
            }
        } // end of fireworkSparks() method

        
        /**
         * drawFirework():
         * - draws a glow behind sparks using ovals,
         * - then calls fireworkSparks to animate particles on top
         */
        public void drawFirework(Graphics2D g2, int x, int y, int width, int height, int red, int green, int blue, String position) {
            int alpha; // opacity of glow rings
            int redMultiplier; // how fast red brightens inward
            int greenMultiplier; // how fast green brightens inward
            int blueMultiplier; // how fast blue brightens inward
            int ringCount; // number of glow rings

            // choose glow style by color
            if (green > red && green >= blue) {
                alpha = 10;
                redMultiplier = 6;
                greenMultiplier = 8;
                blueMultiplier = 10;
                ringCount = 19;

            } // green/turqoise
            else if (blue > green && red > green) {
                alpha = 18;
                redMultiplier = 7;
                greenMultiplier = 4;
                blueMultiplier = 4;
                ringCount = 23;

            } // pink/purple
            else {
                alpha = 15;
                redMultiplier = 5;
                greenMultiplier = 7;
                blueMultiplier = 5;
                ringCount = 30;
            } // yellow

            // draw glow rings from biggest to smallest, getting brighter
            for (int i = 0; i < ringCount; i++) {
                // set the colors, capping at 255
                int ringRed = Math.min((red + i*redMultiplier), 255);
                int ringGreen = Math.min((green + i*greenMultiplier), 255);
                int ringBlue = Math.min((blue + i*blueMultiplier), 255);

                g2.setColor(new Color(ringRed, ringGreen, ringBlue, alpha)); // set the color of the ring
                int shrink = i*6; // amount the circle shrinks by

                // change the concentration of glow based on position param
                if ("right".equals(position)) {
                    g2.fillOval(x+shrink*2, y+shrink, width-shrink*3, height-shrink*3);
                } 
                else if ("center".equals(position)) {
                    g2.fillOval(x+shrink*2, y+shrink*2, width-shrink*4, height-shrink*4);
                } 
                else {
                    g2.fillOval(x+shrink, y+shrink, width-shrink*3, height-shrink*3);
                }
            }

            // draw animated sparks on top
            fireworkSparks(g2, x, y, width, height, red, green, blue, position);
        } // end of drawFirework() method

        
        /**
         * paintComponent:
         * - main drawing method called every repaint()
         * - overrides JPanel's paintComponent() method to draw
         */
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g); // ensure the backgruond will have the main content pane's color
            Graphics2D g2 = (Graphics2D) g; // cast the graphics to Graphics2D for more complex shapes
    
            // render the graphics smoother
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            /**
             * FIREWORKS
             * - three fireworks - yellow (right), pink/purple (left), green (center)
             */
            
            // CONSTANTS
            // small firework (green)
            int smallFWWidth = 480; // how wide
            int smallFWHeight = smallFWWidth; // how tall; same as width
            int smallFWX = PANEL_WIDTH - (smallFWWidth+370); // x position, middle
            int smallFWY = 200; // y position, a bit out of frame
        
            // medium firework (pink)
            int medFWWidth = 450; // how wide
            int medFWHeight = medFWWidth; // same as width
            int medFWX = -50; // x position, left
            int medFWY = -50; // y position, top
        
            // large firework (yellow)
            int largeFWWidth = 700; // how wide (quite big)
            int largeFWHeight = largeFWWidth; // same as width
            int largeFWX = PANEL_WIDTH - largeFWWidth+100; // x position, right
            int largeFWY = PANEL_HEIGHT - largeFWHeight; // y position, top
        
            // draw 3 fireworks, using g2 and the constants we set!
            drawFirework(g2, smallFWX, smallFWY, smallFWWidth, smallFWHeight, 100, 125, 100, "center");
            drawFirework(g2, medFWX, medFWY, medFWWidth, medFWHeight, 180, 110, 140, "left");
            drawFirework(g2, largeFWX, largeFWY, largeFWWidth, largeFWHeight, 185, 100, 67, "right");

            /**
             * SILHOUETTES
             * - fence in front as a dark silhouette with rim light from fireworks
             * - a couple watching the fireworks
             * - drawn using Path2D and a LOT of trial and error
             */

            // silhouette colors for gradients, used for both fence and couple
            Color shadow1 = new Color(30, 20, 25); // darker color for fence, rgb
            Color shadow2 = new Color(60, 35, 30); // lighter color for fence, rgb
            Color light1 = new Color(200, 120, 190, 60); // purple-ish light from fireworks, rgba
            Color light2 = new Color(240, 200, 120, 50); // warm yellow light from fireworks, rgba

            /**
             * FENCE
             * - slightly angled
             */
            // start and end coordinates of the fence
            int x1 = -4; // left start, slightly offset so the glow of the outline is not shown
            int y1 = PANEL_HEIGHT*3/5; // base y for top fence rail
            int x2 = PANEL_WIDTH+4; // right end, slightly offset for same reason
            int y2 = y1+50; // end y for top fence rail
            int gap = 100; // gap between the two horizontal rails, used for y coordinate

            // draw the horizontal parts of the fence
            Path2D.Double fenceRails = new Path2D.Double();
            // lower rail
            fenceRails.moveTo(x1, y1+gap+15);
            fenceRails.lineTo(x1, y1+gap);
            fenceRails.lineTo(x2, y2+gap);
            fenceRails.lineTo(x2, y2+gap+15);
            // upper rail
            fenceRails.moveTo(x1, y1+15);
            fenceRails.lineTo(x1, y1);
            fenceRails.lineTo(x2, y2);
            fenceRails.lineTo(x2, y2+15);
            fenceRails.closePath(); // we have stopped drawing the horizontal rails

            // draw the vertical parts of the fence (supports)
            Path2D.Double fenceSupports = new Path2D.Double();
            // left vertical support
            fenceSupports.moveTo(x2/5, y1-15);
            fenceSupports.curveTo(x2/5, y1-15, x2/5+7, y1-18, x2/5+15, y1-14);
            fenceSupports.lineTo(x2/5-10, PANEL_HEIGHT+1);
            fenceSupports.lineTo(x2/5-25, PANEL_HEIGHT+1);
            // right vertical support
            fenceSupports.moveTo(x2*4/5+15, y1+20);
            fenceSupports.curveTo(x2*4/5+15, y1+20, x2*4/5+7, y1+17, x2*4/5, y1+20);
            fenceSupports.lineTo(x2*4/5, PANEL_HEIGHT+1);
            fenceSupports.lineTo(x2*4/5+15, PANEL_HEIGHT+1);
            // center vertical support
            fenceSupports.moveTo(x2/2, y1+5);
            fenceSupports.curveTo(x2/2, y1+5, x2/2+7, y1+2, x2/2+15, y1+6);
            fenceSupports.lineTo(x2/2+5, PANEL_HEIGHT+1);
            fenceSupports.lineTo(x2/2-10, PANEL_HEIGHT+1);
            fenceSupports.closePath(); // we have stopped drawing the vertical supports

            // overlay polygons to hide awkward overlaps
            Path2D.Double overlay = new Path2D.Double();
            // left top overlay
            overlay.moveTo(x2/5-10, y1+17);
            overlay.lineTo(x2/5+4, y1+2);
            overlay.lineTo(x2/5+18, y1+19);
            overlay.lineTo(x2/5+2, y1+36);
            // left bottom overlay
            overlay.moveTo(x2/5-19, y2+gap-35);
            overlay.lineTo(x2/5-6, y2+gap-50);
            overlay.lineTo(x2/5+7, y2+gap-33);
            overlay.lineTo(x2/5-8, y2+gap-20);
            // center top overlay
            overlay.moveTo(x2/2-8, y1+32);
            overlay.lineTo(x2/2+7, y1+17);
            overlay.lineTo(x2/2+20, y1+34);
            overlay.lineTo(x2/2+5, y1+49);
            // center bottom overlay
            overlay.moveTo(x2/2-13, y2+gap-17);
            overlay.lineTo(x2/2+2, y2+gap-35);
            overlay.lineTo(x2/2+16, y2+gap-15);
            overlay.lineTo(x2/2, y2+gap-1);
            // right top overlay
            overlay.moveTo(x2*4/5-5, y1+49);
            overlay.lineTo(x2*4/5+7, y1+65);
            overlay.lineTo(x2*4/5+23, y1+48);
            overlay.lineTo(x2*4/5+5, y1+30);
            // right bottom overlay
            overlay.moveTo(x2*4/5-7, y2+gap-2);
            overlay.lineTo(x2*4/5+7, y2+gap-20);
            overlay.lineTo(x2*4/5+21, y2+gap-3);
            overlay.lineTo(x2*4/5+5, y2+gap+12);
            overlay.closePath(); // we have stopped drawing the overlays

            // fill in the color of the fence, dark silhouette
            g2.setPaint(new GradientPaint(x1, y1+50, shadow1, x2, y2, shadow2, false));
            g2.fill(fenceRails);
            g2.fill(fenceSupports);

            // rim light from the fireworks on the fence
            g2.setPaint(new GradientPaint(x1, y1+50, light1, x2, y2, light2, false));
            g2.setStroke(new BasicStroke(6)); // make the stroke wider for a noticeable glow
            g2.draw(fenceRails);
            g2.draw(fenceSupports);

            // thinner lighter rim line on top for extra glow!
            g2.setPaint(new GradientPaint(x1, y1+50, new Color(120, 145, 120, 180), x2, y2, new Color(250, 210, 160, 230), false));
            g2.setStroke(new BasicStroke(1)); // set stroke back to 1
            g2.draw(fenceRails);
            g2.draw(fenceSupports);

            // fill overlay to hide awkward edges
            g2.setPaint(new GradientPaint(x1, y1+50, shadow1, x2, y2, shadow2, false));
            g2.fill(overlay);
            
            /**
             * COUPLE SILHOUETTE
             * - two people watching the spectacle happen!
             * - how romantic!
             */
            // some base coordinates
            int baseX = PANEL_WIDTH*5/8+20;
            int baseY = PANEL_HEIGHT*2/3;
            int baseY2 = baseY-100;
            int baseX2 = baseX+100;
            
            // draw the couple
            Path2D.Double couple = new Path2D.Double();
            couple.moveTo(baseX, PANEL_HEIGHT); // start at the bottom
            // first, draw the man
            couple.lineTo(baseX, baseY+20); // pants
            couple.lineTo(baseX-3, baseY+15); // shirt
            couple.curveTo(baseX-5, baseY, baseX+4, baseY-10, baseX+2, baseY-35);
            couple.curveTo(baseX-5, baseY-15, baseX-15, baseY+5, baseX-40, baseY); // arm
            couple.curveTo(baseX-45, baseY+3, baseX-60, baseY-5, baseX-30, baseY-15);
            couple.lineTo(baseX-22, baseY-30);
            couple.curveTo(baseX-15, baseY2-13, baseX-15, baseY2, baseX+18, baseY2-13);
            couple.lineTo(baseX+18, baseY2-18); // neck
            couple.curveTo(baseX+14, baseY2-22, baseX+14, baseY2-15, baseX+7, baseY2-32); // head
            couple.curveTo(baseX+7, baseY2-32, baseX-2, baseY2-43, baseX+5, baseY2-50);
            couple.curveTo(baseX+5, baseY2-60, baseX-12, baseY2-66, baseX+30, baseY2-70);
            couple.curveTo(baseX+55, baseY2-78, baseX+45, baseY2-25, baseX+35, baseY2-18);
            couple.lineTo(baseX+35, baseY2-13); // neck again
            couple.lineTo(baseX+70, baseY2);
            // now onto the woman!
            couple.lineTo(baseX+70, baseY2-10); // neck
            couple.lineTo(baseX+63, baseY2-11);
            couple.curveTo(baseX+63, baseY2-11, baseX+58, baseY2-20, baseX+65, baseY2-37); // face
            couple.lineTo(baseX+63, baseY2-39); // hair
            couple.curveTo(baseX+50, baseY2-39, baseX2, baseY2-80, baseX2+5, baseY2-30);
            couple.lineTo(baseX2-2, baseY2+12);
            couple.curveTo(baseX2-2, baseY2+12, baseX2+5, baseY2+25, baseX2-6, baseY2+30); // man's arm
            couple.lineTo(baseX2-2, baseY-42);
            couple.lineTo(baseX2-6, baseY-42);
            couple.curveTo(baseX2-5, baseY-35, baseX2-20, baseY-25, baseX2-1, baseY-5); // shirt
            couple.lineTo(baseX2-5, baseY);
            couple.curveTo(baseX2-5, baseY, baseX2+10, baseY+32, baseX2+5, baseY+60); // skirt
            couple.lineTo(baseX2-2, baseY+62);
            couple.curveTo(baseX2, baseY+100, baseX2-20, baseY+100, baseX2+10, PANEL_HEIGHT); // legs
            couple.lineTo(baseX2-30, PANEL_HEIGHT);
            couple.lineTo(baseX2-40, baseY+58);
            couple.lineTo(baseX2-48, PANEL_HEIGHT);
            couple.closePath(); // we're done drawing the couple :)
            
            // fill in the couple, setting the gradient
            g2.setPaint(new GradientPaint(baseX-100, PANEL_HEIGHT, shadow1, x2, y2, shadow2, false));
            g2.fill(couple);
            
            // rim light from the fireworks
            g2.setPaint(new GradientPaint(baseX-60, y1, new Color(0,0,0,0), baseX2+5, baseY2-30, light2, false));
            g2.setStroke(new BasicStroke(6)); // thicker for noticeable glow
            g2.draw(couple);
            
            // extra rim light (sharper)
            g2.setPaint(new GradientPaint(baseX-60, y1, new Color(220, 245, 220, 50), baseX2+5, baseY2-30, new Color(255, 255, 255, 200), false));
            g2.setStroke(new BasicStroke(1)); // set back to 1
            g2.draw(couple);
        } // end of the paintComponent() method
    } // end of internal MyJPanel class
    
    
    /** 
     * FireworkArt constructor:
     * - sets up window + panel
     */
    public FireworkArt() {
        JFrame theWindow = new JFrame("Watching the Fireworks"); // create the window
        theWindow.setSize(PANEL_WIDTH, PANEL_HEIGHT); // set the dimensions of the window
        theWindow.setResizable(false); // make sure users cannot resize the window
        theWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // exit when user presses the close button

        MyJPanel artPanel = new MyJPanel(); // create instance of MyJPanel
        artPanel.setOpaque(true); // allow the user to see the panel
        artPanel.setBackground(new Color(87, 67, 76)); // set the background to a dark night sky color
        theWindow.setContentPane(artPanel); // set it to be the main content pane

        theWindow.setVisible(true); // set the window to be visible
    } // end of FireworkArt() constructor method

    
    /**
     * main() method:
     * - calls the FireworkArt() class
     * - leads to creation of window with the graphics
     */
    public static void main(String[] args) {
        new FireworkArt();
    } // end of program main() method
} // end of FireworkArt class
