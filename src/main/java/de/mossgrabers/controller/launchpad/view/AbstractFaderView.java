// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2020
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.launchpad.view;

import de.mossgrabers.controller.launchpad.controller.LaunchpadColorManager;
import de.mossgrabers.controller.launchpad.controller.LaunchpadControlSurface;
import de.mossgrabers.framework.controller.ButtonID;
import de.mossgrabers.framework.daw.DAWColor;
import de.mossgrabers.framework.daw.IModel;
import de.mossgrabers.framework.daw.data.ITrack;

import java.util.Arrays;


/**
 * Abstract base class for views with 8 faders.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public abstract class AbstractFaderView extends SessionView
{
    private static final int    PAD_VALUE_AMOUNT     = 16;

    protected final int []      trackColors          = new int [8];

    private final int []        faderMoveDelay       = new int [8];
    private final int []        faderMoveTimerDelay  = new int [8];
    private final int []        faderMoveDestination = new int [8];

    // @formatter:off
    private static final int [] SPEED_SCALE          =
    {
        1,   1,  1,  1,  1,  1,  1,  1,
        1,   1,  1,  1,  1,  1,  1,  1,
        1,   1,  1,  1,  1,  1,  1,  1,
        2,   2,  2,  2,  2,  2,  2,  2,
        2,   2,  2,  2,  2,  2,  2,  2,
        2,   2,  2,  2,  2,  2,  2,  2,
        3,   3,  3,  3,  3,  3,  3,  3,
        3,   3,  3,  3,  3,  3,  3,  3,

        4,   4,  4,  4,  4,  4,  4,  4,
        5,   5,  5,  5,  5,  5,  5,  5,
        6,   6,  6,  6,  7,  7,  7,  7,
        8,   8,  8,  8,  9,  9,  9,  9,
        10, 10, 10, 10, 11, 11, 11, 11,
        12, 12, 12, 12, 13, 13, 13, 13,
        14, 14, 15, 15, 16, 16, 17, 17,
        18, 19, 20, 21, 22, 23, 24, 25
    };
    // @formatter:on


    /**
     * Constructor.
     *
     * @param surface The surface
     * @param model The model
     */
    public AbstractFaderView (final LaunchpadControlSurface surface, final IModel model)
    {
        super (surface, model);

        Arrays.fill (this.trackColors, -1);
    }


    /**
     * A knob has been used (simulated fader).
     *
     * @param index The index of the knob
     * @param value The value the knob sent
     */
    public abstract void onValueKnob (int index, int value);


    /** {@inheritDoc} */
    @Override
    public void onGridNote (final int note, final int velocity)
    {
        if (velocity == 0)
            return;

        // Simulate faders
        final int num = note - 36;
        final int index = num % 8;
        final int row = num / 8;

        // About 3 seconds on softest velocity
        this.faderMoveDelay[index] = SPEED_SCALE[velocity];
        this.faderMoveTimerDelay[index] = SPEED_SCALE[SPEED_SCALE.length - 1 - velocity];

        final int min = row * PAD_VALUE_AMOUNT;
        final int max = Math.min (127, (row + 1) * PAD_VALUE_AMOUNT - 1);
        int newDestination = this.smoothFaderValue (index, row, max);

        // Support stepping through 4 values
        if (min <= this.faderMoveDestination[index] && this.faderMoveDestination[index] <= max)
        {
            final int step = (this.faderMoveDestination[index] - min) / 4;
            newDestination = min + 4 * (step + 2) - 1;
            if (newDestination > max)
                newDestination = min;
        }
        else if (row == 0)
        {
            newDestination = 0;
        }

        this.faderMoveDestination[index] = newDestination;

        this.moveFaderToDestination (index);
    }


    protected void moveFaderToDestination (final int index)
    {
        final int current = this.getFaderValue (index);
        if (current < this.faderMoveDestination[index])
            this.onValueKnob (index, Math.min (current + this.faderMoveDelay[index], this.faderMoveDestination[index]));
        else if (current > this.faderMoveDestination[index])
            this.onValueKnob (index, Math.max (current - this.faderMoveDelay[index], this.faderMoveDestination[index]));
        else
            return;

        this.surface.scheduleTask ( () -> this.moveFaderToDestination (index), this.faderMoveTimerDelay[index]);
    }


    /**
     * Special handling of pads for smoothing the fader, e.g. special handling of 1st row.
     *
     * @param index The fader index
     * @param row The row of the pressed pad
     * @param value The calculated value
     * @return The smoothed value
     */
    protected int smoothFaderValue (final int index, final int row, final int value)
    {
        final int oldValue = this.getFaderValue (index);
        if (row == 0)
            return oldValue == 0 ? PAD_VALUE_AMOUNT - 1 : 0;
        return value;
    }


    /**
     * Hook for getting the fader value.
     *
     * @param index The index of the fader
     * @return The fader value
     */
    protected abstract int getFaderValue (int index);


    /** {@inheritDoc} */
    @Override
    public void onActivate ()
    {
        Arrays.fill (this.trackColors, -1);

        super.onActivate ();

        this.surface.clearFaders ();
        for (int i = 0; i < 8; i++)
            this.setupFader (i);
    }


    /** {@inheritDoc} */
    @Override
    public int getButtonColor (final ButtonID buttonID)
    {
        return LaunchpadColorManager.LAUNCHPAD_COLOR_BLACK;
    }


    /**
     * Setup a virtual pad fader with the color of the track with the same index.
     *
     * @param index The index of the fader
     */
    public void setupFader (final int index)
    {
        final ITrack track = this.model.getCurrentTrackBank ().getItem (index);
        final int color = this.colorManager.getColorIndex (DAWColor.getColorIndex (track.getColor ()));
        this.surface.setupFader (index, color, false);
    }
}