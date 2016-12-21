package org.openhab.binding.rfxcom.internal.messages;

import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.HOME_CONFORT;

import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComMessageNotImplementedException;

public class RFXComHomeConfortTest {
    @Test(expected = RFXComMessageNotImplementedException.class)
    public void checkNotImplemented() throws Exception {
        RFXComMessageFactory.createMessage(HOME_CONFORT);
    }
}
