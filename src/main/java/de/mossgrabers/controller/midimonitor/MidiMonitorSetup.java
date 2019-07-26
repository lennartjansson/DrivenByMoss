// Written by Jürgen Moßgraber - mossgrabers.de
// (c) 2017-2019
// Licensed under LGPLv3 - http://www.gnu.org/licenses/lgpl-3.0.txt

package de.mossgrabers.controller.midimonitor;

import de.mossgrabers.controller.midimonitor.controller.MidiMonitorControlSurface;
import de.mossgrabers.framework.configuration.ISettingsUI;
import de.mossgrabers.framework.controller.AbstractControllerSetup;
import de.mossgrabers.framework.controller.IControlSurface;
import de.mossgrabers.framework.controller.ISetupFactory;
import de.mossgrabers.framework.daw.IHost;
import de.mossgrabers.framework.daw.midi.IMidiAccess;
import de.mossgrabers.framework.daw.midi.IMidiInput;
import de.mossgrabers.framework.mode.Modes;


/**
 * A simple Midi Monitor, which logs to the console.
 *
 * @author J&uuml;rgen Mo&szlig;graber
 */
public class MidiMonitorSetup extends AbstractControllerSetup<IControlSurface<MidiMonitorConfiguration>, MidiMonitorConfiguration>
{
    /**
     * Constructor.
     *
     * @param host The DAW host
     * @param factory The factory
     * @param globalSettings The global settings
     * @param documentSettings The document (project) specific settings
     */
    public MidiMonitorSetup (final IHost host, final ISetupFactory factory, final ISettingsUI globalSettings, final ISettingsUI documentSettings)
    {
        super (factory, host, globalSettings, documentSettings);

        this.configuration = new MidiMonitorConfiguration (host, null);
    }


    /** {@inheritDoc} */
    @Override
    public void flush ()
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    protected void createModel ()
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    protected void createSurface ()
    {
        final IMidiAccess midiAccess = this.factory.createMidiAccess ();
        final IMidiInput input = midiAccess.createInput ("Midi Monitor");
        this.surfaces.add (new MidiMonitorControlSurface (this.host, this.configuration, input));
    }


    /** {@inheritDoc} */
    @Override
    public void startup ()
    {
        // Intentionally empty
    }


    /** {@inheritDoc} */
    @Override
    protected void updateIndication (final Modes mode)
    {
        // Intentionally empty
    }
}
