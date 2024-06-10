package org.lei.opi.jovp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.lei.opi.core.CameraStreamer;

import es.optocom.jovp.PsychoEngine;
import es.optocom.jovp.PsychoLogic;
import es.optocom.jovp.Timer;
import es.optocom.jovp.rendering.Item;
import es.optocom.jovp.rendering.Model;
import es.optocom.jovp.rendering.Observer;
import es.optocom.jovp.rendering.Texture;
import es.optocom.jovp.definitions.Command;
import es.optocom.jovp.definitions.ViewEye;
import es.optocom.jovp.definitions.ModelType;
import es.optocom.jovp.definitions.TextureType;
import es.optocom.jovp.definitions.Units;

/**
 * Logic for the PsychoEngine
 *
 * @since 0.0.1
 */
public class OpiLogic implements PsychoLogic {

    /** {@value DEFAULT_BACKGROUND_SHAPE} */
    private static final ModelType DEFAULT_BACKGROUND_SHAPE = ModelType.CIRCLE;
    /** {@value DEFAULT_FIXATION_SHAPE} */
    private static final ModelType DEFAULT_FIXATION_SHAPE = ModelType.MALTESE;
    /** {@value DEFAULT_STIMULUS_SHAPE} */
    private static final ModelType DEFAULT_STIMULUS_SHAPE = ModelType.CIRCLE;
    /** {@value DEFAULT_FIXATION_SIZE} */
    private static final int DEFAULT_FIXATION_SIZE = 1;
    /** {@value MINIMUM_TIME_FROM_ONSET} */
    private static final double MINIMUM_TIME_FROM_ONSET = 80;

    /** Depth of background from screen */
    private static float BACK_DEPTH = Observer.ZFAR - 1;
    /** Depth of stimuli from screen */
    private static float STIM_DEPTH = Observer.ZFAR - 2;
    /** Depth of fixation from screen */
    private static float FIX_DEPTH = Observer.ZFAR - 3;

    /** The OPI driver */
    private final OpiJovp driver;

    /** Always 2 backgrounds, but second is unused for MONO */
    private Item[] backgrounds;
    /** Always 2 fixations, but second is unused for MONO */
    private Item[] fixations;
    /** The stim on the screen. 
      * Assumes that stimulus[0] and stimulus[1,2,...] (if it exists) are will be shown for the same duration
    */
    private List<Item> stimulusItems;

    /** Current stim being shown */
    private List<Stimulus> currentStim;

    /** The current index into driver.getStimulus() for presentation. This steps along the list of stimuli in driver. */
    private int stimIndex = -1;

    /** PsychoEngine field of view */
    private float[] fov;

    /** A timer to control, well, you know, er, time? */
    private Timer timer = new Timer();
    /** Accumulates presentation time: useful for dynamic stimulus */
    private int presentationTime;

    /** Keep the start and end times of presentation for looking up camera information */
    private long startStimTimeStamp, buttonPressTimeStamp;

    OpiLogic(OpiJovp driver) {
        this.driver = driver;
    }

    /**
     * Initialize PsychoEngine
     *
     * @param psychoEngine the psychoEngine
     * 
     * @since 0.0.1
     */
    @Override
    public void init(PsychoEngine psychoEngine) {
        backgrounds = new Item[] {
            new Item(new Model(DEFAULT_BACKGROUND_SHAPE), new Texture()),
            new Item(new Model(DEFAULT_BACKGROUND_SHAPE), new Texture())
        };
        fixations = new Item[] {
            new Item(new Model(DEFAULT_FIXATION_SHAPE), new Texture()),
            new Item(new Model(DEFAULT_FIXATION_SHAPE), new Texture())
        };

        backgrounds[0].show(ViewEye.LEFT);
        backgrounds[1].show(ViewEye.RIGHT);
        fixations[0].show(ViewEye.LEFT);
        fixations[1].show(ViewEye.RIGHT);

        // set size of the background to be the field of view
        this.fov = psychoEngine.getFieldOfView();

        // add perimetry items: background, fixation, and stimulus.
        for (int i = 0; i < backgrounds.length; i++) {
            fixations[i].position(0.0d, 0.0d);
            backgrounds[i].position(0.0d, 0.0d);

            fixations[i].size(DEFAULT_FIXATION_SIZE);
            backgrounds[i].size(fov[0], fov[1]);    // TODO: Will this work for Images?

            fixations[i].depth(FIX_DEPTH);
            backgrounds[i].depth(BACK_DEPTH);

            view.add(fixations[i]);
            view.add(backgrounds[i]);
        }

        driver.setActionToNull(); // Action is over
    }

    /**
     * Process input
     *
     * @param command the command received  
     * 
     * @since 0.0.1
     */
    @Override
    public void input(PsychoEngine psychoEngine, Command command) {
        if (command == Command.NONE) return;
        // if no response or response is too early, do nothing
        if(command != Command.YES || timer.getElapsedTime() < MINIMUM_TIME_FROM_ONSET) return;
        buildResponse(true);
        timer.stop();
        for (Item s : stimulusItems) 
            s.show(ViewEye.NONE);
    }

    /**
     * Update the psychoEngine input
     *
     * @param psychoEngine the psychoEngine
     * 
     * @since 0.0.1
     */
    @Override
    public void update(PsychoEngine psychoEngine) {
        // Instructions are always given by the OpiDriver.
        // OpiLogic sets action back to null once instruction is carried out,
        // except when presenting, where the OpiDriver waits for a response.
        //if (driver.action != null) System.out.println(driver.action);
        if (driver.getAction() == null) 
            checkAction();
        else switch(driver.getAction()) {
            case SHOW -> show(psychoEngine);
            case SETUP -> setup();
            case PRESENT -> present();
            case CLOSE -> {
              psychoEngine.finish();
              System.exit(0);
            }
        }
    }

    /** Show psychoEngine */
    private void show(PsychoEngine psychoEngine) {
      psychoEngine.show();
      driver.setActionToNull();
    }

    /** Change background and/or fixation markers 
     * Don't update models or textures if we can avoid it.
     */  
    private void setup() {
        for (int i = 0; i < backgrounds.length; i++) {
            Setup input_bg = driver.getBackgrounds()[i];
            if (input_bg != null) {
                double bgLum = input_bg.bgLum();
                double[] bgCol = input_bg.bgCol();
                backgrounds[i].setColors(gammaLumToColor(bgLum, bgCol), gammaLumToColor(bgLum, bgCol));

                if (input_bg.bgImageFilename().length() > 0) {    // a bit yuck, but rgen needs a default value...
                    backgrounds[i].update(new Texture(input_bg.bgImageFilename()));
                } else {
                    if (backgrounds[i].getTexture().getType() != TextureType.FLAT)
                        backgrounds[i].update(new Texture(TextureType.FLAT, gammaLumToColor(bgLum, bgCol), gammaLumToColor(bgLum, bgCol)));
                }

                    // Update fixation[i] if we need to
                if (fixations[i].getModel().getType() != input_bg.fixShape())
                    fixations[i].update(new Model(input_bg.fixShape()));

                if (input_bg.fixType() == TextureType.IMAGE) {
                    fixations[i].update(new Texture(input_bg.fixImageFilename()));
                } else {
                    if (fixations[i].getTexture().getType() != input_bg.fixType())
                        fixations[i].update(new Texture(input_bg.fixType()));
                    fixations[i].setColor(gammaLumToColor(input_bg.fixLum(), input_bg.fixCol()));
                }

                fixations[i].position(input_bg.fixCx(), input_bg.fixCy());
                fixations[i].size(input_bg.fixSx(), input_bg.fixSy());
                fixations[i].rotation(input_bg.fixRotation());
            }
        }
        driver.setActionToNull();
    }

    /** Present stimulus upon request */
    private void present() {
        if (currentStim == null || stimulusItems == null) {
            currentStim = new ArrayList<Stimulus>();
            stimulusItems = new ArrayList<Item>();
        } 
        stimIndex = 0;  // the first element in the stimulus list
        updateStimuli();
        timer.start();
        presentationTime = 0;
        driver.setActionToNull();
    }

    /** Checks if something must be updated, e.g. if presenting a stimulus or processing the observer's response */
    private void checkAction() {
        if (timer.getElapsedTime() > 0) // if timer is active, we are presenting
            if (stimulusItems.get(0).showing()) {
                double t = currentStim.get(currentStim.size() - 1).t();
                if (timer.getElapsedTime() > presentationTime + t) {
                    presentationTime += t;
                    // If presentation time is over for the last element of the array, then hide stimulus
                    // and ask for eye position from camera
                    if (stimIndex == driver.getStimuliLength() - 1) {
                        buttonPressTimeStamp = System.currentTimeMillis();

                        for (Item s : stimulusItems)
                            s.show(ViewEye.NONE);

                        requestEyePosition(currentStim.get(currentStim.size() - 1).eye(), buttonPressTimeStamp);
                    }
                    else {
                        stimIndex++;
                        updateStimuli();
                    }
                }
            } else if (timer.getElapsedTime() > currentStim.get(currentStim.size() - 1).w()) {
                // if no response, reset timer and send negative response
                timer.stop();
                buildResponse(false);
            };
    }

    /** Create a new item from Stimulus stim */
    private Item createStimItem(Stimulus stim) {
        Model m;
        if (stim.shape() == ModelType.OPTOTYPE)
            m = new Model(stim.optotype());  // give it the optotype
        else
            m = new Model(stim.shape());

        Texture t;
        if (stim.type() == TextureType.IMAGE)
            t = new Texture(stim.imageFilename());  // give it the string filename
        else
            t = new Texture(stim.type());  

            // units is always in ANGLES for now
        return new Item(m, t, Units.ANGLES);
    }

    /** Create stims from the list driver.getStimulus[stimIndex, ...]
     * Elements are taken from driver while t == 0 (ie duration is 0)
     * All taken elements are put into currentStim list.
     * For each one, an Item is created and put into stimulusItems list.
    */
    private void createStimuli() {
        currentStim.clear();
        stimulusItems.clear();

        Stimulus stim = driver.getStimulus(stimIndex);
        for(;;) {
            currentStim.add(stim);

            Item stimItem = createStimItem(stim);

            stimItem.position(stim.x(), stim.y());
            if (stim.fullFoV() != 0) {
                stimItem.size(this.fov[0], this.fov[1]);
            } else {
                stimItem.size(stim.sx(), stim.sy());
            }
            stimItem.rotation(stim.rotation());
            stimItem.contrast(stim.contrast());
            stimItem.frequency(stim.phase(), stim.frequency());
            stimItem.defocus(stim.defocus());
            stimItem.texRotation(stim.texRotation());
            stimItem.setColors(gammaLumToColor(stim.lum(), stim.color1()), gammaLumToColor(stim.lum(), stim.color2()));
            stimItem.envelope(stim.envType(), stim.envSdx(), stim.envSdy(), stim.envRotation());
            stimItem.show(stim.eye());

            stimItem.depth(STIM_DEPTH);
            view.add(stimItem);

            stimulusItems.add(stimItem);

            if (stim.t() == 0) {
                stimIndex++;
                stim = driver.getStimulus(stimIndex);
            } else {
                break;
            }
        }
    }

    /** 
     * Request details of eye position from the camera(s)
     * Response should end up on driver.getConfiguration().webcam().cameraStreamer.responseQueue
     * 
     * @param eye One of ViewEye.LEFT, ViewEye.RIGHT, or ViewEye.BOTH
     * @param timestamp Stamp of the request like System.getCurrentTimeMillis()
     */
    private void requestEyePosition(ViewEye eye, long timestamp) {
        CameraStreamer.Request req;
        if (currentStim.size() == 1) {
            if (eye == ViewEye.LEFT) 
                req = new CameraStreamer.Request(timestamp, driver.getConfiguration().webcam().srcDeviceLeft);
            else 
                req = new CameraStreamer.Request(timestamp, driver.getConfiguration().webcam().srcDeviceRight);
        } else 
            req = new CameraStreamer.Request(timestamp, 0);  // 0 for all cameras
        driver.getConfiguration().webcam().cameraStreamer.requestQueue.add(req);
    }

    /** Update stimulusItems to match the next section of driver.getStimulus(index).
     * Try and reuse existing Items as much as possible.
      * Only create new Items if the stim has new components (ie t == 0)
      * Only create new Models or Textures in existing Items if really needed
    */
    private void updateStimuli() {
        if (stimulusItems.size() == 0) { // first time, create the lot
            createStimuli();
        
            startStimTimeStamp = System.currentTimeMillis();
            requestEyePosition(currentStim.get(0).eye(), startStimTimeStamp);
        }

            // Check each driver.getStimulus(stimIndex) against currentStim[itemIndex] to see if
            //   (a) It exists (ie new stim has more items than currentStim)
            //   (a) OR it should not exist (ie is first or pre t == 0)
            //   (b) OR the model or texture should be updated
            // ASSERT stimulusItems.len >= currentStim
        int itemIndex = 0; // index into stimulusItems (and the prefix of currentStim)
        for(;;) {
            Stimulus stim = driver.getStimulus(stimIndex);
            
            if (itemIndex >= stimulusItems.size()) {
                stimulusItems.add(createStimItem(stim));
            } else {
                boolean newModel = false;
                int newTexture = 0;  // 0 = no new texture, 1 = new texture, 2 = update image of existing texture

                if (itemIndex >= currentStim.size()) { // there is no previous stim available
                    newModel = true;
                    newTexture = 1;
                } else {
                    Stimulus prev = currentStim.get(itemIndex);
                    newModel = stim.shape() != prev.shape();
                    newModel |= stim.shape() == ModelType.OPTOTYPE && prev.shape() == ModelType.OPTOTYPE && !stim.optotype().equals(prev.optotype());

                    if (stim.type() != prev.type())
                        newTexture = 1;
                    else if (stim.type() == TextureType.IMAGE && prev.type() == TextureType.IMAGE && !stim.imageFilename().equals(prev.imageFilename()))
                        newTexture = 2;
                }

                if (newModel)
                    if (stim.shape() == ModelType.OPTOTYPE)
                        stimulusItems.get(itemIndex).update(new Model(stim.optotype()));  // give it the optotype
                    else
                        stimulusItems.get(itemIndex).update(new Model(stim.shape()));

                if (newTexture == 1) {
                    if (stim.type() == TextureType.IMAGE) 
                        stimulusItems.get(itemIndex).update(new Texture(stim.imageFilename()));  // new, string filename
                    else
                        stimulusItems.get(itemIndex).update(new Texture(stim.type()));  
                } else if (newTexture == 2) {
                    Texture t = stimulusItems.get(itemIndex).getTexture();
                    t.updateImage(stim.imageFilename());     // update the texture
                    stimulusItems.get(itemIndex).update(t);  // trigger update of the Item
                }

                stimulusItems.get(itemIndex).position(stim.x(), stim.y());
                if (stim.fullFoV() != 0) {
                    stimulusItems.get(itemIndex).size(this.fov[0], this.fov[1]);
                } else {
                    stimulusItems.get(itemIndex).size(stim.sx(), stim.sy());
                }
                stimulusItems.get(itemIndex).rotation(stim.rotation());
                stimulusItems.get(itemIndex).contrast(stim.contrast());
                stimulusItems.get(itemIndex).frequency(stim.phase(), stim.frequency());
                stimulusItems.get(itemIndex).defocus(stim.defocus());
                stimulusItems.get(itemIndex).texRotation(stim.texRotation());
                stimulusItems.get(itemIndex).setColors(gammaLumToColor(stim.lum(), stim.color1()), gammaLumToColor(stim.lum(), stim.color2()));
                stimulusItems.get(itemIndex).envelope(stim.envType(), stim.envSdx(), stim.envSdy(), stim.envRotation());
                stimulusItems.get(itemIndex).show(stim.eye());
            }

                // record the new currentStim
            if (itemIndex >= currentStim.size())
                currentStim.add(stim);
            else
                currentStim.set(itemIndex, stim);

                // see if we need some more items
            if (stim.t() == 0) {
                itemIndex++;  // local step through stimulusItems and currentStim
                stimIndex++;  // global step through driver
            } else 
                break;
        }

        // Any excess in stimulusItems or currentStim are left (with View.NONE) for later use.
    }
                
    /** 
     * Apply inverse gamma to convert cd/m^2 to RGB in [0, 1]
     * @param luminance [0]=R [1]=G [2]=B cd/m^2
     * */
    private double[] gammaLumToColor(double[] luminance) {
      return driver.getConfiguration().calibration().getColorValues(luminance);
    }

      // multiply luminance * color and then convert 
    private double[] gammaLumToColor(double luminance, double[] color) {
      double lum[] = Arrays.stream(color).map((double c) -> c * luminance).toArray();
      return gammaLumToColor(lum);
    }

    /**
     * Set the driver.response after getting the relevant eye positions 
     * from the camera(s) response queues.
     */
    private void buildResponse(boolean seen) {
        CameraStreamer.Response resp = null;
        Response result = new Response(seen, timer.getElapsedTime());

        int oneTryTime = 50;  // 50 ms
        int totalTries = 10 * 1000 / oneTryTime;  // 10 seconds
        int count = 0;

            // Keep looking for start and end responses until either we get 
            // the wrong response (mis-matching timestamp) or no response
            // too many times (totalTries)
        try {
            int gotData = 0;
            while (gotData < 2) {
                while (resp == null && count < totalTries) {
                    resp = driver.getConfiguration().webcam().cameraStreamer.responseQueue.poll(oneTryTime, TimeUnit.MILLISECONDS);
                    count++;
                }

                if (resp == null) {
                    System.out.println(String.format("No response from camera queue after %s seconds", totalTries * oneTryTime / 1000));
                    break;
                }

                if (resp.requestTimeStamp() == startStimTimeStamp) {
                    result.updateEye(true, resp.x(), resp.y(), resp.diameter(), (int)(resp.acquisitionTimeStamp() - startStimTimeStamp));
                    gotData++;
                } else if (resp.requestTimeStamp() == buttonPressTimeStamp) {
                    result.updateEye(false, resp.x(), resp.y(), resp.diameter(), (int)(resp.acquisitionTimeStamp() - startStimTimeStamp));
                    gotData++;
                } else 
                    driver.getConfiguration().webcam().cameraStreamer.responseQueue.put(resp);  // put it back for someone else
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        driver.setResponse(result);
    }
}
