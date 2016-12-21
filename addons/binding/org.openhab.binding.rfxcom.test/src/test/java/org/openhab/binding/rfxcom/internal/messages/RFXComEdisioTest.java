package org.openhab.binding.rfxcom.internal.messages;

import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComMessageNotImplementedException;

import static org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType.EDISIO;

public class RFXComEdisioTest {
    @Test(expected = RFXComMessageNotImplementedException.class)
    public void checkNotImplemented() throws Exception {
        RFXComMessageFactory.createMessage(EDISIO);
    }
}
